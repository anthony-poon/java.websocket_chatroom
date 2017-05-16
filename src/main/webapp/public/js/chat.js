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
                element.html(msgObj.payload);
                display.append(element);
            } catch (ex) {
                element.html(input);
                display.append(element);
            }
        }
        
    };
    
    this.onSubmit = function(callback) {
        button.off("click");
        button.click(callback);
        input.val("");
    }
}

$(document).ready(function(){
    var chat = new Chat();
    chat.insertRow({
        payload: "Who are you",
        type: "NORMAL"
    });
    chat.onSubmit(function(){
        var name = $("#input-field").val();
        var uri = URI.parse(window.location.href);
        var socket = new WebSocket("ws://"+uri.hostname+":"+uri.port+uri.path+"test?name=" + name);
        socket.onopen = function(evt){
            return false;
        };
        socket.onmessage = function(evt){
            chat.insertRow(evt.data);
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