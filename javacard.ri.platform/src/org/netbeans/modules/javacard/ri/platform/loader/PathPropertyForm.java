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
package org.netbeans.modules.javacard.ri.platform.loader;

import java.awt.event.ActionListener;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.netbeans.modules.javacard.common.JarOrDirectoryFilter;

/**
 * Panel which reads and writes a : delimited path from a properties file.
 *
 * @author Tim Boudreau
 */
public class PathPropertyForm extends javax.swing.JPanel implements ListSelectionListener, ActionListener {
    private final DefaultListModel mdl = new DefaultListModel();
    private final String propName;
    private final Properties properties;
    private char separator = File.pathSeparatorChar;
    private String separatorString = new String(new char[] { separator });
    /** Creates new form PathPropertyForm */
    public PathPropertyForm(Properties properties, String propName) {
        this.properties = properties;
        this.propName = propName;
        initComponents();
        Mnemonics.setLocalizedText(addJarButton, addJarButton.getText());
        Mnemonics.setLocalizedText(removeButton, removeButton.getText());
        Mnemonics.setLocalizedText(upButton, upButton.getText());
        Mnemonics.setLocalizedText(downButton, downButton.getText());
        list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        list.setModel(mdl);
        initModel();
        list.getSelectionModel().addListSelectionListener(this);
        if (mdl.getSize() > 0) {
            list.setSelectedIndex(0);
        }
    }

    boolean initializing;
    private void initModel() {
        initializing = true;
        String value = properties.getProperty(propName);
        if (value != null && !"".equals(value.trim())) {
            String[] items = value.split(separatorString);
            for (String item : items) {
                mdl.addElement(item);
            }
        } else {
            mdl.clear();
        }
        valueChanged(null);
        initializing = false;
    }
    
    private void updateModel() {
        Object toSelect = list.getSelectedValue();
        mdl.clear();
        initModel();
        int max = mdl.size();
        int ix = -1;
        if (toSelect != null) {
            for (int i=0; i < max; i++) {
                Object test = mdl.getElementAt(i);
                if (toSelect.equals(test)) {
                    ix = i;
                    break;
                }
            }
        }
        if (ix == -1 && mdl.size() != 0) {
            list.setSelectedIndex(0);
        } else if (ix != -1) {
            list.setSelectedIndex(ix);
        }
        list.requestFocusInWindow();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        addJarButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();

        list.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(list);

        addJarButton.setText(org.openide.util.NbBundle.getMessage(PathPropertyForm.class, "PathPropertyForm.addJarButton.text")); // NOI18N
        addJarButton.addActionListener(this);

        removeButton.setText(org.openide.util.NbBundle.getMessage(PathPropertyForm.class, "PathPropertyForm.removeButton.text")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(this);

        upButton.setText(org.openide.util.NbBundle.getMessage(PathPropertyForm.class, "PathPropertyForm.upButton.text")); // NOI18N
        upButton.addActionListener(this);

        downButton.setText(org.openide.util.NbBundle.getMessage(PathPropertyForm.class, "PathPropertyForm.downButton.text")); // NOI18N
        downButton.addActionListener(this);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addJarButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                    .addComponent(upButton, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                    .addComponent(downButton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addJarButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addGap(18, 18, 18)
                        .addComponent(upButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downButton)))
                .addContainerGap())
        );
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == addJarButton) {
            PathPropertyForm.this.addJarButtonActionPerformed(evt);
        }
        else if (evt.getSource() == removeButton) {
            PathPropertyForm.this.removeButtonActionPerformed(evt);
        }
        else if (evt.getSource() == upButton) {
            PathPropertyForm.this.upButtonActionPerformed(evt);
        }
        else if (evt.getSource() == downButton) {
            PathPropertyForm.this.downButtonActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void addJarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addJarButtonActionPerformed
        JarOrDirectoryFilter filter = new JarOrDirectoryFilter();
        String title = NbBundle.getMessage (PathPropertyForm.class,
                "TTL_ADD_JAR_OR_FOLDER"); //NOI18N
        String okButtonText = NbBundle.getMessage (PathPropertyForm.class,
                "ACTION_ADD"); //NOI18N
        File[] files = null;
        if ((files = new FileChooserBuilder(PathPropertyForm.class).setFileFilter(filter).setTitle(title).setApproveText(okButtonText).showMultiOpenDialog()) != null) {
            String prop = properties.getProperty(propName);
            StringBuilder sb = prop == null ? new StringBuilder() : new StringBuilder(prop.trim());
            String path = null;
            for (File file : files) {
                path = file.getAbsolutePath();
                mdl.addElement(path);
                if (sb.length() > 0) {
                    sb.append (separator);
                }
                sb.append (path);
            }
            properties.setProperty(propName, sb.toString());
            updateModel();
            if (path != null) { //array was empty
                list.setSelectedValue(path, true);
            }
        }
    }//GEN-LAST:event_addJarButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Set<Object> toRemove = new HashSet<Object>(Arrays.asList(list.getSelectedValues()));
        String[] items = properties.getProperty (propName).split(separatorString);
        StringBuilder sb = new StringBuilder();
        String toSelect = null;
        for (String item : items) {
            if (!toRemove.contains(item)) {
                if (sb.length() > 0) {
                    sb.append (separator);
                }
                sb.append (item);
            } else if (toSelect == null) {
                toSelect = item;
            }
        }
        properties.setProperty (propName, sb.toString());
        updateModel();
        if (toSelect != null) {
            list.setSelectedValue(toSelect, true);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        moveBy (-1);
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        moveBy (1);
    }//GEN-LAST:event_downButtonActionPerformed

    void moveBy (int qty) {
        Object o = list.getSelectedValue();
        int ix = list.getSelectedIndex();
        mdl.removeElementAt(ix);
        int newIndex = Math.max (0, Math.min (ix + qty, mdl.size()));
        mdl.add(newIndex, o);
        updateFromModel();
        list.setSelectedValue(o, true);
    }

    private void updateFromModel() {
        StringBuilder sb = new StringBuilder();
        for (Object o : mdl.toArray()) {
            if (sb.length() > 0) {
                sb.append (separator);
            }
            sb.append (o);
        }
        properties.setProperty(propName, sb.toString());
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addJarButton;
    private javax.swing.JButton downButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList list;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    public void valueChanged(ListSelectionEvent e) {
        int ix = list.getSelectedIndex();
        removeButton.setEnabled(ix >= 0);
        upButton.setEnabled(ix > 0);
        downButton.setEnabled(ix >= 0 && ix < mdl.size() - 1);
    }

}
