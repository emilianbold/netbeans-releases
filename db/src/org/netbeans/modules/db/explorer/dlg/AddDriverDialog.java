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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.db.explorer.node.DriverNode;
import org.netbeans.modules.db.util.DriverListUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;

public class AddDriverDialog extends javax.swing.JPanel {
    
    private DefaultListModel dlm;
    private List<URL> drvs = new LinkedList<URL>();
    private boolean customizer = false;
    private ProgressHandle progressHandle;
    private JComponent progressComponent;
    private DialogDescriptor descriptor;
    
    private static final Logger LOGGER = Logger.getLogger(AddDriverDialog.class.getName());

    /** Creates new AddDriverDialog.
     * @param driverNode driver node to be customized or null to create a new one
     */
    private AddDriverDialog(DriverNode driverNode) {
        initComponents();
        // hack to force the progressContainerPanel honor its preferred height
        // without it, the preferred height is sometimes ignored during resize
        // progressContainerPanel.add(Box.createVerticalStrut(progressContainerPanel.getPreferredSize().height), BorderLayout.EAST);
        initAccessibility();
        dlm = (DefaultListModel) drvList.getModel();

        if (driverNode != null) {
            setDriver(driverNode.getDatabaseDriver().getJDBCDriver());
        }

        DocumentListener documentListener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                updateState();
            }

            public void removeUpdate(DocumentEvent e) {
                updateState();
            }

