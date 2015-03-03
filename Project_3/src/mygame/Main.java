package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import animations.AdvAnimationManagerControl;
import animations.CharacterInputAnimationAppState;
import characters.ChaseCamCharacter;
import com.jme3.app.FlyCamAppState;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.material.RenderState;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.List;
import physics.PhysicsTestHelper;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication implements PhysicsCollisionListener {
  
  private BulletAppState bulletAppState;
  private RigidBodyControl landscape;
  
  private AdvAnimationManagerControl animControl;
    
  
  // Variables for the walls of the world
  private RigidBodyControl side1_phy, side2_phy, side3_phy, side4_phy;
  private static final Box side1, side2, side3, side4;
  private Material wall_material;
  
   private AudioNode audio_gun, audio_explosion, audio_nature, audio_footsteps;
  
  
  private int crateSpawnTimer = 0;
  private ParticleEmitter explosion;
  
  static {
    side1 = new Box(256, 5, 0.5f);
    side1.scaleTextureCoordinates(new Vector2f(3, 6));
          
    side2 = new Box(0.5f, 5f, 256f);
    side2.scaleTextureCoordinates(new Vector2f(3, 6));
          
    side3 = new Box(256, 5, 0.5f);
    side3.scaleTextureCoordinates(new Vector2f(3, 6));
          
    side4 = new Box(0.5f, 5f, 256f);
    side4.scaleTextureCoordinates(new Vector2f(3, 6));   
  }
  
  
  
  private List<Geometry> targets = new ArrayList<Geometry>();
  private Node mainPlayer;
  
  private Vector3f normalGravity = new Vector3f(0, -9.81f, 0);

  public static void main(String[] args) {
    Main app = new Main();
    app.start();
  }
  private float COUNT_FACTOR_F;
  private boolean POINT_SPRITE;

  @Override
  public void simpleInitApp() {
    
    /** Set up Physics */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
    
    stateManager.detach(stateManager.getState(FlyCamAppState.class));
    
    AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        rootNode.addLight(light);
    
    // Set the start position for he camera
    //cam.getLocation().set(0,60,0);
    //cam.update();
        
    // Add the Scene And create collision physics to match the terrain
    Spatial scene = assetManager.loadModel("/Scenes/P3_Scene3.j3o");
    
    CollisionShape sceneShape = 
                CollisionShapeFactory.createMeshShape(scene);
        landscape = new RigidBodyControl(sceneShape, 0);
        scene.addControl(landscape);
    
    scene.addControl(new RigidBodyControl(0));
    bulletAppState.getPhysicsSpace().add(scene);
     bulletAppState.getPhysicsSpace().add(landscape);
    
     //Attach the scene to the root
     rootNode.attachChild(scene);
     
     initMaterials();
     initWalls();
     initAudio();
     
      // add ourselves as collision listener
    getPhysicsSpace().addCollisionListener(this);
     
     
      createPlayerCharacter();
      
       //Add the "bullets" to the scene to allow the player to shoot the balls
//        PhysicsTestHelper.createBallShooter(this,rootNode,bulletAppState.getPhysicsSpace(),
//            sinbadAppState, targets, guiNode, hitText);
//        
        PhysicsTestHelper.createBallShooter(this, rootNode, bulletAppState.getPhysicsSpace(), audio_gun);
      //  PhysicsTestHelper.createMyPhysicsTestWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace(), targets);

        PhysicsTestHelper.spawnCrates(rootNode, assetManager, bulletAppState.getPhysicsSpace(), targets);
        
        
        //Add a custom font and text to the scene
        BitmapFont myFont = assetManager.loadFont("Interface/Fonts/DroidSansMono.fnt");
        
        
         // Create the crosshairs for the ball shooter
        BitmapText crosshairs = new BitmapText(myFont, true); 
        crosshairs.setText("X");
        crosshairs.setColor(ColorRGBA.Yellow);
        crosshairs.setSize(guiFont.getCharSet().getRenderedSize());
        crosshairs.setLocalTranslation(settings.getWidth() / 2,
            settings.getHeight() / 2 + crosshairs.getLineHeight(), 0f);
        guiNode.attachChild(crosshairs);                 
                
  }

  private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }
  
  
    /** We create two audio nodes. */
  private void initAudio() {
    /* gun shot sound is to be triggered by a mouse click. */
    //audio_gun = new AudioNode(assetManager, "Sound/Effects/Gun.wav", false);
    audio_gun = new AudioNode(assetManager, "Sounds/message.ogg", false);

    audio_gun.setPositional(false);
    audio_gun.setLooping(false);
    audio_gun.setVolume(2);
    rootNode.attachChild(audio_gun);
    
    /* explosion sound is to be triggered by a collision. */
    audio_explosion = new AudioNode(assetManager, "Sound/Effects/Bang.wav", false);
    audio_explosion.setPositional(true);
    audio_explosion.setLooping(false);
    audio_explosion.setVolume(1);
    rootNode.attachChild(audio_explosion);

 
    /* nature sound - keeps playing in a loop. */
    audio_nature = new AudioNode(assetManager, "Sounds/Noise.wav", true);
    audio_nature.setPositional(false);
    audio_nature.setLooping(true);  // activate continuous playing
    //audio_nature.setPositional(true);   
    audio_nature.setVolume(1);
    rootNode.attachChild(audio_nature);
    audio_nature.play(); // play continuously!
  }
  
   private void createPlayerCharacter() {
        mainPlayer = (Node) assetManager.loadModel("Models/Jaime/Jaime.j3o");
        mainPlayer.setLocalTranslation(200, 10, 0f);
        ChaseCamCharacter charControl = new ChaseCamCharacter(0.5f, 2.5f, 8f);
        charControl.setGravity(normalGravity);
        charControl.setCamera(cam);
        
        ChaseCamera chaseCam = new ChaseCamera(cam, mainPlayer, inputManager);
        chaseCam.setDragToRotate(false);
        chaseCam.setSmoothMotion(true);
        chaseCam.setLookAtOffset(new Vector3f(0f, 2.7f, -0.1f));
        chaseCam.setDefaultDistance(1.75f);
        chaseCam.setMaxDistance(2f);
        chaseCam.setMinDistance(1.5f);
       
        chaseCam.setTrailingSensitivity(50);
        chaseCam.setChasingSensitivity(10);
        chaseCam.setRotationSpeed(10);
        //chaseCam.setDragToRotate(true);
        //chaseCam.setToggleRotationTrigger();

        mainPlayer.addControl(charControl);
        //mainPlayer.addControl(new RigidBodyControl(1));
                 
        bulletAppState.getPhysicsSpace().add(charControl);

        CharacterInputAnimationAppState appState = new CharacterInputAnimationAppState();
        appState.addActionListener(charControl);
        appState.addAnalogListener(charControl);
        appState.setChaseCamera(chaseCam);
        stateManager.attach(appState);
        rootNode.attachChild(mainPlayer);
        inputManager.setCursorVisible(false);
        
        animControl = new AdvAnimationManagerControl("animations/resources/animations-jaime.properties");
        mainPlayer.addControl(animControl);
        
        appState.addActionListener(animControl);
        appState.addAnalogListener(animControl);
    }
  
  
   
   
   /** Initialize the materials used in this scene. */
  public void initMaterials() {
//    stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//    TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
//    key2.setGenerateMips(true);
//    Texture tex2 = assetManager.loadTexture(key2);
//    stone_mat.setTexture("ColorMap", tex2);
    
    wall_material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key3 = new TextureKey("Blender/2.4x/textures/WarningStrip.png");
    key3.setGenerateMips(true);
    Texture tex3 = assetManager.loadTexture(key3);
    tex3.setWrap(Texture.WrapMode.Repeat);
    wall_material.setTexture("ColorMap", tex3);
    wall_material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
  }
  
   
   
   // Initialize the collision cube used to contain the spheres
  public void initWalls() {
    
    // Create the floor
//    Geometry floor_geo = new Geometry("Floor", floor);
//    floor_geo.setQueueBucket(RenderQueue.Bucket.Transparent);
//    floor_geo.setMaterial(wall_material);
//    this.rootNode.attachChild(floor_geo);
//    floor_phy = new RigidBodyControl(0.0f);
//    floor_geo.addControl(floor_phy);
//    bulletAppState.getPhysicsSpace().add(floor_phy);
//    floor_phy.setPhysicsLocation(new Vector3f(0, -1.8f, 0));
//    floor_phy.setFriction(0.0f);
//    floor_phy.setRestitution(.1f);
          
    
    // Create side1
    Geometry side1_geo = new Geometry("Side1", side1);
    side1_geo.setQueueBucket(RenderQueue.Bucket.Transparent);
    side1_geo.setMaterial(wall_material);
    this.rootNode.attachChild(side1_geo);
    side1_phy = new RigidBodyControl(0.0f);
    side1_geo.addControl(side1_phy);
    bulletAppState.getPhysicsSpace().add(side1_phy);
    side1_phy.setFriction(0.0f);
    side1_phy.setPhysicsLocation(new Vector3f(0, 2.5f, 256));
    side1_phy.setFriction(0.0f);
    side1_phy.setRestitution(.1f);
    
    
    // Create side2
    Geometry side2_geo = new Geometry("Side2", side2);
    side2_geo.setQueueBucket(RenderQueue.Bucket.Transparent);
    side2_geo.setMaterial(wall_material);
    this.rootNode.attachChild(side2_geo);
    side2_phy = new RigidBodyControl(0.0f);
    side2_geo.addControl(side2_phy);
    bulletAppState.getPhysicsSpace().add(side2_phy);
    side2_phy.setFriction(0.0f);
    side2_phy.setPhysicsLocation(new Vector3f(256, 2.5f, 0));
    side2_phy.setFriction(0.0f);
    side2_phy.setRestitution(.1f);
          

    // Create side3
    Geometry side3_geo = new Geometry("Side3", side3);
    side3_geo.setQueueBucket(RenderQueue.Bucket.Transparent);
    side3_geo.setMaterial(wall_material);
    this.rootNode.attachChild(side3_geo);
    side3_phy = new RigidBodyControl(0.0f);
    side3_geo.addControl(side3_phy);
    bulletAppState.getPhysicsSpace().add(side3_phy);
    side3_phy.setFriction(0.0f);
    side3_phy.setPhysicsLocation(new Vector3f(0, 2.5f, -256));
    side3_phy.setFriction(0.0f);
    side3_phy.setRestitution(.1f);
          
          
     // Create side4
    Geometry side4_geo = new Geometry("Side4", side4);
    side4_geo.setQueueBucket(RenderQueue.Bucket.Transparent);
    side4_geo.setMaterial(wall_material);
    this.rootNode.attachChild(side4_geo);
    side4_phy = new RigidBodyControl(0.0f);
    side4_geo.addControl(side4_phy);
    bulletAppState.getPhysicsSpace().add(side4_phy);
    side4_phy.setFriction(0.0f);
    side4_phy.setPhysicsLocation(new Vector3f(-256, 2.5f, 0));
    side4_phy.setFriction(0.0f);
    side4_phy.setRestitution(.1f);
    
   
  }
   
   
   
  @Override
  public void simpleUpdate(float tpf) {
    //TODO: add update code
    
    System.out.println(targets.size());
    
    if(targets.size() > 0) {
      System.out.println(targets.get(0).getName());
    }
    
    if(targets.size() == 0) {
      crateSpawnTimer--;
      System.out.println(crateSpawnTimer);
      
      if(crateSpawnTimer == 0) {
        //explosion.killAllParticles();
        PhysicsTestHelper.spawnCrates(rootNode, assetManager, bulletAppState.getPhysicsSpace(), targets);
      }
    }
   // System.out.println(targets.get(29).getName());

  }

  @Override
  public void simpleRender(RenderManager rm) {
    //TODO: add render code
  }



