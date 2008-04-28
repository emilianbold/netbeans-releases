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
package org.netbeans.modules.ruby.rubyproject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.FileLocator;
import org.openide.ErrorManager;
import org.openide.LifecycleManager;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Run Rake targets defined in the Rails project.
 * Based on the RunTargetsAction for Ant.
 *
 * @todo Refresh the filesystem? The rails "freeze" target nukes and recreates the vendor
 *   director - so it's missing until I do a refresh
 * @todo Do the project target defaults - different for rails and plain
 * @todo Find the rake file
 * @todo localize the target to be used
 * @todo move autotest support over
 * @todo Check navigator
 * @todo Handle "aborted!" target
 *
 * @author Tor Norbye
 */
public class RakeTargetsAction extends SystemAction implements ContextAwareAction {
    
    /**
     * Recent targets per Rakefile.
     * <br>
     * TODO: potential memory leak
     */
    private static Map<FileObject, List<RakeTarget>> recentTargets = new HashMap<FileObject, List<RakeTarget>>();

    /** Set during a refresh */
    private static volatile boolean refreshing;
    private static final String RAKE_T_OUTPUT = "nbproject/private/rake-t.txt"; // NOI18N
    private static final String REFRESH_TARGETS = "netbeans-refresh-targets"; // NOI18N
    private static final String RAKE_ABORTED = "rake aborted!"; // NOI18N
    
    protected boolean debug;

    @Override
    public String getName() {
        return NbBundle.getMessage(RakeTargetsAction.class, "LBL_run_targets_action");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false : "Action should never be called without a context";
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAction(actionContext, debug);
    }

    /**
     * Create the submenu.
     */
    private static JMenu createMenu(Project project, boolean debug) {
        return new LazyMenu(project, debug);
    }

