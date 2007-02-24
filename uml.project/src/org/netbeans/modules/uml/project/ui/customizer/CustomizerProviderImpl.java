/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.project.ui.customizer;

import org.netbeans.modules.uml.project.ui.common.ReferencedJavaProjectModel;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.project.AssociatedSourceProvider;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.MDREventProcessor;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI;
import javax.swing.table.DefaultTableModel;


import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.uml.project.ui.codegen.CodeGeneratorAction;
import org.netbeans.modules.uml.project.ui.customizer.uiapi.UMLCustomizerDialog;


/** Customization of UML project
 *
 * @author Mike Frisino
 */
public class CustomizerProviderImpl implements CustomizerProvider {
    
    private final Project project;
    private final UMLProjectHelper projectHelper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    
    private ProjectCustomizer.Category categories[];
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    
    
    private static Map /*<Project,Dialog>*/project2Dialog = new HashMap(); 
    

    
    public CustomizerProviderImpl(Project project, UMLProjectHelper projectHelper, 
            PropertyEvaluator evaluator,
            ReferenceHelper refHelper
            ) {
        this.project = project;
        this.projectHelper = projectHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
    }
            
    public void showCustomizer() {
        showCustomizer( null );
    }
    
    
    public void showCustomizer ( String preselectedCategory ) {
        showCustomizer ( preselectedCategory, null );
    }
    
    public void showCustomizer( String preselectedCategory, String preselectedSubCategory ) {
        
        Dialog dialog = (Dialog)project2Dialog.get (project);
        if ( dialog != null ) {            
            dialog.setVisible(true);
            return;
        }
        else {
            UMLProjectProperties uiProperties = new UMLProjectProperties( 
                    (UMLProject)project, projectHelper, evaluator, refHelper 
                    );        
            init( uiProperties );

            OptionListener listener = new OptionListener( project, uiProperties );
            HelpCtx helpCtx = new HelpCtx( 
                    "org.netbeans.modules.uml.project.ui.customizer.UMLCustomizer" ); // NOI18N 
            if (preselectedCategory != null && preselectedSubCategory != null) {
                for (int i=0; i<categories.length; i++ ) {
                    if (preselectedCategory.equals(categories[i].getName())) {
                        JComponent component = panelProvider.create (categories[i]);
                        if (component instanceof SubCategoryProvider) {
                            ((SubCategoryProvider)component).showSubCategory(preselectedSubCategory);
                        }
                        break;
                    }
                }
            }
 // original code - invoke ProjectCustomizer to supply us with dialog
 //           dialog = ProjectCustomizer.createCustomizerDialog( categories, panelProvider, preselectedCategory, listener, helpCtx );
//  we need to control dialog to dynamically enable/disable the OK button
// MCF hack   - create our own dialog
        dialog = UMLCustomizerDialog.createCustomizerDialog( categories, panelProvider, preselectedCategory, listener, helpCtx );

            
            
            dialog.addWindowListener( listener );
            dialog.setTitle( MessageFormat.format(                 
                    NbBundle.getMessage( 
                    CustomizerProviderImpl.class, "LBL_Customizer_Title" ), // NOI18N 
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ) );

            project2Dialog.put(project, dialog);
            dialog.setVisible(true);
        }
    }    
    
    // Names of categories
    private static final String MODELING_CATEGORY = "ModelingCategory"; // NOI18N 
    private static final String IMPORTS_CATEGORY = "ImportsCategory"; // NOI18N 
    

    
    private void init( UMLProjectProperties uiProperties ) {
        
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
        
        ProjectCustomizer.Category modeling = ProjectCustomizer.Category.create(
                MODELING_CATEGORY,
                bundle.getString ("LBL_Config_Modeling"), // NOI18N 
                null,
                null);        
  
        ProjectCustomizer.Category imports = ProjectCustomizer.Category.create(
                IMPORTS_CATEGORY,
                bundle.getString ("LBL_Config_Imports"), // NOI18N 
                null,
                null);      

        

        categories = new ProjectCustomizer.Category[] {
                modeling,
                imports
        };
        

        Map panels = new HashMap();
        panels.put( modeling, new CustomizerModeling( uiProperties ) );
		panels.put( imports, new PanelUmlImports( uiProperties ) );

        
        panelProvider = new PanelProvider( panels );
        
    }
    
    private static class PanelProvider implements ProjectCustomizer.CategoryComponentProvider {
        
        private JPanel EMPTY_PANEL = new JPanel();
        
        private Map /*<Category,JPanel>*/ panels;
        
        PanelProvider( Map panels ) {
            this.panels = panels;            
        }
        
        public JComponent create( ProjectCustomizer.Category category ) {
            JComponent panel = (JComponent)panels.get( category );
            return panel == null ? EMPTY_PANEL : panel;
        }
                        
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {
    
        private Project project;
        private UMLProjectProperties uiProperties;
        
        OptionListener( Project project, UMLProjectProperties uiProperties ) {
            this.project = project;
            this.uiProperties = uiProperties;            
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed( ActionEvent e ) {
            ReferencedJavaProjectModel m = uiProperties.REFERENCED_JAVA_PROJECT_MODEL;
            if(uiProperties.getProjectMode().equals(UMLProject.PROJECT_MODE_IMPL_STR) && 
					m.getRefStatus() != ReferencedJavaProjectModel.ReferenceStatus.REF_STATUS_BROKEN)
            {
                // Store the properties into project 
                AssociatedSourceProvider asp = (AssociatedSourceProvider) uiProperties.getProject()
                .getLookup().lookup(AssociatedSourceProvider.class);
                Project oldProject = asp.getAssociatedSourceProject();
                SourceGroup[] oldGroup =asp.getSourceGroups();
                uiProperties.save();
                Project newProject = asp.getAssociatedSourceProject();
                SourceGroup[] newGroup =asp.getSourceGroups();
                if(oldProject!=null && !oldProject.equals(newProject))
                {
                    Log.out("Project is changed in the Customizer");
                    MDREventProcessor.getInstance().fireChanged(oldGroup,newGroup);
                }
                else if(newProject!=null && oldProject == null)
                {
                    Log.out("Project is changed in the Customizer");
                    MDREventProcessor.getInstance().fireChanged(null,newGroup);
                }

                // If user had invoked this from the 'Generate Code' action, call generatecode()
                if(isGenerateCode()) {
                    // Debug.out.println("CustomizerProviderImpl():actionPerformed(): Calling generate code");
                    CodeGeneratorAction codeGenerator = new CodeGeneratorAction();
                    codeGenerator.generateCode(project);
                    setGenerateCode(false);
                }
            }
            
            // Close & dispose the the dialog
            Dialog dialog = (Dialog)project2Dialog.get( project );
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
            }
            
            
            
            
        }        
        
        // Listening to window events ------------------------------------------
                
        public void windowClosed( WindowEvent e) {
            project2Dialog.remove( project );
        }    
        
        public void windowClosing (WindowEvent e) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = (Dialog)project2Dialog.get( project );
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
    
    static interface SubCategoryProvider {
        public void showSubCategory (String name);
    }
                            
    private boolean generateCode = false;
    
    public void setGenerateCode(boolean value) {
        this.generateCode = value;
    }
    
    public boolean isGenerateCode() {
        return generateCode;
    }
}
