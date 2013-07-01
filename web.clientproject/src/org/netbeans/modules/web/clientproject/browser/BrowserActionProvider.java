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
package org.netbeans.modules.web.clientproject.browser;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.javascript.jstestdriver.api.RunTests;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowserFeatures;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.api.WebServer;
import org.netbeans.modules.web.clientproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

public class BrowserActionProvider implements ActionProvider {

    final private ClientSideProject project;
    private final BrowserSupport support;
    private ClientProjectEnhancedBrowserImpl cfg;
    private RequestProcessor RP = new RequestProcessor("js unit testing"); //NOI18N
    private static final Logger LOGGER = Logger.getLogger(BrowserActionProvider.class.getName());

    public BrowserActionProvider(ClientSideProject project, BrowserSupport support, ClientProjectEnhancedBrowserImpl cfg) {
        this.project = project;
        this.support = support;
        this.cfg = cfg;
    }
    
    @Override
    public String[] getSupportedActions() {
        return new String[] {COMMAND_RUN};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (project.isUsingEmbeddedServer()) {
            WebServer.getWebserver().start(project, project.getSiteRootFolder(), project.getWebContextRoot());
        } else {
            WebServer.getWebserver().stop(project);
        }
        String startFile = project.getStartFile();
        String splt[] = ClientSideProjectUtilities.splitPathAndFragment(startFile);
        String justStartFile = splt[0];
        String fragment = splt[1];
        if (COMMAND_RUN.equals(command)) {
            FileObject siteRoot = project.getSiteRootFolder();
            if (siteRoot == null) {
                ProjectProblems.showAlert(project);
                return;
            }
            FileObject fo = siteRoot.getFileObject(justStartFile);
            if (fo == null) {
                DialogDisplayer.getDefault().notify(
                    new DialogDescriptor.Message(
                        org.openide.util.NbBundle.getMessage(BrowserActionProvider.class, "MAIN_FILE", startFile)));
                CustomizerProviderImpl cust = project.getLookup().lookup(CustomizerProviderImpl.class);
                cust.showCustomizer(CompositePanelProviderImpl.RUN);
                // try again:
                splt = ClientSideProjectUtilities.splitPathAndFragment(project.getStartFile());
                justStartFile = splt[0];
                fragment = splt[1];
                fo = siteRoot.getFileObject(justStartFile);
                if (fo == null) {
                    return;
                }
            }
            browseFile(support, fo, fragment);
        } else if (COMMAND_RUN_SINGLE.equals(command)) {
            FileObject fo = getFile(context);
            if (fo != null) {
                browseFile(support, fo);
            }
        } else if (COMMAND_TEST.equals(command)) {
            runTests(null);
        }
    }
    
    private void runTests(final String testName) {
        if (!(project.getConfigFolder() != null && 
                    project.getConfigFolder().getFileObject("jsTestDriver.conf") != null && //NOI18N
                    project.getTestsFolder() != null)) {
            return;
        }

        final FileObject configFile = project.getConfigFolder().getFileObject("jsTestDriver.conf"); //NOI18N
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (testName == null) {
                        RunTests.runAllTests(project, project.getProjectDirectory(), configFile);
                    } else {
                        // not implemented yet as I do not know how:
                        //RunTests.runTests(project, project.getProjectDirectory(), configFile, testName);
                    }
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, "cannot execute tests", t); //NOI18N
                }
            }
        });
    }

    
    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_TEST.equals(command)) {
            return (project.getConfigFolder() != null && 
                    project.getConfigFolder().getFileObject("jsTestDriver.conf") != null && //NOI18N
                    project.getTestsFolder() != null);
        }
        // not sure how to force js-test-driver to run single test; I tried everything according
        // to their documentation and it always runs all tests
//        if (COMMAND_TEST_SINGLE.equals(command)) {
//            FileObject fo = getFile(context);
//            return (fo != null && "js".equals(fo.getExt()) && project.getConfigFolder() != null && 
//                    project.getConfigFolder().getFileObject("jsTestDriver.conf") != null &&
//                    project.getTestsFolder() != null &&
//                    FileUtil.isParentOf(project.getTestsFolder(), fo));
//        }
//        Project prj = context.lookup(Project.class);
//        ClientSideConfigurationProvider provider = prj.getLookup().lookup(ClientSideConfigurationProvider.class);
//        if (provider.getActiveConfiguration().getBrowser() != null) {
//            return true;
//        }
//        return false;
            return true;
        }
    
    private FileObject getFile(Lookup context) {
        return context.lookup(FileObject.class);
    }

    private void browseFile(BrowserSupport bs, FileObject fo) {
        browseFile(bs, fo, "");
    }
    
    private void browseFile(BrowserSupport bs, FileObject fo, String fragment) {
        URL url;
        if (FileUtil.isParentOf(project.getSiteRootFolder(), fo)) {
            url = ServerURLMapping.toServer(project, fo);
            if (fragment.length() > 0) {
                url = WebUtils.stringToUrl(WebUtils.urlToString(url)+fragment);
            }
            bs.load(url, fo);
        } else {
            url = fo.toURL();
            WebBrowser wb = project.getProjectWebBrowser();
            WebBrowserFeatures features = new WebBrowserFeatures(false, false, false, false, false, false);
            wb.createNewBrowserPane(features).showURL(url);
        }
    }
}
