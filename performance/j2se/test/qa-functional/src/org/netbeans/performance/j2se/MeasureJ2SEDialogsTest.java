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
import org.netbeans.performance.j2se.dialogs.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureJ2SEDialogsTest {


    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("UI Responsiveness J2SE Dialogs suite");

        suite.addTest(NbModuleSuite.create(About.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(AddJDBCDriverDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(AddProfilingPointWizard.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(AddServerInstanceDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(AddXMLandDTDSchemaCatalog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(AttachDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(AutoCommentWindow.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(CompareMemorySnapshotsDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(CreateTestsDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(DeleteFileDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(DocumentsDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(FavoritesWindow.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(FilesWindow.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(FindInProjects.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(FindInSourceEditor.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(GotoClassDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(GotoLineDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(HelpContentsWindow.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(HttpMonitorWindow.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(InternationalizeDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(JavaPlatformManager.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(JavadocIndexSearch.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(LibrariesManager.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(NetBeansPlatformManager.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(NewBreakpointDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(NewDatabaseConnectionDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(NewFileDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(NewProjectDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(NewWatchDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OpenFileDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OpenProjectDialog.class, ".*", ".*"));        
        suite.addTest(NbModuleSuite.create(Options.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OutputWindow.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(OverrideMethods.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(PluginManager.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(ProfilerAboutDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(ProfilerCalibrationDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(ProfilerWindows.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(ProjectPropertiesWindow.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(ProjectsWindow.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(PropertyEditorColor.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(PropertyEditorString.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(PropertyEditors.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(ProxyConfiguration.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(RefactorFindUsagesDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(RefactorMoveClassDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(RefactorRenameDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(RuntimeWindow.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(SelectProfilingTaskDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(ServerManager.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(SetDefaultServerDialog.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(TemplateManager.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(ToDoWindow.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(VersioningWindow.class, ".*", ".*"));
  
        return suite;
    }
    
}
