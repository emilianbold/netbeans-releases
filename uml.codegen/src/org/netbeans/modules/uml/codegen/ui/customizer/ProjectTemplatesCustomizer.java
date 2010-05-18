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

package org.netbeans.modules.uml.codegen.ui.customizer;

import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.codegen.action.ui.GenerateCodePanel;
import org.netbeans.modules.uml.project.UMLProject;
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

            UMLProject project = (UMLProject)context.lookup(Project.class);
            GenerateCodePanel panel = new GenerateCodePanel(false, uiProps, project);
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
