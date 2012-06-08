package mygame;

import com.jme3.collision.CollisionResult;
import com.jme3.input.controls.*;
import com.jme3.math.Ray;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    protected Geometry player;
    protected Geometry mark;
    private Boolean isRunning = true;
    private float speed = .03f;
    private BulletAppState bulletAppState;
    
    
    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        initKeys();
        initCrossHairs();
        initMark();
        initFloor();
        
        player = makeCube("player", 0f, 0f, 0f, 1f);
        
        Node pivot = new Node("pivot");
        rootNode.attachChild(pivot);
        
        pivot.attachChild(player);
        pivot.rotate(.4f,.4f,0f);
        
        rootNode.attachChild(player);
        
    }
    
    
    private void initKeys(){
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CLICK", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Backward",new KeyTrigger(KeyInput.KEY_S));       
        inputManager.addMapping("LEFT",new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("RIGHT",new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("UP", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("DOWN", new KeyTrigger(KeyInput.KEY_Q));        
        inputManager.addListener(analogListener, new String[]{"Forward","Backward","LEFT", "RIGHT", "UP", "DOWN"});
        inputManager.addListener(actionListener, new String[]{"CLICK"});
    }
    
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if(name.equals("CLICK") && !keyPressed) {
                 System.out.println("X:" + cam.getDirection().getX());
                    System.out.println("Y:" + cam.getDirection().getY());
                    System.out.println("Z:" + cam.getDirection().getZ());
                    CollisionResults results = new CollisionResults();
                    Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                    rootNode.collideWith(ray, results);
                    System.out.println("-----Collisions? " + results.size() + "----");
                    for(int i = 0; i < results.size(); i++){
                        float dist = results.getCollision(i).getDistance();
                        Vector3f pt = results.getCollision(i).getContactPoint();
                        String hit = results.getCollision(i).getGeometry().getName();
                        System.out.println("* Collision #" + i);
                        System.out.println("You shot " + hit + "at " + pt + ", " + dist + " wu away.");
                              
                    }
                    if(results.size() > 0){
                        try{
                            
                        
                        CollisionResult closest = results.getClosestCollision();
                        mark.setLocalTranslation(closest.getContactPoint());
                        rootNode.attachChild(mark);
                       // rootNode.attachChild(makeCube(closest.getContactPoint()));
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                                                
                    }else{
                        try{  
                        rootNode.detachChild(mark);
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
            }
        }
    };
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if(isRunning) {
                if(name.contains("Forward")) {
                    cam.setLocation(cam.getLocation().add(speed * cam.getDirection().getX(),speed * cam.getDirection().getY(),speed * cam.getDirection().getZ()));
                    
                    
                }
                if(name.contains("Backward")){
                    
                    cam.setLocation(cam.getLocation().add(-speed * cam.getDirection().getX(),-speed * cam.getDirection().getY(),-speed * cam.getDirection().getZ()));
                }
                if(name.contains("LEFT")){
                   cam.setLocation(cam.getLocation().add(speed * cam.getLeft().getX(),speed * cam.getLeft().getY(),speed * cam.getLeft().getZ()));
                }
                if(name.contains("UP")){
                    cam.setLocation(cam.getLocation().add(speed * cam.getUp().getX(),speed * cam.getUp().getY(),speed * cam.getUp().getZ()));
                }
                if(name.contains("DOWN")){
                    cam.setLocation(cam.getLocation().add(-speed * cam.getUp().getX(),-speed * cam.getUp().getY(),-speed * cam.getUp().getZ()));
                }
                if(name.contains("RIGHT")){
                    cam.setLocation(cam.getLocation().add(-speed * cam.getLeft().getX(),-speed * cam.getLeft().getY(),-speed * cam.getLeft().getZ()));
                }
            }
        }
    };
    
    protected void initFloor() {
        Box floor = new Box(Vector3f.ZERO, 10f, 0.5f, 10f);
        Geometry floor_geo = new Geometry("floor", floor);
        Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floor_mat.setColor("Color", ColorRGBA.randomColor());
        floor_geo.setMaterial(floor_mat);
        this.rootNode.attachChild(floor_geo);
        floor_geo.setLocalTranslation(0, -5f, 0);
        RigidBodyControl floor_phy = new RigidBodyControl(0.0f); // floor mass is zero, therefore it doesn't move.
        floor_geo.addControl(floor_phy);
        bulletAppState.getPhysicsSpace().add(floor_phy);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
        
       
        
    }
    protected Geometry makeCube(String name, float x, float y, float z, float mass){
        Box box = new Box(new Vector3f(x,y,z),1,1,1);
        Geometry cube = new Geometry(name, box);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.randomColor());
        cube.setMaterial(mat1);
        RigidBodyControl box_phy = new RigidBodyControl(mass);
        cube.addControl(box_phy);
        bulletAppState.getPhysicsSpace().add(box_phy);
        return cube;
    }
    protected void initCrossHairs(){
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+");
        ch.setLocalTranslation(settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }
    protected void initMark() {
        Sphere sphere = new Sphere(30, 30, 0.2f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
        }
    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
