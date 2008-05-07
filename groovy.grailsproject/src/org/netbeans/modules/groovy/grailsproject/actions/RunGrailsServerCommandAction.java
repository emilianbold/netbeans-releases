/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.grailsproject.actions;

import org.netbeans.modules.groovy.grailsproject.execution.LineSnooper;
import java.net.MalformedURLException;
import org.netbeans.modules.groovy.grailsproject.GrailsServerState;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.io.IOException;
import org.netbeans.api.project.Project;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.util.concurrent.Callable;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.execution.ExecutionService;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;

public class RunGrailsServerCommandAction extends AbstractAction /*implements OutputListener, LineSnooper*/ {

    private final Project project;
    private static final Logger LOG = Logger.getLogger(RunGrailsServerCommandAction.class.getName());

    public RunGrailsServerCommandAction(Project prj) {
        super("Run Application");
        this.project = prj;
    }

    @Override
    public boolean isEnabled() {
        GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
        return !serverState.isRunning();
    }

    public void actionPerformed(ActionEvent e) {
        final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
        if (serverState != null && serverState.isRunning()) {
            return;
        }
        Callable<Process> callable = new Callable<Process>() {

            public Process call() throws Exception {
                Callable<Process> inner = ExecutionSupport.getInstance().createRunApp(GrailsProjectConfig.forProject(project));
                Process process = inner.call();
                final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
                if (serverState != null) {
                    serverState.setProcess(process);
                }
                return process;
            }
        };

        ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName();
        ExecutionService service = new ExecutionService(callable, displayName,
                new ExecutionService.Descriptor() {

            public FileObject getFileObject() {
                return project.getProjectDirectory();
            }

            public LineSnooper getOutputSnooper() {
                return new HttpSnooper();
            }

            public boolean isFrontWindow() {
                return true;
            }

            public boolean isInputVisible() {
                return true;
            }

            public boolean showSuspended() {
                return true;
            }

            public boolean showProgress() {
                return true;
            }

            public Runnable getPostExecution() {
                return new Runnable() {

                    public void run() {
                        final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
                        serverState.setProcess(null);
                    }
                };
            }
        });
        service.run();

    }

    private static class HttpSnooper implements LineSnooper {

        private String urlLine;

        public synchronized void lineFilter(String line) throws IOException {
            if (line.contains("Browse to http:/")) {
                urlLine = line;
                startBrowserWithUrl();
            }
        }

        public synchronized void reset() {
            urlLine = null;
        }

        public synchronized  void startBrowserWithUrl() {
            if (urlLine == null) {
                return;
            }

            String urlString = urlLine.substring(urlLine.indexOf("http://"));

            try {
                HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(urlString));
            } catch (MalformedURLException ex) {
                LOG.log(Level.WARNING, "Could not start browser " + ex.getMessage());

            }
        }

    }
}
