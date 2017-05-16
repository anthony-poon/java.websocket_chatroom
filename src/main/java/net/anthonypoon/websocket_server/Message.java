/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.websocket_server;

import com.google.gson.Gson;

/**
 *
 * @author ypoon
 */
public class Message {
    public enum Type {
        NORMAL,
        ERROR,
        SYSTEM
    }
    private Type type = Type.NORMAL;
    private String payload = "";
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
    
    
}
