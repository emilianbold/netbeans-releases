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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.ui.spi.KenaiUser;
import org.netbeans.modules.kenai.ui.spi.MemberAccessor;
import org.netbeans.modules.kenai.ui.treelist.LeafNode;
import org.netbeans.modules.kenai.ui.treelist.TreeListNode;
import org.netbeans.modules.kenai.ui.spi.MemberHandle;
import org.netbeans.modules.kenai.ui.treelist.TreeLabel;
import org.openide.util.ImageUtilities;

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

    public MemberNode( final MemberHandle user, TreeListNode parent ) {
        super( parent );
        this.user = user;
        user.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        lbl.setIcon(KenaiUser.forName(user.getName()).getIcon());
                    }
                });
            }
        });
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        if( null == panel ) {
            panel = new JPanel( new BorderLayout() );
            panel.setOpaque(false);
            lbl = new TreeLabel( user.getDisplayName() );
            lbl.setIcon(KenaiUser.forName(user.getName()).getIcon());
            panel.add( lbl, BorderLayout.CENTER);
            if (user.hasMessages()) {
                btn = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/collab/resources/newmessage.png", true), getDefaultAction());
                panel.add(btn, BorderLayout.EAST);
                panel.validate();
                btn.setForeground(foreground);
            }
        }
       lbl.setForeground(foreground);
       return panel;
    }

    @Override
    public Action getDefaultAction() {
        return MemberAccessor.getDefault().getStartChatAction(user);
    }
}
