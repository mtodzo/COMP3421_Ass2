package unsw.graphics.world;

import java.io.File;
import java.io.FileNotFoundException;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.opengl.GL3;

import unsw.graphics.Application3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Shader;
import unsw.graphics.geometry.Point2D;



/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class World extends Application3D implements KeyListener{

    private Terrain terrain;
    
    private float positionX = 0;
    private float positionZ = 0;
    private float rotation = 135;
    
    private Point2D myMousePoint = null;
    private static final int ROTATION_SCALE = 1;

    public World(Terrain terrain) {
    	super("Assignment 2", 800, 600);
        this.terrain = terrain;
   
    }
   
    /**
     * Load a level file and display it.
     * 
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        World world = new World(terrain);
        world.start();
    }

	@Override
	public void display(GL3 gl) {
		super.display(gl);
		terrain.display(gl, positionZ, positionX, rotation);
	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);
		
	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);
		getWindow().addKeyListener(this);
		terrain.init(gl);
		
	}

	@Override
	public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 1, 100));
	}
	
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        
        case KeyEvent.VK_UP:
            // Move forward
        	positionZ = positionZ + (float) Math.sin(Math.toRadians(rotation))*0.05f;
        	positionX = positionX + (float) Math.cos(Math.toRadians(rotation))*0.05f;
            break;
        case KeyEvent.VK_DOWN:
            // Move Back
        	positionZ = positionZ - (float) Math.sin(Math.toRadians(rotation))*0.05f;
        	positionX = positionX - (float) Math.cos(Math.toRadians(rotation))*0.05f;
            break;
        case KeyEvent.VK_LEFT:
            // Turn left
        	rotation = rotation - 1;
        	if (rotation < 0) {
        		rotation = rotation + 360;
        	}
            break;
        case KeyEvent.VK_RIGHT:
            // Turn right
        	rotation = rotation + 1;
        	if (rotation >= 360) {
        		rotation = rotation - 360;
        	}
            break;
        default:
        	//Do nothing
            break;
        }

    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    	
    }
	
	/*
	@Override
	public void mouseDragged(MouseEvent e) {
		Point2D p = new Point2D(e.getX(), e.getY());

        if (myMousePoint != null) {
            float dx = p.getX() - myMousePoint.getX();
            float dy = p.getY() - myMousePoint.getY();

            // Note: dragging in the x dir rotates about y
            //       dragging in the y dir rotates about x
            rotateY += dx * ROTATION_SCALE;
            rotateX += dy * ROTATION_SCALE;

        }
        myMousePoint = p;
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		 myMousePoint = new Point2D(e.getX(), e.getY());
	}

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseWheelMoved(MouseEvent e) { }
	*/
}
