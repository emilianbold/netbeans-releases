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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.DefaultComboBoxModel;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.db.util.DriverListUtil;

public class AddDriverDialog extends javax.swing.JPanel {
    
    private DefaultListModel dlm;
    private List drvs;
    private boolean customizer;
    private ProgressHandle progressHandle;
    private JComponent progressComponent;
    
    private static final String BUNDLE = "org.netbeans.modules.db.resources.Bundle"; //NOI18N

    /** Creates new form AddDriverDialog1 */
    public AddDriverDialog() {
        customizer = false;
        initComponents();
        // hack to force the progressContainerPanel honor its preferred height
        // without it, the preferred height is sometimes ignored during resize
        // progressContainerPanel.add(Box.createVerticalStrut(progressContainerPanel.getPreferredSize().height), BorderLayout.EAST);
        initAccessibility();
        dlm = (DefaultListModel) drvList.getModel();
        drvs = new LinkedList();
    }
    
    public AddDriverDialog(JDBCDriver drv) {
        this();
        customizer = true;
        
        String fileName;
        URL[] urls = drv.getURLs();
        for (int i = 0; i < urls.length; i++) {
            FileObject fo = URLMapper.findFileObject(urls[i]);
            if (fo == null) {
                try {
                    fileName = new File(new URI(urls[i].toExternalForm())).getAbsolutePath();
                } catch (URISyntaxException e) {
                    ErrorManager.getDefault().notify(e);
                    fileName = null;
                }
            } else {
                fileName = FileUtil.toFile(fo).getAbsolutePath();
            }
            if (fileName != null) {
                dlm.addElement(fileName);
                drvs.add(urls[i]);
            }
        }
        drvClassComboBox.addItem(drv.getClassName());
        drvClassComboBox.setSelectedItem(drv.getClassName());
        nameTextField.setText(drv.getDisplayName());
    }
    
    private void initAccessibility() {
        ResourceBundle b = NbBundle.getBundle(BUNDLE);
        this.getAccessibleContext().setAccessibleDescription(b.getString("ACS_AddDriverDialogA11yDesc")); //NOI18N
        drvListLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_AddDriverDriverFileA11yDesc")); //NOI18N
        drvList.getAccessibleContext().setAccessibleName(b.getString("ACS_AddDriverDriverFileListA11yName")); //NOI18N
        drvClassLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_AddDriverDriverDriverClassA11yDesc")); //NOI18N
        drvClassComboBox.getAccessibleContext().setAccessibleName(b.getString("ACS_AddDriverDriverDriverClassComboBoxA11yName")); //NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_AddDriverDriverNameA11yDesc")); //NOI18N
        nameTextField.getAccessibleContext().setAccessibleName(b.getString("ACS_AddDriverDriverNameTextFieldA11yName")); //NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(b.getString("ACS_AddDriverAddButtonA11yDesc")); //NOI18N
        findButton.getAccessibleContext().setAccessibleDescription(b.getString("ACS_AddDriverRemoveButtonA11yDesc")); //NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(b.getString("ACS_AddDriverFindButtonA11yDesc")); //NOI18N
        progressContainerPanel.getAccessibleContext().setAccessibleName(b.getString("ACS_AddDriverProgressBarA11yName")); //NOI18N
        progressContainerPanel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_AddDriverProgressBarA11yDesc")); //NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        drvListLabel = new javax.swing.JLabel();
        drvListScrollPane = new javax.swing.JScrollPane();
        drvList = new javax.swing.JList();
        browseButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        drvClassLabel = new javax.swing.JLabel();
        drvClassComboBox = new javax.swing.JComboBox();
        findButton = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        progressMessageLabel = new javax.swing.JLabel();
        progressContainerPanel = new javax.swing.JPanel();

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        drvListLabel.setLabelFor(drvList);
        org.openide.awt.Mnemonics.setLocalizedText(drvListLabel, NbBundle.getBundle(BUNDLE).getString("AddDriverDriverFile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(drvListLabel, gridBagConstraints);

        drvList.setModel(new DefaultListModel());
        drvListScrollPane.setViewportView(drvList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        add(drvListScrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, NbBundle.getBundle(BUNDLE).getString("AddDriverDriverAdd")); // NOI18N
        browseButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        add(browseButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, NbBundle.getBundle(BUNDLE).getString("AddDriverDriverRemove")); // NOI18N
        removeButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        add(removeButton, gridBagConstraints);

        drvClassLabel.setLabelFor(drvClassComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(drvClassLabel, NbBundle.getBundle(BUNDLE).getString("AddDriverDriverClass")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        add(drvClassLabel, gridBagConstraints);

        drvClassComboBox.setEditable(true);
        drvClassComboBox.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(drvClassComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(findButton, NbBundle.getBundle(BUNDLE).getString("AddDriverDriverFind")); // NOI18N
        findButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        add(findButton, gridBagConstraints);

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, NbBundle.getBundle(BUNDLE).getString("AddDriverDriverName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        add(nameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(nameTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(progressMessageLabel, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        add(progressMessageLabel, gridBagConstraints);

        progressContainerPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(progressContainerPanel, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == browseButton) {
                AddDriverDialog.this.browseButtonActionPerformed(evt);
            }
            else if (evt.getSource() == removeButton) {
                AddDriverDialog.this.removeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == drvClassComboBox) {
                AddDriverDialog.this.drvClassComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == findButton) {
                AddDriverDialog.this.findButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void drvClassComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drvClassComboBoxActionPerformed
        if (!customizer)
            nameTextField.setText(DriverListUtil.findFreeName(DriverListUtil.getName((String) drvClassComboBox.getSelectedItem())));
    }//GEN-LAST:event_drvClassComboBoxActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        stopProgress();
        
        ListSelectionModel lsm = drvList.getSelectionModel();
        int count = dlm.getSize();
        int i = 0;
        
        if (count < 1)
            return;
        
        do {
            if (lsm.isSelectedIndex(i)) {
                dlm.remove(i);
                drvs.remove(i);
                count--;
                continue;
            }
            i++;
        } while (count != i);
        
        findDriverClass();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void findButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findButtonActionPerformed
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                startProgress();
                
                for (int i = 0; i < drvs.size(); i++) {
                    try {
                        URL url = (URL)drvs.get(i);
                        File file = new File(new URI(url.toExternalForm()));
                        JarFile jf = new JarFile(file);
                        try {
                            Enumeration entries = jf.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = (JarEntry)entries.nextElement();
                                String className = entry.getName();
                                if (className.endsWith(".class")) { // NOI18N
                                    className = className.replace('/', '.');
                                    className = className.substring(0, className.length() - 6);
                                    InputStream stream = jf.getInputStream(entry);
                                    try {
                                        ClassFile classFile = new ClassFile(stream, false);
                                        for (Iterator iter = classFile.getInterfaces().iterator(); iter.hasNext();) {
                                            ClassName interfaceName = (ClassName)iter.next();
                                            if ("java.sql.Driver".equals(interfaceName.getExternalName())) { // NOI18N
                                                addDriverClass(className);
                                            }
                                        }
                                    } finally {
                                        stream.close();
                                    }
                                }
                            }
                        } finally {
                            jf.close();
                        }
                    } catch (IOException exc) {
                        //PENDING
                    } catch (URISyntaxException use) {
                        //PENDING
                    }
                }
                
                stopProgress();
            }
        }, 0);
    }//GEN-LAST:event_findButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        stopProgress();
        
        JFileChooser fc = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(fc, null);
        fc.setDialogTitle(NbBundle.getBundle(BUNDLE).getString("AddDriver_Chooser_Title")); //NOI18N
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(false);
        
        //.jar and .zip file filter
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return (f.isDirectory() || f.getName().endsWith(".jar") || f.getName().endsWith(".zip")); //NOI18N
            }
            
            public String getDescription() {
                return NbBundle.getBundle(BUNDLE).getString("AddDriver_Chooser_Filter"); //NOI18N
            }
        });
        
