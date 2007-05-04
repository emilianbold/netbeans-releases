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
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.project.AssociatedSourceProvider;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.MDREventProcessor;
import org.netbeans.modules.uml.project.ui.codegen.CodeGeneratorAction;
import org.netbeans.modules.uml.project.ui.customizer.uiapi.UMLCustomizerDialog;
import org.netbeans.modules.uml.project.ui.common.ReferencedJavaProjectModel;
import org.netbeans.modules.uml.ui.swing.commondialogs.CodeGenTemplateManagerPanel;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/** Customization of UML project
 *
 * @author Mike Frisino
 */
public class CustomizerProviderImpl implements CustomizerProvider
{
    private final Project project;
    private final UMLProjectHelper projectHelper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    
    private ProjectCustomizer.Category categories[];
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    public static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-uml-project/Customizer"; //NO18N

    private static Map /*<Project,Dialog>*/project2Dialog = new HashMap();
    
    public CustomizerProviderImpl(
        Project project,
        UMLProjectHelper projectHelper,
        PropertyEvaluator evaluator,
        ReferenceHelper refHelper)
    {
        this.project = project;
        this.projectHelper = projectHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
    }
    
    public void showCustomizer()
    {
        showCustomizer( null );
    }
    
    
    public void showCustomizer(String preselectedCategory)
    {
        showCustomizer(preselectedCategory, null);
    }
    
