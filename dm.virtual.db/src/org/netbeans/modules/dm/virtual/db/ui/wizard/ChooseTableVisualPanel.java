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
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.openide.util.NbBundle;


public final class ChooseTableVisualPanel extends JPanel {
    
    private ChooseTablePanel owner;
    
    private SortedMap<String, Integer> tableDepth = new TreeMap<String, Integer>();
    private Map<String, javax.swing.text.Element> elementMap = new HashMap<String,
            javax.swing.text.Element>();
    
    public ChooseTableVisualPanel(ChooseTablePanel panel) {
        owner = panel;
        initComponents();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ChooseTableVisualPanel.class, "LBL_HTMLTable");
    }

    public boolean canAdvance() {
        Object obj = tableCombo.getSelectedItem();
        if (obj != null) {
            return true;
        }
        return false;
    }

    public int getTableDepth() {
        String tableName = null;
        if(null != tableCombo.getSelectedItem())
            tableName = (String) tableCombo.getSelectedItem();
            return tableDepth.get(tableName);
    }

    public DefaultTableModel getTableDetails() {
        DefaultTableModel model = new DefaultTableModel();
        model.setRowCount(0);
        model.setColumnCount(5);
        Element element = elementMap.get((String)tableCombo.getSelectedItem());
        ElementIterator it = new ElementIterator(element);
        Element elem = null;
        int i = 0;
        int count = 0;
        while ((elem = it.next()) != null) {
            if (elem.getName().equalsIgnoreCase("tr")) {
                if (i++ == 1) {
                    break;
                }
            } else if (elem.getName().equalsIgnoreCase("th") ||
                    elem.getName().equalsIgnoreCase("td")) {
                count++;
            }
        }
        for (i = 0; i < count; i++) {
            Object[] obj = new Object[5];
            obj[0] = i + 1;
            obj[1] = NbBundle.getMessage(ChooseTableVisualPanel.class, "LBL_Column") + String.valueOf(i + 1);
            obj[2] = 60;
            obj[3] = NbBundle.getMessage(ChooseTableVisualPanel.class, "LBL_varchar_displayname");
            obj[4] = new Boolean(true);
            model.addRow(obj);
        }
        return model;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        tableCombo = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        preview = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(450, 300));
        setMinimumSize(new java.awt.Dimension(100, 100));
        setPreferredSize(new java.awt.Dimension(400, 200));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(ChooseTableVisualPanel.class, "TITLE_ChooseTable"));

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(NbBundle.getMessage(ChooseTableVisualPanel.class, "LBL_SamplePreview")));
        jScrollPane1.setAutoscrolls(true);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jTable1);

        org.openide.awt.Mnemonics.setLocalizedText(preview, NbBundle.getMessage(ChooseTableVisualPanel.class, "LBL_Preview"));
        preview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .add(14, 14, 14)
                .add(tableCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 132, Short.MAX_VALUE)
                .add(preview))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE, false)
                    .add(jLabel1)
                    .add(tableCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(preview))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ChooseTableVisualPanel.class, "TITLE_ChooseTable"));
        preview.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ChooseTableVisualPanel.class, "LBL_Preview"));
    }// </editor-fold>//GEN-END:initComponents
    
    private void previewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewActionPerformed
        javax.swing.text.Element element = elementMap.get((String)tableCombo.getSelectedItem());
        final DefaultTableModel model = new DefaultTableModel();
        int i = 0;
        try {
            ElementIterator it = new ElementIterator(element);
            Document doc = element.getDocument();
            Vector rowVector = new Vector();
            javax.swing.text.Element elem = null;
            while((elem = it.next()) != null) {
                if(elem.getName().equalsIgnoreCase("tr")) {
                    if(i != 0) {
                        model.setColumnCount(rowVector.size());
                        model.addRow(rowVector);
                        rowVector = new Vector();
                    }
                    if(i++ == 10) {
                        break;
                    }
                } else if(elem.getName().equalsIgnoreCase("th") ||
                        elem.getName().equalsIgnoreCase("td")) {
                    String dt = doc.getText(elem.getStartOffset(),
                            (elem.getEndOffset() - elem.getStartOffset())).trim();
                    rowVector.add(dt);
                }
            }
            if(i < 10) {
                model.addRow(rowVector);
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(ChooseTableVisualPanel.class.getName()).log(Level.SEVERE, NbBundle.getMessage(ChooseTableVisualPanel.class, "LOG_ExceptionOccured"));
        }
        Vector headerVector = new Vector();
        for(int j = 0; j < model.getColumnCount(); j++) {
            headerVector.add(NbBundle.getMessage(ChooseTableVisualPanel.class, "LBL_Column") + String.valueOf(j+1));
        }
        model.setColumnIdentifiers(headerVector);
        Runnable run = new Runnable() {
            public void run() {
                jTable1.setModel(model);
            }
        };
        SwingUtilities.invokeLater(run);
    }//GEN-LAST:event_previewActionPerformed
    
    /*
     * This method reads the html file and
     * gets all the table data in comma seperated form.
     *
     */
    public void populateTablesList(String url) {
        InputStream in = null;
        File f = new File(url);
        try {
            if (f.exists()) {
                in = new FileInputStream(f);
            } else {
                in = new URL(url).openStream();
            }
        } catch (Exception ex) {
        //ignore
        }
        EditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
        try {
            kit.read(in, doc, 0);
        } catch (IOException ex) {
        //ignore
        } catch (BadLocationException ex) {
        //ignore
        }
        int tableCount = 1;
        int count = 1;
        ElementIterator it = new ElementIterator(doc);
        javax.swing.text.Element element = null;
        while ((element = it.next()) != null) {
            // read all table elements.
            if ("table".equalsIgnoreCase(element.getName())) {
                if (checkIfInnerMostTable(element)) {
                    tableDepth.put(NbBundle.getMessage(ChooseTableVisualPanel.class, "TITLE_TableNumComboBox") + String.valueOf(count), tableCount++);
                    elementMap.put(NbBundle.getMessage(ChooseTableVisualPanel.class, "TITLE_TableNumComboBox") + String.valueOf(count++), element);
                } else {
                    tableCount++;
                }
            }
        }
        tableCombo.removeAllItems();
        Set<String> tableNames = tableDepth.keySet();
        for(String tableName : tableNames) {
            tableCombo.addItem(tableName);
        }
        if(tableCombo.getItemCount() != 0) {
            tableCombo.setSelectedIndex(0);
        }
    }

    private boolean checkIfInnerMostTable(javax.swing.text.Element element) {
        ElementIterator it = new ElementIterator(element);
        javax.swing.text.Element elem = null;
        it.next();
        while ((elem = it.next()) != null) {
            if ("table".equalsIgnoreCase(elem.getName())) {
                return false;
            }
        }
        return true;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton preview;
    private javax.swing.JComboBox tableCombo;
    // End of variables declaration//GEN-END:variables
}

