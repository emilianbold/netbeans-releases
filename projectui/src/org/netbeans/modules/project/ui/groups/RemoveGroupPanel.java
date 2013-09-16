/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.project.ui.groups;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

/**
 * Panel permitting user to remove project group(s).
 * @author mkozeny
 */
public class RemoveGroupPanel extends javax.swing.JPanel {

    public static final String PROP_READY = "ready";
    
    private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    
    /**
     * Creates new form RemoveGroupPanel
     */
    public RemoveGroupPanel() {
        initComponents();
        DefaultListModel model = new DefaultListModel ();
        for (final Group g : Group.allGroups()) {
            model.addElement ("-" + g.getName());
        }
        groupList.setModel (model);
        groupList.setCellRenderer (new CheckBoxRenderrer ());
        groupList.addKeyListener (new KeyAdapter () {

            @Override
            public void keyTyped (KeyEvent e) {
                if (e.getKeyChar () == KeyEvent.VK_SPACE) {
                    int i = groupList.getSelectedIndex ();
                    if (i < 0) return;
                    String name = (String) groupList.getModel ().getElementAt (i);
                    if (name.charAt (0) == '+')
                        ((DefaultListModel) groupList.getModel ()).set (i, "-" + name.substring (1));
                    else
                        ((DefaultListModel) groupList.getModel ()).set (i, "+" + name.substring (1));
                    notifyListeners(this, "selection", null, null);
                }
            }
        });
        groupList.addMouseListener (new MouseAdapter () {

            @Override
            public void mouseClicked (MouseEvent e) {
                int i = groupList.getSelectedIndex ();
                if (i < 0) return;
                String name = (String) groupList.getModel ().getElementAt (i);
                if (name.charAt (0) == '+')
                    ((DefaultListModel) groupList.getModel ()).set (i, "-" + name.substring (1));
                else
                    ((DefaultListModel) groupList.getModel ()).set (i, "+" + name.substring (1));
                notifyListeners(this, "selection", null, null);
            }
        });
    }
    
    public boolean isReady() {
        ListModel model = groupList.getModel ();
        if(model.getSize() != Group.allGroups().size()) {
            return false;
        }
        return true;
    }
    
    public boolean isAtLeastOneGroupSelected() {
        ListModel model = groupList.getModel ();
        for (int i = 0; i < model.getSize (); i++) {
            String n = (String) model.getElementAt (i);
            if (n.charAt (0) == '+') {
                return true;
            }
        }
        return false;
    }
    
    void remove() {
        ListModel model = groupList.getModel ();
        for (int i = 0; i < model.getSize (); i++) {
            String n = (String) model.getElementAt (i);
            if (n.charAt (0) == '+') {
                for (final Group g : Group.allGroups()) {
                    if(n.substring(1).equals(g.getName())) {
                        g.destroy();
                    }
                }
            }
        }
    }
    
    public void addChangeListener(PropertyChangeListener newListener) {
        listeners.add(newListener);
    }
    
    private void notifyListeners(Object object, String property, String oldValue, String newValue) {
        for (PropertyChangeListener listenerIter : listeners) {
          listenerIter.propertyChange(new PropertyChangeEvent(object, property, oldValue, newValue));
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        selectionLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        groupList = new javax.swing.JList();
        jSeparator1 = new javax.swing.JSeparator();

        setMinimumSize(new java.awt.Dimension(320, 220));
        setPreferredSize(new java.awt.Dimension(320, 220));
        setLayout(new java.awt.GridBagLayout());

        selectionLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(selectionLabel, org.openide.util.NbBundle.getMessage(RemoveGroupPanel.class, "RemoveGroupPanel.selectionLabel.text")); // NOI18N
        selectionLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(selectionLabel, gridBagConstraints);

        jScrollPane1.setViewportView(groupList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(jScrollPane1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 372;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 6, 0, 12);
        add(jSeparator1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList groupList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel selectionLabel;
    // End of variables declaration//GEN-END:variables
}
