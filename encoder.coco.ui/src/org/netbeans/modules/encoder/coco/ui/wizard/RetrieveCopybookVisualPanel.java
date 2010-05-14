/*
 * RetrieveCopybookVisualPanel.java
 *
 * Created on July 5, 2007, 2:01 PM
 */

package org.netbeans.modules.encoder.coco.ui.wizard;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.encoder.coco.ui.CocoEncodingConst;

/**
 * The visual panel of the first step of the wizard.
 * 
 * @author Jun Xu
 */
public class RetrieveCopybookVisualPanel extends javax.swing.JPanel {
    
    private final RetrieveCopybookWizardPanel mEnclosing;
    private final Set<ChangeListener> mChangeListener;
    private Preferences mPrefs = Preferences.userNodeForPackage(this.getClass());
    
    /** Creates new form RetrieveCopybookVisualPanel */
    public RetrieveCopybookVisualPanel(RetrieveCopybookWizardPanel enclosing) {
        mEnclosing = enclosing;
        mChangeListener = new HashSet<ChangeListener>();
        initComponents();
        applyPreferences();
    }
    private static final String PREF_IS_FROM_URL = "is_from_url";  //NOI18N
    private static final String PREF_URL = "url";  //NOI18N
    private static final String PREF_LOCAL_FILE = "local_file";  //NOI18N
    private static final String PREF_TARGET = "target";  //NOI18N
    private static final String PREF_OVERWRITE = "overwrite";  //NOI18N

    public void savePreferences() throws BackingStoreException {
        mPrefs.putBoolean(PREF_IS_FROM_URL, jRadioFromURL.isSelected());
        if (jRadioFromURL.isSelected()) {
            mPrefs.put(PREF_URL, jTextURL.getText());
        } else {
            mPrefs.put(PREF_LOCAL_FILE, jTextFileLocation.getText());
        }
        mPrefs.put(PREF_TARGET, jTextTargetFolder.getText());
        mPrefs.putBoolean(PREF_OVERWRITE, jCheckBoxOverwrite.isSelected());
        mPrefs.flush();
    }

    private void applyPreferences() {
        String value = mPrefs.get(PREF_URL, null);
        jTextURL.setText(value);
        value = mPrefs.get(PREF_LOCAL_FILE, null);
        jTextFileLocation.setText(value);
        
        if (mPrefs.getBoolean(PREF_IS_FROM_URL, true)) {
            jRadioFromURL.setSelected(true);
            jRadioFromFile.setSelected(false);
            setSourceType(PropertyValue.FROM_URL);
        } else {
            jRadioFromURL.setSelected(false);
            jRadioFromFile.setSelected(true);
            setSourceType(PropertyValue.FROM_FILE);
        }
        value = jTextTargetFolder.getText();
        if (value == null || value.length() == 0) {
            // only use saved value if not value yet.
            value = mPrefs.get(PREF_TARGET, null);
            if (value != null) {
                jTextTargetFolder.setText(value);
            }
        }
        jCheckBoxOverwrite.setSelected(mPrefs.getBoolean(PREF_OVERWRITE, false));
    }

    @Override
    public String getName() {
        return "Specify Resource Location";
    }
    
    public void addChangeListener(ChangeListener listener) {
        synchronized(mChangeListener) {
            mChangeListener.add(listener);
        }
    }
    
    public void removeChangeListener(ChangeListener listener) {
        synchronized(mChangeListener) {
            mChangeListener.remove(listener);
        }
    }
    
