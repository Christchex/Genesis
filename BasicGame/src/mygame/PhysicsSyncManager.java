/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Chris
 */
class PhysicsSyncManager extends AbstractAppState implements MessageListener {
    private Server server;
    private Client client;
    private float syncFrequency = 0.25f;
    LinkedList<SyncMessageValidator> validators = new LinkedList<SyncMessageValidator>();
    HashMap<Long, Object> syncObjects = new HashMap<Long, Object>();
    double time = 0;
    double offset = Double.MIN_VALUE;
    private double maxDelay = 0.50;
    float syncTimer = 0;
    LinkedList<PhysicsSyncMessage> messageQueue = new LinkedList<PhysicsSyncMessage>();
    Application app;
    
    public PhysicsSyncManager(Application app, Server server){
        this.app = app;
        this.server = server;
    }
    
    public PhysicsSyncManager(Application app, Client client){
        this.app = app;
        this.client = client;
    }
    @Override
    public void update(float tpf){
        time += tpf;
        if(time < 0){
            time = 0;
        }
        if(client != null){
            for(Iterator<PhysicsSyncMessage> it = messageQueue.iterator(); it.hasNext();){
                PhysicsSyncMessage message = it.next();
                if(message.time >= time + offset){
                    doMessage(message);
                    it.remove();
                }
            }
            
        }else if(server != null){
            syncTimer += tpf;
            if(syncTimer >= syncFrequency){
                sendSyncData();
                syncTimer = 0;
            }
        }
    }
    public void addObject(long id, Object object){
        syncObjects.put(id, object);
    }
    public void removeObject(Object object){
        
    }
    public void removeObject(long id){
        syncObjects.remove(id);
    }
    public void clearObjects(){
        syncObjects.clear();
    }
    protected void doMessage(PhysicsSyncMessage message){
        Object object = syncObjects.get(message.syncId);
        if(object != null){
            message.applyData(object);
            
        }else{
            
        }
    }
    protected void enqueueMessage(PhysicsSyncMessage message){
        if(offset == Double.MIN_VALUE){
            offset = this.time - message.time;
            
        }
        double delayTime = (message.time + offset) - time;
        if(delayTime > maxDelay){
            offset -= delayTime - maxDelay;
            
        }else if(delayTime < 0){
            offset -= delayTime;
        }
        messageQueue.add(message);
    }
    public void messageReceived(Object source, Message message) {
        assert (message instanceof PhysicsSyncMessage);
    }

    private void sendSyncData() {
        
    }
}
