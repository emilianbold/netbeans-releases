/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.javacard.project.customizer;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.javacard.common.GuiUtils;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationGroupProvider;
import org.openide.util.HelpCtx;

public final class AppletProjectCustomizerRun extends javax.swing.JPanel implements ActionListener, ItemListener, ChangeListener, ValidationGroupProvider, ListDataListener {
    private Map<Object, String> selection2url;
    private static final int SELECTING_SERVLET = 1;
    private static final int SELECTING_PAGE = 2;
    private int selecting;

    public AppletProjectCustomizerRun(AppletProjectProperties props) {
        initComponents();
        GuiUtils.prepareContainer(this);
        platformAndDevicePanel21.setPlatformAndCard(props);
        platformAndDevicePanel21.setProjectKind(props.getProject().kind());
        servletComboBox.setModel(props.SCRIPTS);
        servletComboBox.setRenderer(new CRen());
        servletComboBox.getModel().addListDataListener(this);
        launchBrowserCheckBox.setModel(props.SEND_SCRIPT);
        selection2url = new HashMap<Object, String>();
        selecting = SELECTING_SERVLET;
        updateSelection();
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.RunPanel"); //NOI18N
    }

    @Override
    public void addNotify() {
        super.addNotify();
        intervalAdded(null);
    }

    public ValidationGroup getValidationGroup() {
        return platformAndDevicePanel21.getValidationGroup();
    }

    public void intervalAdded(ListDataEvent e) {
        boolean hasScripts = servletComboBox.getModel().getSize() > 0;
        launchBrowserCheckBox.setEnabled(hasScripts);
        if (!launchBrowserCheckBox.isEnabled()) {
            launchBrowserCheckBox.setSelected(false);
        }
    }

    public void intervalRemoved(ListDataEvent e) {
        intervalAdded(e);
    }

    public void contentsChanged(ListDataEvent e) {
        intervalAdded(e);
    }

    private static final class CRen extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String s = value == null ? "" : value.toString();
            if (s.startsWith("scripts/") || s.startsWith("scripts\\")) { //NOI18N
                s = s.substring("scripts/".length()); //NOI18N
            }
            return super.getListCellRendererComponent(list, s, index, isSelected, cellHasFocus);
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        servletComboBox = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        launchBrowserCheckBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        platformAndDevicePanel21 = new org.netbeans.modules.javacard.api.PlatformAndDevicePanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 0, 0, 0));

        servletComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servletComboBox.addItemListener(this);
        servletComboBox.addActionListener(this);

        launchBrowserCheckBox.setSelected(true);
        launchBrowserCheckBox.setText(org.openide.util.NbBundle.getMessage(AppletProjectCustomizerRun.class, "AppletProjectCustomizerRun.launchBrowserCheckBox.text")); // NOI18N
        launchBrowserCheckBox.addChangeListener(this);
        launchBrowserCheckBox.addActionListener(this);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(AppletProjectCustomizerRun.class, "AppletProjectCustomizerRun.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(launchBrowserCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(servletComboBox, 0, 415, Short.MAX_VALUE)))
                        .addGap(14, 14, 14))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(platformAndDevicePanel21, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(platformAndDevicePanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(launchBrowserCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(servletComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == servletComboBox) {
            AppletProjectCustomizerRun.this.servletComboBoxActionPerformed(evt);
        }
        else if (evt.getSource() == launchBrowserCheckBox) {
            AppletProjectCustomizerRun.this.launchBrowserCheckBoxActionPerformed(evt);
        }
    }

    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getSource() == servletComboBox) {
            AppletProjectCustomizerRun.this.servletComboBoxItemStateChanged(evt);
        }
    }

    public void stateChanged(javax.swing.event.ChangeEvent evt) {
        if (evt.getSource() == launchBrowserCheckBox) {
            AppletProjectCustomizerRun.this.launchBrowserCheckBoxStateChanged(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void servletComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_servletComboBoxActionPerformed

}//GEN-LAST:event_servletComboBoxActionPerformed

    private void launchBrowserCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchBrowserCheckBoxActionPerformed
        if (servletComboBox.getSelectedItem() == null && servletComboBox.getModel().getSize() > 0) {
            servletComboBox.setSelectedIndex(0);
        }
}//GEN-LAST:event_launchBrowserCheckBoxActionPerformed

    private void launchBrowserCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_launchBrowserCheckBoxStateChanged
        updateSelection();
    }//GEN-LAST:event_launchBrowserCheckBoxStateChanged

    private void servletComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_servletComboBoxItemStateChanged
        updateUrl();
    }//GEN-LAST:event_servletComboBoxItemStateChanged
    
    private void updateSelection() {
        boolean enabled = launchBrowserCheckBox.isSelected();
        
        servletComboBox.setEnabled(enabled);

        updateUrl();
    }
    
    private void updateUrl() {
        if (selection2url == null) {
            //superclass call
            return;
        }
        Object selectedObject = getSelectedServletOrPage();
        String url = selection2url.get(selectedObject);
        if (url == null) {
            if (selecting == SELECTING_PAGE) {
                url = (String) selectedObject;
            } else {
                //url = props.getURLPatternPart((String) selectedObject);
            }
            selection2url.put(selectedObject, url);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox launchBrowserCheckBox;
    private org.netbeans.modules.javacard.api.PlatformAndDevicePanel platformAndDevicePanel21;
    private javax.swing.JComboBox servletComboBox;
    // End of variables declaration//GEN-END:variables

    private Object getSelectedServletOrPage() {
        return servletComboBox.getSelectedItem();
    }
}
