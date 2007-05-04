/*
 * UMLCompositePanelProvider.java
 *
 * Created on May 2, 2007, 6:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
