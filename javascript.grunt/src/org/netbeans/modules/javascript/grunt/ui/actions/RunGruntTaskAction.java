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
package org.netbeans.modules.javascript.grunt.ui.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.grunt.GruntBuildTool;
import org.netbeans.modules.javascript.grunt.exec.GruntExecutable;
import org.netbeans.modules.javascript.grunt.file.GruntTasks;
import org.netbeans.modules.javascript.grunt.file.Gruntfile;
import org.netbeans.modules.javascript.grunt.ui.options.GruntOptionsPanelController;
import org.netbeans.modules.javascript.grunt.util.GruntUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

@ActionID(id = "org.netbeans.modules.javascript.grunt.ui.actions.RunGruntTaskAction", category = "Build")
@ActionRegistration(displayName = "#RunGruntTaskAction.name", lazy = false)
@ActionReferences({
    @ActionReference(path = "Editors/text/grunt+javascript/Popup", position = 900),
    @ActionReference(path = "Loaders/text/grunt+javascript/Actions", position = 150),
    @ActionReference(path = "Projects/org-netbeans-modules-web-clientproject/Actions", position = 180),
    @ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 670),
    @ActionReference(path = "Projects/org-netbeans-modules-web-project/Actions", position = 670),
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 770),
})
@NbBundle.Messages("RunGruntTaskAction.name=Grunt Tasks")
public final class RunGruntTaskAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    static final RequestProcessor RP = new RequestProcessor(RunGruntTaskAction.class.getName(), 2);

    private final Project project;


    public RunGruntTaskAction() {
        this(null);
    }

    public RunGruntTaskAction(Project project) {
        this.project = project;
        setEnabled(project != null);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        // hide this action in IDE Options > Keymap
        putValue(Action.NAME, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        Project contextProject = context.lookup(Project.class);
        if (contextProject != null) {
            // project action
            return createAction(contextProject);
        }
        // gruntfile directly
        FileObject file = context.lookup(FileObject.class);
        if (file == null) {
            DataObject dataObject = context.lookup(DataObject.class);
            if (dataObject != null) {
                file = dataObject.getPrimaryFile();
            }
        }
        if (file == null) {
            return this;
        }
        contextProject = FileOwnerQuery.getOwner(file);
        if (contextProject == null) {
            return null;
        }
        if (!contextProject.getProjectDirectory().equals(file.getParent())) {
            // not a main project gruntfile
            return this;
        }
        return createAction(contextProject);
    }

    private Action createAction(Project contextProject) {
        assert contextProject != null;
        Gruntfile gruntfile = new Gruntfile(contextProject.getProjectDirectory());
        if (!gruntfile.exists()) {
            return this;
        }
        return new RunGruntTaskAction(contextProject);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (project == null) {
            return new Actions.MenuItem(this, false);
        }
        return new LazyMenu(project);
    }

    //~ Inner classes

    private static final class LazyMenu extends JMenu {

        private final Project project;

        // @GuardedBy("EDT")
        boolean menuBuilt = false;


        public LazyMenu(Project project) {
            super(Bundle.RunGruntTaskAction_name());
            assert project != null;
            this.project = project;
        }

        @Override
        public JPopupMenu getPopupMenu() {
            assert EventQueue.isDispatchThread();
            if (!menuBuilt) {
                menuBuilt = true;
                buildMenu();
            }
            return super.getPopupMenu();
        }

        private void buildMenu() {
            assert EventQueue.isDispatchThread();
            GruntTasks gruntTasks = GruntBuildTool.forProject(project).getGruntTasks();
            List<String> tasks = gruntTasks.getTasks();
            if (tasks != null) {
                addTasksMenuItems(tasks);
            }
            // load tasks
            addLoadingMenuItem();
            gruntTasks.processTasks(new GruntTasks.TasksProcessor() {
                @Override
                public void process(final List<String> tasks) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            removeAll();
                            addTasksMenuItems(tasks);
                            refreshMenu();
                        }
                    });
                }
            });
        }

        void refreshMenu() {
            JPopupMenu popupMenu = getPopupMenu();
            popupMenu.pack();
            popupMenu.invalidate();
            popupMenu.revalidate();
            popupMenu.repaint();
        }

        void addTasksMenuItems(@NullAllowed List<String> tasks) {
            assert EventQueue.isDispatchThread();
            if (tasks == null) {
                // grunt cli error?
                addConfigureGruntMenuItem();
                return;
            }
            // default task?
            Set<String> allTasks = new LinkedHashSet<>(tasks);
            if (allTasks.remove(GruntTasks.DEFAULT_TASK)) {
                addTaskMenuItem(GruntTasks.DEFAULT_TASK);
                addSeparator();
            }
            for (String task : allTasks) {
                addTaskMenuItem(task);
            }
            if (!allTasks.isEmpty()) {
                addSeparator();
            }
            addReloadTasksMenuItem();
        }

        private void addTaskMenuItem(final String task) {
            JMenuItem menuitem = new JMenuItem(task);
            menuitem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            GruntExecutable grunt = GruntExecutable.getDefault(project, true);
                            if (grunt != null) {
                                GruntUtils.logUsageGruntBuild();
                                grunt.run(task);
                            }
                        }
                    });
                }
            });
            add(menuitem);
        }

        @NbBundle.Messages("LazyMenu.tasks.loading=Loading Tasks...")
        private void addLoadingMenuItem() {
            JMenuItem menuItem = new JMenuItem(Bundle.LazyMenu_tasks_loading());
            menuItem.setEnabled(false);
            add(menuItem);
        }

        @NbBundle.Messages("LazyMenu.grunt.configure=Configure Grunt...")
        private void addConfigureGruntMenuItem() {
            JMenuItem menuItem = new JMenuItem(Bundle.LazyMenu_grunt_configure());
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    OptionsDisplayer.getDefault().open(GruntOptionsPanelController.OPTIONS_PATH);
                }
            });
            add(menuItem);
        }

        @NbBundle.Messages("LazyMenu.tasks.reload=Reload Tasks")
        private void addReloadTasksMenuItem() {
            JMenuItem menuItem = new JMenuItem(Bundle.LazyMenu_tasks_reload());
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    assert EventQueue.isDispatchThread();
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            GruntTasks gruntTasks = GruntBuildTool.forProject(project).getGruntTasks();
                            gruntTasks.reset();
                            gruntTasks.processTasks(GruntTasks.TasksProcessor.DEV_NULL);
                        }
                    });
                    menuBuilt = false;
                }
            });
            add(menuItem);
        }

    }

}
