package mygame;

import com.jme3.collision.CollisionResult;
import com.jme3.input.controls.*;
import com.jme3.math.Ray;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer;

/**
 * @
 * @version 0.1.3
 * @author Chris
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    PssmShadowRenderer psm;
    BasicShadowRenderer bsr;
    protected Node characterNode;
    private float zSpeed = 0.4f;
    private float xSpeed = 0.3f;
    private CharacterControl player;
    protected Box floor;
    private Boolean debugEnabled = false;
    protected Geometry bob;
    protected Geometry mark;
    protected Geometry inHands;
    private float playerSpeed = .03f;
    private BulletAppState bulletAppState;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false, picked = false, running = false;
    private float maxGrabDist = 15, grabDist;

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);

        Spatial model = assetManager.loadModel("Scenes/basicTerrain.j3o");
        rootNode.attachChild(model);

        RigidBodyControl terrain_geo = new RigidBodyControl(0.0f);
        model.addControl(terrain_geo);

        bulletAppState.getPhysicsSpace().add(terrain_geo);
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);
        initKeys();
        initCrossHairs();
        initMark();
        setUpLight();
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setGravity(40);
        player.setPhysicsLocation(new Vector3f(0, 15, 0));

        psm = new PssmShadowRenderer(assetManager, 1024, 3);
        psm.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal());

        //bsr = new BasicShadowRenderer(assetManager, 256);
        //bsr.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal());
        viewPort.addProcessor(psm);

        bulletAppState.getPhysicsSpace().add(player);
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -9.81f, 0));



    }

    /**
     * 
     */
    private void initKeys() {
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CLICK", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("LEFT", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("RIGHT", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("UP", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("DOWN", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("JUMP", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("DEBUG", new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addMapping("PICK_UP", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("RIGHT_CLICK", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("RUN", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addListener(actionListener, new String[]{"Forward", "Backward", "DEBUG", "LEFT", "RIGHT", "UP", "DOWN", "JUMP", "PICK_UP", "RUN"});
        inputManager.addListener(actionListener, new String[]{"CLICK", "RIGHT_CLICK"});
    }
    
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("LEFT")) {
                left = keyPressed;
            } else if (name.equals("RIGHT")) {
                right = keyPressed;
            } else if (name.equals("Forward")) {
                up = keyPressed;
            } else if (name.equals("Backward")) {
                down = keyPressed;
            } else if (name.equals("RUN")) {
                running = keyPressed;
            } else if (name.equals("JUMP")) {
                player.jump();

            } else if (name.equals("DEBUG")) {
                if (debugEnabled == false && keyPressed) {

                    bulletAppState.getPhysicsSpace().enableDebug(assetManager);
                    debugEnabled = true;
                } else if (debugEnabled == true && keyPressed) {
                    bulletAppState.getPhysicsSpace().disableDebug();
                    debugEnabled = false;
                }

            }
            if (name.equals("RIGHT_CLICK") && !keyPressed) {
                CollisionResults results = new CollisionResults();
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                rootNode.collideWith(ray, results);
                System.out.println("-----Collisions? " + results.size() + "----");
                for (int i = 0; i < results.size(); i++) {
                    float dist = results.getCollision(i).getDistance();
                    Vector3f pt = results.getCollision(i).getContactPoint();
                    String hit = results.getCollision(i).getGeometry().getName();
                    System.out.println("* Collision #" + i);
                    System.out.println("You shot " + hit + "at " + pt + ", " + dist + " wu away.");

                }
                if (results.size() > 0) {
                    try {
                        if (results.getClosestCollision().getGeometry().getMesh() != floor) {
                            rootNode.detachChild(results.getClosestCollision().getGeometry());


                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            if (name.equals("CLICK") && !keyPressed) {
                System.out.println("X:" + cam.getDirection().getX());
                System.out.println("Y:" + cam.getDirection().getY());
                System.out.println("Z:" + cam.getDirection().getZ());
                CollisionResults results = new CollisionResults();
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                rootNode.collideWith(ray, results);
                System.out.println("-----Collisions? " + results.size() + "----");
                for (int i = 0; i < results.size(); i++) {
                    float dist = results.getCollision(i).getDistance();
                    Vector3f pt = results.getCollision(i).getContactPoint();
                    String hit = results.getCollision(i).getGeometry().getName();
                    System.out.println("* Collision #" + i);
                    System.out.println("You shot " + hit + "at " + pt + ", " + dist + " wu away.");

                }
                if (results.size() > 0) {
                    try {


                        CollisionResult closest = results.getClosestCollision();
                        mark.setLocalTranslation(closest.getContactPoint());
                        //rootNode.attachChild(mark);
                        float tempY = closest.getContactPoint().getY();
                        /*if(!(tempY > 0)){
                        tempY =0;
                        }*/
                        makeCube(name, closest.getContactPoint().getX(), tempY, closest.getContactPoint().getZ(), 0.5f);
                        // rootNode.attachChild(makeCube(closest.getContactPoint()));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }

                } else {
                    try {
                        rootNode.detachChild(mark);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            if (name.equals("PICK_UP") && !keyPressed) {
                CollisionResults results = new CollisionResults();
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                rootNode.collideWith(ray, results);
                System.out.println("-----Collisions? " + results.size() + "-----");

                for (int i = 0; i < results.size(); i++) {
                    grabDist = results.getCollision(i).getDistance();
                    Vector3f pt = results.getCollision(i).getContactPoint();
                    String hit = results.getCollision(i).getGeometry().getName();
                    System.out.println("* Collision #" + i);
                    System.out.println("You shot " + hit + "at " + pt + ", " + grabDist + " wu away.");
                }
                if (results.size() > 0) {
                    try {
                        if(picked) {
                            System.out.println("You dropped " + inHands);
                            picked = false;
                        }
                        else if (!results.getClosestCollision().getGeometry().getName().contains("Terrain") && grabDist <= maxGrabDist && !picked) {
                            Geometry grabbed = results.getClosestCollision().getGeometry();
                            System.out.println("You grabbed " + grabbed);
                            pickup(grabbed);
                            picked = true;
                            inHands = grabbed;
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    };

    protected void pickup(Geometry geom) {
        characterNode.attachChild(geom);
        geom.setLocalTranslation(0, 5, 0);
    }
    
    protected void release(Geometry geom) {
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(zSpeed).setY(0.0f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(xSpeed).setY(0.0f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        if(running) {
            xSpeed = 0.6f;
            zSpeed = 0.5f;
        }
        if(!running) {
            xSpeed = 0.4f;
            zSpeed = 0.3f;
        }
        player.setWalkDirection(walkDirection);
        System.out.println("xSpeed: " + xSpeed + ", zSpeed: " + zSpeed);
        cam.setLocation(player.getPhysicsLocation());


    }

    protected void makeCube(String name, float x, float y, float z, float mass) {
        Box box = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry cube = new Geometry(name, box);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat1.setColor("GlowColor", ColorRGBA.randomColor());
        cube.setMaterial(mat1);
        RigidBodyControl box_phy = new RigidBodyControl(mass);
        cube.addControl(box_phy);
        cube.setLocalTranslation(x, y, z);
        bulletAppState.getPhysicsSpace().add(box_phy);
        box_phy.setPhysicsLocation(cube.getLocalTranslation());
        rootNode.attachChild(cube);
    }

    protected void initCrossHairs() {
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("( - )");
        ch.setLocalTranslation(settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

    protected void initcube() {
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

    private void setUpLight() {
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }
}

