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

package org.netbeans.modules.web.freeform.ui;

import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.web.freeform.LookupProviderImpl;
import org.netbeans.modules.web.freeform.WebProjectGenerator;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class WebClasspathCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    /** Creates a new instance of EjbLocationsCategoryProvider */
    public WebClasspathCategoryProvider() {
    }
    
    public Category createCategory(Lookup context) {
        AuxiliaryConfiguration aux = (AuxiliaryConfiguration)context.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        if (LookupProviderImpl.isMyProject(aux)) {
            Category cat = ProjectCustomizer.Category.create("WebClasspath", //NOI18N
                    NbBundle.getMessage(WebClasspathPanel.class, "LBL_ProjectCustomizer_Category_Classpath"), null, null);
            return cat;
        }
        return null;
    }

    public JComponent createComponent(Category category, Lookup context) {
        Project project = (Project)context.lookup(Project.class);
        ProjectAccessor acc = (ProjectAccessor)context.lookup(ProjectAccessor.class);
        AuxiliaryConfiguration aux = (AuxiliaryConfiguration)context.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        assert acc != null;
        assert project != null;
        
        WebClasspathPanel panel = new WebClasspathPanel(false);
        List l = WebProjectGenerator.getWebmodules(acc.getHelper(), aux);
        if (l != null){
            WebProjectGenerator.WebModule wm = (WebProjectGenerator.WebModule)l.get(0);
            panel.setProjectFolders(Util.getProjectLocation(acc.getHelper(), acc.getEvaluator()),
                    FileUtil.toFile(acc.getHelper().getProjectDirectory()));
            panel.setClasspath(wm.classpath, acc.getEvaluator());
            panel.updateButtons();
        }

        category.setOkButtonListener(panel.getCustomizerOkListener(acc.getHelper()));
        return panel;
    }

}
