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

package org.netbeans.modules.ruby.javaint;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.rubyproject.ProjectPropertyExtender;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
//import org.netbeans.modules.ruby.rubyproject.ui.customizer.RubyProjectProperties;

/**
 *
 * @author mkleint
 */
public class RubyCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    public static final String JAVA = "Java";
    
    private String name;
    
    /** Creates a new instance of RubyCompositePanelProvider */
    public RubyCompositePanelProvider(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
        ProjectCustomizer.Category toReturn = null;
        if (JAVA.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    JAVA,
                    bundle.getString( "LBL_Config_Java" ), // NOI18N
                    null,
                    (ProjectCustomizer.Category[])null);
        }
        assert toReturn != null : "No category for name:" + name;
        return toReturn;
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        if (JAVA.equals(nm)) {
            SharedRubyProjectProperties uiProps = (SharedRubyProjectProperties)context.lookup(SharedRubyProjectProperties.class);
            Project project = context.lookup(Project.class);
            return new CustomizerLibraries(project, uiProps, null);
        }
        return new JPanel();

    }


    public static RubyCompositePanelProvider createJava() {
        return new RubyCompositePanelProvider(JAVA);
    }
}
