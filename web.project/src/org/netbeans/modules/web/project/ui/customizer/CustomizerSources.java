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

package org.netbeans.modules.web.project.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableColumn;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.modules.web.project.WebProject;

/**
 *
 * @author  tom, Radko Najman
 */
public class CustomizerSources extends javax.swing.JPanel implements HelpCtx.Provider {
    private String originalEncoding;
    private WebProjectProperties uiProperties;
    
    private File projectFld;
    
    public CustomizerSources( WebProjectProperties uiProperties ) {
        initComponents();
        jScrollPane1.getViewport().setBackground( sourceRoots.getBackground() );
        jScrollPane2.getViewport().setBackground( testRoots.getBackground() );
        
        sourceRoots.setModel( uiProperties.SOURCE_ROOTS_MODEL );
        testRoots.setModel( uiProperties.TEST_ROOTS_MODEL );
        sourceRoots.getTableHeader().setReorderingAllowed(false);
        testRoots.getTableHeader().setReorderingAllowed(false);
        
        FileObject projectFolder = uiProperties.getProject().getProjectDirectory();
        File pf = FileUtil.toFile( projectFolder );
        this.projectLocation.setText( pf == null ? "" : pf.getPath() ); // NOI18N
        this.projectFld = pf;
        
        jTextFieldWebPages.setDocument(uiProperties.WEB_DOCBASE_DIR_MODEL);
        webInfTextField.setDocument(uiProperties.WEBINF_DIR_MODEL);
        
        WebSourceRootsUi.EditMediator emSR = WebSourceRootsUi.registerEditMediator(
                (WebProject)uiProperties.getProject(),
                ((WebProject)uiProperties.getProject()).getSourceRoots(),
                sourceRoots,
                addSourceRoot,
                removeSourceRoot,
                upSourceRoot,
                downSourceRoot);
        
        WebSourceRootsUi.EditMediator emTSR = WebSourceRootsUi.registerEditMediator(
                (WebProject)uiProperties.getProject(),
                ((WebProject)uiProperties.getProject()).getTestSourceRoots(),
                testRoots,
                addTestRoot,
                removeTestRoot,
                upTestRoot,
                downTestRoot);
        
        emSR.setRelatedEditMediator( emTSR );
        emTSR.setRelatedEditMediator( emSR );
        
        this.jComboBoxSourceLevel.setModel(uiProperties.JAVAC_SOURCE_MODEL);
        uiProperties.JAVAC_SOURCE_MODEL.addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
                enableSourceLevel();
            }
            
            public void intervalRemoved(ListDataEvent e) {
                enableSourceLevel();
            }
            
