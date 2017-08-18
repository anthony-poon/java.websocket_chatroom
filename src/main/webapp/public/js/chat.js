function Chat() {
    var notification = $("#notification");
    var button = $("#submit-btn");
    var input = $("#input-field");
    notification.hide();
    this.notify = function(msg) {
        notification.find("#notification-content").html(msg);
        notification.fadeIn(1000, function(){
            setTimeout(function(){
                notification.fadeOut(1000);
            }, 3000);
        });
        
    }
    this.insertRow = function (input) {
        var element = $("<div class='row'></div>");
        var display = $("#chat-display");
        if (typeof input === "object") {
            element.html(input.payload);
            display.append(element);
        } else {
            try {
                var msgObj = JSON.parse(input);
                var str = msgObj.payload;
                var from = msgObj.fromName;
                if (from) {
                    str = "<span class='name'>" + from + ": </span>" + str;
                }
                if (msgObj.timestamp) {
                    var date = new Date(msgObj.timestamp);
                    str = "<span class='timestamp'>[" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "]</span> " + str;
                }
                
                element.html(str);
                switch (msgObj.type) {
                    case "NORMAL":
                        element.addClass("normal");
                        break;
                    case "SYSTEM":
                        element.addClass("system");
                        break;
                    case "ERROR":
                        element.addClass("error");
                        break;
                }
                display.append(element);
            } catch (ex) {
                element.html(input);
                display.append(element);
            }
        }
        
    };
    
    this.onSubmit = function(callback) {
        this.callback = callback
        button.off("click");
        button.click(callback);
        input.val("");
    }
}

$(document).ready(function(){
    var chat = new Chat();
    chat.insertRow({
        payload: "Please enter your name into the chat.",
        type: "NORMAL"
    });
    $("#input-field").keypress(function(evt){
            if (evt.keyCode == 13) {
                $("#submit-btn").trigger("click");
            }
        })
    chat.onSubmit(function(){
        var name = $("#input-field").val();
        var uri = URI.parse(window.location.href);
        var socket = new WebSocket("ws://"+uri.hostname+":"+uri.port+uri.path+"hello?name=" + name);
        socket.onopen = function(evt){
            return false;
        };
        socket.onmessage = function(evt){
            chat.insertRow(evt.data);
            console.log(evt.data);
            return false;
        }
        socket.onerror = function(evt) {
            console.log(evt);
        }
        socket.onclose = function(evt) {
            chat.insertRow("Connection closed ("+evt.code+")");
            console.log(evt);
        }
        chat.onSubmit(function(){
            socket.send($("#input-field").val());
            $("#input-field").val("");
        })
        
        
    });
});