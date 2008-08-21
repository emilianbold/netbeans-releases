/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.projectimport.eclipse.gui;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ImportSourceFilters extends ProjectImporterTestCase {
    WizardOperator importWizard; 
    static final String projectName = "ExcludesIncludesProject"; 
    public ImportSourceFilters(String testName) {
        super(testName);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ExtractToWorkDir(getDataDir().getAbsolutePath(),"testdata.jar");
    }

    public void testImportSourceFilters() {
        importProject(projectName);
        validate();
    }

    private void importProject(String projectName) {
        importWizard = invokeImporterWizard();
        selectProjectFromWS(importWizard,"testdata", projectName);
        importWizard.finish();

        waitForProjectsImporting();

        try {
            NbDialogOperator issuesWindow = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.projectimport.eclipse.core.Bundle", "MSG_ImportIssues"));
            issuesWindow.close();
        } catch (Exception e) {
            // ignore 
        }        
    }

    private void validate() {
        pto = new ProjectsTabOperator();
        ProjectRootNode projectRoot = null;
        try {
            projectRoot = pto.getProjectRootNode(projectName);
        } catch(TimeoutExpiredException tex) {
            fail("No project [ "+projectName+" ] loaded");
        }
        projectRoot.properties();
        String propsDialogCaption = Bundle.getString("org.netbeans.modules.apisupport.project.ui.customizer.Bundle", "LBL_CustomizerTitle", new Object[]{projectName});
        NbDialogOperator propsDialog = null;
        try {
            propsDialog = new NbDialogOperator(propsDialogCaption);
        } catch(TimeoutExpiredException tex) {
            fail("Unable to open project [ "+projectName+" ] properties dialog");
        }
        JTreeOperator tree = new JTreeOperator(propsDialog);
        TreePath path = tree.findPath("Sources");
        tree.selectPath(path);
        
        String btnCaption = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.customizer.Bundle", "CustomizerSources.includeExcludeButton");
        JButtonOperator btn = new JButtonOperator(propsDialog,btnCaption);
        btn.pushNoBlock();
        String customizerCaption = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.customizer.Bundle", "CustomizerSources.title.includeExclude");

        NbDialogOperator customizer = new NbDialogOperator(customizerCaption);
        
        JTextFieldOperator includesBox = new JTextFieldOperator(customizer,1);
        JTextFieldOperator excludesBox = new JTextFieldOperator(customizer,0);

        if(!includesBox.getText().contains(menuPath)) {
            fail("Includes doesn't contain expected "+"IncludeOne*.java"+" mask");
        }
        if(!includesBox.getText().contains(menuPath)) {
            fail("Includes doesn't contain expected "+"IncludeTwo*.java"+" mask");
        }
        if(!includesBox.getText().contains(menuPath)) {
            fail("Includes doesn't contain expected "+"IncludeThree*.java"+" mask");
        } 
        if(!excludesBox.getText().contains(menuPath)) {
            fail("Excludes doesn't contain expected "+"ExcludeThree*.java"+" mask");
        }
        if(!excludesBox.getText().contains(menuPath)) {
            fail("Excludes doesn't contain expected "+"ExcludeTwo*.java"+" mask");
        }   
        if(!excludesBox.getText().contains(menuPath)) {
            fail("Excludes doesn't contain expected "+"ExcludeOne*.java"+" mask");
        }           
        customizer.close();
    }
}
