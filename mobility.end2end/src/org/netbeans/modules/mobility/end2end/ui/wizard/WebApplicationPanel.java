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

/*
 * WebApplicationPanel.java
 *
 * Created on August 3, 2005, 3:07 PM
 */
package org.netbeans.modules.mobility.end2end.ui.wizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.ComboBoxModel;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.mobility.end2end.classdata.AbstractService;
import org.netbeans.modules.mobility.end2end.classdata.ClassService;
import org.netbeans.modules.mobility.end2end.classdata.WSDLService;
import org.netbeans.modules.mobility.end2end.client.config.ClassDescriptor;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.client.config.ServerConfiguration;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientView;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Michal Skvor
 */
final public class WebApplicationPanel extends JPanel
        implements ChangeListener, ActionListener, DocumentListener {
    
    private Project enterpriseProject;
    
    WebProjectsActionListener webProjectsActionListener = new WebProjectsActionListener();
    
    private final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private static final String CLIENT_NAME = "WebToMobileServlet";
    
    private Project project;
    private ModelItem[] groupItems;
    private String expectedExtension;
    private boolean isPackage;
    private SourceGroup folders[];
    private String clientTypeSelection;
    
    private static String openProject =
            NbBundle.getMessage( WebApplicationPanel.class, "LBL_Open_WebProject" ); // NOI18N
    private static String noProject =
            NbBundle.getMessage( WebApplicationPanel.class, "LBL_No_WebProject" ); // NOI18N
    
    private static final String DEFAULT_NEW_PACKAGE_NAME =
            NbBundle.getMessage( WebApplicationPanel.class,
            "LBL_JavaTargetChooserPanelGUI_DefaultNewPackageName"); // NOI18N
    
    private static final ListCellRenderer CELL_RENDERER = new NodeCellRenderer();
    
    public static WebApplicationPanelWizard create() {
        return new WebApplicationPanelWizard();
    }
    
    /** Creates new form WebApplicationPanel */
    public WebApplicationPanel() {
        initComponents();
        
        setName( NbBundle.getMessage( WebApplicationPanel.class, "TITLE_clientTypeStep" ));
        
        setValues();
        
        clientTypeButtonGroup.add( clientToWebAppRadio );
        clientTypeButtonGroup.add( clientToWebServiceRadio );
        
        initAccessibility();
    }
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName( NbBundle.getMessage( WebApplicationPanel.class, "ACSN_Web_Application_Panel" ));
        getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( WebApplicationPanel.class, "ACSD_Web_Application_Panel" ));
        
        enterpriseProjectsComboBox.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( WebApplicationPanel.class, "ACSD_Web_Application_Panel" ));
        documentNameTextField.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( WebApplicationPanel.class, "ACSD_Servlet_Name" ));
        rootComboBox.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( WebApplicationPanel.class, "ACSD_Location" ));
        packageComboBox.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( WebApplicationPanel.class, "ACSD_Server_Package" ));
        fileTextField.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( WebApplicationPanel.class, "ACSD_Server_Created_File" ));
        
        clientToWebAppRadio.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( WebApplicationPanel.class, "ACSD_LBL_ClassClientType" ));
        clientToWebServiceRadio.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( WebApplicationPanel.class, "ACSD_WebServiceClientType" ));
        
    }
    
    private void setValues() {
        Sources sources = null;
        
        final DefaultComboBoxModel projectsModel = new DefaultComboBoxModel( getEnterpriseProjects());
        enterpriseProjectsComboBox.setModel( projectsModel );
        enterpriseProjectsComboBox.setRenderer( new ProjectCellRenderer());
        
        serviceCombo.setRenderer( new ServiceCellRenderer());
        
        if( projectsModel.getSize() == 0 ) {
            projectsModel.addElement( noProject );
            setServletPanelEnabled( false );
            updateWebServices(null);
        } else {
            if( enterpriseProject == null ) {
                enterpriseProject = (Project)projectsModel.getElementAt( 0 );
            }
            updateWebServices(enterpriseProject);
            enterpriseProjectsComboBox.setSelectedItem( enterpriseProject );
            sources = ProjectUtils.getSources( enterpriseProject );
            sources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA );
            
            rootComboBox.setRenderer( CELL_RENDERER );
            packageComboBox.setRenderer( CELL_RENDERER );
            rootComboBox.addActionListener( this );
            
            initValues( CLIENT_NAME, enterpriseProject ); // NOI18N
        }
        projectsModel.addElement( openProject );
        
        final Component packageEditor = packageComboBox.getEditor().getEditorComponent();
        ((javax.swing.JTextField)packageEditor).getDocument().addDocumentListener( this );
        packageComboBox.addActionListener( this );
        enterpriseProjectsComboBox.addActionListener( webProjectsActionListener );
        documentNameTextField.getDocument().addDocumentListener( this );
        
        clientToWebServiceRadio.addActionListener( this );
        clientToWebAppRadio.addActionListener( this );
        
        clientToWebServiceRadio.addChangeListener( this );
        clientToWebAppRadio.addChangeListener( this );
    }
    
    public void addChangeListener(final ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(final ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( ChangeListener cl : listeners) {
            cl.stateChanged(e);
        }
    }
    
    public void stateChanged(@SuppressWarnings("unused")
	final ChangeEvent e) {
        fireChange();
    }
    
    public void changedUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        //System.err.println("- text changed");
        updateText();
        fireChange();
    }
    
    public void insertUpdate(final javax.swing.event.DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void removeUpdate(final javax.swing.event.DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void actionPerformed(final java.awt.event.ActionEvent e) {
        if( rootComboBox == e.getSource()) {
            updatePackages();
            updateText();
        } else if( packageComboBox == e.getSource()) {
            updateText();
            fireChange();
        } else if( packageComboBox.getEditor() == e.getSource()) {
            updateText();
            fireChange();
        } else if( clientToWebAppRadio == e.getSource() ||
                clientToWebServiceRadio == e.getSource()) {
            
            clientTypeSelection = e.getActionCommand();
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

        clientTypeButtonGroup = new javax.swing.ButtonGroup();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        enterpriseProjectsComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        rootComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        clientToWebAppRadio = new javax.swing.JRadioButton();
        clientToWebServiceRadio = new javax.swing.JRadioButton();
        serviceCombo = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(560, 350));
        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/end2end/ui/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, bundle.getString("LBL_Title")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jLabel7, gridBagConstraints);

        jLabel1.setLabelFor(enterpriseProjectsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WebApplicationPanel.class, "LBL_Web_Application")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 1, 0, 0);
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(enterpriseProjectsComboBox, gridBagConstraints);

        jLabel2.setLabelFor(documentNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WebApplicationPanel.class, "LBL_Servlet_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 1, 0, 0);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 65;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(documentNameTextField, gridBagConstraints);

        jLabel3.setLabelFor(rootComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(WebApplicationPanel.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 1, 0, 0);
        add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(rootComboBox, gridBagConstraints);

        jLabel4.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(WebApplicationPanel.class, "LBL_Server_Package")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 1, 0, 0);
        add(jLabel4, gridBagConstraints);

        packageComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(packageComboBox, gridBagConstraints);

        jLabel5.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(WebApplicationPanel.class, "LBL_Server_Created_File")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 1, 0, 0);
        add(jLabel5, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 65;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(fileTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 189;
        gridBagConstraints.ipady = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 1, 0, 0);
        add(jSeparator1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(WebApplicationPanel.class, "LBL_ServiceTypeSelection")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 1, 0, 0);
        jPanel1.add(jLabel6, gridBagConstraints);

        clientTypeButtonGroup.add(clientToWebAppRadio);
        clientToWebAppRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(clientToWebAppRadio, org.openide.util.NbBundle.getMessage(WebApplicationPanel.class, "LBL_ClassClientType")); // NOI18N
        clientToWebAppRadio.setActionCommand("class");
        clientToWebAppRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clientToWebAppRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        jPanel1.add(clientToWebAppRadio, gridBagConstraints);

        clientTypeButtonGroup.add(clientToWebServiceRadio);
        org.openide.awt.Mnemonics.setLocalizedText(clientToWebServiceRadio, org.openide.util.NbBundle.getMessage(WebApplicationPanel.class, "LBL_WebServiceClientType")); // NOI18N
        clientToWebServiceRadio.setActionCommand("wsdlClient");
        clientToWebServiceRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clientToWebServiceRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        clientToWebServiceRadio.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                clientToWebServiceRadioStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 11, 5, 6);
        jPanel1.add(clientToWebServiceRadio, gridBagConstraints);

        serviceCombo.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(serviceCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void clientToWebServiceRadioStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_clientToWebServiceRadioStateChanged
        serviceCombo.setEnabled(clientToWebServiceRadio.isSelected());
    }//GEN-LAST:event_clientToWebServiceRadioStateChanged
    
    protected void enterpriseProjectsListValueChanged( final ActionEvent evt ) {
        enterpriseProjectsComboBox.removeActionListener(webProjectsActionListener);
        final JComboBox cb = (JComboBox) evt.getSource();
        final Object cbItem = cb.getSelectedItem();
        final DefaultComboBoxModel projectsModel = (DefaultComboBoxModel) cb.getModel();
        
        if( cbItem instanceof String ) {
            final String cbItemString = (String)cbItem;
            if( cbItemString.equals( openProject )) {
                final Project oldEntepriseProject = enterpriseProject;
                enterpriseProject = Util.openProject();
                if( enterpriseProject != null ) {
                    /* if noProject in the list - delete it */
                    if( projectsModel.getIndexOf( noProject ) >= 0 ) {
                        projectsModel.removeElement( noProject );
                    }
                    /* add the new opened project - if it is not in the list */
                    if( projectsModel.getIndexOf( enterpriseProject ) < 0 ) {
                        projectsModel.insertElementAt( enterpriseProject, projectsModel.getSize() - 1 );
                    }
                    updateWebServices(enterpriseProject);
                    cb.setSelectedItem( enterpriseProject );
                } else {
                    if( oldEntepriseProject != null ) {
                        cb.setSelectedItem( enterpriseProject = oldEntepriseProject );  //rollback
                    }
                }
            }
        } else {
            enterpriseProject = (Project)cbItem;
            updateWebServices(enterpriseProject);
        }
        if( enterpriseProject != null ) {
            
            final Sources sources = ProjectUtils.getSources( enterpriseProject );
            sources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA );
            setServletPanelEnabled( true );
        }
        if( enterpriseProject != null ) {
            initValues( CLIENT_NAME, enterpriseProject ); //NOI18N
            
        }
        enterpriseProjectsComboBox.addActionListener( webProjectsActionListener );
    }
    
    public void initValues( final String displayName, final Project project ) {
        final Sources sources = ProjectUtils.getSources( project );
        final SourceGroup[] sourceGroups = sources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA );
        this.project = project;
        this.folders = sourceGroups;
        initValues( displayName, project.getProjectDirectory());
    }
    
    public void initValues( final String displayName, final FileObject preselectedFolder ) {
        assert project != null : "Project must be specified."; // NOI18N
        
        // Create list of groups
        this.groupItems = new ModelItem[ folders.length ];
        for (int i = 0; i < folders.length; i++) {
            this.groupItems[i] = new ModelItem(folders[i]);
        }
        
        assert displayName != null;
        
        putClientProperty( "NewFileWizard_Title", "Servlet" );  // NOI18N
        // Setup comboboxes
        rootComboBox.setModel(new DefaultComboBoxModel( this.groupItems ));
        final ModelItem preselectedGroup = getPreselectedGroup( preselectedFolder );
        rootComboBox.setSelectedItem( preselectedGroup );
        updatePackages();
        final ModelItem preselectedPackage = getPreselectedPackage(preselectedGroup, preselectedFolder);
        if (preselectedPackage != null) {
            if (isPackage) {
                
                final String docName = preselectedPackage.toString().length() == 0 ?
                    DEFAULT_NEW_PACKAGE_NAME :
                    preselectedPackage.toString() + "." + DEFAULT_NEW_PACKAGE_NAME; // NOI18N
                
                documentNameTextField.setText(docName);
                final int docNameLen = docName.length();
                final int defPackageNameLen = DEFAULT_NEW_PACKAGE_NAME.length();
                
                documentNameTextField.setSelectionEnd(docNameLen - 1);
                documentNameTextField.setSelectionStart(docNameLen - defPackageNameLen);
            } else {
                packageComboBox.setSelectedItem(preselectedPackage);
            }
            
        }
        documentNameTextField.setText(displayName);
        documentNameTextField.selectAll();
        
        
        // Determine the extension
        final String ext = "java";// NOI18N
        expectedExtension = "." + ext; // NOI18N
        
        updateText();
        fireChange();
    }
    
    private Project[] getEnterpriseProjects() {
        final List<Project> enterpriseProjects = new ArrayList<Project>();
        final Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for( int i = 0; i < projects.length; i++ ) {
            //System.err.println(" + " + projects[i].getProjectDirectory().getName());
            if( Util.isWebProject( projects[i] )) {
                enterpriseProjects.add( projects[i] );
            }
            
        }
        // String[] ps = new String[enterpriseProjects.size()];
        // ps = (String[])enterpriseProjects.toArray(ps);
        Project[] ps = new Project[enterpriseProjects.size()];
        ps = enterpriseProjects.toArray( ps );
        Arrays.sort( ps, new ProjectByDisplayNameComparator());
        return ps;
    }
    
    private void setServletPanelEnabled( final boolean enabled ) {
        rootComboBox.setEnabled( enabled );
        packageComboBox.setEnabled( enabled );
        documentNameTextField.setEnabled( enabled );
    }
    
    private ModelItem getPreselectedGroup(final FileObject folder) {
        for (int i = 0; folder != null && i < groupItems.length; i++) {
            if (groupItems[i].group.getRootFolder().equals(folder) ||
                    FileUtil.isParentOf(groupItems[i].group.getRootFolder(), folder)) {
                return groupItems[i];
            }
        }
        return groupItems[0];
    }
    
    private ModelItem getPreselectedPackage( final ModelItem groupItem, final FileObject folder ) {
        if (folder == null) {
            return null;
        }
        final ModelItem ch[] = groupItem.getChildren();
        final FileObject root = groupItem.group.getRootFolder();
        
        String relPath = FileUtil.getRelativePath(root, folder);
        
        if (relPath == null) {
            // Group Root folder is no a parent of the preselected folder
            // No package should be selected
            return null;
        }
        relPath = relPath.replace('/', '.'); //NOI18N
        
        for (int i = 0; i < ch.length; i++) {
            if (ch[i].toString().equals(relPath)) {
                return ch[i];
            }
        }
        
        return null;
    }
    
    private void updatePackages() {
        packageComboBox.setModel(
                new DefaultComboBoxModel(((ModelItem) rootComboBox.getSelectedItem()).getChildren()));
    }
    
    private void updateText() {
        
        final ModelItem modelItem = (ModelItem) rootComboBox.getSelectedItem();
        final FileObject rootFolder = modelItem.group.getRootFolder();
        final String packageName = getPackageFileName();
        String documentName = documentNameTextField.getText().trim();
        if (isPackage) {
            documentName = documentName.replace('.', '/'); // NOI18N
        } else if (documentName.length() > 0) {
            documentName = documentName + expectedExtension;
        }
        final String createdFileName = FileUtil.getFileDisplayName(rootFolder) +
                (packageName.startsWith("/") || packageName.startsWith(File.separator) ? "" : "/") + // NOI18N
                packageName +
                (packageName.endsWith("/") || packageName.endsWith(File.separator) || packageName.length() == 0 ? "" : "/") + // NOI18N
                documentName;
        
        fileTextField.setText(createdFileName.replace('/', File.separatorChar)); // NOI18N
    }
    
    public String getPackageFileName() {
        
        if( isPackage ) {
            return ""; // NOI18N
        }
        
        final String packageName = packageComboBox.getEditor().getItem().toString();
        return packageName.replace('.', '/'); // NOI18N
    }
    
    public boolean isSelectedClientType() {
        return clientTypeButtonGroup.getSelection() != null;
    }
    
    public String getSelectedClientType() {
        return clientTypeSelection;
    }
    
    public String isValidWebProject() {
        String message = null;
        if (enterpriseProject == null)
            message = NbBundle.getMessage( WebApplicationPanel.class, "ERR_NoProject" ); // NOI18N
        else if (!Util.isWebProject(enterpriseProject))
            message = NbBundle.getMessage( WebApplicationPanel.class, "ERR_NoWebProject" ); // NOI18N
        return message;
    }
    
    boolean isValidWsdl(){
        return getSelectedService() != null;
    }
    
    boolean isWsdlCompiled(){
//        final FileObject fo = enterpriseProject.getProjectDirectory().getFileObject("build/generated/wsclient/"); //NOI18N
//        return fo != null;
        return true;
    }
    
    public String isValidServletLocation() {
        if( getTargetName() == null ) {
            return NbBundle.getMessage( WebApplicationPanel.class, "ERR_File_NoTargetName"); // NOI18N
        }
        
        if( !isValidTypeIdentifier( getTargetName())) {
            return NbBundle.getMessage( WebApplicationPanel.class, "ERR_JavaTargetChooser_InvalidClass" ); // NOI18N
        } else if( !isValidPackageName( getPackageName())) {
            return NbBundle.getMessage( WebApplicationPanel.class, "ERR_JavaTargetChooser_InvalidPackage" ); // NOI18N
            
        }
        
        final FileObject rootFolder = getRootFolder();
        final String errorMessage = canUseFileName(rootFolder, getPackageFileName(), getTargetName(), "java"); // NOI18N
        
        return errorMessage;
    }
    
    static boolean isValidPackageName(final String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        final StringTokenizer tukac = new StringTokenizer(str, "."); // NOI18N
        while (tukac.hasMoreTokens()) {
            final String token = tukac.nextToken();
            if ("".equals(token))
                return false;
            if (!Utilities.isJavaIdentifier(token))
                return false;
        }
        return true;
    }
    
    static boolean isValidTypeIdentifier(final String ident) {
        return !(ident == null || "".equals(ident) || !Utilities.isJavaIdentifier(ident)) ;
    }
    
    public FileObject getRootFolder() {
        return ((ModelItem) rootComboBox.getSelectedItem()).group.getRootFolder();
    }
    
    public SourceGroup getSourceGroup() {
        if (rootComboBox.getSelectedItem() == null){
            return null;
        }
        return ((ModelItem) rootComboBox.getSelectedItem()).group;
    }
    
    public String getProjectName() {
        if( enterpriseProjectsComboBox.getSelectedItem() instanceof String ) {
            return enterpriseProjectsComboBox.getSelectedItem().toString();
        } 
        final ProjectInformation pi = ((Project)enterpriseProjectsComboBox.getSelectedItem()).
                getLookup().lookup( ProjectInformation.class );
        return pi.getName();
    }
    
    public Project getEnterpriseProject(){
        if( enterpriseProjectsComboBox.getSelectedItem() instanceof String ) {
            return null;
        } 
        return (Project)enterpriseProjectsComboBox.getSelectedItem();
    }
    
    public String getPackageName() {
        if( isPackage ) {
            return ""; // NOI18N
        }
        
        return packageComboBox.getEditor().getItem().toString();
    }
    
    public String getTargetName() {
        final String text = documentNameTextField.getText().trim();
        
        if (text.length() == 0) {
            return null;
        }
        return text;
    }
    
    // helper methods copied from project/ui/ProjectUtilities
    /** Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */
    public static String canUseFileName(final FileObject targetFolder, final String folderName, String newObjectName, final String extension) {
        if (extension != null && extension.length() > 0) {
            final StringBuffer sb = new StringBuffer();
            sb.append(newObjectName);
            sb.append('.'); // NOI18N
            sb.append(extension);
            newObjectName = sb.toString();
        }
        
        final String relFileName = folderName + "/" + newObjectName; // NOI18N
        
        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return NbBundle.getMessage( WebApplicationPanel.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }
        
        // target filesystem should be writable
        if (!targetFolder.canWrite()) {
            return NbBundle.getMessage( WebApplicationPanel.class, "MSG_fs_is_readonly"); // NOI18N
        }
        
        
        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage( WebApplicationPanel.class, "MSG_file_already_exist", newObjectName); // NOI18N
        }
        
        // all ok
        return null;
    }
    
    private static boolean existFileName(final FileObject targetFolder, final String relFileName) {
        boolean result = false;
        final File fileForTargetFolder = FileUtil.toFile(targetFolder);
        if (fileForTargetFolder.exists()) {
            result = new File(fileForTargetFolder, relFileName).exists();
        } else {
            result = targetFolder.getFileObject(relFileName) != null;
        }
        
        return result;
    }
    
    protected void updateWebServices(final Project p){
        DefaultComboBoxModel servicesModel = null;
        if (p == null){
            servicesModel = new DefaultComboBoxModel(
                    new String[]{ NbBundle.getMessage( WebApplicationPanel.class, "ERR_NoWebServiceCombo")}); // NOI18N
        } else {
//            final WebServicesClientSupport wscs = WebServicesClientSupport.getWebServicesClientSupport(p.getProjectDirectory());            
//            final FileObject rootFolder = wscs.getWsdlFolder();
            JAXWSClientSupport jaxws = JAXWSClientSupport.getJaxWsClientSupport( p.getProjectDirectory());
            List wsclients = jaxws.getServiceClients();
            if( wsclients.size() > 0 ){
//                final WebServicesClientSupport clientSupport  = WebServicesClientSupport.getWebServicesClientSupport(rootFolder);
//                final WebServicesClientSupport clientSupport  = WebServicesClientSupport.getWebServicesClientSupport( p.getProjectDirectory());
                final JAXWSClientSupport jaxwsClientSupport = JAXWSClientSupport.getJaxWsClientSupport( p.getProjectDirectory());
//                final WebServicesClientView clientView = WebServicesClientView.getWebServicesClientView( p.getProjectDirectory());
//                final WebServicesClientView clientView = WebServicesClientView.getWebServicesClientView(rootFolder);
                final JAXWSClientView jaxwsClientView = JAXWSClientView.getJAXWSClientView();
//                if( clientSupport.getWsdlFolder().getChildren().length == 0 ){ //NO WS
//                    servicesModel = new DefaultComboBoxModel(
//                            new String[]{ NbBundle.getMessage( WebApplicationPanel.class, "ERR_NoWebServiceCombo")}); // NOI18N
//                } else {
                    final Node jaxwsClientRoot = jaxwsClientView.createJAXWSClientView( p );
//                    final Node clientRoot = clientView.createWebServiceClientView(clientSupport.getWsdlFolder());
                    final Node[] nodes = jaxwsClientRoot.getChildren().getNodes();
                    if( nodes.length == 0 ){
                        servicesModel = new DefaultComboBoxModel(
                                new String[]{ NbBundle.getMessage( WebApplicationPanel.class, "MSG_ComputingWebServices" )} );
                        RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                updateWebServices(p);
                            }
                        }, 500);
                    } else {
                        final List<Client> services = new ArrayList<Client>();
                        for( int i = 0; i < nodes.length; i++ ) {
                            Client client = nodes[i].getLookup().lookup( Client.class );
                            services.add( client );
                        }
                        servicesModel = new DefaultComboBoxModel( services.toArray( new Client[ services.size() ] ));                        
                        fireChange();
                    }
