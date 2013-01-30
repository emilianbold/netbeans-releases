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

NetBeans = new Object();

NetBeans.serverURL = function() {
    var serverProtocol = 'ws';
    var serverHost = '127.0.0.1';
    var serverPort = 8008;
    var serverFile = '/';
    return serverProtocol+'://'+serverHost+':'+serverPort+serverFile;
}

NetBeans.DEBUG = true;

NetBeans.managedTabs = new Object();

NetBeans.STATUS_NEW = 0;
NetBeans.STATUS_UNCONFIRMED = 1;
NetBeans.STATUS_MANAGED = 2;
NetBeans.STATUS_NOT_MANAGED = 3;

NetBeans.tabStatus = function(tabId) {
    var tabInfo = this.managedTabs[tabId];
    var status;
    if (tabInfo === undefined) {
        status = this.STATUS_NOT_MANAGED;
    } else {
        status = tabInfo.status;
    }
    return status;
}

NetBeans.cleanup = function() {
    this.socket = null;
    this.socketReady = false;
    this.pendingMessages = [];
}

NetBeans.connectIfNeeded = function() {
    if (this.socket === null) {
        var self = this;
        if (typeof(WebSocket) === 'undefined') {
            this.socket = new MozWebSocket(this.serverURL());
        } else {
            this.socket = new WebSocket(this.serverURL());
        }
        this.socket.onerror = function(e) {
            console.log('Socket error!');
            if (e.name && e.message) {
                console.log(e.name + ': ' + e.message);
            }
            self.cleanup();
        }
        this.socket.onclose = function() {
            self.cleanup();
        }
        this.socket.onopen = function() {
            self.socketReady = true;
            self.sendPendingMessages();
        }
        this.socket.onmessage = function(e) {
            if (self.DEBUG) {
                console.log('Received message: ' + e.data);
            }
            var message;
            try {
                message = JSON.parse(e.data);
            } catch (err) {
                console.log('Message not in JSON format!');
                console.log(err);
                console.log(e.data);
                return;
            }
            self.processMessage(message);
        }
    }
    return this.socketReady;    
}

NetBeans.sendMessage = function(message) {
    if (this.connectIfNeeded()) {
        var messageText = JSON.stringify(message);
        if (this.DEBUG) {
            console.log('Sent message: ' + messageText);
        }
        this.socket.send(messageText);
    } else {
        this.pendingMessages.push(message);
    }
}

NetBeans.sendInitMessage = function(tab) {
    this.sendMessage({
        message: 'init',
        url: tab.url,
        tabId: tab.id
    });
}

NetBeans.sendCloseMessage = function(tabId) {
    this.sendMessage({
        message: 'close',
        tabId: tabId
    });
}

NetBeans.sendUrlChangeMessage = function(tabId, url) {
    this.sendMessage({
        message: 'urlchange',
        tabId: tabId,
        url: url
    });
}

NetBeans.sendPendingMessages = function() {
    for (var i=0; i<this.pendingMessages.length; i++) {
        this.sendMessage(this.pendingMessages[i]);
    }
    this.pendingMessages = [];
}

NetBeans.processMessage = function(message) {
    var type = message.message;
    if (type === 'init') {
        this.processInitMessage(message);
    } else if (type === 'reload') {
        this.processReloadMessage(message);
    } else {
        console.log('Unsupported message!');
        console.log(message);
    }
}

NetBeans.tabIdFromMessage = function(message) {
    var tabIdValue = message.tabId;
    var tabId;
    if (typeof(tabIdValue) === 'number') {
        tabId = tabIdValue;
    } else if (typeof(tabIdValue) === 'string') {
        tabId = parseInt(tabIdValue);
    } else {
        console.log('Missing/incorrect tabId attribute!');
        console.log(message);
    }
    return tabId;
}

NetBeans.processInitMessage = function(message) {
    var tabId = this.tabIdFromMessage(message);
    if (tabId !== undefined) {
        var tabInfo = this.managedTabs[tabId];
        if (tabInfo === undefined) {
            console.log('Ignoring init message for an unknown tab: '+tabId);
        } else if (tabInfo.status === this.STATUS_UNCONFIRMED) {
            if (message.status === 'accepted') {
                // Tab should be managed
                if (tabInfo.closed) {
                    // Delayed confirmation request for already closed tab;
                    // for a tab whose URL changed
                    this.sendCloseMessage(tabId);
                    delete this.managedTabs[tabId];
                } else {
                    tabInfo.status = this.STATUS_MANAGED;
                }
            } else {
                // Tab shouldn't be managed
                delete this.managedTabs[tabId];
            }
        } else {
            console.log('Ignoring init message for a tab for which such message was not requested: '+tabId);
        }
    }
}

NetBeans.processReloadMessage = function(message) {
    var tabId = this.tabIdFromMessage(message);
    if (tabId !== undefined) {
        var status = this.tabStatus(tabId);
        if (status === this.STATUS_MANAGED) {
            this.browserReloadCallback(tabId, message.url);
        } else {
            console.log('Refusing to reload tab that is not managed: '+tabId);
        }
    }
}

NetBeans.tabCreated = function(tabId) {
    this.managedTabs[tabId] = {status: this.STATUS_NEW};
}

NetBeans.tabUpdated = function(tab) {
    var status = this.tabStatus(tab.id);
    var tabInfo = this.managedTabs[tab.id];
    if (status === this.STATUS_NEW) {
        tabInfo.status = this.STATUS_UNCONFIRMED;
        tabInfo.url = tab.url;
        // Send URL to IDE - ask if the tab is managed
        this.sendInitMessage(tab);
    } else if ((tabInfo !== undefined) && (tabInfo.url !== tab.url)) {
        // URL change should not mean that tab was closed; it may notify
        // IDE that different page is opened in the browser pane if such knowledge
        // of such state is desirable.
        if (status === this.STATUS_UNCONFIRMED) {
            // Navigation in an unconfirmed tab
            // Confirmation may be delayed; do nothing for now
        } else if (status === this.STATUS_MANAGED) {
            // Navigation in a managed tab => send "urlchange" message
            this.sendUrlChangeMessage(tab.id, tab.url);
        }
    }
}

NetBeans.tabRemoved = function(tabId) {
    var status = this.tabStatus(tabId);
    if (status === this.STATUS_UNCONFIRMED) {
        // Unconfirmed tab was closed
        // Confirmation may be delayed; Mark it such that we know that
        // "close" message should be sent if such delayed confirmation arrives
        this.managedTabs[tabId].closed = true;
    } else if (status === this.STATUS_MANAGED) {
        // Managed tab was closed => send "closed" message
        this.sendCloseMessage(tabId);
    }
    if (status !== this.STATUS_UNCONFIRMED) {
        // Remove the tab from the set of managed tabs (if it was there)
        delete this.managedTabs[tabId];
    }
}
