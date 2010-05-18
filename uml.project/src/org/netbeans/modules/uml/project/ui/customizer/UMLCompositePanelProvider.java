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

import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jyothi
 */
public class UMLCompositePanelProvider  implements ProjectCustomizer.CompositeCategoryProvider {
    
    // Names of categories
    private static final String MODELING = "Modeling"; // NOI18N 
    private static final String IMPORTS = "Imports"; // NOI18N 
    
    private String name;
    
    
    /** Creates a new instance of UMLCompositePanelProvider 
     * @param name 
     */
    public UMLCompositePanelProvider(String name) {
        this.name = name;
    }

    public Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
        ProjectCustomizer.Category toReturn = null;
        
        if (MODELING.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    MODELING,
                    bundle.getString("LBL_Config_Modeling"), //NOI18N
                    null,
                    null);
        } else if (IMPORTS.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    IMPORTS,
                    bundle.getString( "LBL_Config_Imports" ), // NOI18N
                    null,
                    null);
        }         
        return toReturn;
    }

    public JComponent createComponent(Category category, Lookup context) {
        String nm = category.getName();
        UMLProjectProperties uiProps = (UMLProjectProperties)context.lookup(UMLProjectProperties.class);
        if (MODELING.equals(nm)) {
            return new CustomizerModeling(uiProps);
        } else if (IMPORTS.equals(nm)) {
            return new PanelUmlImports(uiProps);
        } 
        return new JPanel();
    }
    
    public static UMLCompositePanelProvider createModeling() {
        return new UMLCompositePanelProvider(MODELING);
    }
    public static UMLCompositePanelProvider createImports() {
        return new UMLCompositePanelProvider(IMPORTS);
    }
    
}
