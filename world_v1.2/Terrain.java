package unsw.graphics.world;



import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
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

    private TriangleMesh mesh;
    private List<TriangleMesh> meshes =  new ArrayList<>();
    
    private Shader shader;
    
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
    
    private void initMesh(GL3 gl) {
    	boolean first;
        
        for(int x = 0; x < width - 1; x ++) {
            List<Point3D> points = new ArrayList<>();
            List<Integer> indices = new ArrayList<>();
            first = true;
            
        	for(int z = 0; z < depth; z ++) {
        		
        		points.add(new Point3D(x, (float) getGridAltitude(x,z), z));				//P0
        		points.add(new Point3D(x+1, (float) getGridAltitude(x+1,z), z));			//P1
        		
        		if (!first) {
        			
	        		indices.add(2*z-2);
	        		indices.add(2*z);
	        		indices.add(2*z-1);
	        		
	        		indices.add(2*z-1);
	        		indices.add(2*z);
	        		indices.add(2*z+1);
	        		
        		} else {
        			first = false;
        		}
        		
	     	}

            TriangleMesh segment = new TriangleMesh(points, indices, true);
            segment.init(gl);
            meshes.add(segment);
	     }

    }
    public void init(GL3 gl) {
        
    	System.out.println("Init");
    	
        Shader shader = new Shader(gl, "shaders/vertex_phong.glsl",
                "shaders/fragment_phong.glsl");
        shader.use(gl);
        
        // Set the lighting properties
        Shader.setPoint3D(gl, "lightPos", new Point3D(1, -10, 5));
        Shader.setColor(gl, "lightIntensity", Color.WHITE);
        Shader.setColor(gl, "ambientIntensity", new Color(0.2f, 0.2f, 0.2f));
        
        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", Color.WHITE);
        Shader.setColor(gl, "diffuseCoeff", new Color(0.5f, 0.5f, 0.5f));
        Shader.setColor(gl, "specularCoeff", new Color(0.8f, 0.8f, 0.8f));
        Shader.setFloat(gl, "phongExp", 16f);
        
        initMesh(gl);
    }   
        
    public void display(GL3 gl, float rotateX, float rotateY) {

    	CoordFrame3D view = CoordFrame3D.identity().translate(-width/2, 0, -10).rotateX(rotateX).rotateY(rotateY);
    	
        Shader.setPenColor(gl, Color.GREEN);
        // gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE);
        // gl.glCullFace(GL.GL_FRONT);
        // gl.glDisable(GL.GL_CULL_FACE);
        
        int i = 1;
        for (TriangleMesh m : meshes) {
        	if (i == 1) {
        		Shader.setPenColor(gl, Color.GREEN);
        		i++;
        	} else if (i == 2) {
        		Shader.setPenColor(gl, Color.RED);
        		i++;
        	} else if (i == 3) {
        		Shader.setPenColor(gl, Color.BLUE);
        		i++;
        	} else if (i == 4) {
        		Shader.setPenColor(gl, Color.YELLOW);
        		i++;
        	} else if (i == 5) {
        		Shader.setPenColor(gl, Color.MAGENTA);
        		i++;
        	}
            m.draw(gl, view);
        }
    }

    
    public void draw2(GL3 gl) {
    	Texture textureTerrain = new Texture(gl, "res/textures/grass.bmp","bmp", true);
//    	
//    	Shader.setPenColor(gl, Color.red);
//    	Shader.setInt(gl, "tex", 0);
//    	
////    	gl.glEnable(GL.GL_TEXTURE_2D);
//    	gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE);
////    	gl.glLineWidth(6.0f);
//    	
//    	gl.glActiveTexture(GL.GL_TEXTURE0);
//    	gl.glBindTexture(GL.GL_TEXTURE_2D, textureTerrain.getId());
    	
    	List<Point3D> TerrainVerts = new ArrayList<>();
//    	List<Vector3> TerrainNormals = new ArrayList<Vector3>();
        List<Point2D> TerrainTexCoords = new ArrayList<>();
        List<Integer> TerrainIndices = Arrays.asList(0,1,3, 0,3,2);
        boolean first = true;
        
        for(int x = 0; x < width - 1; x ++) {
            List<Point3D> points = new ArrayList<>();
            List<Integer> indices = new ArrayList<>();
            
        	for(int z = 0; z < depth - 1; z ++) {
        		
//        		TerrainVerts.add(new Point3D(x, altitudes[x][z], z));			//P0
//        		TerrainVerts.add(new Point3D(x, altitudes[x][z+1], z+1));		//P1
//        		TerrainVerts.add(new Point3D(x+1, altitudes[x+1][z], z));		//P2
//        		TerrainVerts.add(new Point3D(x+1, altitudes[x+1][z+1], z+1));;	//P3
/*	     		
        		//p0->p1->p3 ccw triangle
        		TerrainVerts.add(new Point3D(x, altitude(x,z), z));				//P0
        		TerrainVerts.add(new Point3D(x+1, altitude(x+1,z), z));			//P1
        		TerrainVerts.add(new Point3D(x+1, altitude(x+1,z+1), z+1));;	//P3
        		//p0->p3->p2 ccw triangle
        		TerrainVerts.add(new Point3D(x, altitude(x,z), z));				//P0
        		TerrainVerts.add(new Point3D(x+1, altitude(x+1,z+1), z+1));;	//P3
        		TerrainVerts.add(new Point3D(x, altitude(x,z+1), z+1));			//P2
	     		
        		TerrainTexCoords.add(new Point2D(0,0));
        		TerrainTexCoords.add(new Point2D(1,0));
        		TerrainTexCoords.add(new Point2D(1,1));
        		
        		TerrainTexCoords.add(new Point2D(0,0));
        		TerrainTexCoords.add(new Point2D(1,1));
        		TerrainTexCoords.add(new Point2D(0,1));
        		*/
        		
        		TerrainVerts.add(new Point3D(x, altitude(x,z), z));				//P0
        		TerrainVerts.add(new Point3D(x+1, altitude(x+1,z), z));			//P1
        		
/*                indices.add(2*j);
                indices.add(2*j + 1);
                indices.add(2*k + 1);
                
                indices.add(2*j);
                indices.add(2*k + 1);
                indices.add(2*k);
        		*/
        		if (!first) {
	        		indices.add(z-1);
	        		indices.add(z);
	        		indices.add(z+1);
	        		
	        		indices.add(z+1);
	        		indices.add(z);
	        		indices.add(z+2);
        		} else {
        			first = false;
        		}
/*        		mesh = new TriangleMesh(TerrainVerts, TerrainIndices, false, TerrainTexCoords);
                mesh.init(gl);
                */
	     	}
            TriangleMesh segment = new TriangleMesh(points, indices, true);
            segment.init(gl);
            meshes.add(segment);
	     }
//        System.out.println("TerrainVerts:" + TerrainVerts.size());
//        mesh = new TriangleMesh(TerrainVerts, true);
//        mesh.init(gl);
        
        Shader.setPenColor(gl, Color.red);
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureTerrain.getId());
        
        CoordFrame3D view = CoordFrame3D.identity().translate(0, 0, -2);
      
        mesh.draw(gl, view);
//    	gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE);

    }

}
