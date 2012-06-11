/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.network.Message;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Chris
 */
@Serializable()
public abstract class PhysicsSyncMessage implements Message{
    public long syncId = -1;
    public double time;
    
    public PhysicsSyncMessage(){
        
    }
    public PhysicsSyncMessage(long id){
        this.syncId = id;
    }
    public boolean isReliable(){
        return true;
    }
    public abstract void applyData(Object object);
}
