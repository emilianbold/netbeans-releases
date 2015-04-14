/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.api.build.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.clientproject.api.build.BuildTools.TasksMenuSupport;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public class TasksMenu extends JMenu {

    static final Logger LOGGER = Logger.getLogger(TasksMenu.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(TasksMenu.class);

    final TasksMenuSupport support;
    final AdvancedTasks advancedTasks;

    // @GuardedBy("EDT")
    private boolean menuBuilt = false;


    public TasksMenu(TasksMenuSupport support) {
        super(support.getTitle(TasksMenuSupport.Title.MENU));
        assert support != null;
        this.support = support;
        advancedTasks = new AdvancedTasks(support.getProject(), support.getIdentifier());
    }

    boolean isMenuBuilt() {
        assert EventQueue.isDispatchThread();
        return menuBuilt;
    }

    void setMenuBuilt(boolean menuBuilt) {
        assert EventQueue.isDispatchThread();
        this.menuBuilt = menuBuilt;
    }

    @Override
    public JPopupMenu getPopupMenu() {
        assert EventQueue.isDispatchThread();
        if (!isMenuBuilt()) {
            setMenuBuilt(true);
            buildMenu();
        }
        return super.getPopupMenu();
    }

    private void buildMenu() {
        assert EventQueue.isDispatchThread();
        final Future<List<String>> tasks = support.getTasks();
        if (tasks.isDone()) {
            try {
                addMenuItems(tasks.get());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return;
        }
        // load tasks
        addLoadingMenuItem();
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final AtomicReference<List<String>> allTasks = new AtomicReference<>(tasks.get(1, TimeUnit.MINUTES));
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            removeAll();
                            addMenuItems(allTasks.get());
                            refreshMenu();
                        }
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException | TimeoutException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
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

    void addMenuItems(@NullAllowed List<String> tasks) {
        Collection<String> allTasks = addTasksMenuItems(tasks);
        if (tasks != null) { // not 'allTasks' intentianlly, one can run default task e.g. with just some parameters
            addAdvancedMenuItems(allTasks);
        }
        addReloadTasksMenuItem();
    }

    @CheckForNull
    private Collection<String> addTasksMenuItems(@NullAllowed List<String> tasks) {
        assert EventQueue.isDispatchThread();
        if (tasks == null) {
            // build tool cli error?
            addConfigureToolMenuItem();
            return null;
        }
        // default task
        final String defaultTaskName = support.getDefaultTaskName();
        if (defaultTaskName != null) {
            addTaskMenuItem(true, defaultTaskName, false);
        }
        addSeparator();
        Set<String> allTasks = new LinkedHashSet<>(tasks);
        if (defaultTaskName != null) {
            allTasks.remove(defaultTaskName);
        }
        for (String task : allTasks) {
            addTaskMenuItem(false, task, false);
        }
        if (!allTasks.isEmpty()) {
            addSeparator();
        }
        return allTasks;
    }

    @NbBundle.Messages("TasksMenu.menu.advanced=Advanced...")
    private void addAdvancedMenuItems(final Collection<String> tasks) {
        assert EventQueue.isDispatchThread();
        assert tasks != null;
        // last advanced tasks
        for (String task : advancedTasks.getTasks()) {
            addTaskMenuItem(false, task, true);
        }
        // advanced...
        JMenuItem menuItem = new JMenuItem(Bundle.TasksMenu_menu_advanced());
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                assert EventQueue.isDispatchThread();
                List<String> tasksWithDefaultTask = new ArrayList<>(tasks.size() + 1);
                // add empty task for default task/action
                tasksWithDefaultTask.add(""); // NOI18N
                tasksWithDefaultTask.addAll(tasks);
                final Pair<Boolean, String> advancedTask = AdvancedTaskPanel.open(support.getTitle(TasksMenuSupport.Title.RUN_ADVANCED),
                        support.getTitle(TasksMenuSupport.Title.TASKS_LABEL), support.getTitle(TasksMenuSupport.Title.BUILD_TOOL_EXEC), tasksWithDefaultTask);
                if (advancedTask != null) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            String task = advancedTask.second();
                            support.runTask(Utilities.parseParameters(task));
                            advancedTasks.addTask(advancedTask.first(), task);
                        }
                    });
                }
            }
        });
        add(menuItem);
        addSeparator();
    }

    private void addTaskMenuItem(final boolean isDefault, final String task, final boolean isAdvanced) {
        JMenuItem menuitem = new JMenuItem(task);
        menuitem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isDefault) {
                            support.runTask();
                        } else if (isAdvanced) {
                            support.runTask(Utilities.parseParameters(task));
                        } else {
                            support.runTask(task);
                        }
                    }
                });
            }
        });
        add(menuitem);
    }

    private void addLoadingMenuItem() {
        JMenuItem menuItem = new JMenuItem(support.getTitle(TasksMenuSupport.Title.LOADING_TASKS));
        menuItem.setEnabled(false);
        add(menuItem);
    }

    private void addConfigureToolMenuItem() {
        JMenuItem menuItem = new JMenuItem(support.getTitle(TasksMenuSupport.Title.CONFIGURE_TOOL));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                support.configure();
            }
        });
        add(menuItem);
    }

    private void addReloadTasksMenuItem() {
        JMenuItem menuItem = new JMenuItem(support.getTitle(TasksMenuSupport.Title.RELOAD_TASKS));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                assert EventQueue.isDispatchThread();
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        support.reloadTasks();
                    }
                });
                setMenuBuilt(false);
            }
        });
        add(menuItem);
    }

}
