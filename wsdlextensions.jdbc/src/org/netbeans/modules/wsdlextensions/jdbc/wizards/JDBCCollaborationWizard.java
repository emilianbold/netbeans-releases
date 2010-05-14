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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.wsdlextensions.jdbc.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.modules.wsdlextensions.jdbc.builder.DBMetaData;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;

/**
 * Wizard to collect name and participating tables information to be used in creating a new JDBC
 * collaboration.
 */
public class JDBCCollaborationWizard extends JDBCWizard {

    private WSDLWizardContext mWsdlWizardContext = null;

    /**
     * Wizard Context from WSDL Wizard
     * 
     * @param context
     */
    public JDBCCollaborationWizard(WSDLWizardContext context) {
        mWsdlWizardContext = context;
    }
    
    /**
     * 
     *
     */
    public JDBCCollaborationWizard() {
    }

    class Descriptor extends JDBCWizardDescriptor {

        public Descriptor(final WizardDescriptor.Iterator iter) {
            super(iter, JDBCCollaborationWizard.this.context);
        }
    }

    class WizardIterator extends JDBCWizardIterator {

        private List panels;
        private WizardDescriptor.Panel collaborationNamePanel;
        private static final int COLLABORATION_NAME_PANEL = 0;

        private JDBCWizardOptionsDescriptorPanel optionsSelectionPanel;
        private static final int OPTIONS_SELECTION_PANEL = 0;

        private JDBCWizardTableSelectionDescriptorPanel tableSelectionPanel;
        private static final int TABLE_SELECTION_PANEL = 1;

        private PreparedStatementDescriptorPanel prepstmtSelectionPanel;
        private static final int PREPAREDSTATEMENT_SELECTION_PANEL = 2;

        private StoredProcedureDescriptorPanel proceduresPanel;
        private static final int PROCEDURES_SELECTION_PANEL = 3;
        
        private JDBCWizardTransferDescriptorPanel tableColumnsSelectionPanel;
        private static final int TABLE_COLUMN_SELECTION_PANEL = 4;
        
        private JDBCWizardSQLSelectionDescriptorPanel sqlPanel;
        private static final int SQLFILE_SELECTION_PANEL = 5;

        private JNDINameFinishDescriptorPanel jndiNamePanel;
        private static final int JNDI_NAME_PANEL = 6;
        private JDBCCollaborationWizard mWizard;
        private DatabaseConnection dbConn;
        private DBMetaData dbMeta;
        private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jdbc.template.Bundle");

    private String templateName;
    private String[] steps;
    private int currentStepIndex = -1;
    private WSDLWizardDescriptorPanel[] wsdlPanels;
        
        public WizardIterator(final JDBCCollaborationWizard wizard) {
            super(wizard.mWsdlWizardContext);
            this.mWizard = wizard;
            super.setup();

            if (this.collaborationNamePanel == null)  {
                this.collaborationNamePanel = new JDBCWizardNameFinishPanel(JDBCCollaborationWizard.this, NbBundle.getMessage(
                        JDBCCollaborationWizard.class, "TITLE_tblwizard_name"));
            }
        }

//        public String name() {
//            return "";
//        }

        public void initialize(final WizardDescriptor wiz) {
            this.mWizard.setDescriptor(wiz);
            super.initialize(wiz);
            if (this.collaborationNamePanel != null) {
                // since the chooser panel is not used (we'll use the wsld wizard instead),
                // we still need to pass in the right via the wizard descriptior  
                collaborationNamePanel.readSettings(wiz);
            }
        }

    @Override
    public WSDLWizardDescriptorPanel current() {
        if (currentStepIndex == -1) {
             return super.current();   
        }
        return wsdlPanels[currentStepIndex];
    }
    
  @Override
    public String[] getSteps() {
        assert templateName != null : "template is not set";
        return steps;
    }

    @Override
    public boolean hasNext() {
        return currentStepIndex < wsdlPanels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return true;
    }
    
