/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = ProjectFactory.class)
public final class AvatarJSProjectFactory implements ProjectFactory2 {
    private static final Logger LOG = Logger.getLogger(AvatarJSProjectFactory.class.getName());
    @StaticResource
    private static final String ICON = "org/netbeans/modules/avatar_js/project/resources/nodejs.png";
    
    
    @Override
    public ProjectManager.Result isProject2(FileObject projectDirectory) {
        FileObject pkgJson = projectDirectory.getFileObject("package.json");
        if (pkgJson == null) {
            return null;
        }
        ImageIcon img = ImageUtilities.loadImageIcon(ICON, false);
        return new ProjectManager.Result(img);
    }

    @Override
    public boolean isProject(FileObject projectDirectory) {
        return isProject2(projectDirectory) != null;
    }

    @Override
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        return new PackageJSONPrj(projectDirectory);
    }

    @Override
    public void saveProject(Project project) throws IOException, ClassCastException {
        if (project instanceof PackageJSONPrj) {
            ((PackageJSONPrj)project).save();
        }
    }
    
    private static final class PackageJSONPrj implements Project, 
    ActionProvider, FileChangeListener {
        private final FileObject dir;
        private final Lookup lkp;
        private JSONObject pckg;

        public PackageJSONPrj(FileObject dir) {
            this.dir = dir;
            this.lkp = Lookups.singleton(this);
            dir.addFileChangeListener(FileUtil.weakFileChangeListener(this, dir));
        }
        
        @Override
        public FileObject getProjectDirectory() {
            return dir;
        }

        @Override
        public Lookup getLookup() {
            return lkp;
        }
        
        public void save() throws IOException {
        }
        
        @Override
        public String[] getSupportedActions() {
            return new String[] {
                ActionProvider.COMMAND_RUN
            };
        }

        @Override
        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            if (ActionProvider.COMMAND_RUN.equals(command)) {
                Object main = getPackage().get("main");
                if (main instanceof String) {
                    FileObject toRun = dir.getFileObject((String)main);
                    if (toRun != null) {
                        ExecItem.executeJS(dir, toRun, "nodejs", command);
                        return;
                    }
                }
            }
            throw new IllegalArgumentException(command);
        }

        @Override
        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            if (
                ActionProvider.COMMAND_RUN.equals(command) ||
                ActionProvider.COMMAND_DEBUG.equals(command)
            ) {
                return getPackage().get("main") != null;
            }
            return false;
        }
        
        private JSONObject getPackage() {
            if (pckg != null) {
                return pckg;
            }
            FileObject fo = dir.getFileObject("package.json");
            if (fo != null) {
                try {
                    pckg = (JSONObject) new JSONParser().parse(fo.asText("UTF-8"));
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Error parsing " + fo, ex);
                }
            }
            if (pckg == null) {
                pckg = new JSONObject();
            }
            return pckg;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            reset();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            reset();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            reset();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            reset();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reset();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            reset();
        }
        
        private void reset() {
            pckg = null;
        }
    }
    
    private static final class ExecItem 
    implements BuildExecutionSupport.ActionItem, Runnable {
        private final String action;
        private final String name;
        private final FileObject dir;
        private final ProcessBuilder pb;
        private Future<Integer> running;

        ExecItem(
            String action, String name,
            FileObject dir, ProcessBuilder pb
        ) {
            this.action = action;
            this.name = name;
            this.dir = dir;
            this.pb = pb;
        }

        static void executeJS(FileObject dir, FileObject toRun, final String prg, String command) {
            File drf = FileUtil.toFile(dir);
            File trf = FileUtil.toFile(toRun);
            ProcessBuilder pb = ProcessBuilder.getLocal();
            pb.setExecutable(prg);
            pb.setArguments(Arrays.asList(trf.getAbsolutePath()));
            pb.setWorkingDirectory(drf.getAbsolutePath());
            final ExecItem ei = new ExecItem(command, toRun.getNameExt(), dir, pb);
            ei.repeatExecution();
        }
        
        @Override
        public String getAction() {
            return action;
        }

        @Override
        public FileObject getProjectDirectory() {
            return dir;
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        @Override
        public void repeatExecution() {
            if (isRunning()) {
                return;
            }
            ExecutionDescriptor ed = new ExecutionDescriptor()
                    .frontWindow(true).inputVisible(true).preExecution(this);
            final ExecutionService serv = ExecutionService.newService(pb, ed, getDisplayName());
            BuildExecutionSupport.registerRunningItem(this);
            running = serv.run();
        }
        
        @Override
        public void run() {
            System.err.println("running");
        }

        @Override
        public boolean isRunning() {
            return running != null && !running.isDone();
        }

        @Override
        public void stopRunning() {
            running.cancel(true);
            try {
                running.get();
            } catch (Exception ex) {
                LOG.log(Level.INFO, "Can''t wait for " + getDisplayName(), ex);
            }
            BuildExecutionSupport.registerFinishedItem(this);
        }
    }
}
