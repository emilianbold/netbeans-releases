/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

var pageWorkers = require("page-worker");
var self = require("self");
var tabs = require("tabs");

var pendingMessages = [];
var backgroundPageReady = false;

// Tabs don't have IDs by default - we assign them our own IDs.
var tabId=0;
var tabIdKey = 'netbeans-tab-id';
assignIdIfNeeded = function(tab) {
  if (tab[tabIdKey] === undefined) {
    tab[tabIdKey] = tabId++;
  }
}

sendMessage = function(message) {
    // page.postMessage() doesn't work until the background page is loaded/ready
    if (backgroundPageReady) {
        page.postMessage(message);
    } else {
        pendingMessages.push(message);
    }
}

sendOpenMessage = function(tabId) {
    sendMessage({
        type: 'open',
        tabId: tabId
    });
}

sendCloseMessage = function(tabId) {
    sendMessage({
        type: 'close',
        tabId: tabId
    });
}

sendReadyMessage = function(tabId, url) {
    sendMessage({
        type: 'ready',
        tabId: tabId,
        url: url
    });
}

// WebSockets are not available to this script for some reason.
// So, we create a background page where the WebSockets are available
// and send information from privilege APIs (available to this script only)
// to the background page.
var page = pageWorkers.Page({
  contentURL: self.data.url('main.html'),
  contentScriptFile : [
      self.data.url('reload.js'),
      self.data.url('reloadInit.js')
  ]
});

page.on('message', function (message) {
    var type = message.type;
    var i;
    if (type === 'reload') {
        // reload request from IDE
        for (i=0; i<tabs.length; i++) {
            tab = tabs[i];
            if (tab[tabIdKey] === message.tabId) {
                if (message.url != undefined) {
                    tab.url = message.url;
                } else {
                    tab.reload();
                }
            }
        }
    } else if (type === 'backgroundPageReady') {
        // background page is loaded
        backgroundPageReady = true;
        for (i=0; i<pendingMessages.length; i++) {
            sendMessage(pendingMessages[i]);
        }
    }
});

// Register event listeners
tabs.on('open', function (tab) {
    assignIdIfNeeded(tab);
    sendOpenMessage(tab[tabIdKey]);
});

tabs.on('ready', function (tab) {
    assignIdIfNeeded(tab);
    sendReadyMessage(tab[tabIdKey], tab.url);
});

tabs.on('close', function (tab) {
    assignIdIfNeeded(tab);
    sendCloseMessage(tab[tabIdKey]);
});

// 'open' event is not delivered for the first tab;
// As a workaround, we go through all existing tabs and consider them as new
// We also consider known urls of existing tabs because it is not clear
// if we always get 'ready' event for the first tab(s).
for each (var tab in tabs) {
    assignIdIfNeeded(tab);
    sendOpenMessage(tab[tabIdKey]);
    var url = tab.url;
    if (url !== undefined && url !== null && url.length !== 0 && url !== 'about:blank') {
        // URL of the tab is known already
        sendReadyMessage(tab[tabIdKey], url);
    }
}