        /**
         * Overrides parent implementation to test for duplicate collab name before advancing to
         * next panel, and skip join panel if fewer than two source tables are selected.
         * 
         * @see org.openide.WizardDescriptor.Iterator#nextPanel
         */
        public void nextPanel() {
            /*  if (this.current().equals(this.sourceTableSelectionPanel)) { // Currently in source OTDs panel.
            final JDBCWizardTransferPanel xferPanel = (JDBCWizardTransferPanel) this.current();
            
            }
            if(this.optionsSelectionPanel.tablesSelected && this.current().equals(this.sourceTableSelectionPanel)) {
            super.index = 4; 
            }
            
            if(this.optionsSelectionPanel.prepStmtsSelected && this.current().equals(this.optionsSelectionPanel)) {
            super.index = 3;
            this.prepstmtSelectionPanel.setConnection(this.optionsSelectionPanel.getConnection());
            }
            if (this.optionsSelectionPanel.proceduresSelected && this.current().equals(this.optionsSelectionPanel)) {
            this.proceduresPanel.setConnection(this.optionsSelectionPanel.getConnection());
            }
            
            
            super.nextPanel(); // Otherwise allow advance.
             */
            
currentStepIndex++;

            if ((this.current().equals(collaborationNamePanel)) ||
                (this.current().equals(optionsSelectionPanel))) {
                // if the SimpleTargetChooser, then we need to call it's storeSetting
                // because this page is bypassed by WSDL Wizard chooser but we
                // need to still process the properties that legacy code is expecting
                collaborationNamePanel.storeSettings(mWizard.getDescriptor());
				optionsSelectionPanel.storeSettings(mWizard.getDescriptor());
            }
            
            super.index = getNextStep(super.index);
        }

        /* Returns the index of the next panel based on the objects selected. */
        private int getNextStep(int currStep) {
            if(this.current().equals(collaborationNamePanel)) {
                return this.OPTIONS_SELECTION_PANEL;
            }
			if(this.current().equals(this.prepstmtSelectionPanel)) {
            	this.jndiNamePanel.setSql(this.prepstmtSelectionPanel.getSQL());
            	this.jndiNamePanel.setPrepStmt(this.prepstmtSelectionPanel.getPrepStmt());
             }
            if (this.current().equals(this.optionsSelectionPanel)) {
                
                if (this.optionsSelectionPanel.isTableSelected()) {
                    this.tableSelectionPanel.setConnection(this.optionsSelectionPanel.getConnection());
                    this.tableSelectionPanel.persistModel();
                    return this.TABLE_SELECTION_PANEL;
                
                } else if (this.optionsSelectionPanel.isPreparedStatementsSelected()) {
                    this.prepstmtSelectionPanel.setConnection(this.optionsSelectionPanel.getConnection());
                    return this.PREPAREDSTATEMENT_SELECTION_PANEL;
                
                } else if (this.optionsSelectionPanel.isProceduresSelected()) {
                    this.proceduresPanel.setConnection(this.optionsSelectionPanel.getConnection());
                    return this.PROCEDURES_SELECTION_PANEL;
                } else if (this.optionsSelectionPanel.isSQLFileSelected()) {
                    if(this.current().equals(this.optionsSelectionPanel)){
                        this.prepstmtSelectionPanel.setConnection(this.optionsSelectionPanel.getConnection());
                        return this.SQLFILE_SELECTION_PANEL;
                    }
                }
            } else if(this.current().equals(this.tableSelectionPanel)) {
                return this.TABLE_COLUMN_SELECTION_PANEL;                
            } else if(this.current().equals(this.sqlPanel) && this.optionsSelectionPanel.isSQLFileSelected()){
                this.prepstmtSelectionPanel.setTextArea(this.sqlPanel.getSqlText());
                return this.PREPAREDSTATEMENT_SELECTION_PANEL;
            } else {
                return JNDI_NAME_PANEL;
            }
            //default
            return currStep++;
        }

