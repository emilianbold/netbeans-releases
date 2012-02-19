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

var Context = function() {
    this.cleanup();
}

Context.currentContext = null;

Context.prototype.cleanup = function() {
    var self = this;
    if (this.socket) {
        if (this.socket.readyState === WebSocket.OPEN) {
            this.socket.close();
        } else if (this.socket.readyState == this.socket.CONNECTING) {
            this.socket.onopen = function() {
                self.close();
            }
        }
    }
    this.socket = null;
    if (this.port) {
        this.port.disconnect();
    }
    this.port = null;
    this.pendingMessages = [];
    this.csIDtoSocketID = new Object();
    this.lastIDSentToCS = 0;
    this.lastIDGotFromCS = 0;
    if (Context.currentContext === this) {
        Context.currentContext = null;
    }
}

Context.prototype.initSocket = function(tabId) {
    var self = this;
    this.socket = new WebSocket("ws://127.0.0.1:8010/");
    this.socket.onerror = function(e) {
        console.log('Socket error!');
        console.log(e);
        self.cleanup();
    }
    this.socket.onopen = function() {
        self.connectedSuccessfully = true;
    }
    this.socket.onclose = function() {
        if (!self.connectedSuccessfully) {
            var message = 'Unable to connect to NetBeans!';
            chrome.tabs.executeScript(tabId, {code: "alert('"+message+"');"}, function() {
                var error = chrome.extension.lastError;
                if (error) {
                    // Attempt to show the message in the tab failed for some
                    // reason => show the message using a standalone dialog
                    alert(message);
                }
            });
        }
        self.cleanup();
    }
    this.socket.onmessage = function(e) {
        var message;
        try {
            message = JSON.parse(e.data);
        } catch (err) {
            console.log('Message not in JSON format!');
            console.log(err);
            console.log(e.data);
            return;
        }
        self.processMessageFromSocket(message);
    }    
}

Context.prototype.processMessageFromSocket = function(message) {
    if (this.port === null) {
        this.pendingMessages.push(message);
    } else {
        var type = message.message;
        if (type === 'eval') {
            this.lastIDSentToCS++;
            this.csIDtoSocketID[this.lastIDSentToCS] = message.id;
            this.port.postMessage({
                message: 'eval',
                id: this.lastIDSentToCS,
                script: message.script
            });
        } else {
            console.log('Ignoring unexpected message from the socket!');
            console.log(message);
        }
    }
}

Context.prototype.initContentScript = function(tabId) {
    var self = this;
    chrome.tabs.executeScript(tabId, {file: 'eval.js'}, function() {
        var error = chrome.extension.lastError;
        if (error) {
            console.log('Error during content script injection!');
            console.log(error);
            self.cleanup();
            if (error instanceof Error) {
                error = error.name + ',' + error.message;
            } else {
                try {
                    error = JSON.stringify(error);
                } catch (err) {}
            }
            alert('Unable to Inspect this page because of the following error: '+error);
            return;
        }
        // Now the script is there => contact it
        self.port = chrome.tabs.connect(tabId);
        self.port.onMessage.addListener(function(message) {
            self.processMessageFromCS(message);
        });
        self.port.onDisconnect.addListener(function() {
            // Navigation to another page
            self.cleanup();
        });
        for (var i=0; i<self.pendingMessages.length; i++) {
            self.processMessageFromSocket(self.pendingMessages[i]);
        }
        self.pendingMessages = [];
    });
}

Context.prototype.processMessageFromCS = function(message) {
    var type = message.message;
    if (type === 'eval') {
        var id = message.id;
        if (typeof(id) === 'number') {
            var response;
            for (var i=this.lastIDGotFromCS+1; i<id; i++) {
                // We got no response for these IDs for some reason
                // => notify the client over the socket
                response = JSON.stringify({
                    message: 'eval',
                    result: 'No response from the content script.',
                    status: 'error',
                    id: this.csIDtoSocketID[i]
                });
                this.socket.send(response);
                delete this.csIDtoSocketID[i];
            }
            response = JSON.stringify({
                message: 'eval',
                result: message.result,
                status: message.status,
                id: this.csIDtoSocketID[id]
            });
            this.socket.send(response);
            delete this.csIDtoSocketID[id];
            this.lastIDGotFromCS = id;
        } else {
            console.log('Ignoring message (from the content script) with a malformed ID.');
            console.log(message);
        }
    } else {
        console.log('Ignoring unexpected message from the content script!');
        console.log(message);        
    }    
}

Context.inspectTab = function(info, tab, accessConfirmed) {
    if (tab.url.indexOf('file:') === 0 && !accessConfirmed) {
        // Check if we have access to files
        chrome.extension.isAllowedFileSchemeAccess(function(allowed) {
            if (allowed) {
                Context.inspectTab(info, tab, allowed);
            } else {
                alert('NetBeans IDE Support Plugin is not allowed to access file URLs. '
                    +'You can allow this access in "wrench menu" > Tools > Extensions. '
                    +'(Expand the handle next to the extension and select the corresponding checkbox.)');
            }
        });
    } else {
        if (this.currentContext) {
            this.currentContext.cleanup();
        }
        this.currentContext = new Context();
        this.currentContext.initSocket(tab.id)
        this.currentContext.initContentScript(tab.id);
    }
}

chrome.contextMenus.create({
    title: 'Inspect in NetBeans',
    onclick: Context.inspectTab,
    documentUrlPatterns: [
        'http://*/*',
        'https://*/*',
        'file://*/*',
    ]
});
