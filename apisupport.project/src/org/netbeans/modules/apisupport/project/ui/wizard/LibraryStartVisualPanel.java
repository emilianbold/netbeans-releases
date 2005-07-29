/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.openide.WizardDescriptor;

/**
 * first panel of the librarywrapper module wizard
 * @author Millos Kleint
 */
final class LibraryStartVisualPanel extends BasicVisualPanel {

    static final String PROP_LIBRARY_PATH = "LIBRARY_PATH_VALUE"; //NOI18N
    static final String PROP_LICENSE_PATH = "LICENSE_PATH_VALUE"; //NOI18N        
    
    private static final String PATH_SEPARATOR = ";";
    
    private NewModuleProjectData data;
    
    private boolean listenersAttached;
    private DocumentListener libraryDL;
    private DocumentListener licenseDL;
    
    /** Creates new form BasicConfVisualPanel */
    public LibraryStartVisualPanel(WizardDescriptor setting) {
        super(setting);
        initComponents();
        data = (NewModuleProjectData) getSettings().getProperty(
                NewModuleProjectData.DATA_PROPERTY_NAME);
        libraryDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { 
                checkLibrary(); 
            }
        };
        licenseDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { 
                checkLicense(); 
            }
        };
        
    }
    
    private void checkLibrary() {
        String text = txtLibrary.getText().trim();
        if (text.length() > 0) {
            StringTokenizer tokens = new StringTokenizer(text, PATH_SEPARATOR);
            while (tokens.hasMoreTokens()) {
                String one = tokens.nextToken();
                File fil = new File(one);
                if (!fil.exists()) {
                    setErrorMessage(getMessage("MSG_Invalid_Library_Path"));
                    return;
                }
                try {
                    JarFile jf = new JarFile(fil);
                } catch (IOException exc) {
                    setErrorMessage(getMessage("MSG_Invalid_Library_Path"));
                    return;
                }
            }
            setErrorMessage(null);
        } else  {
            setErrorMessage(getMessage("MSG_Library_Path_Not_Defined"));
        }
    }
    
    private void checkLicense() {
        String text = txtLicense.getText().trim();
        if (text.length() > 0) {
            File fil = new File(text);
            if (!fil.exists()) {
                setErrorMessage(getMessage("MSG_Invalid_License_Path"));
                return;
            }
        }
        setErrorMessage(null);
    }
    
    
    void refreshData() {
        String license = (String)getSettings().getProperty(PROP_LICENSE_PATH);
        String jars = (String)getSettings().getProperty(PROP_LIBRARY_PATH);
        
        
//        String cnb = data.getCodeNameBase();
//        codeNameBaseValue.setText(cnb);
//        if (cnb.startsWith(EXAMPLE_BASE_NAME)) {
//            codeNameBaseValue.select(0, EXAMPLE_BASE_NAME.length() - 1);
//        }
//        String dn = data.getProjectDisplayName();
//        displayNameValue.setText(dn);
//        checkCodeNameBase();
    }
    
    /** Stores collected data into model. */
    void storeData() {
        String jars = txtLibrary.getText().trim();
        getSettings().putProperty(PROP_LIBRARY_PATH, jars);
        getSettings().putProperty(PROP_LICENSE_PATH, txtLicense.getText().trim());
        populateProjectData(data, jars);
//        // change will be fired -> update data
//        data.setCodeNameBase(getCodeNameBaseValue());
//        data.setProjectDisplayName(displayNameValue.getText());
//        data.setBundle(getBundleValue());
//        if (!libraryModule) {
//            data.setLayer(getLayerValue());
//        }
    }
    
    static void populateProjectData(NewModuleProjectData data, String paths) {
        if (data.getProjectName() != null && data.getCodeNameBase() != null) {
            return;
        }
        StringTokenizer tokens = new StringTokenizer(paths, PATH_SEPARATOR);
        if (tokens.hasMoreTokens()) {
            File fil = new File(tokens.nextToken());
            String name = fil.getName();
            name = name.substring(0, name.lastIndexOf('.') - 1);
            name = name.replaceAll("[0-9._-]+$", "");
            data.setProjectName(name);
            JarFile jf = null;
            String shortestPath = null;
            try {
                jf = new JarFile(fil);
                Enumeration en = jf.entries();
                while (en.hasMoreElements()) {
                    JarEntry entry = (JarEntry)en.nextElement();
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        String nm = entry.getName();
                        String path = nm.substring(0, nm.lastIndexOf('/'));
                        if (shortestPath == null || path.length() < shortestPath.length()) {
                            shortestPath = path;
                        }
                    }
                }
            } catch (IOException e) {
            } finally {
                if (jf != null) {
                    try {
                        jf.close();
                    } catch (IOException e) {
                    }
                }
            }
            if (shortestPath != null) {
                data.setCodeNameBase(shortestPath.replace('/', '.'));
            }
        }
    }
    
    
    public void addNotify() {
        super.addNotify();
        attachDocumentListeners();
    }
    
    public void removeNotify() {
        // prevent checking when the panel is not "active"
        removeDocumentListeners();
        super.removeNotify();
    }
    
    private void attachDocumentListeners() {
        if (!listenersAttached) {
            txtLibrary.getDocument().addDocumentListener(libraryDL);
            txtLicense.getDocument().addDocumentListener(licenseDL);
            listenersAttached = true;
        }
    }
    
    private void removeDocumentListeners() {
        if (listenersAttached) {
            txtLibrary.getDocument().removeDocumentListener(libraryDL);
            txtLicense.getDocument().removeDocumentListener(licenseDL);
            listenersAttached = false;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        confPanel = new javax.swing.JPanel();
        lblLibrary = new javax.swing.JLabel();
        txtLibrary = new javax.swing.JTextField();
        lblLicense = new javax.swing.JLabel();
        txtLicense = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        confPanel.setLayout(new java.awt.GridBagLayout());

        lblLibrary.setLabelFor(txtLibrary);
        org.openide.awt.Mnemonics.setLocalizedText(lblLibrary, org.openide.util.NbBundle.getMessage(LibraryStartVisualPanel.class, "LBL_Library_path"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 12);
        confPanel.add(lblLibrary, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 0);
        confPanel.add(txtLibrary, gridBagConstraints);

        lblLicense.setLabelFor(txtLicense);
        org.openide.awt.Mnemonics.setLocalizedText(lblLicense, org.openide.util.NbBundle.getMessage(LibraryStartVisualPanel.class, "LBL_License_Path"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        confPanel.add(lblLicense, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        confPanel.add(txtLicense, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(LibraryStartVisualPanel.class, "CTL_BrowseButton_o"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        confPanel.add(jButton1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(LibraryStartVisualPanel.class, "CTL_BrowseButton_w"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        confPanel.add(jButton2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        confPanel.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(confPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        JFileChooser chooser = new JFileChooser(txtLicense.getText());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        if (txtLicense.getText().trim().length() > 0) {
            chooser.setSelectedFile(new File(txtLicense.getText().trim()));
        }
        int ret = chooser.showDialog(this, "Select");
        if (ret == JFileChooser.APPROVE_OPTION) {
            txtLicense.setText(chooser.getSelectedFile().getAbsolutePath());
        }
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JFileChooser chooser = new JFileChooser();
        File[] olds = convertStringToFiles(txtLibrary.getText().trim());
        chooser.setSelectedFiles(olds);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        chooser.addChoosableFileFilter(new JarZipFilter());
        int ret = chooser.showDialog(this, "Select");
        if (ret == JFileChooser.APPROVE_OPTION) {
            File[] files =  chooser.getSelectedFiles();
            String path = "";
            for (int i = 0; i < files.length; i++) {
                path = path + files[i] + ( i == files.length - 1 ? "" : ";");
            }
            txtLibrary.setText(path);
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    
    static File[] convertStringToFiles(String path) {
        StringTokenizer tok = new StringTokenizer(path, ";");
        File[] olds = new File[tok.countTokens()]; 
        for (int i = 0; i < olds.length; i++) {
            olds[i] = new File(tok.nextToken());
        }
        return olds;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel confPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblLibrary;
    private javax.swing.JLabel lblLicense;
    private javax.swing.JTextField txtLibrary;
    private javax.swing.JTextField txtLicense;
    // End of variables declaration//GEN-END:variables
    
    private static final class JarZipFilter extends FileFilter {
             public boolean accept(File pathname) {
                 return  pathname.isDirectory() || pathname.getName().endsWith("zip") || pathname.getName().endsWith("jar");
             }
             public String getDescription() {
                 return "*.jar, *.zip";
             }
        
    }
}
