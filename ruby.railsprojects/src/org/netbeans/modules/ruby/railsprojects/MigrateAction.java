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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.rubyproject.RakeSupport;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.openide.LifecycleManager;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
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
    
    /** Build up a nested menu of migration tasks for the given project */
    static void buildMenu(JMenu menu, RailsProject project) {
        JMenuItem menuitem =
            new JMenuItem(NbBundle.getMessage(MigrateAction.class, "CurrentVersion"));
        menuitem.addActionListener(new MigrateMenuItemHandler(project, -1));
        //menuitem.setToolTipText(target.getDescription());
        menu.add(menuitem);

        // Also hardcode in version 0 - drop everything
        menuitem = new JMenuItem(NbBundle.getMessage(MigrateAction.class,
                      "Version0", 0));
        menuitem.addActionListener(new MigrateMenuItemHandler(project, 0));
        //menuitem.setToolTipText(target.getDescription());
        menu.add(menuitem);

        Map<Integer,String> versions = getVersions(project);

        if (!versions.isEmpty()) {
            menu.addSeparator();

            List<Integer> sortedList = new ArrayList<Integer>();
            sortedList.addAll(versions.keySet());
            Collections.sort(sortedList);

            buildMenu(project, menu, 0, sortedList.size()-1, sortedList, versions);
        }
    }
        
    private static void buildMenu(RailsProject project, JMenu menu, int startIndex, int endIndex, List<Integer> versions, Map<Integer,String> descriptions) {
        int MAX_ITEMS = 20; // Max number of entries to show
        int MENU_COUNT = 15; // Number of menus to create (possibly nested)
        if (endIndex - startIndex > MAX_ITEMS) {
            int length = endIndex - startIndex;
            int sqrt = (int)Math.sqrt(length);
            if (sqrt < MENU_COUNT) {
                MENU_COUNT = sqrt;
            }
            int divisions = length / MENU_COUNT;

            if (length % MENU_COUNT == 0) {
                // Pull the last item into the previous menu
                MENU_COUNT--;
            }

            // Split the menu up into len/max divisions
            // Each division is a range that will have a menu item
            for (int i = 0; i <= MENU_COUNT; i++) {
                int start = i*divisions+startIndex;
                int end = (i+1)*divisions-1+startIndex;
                if (start > endIndex) {
                    return;
                }
                if (end > endIndex) {
                    end = endIndex;
                } else if (end == endIndex-1) {
                    // Add the last item into this menu
                    end = endIndex;
                }
                if (end == start) {
                    // A single item - just add it as a menu item
                    buildMenu(project, menu, start, end, versions, descriptions);
                } else {
                    int startVersion = versions.get(start);
                    int endVersion = versions.get(end);
                    JMenu submenu = new JMenu(NbBundle.getMessage(MigrateAction.class, "VersionXtoY", startVersion, endVersion));
                    buildMenu(project, submenu, start, end, versions, descriptions);
                    menu.add(submenu);
                }
            }

            return;
        }

        for (int i = startIndex; i <= endIndex; i++) {
            int version = versions.get(i);
            String description = descriptions.get(version);
            if (description == null) {
                description = "";
            }
            JMenuItem menuitem = new JMenuItem(NbBundle.getMessage(MigrateAction.class,
                        "VersionX", version, description));
            menuitem.addActionListener(new MigrateMenuItemHandler(project, version));
            menu.add(menuitem);
        }
    }

    static Map<Integer,String> getVersions(RailsProject project) {
        FileObject projectDir = project.getProjectDirectory();

        Map<Integer,String> versions = new HashMap<Integer,String>();
        // NOTE - FileObject.getFileObject wants / as a path separator, not File.separator!
        FileObject migrate = projectDir.getFileObject("db/migrate"); // NOI18N

        if (migrate == null) {
            return Collections.emptyMap();
        }

        for (FileObject fo : migrate.getChildren()) {
            String name = fo.getName();
            if (fo.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE) &&
                    name.matches("^\\d\\d\\d_.*")) { // NOI18N
                try {
                    int version = Integer.parseInt(fo.getName().substring(0, 3));
                    String description = RubyUtils.underlinedNameToCamel(name.substring(4));
                    versions.put(version, "- " + description);
                } catch (NumberFormatException nfe) {
                    // Shouldn't happen since we've prevetted the digits
                    Exceptions.printStackTrace(nfe);
                }
            }
        }

        return versions;
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
                
                buildMenu(this, project);
            }

            return super.getPopupMenu();
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
            String displayName = NbBundle.getMessage(MigrateAction.class, "Migration");

            //            ProjectInformation info = ProjectUtils.getInformation(project);
            //
            //            if (info != null) {
            //                displayName = info.getDisplayName();
            //            }
            //            
            File pwd = FileUtil.toFile(project.getProjectDirectory());

            RakeSupport rake = new RakeSupport(project);
            if (version == -1) {
                // Run to the current migration
                rake.runRake(pwd, null, displayName, fileLocator, true, "db:migrate"); // NOI18N
            } else {
                rake.runRake(pwd, null, displayName, fileLocator, true, "db:migrate", // NOI18N
                    "VERSION=" + Integer.toString(version)); // NOI18N
            }
        }
    }
}