public void collision(PhysicsCollisionEvent event) {
  
  //System.out.println("Collision Detected");
  
  if("box".equals(event.getNodeA().getName()) || "box".equals(event.getNodeB().getName())) {
    if("bullet".equals(event.getNodeA().getName()) || "bullet".equals(event.getNodeB().getName())) {
      System.out.println("A box was hit by a bullet");
      
      if("box".equals(event.getNodeA().getName())) {
        targets.remove(event.getNodeA());
      } else if ("box".equals(event.getNodeB().getName())) {
        targets.remove(event.getNodeB());
      }
      
      
      
      
      //Trigger an explosion
      audio_explosion.playInstance(); // play each instance once!
      explosion = new ParticleEmitter("My explosion effect", Type.Triangle, 30);
      
      explosion.setSelectRandomImage(true);
        explosion.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (float) (1f / COUNT_FACTOR_F)));
        explosion.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        explosion.setStartSize(1.3f);
        explosion.setEndSize(2f);
        explosion.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        explosion.setParticlesPerSec(0);
        explosion.setGravity(0, -5, 0);
        explosion.setLowLife(.4f);
        explosion.setHighLife(.5f);
        explosion.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 7, 0));
        explosion.getParticleInfluencer().setVelocityVariation(1f);
        explosion.setImagesX(2);
        explosion.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        mat.setBoolean("PointSprite", POINT_SPRITE);
        explosion.setMaterial(mat);

      
      
      
      rootNode.attachChild(explosion);
      explosion.setLocalTranslation(event.getNodeB().getLocalTranslation());
      explosion.emitAllParticles();
      
      this.rootNode.detachChild(event.getNodeA());
      this.rootNode.detachChild(event.getNodeB());
      
      this.rootNode.updateGeometricState();
      
      crateSpawnTimer = 300;
      
     
      
      

      
    }
  }
  
  
    
    if("sphere1".equals(event.getNodeA().getName()) || "sphere1".equals(event.getNodeB().getName())) {
      
      if("sphere2".equals(event.getNodeA().getName()) || "sphere2".equals(event.getNodeB().getName())) {
        
       // collision_count++;
       // output.println("\nCollision number: " + collision_count);
        //output.print(event.getNodeA().getName() + " collided with ");        
       // output.println(event.getNodeB().getName() + " at position:");
        //output.println("sphere1: " + sphere1_phy.getPhysicsLocation());
        //output.println("sphere2: " + sphere2_phy.getPhysicsLocation());
      }
      
      else if("sphere3".equals(event.getNodeA().getName()) || "sphere3".equals(event.getNodeB().getName())) {
        
      //  collision_count++;
        //output.println("\nCollission number: " + collision_count);
        //output.print(event.getNodeA().getName() + " collided with ");        
        //output.println(event.getNodeB().getName() + " at position:");
        //output.println("sphere1: " + sphere1_phy.getPhysicsLocation());
        //output.println("sphere3: " + sphere3_phy.getPhysicsLocation());
      }
    }
    
    else if("sphere3".equals(event.getNodeA().getName()) || "sphere3".equals(event.getNodeB().getName())) {
      
      if("sphere2".equals(event.getNodeA().getName()) || "sphere2".equals(event.getNodeB().getName())) {
        
      //  collision_count++;
        //output.println("\nCollission number: " + collision_count);
        //output.print(event.getNodeA().getName() + " collided with ");        
        //output.println(event.getNodeB().getName() + " at position:");
        //output.println("sphere2: " + sphere2_phy.getPhysicsLocation());
        //output.println("sphere3: " + sphere3_phy.getPhysicsLocation());
      }
    }    
  }
}