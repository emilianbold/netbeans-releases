/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui.dashboard;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.kenai.ui.treelist.LeafNode;
import org.netbeans.modules.kenai.ui.spi.ProjectAccessor;
import org.openide.util.NbBundle;

/**
 * The very first node in dashboard window when the user isn't logged in.
 * 
 * @author S. Aubrecht
 */
public class LoginNode extends LeafNode {

    private final DashboardImpl dashboard;

    private JPanel panel;
    private LinkButton btnLogin;
    private JLabel lblStatus;
    private LinkButton btnOpenProject;

    public LoginNode( DashboardImpl dashboard ) {
        super( null );
        this.dashboard = dashboard;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        if( null == panel ) {
            panel = new JPanel( new GridBagLayout() );
            panel.setOpaque(false);

            btnLogin = new LinkButton(NbBundle.getMessage(LoginNode.class, "LBL_LoginToKenai"), //NOI18N
                    dashboard.createLoginAction());
            lblStatus = new JLabel();
            lblStatus.setHorizontalAlignment(JLabel.CENTER);
            btnOpenProject = new LinkButton(NbBundle.getMessage(LoginNode.class, "LBL_OpenProject"),  //NOI18N
                    ProjectAccessor.getDefault().getOpenNonMemberProjectAction());

            panel.add( btnLogin, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0,0));
            panel.add( lblStatus, new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
            panel.add( btnOpenProject, new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));
        }

        if( isSelected ) {
            lblStatus.setForeground(foreground);
        } else {
            lblStatus.setForeground(ColorManager.errorColor);
        }
        btnOpenProject.setForeground(foreground, isSelected);
        btnLogin.setForeground(foreground, isSelected);
        return panel;
    }
}
