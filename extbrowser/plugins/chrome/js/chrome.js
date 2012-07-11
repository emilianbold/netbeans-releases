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
 * http://www.NetBeans.org/cddl-gplv2.html
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

// Initialization/cleanup
NetBeans.cleanup();

// Register reload-callback
NetBeans.browserReloadCallback = function(tabId, newUrl) {
    if (newUrl != undefined) {
        chrome.tabs.get(tabId, function(tab) {
            if (tab.url === newUrl) {
                chrome.tabs.reload(tabId, {bypassCache: true});
            } else {
                chrome.tabs.update(tabId, {url: newUrl});
            }
        });
    } else {
        chrome.tabs.reload(tabId, {bypassCache: true});
    }
}

NetBeans.browserCloseCallback = function(tabId) {
    chrome.tabs.remove(tabId);
}

NetBeans.browserAttachDebugger = function(tabId) {
    if (NetBeans.DEBUG) {
        console.log('debugger attach for tab ' + tabId);
    }
    chrome.debugger.attach({tabId : tabId}, "1.0", function(){
        if (chrome.extension.lastError) {
            console.log('debugger attach result code: ' + chrome.extension.lastError);
        }
    });
}

NetBeans.browserDetachDebugger = function(tabId) {
    if (NetBeans.DEBUG) {
        console.log('debugger detaching from tab ' + tabId);
    }
    chrome.debugger.detach({tabId : tabId});
}

// display NB icon in URL bar
NetBeans.showPageIcon = function(tabId) {
    chrome.pageAction.show(tabId);
}
// hide NB icon in URL bar
NetBeans.hidePageIcon = function(tabId) {
    chrome.pageAction.hide(tabId);
}
// show infobar
NetBeans.showInfoBar = function(tabId) {
    chrome.experimental.infobars.show({
        tabId : tabId,
        path: 'html/infobar.html'
    });
}
NetBeans.resetPageSize = function(callback) {
    chrome.windows.getLastFocused(function(win) {
        var opt = {};
        opt.state = 'maximized';
        chrome.windows.update(win.id, opt);
        if (callback) {
            callback();
        }
    });
}
NetBeans.resizePage = function(preset, callback) {
    if (preset == null) {
        this.resetPageSize(callback);
        return;
    }
    var data = NetBeans_Presets.getPreset(preset);
    if (data == null) {
        console.error('Preset [' + preset + '] not found.');
        return;
    }
    this._resizePage(data['width'], data['height'], callback);
}
// resize actual page
NetBeans._resizePage = function(width, height, callback) {
    chrome.windows.getLastFocused(function(win) {
        var opt = {};
        opt.state = 'normal';
        opt.width = parseInt(width);
        opt.height = parseInt(height);
        chrome.windows.update(win.id, opt);
        if (callback) {
            callback();
        }
    });
}
// show preset customizer
NetBeans.showPresetCustomizer = function() {
    chrome.tabs.create({'url': 'html/options.html'});
}

NetBeans.browserSendCommand = function(tabId, id, method, params, callback) {
    if (NetBeans.DEBUG) {
        console.log('send ['+tabId+","+id+","+method+","+JSON.stringify(params));
    }
    chrome.debugger.sendCommand({tabId : tabId}, method, params, 
        function(result) {
            if (chrome.extension.lastError) {
                console.log('debugger send command result code: ' + chrome.extension.lastError);
            } else {
                console.log('debugger send command response: ' + result);
                NetBeans.sendDebuggingResponse(tabId, {id : id, result : result});
            }
        });
}

// "fired" when presets changed
NetBeans_Presets.presetsChanged = function() {
    // no need to refresh popup, refresh only infobar(s)
    var views = chrome.extension.getViews({type: "infobar"});
    console.log('Refreshing ' + views.length +  ' infobars');
    for (var i in views) {
        var view = views[i];
        view.NetBeans_Infobar.redrawPresets();
    }
}


chrome.debugger.onEvent.addListener(function(source, method, params) {
    NetBeans.sendDebuggingResponse(source.tabId, {method : method, params : params});
});

chrome.debugger.onDetach.addListener(function(source) {
    NetBeans.hidePageIcon(source.tabId);
});

// Register tab listeners
chrome.tabs.onCreated.addListener(function(tab) {
    NetBeans.tabCreated(tab.id);
});
chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
    NetBeans.tabUpdated(tab);
});
chrome.tabs.onRemoved.addListener(function(tabId) {
    NetBeans.tabRemoved(tabId);
});

// onCreated event is not delivered for the first tab;
// As a workaround, we go through all existing tabs and consider them as new.
// onUpdated event is not delivered sometimes as well for the first tab;
// Hence, we consider also tab urls that are known already.
chrome.windows.getAll({populate: true}, function(windows) {
    for (var i=0; i<windows.length; i++) {
        var window = windows[i];
        for (var j=0; j<window.tabs.length; j++) {
            var tab = window.tabs[j];
            NetBeans.tabCreated(tab.id);
            var url = tab.url;
            if (url !== undefined && url !== null && url.length !== 0) {
                // URL of the tab is known already
                NetBeans.tabUpdated(tab);
            }
        }
    }    
});
