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

package org.netbeans.modules.web.client.javascript.debugger.ant;

import java.net.URI;

import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.LocationMappersFactory;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * Ant task for web projects
 *
 * @author quynguyen
 */
public class NbJsDebugStart extends Task {
    private String webUrl;
    private String urlPart;

    public String getUrlPart() {
        return urlPart;
    }

    public void setUrlPart(String urlPart) {
        this.urlPart = urlPart;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    @Override
    public void execute() throws BuildException {
        if (webUrl == null) {
            throw new BuildException("The weburl attribute must be set to the client URL"); // NOI18N
        }

        try {
            FileObject projectDir = FileUtil.toFileObject(getProject().getBaseDir());
            projectDir.refresh();

            Project nbProject = FileOwnerQuery.getOwner(projectDir);
            WebModule wm = WebModule.getWebModule(projectDir);

            FileObject projectDocBase = wm.getDocumentBase();

            WebApp app = DDProvider.getDefault().getDDRoot(wm.getDeploymentDescriptor());
            WelcomeFileList list = app.getSingleWelcomeFileList();
            String[] welcomeFiles = list != null ? list.getWelcomeFile() : null;

            String welcomeFile = null;

            if (welcomeFiles != null && welcomeFiles.length > 0) {
                welcomeFile = welcomeFiles[0];
            }

            String serverPrefix = getWebBaseUrl();
            
            LocationMappersFactory mapperFactory = Lookup.getDefault().lookup(LocationMappersFactory.class);
            
            Lookup debuggerLookup = null;
            if (mapperFactory != null) {
                URI appContext = new URI(serverPrefix);
                Map<String, Object> extendedInfo = null;
                
                if (welcomeFile != null) {
                    extendedInfo = new HashMap<String, Object>();
                    extendedInfo.put("welcome-file", welcomeFile); //NOI18N
                }
                
                JSToNbJSLocationMapper forwardMapper = 
                        mapperFactory.getJSToNbJSLocationMapper(projectDocBase, appContext, extendedInfo);
                NbJSToJSLocationMapper reverseMapper = 
                        mapperFactory.getNbJSToJSLocationMapper(projectDocBase, appContext, extendedInfo);
                debuggerLookup = Lookups.fixed(forwardMapper, reverseMapper, nbProject);
            } else {
                debuggerLookup = Lookups.fixed(nbProject);
            }

            log("Project document base: " + FileUtil.getFileDisplayName(projectDocBase));
            log("Server document base: " + getWebBaseUrl());
            log("Client URL: " + webUrl);

            URI clientUrl = new URI(webUrl);
            
            HtmlBrowser.Factory browser = null;
            if (WebClientToolsProjectUtils.isInternetExplorer(nbProject)) {
                browser = WebClientToolsProjectUtils.getInternetExplorerBrowser();
            } else {
                browser = WebClientToolsProjectUtils.getFirefoxBrowser();
            }
            
            if (browser == null) {
                throw new BuildException("The configured debugging browser could not be found"); // NOI18N
            }
            
            WebClientToolsSessionStarterService.startSession(clientUrl, browser, debuggerLookup);
        }catch (Exception ex) {
            throw new BuildException(ex);
        }
    }

    private String getWebBaseUrl() {
        // strip the client.urlPart (from the debug-single action) to get the document
        // base on the server side
        String clientBaseUrl;
        if (urlPart != null && urlPart.length() > 0) {
            int partition = webUrl.lastIndexOf(urlPart);

            if (partition > 0) {
                clientBaseUrl = webUrl.substring(0, partition + 1);
            } else {
                clientBaseUrl = webUrl;
            }
        } else {
            clientBaseUrl = webUrl;
        }


       return clientBaseUrl;
    }


}
