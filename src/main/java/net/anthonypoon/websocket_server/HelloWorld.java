package net.anthonypoon.websocket_server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

@ServerEndpoint("/hello")
public class HelloWorld {
    private static final Map<String, Session> sessions = Collections.synchronizedMap(new HashMap<String, Session>());
    private static final Map<String, String> names = Collections.synchronizedMap(new HashMap<String, String>());
    private String name;
    final static Logger logger = Logger.getLogger(HelloWorld.class);

    @OnOpen
    public void onOpen(Session session, EndpointConfig conf) {
        String queryStr = session.getQueryString();
        Map<String, String> query = parseQueryString(queryStr);
        if (!query.containsKey("name")) {
            onError(session, new Exception("Missing name"));
        } else {
            name = query.get("name");
            sessions.put(session.getId(), session);
            names.put(session.getId(), name);
            logger.log(Level.INFO, name + " joined. ID = " + session.getId());
            Message msg = new Message();
            msg.setPayload(name + " joined the channel.");
            msg.setType(Message.Type.NORMAL);
            boardcast(msg);
        }
    }

    @OnMessage
    public void onMessage(Session session, String payload) {
        try {
            if (payload.startsWith("/")) {
                processCommand(payload);
            }
            logger.log(Level.INFO, payload);
            Message msg = new Message();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            msg.setFromName(name);
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
        logger.log(Level.INFO, "Closed (" + reason.getCloseCode() + ") :" + reason.getReasonPhrase());
        String id = session.getId();
        synchronized (sessions) {
            sessions.remove(id);
        }
        Message msg = new Message();
        msg.setType(Message.Type.SYSTEM);
        String reasonStr = String.valueOf(reason.getCloseCode().getCode()) + " - " + reason.toString();
        switch (reason.getCloseCode().getCode()) {
            case 1001:
            case 1000:
                reasonStr = "User disconnected.";
                break;
            default:
                break;
        }
        msg.setPayload(name + " quitted. Reason: " + reasonStr);
        boardcast(msg);
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
    
    public List<String> getNames() {
        List<String> returnArray = new ArrayList();
        synchronized (names) {
            for (String name : names.values()) {
                returnArray.add(name);
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

    private void boardcast(Message msg){
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
            case "/list-name":
                msg = new Message();
                msg.setPayload(StringUtils.join(getNames(), ", "));
                msg.setType(Message.Type.SYSTEM);
                boardcast(msg);
                break;
        }
    }
}
