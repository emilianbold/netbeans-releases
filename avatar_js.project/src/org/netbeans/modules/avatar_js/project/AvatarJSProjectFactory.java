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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
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
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;
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
        if (
            projectDirectory.getFileObject("nbproject") != null ||
            projectDirectory.getParent().getFileObject("nbproject") != null
        ) {
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
        if (isProject(projectDirectory)) {
            return new PackageJSONPrj(projectDirectory);
        }
        return null;
    }

    @Override
    public void saveProject(Project project) throws IOException, ClassCastException {
        if (project instanceof PackageJSONPrj) {
            ((PackageJSONPrj)project).save();
        }
    }
    
    private static final class PackageJSONPrj implements Project, 
    ActionProvider, FileChangeListener, LogicalViewProvider {
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
                ActionProvider.COMMAND_RUN,
                ActionProvider.COMMAND_DEBUG
            };
        }

        @Override
        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            if (ActionProvider.COMMAND_RUN.equals(command)) {
                Object main = getPackage().get("main");
                if (main instanceof String) {
                    FileObject toRun = dir.getFileObject((String)main);
                    if (toRun != null) {
                        if (canExecute("nodejs")) {
                            ExecItem.executeJS(dir, toRun, "nodejs", command);
                        } else {
                            ExecItem.executeJS(dir, toRun, command, false);
                        }
                        return;
                    }
                }
            }
            if (ActionProvider.COMMAND_DEBUG.equals(command)) {
                Object main = getPackage().get("main");
                if (main instanceof String) {
                    FileObject toRun = dir.getFileObject((String)main);
                    if (toRun != null) {
                        // TODO: debug in avatarjs
                        //if (canExecute("nodejs")) {
                        //    ExecItem.executeJS(dir, toRun, "nodejs", command);
                        //} else {
                            ExecItem.executeJS(dir, toRun, command, true);
                        //}
                        return;
                    }
                }
            }
            throw new IllegalArgumentException(command);
        }

        private static boolean canExecute(String nodecmd) {
            try {
                int exitValue = Runtime.getRuntime().exec(new String[] { nodecmd, "-v" }).exitValue();
                return exitValue == 0;
            } catch (IOException ex) {
                return false;
            }
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

        @Override
        public Node createLogicalView() {
            DataFolder df = DataFolder.findFolder(dir);
            AbstractNode an = new AbstractNode(
                df.createNodeChildren(DataFilter.ALL), getLookup()
            ) {

                @Override
                public Action[] getActions(boolean context) {
                    return Utilities.actionsForPath("Projects/org-netbeans-modules-avatar_js-project/Actions/").toArray(new Action[0]);
                }
            };
            an.setIconBaseWithExtension(ICON);
            an.setName(df.getName());
            return an;
        }

        @Override
        public Node findPath(Node root, Object target) {
            List<String> arr = new LinkedList<>();
            while (target != dir) {
                if (! (target instanceof FileObject)) {
                    return root;
                }
                final FileObject fo = (FileObject)target;
                arr.add(0, fo.getName());
                target = fo.getParent();
            }
            try {
                return NodeOp.findPath(root, arr.toArray(new String[0]));
            } catch (NodeNotFoundException ex) {
                return ex.getClosestNode();
            }
        }
    }
    
    private static final class ExecItem 
    implements BuildExecutionSupport.ActionItem, Runnable {
        private final String action;
        private final String name;
        private final FileObject dir;
        private final ProcessBuilder pb;
        private final AvatarJSExecutor nex;
        private Future<Integer> running;

        ExecItem(
            String action, String name,
            FileObject dir, ProcessBuilder pb
        ) {
            this.action = action;
            this.name = name;
            this.dir = dir;
            this.pb = pb;
            this.nex = null;
        }
        
        ExecItem(
            String action, String name,
            FileObject dir, AvatarJSExecutor nex
        ) {
            this.action = action;
            this.name = name;
            this.dir = dir;
            this.nex = nex;
            this.pb = null;
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
        
        static void executeJS(FileObject dir, FileObject toRun, String command, boolean debug) {
            AvatarJSExecutor nex = new AvatarJSExecutor(toRun, debug);
            final ExecItem ei = new ExecItem(command, toRun.getNameExt(), dir, nex);
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
            if (pb != null) {
                repeatExtExecution();
            } else {
                repeatNashornExecution();
            }
        }
        
        private void repeatExtExecution() {
            ExecutionDescriptor ed = new ExecutionDescriptor()
                    .frontWindow(true).inputVisible(true).postExecution(this);
            final ExecutionService serv = ExecutionService.newService(pb, ed, getDisplayName());
            BuildExecutionSupport.registerRunningItem(this);
            running = serv.run();
        }
        
        private void repeatNashornExecution() {
            ExecutionDescriptor ed = new ExecutionDescriptor()
                    .frontWindow(true).inputVisible(true).postExecution(this);
            BuildExecutionSupport.registerRunningItem(this);
            try {
                running = nex.run(NashornPlatform.getDefault());
            } catch (IOException | UnsupportedOperationException ex) {
                running = new Future<Integer>() {
                    @Override public boolean cancel(boolean mayInterruptIfRunning) { return false; }
                    @Override public boolean isCancelled() { return false; }
                    @Override public boolean isDone() { return true; }
                    @Override public Integer get() { return -1; }
                    @Override public Integer get(long timeout, TimeUnit unit) { return -1; }
                };
            }
        }
        
        @Override
        public void run() {
            if (running.isDone()) {
                BuildExecutionSupport.registerFinishedItem(this);
            }
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
