package unsw.graphics.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.scene.MathUtil;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

    private List<Point2D> points;
    private TriangleMesh roadMesh;
    private Texture RoadTexture;

    private float width;
    
    private TriangleMesh myRoad;
    private Terrain terrain;
    
    private static final float DIVISION_FACTOR = 0.0015f; //the higher this is the more accurate/smooth road is
    												  //but also increases computation cost
    private static final float ALTITUDE_OFFSET = 0.0015f; //to combat 'Z-fighting' of terrain and road
    
    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(float width, List<Point2D> spine) {
        this.width = width;
        this.points = spine;
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return width;
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return points.size() / 3;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public Point2D controlPoint(int i) {
        return points.get(i);
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public Point3D point(float t, float a) {
        int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 3;
        
        Point2D p0 = points.get(i++);
        Point2D p1 = points.get(i++);
        Point2D p2 = points.get(i++);
        Point2D p3 = points.get(i++);
        

        float x = b(0, t) * p0.getX() + b(1, t) * p1.getX() + b(2, t) * p2.getX() + b(3, t) * p3.getX();
        float z = b(0, t) * p0.getY() + b(1, t) * p1.getY() + b(2, t) * p2.getY() + b(3, t) * p3.getY();     
        
        float y = terrain.altitude(x, z) + 0.1f;
        
        return new Point3D(x, y, z);
    }
    
    public Point2D getTangent(float t) {
    	int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 3;
        
        Point2D p0 = points.get(i++);
        Point2D p1 = points.get(i++);
        Point2D p2 = points.get(i++);
        Point2D p3 = points.get(i++);
        
        // figure out my direction vector
        float x = bernsteinCoefficient(0, t) * (p1.getX() - p0.getX()) 
        		+ bernsteinCoefficient(1, t) * (p2.getX() - p1.getX())
        		+ bernsteinCoefficient(2, t) * (p3.getX() - p2.getX());
        
        float y = bernsteinCoefficient(0, t) * (p1.getY() - p0.getY()) 
        		+ bernsteinCoefficient(1, t) * (p2.getY() - p1.getY()) 
        		+ bernsteinCoefficient(2, t) * (p3.getY() - p2.getY());
       
    	return new Point2D(x,y);
    }
    /*
     * The coffecient using the bernstein formula for m = 2 and k = power
     * bernstein formula = C(m, k) * t^k * (1-k)^(m-k)
     */
    private float bernsteinCoefficient(int power, float value){
        switch (power){
            case 0:
                return (float) Math.pow(1 - value, 2);
            case 1:
                return 2 * (1 - value) * value;
            case 2:
                return (float) Math.pow(value, 2);
        }

        throw new IllegalArgumentException("" + value);
    }
    
    /**
     * Calculate the Bezier coefficients
     * 
     * @param i
     * @param t
     * @return
     */
    private float b(int i, float t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;
            
        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }
    

   
    private float[] getNormal(float i) {
    	// find tangent at that point
    	Point2D t = getTangent(i);
    	float magnitude = (float) Math.sqrt(Math.pow(t.getX(), 2) + Math.pow(t.getY(), 2));
		
    	float[] normalised = new float[] {-t.getY()/magnitude, t.getX()/magnitude};
		return normalised;
    }
  
    public void init(GL3 gl) {
    	
    	Shader shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
                "shaders/fragment_tex_phong.glsl");
        shader.use(gl);
        
        // Set the lighting properties
        Shader.setPoint3D(gl, "lightPos", new Point3D(1, -10, 5));
        Shader.setColor(gl, "lightIntensity", Color.WHITE);
        Shader.setColor(gl, "ambientIntensity", new Color(0.5f, 0.5f, 0.5f));
        
        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", Color.WHITE);
        Shader.setColor(gl, "diffuseCoeff", new Color(0.5f, 0.5f, 0.5f));
        Shader.setColor(gl, "specularCoeff", new Color(0.8f, 0.8f, 0.8f));
        Shader.setFloat(gl, "phongExp", 16f);  	
    }
    
    public void display(GL3 gl, int w, int d) {
    	List<Point2D> textCoords = new ArrayList<>();
    	List<Point3D> vertices = new ArrayList<>();
    	List<Integer> indices = new ArrayList<>();
    	
    	int count = 0;
 
    	
    	// change i to let road get more smooth
    	for (float i = 0; i < this.size(); i += 0.002) {
    		count += 1;
    		//get point on spine
    		Point3D spinePoint = new Point3D(i, w, d);
    		
    		
    		// find the normal and normalise it
    		float[] normalPoint = getNormal(i);
    		
    		//normal vector
    		normalPoint[0] *= this.width/2;
    		normalPoint[1] *= this.width/2;
    		
//    		System.out.println(normalPoint[0] +"   " + normalPoint[1] );
    		

    		//1st triangle
    		textCoords.add(new Point2D(spinePoint.getX() - normalPoint[0], spinePoint.getZ() - normalPoint[1]));
    		vertices.add(new Point3D(spinePoint.getX() - normalPoint[0], spinePoint.getY(), spinePoint.getZ() - normalPoint[1]));
    		
    		indices.add((int) (count));
    		indices.add((int) (count + 1));
    		indices.add((int) (count + 3));
    		
    		//2nd triangle
    		textCoords.add(new Point2D(spinePoint.getX() + normalPoint[0], spinePoint.getZ() + normalPoint[1]));
    		vertices.add(new Point3D(spinePoint.getX() + normalPoint[0], spinePoint.getY(), spinePoint.getZ() + normalPoint[1] ));
    		
       		indices.add((int) (count + 2));
    		indices.add((int) (count));
    		indices.add((int) (count + 3));
    	}
    	
    	roadMesh = new TriangleMesh(vertices,indices, true, textCoords);
    	roadMesh.init(gl);
    	
//    	System.out.println(vertices.size() +"   " + indices.size() );
    	roadMesh.draw(gl);
    	
    }

}
