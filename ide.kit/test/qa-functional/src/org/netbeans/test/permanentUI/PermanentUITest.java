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

package org.netbeans.test.permanentUI;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Lukas Hasik
 */
public class PermanentUITest {
    public static Test suite() {
    return NbModuleSuite.create(
      NbModuleSuite.emptyConfiguration()
        .clusters(".*").enableModules(".*")
        .addTest(MainMenuTest.class, 
                  "testFileMenu", //stable
                  "testEditMenu", //stable
                  "testViewMenu", //stable
                  //"testNavigateMenu",//#139450
                  "testSourceMenu",//stable
                  "testRefactorMenu",//stable
                  "testRunMenu",//stable                  
                  "testDebugMenu",//stable
                  "testHelpMenu",//stable
                  //"testToolsMenu",//#146446
                  "testVersioningMenu",//stable
                  "testWindowMenu",//stable
                  "testMnemonicsCollision",//stable

                  "testFile_ProjectGroupSubMenu",  //stable                
                  "testNavigate_InspectSubMenu", //stable
                  //"testView_CodeFoldsSubMenu",//???
                  //"testView_ToolbarsSubMenu",//#149237
                  //"testProfile_AdvancedCommandsSubMenu",
                  //"testProfile_ProfileOtherSubMenu",
                  "testRun_SetMainProjectSubMenu",
                  "testDebug_StackSubMenu",
                  //"testSource_PreprocessorBlocksSubMenu",
                  "testTools_InternationalizationSubMenu",
                  "testTools_PaletteSubMenu",
                  //"testVersioning_CVSSubMenu",
                  //"testVersioning_CVS_BranchesSubMenu",
                  //"testVersioning_LocalHistorySubMenu",
                  //"testVersioning_Mercurial_MergeSubMenu",
                  //"testVersioning_Mercurial_RecoverSubMenu",
                  //"testVersioning_Mercurial_ShareSubMenu",
                  //"testVersioning_Mercurial_ShowSubMenu",
                  "testWindow_DebuggingSubMenu",
                  "testWindow_NavigatingSubMenu",
                  "testWindow_OtherSubMenu",
                  "testWindow_OutputSubMenu",
                  "testWindow_ProfilingSubMenu",
                  "testWindow_VersioningSubMenu")
        .addTest(NewProjectTest.class)
        .addTest(OptionsTest.class)
    );
  }

}
