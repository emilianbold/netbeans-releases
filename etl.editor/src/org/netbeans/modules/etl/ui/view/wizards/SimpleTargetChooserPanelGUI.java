/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.netbeans.modules.etl.ui.view.wizards;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import java.awt.Component;
import java.awt.event.ActionListener;

import java.io.File;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.logger.LogUtil;


/**
 * DOCUMENT ME!
 *
 * @author phrebejk
 */
public class SimpleTargetChooserPanelGUI extends javax.swing.JPanel implements ActionListener,
    DocumentListener {
    /** prefered dimmension of the panels */
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension(500, 340);
    String nbBundle20 = mLoc.t("PRSR001: new");
    String nbBundle28 = mLoc.t("PRSR001: Browse...");
    private final String NEW_FILE_PREFIX = Localizer.parse(nbBundle20); // NOI18N
    private final ListCellRenderer CELL_RENDERER = new GroupCellRenderer();
    private Project project;
    private String expectedExtension;
    private final List /*<ChangeListener>*/ listeners = new ArrayList();
    private SourceGroup[] folders;
    private boolean isFolder;

    // Variables declaration - do not modify                     
    private javax.swing.JPanel bottomPanelContainer;
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JSeparator targetSeparator;

    private static transient final Logger mLogger = LogUtil.getLogger(SimpleTargetChooserPanelGUI.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    /**
     * Creates new form SimpleTargetChooserGUI
     *
     * @param project DOCUMENT ME!
     * @param folders DOCUMENT ME!
     * @param bottomPanel DOCUMENT ME!
     * @param isFolder DOCUMENT ME!
     */
    public SimpleTargetChooserPanelGUI(
        Project project, SourceGroup[] folders, Component bottomPanel, boolean isFolder
    ) {
        this.project = project;
        this.folders = folders;
        this.isFolder = isFolder;
        initComponents();

        locationComboBox.setRenderer(CELL_RENDERER);

        if (bottomPanel != null) {
            bottomPanelContainer.add(bottomPanel, java.awt.BorderLayout.CENTER);
        }

        initValues(null, null);

        browseButton.addActionListener(this);
        locationComboBox.addActionListener(this);
        documentNameTextField.getDocument().addDocumentListener(this);
        folderTextField.getDocument().addDocumentListener(this);
        String nbBundle1 = mLoc.t("PRSR001: Name and Location");
        setName(
           Localizer.parse(nbBundle1)
        ); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param template DOCUMENT ME!
     * @param preselectedFolder DOCUMENT ME!
     */
    public void initValues(FileObject template, FileObject preselectedFolder) {
        assert project != null;

        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

        Sources sources = ProjectUtils.getSources(project);

        folders = sources.getSourceGroups(
                Sources.TYPE_GENERIC
            );

        if ((folders == null) || (folders.length < 1)) {
            folders = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }

        if (folders.length < 2) {
            // one source group i.e. hide Location
            locationLabel.setVisible(false);
            locationComboBox.setVisible(false);
        } else {
            // more source groups user needs to select location
            locationLabel.setVisible(true);
            locationComboBox.setVisible(true);
        }

        locationComboBox.setModel(new DefaultComboBoxModel(folders));

        // Guess the group we want to create the file in
        SourceGroup preselectedGroup = getPreselectedGroup(folders, preselectedFolder);
        locationComboBox.setSelectedItem(preselectedGroup);

        // Create OS dependent relative name
        /*folderTextField.setText(
            getRelativeNativeName(preselectedGroup.getRootFolder(), preselectedFolder)
        );*/
        folderTextField.setText("collaborations");

        String ext = (template == null) ? "" : template.getExt(); // NOI18N
        expectedExtension = (ext.length() == 0) ? "" : ("." + ext); // NOI18N

        String displayName = null;

        try {
            if (template != null) {
                DataObject templateDo = DataObject.find(template);
                displayName = templateDo.getNodeDelegate().getDisplayName();
            }
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName();
        }

        putClientProperty("NewEtlWizard_Title", displayName); // NOI18N

        if (template != null) {
            documentNameTextField.setText(NEW_FILE_PREFIX + template.getName());
            documentNameTextField.selectAll();
        }
        String nbBundle2 = mLoc.t("PRSR001: Folder Name:");
        if (isFolder) {
            jLabel3.setText(
               Localizer.parse(nbBundle2)
            ); // NOI18N
            String nbBundle3 = mLoc.t("PRSR001: N");
            jLabel3.setDisplayedMnemonic(
              Localizer.parse(nbBundle3).charAt(0)
            ); // NOI18N
            String nbBundle4 = mLoc.t("PRSR001: Parent Folder:");
            jLabel2.setText(
              Localizer.parse(nbBundle4)
            ); // NOI18N
            String nbBundle5 = mLoc.t("PRSR001: r");
            jLabel2.setDisplayedMnemonic(
               Localizer.parse(nbBundle5).charAt(0)
            ); // NOI18N
            String nbBundle6 = mLoc.t("PRSR001: Created Folder:");
            jLabel4.setText(
               Localizer.parse(nbBundle6)
            ); // NOI18N
            String nbBundle7 = mLoc.t("PRSR001: C");
            jLabel4.setDisplayedMnemonic(
                Localizer.parse(nbBundle7).charAt(0)
            ); // NOI18N
        } else {
            String nbBundle8 = mLoc.t("PRSR001: File Name:");
            jLabel3.setText(
              Localizer.parse(nbBundle8)
            ); // NOI18N
            String nbBundle9 = mLoc.t("PRSR001: Folder:");
            jLabel2.setText(
              Localizer.parse(nbBundle9)
            ); // NOI18N
            String nbBundle10 = mLoc.t("PRSR001: Created File:");
            jLabel4.setText(
               Localizer.parse(nbBundle10)
            ); // NOI18N
            String nbBundle11 = mLoc.t("PRSR001: N");
            jLabel3.setDisplayedMnemonic(
                Localizer.parse(nbBundle11).charAt(0)
            ); // NOI18N
            String nbBundle12 = mLoc.t("PRSR001: l");
            jLabel2.setDisplayedMnemonic(
              Localizer.parse(nbBundle12).charAt(0)
            ); // NOI18N
            String nbBundle13 = mLoc.t("PRSR001: C");
            jLabel4.setDisplayedMnemonic(
             Localizer.parse(nbBundle13).charAt(0)
            ); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SourceGroup getTargetGroup() {
        return (SourceGroup) locationComboBox.getSelectedItem();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getTargetFolder() {
        String folderName = folderTextField.getText().trim();

        if (folderName.length() == 0) {
            return null;
        } else {
            return folderName.replace(File.separatorChar, '/'); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getTargetName() {
        String text = documentNameTextField.getText().trim();

        if (text.length() == 0) {
            return null;
        } else {
            return text;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List templist;

        synchronized (this) {
            templist = new ArrayList(listeners);
        }

        Iterator it = templist.iterator();

        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(e);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        browseButton.setMnemonic(Localizer.parse(nbBundle28).charAt(0));
        browseButton.getAccessibleContext().setAccessibleName(Localizer.parse(nbBundle28));
        jLabel4 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());
        
        String nbBundle14 = mLoc.t("PRSR001: N/A");
        getAccessibleContext().setAccessibleDescription(
           Localizer.parse(nbBundle14)
        );
        jPanel1.setLayout(new java.awt.GridBagLayout());

        String nbBundle15 = mLoc.t("PRSR001: N");
        jLabel3.setDisplayedMnemonic(
            Localizer.parse(nbBundle15).charAt(0)
        );
        jLabel3.setLabelFor(documentNameTextField);
        String nbBundle16 = mLoc.t("PRSR001: File Name:");
        jLabel3.setText(
           Localizer.parse(nbBundle16)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(documentNameTextField, gridBagConstraints);
        String nbBundle17 = mLoc.t("PRSR001: N/A");
        documentNameTextField.getAccessibleContext().setAccessibleDescription(
           Localizer.parse(nbBundle17)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        add(jPanel1, gridBagConstraints);

        String nbBundle18 = mLoc.t("PRSR001: P");
        jLabel1.setDisplayedMnemonic(
            Localizer.parse(nbBundle18).charAt(0)
        );
        String nbBundle19 = mLoc.t("PRSR001: Project:");
        jLabel1.setText(
          Localizer.parse(nbBundle19)
        );
        jLabel1.setLabelFor(projectTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel1, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectTextField, gridBagConstraints);
        String nbBundle20 = mLoc.t("PRSR001: N/A");
        projectTextField.getAccessibleContext().setAccessibleDescription(
          Localizer.parse(nbBundle20)
        );

        String nbBundle21 = mLoc.t("PRSR001: t");
        locationLabel.setDisplayedMnemonic(
           Localizer.parse(nbBundle21).charAt(0)
        );
        locationLabel.setLabelFor(locationComboBox);
        String nbBundle22 = mLoc.t("PRSR001: Location:");
        locationLabel.setText(
           Localizer.parse(nbBundle22)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(locationLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(locationComboBox, gridBagConstraints);
        String nbBundle23 = mLoc.t("PRSR001: N/A");
        locationComboBox.getAccessibleContext().setAccessibleDescription(
            Localizer.parse(nbBundle23)
        );

        String nbBundle24 = mLoc.t("PRSR001: r");
        jLabel2.setDisplayedMnemonic(
           Localizer.parse(nbBundle24).charAt(0)
        );
        jLabel2.setLabelFor(folderTextField);
        String nbBundle25 = mLoc.t("PRSR001: Folder:");
        jLabel2.setText(
           Localizer.parse(nbBundle25)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(folderTextField, gridBagConstraints);
        String nbBundle26 = mLoc.t("PRSR001: N/A");
        folderTextField.getAccessibleContext().setAccessibleDescription(
          Localizer.parse(nbBundle26)
        );

        String nbBundle27 = mLoc.t("PRSR001: w");
        browseButton.setMnemonic(
            Localizer.parse(nbBundle27).charAt(0)
        );

        browseButton.setText(
           Localizer.parse(nbBundle28)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(browseButton, gridBagConstraints);
        String nbBundle29 = mLoc.t("PRSR001: N/A");
        browseButton.getAccessibleContext().setAccessibleDescription(
          Localizer.parse(nbBundle29)
        );

        String nbBundle30= mLoc.t("PRSR001: C");
        jLabel4.setDisplayedMnemonic(
           Localizer.parse(nbBundle30).charAt(0)
        );
        String nbBundle31= mLoc.t("PRSR001: Created File:");
        jLabel4.setText(
            Localizer.parse(nbBundle31)
        );
        jLabel4.setLabelFor(fileTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel4, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(fileTextField, gridBagConstraints);
        String nbBundle32= mLoc.t("PRSR001: N/A");
        fileTextField.getAccessibleContext().setAccessibleDescription(
           Localizer.parse(nbBundle32)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(targetSeparator, gridBagConstraints);

        bottomPanelContainer.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(bottomPanelContainer, gridBagConstraints);
    }//GEN-END:initComponents

    // End of variables declaration                   
    private SourceGroup getPreselectedGroup(SourceGroup[] groups, FileObject folder) {
        for (int i = 0; (folder != null) && (i < groups.length); i++) {
            if (FileUtil.isParentOf(groups[i].getRootFolder(), folder)) {
                return groups[i];
            }
        }

        return groups[0];
    }
    
    public void setDocumentName(String documentName) {
        documentNameTextField.setText(documentName);
    }

    private String getRelativeNativeName(FileObject root, FileObject folder) {
        if (root == null) {
            throw new NullPointerException("null root passed to getRelativeNativeName"); // NOI18N
        }

        String path;

        if (folder == null) {
            path = ""; // NOI18N
        } else {
            path = FileUtil.getRelativePath(root, folder);
        }

        return (path == null) ? "" : path.replace('/', File.separatorChar); // NOI18N
    }

    private void updateCreatedFolder() {
        FileObject root = ((SourceGroup) locationComboBox.getSelectedItem()).getRootFolder();

        String folderName = folderTextField.getText().trim();
        String documentName = documentNameTextField.getText().trim();

        String createdFileName = FileUtil.getFileDisplayName(root) +
            /*
               ( mFolderText.startsWith("/") || mFolderText.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
               mFolderText +
             */
            ((folderName.startsWith("/") || folderName.startsWith(File.separator)) ? "" : "/") + // NOI18N
            folderName +
            (
                (
                    folderName.endsWith("/") || folderName.endsWith(File.separator) ||
                    (folderName.length() == 0)
                ) ? "" : "/"
            ) + // NOI18N
            documentName + expectedExtension;

        fileTextField.setText(createdFileName.replace('/', File.separatorChar)); // NOI18N

        fireChange();
    }

    // ActionListener implementation -------------------------------------------
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (browseButton == e.getSource()) {
            FileObject fo = null;

            // Show the browse dialog
            SourceGroup group = (SourceGroup) locationComboBox.getSelectedItem();

            fo = BrowseFolders.showDialog(
                    new SourceGroup[] {group}, project,
                    folderTextField.getText().replace(File.separatorChar, '/')
                ); // NOI18N

            if ((fo != null) && fo.isFolder()) {
                String relPath = FileUtil.getRelativePath(group.getRootFolder(), fo);
                folderTextField.setText(relPath.replace('/', File.separatorChar)); // NOI18N
            }
        } else if (locationComboBox == e.getSource()) {
            updateCreatedFolder();
        }
    }

    // DocumentListener implementation -----------------------------------------
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFolder();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFolder();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFolder();
    }

    // Rendering of the location combo box -------------------------------------
    private class GroupCellRenderer extends JLabel implements ListCellRenderer {
        /**
         * Creates a new GroupCellRenderer object.
         */
        public GroupCellRenderer() {
            setOpaque(true);
        }

        /**
         * DOCUMENT ME!
         *
         * @param list DOCUMENT ME!
         * @param value DOCUMENT ME!
         * @param index DOCUMENT ME!
         * @param isSelected DOCUMENT ME!
         * @param cellHasFocus DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected, boolean cellHasFocus
        ) {
            if (value instanceof SourceGroup) {
                SourceGroup group = (SourceGroup) value;
                String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
                String groupDisplayName = group.getDisplayName();

                if (projectDisplayName.equals(groupDisplayName)) {
                    setText(groupDisplayName);
                } else {
                    String nbBundle33 = mLoc.t("PRSR001: {0} [{1}]",groupDisplayName,projectDisplayName);
                    setText(
                        MessageFormat.format(
                            Localizer.parse(nbBundle33), // NOI18N
                            new Object[] {groupDisplayName, projectDisplayName}
                        )
                    );
                }

                setIcon(group.getIcon(false));
            } else {
                setText(value.toString());
                setIcon(null);
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }
    }
}