            public void contentsChanged(ListDataEvent e) {
                enableSourceLevel();
            }
        });
        enableSourceLevel();
        
        this.originalEncoding = ((WebProject)uiProperties.getProject()).evaluator().getProperty(WebProjectProperties.SOURCE_ENCODING);
        if (this.originalEncoding == null) {
            this.originalEncoding = Charset.defaultCharset().name();
        }
        
        this.encoding.setModel(new EncodingModel(this.originalEncoding));
        this.encoding.setRenderer(new EncodingRenderer());
        
        
        this.encoding.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                handleEncodingChange();
            }
        });
        
        initTableVisualProperties(sourceRoots);
        initTableVisualProperties(testRoots);
        this.uiProperties = uiProperties;
    }
    
    private void initTableVisualProperties(JTable table) {
        //table.setGridColor(jTableCpC.getBackground());
        table.setRowHeight(testRoots.getRowHeight() + 4);
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
        column.setMinWidth(220);
        column.setWidth(220);
        column.setMinWidth(75);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerSources.class);
    }
    
    private void enableSourceLevel() {
        this.jComboBoxSourceLevel.setEnabled(jComboBoxSourceLevel.getItemCount()>0);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        jLabelWebPages = new javax.swing.JLabel();
        jTextFieldWebPages = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        webInfLabel = new javax.swing.JLabel();
        webInfTextField = new javax.swing.JTextField();
        webInfBrowseButton = new javax.swing.JButton();
        sourceRootsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourceRoots = new javax.swing.JTable();
        addSourceRoot = new javax.swing.JButton();
        removeSourceRoot = new javax.swing.JButton();
        upSourceRoot = new javax.swing.JButton();
        downSourceRoot = new javax.swing.JButton();
        testRootsPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        testRoots = new javax.swing.JTable();
        addTestRoot = new javax.swing.JButton();
        removeTestRoot = new javax.swing.JButton();
        upTestRoot = new javax.swing.JButton();
        downTestRoot = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabelSourceLevel = new javax.swing.JLabel();
        jComboBoxSourceLevel = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        encoding = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setDisplayedMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_ProjectFolder").charAt(0));
        jLabel1.setLabelFor(projectLocation);
        jLabel1.setText(NbBundle.getMessage(CustomizerSources.class, "CTL_ProjectFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(jLabel1, gridBagConstraints);

        projectLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(projectLocation, gridBagConstraints);
        projectLocation.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_projectLocation")); // NOI18N

        jLabelWebPages.setDisplayedMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_WebPages").charAt(0));
        jLabelWebPages.setLabelFor(jTextFieldWebPages);
        jLabelWebPages.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_WebPagesFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabelWebPages, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jTextFieldWebPages, gridBagConstraints);
        jTextFieldWebPages.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_webPagesFolder")); // NOI18N

        jButtonBrowse.setMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_WebPagesBrowse").charAt(0));
        jButtonBrowse.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_Browse_JButton")); // NOI18N
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 0);
        add(jButtonBrowse, gridBagConstraints);
        jButtonBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_webPagesFolderBrowse")); // NOI18N

        webInfLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("MNE_WebInf").charAt(0));
        webInfLabel.setLabelFor(webInfTextField);
        webInfLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_WebInfFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(webInfLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(webInfTextField, gridBagConstraints);

        webInfBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("MNE_WebInfBrowse").charAt(0));
        webInfBrowseButton.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_WebInf_Browse_JButton")); // NOI18N
        webInfBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webInfBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        add(webInfBrowseButton, gridBagConstraints);

        sourceRootsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setDisplayedMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_SourceRoots").charAt(0));
        jLabel2.setLabelFor(sourceRoots);
        jLabel2.setText(NbBundle.getMessage(CustomizerSources.class, "CTL_SourceRoots")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        sourceRootsPanel.add(jLabel2, gridBagConstraints);

        sourceRoots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Package Folder", "Label"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
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
        jScrollPane1.setViewportView(sourceRoots);
        sourceRoots.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_sourceRoots")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        sourceRootsPanel.add(jScrollPane1, gridBagConstraints);

        addSourceRoot.setMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_AddSourceRoot").charAt(0));
        addSourceRoot.setText(NbBundle.getMessage(CustomizerSources.class, "CTL_AddSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        sourceRootsPanel.add(addSourceRoot, gridBagConstraints);
        addSourceRoot.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_addSourceRoot")); // NOI18N

        removeSourceRoot.setMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_RemoveSourceRoot").charAt(0));
        removeSourceRoot.setText(NbBundle.getMessage(CustomizerSources.class, "CTL_RemoveSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        sourceRootsPanel.add(removeSourceRoot, gridBagConstraints);
        removeSourceRoot.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_removeSourceRoot")); // NOI18N

        upSourceRoot.setMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_UpSourceRoot").charAt(0));
        upSourceRoot.setText(NbBundle.getMessage(CustomizerSources.class, "CTL_UpSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        sourceRootsPanel.add(upSourceRoot, gridBagConstraints);
        upSourceRoot.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_upSourceRoot")); // NOI18N

        downSourceRoot.setMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_DownSourceRoot").charAt(0));
        downSourceRoot.setText(NbBundle.getMessage(CustomizerSources.class, "CTL_DownSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        sourceRootsPanel.add(downSourceRoot, gridBagConstraints);
        downSourceRoot.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_downSourceRoot")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(sourceRootsPanel, gridBagConstraints);

        testRootsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel3.setDisplayedMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_TestRoots").charAt(0));
        jLabel3.setLabelFor(testRoots);
        jLabel3.setText(NbBundle.getMessage(CustomizerSources.class, "CTL_TestRoots")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        testRootsPanel.add(jLabel3, gridBagConstraints);

        testRoots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Package Folder", "Label"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
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
        jScrollPane2.setViewportView(testRoots);
        testRoots.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_testRoots")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        testRootsPanel.add(jScrollPane2, gridBagConstraints);

        addTestRoot.setMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_AddTestRoot").charAt(0));
        addTestRoot.setText(NbBundle.getMessage(CustomizerSources.class, "CTL_AddTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        testRootsPanel.add(addTestRoot, gridBagConstraints);
        addTestRoot.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_addTestRoot")); // NOI18N

        removeTestRoot.setMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_RemoveTestRoot").charAt(0));
        removeTestRoot.setText(NbBundle.getMessage(CustomizerSources.class, "CTL_RemoveTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        testRootsPanel.add(removeTestRoot, gridBagConstraints);
        removeTestRoot.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_removeTestRoot")); // NOI18N

        upTestRoot.setMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_UpTestRoot").charAt(0));
        upTestRoot.setText(NbBundle.getMessage(CustomizerSources.class, "CTL_UpTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        testRootsPanel.add(upTestRoot, gridBagConstraints);
        upTestRoot.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_upTestRoot")); // NOI18N

        downTestRoot.setMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_DownTestRoot").charAt(0));
        downTestRoot.setText(NbBundle.getMessage(CustomizerSources.class, "CTL_DownTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        testRootsPanel.add(downTestRoot, gridBagConstraints);
        downTestRoot.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_downTestRoot")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(testRootsPanel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabelSourceLevel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "MNE_SourceLevel").charAt(0));
        jLabelSourceLevel.setLabelFor(jComboBoxSourceLevel);
        jLabelSourceLevel.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "TXT_SourceLevel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        jPanel1.add(jLabelSourceLevel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.75;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel1.add(jComboBoxSourceLevel, gridBagConstraints);
        jComboBoxSourceLevel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "AN_SourceLevel")); // NOI18N
        jComboBoxSourceLevel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "AD_SourceLevel")); // NOI18N

        jLabel5.setLabelFor(encoding);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "TXT_Encoding")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        jPanel1.add(jLabel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel1.add(encoding, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void webInfBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webInfBrowseButtonActionPerformed
        updateFolder(webInfTextField);
    }//GEN-LAST:event_webInfBrowseButtonActionPerformed
    
    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        updateFolder(jTextFieldWebPages);
    }//GEN-LAST:event_jButtonBrowseActionPerformed
    
    private void updateFolder(JTextField textField) {
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File fileName = new File(textField.getText());
        File folder = fileName.isAbsolute() ? fileName : new File(projectFld, fileName.getPath());
        if (folder.exists()) {
            chooser.setSelectedFile(folder);
        } else {
            chooser.setSelectedFile(projectFld);
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File selected = FileUtil.normalizeFile(chooser.getSelectedFile());
            String newFolder;
            if (CollocationQuery.areCollocated(projectFld, selected)) {
                newFolder = PropertyUtils.relativizeFile(projectFld, selected);
            } else {
                newFolder = selected.getPath();
            }
            textField.setText(newFolder);
        }
    }
    
    private void handleEncodingChange() {
        Charset enc = (Charset) encoding.getSelectedItem();
        String encName;
        if (enc != null) {
            encName = enc.name();
        } else {
            encName = originalEncoding;
        }
        this.uiProperties.putAdditionalProperty(WebProjectProperties.SOURCE_ENCODING, encName);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceRoot;
    private javax.swing.JButton addTestRoot;
    private javax.swing.JButton downSourceRoot;
    private javax.swing.JButton downTestRoot;
    private javax.swing.JComboBox encoding;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JComboBox jComboBoxSourceLevel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelSourceLevel;
    private javax.swing.JLabel jLabelWebPages;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextFieldWebPages;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JButton removeSourceRoot;
    private javax.swing.JButton removeTestRoot;
    private javax.swing.JTable sourceRoots;
    private javax.swing.JPanel sourceRootsPanel;
    private javax.swing.JTable testRoots;
    private javax.swing.JPanel testRootsPanel;
    private javax.swing.JButton upSourceRoot;
    private javax.swing.JButton upTestRoot;
    private javax.swing.JButton webInfBrowseButton;
    private javax.swing.JLabel webInfLabel;
    private javax.swing.JTextField webInfTextField;
    // End of variables declaration//GEN-END:variables
    
    private static class EncodingRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public EncodingRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            assert value instanceof Charset;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((Charset) value).displayName());
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
        
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
        
    }
    
    private static class EncodingModel extends DefaultComboBoxModel {
        
        public EncodingModel(String originalEncoding) {
            Charset defEnc = null;
            for (Charset c : Charset.availableCharsets().values()) {
                if (c.name().equals(originalEncoding)) {
                    defEnc = c;
                }
                addElement(c);
            }
            if (defEnc == null) {
                //Create artificial Charset to keep the original value
                //May happen when the project was set up on the platform
                //which supports more encodings
                try {
                    defEnc = new UnknownCharset(originalEncoding);
                    addElement(defEnc);
                } catch (java.nio.charset.IllegalCharsetNameException e) {
                    //The source.encoding property is completely broken
                    Logger.getLogger(this.getClass().getName()).info("IllegalCharsetName: " + originalEncoding);
                }
            }
            if (defEnc == null) {
                defEnc = Charset.defaultCharset();
            }
            setSelectedItem(defEnc);
        }
    }
    
    private static class UnknownCharset extends Charset {
        
        UnknownCharset(String name) {
            super(name, new String[0]);
        }
        
        public boolean contains(Charset c) {
            throw new UnsupportedOperationException();
        }
        
        public CharsetDecoder newDecoder() {
            throw new UnsupportedOperationException();
        }
        
        public CharsetEncoder newEncoder() {
            throw new UnsupportedOperationException();
        }
    }
}
