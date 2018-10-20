package unsw.graphics.world;

import java.io.File;
import java.io.FileNotFoundException;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;



/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class World extends Application3D implements MouseListener, KeyListener{

    private Terrain terrain;
    
    private float rotateX = 0;
    private float rotateY = 0;
    
    private int width;
    private int depth;
    
    private CoordFrame3D camera;
    private Avatar avatar;
    private Texture AvatarTexture;
    
    private float x = 0;
    private float y = 0;
    private float z = 8;
    private float rotate;
    
    private Point2D myMousePoint = null;
    private boolean third_person = true;
    
    private static final int ROTATION_SCALE = 1;
    private static final float camera_rotation = 0.5f;
    private static final float camera_position = 2f;

    public World(Terrain terrain) {
    	super("Assignment 2", 800, 600);
        this.terrain = terrain;
        this.width = terrain.getwidth();
        this.depth = terrain.getdepth();
        
        this.y = terrain.altitude(x,z) + 0.8f;
        this.avatar = new Avatar(x,y,z);
    }
   
    /**
     * Load a level file and display it.
     * 
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File("res/worlds/test1.json"));
        World world = new World(terrain);
        world.start();
    }
    
    
	@Override
	public void display(GL3 gl) {
		super.display(gl);
		
		setCamera(gl);
		
//		Shader.setViewMatrix(gl, camera.getMatrix());
	
		terrain.display(gl, rotateX, rotateY);
	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);
		avatar.destroy(gl);
		
	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);

		getWindow().addMouseListener(this);
		getWindow().addKeyListener(this);
		terrain.init(gl);
		avatar.init(gl);
	}

	@Override
	public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 0.1f, 100));
	}
	
	
	/*	
	 * 	Camera with Mouse & Key Controller
	 */
	
	// Set Camera
	public void setCamera(GL3 gl) {
		// Set the camera is move by a person
		
		
		y = terrain.altitude(x,z);

		camera = CoordFrame3D.identity()
				.rotateY(-rotate)
				.translate(-x, -y -0.5f, -z);
			
		Shader.setViewMatrix(gl, camera.getMatrix());

		
		if (third_person) {
			avatar.update(x ,y,z);
			avatar.display(gl , rotate);
			System.out.println("avatar:   " + x +"   "+ y +"   "+ z + "\n" + camera.getMatrix());
		} else {
			return;
		}
		
		
	}
	

	// Mouse Controller
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
    
    // Key Controller
    @Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				x -=  Math.sin(Math.toRadians(rotate))*0.1;
				z -=  Math.cos(Math.toRadians(rotate))*0.1;
				break;
			case KeyEvent.VK_DOWN:
				x +=  Math.sin(Math.toRadians(rotate))*0.1;
				z +=  Math.cos(Math.toRadians(rotate))*0.1;
				break;
			case KeyEvent.VK_LEFT:
				rotate += camera_position;
				break;
			case KeyEvent.VK_RIGHT:
				rotate -= camera_position;
				break;
			case KeyEvent.VK_A:
				x -= camera_rotation;
				break;
			case KeyEvent.VK_D:
				x += camera_rotation;
				break;	
			case KeyEvent.VK_W:
				y -= camera_rotation;
				break;
			case KeyEvent.VK_S:
				y += camera_rotation;
				break;
			default:
				break;
			
		}
	}
	
    @Override
    public void keyReleased(KeyEvent e) {
	}
    
    public void keyTyped(KeyEvent e) {
	}
    
}
