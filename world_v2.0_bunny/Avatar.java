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
	private float rotate_Y = -135;

    private double stepDistance = 0.1;
	
	private TriangleMesh bunny;
	private Texture AvatarTexture;
	
    public Avatar() {
    	position = new Point3D(0,0,0);
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
    
    public void display(GL3 gl) {  	
		CoordFrame3D modelFrame = CoordFrame3D.identity()
				.rotateY(-rotate_Y)
				.translate(-0.5f,0.1f,0)
				.translate(-position.getX(), -position.getY() +0.3f, -position.getZ())
//				.rotateY(-rotate_Y)
				.scale(3,3,3);
//    			.translate(position.getX(),position.getY(), position.getZ());
		
		System.out.println("position:  " + position.getX() +"   "+ position.getY()+"   "+ position.getZ() + "\n" + modelFrame.getMatrix());
//		System.out.println("position:  " + position.getX() +"   "+ position.getY());
		
		bunny.draw(gl, modelFrame);
    }
    
    
    public void destroy(GL3 gl) {
    	bunny.destroy(gl);
    }
    
    public float getRotY() {
        return rotate_Y;
    }

    public float getX() {
        return position.getX();
    }

    public float getY() {
        return position.getY();
    }

    public float getZ() {
        return position.getZ();
    }

    public void rotate(float r) {
    	rotate_Y += r;
    }

    public void setY(float y) {
        position = new Point3D(position.getX(), y, position.getZ());
    }

    public float getDirectionX() {
        return (float) Math.sin(Math.toRadians(rotate_Y));
    }
    public float getDirectionZ() {
        return (float) Math.cos(Math.toRadians(rotate_Y));
    }

    public void MoveUp() {
    	position =  new Point3D((float)(position.getX() - 0.1 * Math.sin(Math.toRadians(rotate_Y))), 
    			position.getY(), 
    			(float)(position.getZ() - 0.1 * Math.cos(Math.toRadians(rotate_Y))));
    }

    public void MoveDown() {
    	position =  new Point3D((float)(position.getX() + 0.1 * Math.sin(Math.toRadians(rotate_Y))), 
    			position.getY(), 
    			(float)(position.getZ() + 0.1 * Math.cos(Math.toRadians(rotate_Y))));
    }


    public void changeFpsPos(float x, float z) {
    	position = new Point3D(x, position.getY(), z);
    }
}