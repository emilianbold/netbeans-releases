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
package org.netbeans.modules.java.hints.bugs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.util.Utilities;

/**
 *
 * @author sdedic
 */
public class BroadCatchCustomizer extends javax.swing.JPanel 
    implements ActionListener, DocumentListener, ListSelectionListener {
    private Preferences prefs;
    private Color textBkColor;
    
    /**
     * Creates new form BroadCatchCustomizer
     */
    public BroadCatchCustomizer(Preferences prefs) {
        this.prefs = prefs;
        initComponents();
        lstUmbrellas.setModel(new DefaultListModel());
        
        cbCommonTypes.addActionListener(this);
        cbSuppressUmbrellas.addActionListener(this);
        
        tfNewUmbrella.getDocument().addDocumentListener(this);
        
        lstUmbrellas.addListSelectionListener(this);
        
        btnAddUmbrella.addActionListener(this);
        btnRemoveUbmbrella.addActionListener(this);
        
        initList(lstUmbrellas, 
            prefs.get(BroadCatchBlock.OPTION_UMBRELLA_LIST, BroadCatchBlock.DEFAULT_UMBRELLA_LIST));

        cbSuppressUmbrellas.setSelected(
            prefs.getBoolean(BroadCatchBlock.OPTION_EXCLUDE_UMBRELLA, BroadCatchBlock.DEFAULT_EXCLUDE_UMBRELLA));
        cbCommonTypes.setSelected(
            !prefs.getBoolean(BroadCatchBlock.OPTION_EXCLUDE_COMMON, BroadCatchBlock.DEFAULT_EXCLUDE_COMMON));
        
        enableUmbrella();
    }
    
    private void initList(JList l, String val) {
        StringTokenizer tukac = new StringTokenizer(val, ", ");
        DefaultListModel m = (DefaultListModel)l.getModel();
        while (tukac.hasMoreTokens()) {
            String s = tukac.nextToken();
            if (s.isEmpty()) {
                continue;
            }
            m.addElement(s);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        JList lst = (JList)lse.getSource();
        boolean sel = lst.isEnabled() && !lst.isSelectionEmpty();
        btnRemoveUbmbrella.setEnabled(sel);
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        Document d = de.getDocument();
        updateControls(d, checkIdentifier(d));
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        Document d = de.getDocument();
        updateControls(d, checkIdentifier(d));
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
    }
    
    private void removeSelected(JList list, String prefKey) {
        DefaultListModel m = (DefaultListModel)list.getModel();
        while (!list.isSelectionEmpty()) {
            m.remove(list.getSelectionModel().getLeadSelectionIndex());
        }
        updatePreference(list, prefKey);
    }
    
    private void addNewType(String t, JList list, String prefKey) {
        ((DefaultListModel)list.getModel()).addElement(t);
        list.setSelectedIndex(list.getModel().getSize() - 1);
        tfNewUmbrella.setText(""); // NOI18N
        updatePreference(list, prefKey);
    }
    
    private void updatePreference(JList list, String prefKey) {
        StringBuilder sb = new StringBuilder(35);
        for (Enumeration en = ((DefaultListModel)list.getModel()).elements(); en.hasMoreElements(); ) {
            String s = (String)en.nextElement();
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(s);
        }
        prefs.put(prefKey, sb.toString());
    }
    
    private void updateControls(JTextField tf, JButton addButton, int state) {
        if (textBkColor == null) {
            textBkColor = tf.getBackground();
        }
        switch (state) {
            default:
                tf.setBackground(Color.pink);
                addButton.setEnabled(false);
                break;
            case -1:
                tf.setBackground(textBkColor);
                addButton.setEnabled(false);
                break;
            case 0:
                tf.setBackground(textBkColor);
                addButton.setEnabled(true);
                break;
        }
    }
    
    private void updateControls(Document d, int state) {
        updateControls(tfNewUmbrella, btnAddUmbrella, state);
    }
    
    private int checkIdentifier(Document d) {
        String text;
        
        try {
            text = d.getText(0, d.getLength());
        } catch (BadLocationException ex) {
            return -1;
        }
        
        text = text.trim();
        if (text.isEmpty()) {
            return -1;
        }
        String[] parts = text.split("\\.", -1);
        for (String s : parts) {
            if (s.isEmpty() || !Utilities.isJavaIdentifier(s)) {
                return 1;
            }
        }
        return 0;
        
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object src = ae.getSource();
        if (src == cbCommonTypes) {
            prefs.putBoolean(BroadCatchBlock.OPTION_EXCLUDE_COMMON, !cbCommonTypes.isSelected());
        } else if (src == cbSuppressUmbrellas) {
            prefs.putBoolean(BroadCatchBlock.OPTION_EXCLUDE_UMBRELLA, cbSuppressUmbrellas.isSelected());
            enableUmbrella();
        } else if (src == btnRemoveUbmbrella) {
            removeSelected(lstUmbrellas, BroadCatchBlock.OPTION_UMBRELLA_LIST);
        } else if (src == btnAddUmbrella) {
            addNewType(tfNewUmbrella.getText(), lstUmbrellas, BroadCatchBlock.OPTION_UMBRELLA_LIST);
        }
    }
    
    private void enableUmbrella() {
        boolean enable = cbSuppressUmbrellas.isEnabled() && cbSuppressUmbrellas.isSelected();
        btnAddUmbrella.setEnabled(enable);
        btnRemoveUbmbrella.setEnabled(enable);
        scrUmbrellaTypes.setEnabled(enable);
        lstUmbrellas.setEnabled(enable);
        tfNewUmbrella.setEnabled(enable);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbCommonTypes = new javax.swing.JCheckBox();
        cbSuppressUmbrellas = new javax.swing.JCheckBox();
        lblUmbrellaList = new javax.swing.JLabel();
        scrUmbrellaTypes = new javax.swing.JScrollPane();
        lstUmbrellas = new javax.swing.JList();
        btnRemoveUbmbrella = new javax.swing.JButton();
        tfNewUmbrella = new javax.swing.JTextField();
        btnAddUmbrella = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(cbCommonTypes, org.openide.util.NbBundle.getMessage(BroadCatchCustomizer.class, "BroadCatchCustomizer.cbCommonTypes.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbSuppressUmbrellas, org.openide.util.NbBundle.getMessage(BroadCatchCustomizer.class, "BroadCatchCustomizer.cbSuppressUmbrellas.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblUmbrellaList, org.openide.util.NbBundle.getMessage(BroadCatchCustomizer.class, "BroadCatchCustomizer.lblUmbrellaList.text")); // NOI18N

        scrUmbrellaTypes.setViewportView(lstUmbrellas);

        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveUbmbrella, org.openide.util.NbBundle.getMessage(BroadCatchCustomizer.class, "BroadCatchCustomizer.btnRemoveUbmbrella.text")); // NOI18N
        btnRemoveUbmbrella.setEnabled(false);

        tfNewUmbrella.setText(org.openide.util.NbBundle.getMessage(BroadCatchCustomizer.class, "BroadCatchCustomizer.tfNewUmbrella.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnAddUmbrella, org.openide.util.NbBundle.getMessage(BroadCatchCustomizer.class, "BroadCatchCustomizer.btnAddUmbrella.text")); // NOI18N
        btnAddUmbrella.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbSuppressUmbrellas)
                            .addComponent(cbCommonTypes))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfNewUmbrella, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(scrUmbrellaTypes, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblUmbrellaList)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAddUmbrella, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRemoveUbmbrella)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(cbCommonTypes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSuppressUmbrellas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblUmbrellaList)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrUmbrellaTypes, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoveUbmbrella))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfNewUmbrella, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddUmbrella)))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddUmbrella;
    private javax.swing.JButton btnRemoveUbmbrella;
    private javax.swing.JCheckBox cbCommonTypes;
    private javax.swing.JCheckBox cbSuppressUmbrellas;
    private javax.swing.JLabel lblUmbrellaList;
    private javax.swing.JList lstUmbrellas;
    private javax.swing.JScrollPane scrUmbrellaTypes;
    private javax.swing.JTextField tfNewUmbrella;
    // End of variables declaration//GEN-END:variables
}
