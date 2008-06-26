/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.ruby.rubyproject.rake;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.rubyproject.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.execution.ExecutionService;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.FileSystemAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.RenameAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.InstalledFileLocator;
import org.openide.nodes.FilterNode;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;

/**
 * Supports Rake related operations.
 */
public final class RakeSupport {

    private static final Logger LOGGER = Logger.getLogger(RakeSupport.class.getName());
    /** File storing the 'rake -D' output. */
    static final String RAKE_D_OUTPUT = "nbproject/private/rake-d.txt"; // NOI18N
    /** Standard names used for Rakefile. */
    static final String[] RAKEFILE_NAMES = new String[] {
        "rakefile", "Rakefile", "rakefile.rb", "Rakefile.rb" // NOI18N
    };

    private final Project project;

    public RakeSupport(Project project) {
        this.project = project;
    }

    /**
     * Tries to find Rakefile for a given project.
     * 
     * @param project project to be searched
     * @return found Rakefile or <code>null</code> if not found
     */
    public static FileObject findRakeFile(final Project project) {
        FileObject pwd = project.getProjectDirectory();

        // See if we're in the right directory
        for (String s : RakeSupport.RAKEFILE_NAMES) {
            FileObject f = pwd.getFileObject(s);
            if (f != null) {
                return f;
            }
        }

        // Try to adjust the directory to a folder which contains a rakefile
        Sources src = ProjectUtils.getSources(project);
        //TODO: needs to be generified
        SourceGroup[] rubygroups = src.getSourceGroups(RubyProject.SOURCES_TYPE_RUBY);
        if (rubygroups != null && rubygroups.length > 0) {
            for (SourceGroup group : rubygroups) {
                FileObject f = group.getRootFolder();
                for (String s : RakeSupport.RAKEFILE_NAMES) {
                    FileObject r = f.getFileObject(s);
                    if (r != null) {
                        return r;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isRakeFile(FileObject fo) {
        if (!fo.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
            return false;
        }

        String name = fo.getName().toLowerCase();

        if (name.equals("rakefile")) { // NOI18N

            return true;
        }

        String ext = fo.getExt().toLowerCase();

        if (ext.equals("rake")) { // NOI18N

            return true;
        }

        return false;
    }

    /**
     * Runs 'rake -D' and writes the output into the {@link #RAKE_D_OUTPUT} if
     * succeed.
     * 
     * @param project project for which tasks are read
     */
    public static void refreshTasks(final Project project) {
        try {
            FileObject rakeD = project.getProjectDirectory().getFileObject(RAKE_D_OUTPUT);
            // clean old content
            if (rakeD != null && rakeD.isData()) {
                rakeD.delete();
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        if (!RubyPlatform.hasValidRake(project, true)) {
            return;
        }

        String rakeOutput = readRakeTasksOutput(project);
        if (rakeOutput != null) {
            writeRakeTasks(project, rakeOutput);
        }
    }

    /**
     * Runs 'rake -D' and returns the output.
     *
     * @param project project for which tasks are read
     * @return rake output; might be <tt>null</tt> when underlaying rake command fails.
     */
    private static String readRakeTasksOutput(final Project project) {
        File pwd;
        FileObject rakeFile = RakeSupport.findRakeFile(project);
        if (rakeFile == null) {
            pwd = FileUtil.toFile(project.getProjectDirectory());
        } else {
            pwd = FileUtil.toFile(rakeFile.getParent());
        }

        // Install the given gem
        RubyPlatform platform = RubyPlatform.platformFor(project);

        return dumpRakeTasksInfo(platform, pwd);
        // TODO: we are not able to parse complex Rakefile (e.g. rails'), using -P argument, yet
        // sb.append(hiddenRakeRunner(cmd, rakeCmd, pwd, "-P"));
    }

    /**
     * Returns namespace-task tree for the given project.
     */
    public static Set<RakeTask> getRakeTaskTree(final Project project) {
        RakeTaskReader rtreader = new RakeTaskReader(project);
        return rtreader.getRakeTaskTree();
    }

    /**
     * Returns flat, namespace-ignoring, list of Rake tasks for the given
     * project.
     */
    static Set<RakeTask> getRakeTasks(final Project project) {
        Set<RakeTask> taskTree = RakeSupport.getRakeTaskTree(project);
        Set<RakeTask> tasks = new TreeSet<RakeTask>();
        addTasks(tasks, taskTree);
        return tasks;
    }

    private static void addTasks(final Set<RakeTask> flatAccumulator, final Set<RakeTask> taskTree) {
        for (RakeTask task : taskTree) {
            if (task.isNameSpace()) {
                addTasks(flatAccumulator, task.getChildren());
            } else {
                flatAccumulator.add(task);
            }
        }
    }

    /**
     * Returns {@link RakeTask} for the give <tt>task</tt>.
     *
     * @param project Ruby project
     * @param task task to be find
     * @return <tt>null</tt> if not found; {@link RakeTask} instance othewise
     */
    public static RakeTask getRakeTask(final Project project, final String task) {
        Set<RakeTask> tasks = getRakeTasks(project);
        for (RakeTask rakeTask : tasks) {
            if (rakeTask.getTask().equals(task)) {
                return rakeTask;
            }
        }
        return null;
    }

    private static String dumpRakeTasksInfo(RubyPlatform platform, File pwd) {
        List<String> argList = new ArrayList<String>();
        File cmd = platform.getInterpreterFile();
        if (!cmd.getName().startsWith("jruby") || RubyExecution.LAUNCH_JRUBY_SCRIPT) { // NOI18N
            argList.add(cmd.getPath());
        }

        argList.addAll(RubyExecution.getRubyArgs(platform));

        File platformInfoScript = InstalledFileLocator.getDefault().locate(
                "rake_tasks_info.rb", "org.netbeans.modules.ruby.project", false);  // NOI18N
        if (platformInfoScript == null) {
            throw new IllegalStateException("Cannot locate rake_tasks_info.rb script"); // NOI18N
        }

        argList.add(platformInfoScript.getAbsolutePath());

        String[] args = argList.toArray(new String[argList.size()]);
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(pwd);
        pb.redirectErrorStream(false);

        // PATH additions for JRuby etc.
        RubyExecution.setupProcessEnvironment(pb.environment(), cmd.getParent(), false);
        GemManager.adjustEnvironment(platform, pb.environment());

        int exitCode = -1;

        String stdout = null;
        try {
            ExecutionService.logProcess(pb);
            Process process = pb.start();

            stdout = readInputStream(process.getInputStream());
            String stderr = readInputStream(process.getErrorStream());

            exitCode = process.waitFor();

            if (exitCode != 0) {
                LOGGER.severe("rake process failed (stdout):\n\n" + stdout); // NOI18N
                LOGGER.severe("rake process failed (stderr):\n\n" + stderr); // NOI18N
                Util.notifyLocalized(RakeSupport.class, "RakeSupport.rake.task.fetching.fails", // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE, stderr);
                return null;
            }
            if (stderr.length() > 0) {
                LOGGER.warning("rake process warnings:\n\n" + stderr); // NOI18N
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (InterruptedException ie) {
            Exceptions.printStackTrace(ie);
        }

        return stdout;
    }

    private static String readInputStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;

        try {
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
                sb.append('\n');
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return sb.toString();
    }

    static void writeRakeTasks(Project project, final String rakeDOutput) {
        final FileObject projectDir = project.getProjectDirectory();

        try {
            projectDir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {

                public void run() throws IOException {
                    FileObject rakeTasksFile = projectDir.getFileObject(RAKE_D_OUTPUT);

                    if (rakeTasksFile != null) {
                        rakeTasksFile.delete();
                    }

                    rakeTasksFile = FileUtil.createData(projectDir, RAKE_D_OUTPUT);

                    OutputStream os = rakeTasksFile.getOutputStream();
                    Writer writer = new BufferedWriter(new OutputStreamWriter(os));
                    writer.write(rakeDOutput);
                    writer.close();
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public static final class RakeNode extends FilterNode {

        private Action[] actions;

        public RakeNode(final FileObject rakeFO) throws DataObjectNotFoundException {
            super(DataObject.find(rakeFO).getNodeDelegate());
        }

        public @Override Action[] getActions(boolean context) {
            if (actions == null) {
                actions = new Action[] {
                    SystemAction.get(OpenAction.class),
                    null,
                    SystemAction.get(CutAction.class),
                    SystemAction.get(CopyAction.class),
                    SystemAction.get(PasteAction.class),
                    null,
                    SystemAction.get(DeleteAction.class),
                    SystemAction.get(RenameAction.class),
                    null,
                    SystemAction.get(FileSystemAction.class),
                    null,
                    SystemAction.get(ToolsAction.class),
                    SystemAction.get(PropertiesAction.class),
                };
            }
            return actions;
        }
    }
}
