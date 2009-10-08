/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.compapp.projects.base.ui.customizer.catalog;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;

// todo NB_65_VLV
public class CustomizerProviderImpl implements CustomizerProvider {
    
    private final Project project;
    private final AntProjectHelper antProjectHelper;   
    private final ReferenceHelper refHelper;
    
    private List<ProjectCustomizer.Category> categories;
    private PanelProvider panelProvider;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    
    private static Map<Project,Dialog> project2Dialog = new HashMap<Project,Dialog>(); 
    
    // Names of categories
    private static final String REFERENCE = "Project References";
    private static final String CATALOG = "XML Catalog";
    
    public CustomizerProviderImpl(Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        this.project = project;
        this.refHelper = refHelper;
        this.antProjectHelper = antProjectHelper;
    }
            
    public void showCustomizer() {
        showCustomizer(null);
    }
    
    
    public void showCustomizer (String preselectedCategory) {
        showCustomizer (preselectedCategory, null);
    }
    
    public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {
        
        Dialog dialog = project2Dialog.get(getProject());
        if (dialog != null) {            
            dialog.setVisible(true);
            return;
        }
        else {
            init();
            OptionListener listener = new OptionListener(getProject());

            List<ProjectCustomizer.Category> listCategories = getCategories();
            if (preselectedCategory != null && preselectedSubCategory != null) {
                for (ProjectCustomizer.Category category:listCategories) {
                    if (preselectedCategory.equals(category.getName())) {
                        JComponent component = panelProvider.create (category);
                        if (component instanceof SubCategoryProvider) {
                            ((SubCategoryProvider)component).showSubCategory(preselectedSubCategory);
                        }
                        break;
                    }
                }
            }
            dialog = ProjectCustomizer.createCustomizerDialog(listCategories.toArray(
                    new ProjectCustomizer.Category[listCategories.size()]), 
                    panelProvider, preselectedCategory, listener, null);
            dialog.addWindowListener(new OptionListener(getProject()));
            dialog.setTitle(NbBundle.getMessage(
                    CustomizerProviderImpl.class, "LBL_Customizer_Title", // NOI18N
                    ProjectUtils.getInformation(getProject()).getDisplayName()));

            project2Dialog.put(getProject(), dialog);
            dialog.setVisible(true);
        }
    }    
    
    /**
     * This api is called when showCustomizer is invoked and if customizer 
     * provider is not fully initialized.
     * It initializes the categories list and the panel provider (CategoryComponentProvider)
     */
    protected void init() {
        categories = new ArrayList<ProjectCustomizer.Category>();
        panelProvider = new PanelProvider(createCategoriesMap());
    }
    
    /**
     * Getter for project
     */
    protected Project getProject() {
        return project;
    }

    /**
     * Getter for antProjectHelper
     */
    protected AntProjectHelper getAntProjectHelper() {
        return antProjectHelper;
    }

    /**
     * Getter for refHelper
     */
    protected ReferenceHelper getRefHelper() {
        return refHelper;
    }

    /**
     * Getter for categories
     */
    protected List<ProjectCustomizer.Category> getCategories() {
        return categories;
    }

    /**
     * This api is called when showCustomizer is invoked and if customizer 
     * provider is not fully initialized.
     * The default implementation creates Project Reference and XML Catalog Categories.
     * Subclasses can override this and may or may not call super depending upon,
     * the categories are desired in project customizer.
     */
    protected Map<ProjectCustomizer.Category,JComponent> createCategoriesMap() {
        ProjectCustomizer.Category references = ProjectCustomizer.Category.create(
                REFERENCE,
                NbBundle.getMessage(CustomizerProviderImpl.class,
                "LBL_Customizer_Category_ProjectReferences"),
                null);
        
        ProjectCustomizer.Category catalog = ProjectCustomizer.Category.create(
                CATALOG,
                NbBundle.getMessage(CustomizerProviderImpl.class,
                "LBL_Customizer_Category_XMLCatalog"),
                null);
        
        getCategories().add(references);
        getCategories().add(catalog);
        
        Map<ProjectCustomizer.Category,JComponent> panels = 
                new HashMap<ProjectCustomizer.Category,JComponent>();
        panels.put(references, new CustomizerProjectReferences(getProject(), getRefHelper()));
        panels.put(catalog, new CustomizerXMLCatalog(getProject()));
        return panels;
    }

    /**
     * This api is called when OK button on the Project customizer is clicked.
     * The default implementation just stores the changes in the XML catalog file.
     * Project specific implemenataions must override this api to store project
     * specific properties and should call super, so that XML catalog changes are persisted.
     */
    protected void storeProjectData() {
        // default Impl saves catalog data
        for (JComponent comp :panelProvider.panels.values()) {
            if(comp instanceof CustomizerXMLCatalog) {
                ((CustomizerXMLCatalog)comp).storeProjectData();
            }
        }
    }

    /**
     * CategoryComponentProvider provider class.
     * It stores categories and there corresponding UI in a map.
     * An instance of PanelProvider is stored in CustomizerProviderImpl instance
     */
    private static class PanelProvider implements ProjectCustomizer.CategoryComponentProvider {
        
        private JPanel EMPTY_PANEL = new JPanel();
        
        private Map<ProjectCustomizer.Category,JComponent> panels;
        
        PanelProvider(Map<ProjectCustomizer.Category,JComponent> panels) {
            this.panels = panels;            
        }
        
        public JComponent create(ProjectCustomizer.Category category) {
            JComponent panel = panels.get(category);
            return panel == null ? EMPTY_PANEL : panel;
        }
                        
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {
    
        private Project project;
        
        OptionListener(Project project) {
            this.project = project;
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed(ActionEvent e) {
            // Store the properties into project 
            storeProjectData();
            // Close & dispose the the dialog
            Dialog dialog = project2Dialog.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }        
        
        // Listening to window events ------------------------------------------
                
        public void windowClosed(WindowEvent e) {
            project2Dialog.remove(project);
        }    
        
        public void windowClosing(WindowEvent e) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = project2Dialog.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
    
    public static interface SubCategoryProvider {
        public void showSubCategory (String name);
    }
}
