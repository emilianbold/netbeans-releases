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

package org.netbeans.modules.java.freeform.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.java.freeform.LookupProviderImpl;
import org.netbeans.modules.java.freeform.jdkselection.JdkConfiguration;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class ClasspathCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    /** Creates a new instance of ClasspathCategoryProvider */
    public ClasspathCategoryProvider() {
    }
    
    public Category createCategory(Lookup context) {
        AuxiliaryConfiguration aux = context.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        if (LookupProviderImpl.isMyProject(aux)) {
            Category cat = ProjectCustomizer.Category.create("classpath", //NOI18N
                    NbBundle.getMessage(ClasspathPanel.class, "LBL_ProjectCustomizer_Category_Classpath"), null, new Category[] { null });
            return cat;
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
        
        final JdkConfiguration jdkConf = new JdkConfiguration(project, acc.getHelper(), acc.getEvaluator());
        
        final ClasspathPanel panel = new ClasspathPanel(jdkConf);
        final JavaPlatform initialPlatform = (JavaPlatform) panel.javaPlatform.getSelectedItem();
        ProjectModel pm = context.lookup(ProjectModel.class);
        assert pm != null;
        panel.setModel(pm);
        pm.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                panel.updateControls();
            }
        });
        category.setOkButtonListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JavaPlatform p = (JavaPlatform) panel.javaPlatform.getSelectedItem();
                if (p != initialPlatform) {
                    try {
                        jdkConf.setSelectedPlatform(p);
                    } catch (IOException x) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, x);
                    }
                }
            }
        });
        return panel;
        
    }

}
