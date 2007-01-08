/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Customizer for Enterprise Application packaging.
 */
public class CustomizerJarContent extends JPanel implements ArchiveCustomizerPanel, ListSelectionListener, HelpCtx.Provider {
    
    private Dialog dialog;
    private final AddFilter filterDlg = new AddFilter();
    private final DefaultListModel dlm = new DefaultListModel();
    private final EarProjectProperties earProperties;
    private final VisualPropertySupport vps;
    private final VisualArchiveIncludesSupport vws;
    private final ActionListener actionListener;
    
    public CustomizerJarContent(EarProjectProperties earProperties) {
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_A11YDesc"));
        
        this.earProperties = earProperties;
        vps = new VisualPropertySupport(earProperties);
        vws = new VisualArchiveIncludesSupport( earProperties.getProject(),
                (String) earProperties.get(EarProjectProperties.J2EE_PLATFORM),
                jTableAddContent,
                jButtonAddJar,
                jButtonAddLib,
                jButtonAddProject,
                jButtonRemove);
        
        jListExContent.setModel(dlm);
        
        // XXX correct these when the sematics are well defined
        //jButtonAddLib.setEnabled(false);
        
        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (event.getSource() == DialogDescriptor.OK_OPTION) {
                    dlm.addElement(filterDlg.getExpression());
                    setExcludeProperty();
                    closeDialog();
                }
            }
        };
        
        jListExContent.getSelectionModel().addListSelectionListener(this);
        initTableVisualProperties(jTableAddContent);
    }
    
    private void initTableVisualProperties(JTable table) {
        //table.setGridColor(jTableCpC.getBackground());
        table.setRowHeight(jTableAddContent.getRowHeight() + 4);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
   
        //#88174 - Need horizontal scrollbar for library names
        //ugly but I didn't find a better way how to do it
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(230);
        column.setWidth(230);
        column.setMinWidth(75);
        column = table.getColumnModel().getColumn(1);
        column.setMinWidth(135);
        column.setWidth(135);
        column.setMinWidth(28);
    }
    
    public void initValues() {
        vps.register(jTextFieldFileName, EarProjectProperties.JAR_NAME);
        vps.register(jCheckBoxCommpress, EarProjectProperties.JAR_COMPRESS);
        vps.register(vws, EarProjectProperties.JAR_CONTENT_ADDITIONAL);
        
        dlm.removeAllElements();
        String exclude = (String) earProperties.get(EarProjectProperties.BUILD_CLASSES_EXCLUDES);
        if (exclude != null) {
            StringTokenizer excludeTokenizer = new StringTokenizer(exclude, ","); //NOI18N
            while (excludeTokenizer.hasMoreElements()) {
                dlm.addElement(excludeTokenizer.nextToken());
            }
        } else {
            dlm.addElement("**/*.java"); //NOI18N
        }
        
        // Set the initial state of the buttons
        valueChanged(null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabelFileName = new javax.swing.JLabel();
        jTextFieldFileName = new javax.swing.JTextField();
        jCheckBoxCommpress = new javax.swing.JCheckBox();
        jLabelExContent = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListExContent = new javax.swing.JList();
        jButtonAddFilter = new javax.swing.JButton();
        jButtonRemoveFilter = new javax.swing.JButton();
        jLabelAddContent = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableAddContent = new javax.swing.JTable();
        jButtonAddJar = new javax.swing.JButton();
        jButtonAddLib = new javax.swing.JButton();
        jButtonAddProject = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabelFileName.setLabelFor(jTextFieldFileName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelFileName, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_FileName_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        jPanel1.add(jLabelFileName, gridBagConstraints);

        jTextFieldFileName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jTextFieldFileName, gridBagConstraints);
        jTextFieldFileName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_FileName_A11YDesc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxCommpress, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_Commpres_JCheckBox"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jCheckBoxCommpress, gridBagConstraints);
        jCheckBoxCommpress.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_Commpres_A11YDesc"));

        jLabelExContent.setLabelFor(jListExContent);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelExContent, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_Content_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 2, 0);
        add(jLabelExContent, gridBagConstraints);

        jScrollPane1.setViewportView(jListExContent);
        jListExContent.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_Content_A11YDesc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddFilter, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_AddFilter_JButton"));
        jButtonAddFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddFilterActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonAddFilter, gridBagConstraints);
        jButtonAddFilter.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_AddFilter_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveFilter, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_RemoveFilter_JButton"));
        jButtonRemoveFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveFilterActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(jButtonRemoveFilter, gridBagConstraints);
        jButtonRemoveFilter.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_Remove_A11YDesc"));

        jLabelAddContent.setLabelFor(jTableAddContent);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelAddContent, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_AddContent_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 2, 0);
        add(jLabelAddContent, gridBagConstraints);

        jTableAddContent.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(jTableAddContent);
        jTableAddContent.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "LBL_AACH_ProjectJarFiles_JLabel"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJar, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonAddJar, gridBagConstraints);
        jButtonAddJar.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_AddJar_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLib, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_AddLib_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonAddLib, gridBagConstraints);
        jButtonAddLib.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_AddLib_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddProject, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_AddProject_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonAddProject, gridBagConstraints);
        jButtonAddProject.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_AddProject_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemove, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_Remove_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonRemove, gridBagConstraints);
        jButtonRemove.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_AdditionalRemove_A11YDesc"));

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void jButtonRemoveFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveFilterActionPerformed
        Object[] items = jListExContent.getSelectedValues();
        for (int i = 0; i < items.length; i++) {
            dlm.removeElement(items[i]);
        }
        setExcludeProperty();
    }//GEN-LAST:event_jButtonRemoveFilterActionPerformed
    
    private void jButtonAddFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddFilterActionPerformed
        DialogDescriptor descriptor = new DialogDescriptor(filterDlg, NbBundle.getMessage(CustomizerJarContent.class, "LBL_AddFilter_Title"), true, actionListener); //NOI18N
        Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
        descriptor.setClosingOptions(closingOptions);
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
    }//GEN-LAST:event_jButtonAddFilterActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddFilter;
    private javax.swing.JButton jButtonAddJar;
    private javax.swing.JButton jButtonAddLib;
    private javax.swing.JButton jButtonAddProject;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JButton jButtonRemoveFilter;
    private javax.swing.JCheckBox jCheckBoxCommpress;
    private javax.swing.JLabel jLabelAddContent;
    private javax.swing.JLabel jLabelExContent;
    private javax.swing.JLabel jLabelFileName;
    private javax.swing.JList jListExContent;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableAddContent;
    private javax.swing.JTextField jTextFieldFileName;
    // End of variables declaration//GEN-END:variables
    
    private void closeDialog() {
        if (dialog != null) {
            dialog.dispose();
        }
    }
    
    public void valueChanged(ListSelectionEvent e) {
        jButtonRemoveFilter.setEnabled(!(jListExContent.isSelectionEmpty()));
    }
    
    private void setExcludeProperty() {
        String exclude = dlm.toString();
        exclude = exclude.replaceAll(" ", ""); //NOI18N
        exclude = exclude.substring(1, exclude.length() -1);
        earProperties.put(EarProjectProperties.BUILD_CLASSES_EXCLUDES, exclude);
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerJarContent.class);
    }
    
}
