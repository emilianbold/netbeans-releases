package org.netbeans.modules.mashup.tables.wizard;

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
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


public final class SpreadsheetChooserVisualPanel extends JPanel {
    
    private SpreadsheetChooserPanel owner;
    private static transient final Logger mLogger = Logger.getLogger(SpreadsheetChooserVisualPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
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
        rowsToShow.setInputVerifier(new NumberVerifier());
    }
    
    @Override
    public String getName() {
        String nbBundle1 = mLoc.t("BUND295: Choose a Sheet");
        return nbBundle1.substring(15);
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
                totalRows.setText("Total Rows: " + String.valueOf(
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
            for(int i = 0; i < sheet.getRows(); i++) {
                if(i == max) {
                    break;
                }
                Cell[] cells = sheet.getRow(i);
                model.setColumnCount(cells.length);
                Vector rowVector = new Vector();
                for(int j = 0; j < cells.length; j++) {
                    rowVector.add(cells[j].getContents());
                }
                model.addRow(rowVector);
                if(colCount == -1) {
                    colCount = cells.length;
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
            columns = colCount;
            Vector headerVector = new Vector();
            for(int i = 0; i < colCount; i++) {
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, "Preview");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(" Preview"));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jTable1);

        org.openide.awt.Mnemonics.setLocalizedText(totalRows, "Total Rows:");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "Rows to Show:");

        rowsToShow.setText("10");

        error.setForeground(new java.awt.Color(255, 51, 102));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Sheet");

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
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 88, Short.MAX_VALUE)
                        .add(jButton1))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 412, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            totalRows.setText("Total Rows: " + String.valueOf(
                    spreadsheetData.getSheet((String) sheetCombo.getSelectedItem()).getRows()));   
            error.setText("");
        } catch (Exception ex) {
            error.setText("Unable to get Sheet data.");
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

