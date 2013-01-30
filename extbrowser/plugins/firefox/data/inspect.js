/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

socketReady = false;
csIDtoSocketID = new Object();
lastIDSentToCS = 0;
lastIDGotFromCS = 0;
inspectMessage = null;

serverURL = 'ws://127.0.0.1:8008/';
if (WebSocket) {
    socket = new WebSocket(serverURL);
} else {
    socket = new MozWebSocket(serverURL);
}

socket.onerror = function(e) {
    console.log('Socket error!');
    if (socketReady) {
        sendDetachMessage();
    } else {
        sendFailedMessage();
    }
};

socket.onopen = function() {
    socketReady = true;
    if (inspectMessage) {
        socket.send(JSON.stringify(inspectMessage));
        inspectMessage = null;
    }
};

socket.onclose = function() {
    sendDetachMessage();
};

socket.onmessage = function(e) {
    var message;
    try {
        message = JSON.parse(e.data);
    } catch (err) {
        console.log('Message not in JSON format!');
        console.log(err);
        console.log(e.data);
        return;
    }
    processMessageFromSocket(message);
};

processMessageFromSocket = function(message) {
    var type = message.message;
    if (type === 'eval') {
        lastIDSentToCS++;
        csIDtoSocketID[lastIDSentToCS] = message.id;
        self.postMessage({
            message: 'eval',
            id: lastIDSentToCS,
            script: message.script
        });
    } else {
        console.log('Ignoring unexpected message from the socket!');
        console.log(message);
    }
};

processMessageFromInspectedPage = function(message) {
    var type = message.message;
    if (type === 'eval') {
        var id = message.id;
        if (typeof(id) === 'number') {
            var response;
            for (var i=lastIDGotFromCS+1; i<id; i++) {
                // We got no response for these IDs for some reason
                // => notify the client over the socket
                response = JSON.stringify({
                    message: 'eval',
                    result: 'No response from the content script.',
                    status: 'error',
                    id: csIDtoSocketID[i]
                });
                socket.send(response);
                delete csIDtoSocketID[i];
            }
            response = JSON.stringify({
                message: 'eval',
                result: message.result,
                status: message.status,
                id: csIDtoSocketID[id]
            });
            socket.send(response);
            delete csIDtoSocketID[id];
            lastIDGotFromCS = id;
        } else {
            console.log('Ignoring message (from the content script) with a malformed ID.');
            console.log(message);
        }
    } else if (type === 'selection') {
        socket.send(JSON.stringify(message));
    } else {
        console.log('Ignoring unexpected message from the content script!');
        console.log(message);        
    }    
};

sendDetachMessage = function() {
    try {
        self.postMessage({
            message: 'detach'
        });
    } catch (e) {
        // We are detached already
    }
};

sendFailedMessage = function() {
    self.postMessage({
        message: 'failed'
    });
}

self.on('message', function(message) {
    if (message.message === 'inspect') {
        if (socketReady) {
            socket.send(JSON.stringify(message));
        } else {
            inspectMessage = message;
        }
    } else {
        processMessageFromInspectedPage(message);
    }
});

// Notify the main script that this script is ready
self.postMessage({
    message: 'ready'
});
