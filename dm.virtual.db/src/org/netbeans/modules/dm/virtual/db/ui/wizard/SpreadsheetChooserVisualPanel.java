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
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import org.openide.util.NbBundle;


public final class SpreadsheetChooserVisualPanel extends JPanel {
    
    private SpreadsheetChooserPanel owner;
    private boolean canAdvance = false;
    
    private int columns = -1;
    
    private String file;
    
    class NumberVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            String inp = ((JTextField)input).getText().trim();
            try {
                int i = Integer.parseInt(inp);
                if(i > 100) {
                    return false;
                }
            } catch (NumberFormatException ex) {
                return false;
            }
            return true;
        }
    }
    
    /**
     * Creates new form SpreadsheetChooserVisualPanel
     */
    public SpreadsheetChooserVisualPanel(SpreadsheetChooserPanel panel) {
        owner = panel;
        initComponents();
        jButton1.setMnemonic('P');
        rowsToShow.setInputVerifier(new NumberVerifier());
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "TITLE_SpreadSheetChooserWizard");
    }
    
    public void populateSheets(String fileName) {
        file = fileName;
        String[] sheets = null;
        try {
            // parse xls file and get results.
            InputStream in = null;
            File f = new File(fileName);
            if(f.exists()) {
                in = new FileInputStream(f);
            } else {
                in = new URL(fileName).openStream();
            }
            Workbook spreadsheetData = Workbook.getWorkbook(in, getWorkbookSettings());
            sheets = spreadsheetData.getSheetNames();
            if(sheets.length != 0) {
                totalRows.setText(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_SpreadSheetLBLTotalRows")+ String.valueOf(
                        spreadsheetData.getSheet(0).getRows()));
            }
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setSheetNames(sheets);
    }
    
    private void setSheetNames(String[] sheetNames) {
        sheetCombo.removeAllItems();
        for(String sheetName : sheetNames) {
            sheetCombo.addItem(sheetName);
        }
        
        if(sheetCombo.getItemCount() != 0) {
            sheetCombo.setSelectedIndex(0);
            canAdvance = true;
        }
    }
    
    public String getSelectedSheetName() {
        if(sheetCombo.getItemCount() != 0) {
            return (String)sheetCombo.getSelectedItem();
        } 
        return "";        
    }
    
    private WorkbookSettings getWorkbookSettings() {
        WorkbookSettings settings = new WorkbookSettings();
        settings.setDrawingsDisabled(true);
        settings.setAutoFilterDisabled(true);
        settings.setSuppressWarnings(true);
        settings.setNamesDisabled(true);
        settings.setIgnoreBlanks(true);
        settings.setCellValidationDisabled(true);
        settings.setFormulaAdjust(false);
        settings.setPropertySets(false);
        return settings;
    }    
    
    private DefaultTableModel getDataForSheet(String sheetName) {
        DefaultTableModel model = new DefaultTableModel();        
        int colCount = -1;
        int colLength = 0;
        try {
            FileInputStream in = new FileInputStream(new File(file));
            int max = 10;
            try {
                max = Integer.parseInt(rowsToShow.getText().trim());
            } catch (NumberFormatException ex) {
                max = 10;
                rowsToShow.setText("10");
            }
            Workbook spreadSheetData = Workbook.getWorkbook(in, getWorkbookSettings());
            Sheet sheet = spreadSheetData.getSheet(sheetName);
            boolean isNullCell = false;
            for(int i = 0; i < sheet.getRows(); i++) {
                if(i == max) {
                    break;
                }
                Cell[] cells = sheet.getRow(i);
                colLength = colLength<cells.length?cells.length:colLength;
                if(colLength < colCount){
                    colLength = colCount;
                    isNullCell = true;
                }
                model.setColumnCount(colLength);
                Vector rowVector = new Vector();
                if (!isNullCell) {
                    for (int j = 0; j < cells.length; j++) {
                        rowVector.add(cells[j].getContents());
                    }
                } else {
                    for (int j = 0; j < cells.length; j++) {
                        rowVector.add(null);
                    }
                    isNullCell = false;
                }
                model.addRow(rowVector);
                if(colCount == -1) {
                    colCount = colLength;
                }
            }
            spreadSheetData.close();
            if(in != null) {
                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // set the column headers.
        if(colCount != -1) {
            columns = colLength;
            Vector headerVector = new Vector();
            for(int i = 0; i < colLength; i++) {
                headerVector.add("Column " + String.valueOf(i+1));
            }
            model.setColumnIdentifiers(headerVector);
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

        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        totalRows = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        rowsToShow = new javax.swing.JTextField();
        error = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        sheetCombo = new javax.swing.JComboBox();

        setMaximumSize(new java.awt.Dimension(500, 500));
        setMinimumSize(new java.awt.Dimension(100, 100));
        setPreferredSize(new java.awt.Dimension(415, 220));

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_Preview"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_Preview")));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jTable1);
        jTable1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_Preview"));
        jTable1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_Preview"));

        org.openide.awt.Mnemonics.setLocalizedText(totalRows, NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_SpreadSheetLBLTotalRows"));

        jLabel3.setDisplayedMnemonic('R');
        jLabel3.setLabelFor(rowsToShow);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_SpreadSheetRowsToShowLbl"));

        rowsToShow.setText("10");

        error.setForeground(new java.awt.Color(255, 51, 102));
        error.setLabelFor(jTable1);

        jLabel1.setDisplayedMnemonic('S');
        jLabel1.setLabelFor(sheetCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_SpreadSheetSheetLbl"));

        sheetCombo.setToolTipText(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "TOOLTIP_SpreadSheetCombo"));
        sheetCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sheetComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, error, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(sheetCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 136, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                                .add(totalRows)
                                .add(79, 79, 79)
                                .add(jLabel3)))
                        .add(18, 18, 18)
                        .add(rowsToShow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jButton1))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 412, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(sheetCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(totalRows)
                    .add(jLabel3)
                    .add(rowsToShow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(error, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 135, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(91, 91, 91))
        );

        jButton1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_Preview"));
        totalRows.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_SpreadSheetLBLTotalRows"));
        jLabel3.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_SpreadSheetRowsToShowLbl"));
        jLabel3.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_SpreadSheetRowsToShowLbl"));
        rowsToShow.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "TOOLTIP_SpreadSheetRowsToShow"));
        rowsToShow.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "TOOLTIP_SpreadSheetRowsToShow"));
        jLabel1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_SpreadSheetSheetLbl"));
        jLabel1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_SpreadSheetSheetLbl"));
        sheetCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "ACSD_SpreadSheetCombo"));
        sheetCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "ACSD_SpreadSheetCombo"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 222, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void sheetComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sheetComboActionPerformed
        try {
            FileInputStream in = new FileInputStream(new File(file));
            Workbook spreadsheetData = Workbook.getWorkbook(in);
            totalRows.setText(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_SpreadSheetLBLTotalRows")+ String.valueOf(
                    spreadsheetData.getSheet((String) sheetCombo.getSelectedItem()).getRows()));   
            error.setText("");
        } catch (Exception ex) {
            error.setText(NbBundle.getMessage(SpreadsheetChooserVisualPanel.class, "LBL_SpreadSheetError"));
        }       
        Runnable run = new Runnable(){
            public void run() {
                jTable1.setModel(new DefaultTableModel());
            }
        };
        SwingUtilities.invokeLater(run);
    }//GEN-LAST:event_sheetComboActionPerformed
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        final DefaultTableModel model = getDataForSheet((String)sheetCombo.getSelectedItem());
        Runnable run = new Runnable() {
            public void run() {
                jTable1.setModel(model);
            }
        };
        SwingUtilities.invokeLater(run);
        error.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    public boolean canAdvance() {
        return (sheetCombo.getItemCount() != 0 && sheetCombo.getSelectedIndex() != -1);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel error;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField rowsToShow;
    private javax.swing.JComboBox sheetCombo;
    private javax.swing.JLabel totalRows;
    // End of variables declaration//GEN-END:variables
    
}

