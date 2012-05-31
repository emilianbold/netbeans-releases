/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.actions.base;

import org.netbeans.modules.cnd.remote.actions.OpenRemoteProjectAction;
import java.awt.event.ActionEvent;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerListUI;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Vladimir Kvashin
 */
public abstract class RemoteOpenActionBase extends AbstractAction implements DynamicMenuContent, Presenter.Toolbar {

    protected static final String ENV_KEY = "org.netbeans.modules.cnd.remote.actions.ENV"; // NOI18N
    private JMenuItem lastPresenter;
    private JButton lastToolbarPresenter;
    private final boolean allowLocal;
    private boolean isEnabledToolbarAction = true;
    
    public RemoteOpenActionBase(String name, boolean allowLocal) {
        super(name);
        this.allowLocal = allowLocal;
    }

    @Override
    public JComponent[] getMenuPresenters() {
        lastPresenter = createSubMenu();
        return new JComponent[] { lastPresenter };
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] jcs) {
        lastPresenter = createSubMenu();
        return new JComponent[] { lastPresenter };
    }
    
    @Override
    public JButton getToolbarPresenter() {
        if (!allowLocal) {
            isEnabledToolbarAction = !ServerList.getDefaultRecord().getExecutionEnvironment().isLocal();
        }
        lastToolbarPresenter = new JButton() {

            @Override
            public void setEnabled(boolean b) {
                if (!allowLocal) {
                    super.setEnabled(isEnabledToolbarAction);
                } else {
                    super.setEnabled(b);
                }
            }

            @Override
            public boolean isEnabled() {
                if (!allowLocal) {
                    return isEnabledToolbarAction;
                } else {
                    return super.isEnabled();
                }
            }
        };
        Actions.connect(lastToolbarPresenter, this);
        return lastToolbarPresenter;
    }

    private JMenuItem createSubMenu() throws MissingResourceException {
        String label = getSubmenuTitle();
        JMenu subMenu = new JMenu(label);
        subMenu.setIcon(getIcon());
        //if (isEnabled()) {
            for (ServerRecord record : ServerList.getRecords()) {
                if (allowLocal || record.isRemote()) {
                    String text = getItemTitle(record);
                    JMenuItem item = new JMenuItem(text);
                    item.putClientProperty(ENV_KEY, record.getExecutionEnvironment());
                    item.addActionListener(this);
                    subMenu.add(item);
                }
            }
            if (subMenu.getItemCount() > 0) {
                subMenu.add(new JSeparator());
            }
            JMenuItem item = new JMenuItem(NbBundle.getMessage(OpenRemoteProjectAction.class, "LBL_ManagePlatforms_Name"));
            item.putClientProperty(ENV_KEY, null);
            item.addActionListener(this);
            subMenu.add(item);
        //} else {
        //    subMenu.setEnabled(false);
        //}
        return subMenu;
    }

    @Override
    public void setEnabled(boolean enabled) {
        isEnabledToolbarAction = enabled;
        if (lastToolbarPresenter != null) {
            lastToolbarPresenter.setEnabled(enabled);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) e.getSource();
            ExecutionEnvironment env = (ExecutionEnvironment) item.getClientProperty(ENV_KEY);
            if (env == null) {
                ServerListUI.showServerListDialog();
            } else {
                actionPerformed(env);                
            }
        }
    }    

    protected abstract Icon getIcon();
    protected abstract void actionPerformed(ExecutionEnvironment env);
    protected abstract String getSubmenuTitle();
    protected abstract String getItemTitle(ServerRecord record);
}
