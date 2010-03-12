/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

public class MoreBuildActionsAction extends AbstractAction implements Presenter.Menu, Presenter.Popup {
    private JMenu subMenu;
    /** Creates a new instance of BrowserAction */
    public MoreBuildActionsAction() {
        super(NbBundle.getMessage( MoreBuildActionsAction.class, "LBL_MoreBuildActionsAction_Name"), null);   // NOI18N
    }
        
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        // no operation
    }

    @Override
    public JMenuItem getPopupPresenter() {
        createSubMenu();
        return subMenu;
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return subMenu;
    }
        
    private void createSubMenu() {
        if ( subMenu == null ) {
            String label = getString("LBL_MoreBuildActionsAction_Name"); // NOI18N
            subMenu = new JMenu( label );
        }
        
        subMenu.removeAll();
        subMenu.add(ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BUILD_PACKAGE, getString("LBL_BuildPackagesAction_Name"), null)); // NOI18N
        subMenu.add(ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BATCH_BUILD, getString("LBL_BatchBuildAction_Name"), null)); // NOI18N
    }

    private static String getString(String key) {
        return NbBundle.getMessage(MoreBuildActionsAction.class, key);
    }
    
}
