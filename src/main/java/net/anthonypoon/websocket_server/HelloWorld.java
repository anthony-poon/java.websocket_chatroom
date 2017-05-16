package net.anthonypoon.websocket_server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

@ServerEndpoint("/hello")
public class HelloWorld {
    private static final Map<String, Session> sessions = Collections.synchronizedMap(new HashMap<String, Session>());
    private String name;
    final static Logger logger = Logger.getLogger(HelloWorld.class);

    @OnOpen
    public void onOpen(Session session, EndpointConfig conf) {
        //try {
            String queryStr = session.getQueryString();
            Map<String, String> query = parseQueryString(queryStr);
            if (!query.containsKey("name")) {
                onError(session, new Exception("Missing name"));
            } else {
                name = query.get("name");
                sessions.put(session.getId(), session);
                logger.log(Level.INFO, name + " joined. ID = " + session.getId());
                Message msg = new Message();
                msg.setPayload(name + " joined the channel.");
                msg.setType(Message.Type.NORMAL);
                //boardcast(msg);
                session.getAsyncRemote().sendText(msg.toJson());
            }

        //} catch (IOException ex) {
        //    logger.log(Level.ERROR, ex.getMessage());
        //}

    }

    @OnMessage
    public void onMessage(Session session, String payload) {
        try {
            if (payload.startsWith("/")) {
                processCommand(payload);
            }
            logger.log(Level.INFO, payload);
            Message msg = new Message();
            msg.setPayload(payload);
            msg.setType(Message.Type.NORMAL);
            boardcast(msg);
        } catch (IOException ex) {
            logger.log(Level.ERROR, ex.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {

        logger.log(Level.ERROR, t.getMessage());
        for (StackTraceElement err : t.getStackTrace()) {
            logger.log(Level.ERROR, err);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        try {
            logger.log(Level.INFO, "Closed (" + reason.getCloseCode() + ") :" + reason.getReasonPhrase());
            String id = session.getId();
            synchronized (sessions) {
                sessions.remove(id);
            }
            Message msg = new Message();
            msg.setType(Message.Type.SYSTEM);
            msg.setPayload(name + " quitted. Reason: " + reason.getReasonPhrase());
            boardcast(msg);
        } catch (IOException ex) {
            logger.log(Level.ERROR, ex.getMessage());
        }
    }

    public List<String> getSessionId() {
        List<String> returnArray = new ArrayList();
        synchronized (sessions) {
            for (String id : sessions.keySet()) {
                returnArray.add(id);
            }
        }
        return returnArray;
    }

    private static Map<String, String> parseQueryString(String str) {
        Map<String, String> returnMap = new HashMap();
        String name = "";
        String value = "";

        for (String nameValuePair : str.split("&")) {

            if (nameValuePair.contains("=")) {
                name = StringEscapeUtils.escapeHtml4(nameValuePair.split("=")[0]);
                value = StringEscapeUtils.escapeHtml4(nameValuePair.split("=")[1]);

            } else {
                name = StringEscapeUtils.escapeHtml4(nameValuePair.split("=")[0]);
                value = "true";
            }
            returnMap.put(name, value);
        }
        return returnMap;
    }

    private void boardcast(Message msg) throws IOException {
        synchronized (sessions) {
            for (Session s : sessions.values()) {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(msg.toJson());
                    } catch (IOException ex) {
                        logger.log(Level.ERROR, ex.getMessage());
                    }
                }
            }
        }
    }

    private void processCommand(String str) throws IOException {
        switch (str) {
            case "/list-session":
                Message msg = new Message();
                msg.setPayload(StringUtils.join(getSessionId(), ", "));
                msg.setType(Message.Type.SYSTEM);
                boardcast(msg);
                break;
        }
    }
}
