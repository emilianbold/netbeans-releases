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
