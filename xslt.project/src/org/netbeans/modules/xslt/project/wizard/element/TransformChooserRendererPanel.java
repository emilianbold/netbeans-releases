/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xslt.project.wizard.element;

import java.awt.GridBagConstraints;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableCellRenderer;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformChooserRendererPanel extends JPanel {
    public TransformChooserRendererPanel() {
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints c = new GridBagConstraints();
        myPanel = new JPanel();
        myLabel = new DefaultTableCellRenderer();
        myButton = new JButton();
        
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        c.gridheight = java.awt.GridBagConstraints.REMAINDER;
        c.fill = java.awt.GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        myPanel.add(myLabel, c);
        
        //button
        setLayout(new java.awt.GridBagLayout());
        
        myButton.setText("...");
        myButton.setMargin(new java.awt.Insets(TINY_SIZE, SMALL_SIZE, TINY_SIZE, SMALL_SIZE));
        myButton.setMaximumSize(new java.awt.Dimension(LARGE_SIZE, LARGE_SIZE));
        myButton.setMinimumSize(new java.awt.Dimension(LARGE_SIZE, LARGE_SIZE));
        myButton.setPreferredSize(new java.awt.Dimension(LARGE_SIZE, LARGE_SIZE));

        c = new java.awt.GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        add(myButton, c);

        //panel
        c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = java.awt.GridBagConstraints.BOTH;
        c.weightx = 1.0;
        add(myPanel, c);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        myLabel.setEnabled(enabled);
    }

    public DefaultTableCellRenderer getLabel() {
        return myLabel;
    }
    
    public void setLabel(DefaultTableCellRenderer label) {
        removeAll();
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(label, gridBagConstraints);
        revalidate();
    }
    
    private JButton myButton;
    private DefaultTableCellRenderer myLabel;
    private JPanel myPanel;
}
