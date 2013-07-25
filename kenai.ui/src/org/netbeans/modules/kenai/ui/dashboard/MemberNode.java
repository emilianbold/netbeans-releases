/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.kenai.ui.MemberAccessorImpl;
import org.netbeans.modules.kenai.ui.api.KenaiUserUI;
import org.netbeans.modules.team.server.ui.common.LinkButton;
import org.netbeans.modules.team.server.ui.spi.MemberHandle;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Node for a single project's source repository.
 *
 * @author Jan Becicka
 */
public class MemberNode extends LeafNode {

    private final MemberHandle user;

    private JPanel panel;
    private JLabel lbl;
    private LinkButton btn;
    private final Object LOCK = new Object();

    public MemberNode( final MemberHandle user, TreeListNode parent ) {
        super( parent );
        this.user = user;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int maxWidth) {
        synchronized (LOCK) {
            if (null == panel) {
                panel = new JPanel(new BorderLayout());
                panel.setOpaque(false);
                String role = user.getRole();
                String displayName = role != null
                        ? NbBundle.getMessage(MemberNode.class, "LBL_MEMBER_FORMAT", user.getDisplayName(), role) // NOI18N
                        : user.getDisplayName();
                lbl = new TreeLabel(displayName) {

                    @Override
                    public String getToolTipText() {
                        return NbBundle.getMessage(MemberNode.class, user.isOnline() ? "LBL_ONLINE_MEMBER_TOOLTIP" : "LBL_OFFLINE_MEMBER_TOOLTIP", user.getDisplayName(), user.getFullName()); // NOI18N
                    }
                };
                lbl.setIcon(new KenaiUserUI(user.getFQN()).getIcon());
                panel.add(lbl, BorderLayout.CENTER);
                btn = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/collab/resources/newmessage.png", true), getDefaultAction()); // NOI18N
                panel.add(btn, BorderLayout.EAST);
                
                panel.validate();
            }
            lbl.setForeground(foreground);
            if (btn != null) {
                btn.setForeground(foreground);
                btn.setVisible(user.hasMessages());
            }
            return panel;
        }
    }

    @Override
    public Action getDefaultAction() {
        return MemberAccessorImpl.getDefault().getStartChatAction(user);
    }

    @Override
    public Action[] getPopupActions() {
        return new Action[]{getDefaultAction()};
    }

}