//                }
            } else {
                servicesModel = new DefaultComboBoxModel( new String[]{
                    NbBundle.getMessage( WebApplicationPanel.class, "ERR_NoWebServiceInProject" )} ); // NOI18N
            }
        }
        serviceCombo.setModel( servicesModel );
    }
        
    Client getSelectedService() {
        final Object o = serviceCombo.getSelectedItem();
        if( o instanceof Client ) {
            return (Client) o;
        }
        return null;
    }
    
    boolean isWsdl(){
        return clientToWebServiceRadio.isSelected();
    }
    /**
     * Comparator which compares names of projects depending on the locale
     *
     * @author Sigal Duek
     */
    public static class ProjectByDisplayNameComparator implements Comparator<Project> {
        
        private static Collator COLLATOR = Collator.getInstance();
        
        public int compare( final Project p1, final Project p2 ) {
            
            //            Uncoment to make the main project be the first one
            //            but then needs to listen to main project change
            //            if ( OpenProjectList.getDefault().isMainProject( p1 ) ) {
            //                return -1;
            //            }
            //
            //            if ( OpenProjectList.getDefault().isMainProject( p2 ) ) {
            //                return 1;
            //            }
            
            return COLLATOR.compare( ProjectUtils.getInformation( p1 ).getDisplayName(),
                    ProjectUtils.getInformation( p2 ).getDisplayName());
        }
    }    
    
    private class WebProjectsActionListener implements ActionListener {
        
        WebProjectsActionListener() {
            //to avoid creation of accessor class
        }
        
        public void actionPerformed( final ActionEvent evt ) {
            enterpriseProjectsListValueChanged( evt );
        }
    }
    
    private static class ProjectCellRenderer extends JLabel implements ListCellRenderer {
        
        public ProjectCellRenderer() {
            setOpaque( true );
        }
        
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                @SuppressWarnings("unused")
				final int index,
                final boolean isSelected,
                @SuppressWarnings("unused")
				final boolean cellHasFocus) {
            
            if (value instanceof Project) {
                final ProjectInformation pi = ProjectUtils.getInformation((Project) value);
                setText(pi.getDisplayName());
                setIcon(pi.getIcon());
            } else {
                setText(value == null ? "" : value.toString()); // NOI18N
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
    
    private static class ServiceCellRenderer extends JLabel implements ListCellRenderer {
        
        public ServiceCellRenderer() {
            setOpaque( true );
        }
        
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                @SuppressWarnings("unused")
				final int index,
                final boolean isSelected,
                @SuppressWarnings("unused")
				final boolean cellHasFocus) {
            
            if( value instanceof Client ) {
                final Client client = (Client) value;
                setText( client.getName());
                setIcon( null );
            } else if (value instanceof DataObject) {
                final DataObject doj = (DataObject) value;
                setText(doj.getNodeDelegate().getDisplayName());
                setIcon(new ImageIcon(doj.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)));
            } else {
                setText(value == null ? "" : value.toString()); // NOI18N
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
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton clientToWebAppRadio;
    private javax.swing.JRadioButton clientToWebServiceRadio;
    private javax.swing.ButtonGroup clientTypeButtonGroup;
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JComboBox enterpriseProjectsComboBox;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JComboBox rootComboBox;
    private javax.swing.JComboBox serviceCombo;
    // End of variables declaration//GEN-END:variables
    
    
    private static class ModelItem {
        
        private static final String DEFAULT_PACKAGE_DISPLAY_NAME =
                NbBundle.getMessage( WebApplicationPanel.class, "LBL_JavaTargetChooserPanelGUI_DefaultPackage" ); // NOI18N
        
        private Node node;
        SourceGroup group;
        final private Icon icon;
        private ModelItem[] children;
        
        // For source groups
        public ModelItem( SourceGroup group ) {
            this.group = group;
            this.icon = group.getIcon( false );
        }
        
        // For packages
        public ModelItem( Node node ) {
            this.node = node;
            this.icon = new ImageIcon( node.getIcon( java.beans.BeanInfo.ICON_COLOR_16x16 ));
        }
        
        public String getDisplayName() {
            if (group != null) {
                return group.getDisplayName();
            }
            final String nodeName = node.getName();
            return nodeName.length() == 0 ? DEFAULT_PACKAGE_DISPLAY_NAME : nodeName;
        }
        
        public Icon getIcon() {
            return icon;
        }
        
        public String toString() {
            if (group != null) {
                return getDisplayName();
            } 
            return node.getName();
        }
        
        public ModelItem[] getChildren() {
            if (group == null) {
                return null;
            } 
            if (children == null) {
                final Node n = PackageView.createPackageView(group);
                final Node nodes[] = n.getChildren().getNodes(true);
                children = new ModelItem[nodes.length];
                for (int i = 0; i < nodes.length; i++) {
                    children[i] = new ModelItem(nodes[i]);
                }
            }
            return children;
        }
        
    }
    
    public static class NodeCellRenderer extends JLabel implements ListCellRenderer {
        
        public NodeCellRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(final JList list, final Object value, @SuppressWarnings("unused")
		final int index, final boolean isSelected, @SuppressWarnings("unused")
		final boolean cellHasFocus) {
            if (value instanceof ModelItem) {
                final ModelItem item = (ModelItem) value;
                setText(item.getDisplayName());
                setIcon(item.getIcon());
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
    
    public static class WebApplicationPanelWizard implements TemplateWizard.Panel, ChangeListener {
        
        public static final HelpCtx HELP_CTX = new HelpCtx( "me.wcb_webappselection" );
        
        WebApplicationPanel gui;
        private final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
        
        private final SpecificationVersion JDK_14 = new SpecificationVersion("1.4");   //NOI18N
        
        private TemplateWizard templateWizard;
        
        public void addChangeListener( final ChangeListener changeListener ) {
            listeners.add( changeListener );
        }
        
        public void removeChangeListener( final ChangeListener changeListener ) {
            listeners.remove( changeListener );
        }
        
        private void fireChange() {
            final ChangeEvent e = new ChangeEvent(this);
            for ( ChangeListener cl : listeners ) {
                cl.stateChanged(e);
            }
        }
        
        public boolean isValid() {
            
            String guiMessage;
            if(( guiMessage = gui.isValidWebProject()) != null){
                setLocalizedErrorMessage(guiMessage); // NOI18N
                return false;
            } else if ((guiMessage = gui.isValidServletLocation()) != null ){
                setLocalizedErrorMessage(guiMessage); // NOI18N
                return false;
            }
            
            final boolean isPackage = false;
            if ( isPackage ) {
                if ( !WebApplicationPanel.isValidPackageName( gui.getTargetName() ) ) {
                    setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" ); // NOI18N
                    return false;
                }
            } else {
                if ( !WebApplicationPanel.isValidTypeIdentifier( gui.getTargetName() ) ) {
                    setErrorMessage( "ERR_JavaTargetChooser_InvalidClass" ); // NOI18N
                    return false;
                } else if ( !WebApplicationPanel.isValidPackageName( gui.getPackageName() ) ) {
                    setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" ); // NOI18N
                    return false;
                }
            }
            
            // check if the file name can be created
            // FileObject template = Templates.getTemplate( wizard );
            
            boolean returnValue = true;
            
            final FileObject rootFolder = gui.getRootFolder();
            final String errorMessage = WebApplicationPanel.canUseFileName(rootFolder, gui.getPackageFileName(), gui.getTargetName(), "java"); // NOI18N
            if (gui.isShowing()) {
                setLocalizedErrorMessage(errorMessage);
            }
            if (errorMessage!=null) returnValue=false;
            
            
            //Only warning, display it only if everything else id OK.
            final String sl = SourceLevelQuery.getSourceLevel(rootFolder);
            if (!isPackage && returnValue && gui.getPackageName().length() == 0 && sl != null && JDK_14.compareTo(new SpecificationVersion(sl))<=0) {
                setErrorMessage( "ERR_JavaTargetChooser_DefaultPackage" ); // NOI18N
            } else {
                if (returnValue) { //everything is valid
                    setErrorMessage(null);
                }
            }
            
            // Check if we selected something
            if( !gui.isSelectedClientType() && returnValue ) {
                setErrorMessage( "ERR_Unselected_Client_Type" ); // NOI18N
                returnValue = false;
            }
            
            if (gui.isWsdl() && !gui.isValidWsdl()){
                setErrorMessage( "ERR_NoWebService" ); // NOI18N
                returnValue = false;
            }
            
            if (gui.isWsdl() && !gui.isWsdlCompiled()){
                setErrorMessage( "ERR_WebServiceNotCompiled" ); // NOI18N
                returnValue = false;
            }
            
            return returnValue;
        }
        
        private void setErrorMessage( final String key ) {
            if ( key == null ) {
                setLocalizedErrorMessage( " " ); // NOI18N
            } else {
                setLocalizedErrorMessage( NbBundle.getMessage( WebApplicationPanel.class, key) ); // NOI18N
            }
        }
        
        private void setLocalizedErrorMessage(final String message) {
            if( templateWizard != null )
                templateWizard.putProperty("WizardPanel_errorMessage", message); // NOI18N
        }
        
        public void readSettings( final Object settings ) {
            templateWizard = (TemplateWizard)settings;
            // TODO remove
            getComponent();
            isValid();
            templateWizard.putProperty("NewFileWizard_Title", // NOI18N
                    NbBundle.getMessage(WebApplicationPanel.class, "LBL_WebServiceClient"));// NOI18N
        }
        
        public void storeSettings( final Object settings ) {
            templateWizard = (TemplateWizard)settings;
            
            // TODO remove
            getComponent();
            
            final Configuration configuration = (Configuration)templateWizard.
                    getProperty( GenericServiceIterator.PROP_CONFIGURATION );
            
            configuration.setServiceType( gui.getSelectedClientType());
            
            final ServerConfiguration sc = new ServerConfiguration();
            
            String className = gui.getPackageName();
            if( !"".equals( className )) {
                className += "." + gui.getTargetName(); // NOI18N
            } else {
                className = gui.getTargetName();
            }
            final Project project = gui.getEnterpriseProject();
            templateWizard.putProperty(GenericServiceIterator.PROP_SERVER_PROJECT, project);
            
            if ( gui.getSourceGroup() == null ) return; //no web project available
            
            final String path = gui.getSourceGroup().getName();
            final ClassDescriptor cd = new ClassDescriptor( className, path);
            sc.setClassDescriptor( cd );
            sc.setProjectName( gui.getProjectName());
            sc.setProperties( new Properties());
            
            configuration.setServerConfiguration( sc );
            
            final List<AbstractService> l = configuration.getServices();
            Object os = null;
            if ( l != null && l.size() > 0){
                os = l.get(0);
            }
            if (gui.isWsdl()){
                configuration.setServiceType(Configuration.WSDLCLASS_TYPE);
                WSDLService wsdlService = null;
                if ( os instanceof WSDLService ) { //is wsdl
                    wsdlService = (WSDLService)os;
                }
                if( gui.getSelectedService() != null ) {
//                    final String serviceName = gui.getSelectedService();
                    final Client client = gui.getSelectedService();
                    wsdlService = new WSDLService();
                    wsdlService.setName( client.getName());
                    wsdlService.setFile( client.getLocalWsdlFile());
                    wsdlService.setUrl( client.getWsdlUrl());
                    final List<AbstractService> services = new ArrayList<AbstractService>();
                    services.add( wsdlService );
                    configuration.setServices( services );
                }
//                if (gui.getSelectedService() != null){
//                    final DataObject doj = gui.getSelectedService();
//                    if ( wsdlService == null ||
//                            ( wsdlService.getFile() != null && !wsdlService.getFile().equals(doj.getPrimaryFile().getNameExt()))){
//                        wsdlService = new WSDLService();
//                        wsdlService.setName( doj.getName() );
//                        wsdlService.setFile( doj.getPrimaryFile().getNameExt() );
//                        final List<AbstractService> services = new ArrayList<AbstractService>();
//                        services.add(wsdlService);
//                        configuration.setServices(services);
//                    }
//                }
            } else {
                configuration.setServiceType(Configuration.CLASS_TYPE);
                final List<AbstractService> services = new ArrayList<AbstractService>();
                ClassService classService = null;
                if ( !(os instanceof ClassService)){
                    classService = new ClassService();
                    services.add(classService);
                    configuration.setServices(services);
                }
            }
        }
        
        public HelpCtx getHelp() {
            return HELP_CTX;
        }
        
        public java.awt.Component getComponent() {
            if( gui == null ) {
                gui = new WebApplicationPanel();
                gui.addChangeListener( this );
            }
            return gui;
        }
        
        public void stateChanged( @SuppressWarnings("unused")
		final ChangeEvent e ) {
            if( templateWizard != null ){
                templateWizard.setValid( isValid());
                fireChange();
            }
        }
    }
}
