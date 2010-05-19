/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.projects.common.ui.actions;

import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.common.Helper;
import org.netbeans.modules.compapp.projects.common.ui.ImplicitReferenceCreator;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

/**
 * This class implements project action performer interface which can be used
 * to add entries into implicit catalog. This can be used to add project
 * actions for this.
 * User should use the #createProjectSensitiveAction() to create an instance
 * of Action that delegates the work to this implemenation.
 * @author chikkala
 */
public class AddImplicitCatalogEntryActionPerformer implements ProjectActionPerformer {
    /**  command that can be used in the project action provider to add the 
    action that can add implicit catalog entries */    
    public static final String COMMAND = "add-implicit-catalog-entries";
    public boolean enable(Project project) {
        assert project != null;
        return Helper.hasImplicitCatalogSupport(project);
    }

    public void perform(Project project) {
        assert project != null;
        String title = NbBundle.getMessage(AddImplicitCatalogEntryActionPerformer.class, "TITLE_SelectNamespaces");
        ImplicitReferenceCreator refCreator = ImplicitReferenceCreator.newInstance(project);
        Object result = ImplicitReferenceCreator.showSelectionDialog(refCreator, title);
        if (result == DialogDescriptor.OK_OPTION) {
            refCreator.addSelectedNamespacesToCatalog(project);
        }
    }

    /**
     * use this to create a actions to the project sensitive list actions.
     * @return
     */
    public static Action createProjectSensitiveAction() {
        ProjectActionPerformer performer = new AddImplicitCatalogEntryActionPerformer();
        String name = NbBundle.getMessage(AddImplicitCatalogEntryActionPerformer.class, "LBL_AddImplicitCatalogEntryAction");
        return ProjectSensitiveActions.projectSensitiveAction(performer, name, null);
    }
}
