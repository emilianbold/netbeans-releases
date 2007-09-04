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

package org.netbeans.modules.mobility.cldcplatform.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.cldcplatform.UEIEmulatorConfiguratorImpl;
import org.netbeans.spi.mobility.cldcplatform.CLDCPlatformDescriptor;
import org.netbeans.spi.mobility.cldcplatform.CustomCLDCPlatformConfigurator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import java.net.URL;

/**
 *
 * @author  dave
 */
public class DetectPanel extends javax.swing.JPanel {
    
    static final String PROP_PLATFORMS = "Platforms"; // NOI18N
    
    protected final DefaultListModel platformsListModel = new DefaultListModel();
    private final DefaultListModel javadocListModel = new DefaultListModel();
    private final DefaultListModel sourceListModel = new DefaultListModel();
    
    private final PlatformsRenderer platformsRenderer = new PlatformsRenderer();
    private final CheckListener checkListener = new CheckListener();
    private final DocumentListener nameFieldListener = new NameFieldListener();
    private final FoldersRenderer foldersRenderer = new FoldersRenderer();
    
    protected static final String DETECTION_IN_PROGRESS = NbBundle.getMessage(DetectPanel.class, "msg_detection_in_progress"); //NOI18N
    protected static final String INVALID_NAME = NbBundle.getMessage(DetectPanel.class, "msg_invalid_name"); //NOI18N
    protected static final String NAME_COLLISION = NbBundle.getMessage(DetectPanel.class, "msg_name_collision"); //NOI18N
    protected static final String ALREADY_INSTALLED = NbBundle.getMessage(DetectPanel.class, "msg_already_installed"); //NOI18N
    protected static final String DETECTION_FAILED = NbBundle.getMessage(DetectPanel.class, "msg_detection_failed"); //NOI18N
    
    protected static final Color COLOR_GRAY = UIManager.getColor("Label.disabledForeground"); //NOI18N
    protected static final Color COLOR_RED = new Color(192, 0, 0);
    
    private String fileChooserValue;
    
    protected DetectWizardPanel wizardPanel;
    private WizardDescriptor wizardDescriptor;
    protected boolean stop;
    final private Map<File,PlatformDescriptor> descriptors = new HashMap<File,PlatformDescriptor>();
    
