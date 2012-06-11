/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.base.*;
import com.jme3.network.*;

import com.jme3.system.JmeContext;


/**
 *
 * @author Chris
 */
public class Genesis_Server extends SimpleApplication {
    private static com.jme3.network.Server server;
    private static Genesis_Server app;
    @Override
    public void simpleInitApp() {
        
        try{
            server = Network.createServer(6666);
            server.start();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        bulletState = new BulletAppState();
        getStateManager().attach(bulletState);
                
    }
    private PhysicsSyncManager syncManager;
    private BulletAppState bulletState;
    
    
    public static void main(String[] args){
        app = new Genesis_Server();
        app.setPauseOnLostFocus(false);
        app.start();
        
    }
}
