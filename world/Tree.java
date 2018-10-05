package unsw.graphics.world;

import java.io.IOException;

import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {

    private Point3D position;
    private TriangleMesh tree;
    
    public Tree(float x, float y, float z) {
        position = new Point3D(x, y, z);
    }
    
    public Point3D getPosition() {
        return position;
    }
    
    public Tree() throws IOException {
    	tree = new TriangleMesh("res/models/tree.ply", true, true);
    }
   
    public void draw(GL3 gl) {
    	CoordFrame3D treeFrame = CoordFrame3D.identity()
    			.translate(position)
    			.scale(0.1f, 0.1f, 0.1f);
    	
    	tree.draw(gl, treeFrame);
    	
    	
    	float diffuse = 0.1f;
        float specular = 1.0f;
        float ambient = 0.1f;
        
        
    }

}