            public void changedUpdate(DocumentEvent e) {
                updateState();
            }
        };
        nameTextField.getDocument().addDocumentListener(documentListener);
        drvList.getModel().addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent evt) {
                updateState();
            }

            public void intervalRemoved(ListDataEvent evt) {
                updateState();
            }

            public void contentsChanged(ListDataEvent evt) {
                updateState();
            }
        });
        Component editorComponent = drvClassComboBox.getEditor().getEditorComponent();
        if (editorComponent instanceof JTextComponent) {
            ((JTextComponent) editorComponent).getDocument().addDocumentListener(documentListener);
        }
    }
    
    /** Fills this dialog by parameters of given driver. */
    private void setDriver(JDBCDriver drv) {
        customizer = true;
        
        String fileName = null;
        URL[] urls = drv.getURLs();
        for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            if ("nbinst".equals(url.getProtocol())) { // NOI18N
                // try to get a file: URL for the nbinst: URL
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                    if (localURL != null) {
                        url = localURL;
                    }
                }
            }
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                File diskFile = FileUtil.toFile(fo);
                if (diskFile != null) {
                    fileName = diskFile.getAbsolutePath();
                }
            } else {
                if (url.getProtocol().equals("file")) {  //NOI18N
                    try {
                        fileName = new File(new URI(url.toExternalForm())).getAbsolutePath();
                    } catch (URISyntaxException e) {
                        Exceptions.printStackTrace(e);
                        fileName = null;
                    }
                }
            }
            if (fileName != null) {
                dlm.addElement(fileName);
                // use urls[i], not url, because we want to add the original URL
                drvs.add(urls[i]);
            }
        }
        drvClassComboBox.addItem(drv.getClassName());
        drvClassComboBox.setSelectedItem(drv.getClassName());
        nameTextField.setText(drv.getDisplayName());
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDialogA11yDesc")); //NOI18N
        drvListLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverFileA11yDesc")); //NOI18N
        drvList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverFileListA11yName")); //NOI18N
        drvClassLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverDriverClassA11yDesc")); //NOI18N
        drvClassComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverDriverClassComboBoxA11yName")); //NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverNameA11yDesc")); //NOI18N
        nameTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverNameTextFieldA11yName")); //NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverAddButtonA11yDesc")); //NOI18N
        findButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverRemoveButtonA11yDesc")); //NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverFindButtonA11yDesc")); //NOI18N
        progressContainerPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverProgressBarA11yName")); //NOI18N
        progressContainerPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverProgressBarA11yDesc")); //NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        org.openide.awt.Mnemonics.setLocalizedText(drvListLabel, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverFile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(drvListLabel, gridBagConstraints);

        drvList.setModel(new DefaultListModel());
        drvList.addListSelectionListener(formListener);
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

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverAdd")); // NOI18N
        browseButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        add(browseButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverRemove")); // NOI18N
        removeButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        add(removeButton, gridBagConstraints);

        drvClassLabel.setLabelFor(drvClassComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(drvClassLabel, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverClass")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(findButton, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverFind")); // NOI18N
        findButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        add(findButton, gridBagConstraints);

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverName")); // NOI18N
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

    private class FormListener implements java.awt.event.ActionListener, javax.swing.event.ListSelectionListener {
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

        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
            if (evt.getSource() == drvList) {
                AddDriverDialog.this.drvListValueChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void drvClassComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drvClassComboBoxActionPerformed
        if (!customizer) {
            nameTextField.setText(DriverListUtil.findFreeName(DriverListUtil.getName((String) drvClassComboBox.getSelectedItem())));
        }
    }//GEN-LAST:event_drvClassComboBoxActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        stopProgress();
        
        ListSelectionModel lsm = drvList.getSelectionModel();
        int count = dlm.getSize();
        int i = 0;
        
        if (count < 1) {
            return;
        }
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
        findDriverClassByInspection();
    }//GEN-LAST:event_findButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        stopProgress();

        FileChooserBuilder fileChooserBuilder = new FileChooserBuilder(AddDriverDialog.class);
        fileChooserBuilder.setTitle(NbBundle.getMessage(AddDriverDialog.class, "AddDriver_Chooser_Title")); //NOI18N
        //.jar and .zip file filter
        fileChooserBuilder.setFileFilter(new FileFilter() {

            public boolean accept(File f) {
                return (f.isDirectory() || f.getName().endsWith(".jar") || f.getName().endsWith(".zip")); //NOI18N
            }

            public String getDescription() {
                return NbBundle.getMessage(AddDriverDialog.class, "AddDriver_Chooser_Filter"); //NOI18N
            }
        });

        File[] selectedFiles = fileChooserBuilder.showMultiOpenDialog();
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                if (file.isFile()) {
                    if (dlm.contains(file.toString())) {
                        // file already added
                        NotifyDescriptor msgDesc = new NotifyDescriptor.Message(NbBundle.getMessage(AddDriverDialog.class, "AddDriverDuplicateFile", file.toString()));
                        DialogDisplayer.getDefault().notify(msgDesc);
                        continue;
                    }
                    dlm.addElement(file.toString());
                    try {
                        drvs.add(file.toURI().toURL());
                    } catch (MalformedURLException exc) {
                        LOGGER.log(Level.WARNING,
                                "Unable to add driver jar file " +
                                file.getAbsolutePath() +
                                ": can not convert to URL", exc);
                    }
                }
            }
            findDriverClass();
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void drvListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_drvListValueChanged
        updateState();
    }//GEN-LAST:event_drvListValueChanged
            
    
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
    
    private boolean isDriverClass(URLClassLoader jarloader, String className) {
        Class<?> clazz;

        try {
            clazz = jarloader.loadClass(className);
        } catch ( Throwable t ) {
            LOGGER.log(Level.FINE, null, t);
            
            LOGGER.log(Level.INFO, 
                 "Got an exception trying to load class " +
                 className + " during search for JDBC drivers in " +
                 " driver jar(s): " + t.getClass().getName() + ": "
                 + t.getMessage() + ".  Skipping this class..."); // NOI18N

            return false;         
        }

        if ( Driver.class.isAssignableFrom(clazz) ) {
            return true;
        }
        
        return false;
    }
        
    public String getDisplayName() {
        return nameTextField.getText();
    }
    
    public List<URL> getDriverLocation() {
        return drvs;
    }
    
    public String getDriverClass() {
        return (String) drvClassComboBox.getSelectedItem();
    }

    private void findDriverClass() {
        JarFile jf;
        String[] drivers = DriverListUtil.getDrivers ().toArray (new String[DriverListUtil.getDrivers ().size ()]);
        
        drvClassComboBox.removeAllItems();
        for (int i = 0; i < drvs.size(); i++) {
            try {
                URL url = drvs.get (i);

                if ("nbinst".equals(url.getProtocol())) { // NOI18N
                    // try to get a file: URL for the nbinst: URL
                    FileObject fo = URLMapper.findFileObject(url);
                    if (fo != null) {
                        URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                        if (localURL != null) {
                            url = localURL;
                        }
                    }
                }

                File file = new File(new URI(url.toExternalForm()));
                jf = new JarFile(file);
                for (int j = 0; j < drivers.length; j++) {
                    if (jf.getEntry(drivers[j].replace('.', '/') + ".class") != null) {  //NOI18N
                        addDriverClass(drivers[j]);
                    }
                }
                jf.close();
            } catch (IOException exc) {
                //PENDING
            } catch (URISyntaxException e) {
                //PENDING
            }
        }
    }
    
    private void findDriverClassByInspection() {
        drvClassComboBox.removeAllItems();
        findButton.setEnabled(false);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                startProgress();
                                     
                // This classloader is used to load classes
                // from the jar files for the driver.  We can then use
                // introspection to see if a class in one of these jar files
                // implements java.sql.Driver
                URLClassLoader jarloader = 
                    new URLClassLoader(drvs.toArray(new URL[drvs.size ()]),this.getClass ().getClassLoader ());

                for (int i = 0; i < dlm.size(); i++) {
                    try {
                        String file  = (String)dlm.get(i);
                        JarFile jf = new JarFile(file);
                        try {
                            Enumeration entries = jf.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = (JarEntry)entries.nextElement();
                                String className = entry.getName();
                                if (className.endsWith(".class")) { // NOI18N
                                    className = className.replace('/', '.');
                                    className = className.substring(0, className.length() - 6);
                                    if ( isDriverClass(jarloader, className) ) {
                                        if (progressHandle != null) {
                                            addDriverClass(className);
                                        } else {
                                            // already stopped
                                            updateState();
                                            return;
                                        }
                                    }
                                }
                            }
                        } finally {
                            jf.close();
                        }
                    } catch (IOException exc) {
                        //PENDING
                    }
                }
                stopProgress();
                updateState();
            }
        }, 0);
    }
    
    private void addDriverClass(String drv) {
        if (((DefaultComboBoxModel) drvClassComboBox.getModel()).getIndexOf(drv) < 0) {
            drvClassComboBox.addItem(drv);
        }
    }
    
    private void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressHandle = ProgressHandleFactory.createHandle(null);
                progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
                progressContainerPanel.add(progressComponent, BorderLayout.CENTER);
                progressHandle.start();
                progressMessageLabel.setText(NbBundle.getMessage (AddDriverDialog.class, "AddDriverProgressStart"));
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

    private void setDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        updateState();
    }

    /** Updates state of UI controls. */
    private void updateState() {
        // update Remove button state
        removeButton.setEnabled(drvList.getSelectedIndices().length > 0);
        // update Find button state
        findButton.setEnabled(progressHandle == null && drvList.getModel().getSize() > 0);
        // update status line and OK button
        String message = null;
        if (drvList.getModel().getSize() == 0) {
            message = NbBundle.getMessage(AddDriverDialog.class, "AddDriverMissingFile");
        } else if (drvClassComboBox.getEditor().getItem().toString().length() == 0) {
            message = NbBundle.getMessage(AddDriverDialog.class, "AddDriverMissingClass");
        } else if (nameTextField.getText().length() == 0) {
            message = NbBundle.getMessage(AddDriverDialog.class, "AddDriverMissingName");
        } else if (!customizer && nameTextField.getText().length() > 0) {
            String newDisplayName = nameTextField.getText();
            for (JDBCDriver driver : JDBCDriverManager.getDefault().getDrivers()) {
                if (driver.getDisplayName().equalsIgnoreCase(newDisplayName)) {
                    message = NbBundle.getMessage(AddDriverDialog.class, "AddDriverDuplicateName");
                    break;
                }
            }
        }
        if (message != null) {
            descriptor.getNotificationLineSupport().setInformationMessage(message);
            descriptor.setValid(false);
        } else {
            descriptor.getNotificationLineSupport().clearMessages();
            descriptor.setValid(true);
        }
    }

    /** Shows New JDBC Driver dialog and returns driver instance if user
     * clicks OK. Otherwise it returns null.
     * @param driverNode existing driver node to be customized or null to create new one
     * @return driver instance if user clicks OK, null otherwise
     */
    public static JDBCDriver showDialog(DriverNode driverNode) {
        AddDriverDialog dlgPanel = new AddDriverDialog(driverNode);

        DialogDescriptor descriptor = new DialogDescriptor(dlgPanel, NbBundle.getMessage(AddDriverDialog.class, "AddDriverDialogTitle")); //NOI18N
        descriptor.createNotificationLineSupport();
        dlgPanel.setDescriptor(descriptor);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);

        JDBCDriver driver = null;
        if (DialogDescriptor.OK_OPTION == descriptor.getValue()) {
            List<URL> drvLoc = dlgPanel.getDriverLocation();
            String drvClass = dlgPanel.getDriverClass();
            String displayName = dlgPanel.getDisplayName();
            String name = displayName;
            try {
                if (driverNode != null) {
                    // keep old name
                    name = driverNode.getDatabaseDriver().getJDBCDriver().getName();
                    driverNode.destroy();
                }
                driver = JDBCDriver.create(name, displayName, drvClass, drvLoc.toArray(new URL[0]));
                JDBCDriverManager.getDefault().addDriver(driver);
            } catch (DatabaseException exc) {
                Exceptions.printStackTrace(exc);
            }
        }
        return driver;
    }

    /** Shows New JDBC Driver dialog and returns driver instance if user
     * clicks OK. Otherwise it returns null.
     * @return driver instance if user clicks OK, null otherwise
     */
    public static JDBCDriver showDialog() {
        return showDialog(null);
    }
}
