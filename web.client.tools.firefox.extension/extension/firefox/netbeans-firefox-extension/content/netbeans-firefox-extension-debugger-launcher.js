/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Sandip V. Chitale (sandipchitale@netbeans.org)
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

(function() {
    
    // Version
    this.VERSION = NetBeans.Constants.VERSION;
    
    // function
    function onChromeLoad (e) {
        // Chrome window loaded. Remove listener.
        window.removeEventListener("load", onChromeLoad, false);

        // Listen to loading of URLs
        window.getBrowser().addEventListener("load", onLoadURL, true);
    }

    // function
    function onLoadURL(e) {
        if (e.originalTarget instanceof HTMLDocument) {
            var doc = e.originalTarget;
            var href = doc.location.href;
            // Does the URL match the magic URL
            if (href && href.indexOf('modules.ext.debugger---') != -1) {
                // extract arguments
                var search = href.substring(href.lastIndexOf("---") + 3);
                search = search.substring(0, search.length - 5);
                if (search) {
                    // split arguments
                    var searchArgs = search.split("--");
                    var port = undefined;
                    var sessionId = undefined;
                    for (var i = 0; i < searchArgs.length; i++) {
                        var searchArg = searchArgs[i];

                        // split name value pair
                        var nameValue= searchArg.split("=");

                        if (nameValue[0] == "netbeans-debugger-port") {
                            // port
                            port = nameValue[1];
                        } else if (nameValue[0] == "netbeans-debugger-session-id") {
                            // session id
                            sessionId = nameValue[1];
                        }
                    }
                    // found port and sessin id
                    if (port && sessionId) {
                        // this is the relaunched new window or the very first browser window
                        if (window.getBrowser().browsers.length == 1 &&  window.getBrowser().getBrowserAtIndex(0).currentURI.spec == href) {
                            window.setTimeout(
                            function() {
                                try {
                                  var shellService = NetBeans.Utils.CCSV(
                                  NetBeans.Constants.ShellServiceCID,
                                  NetBeans.Constants.ShellServiceIF);
                                  if ( shellService && shellService.shouldCheckDefaultBrowser ) {
                                      /* do check to get subsequest calls to
                                       * nsIShellService.shouldCheckDefaultBrowser return false
                                       */
                                      shellService.isDefaultBrowser(true);
                                  }
                                } catch (ex) {
                                    NetBeans.Logger.logMessage(ex.message);
                                }
                                NetBeans.Debugger.initDebugger(port, sessionId);
                            }
                            ,1000);
                        } else {
                            // relaunched in a new window
                            window.openDialog(
                            "chrome://browser/content/browser.xul", // launch Browser
                            "_blank",                               // new Browser window
                            "chrome,centerscreen,dialog=0,menubar=1,location=1,toolbar=1,directories=1,status=1,resizable=1,scrollbars=1",
                            href);
                            
                            closeTabForURL(window.getBrowser(), href);
                        }
                    }
                }
            }
        }
    }

    // Based off example code in: http://developer.mozilla.org/en/Code_snippets/Tabbed_browser
    function closeTabForURL(tabbrowser, url) {
            var numTabs = tabbrowser.browsers.length;
            for(var index=0; index<numTabs; index++) {
                var currentBrowser = tabbrowser.getBrowserAtIndex(index);
                if (url == currentBrowser.currentURI.spec) {
                    var tabForURL = tabbrowser.mTabs[index];
                    tabbrowser.removeTab(tabForURL);
                    return true;
                }
            }

            return false;
        }

    
    // Listen on loading of chrome window
    window.addEventListener("load", onChromeLoad, false);
}).apply(NetBeans.DebuggerLauncher);