        if (fc.showOpenDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) { //NOI18N
            File[] files = fc.getSelectedFiles();            
            for (int i = 0; i < files.length; i++)
                if (files[i] != null && files[i].isFile()) {
                    dlm.addElement(files[i].toString());
                    try {
                        drvs.add(files[i].toURI().toURL());
                    } catch (MalformedURLException exc) {
                        //PENDING
                    }
                }
            
            findDriverClass();
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox drvClassComboBox;
    private javax.swing.JLabel drvClassLabel;
    private javax.swing.JList drvList;
    private javax.swing.JLabel drvListLabel;
    private javax.swing.JScrollPane drvListScrollPane;
    private javax.swing.JButton findButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JPanel progressContainerPanel;
    private javax.swing.JLabel progressMessageLabel;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
    public String getDisplayName() {
        return nameTextField.getText();
    }
    
    public List getDriverLocation() {
        return drvs;
    }
    
    public String getDriverClass() {
        return (String) drvClassComboBox.getSelectedItem();
    }
    
    private void findDriverClass() {
        JarFile jf;
        String[] drivers = (String[]) DriverListUtil.getDrivers().toArray(new String[DriverListUtil.getDrivers().size()]);
        
        drvClassComboBox.removeAllItems();
        for (int i = 0; i < drvs.size(); i++) {
            try {
                URL url = (URL)drvs.get(i);
                File file = new File(new URI(url.toExternalForm()));
                jf = new JarFile(file);
                for (int j = 0; j < drivers.length; j++)
                    if (jf.getEntry(drivers[j].replace('.', '/') + ".class") != null) //NOI18N
                        addDriverClass(drivers[j]);
                jf.close();
            } catch (IOException exc) {
                //PENDING
            } catch (URISyntaxException e) {
                //PENDING
            }
        }
    }
    
    private void addDriverClass(String drv) {
        if (((DefaultComboBoxModel) drvClassComboBox.getModel()).getIndexOf(drv) < 0)
            drvClassComboBox.addItem(drv);
    }
    
    private void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressHandle = ProgressHandleFactory.createHandle(null);
                progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
                progressContainerPanel.add(progressComponent, BorderLayout.CENTER);
                progressHandle.start();
                progressMessageLabel.setText(NbBundle.getBundle(BUNDLE).getString("AddDriverProgressStart"));
            }
        });
    }

    private void stopProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (progressHandle != null) {
                    progressHandle.finish();
                    progressHandle = null;
                    progressMessageLabel.setText(" "); // NOI18N
                    progressContainerPanel.remove(progressComponent);
                    // without this, the removed progress component remains painted on its parent... why?
                    repaint();
                }
            }
        });
    }
}
