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
    
    private Texture textureGrass;
    private TriangleMesh mesh;
    
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
     * Non-integer points should be interpolated from neighboring grid points
     * 
     * @param x
     * @param z
     * @return
     */
    public float altitude(float x, float z) {
        float altitude = 0;
        // TODO: Implement this
        
        //Return zero if object outside of the boundaries
        /**
         * (x_0, z_1)		(x_1,z_1)
         * 		+------+
         * 		-     +-
         *		-   +  -
         *		- +    -
         *		+------+
         *  (x_0, z_0)		(x_1,z_0) 
         * 
         */
        if(x < 0 || x > width || z < 0 || z > depth) {
        	return 0.0f;
        }
        
        //get remainder of non-integer points
        double x_r = x - (int)x;
        double z_r = z - (int)z;
        
        int x_0 = (int)x; 
        int x_1 = (int)x;
        int z_0 = (int)z;
        int z_1 = (int)z;
        
        if(x - x_0 != 0) {
        	x_1 ++;
        }
        if(z - z_0 != 0) {
        	z_1 ++;
        }
        
        double h_0 = getGridAltitude(x_0, z_0);
        double h_1 = getGridAltitude(x_1, z_0);
        double h_2 = getGridAltitude(x_0, z_1);
        double h_3 = getGridAltitude(x_1, z_1);
        
        double a_0 = (1 - x_r)*h_0;
        double a_1 = x_r * h_1;
        double a_2 = (1 - x_r)*h_2;
        double a_3 = x_r * h_3;
        
        altitude = (float)((1 - z_r)*(a_0 + a_1) + z_r * (a_2 + a_3));
//        System.out.println("altitude:  " + altitude);
        
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
       

    public void draw(GL3 gl) {
    	textureGrass = new Texture(gl, "res/textures/grass.bmp","bmp", true);

//    	shader =  new Shader(gl, "shaders/vertex_tex_3d.glsl","shaders/fragment_tex_3d.glsl");
//    	shader.use(gl);
//    	
    	Shader.setPenColor(gl, Color.red);
    	Shader.setInt(gl, "tex", 0);
    	
//    	gl.glEnable(GL.GL_TEXTURE_2D);
    	gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE);
    	gl.glLineWidth(6.0f);
    	
    	gl.glActiveTexture(GL.GL_TEXTURE0);
    	gl.glBindTexture(GL.GL_TEXTURE_2D, textureGrass.getId());
    	
    	List<Point3D> grassVerts = new ArrayList<>();
    	List<Point3D> triangleVerts = new ArrayList<>(); 
//        List<Point2D> grassTexCoords = new ArrayList<>();
        List<Integer> grassIndices = Arrays.asList(0,1,2, 0,3,2);
        
        for(int x = 0; x < width - 1; x ++) {
        	for(int z = 0; z < depth - 1; z ++) {
        		Point3D p_0 = new Point3D(x, altitudes[x][z], z);
        		Point3D p_1 = new Point3D(x, altitudes[x][z+1], z+1);
        		Point3D p_2 = new Point3D(x+1, altitudes[x+1][z+1], z+1);
        		Point3D p_3 = new Point3D(x+1, altitudes[x+1][z], z);
        		
//	     		grassVerts.add(new Point3D(x, altitudes[x][z], z));			//P0
//	     		grassVerts.add(new Point3D(x, altitudes[x][z+1], z+1));		//P1
//	     		grassVerts.add(new Point3D(x+1, altitudes[x+1][z+1], z+1));	//P2
//	     		grassVerts.add(new Point3D(x+1, altitudes[x+1][z], z));;	//P3
//	     		
//	            grassTexCoords.add(new Point2D(0, 0));
//	            grassTexCoords.add(new Point2D(0, 1));
//	            grassTexCoords.add(new Point2D(1, 0));
//	            grassTexCoords.add(new Point2D(1, 1));
	                 
	     		triangleVerts.add(p_0);
	     		triangleVerts.add(p_1);
	     		triangleVerts.add(p_2);
	     		
	     		triangleVerts.add(p_0);
	     		triangleVerts.add(p_2);
	     		triangleVerts.add(p_3);
	     		
	     		
//	            System.out.println("grassVerts:" + grassVerts);
	     	}
	     }
//        System.out.println("grassVerts:" + grassVerts.size());
        
        
        mesh = new TriangleMesh(triangleVerts, true);
        
        mesh.init(gl);
        mesh.draw(gl);
//    	gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE);

    }


}
