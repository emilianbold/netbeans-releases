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

// ******************
// * RELOAD ON SAVE *
// ******************

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

page.on('error', function (error) {
    console.log(error.name + ': ' + error.message);
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

// *******************
// * PAGE INSPECTION *
// *******************

var contextMenu = require('context-menu');
var {Cc, Ci} = require('chrome');

var PageInspectionContext = function() {
    this.cleanup();
};

PageInspectionContext.currentContext = null;

PageInspectionContext.inspect = function() {
    if (this.currentContext) {
        this.currentContext.cleanup();
    }
    this.currentContext = new PageInspectionContext();
    this.currentContext.tab = tabs.activeTab;
    this.currentContext.initInspectedPage()
    this.currentContext.initBackgroundPage();
}

PageInspectionContext.prototype.cleanup = function() {
    var temp;
    this.inspectedPageReady = false;
    this.backgroundPageReady = false;
    this.pendingMessages = [];
    if (this.tabListener) {
        this.tab.removeListener('ready', this.tabListener);
    }
    this.tab = null;
    this.tabListener = null;
    if (this.inspectedPage) {
        temp = this.inspectedPage;
        this.inspectedPage = null;
        temp.destroy();
    }
    if (this.backgroundPage) {
        temp = this.backgroundPage;
        this.backgroundPage = null;
        temp.destroy();
    }
    if (PageInspectionContext.currentContext === this) {
        PageInspectionContext.currentContext = null;
    }
}

PageInspectionContext.prototype.initInspectedPage = function() {
    var that = this;
    this.tabId = this.tab[tabIdKey];
    this.inspectedPage = this.tab.attach({
        contentScriptFile: self.data.url('eval.js')
    });
    this.inspectedPage.on('message', function(message) {
        var type = message.message;
        if (type === 'ready') {
            that.inspectedPageReady = true;
            for (var i=0; i<that.pendingMessages; i++) {
                that.inspectedPage.postMessage(that.pendingMessages[i]);
            }
            that.pendingMessages = [];
        } else if (type === 'matchedRules') {
            var rules = that.getMatchedRules(message.element);
            that.backgroundPage.postMessage({
                message: 'eval',
                status: 'ok',
                id: message.id,
                result: rules
            });
        } else {
            that.backgroundPage.postMessage(message);
        }
    });
    this.inspectedPage.on('detach', function() {
        that.inspectedPage = null;
        that.cleanup();
    });
    // Detach is not fired during navigation to another page
    this.tabListener = function() {
        that.cleanup();
    };
    this.tab.on('ready', this.tabListener);
};

PageInspectionContext.prototype.initBackgroundPage = function() {
    var that = this;
    this.backgroundPage = pageWorkers.Page({
        contentURL: self.data.url('main.html'),
        contentScriptFile : self.data.url('inspect.js')
    });
    this.backgroundPage.on('message', function (message) {
        var type = message.message;
        if (type === 'ready') {
            that.backgroundPageReady = true;
            that.backgroundPage.postMessage({
                message: 'inspect',
                tabId: that.tabId
            });
        } else if (type === 'detach') {
            // Tab closed
            that.cleanup();
        } else if (type === 'failed') {
            // WebSocket connection not established,
            // notify user that Inspect action failed
            var worker = that.tab.attach({
                contentScript: 'window.alert("Unable to connect to NetBeans!"); self.postMessage("");',
                onMessage: function() {
                    // Alert shown => the worker can be destroyed
                    worker.destroy();
                }
            });
            that.cleanup();
        } else {
            if (that.inspectedPageReady) {
                that.inspectedPage.postMessage(message);
            } else {
                that.pendingMessages.push(message);
            }
        }
    });
    this.backgroundPage.on('error', function (error) {
        console.log(error.name + ': ' + error.message);
        that.cleanup();
    });
};

PageInspectionContext.prototype.getMatchedRules = function(elementHandle) {
    var matchedRules = [];
    var inIDOMUtils = Cc['@mozilla.org/inspector/dom-utils;1'].getService(Ci.inIDOMUtils);
    var wm = Components.classes['@mozilla.org/appshell/window-mediator;1']  
                .getService(Components.interfaces.nsIWindowMediator);  
    var browserEnumerator = wm.getEnumerator('navigator:browser');  

    while (browserEnumerator.hasMoreElements()) {  
        var browserWin = browserEnumerator.getNext();  
        var tabbrowser = browserWin.gBrowser;
        var numTabs = tabbrowser.browsers.length;
        for (var index = 0; index < numTabs; index++) {
            var browser = tabbrowser.getBrowserAtIndex(index);  
// PENDING more tabs with the same URL
            if (this.tab && this.tab.url === browser.currentURI.spec) {
                var document = browser.contentDocument;
                var element = this.findElement(document.documentElement, elementHandle);
                if (element === null) {
                    console.log('Element not found!');
                    return matchedRules;
                } else {
                    var rules = inIDOMUtils.getCSSStyleRules(element);
                    var numRules = rules.Count();
                    // The specificity of the returned rules increases with
                    // the increasing index => we process them in the opposite order
                    for (var i = numRules-1; i >= 0; i--) {
                        var rule = rules.GetElementAt(i);
                        var ruleInfo = {};
                        ruleInfo.sourceURL = rule.parentStyleSheet.href;
                        ruleInfo.selector = rule.selectorText;
                        var styleInfo = new Object();
                        ruleInfo.style = styleInfo;
                        var style = rule.style;
                        for (var j=0; j<style.length; j++) {
                            var stylename = style[j];
                            var stylevalue = style.getPropertyValue(stylename);
                            styleInfo[stylename] = stylevalue;
                        }
                        matchedRules.push(ruleInfo);
                    }
                }
                return matchedRules;
            }
        }
    }
    return matchedRules;
};

PageInspectionContext.prototype.findElement = function(parent, elementHandle) {
    if (elementHandle.length === 0) {
        return parent;
    }
    var index = elementHandle.shift();
    var child = parent.firstChild;
    while (child && (index >= 0)) {
        if (child && child.nodeType === 1) {
            if (index === 0) {
                return this.findElement(child, elementHandle);
            } else {
                index--;
            }
        }
        child = child.nextSibling;
    }
    return null;
};

// Unfortunately, the main script (=this script) doesn't support WebSockets.
// Hence, we have to start WebSocket communication in some content script.
// Content script of the inspected page cannot be used for this purpose because
// FireFox doesn't allow ws: (i.e., unsecured WebSocket connections) from
// pages with https: schema. Therefore, we use a background page for that.
contextMenu.Item({
    label: "Inspect in NetBeans",
    image: self.data.url('netbeans16.png'),
    context: [
        contextMenu.URLContext(["file://*", "http://*", "https://*"]),
        contextMenu.SelectorContext("*")
    ],
    contentScript: 'self.on("click", function () {' +
                   '  self.postMessage("inspect");' +
                   '});',
    onMessage: function(message) {
        if (message === 'inspect') {
            PageInspectionContext.inspect();
        } else {
            console.log('Unexpected message from the content script of the menu item!');
        }
    }
});
