/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.websocket_server;

import java.io.IOException;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.log4j.Logger;

/**
 *
 * @author ypoon
 */
@ServerEndpoint("/test")
public class Test {
    final static Logger logger = Logger.getLogger(HelloWorld.class);
    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            logger.info(message);
            session.getBasicRemote().sendText(message);
        } catch (IOException ex) {
        }
    }
    
}