        /* Returns the index of the previous panel based on the objects selected. */
        private int getPreviousStep(int currStep) {
			Boolean isPrepSelected = (Boolean) mWizard.getDescriptor().getProperty(WizardConstants.PREPSTMTS_SELECTED);
            Boolean isTableSelected = (Boolean) mWizard.getDescriptor().getProperty(WizardConstants.TABLES_SELECTED);
            Boolean isProcSelected = (Boolean) mWizard.getDescriptor().getProperty(WizardConstants.PROCEDURES_SELECTED);
			Boolean isSqlFileSelected = (Boolean) mWizard.getDescriptor().getProperty(WizardConstants.SQLFILE_SELECTED);
                    
            if (this.current().equals(this.optionsSelectionPanel)) {
                return this.COLLABORATION_NAME_PANEL;
            } else {
                if(this.current().equals(this.tableSelectionPanel)|| 
                        this.current().equals(this.sqlPanel)|| 
                        this.current().equals(this.proceduresPanel)) {
                    return this.OPTIONS_SELECTION_PANEL;
                } else if(this.current().equals(this.prepstmtSelectionPanel) && isSqlFileSelected) {
                  return this.SQLFILE_SELECTION_PANEL;  
                }else if(this.current().equals(this.prepstmtSelectionPanel) && isPrepSelected) {
                  return this.OPTIONS_SELECTION_PANEL;  
                }else if(this.current().equals(this.tableColumnsSelectionPanel)) {
                  return this.TABLE_SELECTION_PANEL;  
                } else if(this.current().equals(this.jndiNamePanel)) {
                    if (((isPrepSelected != null ) && isPrepSelected) || ((isSqlFileSelected !=null) && isSqlFileSelected)) {
                        return this.PREPAREDSTATEMENT_SELECTION_PANEL;
                    } else if((isTableSelected != null) && isTableSelected) {                        
                        return this.TABLE_COLUMN_SELECTION_PANEL;
                    } else if((isProcSelected !=null) && isProcSelected) {
                        return this.PROCEDURES_SELECTION_PANEL;
                    }else if((isSqlFileSelected !=null) && isSqlFileSelected) {
                        return this.PREPAREDSTATEMENT_SELECTION_PANEL;
                    }
                }
            }
            //default
            return currStep++;
        }

        /**
         * Overrides parent implementation to skip join panel if fewer than two source tables are
         * selected.
         * 
         * @see org.openide.WizardDescriptor.Iterator#previousPanel
         */
        public void previousPanel() {
            currentStepIndex--;
            //super.previousPanel(); // Otherwise use parent implementation.
            super.index = getPreviousStep(super.index);
        }

        public List createPanels(final WizardDescriptor wiz) {
            final List srcModel = new ArrayList();
            final List destModel = new ArrayList();

            final List dbModels = new ArrayList();

            final List sourceDBModels = new ArrayList();
            final List targetDBModels = new ArrayList();

            Project project = Templates.getProject(wiz);
            if (project != null) {
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);

                if (groups == null || groups.length < 1) {
                    groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                }

                this.collaborationNamePanel = new SimpleTargetChooserPanel(getWSDLWizardContext(),
                        project, groups, null, false);
            }


            this.optionsSelectionPanel = new JDBCWizardOptionsDescriptorPanel(mWsdlWizardContext,
                    "Select one of the following :");
            this.tableSelectionPanel = new JDBCWizardTableSelectionDescriptorPanel(mWsdlWizardContext,
                    NbBundle.getMessage(JDBCCollaborationWizard.class,
                    "TITLE_tblwizard_selecttableobjects"));
//            this.tableSelectionPanel.initialize();

            this.tableColumnsSelectionPanel = new JDBCWizardTransferDescriptorPanel(mWsdlWizardContext,
                    NbBundle.getMessage(JDBCCollaborationWizard.class,
                    "TITLE_tblwizard_selectsources"));

