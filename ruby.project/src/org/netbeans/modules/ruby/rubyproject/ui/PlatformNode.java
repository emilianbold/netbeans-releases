/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rubyproject.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.ruby.rubyproject.PlatformChangeListener;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 * Represents Ruby Platform node in the Ruby project's logical view.
 */
final class PlatformNode extends AbstractNode {

    private static final Logger LOGGER = Logger.getLogger(PlatformNode.class.getName());
    
    private static final String PLATFORM_ICON = "org/netbeans/modules/ruby/rubyproject/resources/platform.gif"; // NOI18N
    private final RubyBaseProject project;

    PlatformNode(final RubyBaseProject project) {
        super(Children.LEAF);
        this.project = project;
        setIconBaseWithExtension(PLATFORM_ICON);
        project.addPlatformChangeListener(new PlatformChangeListener() {
            public void platformChanged() {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        PlatformNode.this.fireDisplayNameChange(null, null);
                        PlatformNode.this.fireNameChange(null, null);
                    }
                });
            }
        });
    }

    @Override
    public String getDisplayName() {
        return project.getPlatform().getLabel();
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            new ChangePlatformAction()
        };
    }

    private final class ChangePlatformAction extends AbstractAction implements Presenter.Popup {

        public void actionPerformed(ActionEvent e) {
            assert false : "Action should not be called directly";
        }

        public JMenuItem getPopupPresenter() {
            return createMenu();
        }

        private JMenuItem createMenu() {
            JMenu menu = new JMenu(NbBundle.getMessage(PlatformNode.class, "PlatformNode.Change"));
            for (final RubyPlatform platform : RubyPlatformManager.getSortedPlatforms()) {
                JMenuItem item = new JMenuItem(platform.getLabel());
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            project.changeAndStorePlatform(platform);
                        } catch (IOException ex) {
                            LOGGER.log(Level.SEVERE, "Unable to change the platform: " + ex.getLocalizedMessage(), ex);
                        }
                    }
                });
                menu.add(item);
            }
            return menu;
        }
    }
}
