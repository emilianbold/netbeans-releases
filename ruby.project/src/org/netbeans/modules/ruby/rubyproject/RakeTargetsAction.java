/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.rubyproject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
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
import java.util.List;
import java.util.Map;

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
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.rubyproject.RakeSupport;
import org.netbeans.modules.ruby.rubyproject.api.RubyExecution;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.rubyproject.execution.FileLocator;
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
public final class RakeTargetsAction extends SystemAction implements ContextAwareAction {
    private static List<RakeTarget> recentTargets = new ArrayList<RakeTarget>();

    /** Set during a refresh */
    private static volatile boolean refreshing;
    private static final String RAKE_T_OUTPUT = "nbproject/private/rake-t.txt"; // NOI18N
    private static final String REFRESH_TARGETS = "netbeans-refresh-targets"; // NOI18N
    private static final String RAKE_ABORTED = "rake aborted!"; // NOI18N

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
        return new ContextAction(actionContext);
    }

    /**
     * Create the submenu.
     */
    private static JMenu createMenu(Project project) {
        return new LazyMenu(project);
    }

    private static List<RakeTarget> getRakeTargets(Project project) {
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
        if (!RubyInstallation.getInstance().isValidRake(false)) {
            return;
        }

        refreshing = true;

        String rakeOutput = hiddenRakeRunner(project);
        writeRakeTargets(project, rakeOutput);
        refreshing = false;
    }

    private static FileObject getRakeFile(Project project) {
        FileObject pwd = project.getProjectDirectory();

        // See if we're in the right directory
        String[] rakeFiles = new String[] {"rakefile", "Rakefile", "rakefile.rb", "Rakefile.rb" }; // NOI18N
        for (String s : rakeFiles) {
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
                for (String s : rakeFiles) {
                    FileObject r = f.getFileObject(s);
                    if (r != null) {
                        return r;
                    }
                }
            }
        }
        
        return null;
    }
    
    private static String hiddenRakeRunner(Project project) {
        File pwd;
        FileObject rakeFile = getRakeFile(project);
        if (rakeFile == null) {
            pwd = FileUtil.toFile(project.getProjectDirectory());
        } else {
            pwd = FileUtil.toFile(rakeFile.getParent());
        }

        // Install the given gem
        String rakeCmd = RubyInstallation.getInstance().getRake();
        List<String> argList = new ArrayList<String>();

        File cmd = new File(RubyInstallation.getInstance().getRuby());

        if (!cmd.getName().startsWith("jruby") || RubyExecution.LAUNCH_JRUBY_SCRIPT) {
            argList.add(cmd.getPath());
        }

        String rubyHome = cmd.getParentFile().getParent();
        String cmdName = cmd.getName();
        argList.addAll(RubyExecution.getRubyArgs(rubyHome, cmdName));

        argList.add(rakeCmd);
        argList.add("-T");

        String[] args = argList.toArray(new String[argList.size()]);
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(pwd);
        pb.redirectErrorStream(true);

        // PATH additions for JRuby etc.
        Map<String, String> env = pb.environment();
        new RubyExecution(new ExecutionDescriptor("rake", pwd).cmd(cmd)).setupProcessEnvironment(env);

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

        public ContextAction(Lookup lkp) {
            super(SystemAction.get(RakeTargetsAction.class).getName());

            Collection<?extends Project> apcs = lkp.lookupAll(Project.class);
            Project p = null;

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
                return createMenu(project);
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
        private boolean initialized = false;

        public LazyMenu(Project project) {
            super(SystemAction.get(RakeTargetsAction.class).getName());
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
                for (int i = recentTargets.size() - 1; i >= 0; i--) {
                    RakeTarget target = recentTargets.get(i);
                    assert target.isTarget();

                    // Show the target name (e.g. doc:app) rather than the display name (app)
                    JMenuItem menuitem = new JMenuItem(target.getTarget());
                    menuitem.addActionListener(new TargetMenuItemHandler(project, target));
                    menuitem.setToolTipText(target.getDescription());
                    add(menuitem);
                    needsep = true;
                }

                if (needsep) {
                    needsep = false;
                    addSeparator();
                }

                // TODO - add the default target here (if I can find out what it is)
                for (RakeTarget target : targets) {
                    if (target.isTarget()) {
                        JMenuItem menuitem = new JMenuItem(target.getDisplayName());
                        menuitem.addActionListener(new TargetMenuItemHandler(project, target));
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
                        new RakeTarget(REFRESH_TARGETS, null, null)));
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
                    menuitem.addActionListener(new TargetMenuItemHandler(project, child));
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

        public TargetMenuItemHandler(Project project, RakeTarget target) {
            this.project = project;
            this.target = target;
        }

        public void actionPerformed(ActionEvent ev) {
            // #16720 part 2: don't do this in the event thread...
            RequestProcessor.getDefault().post(this);
        }

        public void run() {
            if (!RubyInstallation.getInstance().isValidRake(true)) {
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

            if (info != null) {
                displayName = info.getDisplayName();
            }

            File pwd = null;

            FileObject rakeFile = getRakeFile(project);
            if (rakeFile == null) {
                pwd = FileUtil.toFile(project.getProjectDirectory());
            }

            // XXX TODO - how do we obtain the target name now?
            String charsetName = null;
            RakeSupport rake = new RakeSupport(charsetName);
            // TODO - set class path?

            String targetName = target.getTarget();

            if (targetName != null && (targetName.equals("test") || targetName.startsWith("test:"))) { // NOI18N
                rake.setTest(true);
            }

            rake.runRake(pwd, rakeFile, displayName, fileLocator, true, targetName);

            // Update recent targets list: add or move to end
            recentTargets.remove(target);
            recentTargets.add(target);
        }
    }

    private static class RakeTarget {
        private String target;
        private String description;
        private String displayName;
        private List<RakeTarget> children;

        /** Create a folder */
        public RakeTarget(String displayName) {
            this.displayName = displayName;
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
    }
}
