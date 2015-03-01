/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package animations;

import characters.MyGameCharacterControl;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Prof Wireman
 */
public class AdvAnimationManagerControl extends AbstractControl implements AnimEventListener, ActionListener, AnalogListener{

    private AnimControl animControl;
    private AnimChannel upperChannel;
    private AnimChannel lowerChannel;
    private Vector3f modelForwardDir;
    private Vector3f modelLeftDir;
    boolean forward, backward, leftRotate, rightRotate, leftStrafe, rightStrafe, jumpStarted, inAir, firing, waving, kicking;
    private Properties animationNames;

    public enum Animation{
        Idle("idle", LoopMode.Loop, 0.2f),
        Walk("walk", LoopMode.Loop, 0.2f),
        Run("run", LoopMode.Loop, 0.2f),
        JumpStart("jump.start", LoopMode.DontLoop, 0.2f),
        Jumping("jump.mid", LoopMode.Loop, 0.0f),
        JumpEnd("jump.end", LoopMode.DontLoop, 0.1f),
        Punches("attack1", LoopMode.DontLoop, 0.1f),
        Taunt("taunt", LoopMode.DontLoop, 0.1f),
        Wave("greet", LoopMode.DontLoop, 0.1f),
        SideKick("attack2", LoopMode.DontLoop, 0.1f);
        
        Animation(String key, LoopMode loopMode, float blendTime){
            this.key = key;
            this.loopMode = loopMode;
            this.blendTime = blendTime;
        }
        
        String key;
        LoopMode loopMode;
        float blendTime;
    }
    
    public enum Channel{
        Upper,
        Lower,
        All,
    }
    
    public AdvAnimationManagerControl(){
        
    }
    
    public AdvAnimationManagerControl(String animationNameFile){
        animationNames = new Properties();
        try {
            animationNames.load(getClass().getClassLoader().getResourceAsStream(animationNameFile));
        } catch (IOException ex) {
            Logger.getLogger(AdvAnimationManagerControl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public String getAnimationName(String key){
        String animName = animationNames.getProperty(key);
        if(animName != null){
            return animName;
        }
        return key;
    }
    @Override
    protected void controlUpdate(float tpf) {
        if(inAir){
            MyGameCharacterControl charControl =spatial.getControl(MyGameCharacterControl.class);
            if(charControl != null && charControl.isOnGround()){
//                setAnimation(Animation.JumpEnd);
                setAnimation(Animation.Idle);
                jumpStarted = false;
                inAir = false;
            }
        }
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
    @Override
    public Control cloneForSpatial(Spatial spatial) {
        AdvAnimationManagerControl control = new AdvAnimationManagerControl();
        control.animationNames = animationNames;
        return control;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        animControl = spatial.getControl(AnimControl.class);
        upperChannel = animControl.createChannel();
        lowerChannel = animControl.createChannel();
        upperChannel.addFromRootBone("spine");
        lowerChannel.addBone("Root");
        lowerChannel.addFromRootBone("pelvis");

        animControl.addListener(this);
    }
    
    public void setAnimation(Animation animation, Channel channel){
        System.out.println(" " + animation);
        switch(channel){
            case Upper:
                setAnimation(animation, upperChannel);
                break;
            case Lower:
                setAnimation(animation, lowerChannel);
                break;
            case All:
                setAnimation(animation, upperChannel);
                setAnimation(animation, lowerChannel);
                break;
        }
    }
    
    private void setAnimation(Animation animation, AnimChannel channel) {
        if(channel.getAnimationName() == null || !channel.getAnimationName().equals(animation.name())){
            channel.setAnim(getAnimationName(animation.key), animation.blendTime);
        }
        
        channel.setLoopMode(animation.loopMode);
                
    }
    
    public void setAnimation(Animation animation){
        setAnimation(animation, upperChannel);
        setAnimation(animation, lowerChannel);
    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if(channel.getLoopMode() == LoopMode.DontLoop){

            Animation newAnim = Animation.Idle;
            Animation anim = Animation.valueOf(animName);
            switch(anim){
                case JumpStart:
                    newAnim = Animation.Jumping;
                    inAir = true;
                    break;
                case Punches:
                    firing = false;
                    break;
                    case Wave:
                    waving = false;
                    break;
                case SideKick:
                    kicking = false;
                    break;
            }
            setAnimation(newAnim, channel);
        }
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }
    
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("StrafeLeft")) {
            leftStrafe = value;
        } else if (binding.equals("StrafeRight")) {
            rightStrafe = value;
        } else if (binding.equals("MoveForward")) {
            forward = value;
        } else if (binding.equals("MoveBackward")) {
            backward = value;
        }
        
        
        
        // Add in fucntionality for Waving and SideKick
        
        else if (binding.equals("Wave") && value) {
            waving = true;
            setAnimation(Animation.Wave);
        }
        
        
        else if (binding.equals("SideKick") && value) {
            kicking = true;
            setAnimation(Animation.SideKick);
        }
        
        
        
        
        else if (binding.equals("Fire") && value) {
            firing = true;
            setAnimation(Animation.Punches, Channel.Upper);
        } else if (binding.equals("Jump") && value) {
            jumpStarted = true;
            setAnimation(Animation.JumpStart);
        }
        if(jumpStarted || firing || waving || kicking){
            // Do nothing
        } else if(forward || backward || rightStrafe || leftStrafe){
            setAnimation(Animation.Walk);
        } else {
            setAnimation(Animation.Idle);
        }
        
    }

    public void onAnalog(String name, float value, float tpf) {
        
    }
    
}
