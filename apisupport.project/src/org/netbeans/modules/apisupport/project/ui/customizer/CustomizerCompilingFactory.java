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

package org.netbeans.modules.apisupport.project.ui.customizer;

import javax.swing.JComponent;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class CustomizerCompilingFactory implements ProjectCustomizer.CompositeCategoryProvider {
    
    /** Creates a new instance of CustomizerCompilingFactory */
    public CustomizerCompilingFactory() {
    }
    
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(
                CustomizerProviderImpl.CATEGORY_COMPILING, 
                NbBundle.getMessage(CustomizerCompilingFactory.class, "LBL_ConfigCompiling"),
                null);
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        SingleModuleProperties props = context.lookup(SingleModuleProperties.class);
        assert props != null;
        return new CustomizerCompiling(props, category);
    }


}
