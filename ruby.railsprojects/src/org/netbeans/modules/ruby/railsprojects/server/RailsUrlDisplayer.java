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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.railsprojects.server;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.LocationMappersFactory;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionException;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * A Url displayer for Rails apps (refactored out from {@link RailsServerManager}).
 *
 * @author Erno Mononen
 */
final class RailsUrlDisplayer {

    private static final Logger LOGGER = Logger.getLogger(RailsUrlDisplayer.class.getName());
    /**
     * A hidden flag to turn off automatic browser display on server startup.
     * Should probably be exposed as a user visible option somewhere.
     */
    private static boolean NO_BROWSER = Boolean.getBoolean("rails.nobrowser");


    static void showURL(String contextRoot, String relativeUrl, int port, boolean runClientDebugger, RailsProject project) {

        if (NO_BROWSER) {
            return;
        }

        LOGGER.fine("Opening URL: " + "http://localhost:" + port + "/" + relativeUrl);
        try {
            URL url = new URL("http://localhost:" + port + contextRoot + "/" + relativeUrl); // NOI18N

            if (!runClientDebugger) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            } else {
                // launch browser with clientside debugger
                FileObject projectDocBase = project.getRakeProjectHelper().resolveFileObject("public"); // NOI18N
                String hostPrefix = "http://localhost:" + port + "/"; // NOI18N

                HtmlBrowser.Factory browser = null;
                if (WebClientToolsProjectUtils.isInternetExplorer(project)) {
                    browser = WebClientToolsProjectUtils.getInternetExplorerBrowser();
                } else {
                    browser = WebClientToolsProjectUtils.getFirefoxBrowser();
                }

                if (browser == null) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                    return;
                }

                LocationMappersFactory mapperFactory = Lookup.getDefault().lookup(LocationMappersFactory.class);

                Lookup debuggerLookup = null;
                if (mapperFactory != null) {
                    URI appContext = new URI(hostPrefix);

                    // If the public/index.html file exists assume that it is the welcome file.
                    Map<String, Object> extendedInfo = null;
                    FileObject welcomeFile = projectDocBase.getFileObject("index.html");  //NOI18N
                    if (welcomeFile != null) {
                        extendedInfo = new HashMap<String, Object>();
                        extendedInfo.put("welcome-file", "index.html"); //NOI18N
                    }

                    JSToNbJSLocationMapper forwardMapper =
                            mapperFactory.getJSToNbJSLocationMapper(projectDocBase, appContext, extendedInfo);
                    NbJSToJSLocationMapper reverseMapper =
                            mapperFactory.getNbJSToJSLocationMapper(projectDocBase, appContext, extendedInfo);
                    debuggerLookup = Lookups.fixed(forwardMapper, reverseMapper, project);
                } else {
                    debuggerLookup = Lookups.fixed(project);
                }

                WebClientToolsSessionStarterService.startSession(url.toURI(), browser, debuggerLookup);
            }
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (URISyntaxException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (WebClientToolsSessionException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
}