    /** Creates new form DetectPanel */
    public DetectPanel(DetectWizardPanel wizardPanel) {
        this.wizardPanel = wizardPanel;
        initComponents();
        platformsList.addMouseListener(checkListener);
        platformsList.addKeyListener(checkListener);
        platformsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(@SuppressWarnings("unused")
			final ListSelectionEvent e) {
                updateDetails();
            }
        });
        nameField.getDocument().addDocumentListener(nameFieldListener);
        infoPanel.setEditorKitForContentType("text/html", new HTMLEditorKit()); //NOI18N
        infoPanel.setContentType("text/html;charset=UTF-8"); //NOI18N
        javadocList.addListSelectionListener(new ListSelectionListener() {
            @SuppressWarnings("synthetic-access")
			public void valueChanged(@SuppressWarnings("unused")
			final ListSelectionEvent e) {
                removeJavadocButton.setEnabled(javadocList.getSelectedValue() != null);
            }
        });
        sourceList.addListSelectionListener(new ListSelectionListener() {
            @SuppressWarnings("synthetic-access")
			public void valueChanged(@SuppressWarnings("unused")
			final ListSelectionEvent e) {
                removeSourceButton.setEnabled(sourceList.getSelectedValue() != null);
            }
        });
    }
    
    
    public void showError(final String message) {
        if (wizardDescriptor != null) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", message); // NOI18N
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings({"synthetic-access","deprecation"}) 
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        platformsList = new javax.swing.JList();
        switchPanel = new javax.swing.JPanel();
        notSelectedPanel = new javax.swing.JPanel();
        notDetectedPanel = new javax.swing.JPanel();
        notDetectedLabel = new javax.swing.JLabel();
        errorPlatformPanel = new javax.swing.JPanel();
        errorPlatformLabel = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        infoPanel = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        javadocList = new javax.swing.JList();
        addJavadocButton = new javax.swing.JButton();
        removeJavadocButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        sourceList = new javax.swing.JList();
        addSourceButton = new javax.swing.JButton();
        removeSourceButton = new javax.swing.JButton();

        setName(NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Platforms")); // NOI18N
        setPreferredSize(new java.awt.Dimension(540, 450));
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setBorder(null);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel5.setLabelFor(platformsList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Select_Platforms")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel4.add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ACSD_DetectPanel_Select_Platform")); // NOI18N

        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 150));

        platformsList.setModel(platformsListModel);
        platformsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        platformsList.setCellRenderer(platformsRenderer);
        jScrollPane1.setViewportView(platformsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel4.add(jScrollPane1, gridBagConstraints);

        jSplitPane1.setTopComponent(jPanel4);

        switchPanel.setLayout(new java.awt.CardLayout());

        notSelectedPanel.setLayout(new java.awt.BorderLayout());
        switchPanel.add(notSelectedPanel, "NotSelected");

        notDetectedPanel.setLayout(new java.awt.BorderLayout());

        notDetectedLabel.setForeground(notDetectedPanel.getBackground ().darker ());
        notDetectedLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(notDetectedLabel, NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Detecting_details")); // NOI18N
        notDetectedLabel.setOpaque(true);
        notDetectedPanel.add(notDetectedLabel, java.awt.BorderLayout.CENTER);

        switchPanel.add(notDetectedPanel, "NotDetected");

        errorPlatformPanel.setLayout(new java.awt.BorderLayout());

        errorPlatformLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(errorPlatformLabel, NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Platform_detection_error")); // NOI18N
        errorPlatformLabel.setOpaque(true);
        errorPlatformPanel.add(errorPlatformLabel, java.awt.BorderLayout.CENTER);

        switchPanel.add(errorPlatformPanel, "ErrorPlatform");

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Platform_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel3.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ACSD_DetectPanel_Platform_Name")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        jPanel3.add(nameField, gridBagConstraints);

        jLabel2.setLabelFor(infoPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Platform_Details")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 6);
        jPanel3.add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ACSD_DetectPanel_Platform_Details")); // NOI18N

        infoPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        infoPanel.setEditable(false);
        jScrollPane2.setViewportView(infoPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        jPanel3.add(jScrollPane2, gridBagConstraints);

        jTabbedPane1.addTab(NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Description"), jPanel3); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel3.setLabelFor(javadocList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Pl_Javadoc")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ASCD_DetectPanel_Pl_Javadoc")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ASCD_DetectPanel_Pl_Javadoc")); // NOI18N

        javadocList.setModel(javadocListModel);
        javadocList.setCellRenderer(foldersRenderer);
        jScrollPane3.setViewportView(javadocList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
        jPanel1.add(jScrollPane3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addJavadocButton, NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Add")); // NOI18N
        addJavadocButton.setActionCommand(org.openide.util.NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Add")); // NOI18N
        addJavadocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addJavadocButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel1.add(addJavadocButton, gridBagConstraints);
        addJavadocButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ASCD_DetectPanel_Add")); // NOI18N
        addJavadocButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ASCD_DetectPanel_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeJavadocButton, NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Remove")); // NOI18N
        removeJavadocButton.setActionCommand(org.openide.util.NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Remove")); // NOI18N
        removeJavadocButton.setEnabled(false);
        removeJavadocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeJavadocButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        jPanel1.add(removeJavadocButton, gridBagConstraints);

        jTabbedPane1.addTab(NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Javadoc"), jPanel1); // NOI18N

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel4.setLabelFor(sourceList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_PL_Sources")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel2.add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ASCD_DetectPanel_PL_Sources")); // NOI18N

        sourceList.setModel(sourceListModel);
        sourceList.setCellRenderer(foldersRenderer);
        jScrollPane4.setViewportView(sourceList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
        jPanel2.add(jScrollPane4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addSourceButton, NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Add")); // NOI18N
        addSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSourceButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel2.add(addSourceButton, gridBagConstraints);
        addSourceButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ASCD_DetectPanel_Add")); // NOI18N
        addSourceButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ASCD_DetectPanel_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeSourceButton, org.openide.util.NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Remove")); // NOI18N
        removeSourceButton.setEnabled(false);
        removeSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSourceButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        jPanel2.add(removeSourceButton, gridBagConstraints);
        removeSourceButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ASCD_DetectPanel_Remove")); // NOI18N
        removeSourceButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ASCD_DetectPanel_Remove")); // NOI18N

        jTabbedPane1.addTab(NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Sources"), jPanel2); // NOI18N

        switchPanel.add(jTabbedPane1, "Details");

        jSplitPane1.setBottomComponent(switchPanel);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    private void addJavadocButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addJavadocButtonActionPerformed
        final J2MEPlatform platform = findSelectedPlatform();
        if (platform == null)
            return;
        final String value = browse(NbBundle.getMessage(DetectPanel.class, "TITLE_DetectPanel_SelectJavaDoc")); // NOI18N
        if (value == null)
            return;
        URL o = J2MEPlatform.localfilepath2url(value);
        if (o == null)
            return;
        final ArrayList<URL> list = new ArrayList<URL>(platform.getJavadocFolders());
        list.add(o);
        platform.setJavadocFolders(list);
        descriptorUpdated();
    }//GEN-LAST:event_addJavadocButtonActionPerformed
    
    private void removeJavadocButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeJavadocButtonActionPerformed
        final J2MEPlatform platform = findSelectedPlatform();
        if (platform == null)
            return;
        final Object selectedValue = javadocList.getSelectedValue();
        if (selectedValue == null)
            return;
        final ArrayList<URL> list = new ArrayList<URL>(platform.getJavadocFolders());
        list.remove(selectedValue);
        platform.setJavadocFolders(list);
        descriptorUpdated();
    }//GEN-LAST:event_removeJavadocButtonActionPerformed
    
    private void addSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSourceButtonActionPerformed
        final J2MEPlatform platform = findSelectedPlatform();
        if (platform == null)
            return;
        final String value = browse(NbBundle.getMessage(DetectPanel.class, "TITLE_DetectPanel_SelectSource")); // NOI18N
        if (value == null)
            return;
        final FileObject o = platform.resolveRelativePathToFileObject(value);
        if (o == null)
            return;
        final ArrayList<FileObject> list = new ArrayList<FileObject>(Arrays.asList(platform.getSourceFolders().getRoots()));
        list.add(o);
        platform.setSourceFolders(list);
        descriptorUpdated();
    }//GEN-LAST:event_addSourceButtonActionPerformed
    
    private void removeSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSourceButtonActionPerformed
        final J2MEPlatform platform = findSelectedPlatform();
        if (platform == null)
            return;
        final Object selectedValue = sourceList.getSelectedValue();
        if (selectedValue == null)
            return;
        final ArrayList<FileObject> list = new ArrayList<FileObject>(Arrays.asList(platform.getSourceFolders().getRoots()));
        list.remove(selectedValue);
        platform.setSourceFolders(list);
        descriptorUpdated();
    }//GEN-LAST:event_removeSourceButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addJavadocButton;
    private javax.swing.JButton addSourceButton;
    private javax.swing.JLabel errorPlatformLabel;
    private javax.swing.JPanel errorPlatformPanel;
    private javax.swing.JTextPane infoPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList javadocList;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel notDetectedLabel;
    private javax.swing.JPanel notDetectedPanel;
    private javax.swing.JPanel notSelectedPanel;
    private javax.swing.JList platformsList;
    private javax.swing.JButton removeJavadocButton;
    private javax.swing.JButton removeSourceButton;
    private javax.swing.JList sourceList;
    private javax.swing.JPanel switchPanel;
    // End of variables declaration//GEN-END:variables
    
    
    public void readSettings(final WizardDescriptor descriptor) {
        this.wizardDescriptor = descriptor;
        final File[] folders = ((Set<File>)descriptor.getProperty(FindPanel.PROP_PLATFORM_FOLDERS)).toArray(new File[0]);
        platformsListModel.clear();
        for (int i=0; i<folders.length; i++) {
            PlatformDescriptor pd = descriptors.get(folders[i]);
            if (pd == null) {
                pd = new PlatformDescriptor(folders[i]);
                descriptors.put(folders[i], pd);
            }
            platformsListModel.addElement(pd);
        }
        if (folders.length > 0) platformsList.setSelectedIndex(0);
        showError(NbBundle.getMessage(DetectPanel.class, "WARN_SearchInProgress"));//NOI18N
        stop = false;
        RequestProcessor.getDefault().post(new DetectRunnable());
    }
    
    public void storeSettings(final WizardDescriptor descriptor) {
        stop = true;
        descriptor.putProperty(PROP_PLATFORMS, getPlatforms());
    }
    
    public J2MEPlatform[] getPlatforms() {
        final Object[] descs = platformsListModel.toArray();
        final ArrayList<J2MEPlatform> list = new ArrayList<J2MEPlatform>();
        for (int i = 0; i < descs.length; i++) {
            final PlatformDescriptor desc = (PlatformDescriptor) descs[i];
            if (desc.isProbed() && desc.isSelected()) {
                final J2MEPlatform platform = desc.getPlatform();
                if (platform != null)
                    list.add(platform);
            }
        }
        return list.toArray(new J2MEPlatform[list.size()]);
    }
    
    public boolean isValid() {
        final Object descs[] = platformsListModel.toArray();
        if (checkForError(findSelectedPlatformDescriptor())) return false;
        boolean selected = false;
        for (int i=0; i<descs.length; i++) {
            if(checkForError((PlatformDescriptor)descs[i])) return false;
            selected |= ((PlatformDescriptor)descs[i]).isSelected();
        }
        if (!selected) {
            showError(NbBundle.getMessage(DetectPanel.class, "ERR_NothingSelected"));//NOI18N
            return false;
        }
        showError(null);
        return true;
    }
    
    private boolean checkForError(final PlatformDescriptor pd) {
        if (pd == null) return false;
        if (!pd.isNameValid()) showError(NbBundle.getMessage(DetectPanel.class, "ERR_InvalidName"));//NOI18N
        else if (pd.isNameCollision()) showError(NbBundle.getMessage(DetectPanel.class, "ERR_NameCollision"));//NOI18N
        else return false;
        return true;
    }
    
    protected void descriptorUpdated() {
        platformsList.repaint();
        updateDetails();
        wizardPanel.fireChanged();
    }
    
    protected void updateDetails() {
        final PlatformDescriptor descriptor = findSelectedPlatformDescriptor();
        if (descriptor == null) {
            ((CardLayout) switchPanel.getLayout()).show(switchPanel, "NotSelected"); //NOI18N
            return;
        }
        if (! descriptor.isProbed()) {
            ((CardLayout) switchPanel.getLayout()).show(switchPanel, "NotDetected"); //NOI18N
            return;
        }
        final J2MEPlatform platform = descriptor.getPlatform();
        if (platform == null) {
            ((CardLayout) switchPanel.getLayout()).show(switchPanel, "ErrorPlatform"); //NOI18N
            return;
        }
        nameField.getDocument().removeDocumentListener(nameFieldListener);
        nameField.setText(platform.getDisplayName());
        nameField.getDocument().addDocumentListener(nameFieldListener);
        
        infoPanel.setText(generateDescriptionForPlatform(platform));
        
        ((CardLayout) switchPanel.getLayout()).show(switchPanel, "Details"); //NOI18N
        infoPanel.setCaretPosition(0);
        
        final FileObject[] al = platform.getSourceFolders().getRoots();
        sourceListModel.clear();
        if (al != null) for (int a = 0; a < al.length; a ++)
            sourceListModel.addElement(al[a]);
        
        final java.util.List<URL> li = platform.getJavadocFolders();
        javadocListModel.clear();
        if (li != null) 
        	for (URL lia : li )
            	javadocListModel.addElement(lia);
        wizardPanel.fireChanged();
    }
    
    protected PlatformDescriptor findSelectedPlatformDescriptor() {
        return (PlatformDescriptor) platformsList.getSelectedValue();
    }
    
    private J2MEPlatform findSelectedPlatform() {
        final DetectPanel.PlatformDescriptor descriptor = findSelectedPlatformDescriptor();
        return descriptor != null ? descriptor.getPlatform() : null;
    }
    
    private String generateDescriptionForPlatform(final J2MEPlatform platform) {
        final StringBuffer sb = new StringBuffer(40);
        
        sb.append("<html><font face=\"dialog\" size=\"-1\">"); //NOI18N
        sb.append(NbBundle.getMessage(DetectPanel.class, "MSG_DetectPanel_Devices")); //NOI18N
        final J2MEPlatform.Device[] devices = platform.getDevices();
        if (devices != null)
            for (int i = 0; i < devices.length; i++) {
            final J2MEPlatform.Device device = devices[i];
            sb.append(i > 0 ? ", " : " "); //NOI18N
            sb.append(device.getName());
            }
        
        final ArrayList<String> profiles = new ArrayList<String>();
        final ArrayList<String> configurations = new ArrayList<String>();
        final ArrayList<String> optionals = new ArrayList<String>();
        
        if (devices != null) for (int a = 0; a < devices.length; a ++) {
            final J2MEPlatform.J2MEProfile[] ps = devices[a].getProfiles();
            if (ps != null) for (int b = 0; b < ps.length; b ++) {
                final String n = ps[b].toString();
                final String type = ps[b].getType();
                if (J2MEPlatform.J2MEProfile.TYPE_PROFILE.equals(type)) {
                    if (!profiles.contains(n))
                        profiles.add(n);
                } else if (J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION.equals(type)) {
                    if (!configurations.contains(n))
                        configurations.add(n);
                } else if (J2MEPlatform.J2MEProfile.TYPE_OPTIONAL.equals(type)) {
                    if (!optionals.contains(n))
                        optionals.add(n);
                }
            }
        }
        
        sb.append(NbBundle.getMessage(DetectPanel.class, "MSG_DetectPanel_Profiles")); //NOI18N
        printToStringBuffer(profiles.toArray(), sb);
        
        sb.append(NbBundle.getMessage(DetectPanel.class, "MSG_DetectPanel_Configurations")); //NOI18N
        printToStringBuffer(configurations.toArray(), sb);
        
        sb.append(NbBundle.getMessage(DetectPanel.class, "MSG_DetectPanel_Optional")); //NOI18N
        printToStringBuffer(optionals.toArray(), sb);
        
        return sb.toString();
    }
    
    private void printToStringBuffer(final Object[] objs, final StringBuffer sb) {
        Arrays.sort(objs);
        if (objs.length > 0) {
            for (int i = 0; i < objs.length; i++)
                sb.append(i > 0 ? ", " : " ").append(objs[i]); //NOI18N
        } else {
            sb.append(NbBundle.getMessage(DetectPanel.class, "Msg_DetectPanel_None")); //NOI18N
        }
    }
    
    private String browse(final String title) {
        final String oldValue = fileChooserValue;
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return (f.exists() && f.canRead() && (f.isDirectory() || (f.getName().endsWith(".zip") || f.getName().endsWith(".jar")))); //NOI18N
            }
            
            public String getDescription() {
                return NbBundle.getMessage(DetectPanel.class, "TXT_ZipFilter"); // NOI18N
            }
        });
        if (oldValue != null)
            chooser.setSelectedFile(new File(oldValue));
        chooser.setDialogTitle(title);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileChooserValue = chooser.getSelectedFile().getAbsolutePath();
            return fileChooserValue;
        }
        return null;
    }
    
    private class PlatformDescriptor {
        
        final private String root;
        private boolean probed;
        private J2MEPlatform platform;
        private boolean selected;
        
        public PlatformDescriptor(File root) {
            this.root = root.getAbsolutePath();
        }
        
        public String getRoot() {
            return root;
        }
        
        public void detect() {
            synchronized (this) {
                platform = null;
                final Iterator<? extends CustomCLDCPlatformConfigurator> it =  Lookup.getDefault().lookup(new Lookup.Template<CustomCLDCPlatformConfigurator>(CustomCLDCPlatformConfigurator.class)).allInstances().iterator();
                final File rootDir = new File(root);
                while (it.hasNext() && platform == null) {
                    platform = createPlatformFromDescriptor(it.next().getPlatform(rootDir));
                }
                if (platform == null) platform = new UEIEmulatorConfiguratorImpl(root).getPlatform();
                selected = platform != null && !isAlreadyInstalled();
                probed = true;
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    descriptorUpdated();
                }
            });
        }
        
        private J2MEPlatform createPlatformFromDescriptor(final CLDCPlatformDescriptor desc) {
            if (desc == null) return null;
            return new J2MEPlatform(J2MEPlatform.computeUniqueName(desc.displayName), desc.home, desc.type, desc.displayName, desc.srcPath, desc.docPath, desc.preverifyCmd, desc.runCmd, desc.debugCmd, createDevices(desc.devices));
        }
        
        private J2MEPlatform.Device[] createDevices(final java.util.List<CLDCPlatformDescriptor.Device> list) {
            J2MEPlatform.Device[] devices = new J2MEPlatform.Device[list.size()];
            for (int i=0; i<devices.length; i++) {
                final CLDCPlatformDescriptor.Device d = list.get(i);
                devices[i] = new J2MEPlatform.Device(d.name, d.description, d.securityDomains == null ? null : (String[])d.securityDomains.toArray(new String[d.securityDomains.size()]), createProfiles(d.profiles), createScreen(d.screen));
            }
            return devices;
        }
        
        private J2MEPlatform.J2MEProfile[] createProfiles(final java.util.List<CLDCPlatformDescriptor.Profile> list) {
            J2MEPlatform.J2MEProfile[] profiles = new J2MEPlatform.J2MEProfile[list.size()];
            for (int i=0; i<profiles.length; i++) {
                final CLDCPlatformDescriptor.Profile p = list.get(i);
                profiles[i] = new J2MEPlatform.J2MEProfile(p.name, p.version, p.displayName, getProfileType(p.type), p.dependencies, p.classPath, p.def);
            }
            return profiles;
        }
        
        private String getProfileType(final CLDCPlatformDescriptor.ProfileType type) {
            if (type == CLDCPlatformDescriptor.ProfileType.Configuration) return J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION;
            if (type == CLDCPlatformDescriptor.ProfileType.Profile) return J2MEPlatform.J2MEProfile.TYPE_PROFILE;
            return J2MEPlatform.J2MEProfile.TYPE_OPTIONAL;
        }
        
        private J2MEPlatform.Screen createScreen(final CLDCPlatformDescriptor.Screen s) {
            return new J2MEPlatform.Screen(String.valueOf(s.width), String.valueOf(s.height), String.valueOf(s.bitDepth), String.valueOf(s.color), String.valueOf(s.touch));
        }
        
        public boolean isAlreadyInstalled() {
            return platform == null ? false : JavaPlatformManager.getDefault().getPlatforms(platform.getDisplayName(), null).length > 0;
        }
        
        public boolean isNameCollision() {
            if (platform == null || !selected) return false;
            if (isAlreadyInstalled()) return true;
            final Object descs[] = platformsListModel.toArray();
            final String name = getPlatform().getDisplayName();
            for (int i=0; i<descs.length; i++) {
                final PlatformDescriptor pd = (PlatformDescriptor)descs[i];
                if (pd != this && pd.isSelected() && pd.getPlatform() != null && pd.getPlatform().getDisplayName().equals(name)) return true;
            }
            return false;
        }
        
        public boolean isProbed() {
            return probed;
        }
        
        public boolean isSelected() {
            return selected;
        }
        
        public J2MEPlatform getPlatform() {
            return platform;
        }
        
        public boolean isNameValid() {
            return platform == null ? true : !selected || platform.getDisplayName().trim().length() > 0;
        }
        
        
        @SuppressWarnings("synthetic-access")
		public void updateRenderer(final JCheckBox renderer, final boolean isSelected) {
            renderer.setSelected(selected);
            if (! probed) {
                renderer.setText(root + DETECTION_IN_PROGRESS); //NOI18N
                renderer.setForeground(isSelected ? platformsList.getSelectionForeground() : COLOR_GRAY);
            } else if (platform != null) {
                if (!isNameValid()) {
                    renderer.setText(root + INVALID_NAME); //NOI18N
                    renderer.setForeground(isSelected ? platformsList.getSelectionForeground() : COLOR_RED);
                } else if (isNameCollision()) {
                    renderer.setText(root + NAME_COLLISION); //NOI18N
                    renderer.setForeground(isSelected ? platformsList.getSelectionForeground() : COLOR_RED);
                } else if (isAlreadyInstalled()) {
                    renderer.setText(root + ALREADY_INSTALLED); //NOI18N
                    renderer.setForeground(isSelected ? platformsList.getSelectionForeground() : platformsList.getForeground());
                } else {
                    renderer.setText(root); //NOI18N
                    renderer.setForeground(isSelected ? platformsList.getSelectionForeground() : platformsList.getForeground());
                }
            } else {
                renderer.setText(root + DETECTION_FAILED); //NOI18N
                renderer.setForeground(isSelected ? platformsList.getSelectionForeground() : COLOR_RED);
            }
        }
        
        public void invertSelection() {
            synchronized (this) {
                if (selected) {
                    selected = false;
                    descriptorUpdated();
                } else if (probed  &&  platform != null) {
                    selected = true;
                    descriptorUpdated();
                }
            }
        }
        
        public boolean equals(final Object obj) {
            return obj instanceof PlatformDescriptor && ((PlatformDescriptor)obj).root.equals(this.root);
        }
        
    }
    
    private class PlatformsRenderer extends JCheckBox implements ListCellRenderer {
        
        Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        
        public PlatformsRenderer() {
            setOpaque(true);
        }
        
        @SuppressWarnings("synthetic-access")
		public Component getListCellRendererComponent(@SuppressWarnings("unused")
		final JList list, final Object value, @SuppressWarnings("unused")
		final int index, final boolean isSelected, final boolean cellHasFocus) {
            setBackground(isSelected ? platformsList.getSelectionBackground() : platformsList.getBackground());
            Border border = null;
            if (cellHasFocus)
                border = UIManager.getBorder("List.focusCellHighlightBorder"); //NOI18N
            setBorder(border != null ? border : emptyBorder);
            final PlatformDescriptor descriptor = (PlatformDescriptor) value;
            descriptor.updateRenderer(this, isSelected);
            return this;
        }
        
    }
    
    private class CheckListener implements MouseListener, KeyListener {
        
        CheckListener() {
            // To avoid creation of accessor class
        }
        
        public void mouseClicked(final MouseEvent e) {
            if (e.getX() < 20)
                check();
        }
        
        public void mouseEntered(@SuppressWarnings("unused")
		final MouseEvent e) {
        }
        
        public void mouseExited(@SuppressWarnings("unused")
		final MouseEvent e) {
        }
        
        public void mousePressed(@SuppressWarnings("unused")
		final MouseEvent e) {
        }
        
        public void mouseReleased(@SuppressWarnings("unused")
		final MouseEvent e) {
        }
        
        public void keyPressed(final KeyEvent e) {
            if (e.getKeyChar() == ' ')
                check();
        }
        
        public void keyReleased(@SuppressWarnings("unused")
		final KeyEvent e) {
        }
        
        public void keyTyped(@SuppressWarnings("unused")
		final KeyEvent e) {
        }
        
        private void check() {
            final PlatformDescriptor descriptor = findSelectedPlatformDescriptor();
            if (descriptor != null)
                descriptor.invertSelection();
        }
        
    }
    
    private class NameFieldListener implements DocumentListener {
        
        NameFieldListener() {
            // To avoid creation of accessor class
        }
        
        public void changedUpdate(@SuppressWarnings("unused")
		final DocumentEvent e) {
            update();
        }
        
        public void insertUpdate(@SuppressWarnings("unused")
		final DocumentEvent e) {
            update();
        }
        
        public void removeUpdate(@SuppressWarnings("unused")
		final DocumentEvent e) {
            update();
        }
        
        @SuppressWarnings("synthetic-access")
		private void update() {
            final PlatformDescriptor descriptor = findSelectedPlatformDescriptor();
            final J2MEPlatform platform = descriptor.getPlatform();
            if (platform == null)
                return;
            final String text = nameField.getText();
            platform.setDisplayName(text);
            platform.setName(J2MEPlatform.computeUniqueName(text));
            platformsList.repaint();
            wizardPanel.fireChanged();
        }
        
    }
    
    private class FoldersRenderer extends JLabel implements ListCellRenderer {
        
        Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        
        public FoldersRenderer() {
            setOpaque(true);
        }
        
        @SuppressWarnings("synthetic-access")
		public Component getListCellRendererComponent(@SuppressWarnings("unused")
		final JList list, final Object value, @SuppressWarnings("unused")
		final int index, final boolean isSelected, final boolean cellHasFocus) {
            setBackground(isSelected ? platformsList.getSelectionBackground() : platformsList.getBackground());
            setForeground(isSelected ? platformsList.getSelectionForeground() : platformsList.getForeground());
            Border border = null;
            if (cellHasFocus)
                border = UIManager.getBorder("List.focusCellHighlightBorder"); // NOI18N
            setBorder(border != null ? border : emptyBorder);
            
            String text;
            if (value instanceof FileObject)
                text = J2MEPlatform.getFilePath((FileObject) value);
            else if (value instanceof URL)
                text = J2MEPlatform.getFilePath(URLMapper.findFileObject((URL) value));
            else
                text = null;
            if (text == null)
                text = value != null ? value.toString() : ""; // NOI18N
            setText(text);
            
            return this;
        }
        
    }
    
    class DetectRunnable extends JPanel implements Runnable {
        
        final private JLabel searchLabel;
        final protected ProgressHandle progress;
        
        public DetectRunnable() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc;
            searchLabel = new JLabel();
            gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(12, 12, 6, 12), 0, 0);
            add(searchLabel, gbc);
            gbc = new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 12, 6, 12), 0, 0);
            progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(SearchRunnable.class, "Title_DetectRunnable_Searching")); //NOI18N
            add(ProgressHandleFactory.createProgressComponent(progress), gbc);
            setPreferredSize(new Dimension(400, 70));
        }
        
        public void run() {
            progress.start(platformsListModel.size());
            final Dialog[] dialog = new Dialog[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        dialog[0] = DialogDisplayer.getDefault().createDialog(new DialogDescriptor(DetectRunnable.this, NbBundle.getMessage(SearchRunnable.class, "Title_DetectRunnable_Searching"), true, new Object[]{
                            NotifyDescriptor.CANCEL_OPTION,
                        }, NotifyDescriptor.CANCEL_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(DetectPanel.class), new ActionListener() {
                            public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
                                stop = true;
                            }
                        }));
                    }
                });
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (! stop)
                            dialog[0].setVisible(true);
                    }
                });
                
                for (int i=0; !stop&&i<platformsListModel.size(); i++) {
                    progress.progress(i);
                    final PlatformDescriptor pd = (PlatformDescriptor)platformsListModel.getElementAt(i);
                    if (!pd.isProbed()) {
                        setCurrentPath(pd.getRoot());
                        pd.detect();
                        wizardPanel.fireChanged();
                    }
                }
                progress.progress(platformsListModel.size());
                if (stop) showError(null);
                stop = true;
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        progress.finish();
                        dialog[0].setVisible(false);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace(); // TODO
            } catch (InvocationTargetException e) {
                e.printStackTrace(); // TODO
            }
        }
        
        public void setCurrentPath(final String platform) {
            searchLabel.setText(NbBundle.getMessage(SearchRunnable.class, "LBL_DetectingPath", platform)); // NOI18N
        }
        
    }
    
}