    public void notifyStateChange() {
        ChangeListener[] listeners =
                mChangeListener.toArray(new ChangeListener[0]);
        ChangeEvent e = new ChangeEvent(this);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].stateChanged(e);
        }
    }
    
    public String getTargetFolder() {
        return jTextTargetFolder.getText();
    }
    /**
     * Sets the target location.
     */
    public void setTargetLocation(String targetLocation) {
        jTextTargetFolder.setText(targetLocation);
    }
    
    public PropertyValue getSourceType() {
        if (jRadioFromURL.isSelected()) {
            return PropertyValue.FROM_URL;
        } else if (jRadioFromFile.isSelected()) {
            return PropertyValue.FROM_FILE;
        }
        // should never reach here.
        return PropertyValue.UNKNOWN_SOURCE_TYPE;
    }
    
    /**
     * Sets the source type.
     * 
     * @param sourceType the source type.  Values should be the strings
     * defined in <code>PropertyValues</code>.
     */
    public void setSourceType(PropertyValue sourceType) {
        if (PropertyValue.FROM_URL.equals(sourceType)) {
            jTextURL.setEnabled(true);
            jTextFileLocation.setEnabled(false);
            jButtonBrowseSource.setEnabled(false);
        } else if (PropertyValue.FROM_FILE.equals(sourceType)) {
            jTextURL.setEnabled(false);
            jTextFileLocation.setEnabled(true);
            jButtonBrowseSource.setEnabled(true);
        } else {
            throw new IllegalArgumentException(
                    "Unknown source type: " + sourceType);
        }
    }
    
    public String getSourceLocation() {
        if (jRadioFromFile.isSelected()) {
            return jTextFileLocation.getText();
        }
        return jTextURL.getText();
    }
    
    public boolean getOverwriteExist() {
        return jCheckBoxOverwrite.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourceTypeButtonGroup = new javax.swing.ButtonGroup();
        jLabelSpecifySource = new javax.swing.JLabel();
        jRadioFromURL = new javax.swing.JRadioButton();
        jTextURL = new javax.swing.JTextField();
        jRadioFromFile = new javax.swing.JRadioButton();
        jTextFileLocation = new javax.swing.JTextField();
        jButtonBrowseSource = new javax.swing.JButton();
        jLabelSpecifyTarget = new javax.swing.JLabel();
        jTextTargetFolder = new javax.swing.JTextField();
        jButtonBrowseTarget = new javax.swing.JButton();
        jCheckBoxOverwrite = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jLabelFile = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel1.name")); // NOI18N
        setPreferredSize(new java.awt.Dimension(502, 301));

        jLabelSpecifySource.setDisplayedMnemonic('S');
        jLabelSpecifySource.setLabelFor(jRadioFromURL);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSpecifySource, org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jLabelSpecifySource.text")); // NOI18N
        jLabelSpecifySource.setName("lblSpecifySource"); // NOI18N

        sourceTypeButtonGroup.add(jRadioFromURL);
        jRadioFromURL.setMnemonic('U');
        org.openide.awt.Mnemonics.setLocalizedText(jRadioFromURL, org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jRadioFromURL.label")); // NOI18N
        jRadioFromURL.setToolTipText(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jRadioFromURL.toolTipText")); // NOI18N
        jRadioFromURL.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioFromURL.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioFromURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioFromURLActionPerformed(evt);
            }
        });

        jTextURL.setText("http://");
        jTextURL.setToolTipText(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jTextURL.toolTipText")); // NOI18N
        jTextURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextURLActionPerformed(evt);
            }
        });
        jTextURL.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextURLFocusLost(evt);
            }
        });

        sourceTypeButtonGroup.add(jRadioFromFile);
        jRadioFromFile.setMnemonic('L');
        org.openide.awt.Mnemonics.setLocalizedText(jRadioFromFile, org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jRadioFromFile.text")); // NOI18N
        jRadioFromFile.setToolTipText(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jRadioFromFile.toolTipText")); // NOI18N
        jRadioFromFile.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioFromFile.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioFromFileActionPerformed(evt);
            }
        });

        jTextFileLocation.setText(null);
        jTextFileLocation.setToolTipText(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "GenerateXSDVisualPanel.jTextFileLocation.toolTipText")); // NOI18N
        jTextFileLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFileLocationActionPerformed(evt);
            }
        });
        jTextFileLocation.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFileLocationFocusLost(evt);
            }
        });

        jButtonBrowseSource.setMnemonic('w');
        org.openide.awt.Mnemonics.setLocalizedText(jButtonBrowseSource, org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jButtonBrowseSource.text")); // NOI18N
        jButtonBrowseSource.setToolTipText(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jButtonBrowseSource.toolTipText")); // NOI18N
        jButtonBrowseSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseSourceActionPerformed(evt);
            }
        });

        jLabelSpecifyTarget.setDisplayedMnemonic('T');
        jLabelSpecifyTarget.setLabelFor(jTextTargetFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSpecifyTarget, org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jLabelSpecifyTarget.text")); // NOI18N

        jTextTargetFolder.setText(null);

        jButtonBrowseTarget.setMnemonic('e');
        org.openide.awt.Mnemonics.setLocalizedText(jButtonBrowseTarget, org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jButtonBrowseTarget.text")); // NOI18N
        jButtonBrowseTarget.setToolTipText(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jButtonBrowseTarget.toolTipText")); // NOI18N
        jButtonBrowseTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseTargetActionPerformed(evt);
            }
        });

        jCheckBoxOverwrite.setMnemonic('O');
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxOverwrite, org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jCheckBoxOverwrite.text")); // NOI18N
        jCheckBoxOverwrite.setToolTipText(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jCheckBoxOverwrite.toolTipText")); // NOI18N
        jCheckBoxOverwrite.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxOverwrite.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel1.setDisplayedMnemonic('R');
        jLabel1.setLabelFor(jTextURL);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "U&RL:");

        jLabelFile.setDisplayedMnemonic('i');
        jLabelFile.setLabelFor(jTextFileLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelFile, org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jLabelFile.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelSpecifySource, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jRadioFromURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 355, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel1)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jTextURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 384, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 70, Short.MAX_VALUE))
                                    .add(jRadioFromFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 352, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabelFile)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jTextFileLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 384, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jButtonBrowseSource, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(4, 4, 4))))
                            .add(layout.createSequentialGroup()
                                .add(jLabelSpecifyTarget, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 389, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jTextTargetFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jButtonBrowseTarget, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(4, 4, 4))
                                    .add(jCheckBoxOverwrite, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 224, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(43, 43, 43)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabelSpecifySource, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jRadioFromURL)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jTextURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jRadioFromFile)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelFile)
                    .add(jTextFileLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonBrowseSource))
                .add(18, 18, 18)
                .add(jLabelSpecifyTarget)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonBrowseTarget)
                    .add(jTextTargetFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxOverwrite)
                .add(90, 90, 90))
        );

        jLabelSpecifySource.getAccessibleContext().setAccessibleName("Specify Source section");
        jLabelSpecifySource.getAccessibleContext().setAccessibleDescription("Specify Source section");
        jRadioFromURL.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jRadioFromURL.AccessibleContext.accessibleName")); // NOI18N
        jRadioFromURL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jRadioFromURL.AccessibleContext.accessibleDescription")); // NOI18N
        jTextURL.getAccessibleContext().setAccessibleName("URL for source COBOL Copybooks");
        jTextURL.getAccessibleContext().setAccessibleDescription("URL for the source COBOL Copybooks");
        jRadioFromFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jRadioFromFile.AccessibleContext.accessibleName")); // NOI18N
        jRadioFromFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jRadioFromFile.AccessibleContext.accessibleDescription")); // NOI18N
        jTextFileLocation.getAccessibleContext().setAccessibleName("Local source file of COBOL Copybooks");
        jTextFileLocation.getAccessibleContext().setAccessibleDescription("Local file for the source COBOL Copybooks");
        jButtonBrowseSource.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jButtonBrowseSource.AccessibleContext.accessibleName")); // NOI18N
        jLabelSpecifyTarget.getAccessibleContext().setAccessibleName("Target Folder");
        jLabelSpecifyTarget.getAccessibleContext().setAccessibleDescription("Target folder to save retrieved COBOL Copybook document");
        jTextTargetFolder.getAccessibleContext().setAccessibleName("Target Folder");
        jTextTargetFolder.getAccessibleContext().setAccessibleDescription("Target Folder for retrieved document");
        jButtonBrowseTarget.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.jButtonBrowseTarget.AccessibleContext.accessibleName")); // NOI18N
        jCheckBoxOverwrite.getAccessibleContext().setAccessibleName("Overwrite files with same name");
        jLabel1.getAccessibleContext().setAccessibleName("Label - URL");
        jLabel1.getAccessibleContext().setAccessibleDescription("Label - URL");
        jLabelFile.getAccessibleContext().setAccessibleName("Label - File");
        jLabelFile.getAccessibleContext().setAccessibleDescription("Label - File");

        getAccessibleContext().setAccessibleName("Details for retrieving");
        getAccessibleContext().setAccessibleDescription("Details for retrieving document");
    }// </editor-fold>//GEN-END:initComponents

