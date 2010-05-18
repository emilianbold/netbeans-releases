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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uml.project.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

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
import org.netbeans.modules.uml.project.ui.common.ReferencedJavaProjectModel;
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
            ReferencedJavaProjectModel m = uiProperties.referencedJavaProjectModel;
            
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
