package unsw.graphics.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    private int width;
    private int depth;
    private float[][] altitudes;
    private List<Tree> trees;
    private List<Road> roads;
    private Vector3 sunlight;

    private TriangleMesh terrainMesh;
    private List<TriangleMesh> meshes =  new ArrayList<>();
    
    private Shader shader;
    
    private Texture terrainTexture;
    
    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth, Vector3 sunlight) {
        this.width = width;
        this.depth = depth;
        altitudes = new float[width][depth];
        trees = new ArrayList<Tree>();
        roads = new ArrayList<Road>();
        this.sunlight = sunlight;
    }

    public List<Tree> trees() {
        return trees;
    }

    public List<Road> roads() {
        return roads;
    }

    public Vector3 getSunlight() {
        return sunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        sunlight = new Vector3(dx, dy, dz);      
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return altitudes[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, float h) {
        altitudes[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     * 
     * @param x
     * @param z
     * @return
     */
    public float altitude(float x, float z) {
        float altitude = 0;
        // TODO: Implement this
        
        /**
         *     P2			   P3
         * (x_0, z_1)		(x_1,z_1)
         * 			+------+
         * 			|     +|
         *			|   +  |
         *			| +    |
         *			+------+
         *  (x_0, z_0)		(x_1,z_0) 
         * 		P0			   P1
         */
        
        //Return zero if object outside of the boundaries
        if(x < 0 || x > width || z < 0 || z > depth) {
        	return 0;
        }
        
        
        //get remainder of non-integer points
        double x_r = x - (int)x;
        double z_r = z - (int)z;
        
        int x_0 = (int)x; 
        int z_0 = (int)z;
        
        if (x - x_0 != 0 && z - z_0 != 0) {
        	int x_1 = x_0 +1;
        	double a_0 = getGridAltitude(x_0, z_0);
        	double a_1 = getGridAltitude(x_1, z_0);
        	altitude = (float)((1 - x_r)*a_0 + x_r * a_1);
        } else if (x - x_0 != 0) {
        	int x_1 = x_0 +1;
        	double a_0 = getGridAltitude(x_0, z_0);
        	double a_1 = getGridAltitude(x_1, z_0);
        	altitude = (float)((1 - x_r)*a_0 + x_r * a_1);
        } else if(z - z_0 != 0) {
        	int z_1 = z_0 +1;
        	double a_0 = getGridAltitude(x_0, z_0);
        	double a_1 = getGridAltitude(x_0, z_1);
        	altitude = (float)((1 - z_r)*a_0 + z_r * a_1);
        } else {
        	altitude = (float) getGridAltitude(x_0, z_0);
        }
        
//        double a_0 = (1 - x_r)*getGridAltitude(x_0, z_0) + x_r * getGridAltitude(x_1, z_0);;
//        double a_1 = (1 - x_r)* getGridAltitude(x_0, z_1) + x_r * getGridAltitude(x_1, z_1);        
//        altitude = (float)((1 - z_r)*a_0 + z_r * a_1);
        
//        System.out.println("myNormals: " + myNormals[0][2]);
        
        return altitude;
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(float x, float z) {
        float y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        trees.add(tree);
    }


    /**
     * Add a road. 
     * 
     * @param x
     * @param z
     */
    public void addRoad(float width, List<Point2D> spine) {
        Road road = new Road(width, spine);
        roads.add(road);        
    }
    
    private void initTerainStrips(GL3 gl) {
    	
    	terrainTexture = new Texture(gl, "res/textures/grass.bmp", "bmp", true);
    	
        // Define points, indices and texture coordinates
        boolean first;
        
        for(int x = 0; x < width - 1; x ++) {
            List<Point3D> points = new ArrayList<>();
            List<Integer> indices = new ArrayList<>();
            List<Point2D> textCoords = new ArrayList<>();
            first = true;
            
        	for(int z = 0; z < depth; z ++) {
        		
        		// Add points
        		points.add(new Point3D(x, (float) getGridAltitude(x,z), z));
        		points.add(new Point3D(x+1, (float) getGridAltitude(x+1,z), z));
        		
        		//Add texture Coordinates
        		textCoords.add(new Point2D(z,0));
        		textCoords.add(new Point2D(z,1));
        		
        		//Set up indices between current row and subsequent row. Ignore on first pass
        		if (!first) {
        			//Top left triangle
	        		indices.add(2*z-2);
	        		indices.add(2*z);
	        		indices.add(2*z-1);
	        		
	        		//Bottom right triangle
	        		indices.add(2*z-1);
	        		indices.add(2*z);
	        		indices.add(2*z+1);
        		} else {
        			first = false;
        		}
        		
	     	}

            TriangleMesh segment = new TriangleMesh(points, indices, true, textCoords);
            //TriangleMesh segment = new TriangleMesh(points, indices, true);
            segment.init(gl);
            meshes.add(segment);
	     }

    }

    private void initTerain(GL3 gl) {
    	
    	terrainTexture = new Texture(gl, "res/textures/grass.bmp", "bmp", true);
    	
        // Define points, indices and texture coordinates
        boolean firstRow = true;
        boolean firstColumn = true;
        
        List<Point3D> points = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Point2D> textCoords = new ArrayList<>();
        
        for(int x = 0; x < width; x ++) {
            firstRow = true;
            
        	for(int z = 0; z < depth; z ++) {
        		
        		// Add points
        		points.add(new Point3D(x, (float) getGridAltitude(x,z), z));
        		
        		//Add texture Coordinates
        		textCoords.add(new Point2D(x,z));
        		
        		//Set up indices between current row and subsequent row. Ignore on first pass
        		if (!firstRow && !firstColumn) {
        			//Top left triangle
	        		indices.add( depth*(x-1) + (z-1) );
	        		indices.add( depth*(x-1) +  z    );
	        		indices.add( depth* x    + (z-1) );
	        		
	        		//Bottom right triangle
	        		indices.add( depth* x    + (z-1) );
	        		indices.add( depth*(x-1) +  z    );
	        		indices.add( depth* x    +  z    );
        		} else {
        			firstRow = false;
        		}
        		
	     	}
        	firstColumn = false;
	     }

        terrainMesh = new TriangleMesh(points, indices, true, textCoords);
        terrainMesh.init(gl);
        
    }
    
    public void init(GL3 gl) {

        shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
                "shaders/fragment_tex_phong.glsl");
        shader.use(gl);
        
        // Set the lighting properties (sunlight as the light source?)
        Shader.setPoint3D(gl, "lightPos", getSunlight().asPoint3D());
        Shader.setColor(gl, "lightIntensity", Color.WHITE);
        Shader.setColor(gl, "ambientIntensity", new Color(0.1f, 0.1f, 0.1f));
        
        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", Color.WHITE);
        Shader.setColor(gl, "diffuseCoeff", new Color(0.2f, 0.2f, 0.2f));
        Shader.setColor(gl, "specularCoeff", new Color(0.1f, 0.1f, 0.1f));
        Shader.setFloat(gl, "phongExp", 16f);
        
        initTerain(gl);
        
        for (Tree t : trees) {
            t.init(gl);
        }
    }   
        
    public void display(GL3 gl, float rotateX, float rotateY) {
        
    	Shader.setPenColor(gl, Color.WHITE);
        
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);

    	CoordFrame3D view = CoordFrame3D.identity().translate(-width/4, 0, -10).rotateX(rotateX).rotateY(rotateY);
//        Shader.setViewMatrix(gl, view.getMatrix());

    	drawTerrain(gl, view);
    	drawRoad(gl,view);
    	drawTrees(gl,view);
    	
    	}
    

    private void drawTerrain(GL3 gl, CoordFrame3D view) {
        
    	//Texture option
        gl.glBindTexture(GL.GL_TEXTURE_2D, terrainTexture.getId());
    	
        terrainMesh.draw(gl, view);
    }
    
    private void drawTrees(GL3 gl, CoordFrame3D view) {
    	// TODO
        for(Tree t : trees){
        	t.display(gl, view);
        }
    }
    
    private void drawRoad(GL3 gl, CoordFrame3D view) {
    	//TODO
    }
    
    public void destroy(GL3 gl) {
    	for (TriangleMesh m : meshes) {
            m.destroy(gl);
        }
    	for(Tree t : trees){
        	t.destroy(gl);
        }
//    	for(Road r : roads){
//        	r.destroy(gl);
//        }
    	
    	
    }
    
}