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

/*
 * MissingClientPanel.java
 *
 * Created on Jul 9, 2008, 4:55:42 PM
 */

package org.netbeans.modules.bugtracking.jira;

import java.awt.Dimension;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.LayoutStyle;

/**
 *
 * @author Tomas Stupka
 */
class MissingJiraSupportPanel extends javax.swing.JPanel {

    final javax.swing.JButton downloadButton = new javax.swing.JButton();
    private javax.swing.JTextPane pane;
    
    /** Creates new form MissingClientPanel */
    public MissingJiraSupportPanel(boolean containerGaps, String msg) {
        initComponents(containerGaps, msg);
    }

    private void initComponents(boolean containerGaps, String msg) {

        pane = new javax.swing.JTextPane();
        pane.setBackground(this.getBackground());
        pane.setContentType("text/html"); // NOI18N
        pane.setText(msg);
        pane.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(downloadButton, org.openide.util.NbBundle.getMessage(MissingJiraSupportPanel.class, "MissingJiraSupportPanel.downloadButton.text")); // NOI18N
        this.setPreferredSize(new Dimension(650, 100));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(createSequentialGroup(layout, containerGaps)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(pane, GroupLayout.PREFERRED_SIZE, 200, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downloadButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, createSequentialGroup(layout, containerGaps)
                .addComponent(pane)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(downloadButton))
                .addContainerGap(60, Short.MAX_VALUE)
                    )
        );
    }

    private SequentialGroup createSequentialGroup(GroupLayout layout, boolean containerGaps) {
        SequentialGroup sg = layout.createSequentialGroup();
        if (containerGaps) {
            sg.addContainerGap();
        }
        return sg;
    }
    
}