            this.prepstmtSelectionPanel = new PreparedStatementDescriptorPanel(mWsdlWizardContext,
                    "Enter A Prepared Statement");

            this.sqlPanel = new JDBCWizardSQLSelectionDescriptorPanel(mWsdlWizardContext,
                    "Please select a SQL File");
            
            this.proceduresPanel = new StoredProcedureDescriptorPanel(mWsdlWizardContext, NbBundle.getMessage(JDBCCollaborationWizard.class,
                    "TITLE_stored_procedure_step"));

            this.jndiNamePanel = new JNDINameFinishDescriptorPanel(mWsdlWizardContext,
                    NbBundle.getMessage(JDBCCollaborationWizard.class,
                    "STEP_tblwizard_jndiname"));

            this.panels = new ArrayList();
            if (this.collaborationNamePanel != null) {
//                this.panels.add(this.collaborationNamePanel);
            }
            this.panels.add(this.optionsSelectionPanel);
            this.panels.add(this.tableSelectionPanel);
            this.panels.add(this.prepstmtSelectionPanel);
            this.panels.add(this.proceduresPanel);
            this.panels.add(this.tableColumnsSelectionPanel);
            this.panels.add(this.sqlPanel);
            this.panels.add(this.jndiNamePanel);
            return Collections.unmodifiableList(this.panels);
        }

        public String[] createSteps() {
            try {
                return new String[]{
                            // TODO - need make wizard steps text match actual panel being viewed
//                            NbBundle.getMessage(JDBCCollaborationWizard.class, "FILE_TYPE"),
//                            NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_tblwizard_name"),
                            NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_OPTIONS_SELECT"),
                            NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_tblwizard_select"),
                            NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_tblwizard_sources"),
                            NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_PREPARED_STMT"),
                            NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_PROCEDURES_SELECT"),
                            NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_SQLFILE_SELECT"),
                            NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_tblwizard_jndiname")
                        };
            } catch (final MissingResourceException e) {
                return new String[]{};
            }
        }
        