    static List<RakeTarget> getRakeTargets(Project project) {
        try {
            String rakeOutput = readRakeTargets(project);

            if (rakeOutput == null) {
                rakeOutput = getDefaultRakeOutput();
            }

            return parseTargets(new StringReader(rakeOutput));
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);

            return Collections.emptyList();
        }
    }

    private static List<RakeTarget> parseTargets(Reader is)
        throws IOException {
        BufferedReader reader = new BufferedReader(is);
        List<RakeTarget> targets = new ArrayList<RakeTarget>(40);
        Map<String, RakeTarget> map = new HashMap<String, RakeTarget>(50);
        Set<String> processedTargets = new HashSet<String>();

        while (true) {
            String line = reader.readLine();

            if (line == null) {
                break;
            }

            if (line.startsWith(RAKE_ABORTED)) {
                continue;
            }

            if (!line.startsWith("rake ")) { // NOI18N

                continue;
            }

            int start = 5;
            int end = line.indexOf(' ', start);

            if (end == -1) {
                end = line.indexOf('#');

                if (end == -1) {
                    end = line.length();
                }
            }

            String target = line.substring(start, end);
            if (!processedTargets.add(target)) {
                continue;
            }
            String description = null;
            int descIndex = line.indexOf('#', end);

            if ((descIndex != -1) && (descIndex < (line.length() - 2))) {
                description = line.substring(descIndex + 2);
            }

            // Tokenize into categories (db:fixtures:load -> db | fixtures | load)
            RakeTarget parent = null;
            String[] path = target.split(":"); // NOI18N
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < (path.length - 1); i++) {
                if (sb.length() > 0) {
                    sb.append(':');
                }

                sb.append(path[i]);

                String folderPath = sb.toString();
                RakeTarget p = map.get(folderPath);

                if (p == null) {
                    RakeTarget r = new RakeTarget(path[i]);

                    if (parent == null) {
                        targets.add(r);
                    } else {
                        parent.addChild(r);
                    }

                    map.put(folderPath, r);
                    parent = r;
                } else {
                    parent = p;
                }
            }

            RakeTarget t = new RakeTarget(target, path[path.length - 1], description);

            if (parent != null) {
                parent.addChild(t);
            } else {
                targets.add(t);
            }
        }

        return targets;
    }

    private static String readRakeTargets(Project project) {
        final FileObject projectDir = project.getProjectDirectory();

        try {
            final StringBuilder sb = new StringBuilder(5000);
            projectDir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        FileObject rakeTargetFile = projectDir.getFileObject(RAKE_T_OUTPUT);

                        if (rakeTargetFile == null) {
                            return;
                        }

                        BufferedReader reader = null;
                        try {
                            InputStream is = rakeTargetFile.getInputStream();
                            reader = new BufferedReader(new InputStreamReader(is));

                            while (true) {
                                String line = reader.readLine();

                                if (line == null) {
                                    break;
                                }

                                sb.append(line);
                                sb.append('\n');
                            }
                        } finally {
                            if (reader != null) {
                                reader.close();
                            }
                        }
                    }
                });

            if (sb.length() > 0) {
                return sb.toString();
            } else {
                return null;
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);

            return null;
        }
    }

    static void writeRakeTargets(Project project, final String rakeTOutput) {
        final FileObject projectDir = project.getProjectDirectory();

        try {
            projectDir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        FileObject rakeTargetFile = projectDir.getFileObject(RAKE_T_OUTPUT);

                        if (rakeTargetFile != null) {
                            rakeTargetFile.delete();
                        }

                        rakeTargetFile = FileUtil.createData(projectDir, RAKE_T_OUTPUT);

                        OutputStream os = rakeTargetFile.getOutputStream();
                        Writer writer = new BufferedWriter(new OutputStreamWriter(os));
                        writer.write(rakeTOutput);
                        writer.close();
                    }
                });
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }

    static String getDefaultRakeOutput() {
        // TODO: I want to read this for the current project and regenerate it when the Rake files (or rails) is seen
        // to be updated.

        // Output from rake -T in a Rails 1.2.3 project:
        return "" + // NOI18N
            "rake db:fixtures:load          # Load fixtures into the current environment's database.  Load specific fixtures using FIXTURES=x,y\n" + // NOI18N
            "rake db:migrate                # Migrate the database through scripts in db/migrate. Target specific version with VERSION=x\n" + // NOI18N
            "rake db:schema:dump            # Create a db/schema.rb file that can be portably used against any DB supported by AR\n" + // NOI18N
            "rake db:schema:load            # Load a schema.rb file into the database\n" + // NOI18N
            "rake db:sessions:clear         # Clear the sessions table\n" + // NOI18N
            "rake db:sessions:create        # Creates a sessions table for use with CGI::Session::ActiveRecordStore\n" + // NOI18N
            "rake db:structure:dump         # Dump the database structure to a SQL file\n" + // NOI18N
            "rake db:test:clone             # Recreate the test database from the current environment's database schema\n" + // NOI18N
            "rake db:test:clone_structure   # Recreate the test databases from the development structure\n" + // NOI18N
            "rake db:test:prepare           # Prepare the test database and load the schema\n" + // NOI18N
            "rake db:test:purge             # Empty the test database\n" + // NOI18N
            "rake doc:app                   # Build the app HTML Files\n" + // NOI18N
            "rake doc:clobber_app           # Remove rdoc products\n" + // NOI18N
            "rake doc:clobber_plugins       # Remove plugin documentation\n" + // NOI18N
            "rake doc:clobber_rails         # Remove rdoc products\n" + // NOI18N
            "rake doc:plugins               # Generate documation for all installed plugins\n" + // NOI18N
            "rake doc:rails                 # Build the rails HTML Files\n" + // NOI18N
            "rake doc:reapp                 # Force a rebuild of the RDOC files\n" + // NOI18N
            "rake doc:rerails               # Force a rebuild of the RDOC files\n" + // NOI18N
            "rake log:clear                 # Truncates all *.log files in log/ to zero bytes\n" + // NOI18N
            "rake rails:freeze:edge         # Lock to latest Edge Rails or a specific revision with REVISION=X (ex: REVISION=4021) or a tag with TAG=Y (ex: TAG=rel_1-1-0)\n" + // NOI18N
            "rake rails:freeze:gems         # Lock this application to the current gems (by unpacking them into vendor/rails)\n" + // NOI18N
            "rake rails:unfreeze            # Unlock this application from freeze of gems or edge and return to a fluid use of system gems\n" + // NOI18N
            "rake rails:update              # Update both configs, scripts and public/javascripts from Rails\n" + // NOI18N
            "rake rails:update:configs      # Update config/boot.rb from your current rails install\n" + // NOI18N
            "rake rails:update:javascripts  # Update your javascripts from your current rails install\n" + // NOI18N
            "rake rails:update:scripts      # Add new scripts to the application script/ directory\n" + // NOI18N
            "rake stats                     # Report code statistics (KLOCs, etc) from the application\n" + // NOI18N
            "rake test                      # Test all units and functionals\n" + // NOI18N
            "rake test:functionals          # Run the functional tests in test/functional\n" + // NOI18N
            "rake test:integration          # Run the integration tests in test/integration\n" + // NOI18N
            "rake test:plugins              # Run the plugin tests in vendor/plugins/** /test (or specify with PLUGIN=name)\n" + // NOI18N
            "rake test:recent               # Test recent changes\n" + // NOI18N
            "rake test:uncommitted          # Test changes since last checkin (only Subversion)\n" + // NOI18N
            "rake test:units                # Run the unit tests in test/unit\n" + // NOI18N
            "rake tmp:cache:clear           # Clears all files and directories in tmp/cache\n" + // NOI18N
            "rake tmp:clear                 # Clear session, cache, and socket files from tmp/\n" + // NOI18N
            "rake tmp:create                # Creates tmp directories for sessions, cache, and sockets\n" + // NOI18N
            "rake tmp:pids:clear            # Clears all files in tmp/pids\n" + // NOI18N
            "rake tmp:sessions:clear        # Clears all files in tmp/sessions\n" + // NOI18N
            "rake tmp:sockets:clear         # Clears all files in tmp/sockets\n"; // NOI18N
    }

    public static void refreshTargets(Project project) {
        if (!RubyPlatform.hasValidRake(project, true)) {
            return;
        }

        refreshing = true;

        String rakeOutput = hiddenRakeRunner(project);
        writeRakeTargets(project, rakeOutput);
        refreshing = false;
    }

    private static String hiddenRakeRunner(Project project) {
        File pwd;
        FileObject rakeFile = RakeSupport.findRakeFile(project);
        if (rakeFile == null) {
            pwd = FileUtil.toFile(project.getProjectDirectory());
        } else {
            pwd = FileUtil.toFile(rakeFile.getParent());
        }

        // Install the given gem
        String rakeCmd = RubyPlatform.gemManagerFor(project).getRake();
        RubyPlatform platform = RubyPlatform.platformFor(project);

        StringBuffer sb = new StringBuffer();
        sb.append(hiddenRakeRunner(platform, rakeCmd, pwd, "-T"));
        // TODO: we are not able to parse complex Rakefile (e.g. rails'), using -P argument, yet
        // sb.append(hiddenRakeRunner(cmd, rakeCmd, pwd, "-P"));
        return sb.toString();
    }
    
    private static String hiddenRakeRunner(RubyPlatform platform, String rakeCmd, File pwd, String rakeArg) {

        List<String> argList = new ArrayList<String>();
        File cmd = platform.getInterpreterFile();
        if (!cmd.getName().startsWith("jruby") || RubyExecution.LAUNCH_JRUBY_SCRIPT) {
            argList.add(cmd.getPath());
        }

        argList.addAll(RubyExecution.getRubyArgs(platform));

        argList.add(rakeCmd);
        argList.add(rakeArg);

        String[] args = argList.toArray(new String[argList.size()]);
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(pwd);
        pb.redirectErrorStream(true);

        // PATH additions for JRuby etc.
        Map<String, String> env = pb.environment();
        new RubyExecution(new ExecutionDescriptor(platform, "rake", pwd).cmd(cmd)).setupProcessEnvironment(env);

        int exitCode = -1;

        StringBuilder sb = new StringBuilder(5000);

        try {
            Process process = pb.start();

            InputStream is = process.getInputStream();
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
                    sb.append("\n");
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }

            exitCode = process.waitFor();

            if (exitCode != 0) {
                try {
                    // This might not be necessary now that I'm
                    // calling ProcessBuilder.redirectErrorStream(true)
                    // but better safe than sorry
                    is = process.getErrorStream();
                    isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);

                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append('\n');
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        return sb.toString();
    }

    /**
     * The particular instance of this action for a given project.
     */
    private static final class ContextAction extends AbstractAction implements Presenter.Popup {
        
        private final Project project;
        private final boolean debug;

        public ContextAction(Lookup lkp, boolean debug) {
            super(SystemAction.get(debug ? RakeTargetsDebugAction.class : RakeTargetsAction.class).getName());
            this.debug = debug;

            Collection<?extends Project> apcs = lkp.lookupAll(Project.class);

            if (apcs.size() == 1) {
                project = apcs.iterator().next();
            } else {
                project = null;
            }

            super.setEnabled(project != null);
        }

        public void actionPerformed(ActionEvent e) {
            assert false : "Action should not be called directly";
        }

        public JMenuItem getPopupPresenter() {
            if (project != null) {
                return createMenu(project, debug);
            } else {
                return new Actions.MenuItem(this, false);
            }
        }

        @Override
        public void setEnabled(boolean b) {
            assert false : "No modifications to enablement status permitted";
        }
    }

    private static final class LazyMenu extends JMenu {
        
        private final Project project;
        private boolean initialized;
        private final boolean debug;

        public LazyMenu(Project project, boolean debug) {
            super(SystemAction.get(debug ? RakeTargetsDebugAction.class : RakeTargetsAction.class).getName());
            this.debug = debug;
            this.project = project;
        }

        @Override
        public JPopupMenu getPopupMenu() {
            if (refreshing) {
                super.removeAll();

                JMenuItem menuitem =
                    new JMenuItem(NbBundle.getMessage(RakeTargetsAction.class, "Refreshing"));
                add(menuitem);
                initialized = false;

                return super.getPopupMenu();
            }

            if (!initialized) {
                initialized = true;
                super.removeAll();

                List<RakeTarget> targets = getRakeTargets(project);
                boolean needsep = false;

                // List my recent targets 
                // Most recent order
                FileObject rakeFile = RakeSupport.findRakeFile(project);
                if (rakeFile != null) {
                    List<RakeTarget> recent = recentTargets.get(rakeFile);
                    if (recent != null) {
                        for (int i = recent.size() - 1; i >= 0; i--) {
                            RakeTarget target = recent.get(i);
                            assert target.isTarget();

                            // Show the target name (e.g. doc:app) rather than the display name (app)
                            JMenuItem menuitem = new JMenuItem(target.getTarget());
                            menuitem.addActionListener(new TargetMenuItemHandler(project, target, debug));
                            menuitem.setToolTipText(target.getDescription());
                            add(menuitem);
                            needsep = true;
                        }
                    }
                }

                if (needsep) {
                    needsep = false;
                    addSeparator();
                }

                // TODO - add the default target here (if I can find out what it is)
                for (RakeTarget target : targets) {
                    if (target.isTarget()) {
                        JMenuItem menuitem = new JMenuItem(target.getDisplayName());
                        menuitem.addActionListener(new TargetMenuItemHandler(project, target, debug));
                        menuitem.setToolTipText(target.getDescription());
                        add(menuitem);
                    } else {
                        JMenu submenu = buildMenu(target);
                        add(submenu);
                    }

                    needsep = true;
                }

                if (needsep) {
                    needsep = false;
                    addSeparator();
                }

                JMenuItem menuitem =
                    new JMenuItem(NbBundle.getMessage(RakeTargetsAction.class, "RefreshTargets"));
                menuitem.addActionListener(new TargetMenuItemHandler(project,
                        new RakeTarget(REFRESH_TARGETS, null, null), debug));
                menuitem.setToolTipText(NbBundle.getMessage(RakeTargetsAction.class,
                        "RefreshTargetsHint"));
                add(menuitem);

                //add(new AdvancedAction(project, allTargets));
            }

            return super.getPopupMenu();
        }

        private JMenu buildMenu(RakeTarget target) {
            assert !target.isTarget();

            JMenu submenu = new JMenu(target.getDisplayName());

            List<RakeTarget> children = target.getChildren();

            for (RakeTarget child : children) {
                if (child.isTarget()) {
                    JMenuItem menuitem = new JMenuItem(child.getDisplayName());
                    menuitem.addActionListener(new TargetMenuItemHandler(project, child, debug));
                    menuitem.setToolTipText(child.getDescription());
                    submenu.add(menuitem);
                } else {
                    // Recurse
                    JMenu m = buildMenu(child);
                    submenu.add(m);
                }
            }

            return submenu;
        }
    }

    /**
     * Action handler for a menu item representing one target.
     */
    private static final class TargetMenuItemHandler implements ActionListener, Runnable {
        
        private final Project project;
        private final RakeTarget target;
        private final boolean debug;

        public TargetMenuItemHandler(Project project, RakeTarget target, boolean debug) {
            this.project = project;
            this.target = target;
            this.debug = debug;
        }

        public void actionPerformed(ActionEvent ev) {
            // #16720 part 2: don't do this in the event thread...
            RequestProcessor.getDefault().post(this);
        }

        public void run() {
            if (!RubyPlatform.hasValidRake(project, true)) {
                return;
            }
            if (!RubyPlatform.platformFor(project).showWarningIfInvalid()) {
                return;
            }

            if (target.getTarget().equals(REFRESH_TARGETS)) {
                // Run rake -T to get the actual list of targets
                refreshing = true;

                final ProgressHandle handle;
                handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(
                            RakeTargetsAction.class, "RefreshingTargets"));
                handle.start();
                handle.switchToIndeterminate();

                RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            try {
                                refreshTargets(project);
                            } finally {
                                if (handle != null) {
                                    handle.finish();
                                }
                            }
                        }
                    });

                return;
            }

            // Save all files first
            LifecycleManager.getDefault().saveAll();

            
            // EMPTY CONTEXT??
            FileLocator fileLocator = new RubyFileLocator(Lookup.EMPTY, project);
            String displayName = NbBundle.getMessage(RakeTargetsAction.class, "Rake");

            ProjectInformation info = ProjectUtils.getInformation(project);

            File pwd = null;

            FileObject rakeFile = RakeSupport.findRakeFile(project);
            if (rakeFile == null) {
                pwd = FileUtil.toFile(project.getProjectDirectory());
            }

            RakeSupport rake = new RakeSupport(project);
            
            String targetName = target.getTarget();

            if (targetName != null && (targetName.equals("test") || targetName.startsWith("test:"))) { // NOI18N
                rake.setTest(true);
            }

            if (info != null) {
                displayName = info.getDisplayName();
            }
            
            displayName += " (" + targetName  + ')';

            rake.runRake(pwd, rakeFile, displayName, fileLocator, true, debug, targetName);

            // Update recent targets list: add or move to end
            if (rakeFile != null) {
                List<RakeTarget> recent = recentTargets.get(rakeFile);
                if (recent == null) {
                    recent = new ArrayList<RakeTarget>();
                    recentTargets.put(rakeFile, recent);
                }
                recent.remove(target);
                recent.add(target);
            }
        }
    }

    private static class RakeTarget {
        
        private final String target;
        private final String description;
        private final String displayName;
        private List<RakeTarget> children;

        /** Create a folder */
        public RakeTarget(String displayName) {
            this(null, displayName, null);
        }

        /** Create an actual target */
        public RakeTarget(String target, String displayName, String description) {
            this.target = target;
            this.displayName = displayName;
            this.description = description;
        }

        public boolean isTarget() {
            return target != null;
        }

        public String getTarget() {
            return target;
        }

        public List<RakeTarget> getChildren() {
            return children;
        }

        public String getDescription() {
            return description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void addChild(RakeTarget child) {
            if (children == null) {
                children = new ArrayList<RakeTarget>();
            }

            children.add(child);
        }

        public @Override boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RakeTarget other = (RakeTarget) obj;
            if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
                return false;
            }
            return true;
        }

        public @Override int hashCode() {
            int hash = 7;
            hash = 59 * hash + (this.target != null ? this.target.hashCode() : 0);
            return hash;
        }
        
    }
}
