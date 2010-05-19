/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.workflow.project;

import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.base.ui.IcanproXmlCustomizerProvider;
import org.netbeans.modules.compapp.projects.base.ui.customizer.CustomizerGeneral;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class WorkflowProjectCustomizerProvider extends 
        IcanproXmlCustomizerProvider
{
    //private IcanproProjectProperties projectProperties;
    private String projectConfigurationNamespace;

    public WorkflowProjectCustomizerProvider(Project project,
            AntProjectHelper helper, ReferenceHelper refHelper)
    {
        super(project, helper, refHelper);
    }

    public WorkflowProjectCustomizerProvider(Project project,
            AntProjectHelper helper, ReferenceHelper refHelper,
            String projectConfigurationNamespace)
    {
        super(project, helper, refHelper, projectConfigurationNamespace);
        this.projectConfigurationNamespace = projectConfigurationNamespace;
    }

    public WorkflowProjectCustomizerProvider(Project project,
            AntProjectHelper helper, ReferenceHelper refHelper,
            String projectConfigurationNamespace, boolean showAllowBetaFeatures)
    {
        super(project, helper, refHelper, projectConfigurationNamespace,
                showAllowBetaFeatures);
        this.projectConfigurationNamespace = projectConfigurationNamespace;
    }

    @Override
    protected Map<ProjectCustomizer.Category,JComponent> createCategoriesMap() {
        ProjectCustomizer.Category advanced = ProjectCustomizer.Category.create(
                ADVANCED,
                NbBundle.getMessage(WorkflowProjectCustomizerProvider.class,
                "LBL_Config_Advanced"), // NOI18N
                null, null);

        Map<ProjectCustomizer.Category,JComponent> map = super.createCategoriesMap();

        getCategories().add(1, advanced);

        AdvancedWorkflowCustomizerPanel advancedPanel
                = new AdvancedWorkflowCustomizerPanel(getAntProjectHelper());

        map.put(advanced, advancedPanel);


        return map;
    }

    @Override
    protected void storeProjectData() {
        super.storeProjectData();
    }

    public static final String ADVANCED = "Advanced"; // NOI18N
}
