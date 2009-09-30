/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.common.Helper;
import org.netbeans.modules.compapp.projects.common.ImplicitCatalogValidator;
import org.netbeans.modules.compapp.projects.common.ImplicitCatalogValidator.ResultPrinter;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * This class implements project action performer interface which can be used
 * to do implicit catalog validation action. This can be used to add project
 * actions.
 * User should use the #createProjectSensitiveAction() to create an instance
 * of Action that delegates the work to this implemenation.
 * @author chikkala
 */
public class ValidateImplicitCatalogActionPerformer implements ProjectActionPerformer {

    /**  command that can be used in the project action provider to add the 
    action that can validate the implicit catalog entries */
    public static final String COMMAND = "validate-implicit-catalog";

    public boolean enable(Project project) {
        assert project != null;
        return Helper.hasImplicitCatalogSupport(project);
    }

    public void perform(Project project) {
        
        ImplicitCatalogValidator validator = null;
        ResultPrinter prn = null;
        final String outputTitle = 
                NbBundle.getMessage(ValidateImplicitCatalogActionPerformer.class, "icat.validator.output.title");
        
        prn = new ResultPrinter() {

            @Override
            protected void initWriters() {
                InputOutput inOut = IOProvider.getDefault().getIO(outputTitle, false);
                setOutWriter(inOut.getOut());
                setErrorWriter(inOut.getErr());
            }
        };

        try {
            validator = ImplicitCatalogValidator.newInstance(project);
            validator.setResultPrinter(prn);
            validator.validate();
        } catch (Exception ex) {
            prn.println(ex.getMessage());
        // Exceptions.printStackTrace(ex);            
        }

    }

    /**
     * 
     * @return
     */
    public static Action createProjectSensitiveAction() {
        ProjectActionPerformer performer = new ValidateImplicitCatalogActionPerformer();
        String name = NbBundle.getMessage(ValidateImplicitCatalogActionPerformer.class, "LBL_ValidateImplicitCatalogAction");
        return ProjectSensitiveActions.projectSensitiveAction(performer, name, null);
    }
}
