/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Zezula
 */
class ClassMemberPanelToolbar extends JPanel implements ActionListener {

    @StaticResource
    private static final String JDOC_ICON = "org/netbeans/modules/java/navigation/resources/javadoc_open.png";          //NOI18N
    private static final String CMD_JDOC = "jdoc";  //NOI18N

    private final ClassMemberPanelUI ui;

    @NbBundle.Messages({
        "TXT_OpenJDoc=Open Javadoc Window",
    })
    ClassMemberPanelToolbar(@NonNull final ClassMemberPanelUI ui) {
        this.ui = ui;
        setLayout(new GridBagLayout());
        final Box box = new Box(BoxLayout.X_AXIS);
        box.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 5));
        final JToolBar toolbar = new NoBorderToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setBorderPainted(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder());
        toolbar.setOpaque(false);
        toolbar.setFocusable(false);
        final JComboBox history = new JComboBox();
        toolbar.add(history);
        final JButton jdocButton = new JButton(ImageUtilities.loadImageIcon(JDOC_ICON, true));
        jdocButton.setActionCommand(CMD_JDOC);
        jdocButton.addActionListener(this);
        jdocButton.setFocusable(false);
        jdocButton.setToolTipText(Bundle.TXT_OpenJDoc());
        toolbar.add(jdocButton);
        box.add (toolbar);
        final GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        add(box,c);        
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (CMD_JDOC.equals(e.getActionCommand())) {
            final TopComponent win = JavadocTopComponent.findInstance();
            if (win != null && !win.isShowing()) {
                win.open();
                win.requestVisible();
                ui.scheduleJavadocRefresh(0);
            }
        }
    }
}
