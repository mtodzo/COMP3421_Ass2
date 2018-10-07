package unsw.graphics.world;

import java.io.IOException;

import com.jogamp.opengl.GL3;

import unsw.graphics.Application3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

/*
 * Avatar is a cute bunny, moving by key
 * 
 * 
 */
public class Avatar{
	private Point3D position;
	private float x = 1 ;
	private float y = -1;
	private float z = 4 ;
	
	private TriangleMesh bunny;
	private Texture texture;
	
	private boolean third_person = false;
	
	
	public Avatar() throws IOException {
		bunny = new TriangleMesh("res/models/bunny.ply", true, true);
	}
	

    public void init(GL3 gl) {
        bunny.init(gl);
        
        texture = new Texture(gl, "res/textures/BrightPurpleMarble.png", "png", false);
        
        Shader shader = new Shader(gl, "shaders/vertex_phong.glsl", "shaders/fragment_phong.glsl");
        shader.use(gl);
        
	}
    
    public void display(GL3 gl, Terrain terrain) {
    	if (third_person) {
    		position = new Point3D(x, y, z);
    		// camera should be behind the bunny
    		y = - terrain.altitude(-x, -z) -1;
    		
    		
    		
    	}
    }
}