private void jTextFileLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFileLocationActionPerformed
    notifyStateChange();
}//GEN-LAST:event_jTextFileLocationActionPerformed

private void jTextURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextURLActionPerformed
    notifyStateChange();
}//GEN-LAST:event_jTextURLActionPerformed

private void jButtonBrowseTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseTargetActionPerformed
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
//    fileChooser.setFileFilter(new FileFilter() {
//
//        public boolean accept(File f) {
//            return f.isDirectory() || CocoEncodingConst.isCpy(f.getName());
//        }
//
//        public String getDescription() {
//            return org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.cpyFileFilterDescription"); // NOI18N
//        }
//    });
    String currentSelected = jTextTargetFolder.getText();
    if (currentSelected != null && currentSelected.length() > 0) {
        fileChooser.setSelectedFile(new File(currentSelected));
    }
    if (fileChooser.showDialog(this, null) == JFileChooser.APPROVE_OPTION) {
        jTextTargetFolder.setText(fileChooser.getSelectedFile().getAbsolutePath());
    }
    notifyStateChange();
}//GEN-LAST:event_jButtonBrowseTargetActionPerformed

private void jButtonBrowseSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseSourceActionPerformed
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileFilter(new FileFilter() {

        public boolean accept(File f) {
            return f.isDirectory() || CocoEncodingConst.isCOBOL(f.getName());
        }

        public String getDescription() {
            return org.openide.util.NbBundle.getMessage(RetrieveCopybookVisualPanel.class, "RetrieveCopybookVisualPanel.cpyFileFilterDescription"); // NOI18N
        }
    });
    String currentSelected = jTextFileLocation.getText();
    if (currentSelected != null && currentSelected.length() > 0) {
        fileChooser.setSelectedFile(new File(currentSelected));
    }
    if (fileChooser.showDialog(this, null) == JFileChooser.APPROVE_OPTION) {
        jTextFileLocation.setText(fileChooser.getSelectedFile().getAbsolutePath());
    }
    notifyStateChange();
}//GEN-LAST:event_jButtonBrowseSourceActionPerformed

