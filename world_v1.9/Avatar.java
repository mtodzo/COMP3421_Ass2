package unsw.graphics.world;

import java.awt.Color;
import java.io.IOException;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame3D;
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
	
	private TriangleMesh bunny;
	private Texture AvatarTexture;
	
	private float x;
	private float z;
	private float y;
	
    public Avatar(float x, float y, float z) {
    	position = new Point3D(x, y, z);
    }
	
    public void init(GL3 gl) {
    	try{
    		bunny = new TriangleMesh("res/models/bunny.ply", true, true);
        } catch (IOException i) {
        	System.out.println("res/models/tree.ply file is not found");
        	i.printStackTrace();
        }
    	AvatarTexture = new Texture(gl, "res/textures/BrightPurpleMarble.png", "png", true);
    	
    	bunny.init(gl);
    	
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
    
    public void display(GL3 gl, float rotate) {
//    	CoordFrame3D frame = CoordFrame3D.identity()
//    			.scale(5,5,5);
    	
		CoordFrame3D modelFrame = CoordFrame3D.identity()

				.rotateY(-180-rotate)
				.translate(-position.getX(), -position.getY(), -position.getZ() + 1.5f)
				.scale(5,5,5);
//    			.rotateY(180)
//    			.translate(position.getX(),position.getY(), position.getZ());
		
		bunny.draw(gl, modelFrame);
//		System.out.println(modelFrame.getMatrix());
		System.out.println("position:  " + position.getX() +"   "+ position.getY()+"   "+ position.getZ() + "\n" + modelFrame.getMatrix());
		
    }
    
    public void update(float x, float y, float z) {
    	this.position = new Point3D(x, y, z);
    }
    
    public void destroy(GL3 gl) {
    	bunny.destroy(gl);
    }
    
}