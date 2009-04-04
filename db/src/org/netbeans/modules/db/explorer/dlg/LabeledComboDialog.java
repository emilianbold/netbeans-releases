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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
* @author Slavek Psenicka
*/
public class LabeledComboDialog {
    boolean result = false;
    Dialog dialog = null;
    Object combosel = null;
    JComboBox combo;

    public LabeledComboDialog(String title, String lab, Vector<String> items) {
        try {
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);

            pane.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage (LabeledComboDialog.class, "ACS_AddToIndexDialogA11yDesc")); //NOI18N
            
            // Title

            JLabel label = new JLabel(lab);
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 0;
            layout.setConstraints(label, con);
            pane.add(label);

            // Combo

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 0;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            combo = new JComboBox (items);
            combo.getAccessibleContext().setAccessibleName(NbBundle.getMessage (LabeledComboDialog.class, "ACS_AddToIndexComboA11yName")); //NOI18N
            combo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (LabeledComboDialog.class, "ACS_AddToIndexComboA11yDesc")); //NOI18N
            combo.setToolTipText(NbBundle.getMessage (LabeledComboDialog.class, "ACS_AddToIndexComboA11yDesc")); //NOI18N
            layout.setConstraints(combo, con);
            pane.add(combo);

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    boolean dispcond = true;
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        result = true;
                        combosel = combo.getSelectedItem();
                    } else
                        result = false;

                    if (dispcond) {
                        dialog.setVisible(false);
                        dialog.dispose();
                    } else
                        Toolkit.getDefaultToolkit().beep();
                }
            };

            DialogDescriptor descriptor = new DialogDescriptor(pane, title, true, listener);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(false);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    public boolean run() {
        if (dialog != null)
            dialog.setVisible(true);
        
        return result;
    }

    public Object getSelectedItem() {
        return combosel;
    }
}
