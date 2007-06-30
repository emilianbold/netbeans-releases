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

package org.netbeans.modules.uml.codegen.ui.customizer;

import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.uml.codegen.action.ui.GenerateCodePanel;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jyothi
 * @author Craig Conover, craig.conover@sun.com
 */
public class ProjectTemplatesCustomizer 
    implements ProjectCustomizer.CompositeCategoryProvider
{
    private static final String TEMPLATES = "Templates"; // NOI18N
    private String name;
    
    /** Creates a new instance of TemplatesManagerCustomizer
     * @param name
     */
    public ProjectTemplatesCustomizer(String name)
    {
        this.name = name;
    }
    
    public Category createCategory(Lookup context)
    {
        ResourceBundle bundle = NbBundle.getBundle( 
            ProjectTemplatesCustomizer.class);
        
        ProjectCustomizer.Category toReturn = null;
        
        if (TEMPLATES.equals(name))
        {
            toReturn = ProjectCustomizer.Category.create(
                TEMPLATES,
                bundle.getString("LBL_Config_Templates"), // NOI18N
                null,
                null);
        }

        return toReturn;
    }
    
    public JComponent createComponent(Category category, Lookup context)
    {
        UMLProjectProperties uiProps = 
            (UMLProjectProperties)context.lookup(UMLProjectProperties.class);
        
        if (TEMPLATES.equals(category.getName()))
        {
            // CustomizerProviderImpl.SubCategoryProvider prov = 
            //     (CustomizerProviderImpl.SubCategoryProvider)context.lookup(
            //     CustomizerProviderImpl.SubCategoryProvider.class);
            // assert prov != null : 
            //     "Assuming CustomizerProviderImpl.SubCategoryProvider in customizer context";
            // return new CustomizerLibraries(uiProps, prov);
            // return new TemplatesPanel(uiProps);
            
//            DomainTemplatesManagerPanel panel = 
//                new DomainTemplatesManagerPanel(uiProps);
//            ProjectTemplatesPanel panel = new ProjectTemplatesPanel(uiProps);

            GenerateCodePanel panel = new GenerateCodePanel(false, uiProps);
            category.setOkButtonListener(panel);
            return panel;
        }
        
        return new JPanel();
    }
    
    /**
     *
     * @return
     */
    public static ProjectTemplatesCustomizer createTemplates()
    {
        return new ProjectTemplatesCustomizer(TEMPLATES);
    }
    
}
