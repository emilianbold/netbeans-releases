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

package org.netbeans.modules.ant.freeform.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 *
 * @author mkleint
 */
public class TargetMappingCategoryProvider implements org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider {
    
    /** Creates a new instance of TargetMappingCustomizerProvider */
    public TargetMappingCategoryProvider() {
    }
    
    public Category createCategory(Lookup context) {
        FreeformProject project = context.lookup(FreeformProject.class);
        assert project != null;
        if (project.usesAntScripting()) {
            return org.netbeans.spi.project.ui.support.ProjectCustomizer.Category.create("targetMapping", 
                    NbBundle.getMessage(TargetMappingPanel.class, "LBL_ProjectCustomizer_Category_Targets"), null, null);
        }
        return null;
    }

    public JComponent createComponent(Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        ProjectAccessor acc = context.lookup(ProjectAccessor.class);
        AuxiliaryConfiguration aux = context.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        assert acc != null;
        assert project != null;
        
        List<TargetDescriptor> extraTargets = new ArrayList<TargetDescriptor>();
        for (ProjectNature pn : FreeformProject.PROJECT_NATURES.allInstances()) {
            extraTargets.addAll(pn.getExtraTargets(project, acc.getHelper(), acc.getEvaluator(), aux));
        }
        
        TargetMappingPanel panel = new TargetMappingPanel(extraTargets, acc.getEvaluator(), acc.getHelper());
        category.setOkButtonListener(panel.getCustomizerOkListener());
        return panel;
    }

}
