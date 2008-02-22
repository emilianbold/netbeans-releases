/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.cnd.editor.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.modules.cnd.editor.filecreation.CndHandlableExtensions;
import org.netbeans.modules.cnd.editor.filecreation.ExtensionsSettings;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author sg155630
 */
public class CndOtherOptionsPanel extends javax.swing.JPanel implements ActionListener {

    private final Collection<? extends CndHandlableExtensions> res;
    
    public CndOtherOptionsPanel() {
        res = Lookup.getDefault().lookupResult(CndHandlableExtensions.class).allInstances();
//        es = new ExtensionsSettings[res.allInstances().size()];
//        int i = 0;
//        for (CndHandlableExtensions che : res.allInstances()) {
//            es[i++] = ExtensionsSettings.getInstance(che);
//        }
        setName("TAB_CndOtherOptionsTab"); // NOI18N (used as a pattern...)
        initComponents();
        initGeneratedComponents();
    }

    void applyChanges() {
        isChanged = false;
    }

    void update() {
    }

    // for OptionsPanelSupport
    private boolean isChanged = false;

    void cancel() {
        isChanged = false;
    }

    boolean isChanged() {
        return isChanged;
    }

    public void actionPerformed(ActionEvent e) {
        isChanged = true;
    }

    private void initGeneratedComponents() {
        int count = res.size();
        JLabel[] labels = new JLabel[count];
        JTextField[] textfields = new JTextField[count];
        JButton[] buttons = new JButton[count];

        int i = 0;
        for (Iterator<? extends CndHandlableExtensions> it = res.iterator(); it.hasNext();i++) {
            CndHandlableExtensions che = it.next();
            ExtensionsSettings es = ExtensionsSettings.getInstance(che);

            labels[i] = new JLabel();
            labels[i].setText(che.getDisplayName());
            textfields[i] = new JTextField();

            String list = "";
            for (Enumeration e = es.getExtensionList().extensions(); e != null &&  e.hasMoreElements();) {
                if (list.length() > 0) {
                    list += ", ";
                }
                list += (String) e.nextElement();
            }

            textfields[i].setText(list);
            textfields[i].setEditable(false);
            
            buttons[i] = new JButton();
            buttons[i].setText(NbBundle.getMessage(CndOtherOptionsPanel.class, "CndOtherOptionsPanel.Extensions.EditButton"));
            buttons[i].setEnabled(false);
         }

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        GroupLayout.SequentialGroup horizontalGroup = layout.createSequentialGroup();
        horizontalGroup.add(12, 12, 12);

        GroupLayout.ParallelGroup labelsGroup = layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING);
        for (int j = 0; j < count; j++) {
            labelsGroup.add(labels[j]);
        }

        horizontalGroup.add(labelsGroup);
        horizontalGroup.add(4, 4, 4);

        GroupLayout.ParallelGroup textfieldsGroup = layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false);
        for (int k = 0; k < count; k++) {
            textfieldsGroup.add(textfields[k], org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE);
        }
        horizontalGroup.add(textfieldsGroup);
        horizontalGroup.add(6, 6, 6);
        
        GroupLayout.ParallelGroup buttonsGroup = layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING);
        for (int n = 0; n < count; n++) {
            buttonsGroup.add(buttons[n]);
        }
        horizontalGroup.add(buttonsGroup);

        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(horizontalGroup)
                    .add(jLabelTitle_Extensions))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabelTitle_Extensions)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED);
        
        for (int j = 0; j < count; j++) {
            verticalGroup.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labels[j])
                        .add(textfields[j], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(buttons[j], org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));
            if (j != count - 1) {
                verticalGroup.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED);
            } else {
                verticalGroup.addContainerGap(220, Short.MAX_VALUE);
            }
                
        }

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(verticalGroup)
        );
        
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelTitle_Extensions = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        jLabelTitle_Extensions.setText(org.openide.util.NbBundle.getMessage(CndOtherOptionsPanel.class, "CndOtherOptionsPanel.jLabelTitle_Extensions.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(CndOtherOptionsPanel.class, "CndOtherOptionsPanel.jLabel3.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelTitle_Extensions)
                    .add(jLabel3))
                .addContainerGap(326, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabelTitle_Extensions)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addContainerGap(250, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelTitle_Extensions;
    // End of variables declaration//GEN-END:variables

}
