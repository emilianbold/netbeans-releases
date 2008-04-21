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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.rubyproject.RakeSupport;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
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

        Map<Long,String> versions = getVersions(project);

        if (!versions.isEmpty()) {
            menu.addSeparator();

            List<Long> sortedList = new ArrayList<Long>();
            sortedList.addAll(versions.keySet());
            Collections.sort(sortedList);

            buildMenu(project, menu, 0, sortedList.size()-1, sortedList, versions);
        }
    }
        
    private static void buildMenu(RailsProject project, JMenu menu, int startIndex, int endIndex, List<Long> versions, Map<Long,String> descriptions) {
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
                    long startVersion = versions.get(start);
                    long endVersion = versions.get(end);
                    JMenu submenu = new JMenu(NbBundle.getMessage(MigrateAction.class, "VersionXtoY",
                            Long.toString(startVersion), Long.toString(endVersion)));
                    buildMenu(project, submenu, start, end, versions, descriptions);
                    menu.add(submenu);
                }
            }

            return;
        }

        for (int i = startIndex; i <= endIndex; i++) {
            long version = versions.get(i);
            String description = descriptions.get(version);
            if (description == null) {
                description = "";
            }
            JMenuItem menuitem = new JMenuItem(NbBundle.getMessage(MigrateAction.class,
                        "VersionX", Long.toString(version), description));
            menuitem.addActionListener(new MigrateMenuItemHandler(project, version));
            menu.add(menuitem);
        }
    }

    static Map<Long,String> getVersions(RailsProject project) {
        FileObject projectDir = project.getProjectDirectory();

        Map<Long,String> versions = new HashMap<Long,String>();
        // NOTE - FileObject.getFileObject wants / as a path separator, not File.separator!
        FileObject migrate = projectDir.getFileObject("db/migrate"); // NOI18N

        if (migrate == null) {
            return Collections.emptyMap();
        }

        for (FileObject fo : migrate.getChildren()) {
            String name = fo.getName();
            if (fo.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
                if (name.matches("^\\d\\d\\d_.*")) { // NOI18N
                    try {
                        long version = Integer.parseInt(fo.getName().substring(0, 3));
                        String description = RubyUtils.underlinedNameToCamel(name.substring(4));
                        versions.put(version, "- " + description);
                    } catch (NumberFormatException nfe) {
                        // Shouldn't happen since we've prevetted the digits
                        Exceptions.printStackTrace(nfe);
                    }
                } else if (name.matches("^\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d_.*")) { // NOI18N
                    try {
                        long version = Long.parseLong(fo.getName().substring(0, 14));
                        String description = RubyUtils.underlinedNameToCamel(name.substring(15));
                        versions.put(version, "- " + description);
                    } catch (NumberFormatException nfe) {
                        // Shouldn't happen since we've prevetted the digits
                        Exceptions.printStackTrace(nfe);
                    }
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
        private final long version;
        private static final String WARN_ON_CLEAR_PREF_ID = "confirmMigratingToVersion0"; //NOI18N

        public MigrateMenuItemHandler(RailsProject project, long version) {
            this.project = project;
            this.version = version;
        }

        public void actionPerformed(ActionEvent ev) {
            // #16720 part 2: don't do this in the event thread...
            RequestProcessor.getDefault().post(this);
        }

        public void run() {
            if (!RubyPlatform.hasValidRake(project, true)) {
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
            if (version == 0 && !confirmReset()) {
                return;
            }
            if (version == -1) {
                // Run to the current migration
                rake.runRake(pwd, null, displayName, fileLocator, true, false, "db:migrate"); // NOI18N
            } else {
                rake.runRake(pwd, null, displayName, fileLocator, true, false, "db:migrate", // NOI18N
                    "VERSION=" + Long.toString(version)); // NOI18N
            }
        }
        
        /**
         * Displays a dialog for confirming whether the migrations should be run.
         * See #125606.
         */
        private boolean confirmReset() {
            
            Preferences prefs = NbPreferences.forModule(MigrateAction.class);
            if (!prefs.getBoolean(WARN_ON_CLEAR_PREF_ID, true)) {
                return true;
            }
            final JCheckBox showWarning = new JCheckBox(NbBundle.getMessage(MigrateAction.class, "ShowConfirmDialog"));
            showWarning.setSelected(true);
            DialogDescriptor dd =
                    new DialogDescriptor(
                    NbBundle.getMessage(MigrateAction.class, "ConfirmReset"),
                    NbBundle.getMessage(MigrateAction.class, "ConfirmResetTitle"));

            Object[] options = new Object[]{
                DialogDescriptor.OK_OPTION, DialogDescriptor.NO_OPTION, DialogDescriptor.CANCEL_OPTION
            };
            
            dd.setOptions(options);
            dd.setClosingOptions(options);
            dd.setAdditionalOptions(new Object[]{showWarning});
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (result.equals(NotifyDescriptor.OK_OPTION) || result.equals(NotifyDescriptor.NO_OPTION)) {
                prefs.putBoolean(WARN_ON_CLEAR_PREF_ID, showWarning.isSelected());
            }
            return result.equals(NotifyDescriptor.OK_OPTION);
        }
    }
}
