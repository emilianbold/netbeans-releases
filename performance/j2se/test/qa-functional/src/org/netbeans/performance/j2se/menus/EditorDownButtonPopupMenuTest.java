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

package org.netbeans.performance.j2se.menus;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test List Of The Recent Opened Windows popup menu on Editor Window down button if 10 files opened
 * @author  juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class EditorDownButtonPopupMenuTest extends PerformanceTestCase {

    /** Test of popup menu on Editor's 'Down Button' */
    public EditorDownButtonPopupMenuTest(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN=200;
    }
    
    /** Test of popup menu on Editor's 'Down Button' */
    public EditorDownButtonPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN=200;
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(EditorDownButtonPopupMenuTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }
    
    public void testEditorDownButtonPopupMenu(){
        doMeasurement();
    }
    
    @Override
    public void initialize(){
        CommonUtilities.openFiles("PerformanceTestFoldersData", getTenSelectedFiles());
    }
    
    public void prepare(){
    }
    
    public ComponentOperator open(){
        EditorWindowOperator.btDown().clickForPopup();
        ComponentOperator popupComponent = new ComponentOperator(EditorWindowOperator.btDown().getContainer(ComponentSearcher.getTrueChooser("org.netbeans.core.windows.view.ui.RecentViewListDlg")));
        return popupComponent;
  }
    
    @Override
    public void shutdown(){
        EditorWindowOperator.closeDiscard();
    }
    
    private static String[][] getTenSelectedFiles(){
        String[][] files_path = {
            {"folders.javaFolder100","AboutDialogTest.java"},
            {"folders.javaFolder100","AttachDialogTest.java"},
            {"folders.javaFolder100","CreateTestsDialogTest.java"},
            {"folders.javaFolder100","DeleteFileDialogTest.java"},
            {"folders.javaFolder100","FilesWindowTest.java"},
            {"folders.javaFolder50","ServerManagerTest.java"},
            {"folders.javaFolder50","RuntimeWindowTest.java"},
            {"folders.javaFolder50","PluginManagerTest.java"},
            {"folders.javaFolder50","OptionsTest.java"},
            {"folders.javaFolder50","ProfilerWindowsTest.java"},
            {"folders.javaFolder50","NewFileDialogTest.java"}
        };
        return files_path;
    }
    
}
