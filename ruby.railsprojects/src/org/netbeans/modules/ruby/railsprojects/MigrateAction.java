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
package org.netbeans.modules.ruby.railsprojects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.netbeans.modules.ruby.rubyproject.RakeSupport;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.openide.LifecycleManager;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
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
 * Build up menu from
 *    db/migrate/001_init.rb
 * etc.
 *
 * What about migration - can schema.rb contain many versions?
 * ActiveRecord::Schema.define(:version => 2) do
 * ...
 *
 * @author Tor Norbye
 */
public final class MigrateAction extends SystemAction implements ContextAwareAction {
    @Override
    public String getName() {
        return NbBundle.getMessage(MigrateAction.class, "LBL_rake_migrate");
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
    private static JMenu createMenu(RailsProject project) {
        return new LazyMenu(project);
    }

    /**
     * The particular instance of this action for a given project.
     */
    private static final class ContextAction extends AbstractAction implements Presenter.Popup {
        private final RailsProject project;

        public ContextAction(Lookup lkp) {
            super(SystemAction.get(MigrateAction.class).getName());

            Collection<?extends RailsProject> apcs = lkp.lookupAll(RailsProject.class);

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
        private final RailsProject project;
        private boolean initialized = false;

        public LazyMenu(RailsProject project) {
            super(SystemAction.get(MigrateAction.class).getName());
            this.project = project;
        }

        @Override
        public JPopupMenu getPopupMenu() {
            if (!initialized) {
                initialized = true;
                super.removeAll();

                JMenuItem menuitem =
                    new JMenuItem(NbBundle.getMessage(MigrateAction.class, "CurrentVersion"));
                menuitem.addActionListener(new MigrateMenuItemHandler(project, -1));
                //menuitem.setToolTipText(target.getDescription());
                add(menuitem);

                Integer[] versions = getVersions(project);

                if (versions.length > 0) {
                    addSeparator();

                    for (int version : versions) {
                        menuitem = new JMenuItem(NbBundle.getMessage(MigrateAction.class,
                                    "VersionX", version));
                        menuitem.addActionListener(new MigrateMenuItemHandler(project, version));
                        add(menuitem);
                    }
                }
            }

            return super.getPopupMenu();
        }

        private Integer[] getVersions(RailsProject project) {
            FileObject projectDir = project.getProjectDirectory();

            Set<Integer> versions = new HashSet<Integer>();
            // NOTE - FileObject.getFileObject wants / as a path separator, not File.separator!
            FileObject migrate = projectDir.getFileObject("db/migrate"); // NOI18N

            // Allow VERSION=0 to clear database
            versions.add(Integer.valueOf(0));
            
            if (migrate == null) {
                return versions.toArray(new Integer[versions.size()]);
            }

            for (FileObject fo : migrate.getChildren()) {
                if (fo.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE) &&
                        fo.getName().matches("\\d\\d\\d_.*")) { // NOI18N
                    int version = Integer.parseInt(fo.getName().substring(0, 3));
                    versions.add(version);
                }
            }

            List<Integer> sortedList = new ArrayList<Integer>();
            sortedList.addAll(versions);
            Collections.sort(sortedList);

            return sortedList.toArray(new Integer[sortedList.size()]);
        }
    }

    /**
     * Action handler for a menu item representing one target.
     */
    private static final class MigrateMenuItemHandler implements ActionListener, Runnable {
        private final RailsProject project;
        private final int version;

        public MigrateMenuItemHandler(RailsProject project, int version) {
            this.project = project;
            this.version = version;
        }

        public void actionPerformed(ActionEvent ev) {
            // #16720 part 2: don't do this in the event thread...
            RequestProcessor.getDefault().post(this);
        }

        public void run() {
            if (!RubyInstallation.getInstance().isValidRake(true)) {
                return;
            }

            // Save all files first
            LifecycleManager.getDefault().saveAll();

            // EMPTY CONTEXT??
            RailsFileLocator fileLocator = new RailsFileLocator(Lookup.EMPTY, project);
            String displayName = "Migration";

            //            ProjectInformation info = ProjectUtils.getInformation(project);
            //
            //            if (info != null) {
            //                displayName = info.getDisplayName();
            //            }
            //            
            File pwd = FileUtil.toFile(project.getProjectDirectory());

            if (version == -1) {
                // Run to the current migration
                RakeSupport rake = new RakeSupport(project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING));
                rake.runRake(pwd, null, displayName, fileLocator, true, "db:migrate"); // NOI18N
            } else {
                RakeSupport rake = new RakeSupport(project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING));
                rake.runRake(pwd, null, displayName, fileLocator, true, "db:migrate", // NOI18N
                    "VERSION=" + Integer.toString(version)); // NOI18N
            }
        }
    }
}
