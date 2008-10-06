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

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.dialogs.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureJ2SEDialogsTest {


    public static NbTestSuite suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("UI Responsiveness J2SE Dialogs suite");
        System.setProperty("suitename", "org.netbeans.performance.j2se.MeasureJ2SEDialogsTest");

        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(About.class)
        .addTest(AddJDBCDriverDialog.class)
        .addTest(AddProfilingPointWizard.class)
        .addTest(AddServerInstanceDialog.class)
        //--.addTest(AddXMLandDTDSchemaCatalog.class)
        .addTest(AttachDialog.class)
        //--.addTest(AutoCommentWindow.class)
        //--.addTest(CompareMemorySnapshotsDialog.class)
        .addTest(CreateTestsDialog.class)
        .addTest(DeleteFileDialog.class)
        .addTest(DocumentsDialog.class)
        .addTest(FavoritesWindow.class)
        .addTest(FilesWindow.class)
        .addTest(FindInProjects.class)
        //--.addTest(FindInSourceEditor.class)
        //--.addTest(GotoClassDialog.class)
        .addTest(GotoLineDialog.class)
        .addTest(HelpContentsWindow.class)
        .addTest(HttpMonitorWindow.class)
        .addTest(InternationalizeDialog.class)
        .addTest(JavaPlatformManager.class)
        .addTest(JavadocIndexSearch.class)
        .addTest(LibrariesManager.class)
        .addTest(NetBeansPlatformManager.class)
        .addTest(NewBreakpointDialog.class)
        .addTest(NewDatabaseConnectionDialog.class)
        //--.addTest(NewFileDialog.class)
        .addTest(NewProjectDialog.class)
        .addTest(NewWatchDialog.class)
        .addTest(OpenFileDialog.class)
        .addTest(OpenProjectDialog.class)
        .addTest(Options.class)
        .addTest(OutputWindow.class)
        //--.addTest(OverrideMethods.class)
        .addTest(PluginManager.class)
        //--.addTest(ProfilerAboutDialog.class)
        //--.addTest(ProfilerCalibrationDialog.class)
        .addTest(ProfilerWindows.class)
        .addTest(ProjectPropertiesWindow.class)
        .addTest(ProjectsWindow.class)
        .addTest(PropertyEditorColor.class)
        //--.addTest(PropertyEditors.class)
        //--.addTest(PropertyEditorString.class)
        .addTest(ProxyConfiguration.class)
        .addTest(RefactorFindUsagesDialog.class)
        .addTest(RefactorMoveClassDialog.class)
        //--.addTest(RefactorRenameDialog.class)
        .addTest(RuntimeWindow.class)
        .addTest(SelectProfilingTaskDialog.class)
        .addTest(ServerManager.class)
        //--.addTest(SetDefaultServerDialog.class)
        .addTest(TemplateManager.class)
        .addTest(ToDoWindow.class)
        .addTest(VersioningWindow.class)
        .enableModules(".*").clusters(".*").reuseUserDir(true)));
  
        return suite;
    }
    
}
