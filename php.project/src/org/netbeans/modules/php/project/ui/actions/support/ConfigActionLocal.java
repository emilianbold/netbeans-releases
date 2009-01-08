/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.actions.support;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.Utils;
import org.netbeans.modules.php.project.spi.XDebugStarter;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.LocationMappersFactory;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionException;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Action implementation for LOCAL configuration.
 * It means running and debugging web pages on a local web server.
 * @author Tomas Mysik
 */
public class ConfigActionLocal extends ConfigAction {

    @Override
    public boolean isRunProjectEnabled(PhpProject project) {
        return isRunProjectEnabled();
    }

    @Override
    public boolean isDebugProjectEnabled(PhpProject project) {
        return isDebugProjectEnabled();
    }

    @Override
    public boolean isRunFileEnabled(PhpProject project, Lookup context) {
        FileObject webRoot = ProjectPropertiesSupport.getWebRootDirectory(project);
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, webRoot);
        return file != null;
    }

    @Override
    public boolean isDebugFileEnabled(PhpProject project, Lookup context) {
        if (XDebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(project, context);
    }

    @Override
    public void runProject(PhpProject project) {
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(CommandUtils.urlForProject(project));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void debugProject(final PhpProject project) {
        Runnable runnable = new Runnable() {
            public void run() {
                    try {
                        URL debugUrl = CommandUtils.urlForDebugProject(project);
                        assert debugUrl != null;
                        if (CommandUtils.getDebugInfo(project).debugClient) {
                            try {
                                launchJavaScriptDebugger(project, debugUrl);
                            } catch (URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        } else {
                            HtmlBrowser.URLDisplayer.getDefault().showURL(debugUrl);
                        }
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
        };

        boolean jsDebuggingAvailable = WebClientToolsSessionStarterService.isAvailable();
        if (jsDebuggingAvailable) {
            boolean keepDebugging = WebClientToolsProjectUtils.showDebugDialog(project);
            if (!keepDebugging) {
                return;
            }
        }

        if (!jsDebuggingAvailable || WebClientToolsProjectUtils.getServerDebugProperty(project)) {
            //temporary; after narrowing deps. will be changed
            XDebugStarter dbgStarter = XDebugStarterFactory.getInstance();
            if (dbgStarter != null) {
                if (dbgStarter.isAlreadyRunning()) {
                    if (CommandUtils.warnNoMoreDebugSession()) {
                        dbgStarter.stop();
                        debugProject(project);
                    }
                } else {
                    FileObject webRoot = ProjectPropertiesSupport.getWebRootDirectory(project);
                    final FileObject fileForProject = CommandUtils.fileForProject(project, webRoot);
                    if (fileForProject != null) {
                        startDebugger(project, dbgStarter, runnable, fileForProject);
                    } else {
                        String idxFileName = ProjectPropertiesSupport.getIndexFile(project);
                        String err = NbBundle.getMessage(ConfigActionLocal.class, "ERR_Missing_IndexFile", idxFileName);
                        final Message messageDecriptor = new NotifyDescriptor.Message(err, NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(messageDecriptor);
                        project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer(CompositePanelProviderImpl.RUN);
                    }
                }
            }
        } else {
            runnable.run();
        }
    }

    @Override
    public void runFile(PhpProject project, Lookup context) {
        try {
            // need to fetch these vars _before_ focus changes (can happen in eventuallyUploadFiles() method)
            final URL url = CommandUtils.urlForContext(project, context);
            assert url != null;

            // XXX for REMOTE
            //eventuallyUploadFiles(CommandUtils.filesForSelectedNodes());
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            //TODO: improve error handling
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void debugFile(final PhpProject project, Lookup context) {
        // need to fetch these vars _before_ focus changes (can happen in eventuallyUploadFiles() method)
        FileObject webRoot = ProjectPropertiesSupport.getWebRootDirectory(project);
        final FileObject selectedFile = CommandUtils.fileForContextOrSelectedNodes(context, webRoot);
        URL url = null;
        try {
            url = CommandUtils.urlForDebugContext(project, context);
        } catch (MalformedURLException ex) {
            //TODO improve error handling
            Exceptions.printStackTrace(ex);
            return;
        }
        assert url != null;
        final URL debugUrl = url;

        // XXX for REMOTE
        //eventuallyUploadFiles(CommandUtils.filesForSelectedNodes());
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    if (CommandUtils.getDebugInfo(project).debugClient) {
                        try {
                            launchJavaScriptDebugger(project, debugUrl);
                        } catch (URISyntaxException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else {
                        HtmlBrowser.URLDisplayer.getDefault().showURL(debugUrl);
                    }
                } catch (MalformedURLException ex) {
                    //TODO improve error handling
                    Exceptions.printStackTrace(ex);
                }
            }
        };

        boolean jsDebuggingAvailable = WebClientToolsSessionStarterService.isAvailable();
        if (jsDebuggingAvailable) {
            boolean keepDebugging = WebClientToolsProjectUtils.showDebugDialog(project);
            if (!keepDebugging) {
                return;
            }
        }

        if (!jsDebuggingAvailable || WebClientToolsProjectUtils.getServerDebugProperty(project)) {
            XDebugStarter dbgStarter = XDebugStarterFactory.getInstance();
            if (dbgStarter != null) {
                if (dbgStarter.isAlreadyRunning()) {
                    if (CommandUtils.warnNoMoreDebugSession()) {
                        dbgStarter.stop();
                        debugFile(project, context);
                    }
                } else {
                    startDebugger(project, dbgStarter, runnable, selectedFile);
                }
            }
        } else {
            runnable.run();
        }
    }

    private void startDebugger(final PhpProject project, final XDebugStarter dbgStarter, final Runnable initDebuggingCode,
            final FileObject debuggedFile) {
        Cancellable cancellable = new Cancellable() {
            public boolean cancel() {
                return true;
            }
        };
        Callable<Cancellable> initDebuggingCallable = Executors.callable(initDebuggingCode, cancellable);
        dbgStarter.start(project, initDebuggingCallable, debuggedFile, false);
    }

    private void launchJavaScriptDebugger(PhpProject project, URL url) throws MalformedURLException, URISyntaxException {
        LocationMappersFactory mapperFactory = Lookup.getDefault().lookup(LocationMappersFactory.class);
        Lookup debuggerLookup = null;
        if (mapperFactory != null) {
            URI appContext = CommandUtils.getBaseURL(project).toURI();
            FileObject[] srcRoots = Utils.getSourceObjects(project);

            JSToNbJSLocationMapper forwardMapper =
                    mapperFactory.getJSToNbJSLocationMapper(srcRoots, appContext, null);
            NbJSToJSLocationMapper reverseMapper =
                    mapperFactory.getNbJSToJSLocationMapper(srcRoots, appContext, null);
            debuggerLookup = Lookups.fixed(forwardMapper, reverseMapper, project);
        } else {
            debuggerLookup = Lookups.fixed(project);
        }

        URI clientUrl = url.toURI();

        HtmlBrowser.Factory browser = null;
        if (WebClientToolsProjectUtils.isInternetExplorer(project)) {
            browser = WebClientToolsProjectUtils.getInternetExplorerBrowser();
        } else {
            browser = WebClientToolsProjectUtils.getFirefoxBrowser();
        }

        if (browser == null) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } else {
            try {
                WebClientToolsSessionStarterService.startSession(clientUrl, browser, debuggerLookup);
            } catch (WebClientToolsSessionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
