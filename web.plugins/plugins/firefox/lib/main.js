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

var script =
  "var socket = new MozWebSocket('ws://127.0.0.1:1234');"+
  "self.on('message', function (message) {"+
  "  if (socket.readyState === socket.OPEN) {"+
  "    socket.send(message);"+
  "  }"+
  "});"+
  "socket.onopen=function () {"+
  "  self.postMessage('init');"+
  "};"+
  "socket.onmessage=function (e) {"+
  "  self.postMessage(e.data);"+
  "};";
 
var page = pageWorkers.Page({
  contentURL: 'http://www.google.com', // PENDING replace with url to some dummy page
  contentScript: script,
  contentScriptWhen: 'start',
});

var tabs = require("tabs");

page.on('message', function (message) {
  var tab;
  if (message === 'init') {
    for (var i=0; i<tabs.length; i++) {
      tab = tabs[i];
      assignIdIfNeeded(tab);
      page.postMessage('id '+tab[tabIdKey]+'\nurl '+tab.url+'\nstatus unknown\n');
    }
  } else {
    // Reload request
    for (var i=0; i<tabs.length; i++) {
      tab = tabs[i];
      if (tab[tabIdKey] == message) {
        tab.reload();
      }
    }
  }
});
 
tabs.on('open', function (tab) {
  assignIdIfNeeded(tab);
  page.postMessage('id '+tab[tabIdKey]+'\nstatus loading\n');
});

tabs.on('ready', function (tab) {
  assignIdIfNeeded(tab);
  page.postMessage('id '+tab[tabIdKey]+'\nurl '+tab.url+'\nstatus complete\n');
});

tabs.on('close', function (tab) {
  assignIdIfNeeded(tab);
  page.postMessage('id '+tab[tabIdKey]+'\nclosed true\n');
});

var tabId=0;
var tabIdKey = 'netbeans-tab-id';
function assignIdIfNeeded(tab) {
  if (tab[tabIdKey] === undefined) {
    tab[tabIdKey] = tabId++;
  }
}
