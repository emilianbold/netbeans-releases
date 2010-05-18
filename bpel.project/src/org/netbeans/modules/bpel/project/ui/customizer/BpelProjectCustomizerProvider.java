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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.project.ui.customizer;

import java.util.Map;
import javax.swing.JComponent;

import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.NbBundle;

import org.netbeans.modules.xml.catalogsupport.ui.customizer.CustomizerProviderImpl;

import org.netbeans.modules.bpel.project.BpelproProject;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;

/** 
 * Customization of BPEL project
 *
 * @author Ajit Bhate
 */
public class BpelProjectCustomizerProvider extends CustomizerProviderImpl {
    
    private static final String GENERAL = "General";
    private IcanproProjectProperties projectProperties;

    public BpelProjectCustomizerProvider(BpelproProject project) {
        super(project,project.getAntProjectHelper(),project.getReferenceHelper());
    }
    
    @Override
    protected Map<ProjectCustomizer.Category,JComponent> createCategoriesMap() {
        ProjectCustomizer.Category general = ProjectCustomizer.Category.create(
                GENERAL,
                NbBundle.getMessage(BpelProjectCustomizerProvider.class, "LBL_Config_General"), // NOI18N
                null);
        getCategories().add(general);
        Map<ProjectCustomizer.Category,JComponent> map = super.createCategoriesMap();
        projectProperties = ((BpelproProject)getProject()).getProjectProperties();
        CustomizerGeneral generalPanel = new CustomizerGeneral(projectProperties);
        generalPanel.initValues();
        map.put(general, generalPanel);
        return map;
    }

    @Override
    protected void storeProjectData() {
        super.storeProjectData();
        projectProperties.store();
    }
}
