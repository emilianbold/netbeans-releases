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
package org.netbeans.modules.php.phing.ui.actions;

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
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.phing.PhingBuildTool;
import org.netbeans.modules.php.phing.exec.PhingExecutable;
import org.netbeans.modules.php.phing.file.PhingTargets;
import org.netbeans.modules.php.phing.ui.options.PhingOptionsPanelController;
import org.netbeans.modules.php.phing.util.PhingUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

@ActionID(id = "org.netbeans.modules.php.phing.ui.actions.RunPhingTargetAction", category = "Build")
@ActionRegistration(displayName = "#RunPhingTargetAction.name", lazy = false)
@ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 630)
@NbBundle.Messages("RunPhingTargetAction.name=Phing Targets")
public final class RunPhingTargetAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    static final RequestProcessor RP = new RequestProcessor(RunPhingTargetAction.class.getName(), 2);

    private final Project project;


    public RunPhingTargetAction() {
        this(null);
    }

    public RunPhingTargetAction(Project project) {
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
        return this;
    }

    private Action createAction(Project contextProject) {
        assert contextProject != null;
        PhingBuildTool phingBuildTool = PhingBuildTool.inProject(contextProject);
        if (phingBuildTool == null) {
            return this;
        }
        if (!phingBuildTool.getBuildXml().exists()) {
            return this;
        }
        return new RunPhingTargetAction(contextProject);
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
            super(Bundle.RunPhingTargetAction_name());
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
            PhingTargets phingTargets = PhingBuildTool.forProject(project).getPhingTargets();
            List<String> targets = phingTargets.getTargets();
            if (targets != null) {
                addTargetsMenuItems(targets);
            }
            // load targets
            addLoadingMenuItem();
            phingTargets.processTargets(new PhingTargets.TargetsProcessor() {
                @Override
                public void process(final List<String> targets) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            removeAll();
                            addTargetsMenuItems(targets);
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

        void addTargetsMenuItems(@NullAllowed List<String> targets) {
            assert EventQueue.isDispatchThread();
            if (targets == null) {
                // phing cli error?
                addConfigurePhingMenuItem();
                return;
            }
            // default target
            addDefaultMenuItem();
            addSeparator();
            Set<String> allTargets = new LinkedHashSet<>(targets);
            allTargets.remove(PhingTargets.DEFAULT_TARGET);
            for (String target : allTargets) {
                addTargetMenuItem(target);
            }
            if (!allTargets.isEmpty()) {
                addSeparator();
            }
            addReloadTargetsMenuItem();
        }

        @NbBundle.Messages("LazyMenu.targets.default=default")
        private void addDefaultMenuItem() {
            JMenuItem menuitem = new JMenuItem(Bundle.LazyMenu_targets_default());
            menuitem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            PhingExecutable phing = PhingExecutable.getDefault(project, true);
                            if (phing != null) {
                                PhingUtils.logUsagePhingBuild();
                                phing.run();
                            }
                        }
                    });
                }
            });
            add(menuitem);
        }


        private void addTargetMenuItem(final String targets) {
            JMenuItem menuitem = new JMenuItem(targets);
            menuitem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            PhingExecutable phing = PhingExecutable.getDefault(project, true);
                            if (phing != null) {
                                PhingUtils.logUsagePhingBuild();
                                phing.run(targets);
                            }
                        }
                    });
                }
            });
            add(menuitem);
        }

        @NbBundle.Messages("LazyMenu.targets.loading=Loading Targets...")
        private void addLoadingMenuItem() {
            JMenuItem menuItem = new JMenuItem(Bundle.LazyMenu_targets_loading());
            menuItem.setEnabled(false);
            add(menuItem);
        }

        @NbBundle.Messages("LazyMenu.phing.configure=Configure Phing...")
        private void addConfigurePhingMenuItem() {
            JMenuItem menuItem = new JMenuItem(Bundle.LazyMenu_phing_configure());
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    OptionsDisplayer.getDefault().open(PhingOptionsPanelController.OPTIONS_PATH);
                }
            });
            add(menuItem);
        }

        @NbBundle.Messages("LazyMenu.targets.reload=Reload Targets")
        private void addReloadTargetsMenuItem() {
            JMenuItem menuItem = new JMenuItem(Bundle.LazyMenu_targets_reload());
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    assert EventQueue.isDispatchThread();
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            PhingTargets phingTargets = PhingBuildTool.forProject(project).getPhingTargets();
                            phingTargets.reset();
                            phingTargets.processTargets(PhingTargets.TargetsProcessor.DEV_NULL);
                        }
                    });
                    menuBuilt = false;
                }
            });
            add(menuItem);
        }

    }

}