    /**
     * Based on the template selected, define the appropriate panels
     * and steps to show
     * 
     * @param templateName
     */        
   public void setTemplateName(String templateName) {
        this.templateName = templateName;
        currentStepIndex = -1;
        WSDLWizardContext context = getWSDLWizardContext();
        if (templateName.equals(mBundle.getString("TEMPLATE_name_Table"))) {
            // Table panels
            wsdlPanels = new WSDLWizardDescriptorPanel[]{
                        optionsSelectionPanel, tableSelectionPanel, 
                        tableColumnsSelectionPanel, jndiNamePanel
            };  
            optionsSelectionPanel.setSelection(mBundle.
                    getString("TEMPLATE_name_Table"));
            if (optionsSelectionPanel.getConnection() != null) {
                tableSelectionPanel.setConnection(this.optionsSelectionPanel.
                        getConnection());
                tableSelectionPanel.persistModel();            
            }
        } else if (templateName.equals(mBundle.
                getString("TEMPLATE_name_PreparedStatements"))) {
            // Prepared Statement panels
            wsdlPanels = new WSDLWizardDescriptorPanel[]{
                        optionsSelectionPanel,
                        prepstmtSelectionPanel, jndiNamePanel
            };         
            optionsSelectionPanel.setSelection(mBundle.
                    getString("TEMPLATE_name_PreparedStatements"));
            if (optionsSelectionPanel.getConnection() != null) {
                prepstmtSelectionPanel.setConnection(this.optionsSelectionPanel.
                        getConnection());      
            }      
        } else if (templateName.equals(mBundle.
                getString("TEMPLATE_name_Procedures"))) {
            // Stored Procedures panels
            wsdlPanels = new WSDLWizardDescriptorPanel[]{
                        optionsSelectionPanel,
                        proceduresPanel, jndiNamePanel
            };         
            optionsSelectionPanel.setSelection(mBundle.
                    getString("TEMPLATE_name_Procedures"));
            if (optionsSelectionPanel.getConnection() != null) {
                proceduresPanel.setConnection(this.optionsSelectionPanel.
                        getConnection());      
            }      
        } else if (templateName.equals(mBundle.
                getString("TEMPLATE_name_SQLFile"))) {
            // SQL File panels
            wsdlPanels = new WSDLWizardDescriptorPanel[]{
                        optionsSelectionPanel,
                        sqlPanel, prepstmtSelectionPanel, jndiNamePanel
            };         
            optionsSelectionPanel.setSelection(mBundle.
                    getString("TEMPLATE_name_SQLFile"));
            if (optionsSelectionPanel.getConnection() != null) {
                proceduresPanel.setConnection(this.optionsSelectionPanel.
                        getConnection());   
                prepstmtSelectionPanel.setTextArea(sqlPanel.getSqlText());                
            }      
        }        
        
        // define the steps based on the appropriate panels' name
        steps = new String[wsdlPanels.length];
        int i = 0;
        for (WSDLWizardDescriptorPanel panel : wsdlPanels) {
            steps[i++] = panel.getName();
        }        
        
    }
        
    }
    /** Key name used to reference database sources in wizard context. */
    public static final String DATABASE_SOURCES = "database_sources";
    /** Key name used to reference JNDI Name. */
    public static final String JNDI_NAME = "jndi_name";
    public static final String DBTYPE = "dbtype";
    /** Key name used to reference collaboration name in wizard context. */
    public static final String COLLABORATION_NAME = "collaboration_name";
    /** Key name used to reference Project in wizard context. */
    public static final String PROJECT = "project";
    /** Key name used to reference Collection of runtime input args in wizard context. */
    public static final String RUNTIME_INPUTS = "runtime_inputs";
    /** Key name used to reference List of source OTDs in wizard context. */
    public static final String SOURCE_OTDS = "source_otds";
    public static final int SOURCE_PANEL_INDEX = 2;
    /** Key name used to reference List of source tables in wizard context. */
    public static final String SOURCE_TABLES = "source_tables";

    /* Defines panels to be displayed */
    private WizardDescriptor descriptor;

    /* Wizard iterator; handles display and movement among wizard panels */
    private JDBCWizardIterator iterator;
    private Project project;

    public JDBCCollaborationWizard(Project prj) {
        this.project = prj;
    }

//    public static WizardDescriptor.Iterator newTemplateIterator() {
    public static JDBCWizardIterator newTemplateIterator(WSDLWizardContext context) {
        final JDBCCollaborationWizard wizard = new JDBCCollaborationWizard(context);
        wizard.initialize(wizard.mWsdlWizardContext);
        return wizard.getIterator();
    }

    /**
     * @see JDBCWizard#getDescriptor
     */
    public WizardDescriptor getDescriptor() {
        if (this.descriptor == null) {
            this.descriptor = new Descriptor(this.iterator);
        }
        return this.descriptor;
    }

    public void setDescriptor(final WizardDescriptor wd) {
        this.descriptor = wd;
    }

    /**
     * @see JDBCWizard#getIterator
     */
    public JDBCWizardIterator getIterator() {
        return this.iterator;
    }

    // public Project getProject() {
    // return project;
    // }
    /**
     * Initializes iterator and descriptor for this wizard.
     */
    public void initialize() {
        this.iterator = new WizardIterator(this);

    }
    
    /**
     * Initializes iterator and descriptor for this wizard.
     */
    public void initialize(WSDLWizardContext context) {
        this.iterator = new WizardIterator(this);
    }    

    /**
     * Performs processing to handle cancellation of this wizard.
     */
    protected void cancel() {
    }

    /**
     * Performs processing to cleanup any resources used by this wizard.
     */
    protected void cleanup() {
    }

    protected String getDialogTitle() {
        return NbBundle.getMessage(JDBCCollaborationWizard.class, "TITLE_dlg_new_collab");
    }
}