    public void showCustomizer(
        String preselectedCategory, String preselectedSubCategory)
    {
        Dialog dialog = (Dialog)project2Dialog.get(project);

        if (dialog != null)
        {
            dialog.setVisible(true);
            return;
        }

        else
        {
            UMLProjectProperties uiProperties = new UMLProjectProperties(
                (UMLProject)project, projectHelper, evaluator, refHelper);
            
            Lookup context = Lookups.fixed(new Object[] {
                project,
                uiProperties,
                new SubCategoryProvider(preselectedCategory, preselectedSubCategory)
            });
//            init(uiProperties);
            
            OptionListener listener = new OptionListener( project, uiProperties );
            
            HelpCtx helpCtx = new HelpCtx(
                "org.netbeans.modules.uml.project.ui.customizer.UMLCustomizer" ); // NOI18N

//            if (preselectedCategory != null && preselectedSubCategory != null)
//            {
//                for (int i=0; i<categories.length; i++ )
//                {
//                    if (preselectedCategory.equals(categories[i].getName()))
//                    {
//                        JComponent component = 
//                            panelProvider.create(categories[i]);
//                        
//                        if (component instanceof SubCategoryProvider)
//                        {
//                            ((SubCategoryProvider)component).showSubCategory(
//                                preselectedSubCategory);
//                        }
//                        
//                        break;
//                    }
//                }
//            }
            //JM: We don't have JavaImplementation project types anymore.. so don't need a custom dialog..              
//            dialog = UMLCustomizerDialog.createCustomizerDialog(
//                categories, panelProvider, 
//                preselectedCategory, listener, helpCtx);
            dialog = ProjectCustomizer.createCustomizerDialog( CUSTOMIZER_FOLDER_PATH, context, preselectedCategory, listener, null );
            
            dialog.addWindowListener(listener);
            
            dialog.setTitle(MessageFormat.format(
                NbBundle.getMessage(
                    CustomizerProviderImpl.class, "LBL_Customizer_Title"), // NOI18N
                    new Object[] 
                        {ProjectUtils.getInformation(project).getDisplayName()}));
            
            project2Dialog.put(project, dialog);
            dialog.setVisible(true);
        }
    }
    
//    // Names of categories
//    private static final String MODELING_CATEGORY = "ModelingCategory"; // NOI18N
//    private static final String IMPORTS_CATEGORY = "ImportsCategory"; // NOI18N
//    private static final String CODEGEN_CATEGORY = "CodeGenCategory"; // NOI18N
//    
//    
//    private void init( UMLProjectProperties uiProperties )
//    {
//        
//        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
//        
//        ProjectCustomizer.Category modeling = ProjectCustomizer.Category.create(
//            MODELING_CATEGORY,
//            bundle.getString("LBL_Config_Modeling"), // NOI18N
//            null,
//            null);
//        
//        ProjectCustomizer.Category imports = ProjectCustomizer.Category.create(
//            IMPORTS_CATEGORY,
//            bundle.getString("LBL_Config_Imports"), // NOI18N
//            null,
//            null);
//        
//        ProjectCustomizer.Category codegen = ProjectCustomizer.Category.create(
//            CODEGEN_CATEGORY,
//            bundle.getString("LBL_Code_Gen"), // NOI18N
//            null,
//            null);
//        
//        categories = new ProjectCustomizer.Category[]
//            {modeling, imports, codegen};
//        
//        Map panels = new HashMap();
//        panels.put(modeling, new CustomizerModeling(uiProperties));
//        panels.put(imports, new PanelUmlImports(uiProperties));
//        panels.put(codegen, new CodeGenTemplateManagerPanel(
//            uiProperties.getCodeGenTemplatesArray()));
//        
//        panelProvider = new PanelProvider(panels);
//    }
//    
//    private static class PanelProvider implements ProjectCustomizer.CategoryComponentProvider
//    {
//        private JPanel EMPTY_PANEL = new JPanel();
//        private Map /*<Category,JPanel>*/ panels;
//        
//        PanelProvider(Map panels)
//        {
//            this.panels = panels;
//        }
//        
//        public JComponent create(ProjectCustomizer.Category category)
//        {
//            JComponent panel = (JComponent)panels.get(category);
//            return panel == null ? EMPTY_PANEL : panel;
//        }
//    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener
    {
        private Project project;
        private UMLProjectProperties uiProperties;
        
        OptionListener( Project project, UMLProjectProperties uiProperties )
        {
            this.project = project;
            this.uiProperties = uiProperties;
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed( ActionEvent e )
        {
            ReferencedJavaProjectModel m = uiProperties.REFERENCED_JAVA_PROJECT_MODEL;
            
            if (uiProperties.getProjectMode().equals(UMLProject.PROJECT_MODE_IMPL_STR) &&
                m.getRefStatus() != ReferencedJavaProjectModel.ReferenceStatus.REF_STATUS_BROKEN)
            {
                // Store the properties into project
                AssociatedSourceProvider asp = 
                    (AssociatedSourceProvider) uiProperties.getProject()
                    .getLookup().lookup(AssociatedSourceProvider.class);
                
                Project oldProject = asp.getAssociatedSourceProject();
                SourceGroup[] oldGroup =asp.getSourceGroups();
                uiProperties.save();
                Project newProject = asp.getAssociatedSourceProject();
                SourceGroup[] newGroup =asp.getSourceGroups();
                
                if (oldProject!=null && !oldProject.equals(newProject))
                {
                    Log.out("Project is changed in the Customizer");
                    MDREventProcessor.getInstance().fireChanged(oldGroup,newGroup);
                }
                
                else if (newProject!=null && oldProject == null)
                {
                    Log.out("Project is changed in the Customizer");
                    MDREventProcessor.getInstance().fireChanged(null,newGroup);
                }
            }
            
            // uiProperties.setCodeGenTemplates();

            
            // Close & dispose the dialog
            Dialog dialog = (Dialog)project2Dialog.get( project );
            if (dialog != null)
            {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
        
        // Listening to window events ------------------------------------------
        
        public void windowClosed( WindowEvent e)
        {
            project2Dialog.remove( project );
        }
        
        public void windowClosing(WindowEvent e)
        {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = (Dialog)project2Dialog.get( project );
            if (dialog != null)
            {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
    
//    static interface SubCategoryProvider
//    {
//        public void showSubCategory(String name);
//    }
    
    static final class SubCategoryProvider {

        private String subcategory;

        private String category;

        SubCategoryProvider(String category, String subcategory) {
            this.category = category;
            this.subcategory = subcategory;
        }
        public String getCategory() {
            return category;
        }
        public String getSubcategory() {
            return subcategory;
        }
    }
}
