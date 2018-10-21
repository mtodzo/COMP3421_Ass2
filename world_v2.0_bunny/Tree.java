package unsw.graphics.world;

import java.awt.Color;
import java.io.IOException;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {

    private Point3D position;
    
    private TriangleMesh model;
    
    public Tree(float x, float y, float z) {
        position = new Point3D(x, y, z);
    }
    
    public Point3D getPosition() {
        return position;
    }

    public void init(GL3 gl) {
    	// create tree model by tree.ply
        try{
        	model = new TriangleMesh("res/models/tree.ply", true, true);
        } catch (IOException i) {
        	System.out.println("res/models/tree.ply file is not found");
        	i.printStackTrace();
        }
    	
    	model.init(gl);
    	
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
    
    public void display(GL3 gl, CoordFrame3D view) {
    	
    	CoordFrame3D modelFrame = view
    			.translate(position.getX(),position.getY() + 0.45f,position.getZ())
    			.rotateY(180)
    			.scale(0.1f, 0.1f, 0.1f);
    			
//    	System.out.println(position.getX()+"   "+position.getY()+"   "+position.getZ());

    	
        model.draw(gl, modelFrame);
     }
    
    public void destroy(GL3 gl) {
    	model.destroy(gl);
    }
    

}
