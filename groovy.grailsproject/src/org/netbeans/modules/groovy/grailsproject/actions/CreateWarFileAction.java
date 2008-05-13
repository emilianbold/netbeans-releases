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
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsRuntime;
import org.netbeans.modules.groovy.grailsproject.execution.DefaultDescriptor;
import org.netbeans.modules.groovy.grailsproject.execution.ExecutionService;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class CreateWarFileAction extends AbstractAction implements LineSnooper {

    private static final Logger LOG = Logger.getLogger(CreateWarFileAction.class.getName());

    private final Project prj;

    private final GrailsProjectConfig prjConfig;

    private final boolean autodeploy;

    public CreateWarFileAction(Project prj) {
        super("Create war file");
        this.prj = prj;
        prjConfig = GrailsProjectConfig.forProject(prj);
        autodeploy = prjConfig.getAutoDeployFlag();

    }

    public boolean isEnabled() {
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        final GrailsRuntime runtime = GrailsRuntime.getInstance();
        if (!runtime.isConfigured()) {
            ConfigSupport.showConfigurationWarning(runtime);
            return;
        }

        String command = "war"; // NOI18N
        ProjectInformation inf = prj.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName() + " (" + command + ")"; // NOI18N

        Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand(
                command, GrailsProjectConfig.forProject(prj)); // NOI18N
        ExecutionService service = new ExecutionService(callable, displayName,
                new DefaultDescriptor(prj, autodeploy ? this : null, null, false));

        service.run();
    }

    public void lineFilter(String line) throws IOException {
        if (line.contains("Done creating WAR") || line.contains("Created WAR")) { // NOI18N
            LOG.log(Level.FINEST, "War file created, copy");
            FileObject prjDir = prj.getProjectDirectory();

            LOG.log(Level.FINEST, "Project Directory: " + prjDir.getPath());

            for (Enumeration e = prjDir.getChildren(false); e.hasMoreElements();) {
                FileObject fo = (FileObject) e.nextElement();
                if (fo != null) {
                    if (fo.getExt().toUpperCase().startsWith("WAR")) {
                        LOG.log(Level.FINEST, "Extention is OK: " + fo.getExt());
                        String deployDir = prjConfig.getDeployDir();

                        LOG.log(Level.FINEST, "Target dir from config: " + deployDir);

                        if (deployDir != null && deployDir.length() > 0) {

                            File targetFile = new File(deployDir);
                            FileObject target = FileUtil.toFileObject(targetFile);
                            LOG.log(Level.FINEST, "Copy file (source)     :" + fo.getPath());
                            LOG.log(Level.FINEST, "Copy file (destination):" + target.getPath());
                            LOG.log(Level.FINEST, "Copy file (name)       :" + fo.getName());
                            FileUtil.copyFile(fo, target, fo.getName());
                        }
                    }
                }
            }
        }
    }
}
