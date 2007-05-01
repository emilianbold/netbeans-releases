package org.netbeans.modules.mashup.tables.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public final class FileSelectionVisualPanel extends JPanel {
    
    private File selectedFile;
    
    private boolean canAdvance = false;
    
    private FileSelectionPanel owner;
    
    /**
     * Creates new form FileSelectionVisualPanel
     */
    public FileSelectionVisualPanel(FileSelectionPanel panel) {
        owner = panel;
        initComponents();
        disableCancelButton(fileChooser);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = (JFileChooser)e.getSource();
                selectedFile = fileChooser.getSelectedFile();
                addToModel(selectedFile.getAbsolutePath());                
            }
        });
        url.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e){
                if(url.getText().trim().length() == 0) {
                    addEntry.setEnabled(false);
                } else {
                    addEntry.setEnabled(true);
                }
            }
        });
        removeButton.setEnabled(false);
        addEntry.setEnabled(false);
        //jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setTableColumnSize();
    }
    
    public String getName() {
        return "Choose Data source";
    }
    
    public boolean canAdvance() {
        return canAdvance;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        fileChooser = new javax.swing.JFileChooser();
        panel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        url = new javax.swing.JTextField();
        addEntry = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        removeButton = new javax.swing.JButton();
        error = new javax.swing.JLabel();

        fileChooser.setApproveButtonText("Add");
        fileChooser.setApproveButtonToolTipText("Add Table Source");
        fileChooser.setDialogType(javax.swing.JFileChooser.CUSTOM_DIALOG);
        fileChooser.setToolTipText("Choose a file");
        fileChooser.setAutoscrolls(true);
        fileChooser.setBorder(javax.swing.BorderFactory.createTitledBorder("Choose a File"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "URL");

        org.openide.awt.Mnemonics.setLocalizedText(addEntry, "Add");
        addEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEntryActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout panel1Layout = new org.jdesktop.layout.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panel1Layout.createSequentialGroup()
                .add(jLabel3)
                .add(50, 50, 50)
                .add(url, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addEntry)
                .addContainerGap())
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(url, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(addEntry)
                .add(jLabel3))
        );

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Table Source"));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "#", "Table Source"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        jScrollPane1.setViewportView(jTable1);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, "Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        error.setForeground(new java.awt.Color(255, 0, 51));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(fileChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(error, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 402, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(panel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileChooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 254, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(error, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(removeButton)))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        removeFromModel();
    }//GEN-LAST:event_removeButtonActionPerformed
    
    private void addEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEntryActionPerformed
        String urlAdd = url.getText().trim();
        addToModel(urlAdd);
    }//GEN-LAST:event_addEntryActionPerformed
    
    public void setErrorText(String string) {
        error.setText(string);
    }
    
    private void disableCancelButton(Component c) {
        if (c instanceof Container) {
            Component[] comps = ((Container) c).getComponents();
            for (int i = 0; i < comps.length; i++) {
                if (comps[i] instanceof JButton) {
                    if ("Cancel".equalsIgnoreCase(((JButton) comps[i]).getText())) {
                        ((Container) c).remove(comps[i]);
                        break;
                    }
                } else {
                    disableCancelButton(comps[i]);
                }
            }
        }
    }
    
    public DefaultTableModel getSelectedTables() {
        return (DefaultTableModel) jTable1.getModel();
    }
    
    private void addToModel(String urlAdd) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        Object[] obj = new Object[2];
        obj[0] = model.getRowCount() + 1;
        obj[1] = urlAdd;
        model.addRow(obj);
        setTableModel(model);
        canAdvance = true;
        removeButton.setEnabled(true);
        owner.fireChangeEvent();
        setErrorText("");
        url.setText("");
    }
    
    private void removeFromModel() {
        final DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        int[] rows = jTable1.getSelectedRows();
        for(int row : rows) {
            model.removeRow(row);
        }
        
        // renumber all the rows.
        for(int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0);
        }
        
        setTableModel(model);
        if (model.getRowCount() == 0) {
            canAdvance = false;
            removeButton.setEnabled(false);
            setErrorText("No table available for processing.");
        }
        owner.fireChangeEvent();
    }
    
    private void setTableModel(final DefaultTableModel model) {
        Runnable run = new Runnable(){
            public void run() {
                Object[] obj = new Object[2];
                obj[0] = "#";
                obj[1] = "Table Source";
                model.setColumnIdentifiers(obj);
                setTableColumnSize();
                jTable1.setModel(model);
            }
        };
        SwingUtilities.invokeLater(run);
    }
    
    private void setTableColumnSize() {
        TableColumnModel tableColumnModel = jTable1.getColumnModel();
        tableColumnModel.getColumn(0).setMinWidth(5);
        tableColumnModel.getColumn(0).setMaxWidth(30);
        tableColumnModel.getColumn(0).setPreferredWidth(30);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addEntry;
    private javax.swing.JLabel error;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel panel1;
    private javax.swing.JButton removeButton;
    private javax.swing.JTextField url;
    // End of variables declaration//GEN-END:variables
    
}

