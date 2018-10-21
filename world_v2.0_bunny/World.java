package unsw.graphics.world;

import java.io.File;
import java.io.FileNotFoundException;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.glu.GLU;

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

    
    private CoordFrame3D camera;
    private Avatar avatar;
    private Texture AvatarTexture;
    
    private float x = 0;
    private float y = 0;
    private float z = 0;
    
    Point3D lineofsight = new Point3D(1, 0, -1);
    
    private float rotate = -135;
    
    private Point2D myMousePoint = null;
    private boolean third_person = true;
    
    private static final int ROTATION_SCALE = 1;
    private static final float camera_position = 2f;

    public World(Terrain terrain) {
    	super("Assignment 2", 800, 600);
        this.terrain = terrain;
        
        this.avatar = new Avatar();
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
		
		
		y = terrain.altitude(x,z) + 0.5f;
		
//		CoordFrame3D frame = CoordFrame3D.identity().rotateY(135);
		
		camera = CoordFrame3D.identity()
				.rotateY(-rotate)
				.translate(-x, -y, -z);
			
		Shader.setViewMatrix(gl, camera.getMatrix());

		avatar.setY(y);
		avatar.display(gl);
			
		System.out.println("avatar:   " + x +"   "+ y +"   "+ z + "\n" + camera.getMatrix());

		
		
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
				if (third_person) {
//					x -=  Math.sin(Math.toRadians(rotate))*0.1;
//					z -=  Math.cos(Math.toRadians(rotate))*0.1;
					avatar.MoveUp();
					x = avatar.getX();
					z = avatar.getZ();
					
				} else {
					x -=  Math.sin(Math.toRadians(rotate))*0.1;
					z -=  Math.cos(Math.toRadians(rotate))*0.1;
					avatar.changeFpsPos(x, y);
				}

                break;
			
			case KeyEvent.VK_DOWN:
				if (third_person) {
//					x +=  Math.sin(Math.toRadians(rotate))*0.1;
//					z +=  Math.cos(Math.toRadians(rotate))*0.1;
					avatar.MoveDown();
					x = avatar.getX();
					z = avatar.getZ();
					
				} else {
					x +=  Math.sin(Math.toRadians(rotate))*0.1;
					z +=  Math.cos(Math.toRadians(rotate))*0.1;
					avatar.changeFpsPos(x, y);
				}
				break;
				
			case KeyEvent.VK_LEFT:
				if (third_person) {
					rotate += camera_position;
				} else {
					rotate += camera_position;
				}
				avatar.rotate(-camera_position);
				break;
				
			case KeyEvent.VK_RIGHT:
				if (third_person) {
					rotate -= camera_position;
				} else {
					rotate -= camera_position;
				}
				avatar.rotate(camera_position);
				break;
				
			default:
				break;
			
		}
	}

    public boolean onTerrain() {
        float x_f = x;
        float z_f = z;
        float size = terrain.getwidth()-1;
        
        return x_f >= 0 && z_f >= 0 && z_f < size && x_f < size;
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
	}
    
    public void keyTyped(KeyEvent e) {
	}
    
    public float getEyeX(){
        if (third_person) {
            // eye of camera is distance units behind avatar
            return (float) (avatar.getX() - (2 * Math.sin(Math.toRadians(avatar.getRotY()))));
        } else {
            return x;
        }
    }
    
    public float getY() {
        if (third_person) {
            return avatar.getY() + 1;
        } else {
            return y + 1.5f;
        }
    }
    
    public double getEyeZ() {
        if (third_person) {
            // eye of camera is distance units behind avatar
           return avatar.getZ()-(2 * Math.cos(Math.toRadians(avatar.getRotY())));
        } else {
            return z;
        }
    }
    
    public float getCenterX() {
        if (third_person) {
            return avatar.getX();
        } else {
            return x + lineofsight.getX();
        }
    }
    
    public double getCenterZ() {
        if (third_person) {
            return avatar.getZ();
        } else {
            return z + lineofsight.getZ();
        }
    }
    
    
}
