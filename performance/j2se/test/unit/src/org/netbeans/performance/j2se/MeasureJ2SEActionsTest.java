/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.performance.j2se;


import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.performance.j2se.actions.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureJ2SEActionsTest {

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("UI Responsiveness J2SE Actions suite");

        /* TBD
        suite.addTest(NbModuleSuite.create(AddToFavorites.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(CloseAllEditors.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(CloseEditor.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(CloseEditorModified.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(CloseEditorTab.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(CreateNBProject.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(CreateProject.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(DeleteFolder.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(ExpandNodesInComponentInspector.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(ExpandNodesProjectsView.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(JavaCompletionInEditor.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OpenFiles.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OpenFilesNoCloneableEditor.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OpenFilesNoCloneableEditorWithOpenedEditor.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OpenFilesWithOpenedEditor.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OpenFormFile.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OpenFormFileWithOpenedEditor.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OpenJspFile.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OpenJspFileWithOpenedEditor.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(PageUpPageDownInEditor.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(PasteInEditor.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(RefactorFindUsages.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(SaveModifiedFile.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(SelectCategoriesInNewFile.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(SwitchToFile.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(SwitchView.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(TypingInEditor.class, ".*", ".*"));
*/       
        return suite;
    }
    
}
