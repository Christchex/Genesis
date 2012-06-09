/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
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
    
    @Override
    public void simpleInitApp() {
        
        try{
            Server myServer = Network.createServer(6666);
            myServer.start();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        
    }
    public static void main(String[] args){
        
    }
}