private void jRadioFromFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioFromFileActionPerformed
    // TODO add your handling code here:
    setSourceType(PropertyValue.FROM_FILE);
    notifyStateChange();
}//GEN-LAST:event_jRadioFromFileActionPerformed

private void jRadioFromURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioFromURLActionPerformed
    // TODO add your handling code here:
    setSourceType(PropertyValue.FROM_URL);
    notifyStateChange();
}//GEN-LAST:event_jRadioFromURLActionPerformed

private void jTextURLFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextURLFocusLost
    setSourceType(PropertyValue.FROM_URL);
    notifyStateChange();
}//GEN-LAST:event_jTextURLFocusLost

private void jTextFileLocationFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFileLocationFocusLost
    setSourceType(PropertyValue.FROM_FILE);
    notifyStateChange();
}//GEN-LAST:event_jTextFileLocationFocusLost
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBrowseSource;
    private javax.swing.JButton jButtonBrowseTarget;
    private javax.swing.JCheckBox jCheckBoxOverwrite;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelFile;
    private javax.swing.JLabel jLabelSpecifySource;
    private javax.swing.JLabel jLabelSpecifyTarget;
    private javax.swing.JRadioButton jRadioFromFile;
    private javax.swing.JRadioButton jRadioFromURL;
    private javax.swing.JTextField jTextFileLocation;
    private javax.swing.JTextField jTextTargetFolder;
    private javax.swing.JTextField jTextURL;
    private javax.swing.ButtonGroup sourceTypeButtonGroup;
    // End of variables declaration//GEN-END:variables
    
}
