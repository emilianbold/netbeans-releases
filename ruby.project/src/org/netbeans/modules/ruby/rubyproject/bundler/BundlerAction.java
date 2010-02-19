/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rubyproject.bundler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.project.Project;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Erno Mononen
 */
public final class BundlerAction extends AbstractAction implements ContextAwareAction {

    private final BundlerSupport support;

    public BundlerAction(BundlerSupport support) {
        this.support = support;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        assert false : "No context";
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAction(actionContext);
    }

    private final class ContextAction extends AbstractAction implements Presenter.Popup {

        private final Project project;

        public ContextAction(Lookup lkp) {
            super(NbBundle.getMessage(BundlerAction.class, "Bundler"));

            Collection<? extends Project> apcs = lkp.lookupAll(Project.class);

            if (apcs.size() == 1) {
                project = apcs.iterator().next();
            } else {
                project = null;
            }

            super.setEnabled(project != null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false : "Action should not be called directly";
        }

        @Override
        public JMenuItem getPopupPresenter() {
            if (project != null) {
                return createMenu();
            } else {
                return new Actions.MenuItem(this, false);
            }
        }

        @Override
        public void setEnabled(boolean b) {
            assert false : "No modifications to enablement status permitted";
        }

        private JMenuItem createMenu() {
            JMenu menu = new LazyMenu(support);
            return menu;
        }
    }

    private static final class LazyMenu extends JMenu {

        private final BundlerSupport support;
        private boolean initialized;

        public LazyMenu(BundlerSupport support) {
            super(NbBundle.getMessage(BundlerAction.class, "Bundler"));
            this.support = support;
        }

        @Override
        public JPopupMenu getPopupMenu() {
            if (!initialized) {
                initialized = true;
                super.removeAll();
                for (BundlerSupport.Task task : support.getTasks()) {
                    final JMenuItem item = new JMenuItem(task.name);
                    item.setToolTipText(task.descriptor);
                    item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            support.runBundlerTask(item.getText(), null, true, null);
                        }
                    });
                    add(item);
                }
                if (support.canUpdateIndices()) {
                    addSeparator();
                    JMenuItem updateIndices = new JMenuItem(NbBundle.getMessage(BundlerAction.class, "UpdateIndices"));
                    updateIndices.setToolTipText(NbBundle.getMessage(BundlerAction.class, "UpdateIndicesDesc"));
                    updateIndices.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            support.updateIndices();
                        }
                    });
                    add(updateIndices);
                }
            }
            return super.getPopupMenu();
        }

    }

}
