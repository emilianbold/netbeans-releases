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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.railsprojects.ui.customizer;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class RailsCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String BUILD = "Build"; // NOI18N
    public static final String RAILS = "Rails"; // NOI18N
    
    private String name;
    
    /** Creates a new instance of RailsCompositePanelProvider */
    public RailsCompositePanelProvider(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
        ProjectCustomizer.Category toReturn = null;
        if (BUILD.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    BUILD,
                    bundle.getString( "LBL_Config_Build" ), // NOI18N
                    null,
                    (ProjectCustomizer.Category[])null);
        } else if (RAILS.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RAILS,
                    bundle.getString( "LBL_Config_Rails" ), // NOI18N
                    null,
                    (ProjectCustomizer.Category[])null);
        }
        assert toReturn != null : "No category for name:" + name;
        return toReturn;
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        RailsProjectProperties uiProps = context.lookup(RailsProjectProperties.class);
        if (BUILD.equals(nm)) {
            return new CustomizerCompile(uiProps);
        } else if (RAILS.equals(nm)) {
            return new CustomizerRun(uiProps);
        }
        return new JPanel();

    }

    public static RailsCompositePanelProvider createBuild() {
        return new RailsCompositePanelProvider(BUILD);
    }

    public static RailsCompositePanelProvider createRails() {
        return new RailsCompositePanelProvider(RAILS);
    }
}
