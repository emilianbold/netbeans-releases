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

package org.netbeans.performance.j2se.actions;

import java.io.File;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
/**
 * Test of Delete nodes/folders in the Explorer.
 *
 * @author  mmirilovic@netbeans.org
 */
public class DeleteFolderTest extends PerformanceTestCase {
    
    /** Projects tab */
    private static ProjectsTabOperator projectTab;
    
    /** Name of the folder which test creates and expands */
    private static String folderToBeDeleted;
    
    /** Node represantation of the folder which test creates and delete */
    private static Node nodeToBeDeleted;
    
    
    /**
     * Creates a new instance of DeleteFolder
     * @param testName the name of the test
     */
    public DeleteFolderTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of DeleteFolder
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public DeleteFolderTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(DeleteFolderTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }
    
    public void testDeleteFolderWith50JavaFiles(){
        WAIT_AFTER_PREPARE = 10000;
        WAIT_AFTER_OPEN = 20000;
        folderToBeDeleted = "javaFolder50";
        doMeasurement();
    }
    
    public void testDeleteFolderWith100JavaFiles(){
        WAIT_AFTER_PREPARE = 10000;
        WAIT_AFTER_OPEN = 20000;
        folderToBeDeleted = "javaFolder100";
        doMeasurement();
    }
    
    @Override
    public void initialize(){
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);
        repaintManager().addRegionFilter(repaintManager().EXPLORER_FILTER);
    }
    
    public ComponentOperator open(){
        new DeleteAction().performPopup(nodeToBeDeleted);
        new NbDialogOperator("Delete").ok();
        return null;
    }
    
    @Override
    public void close(){
    }
    
    public void prepare(){
        try {
            String projectPath = this.getDataDir() + File.separator+ "PerformanceTestFoldersData";
            log("========== Projects path =" + projectPath);
            
            File foldersDir = new File(projectPath + File.separator + "src" + File.separator + "folders");
            log("========== Folders path ="+foldersDir.getPath());
            
            
            File originalDir = new File(foldersDir, folderToBeDeleted);
            File copyDeleteDir = new File(foldersDir, folderToBeDeleted + "_delete");
            copyDeleteDir.mkdir();
            
            log("========== Original Dir path =" + originalDir.getPath());
            log("========== Copy Delete Dir path =" + copyDeleteDir.getPath());

            CommonUtilities.copyFile(new File (foldersDir, "Test.java"),new File (copyDeleteDir, "Test.java"));
            waitNoEvent(1000);
            
            SourcePackagesNode sourcePackagesNode = new SourcePackagesNode("PerformanceTestFoldersData");
            
            Node foldersNode = new Node(sourcePackagesNode, "folders");
            foldersNode.expand();
            
            /* no more present after retouche merge
            new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenu(Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/File")+"|"+Bundle.getStringTrimmed("org.netbeans.modules.javacore.Bundle","LBL_RescanAction"),"|"); //File|Refresh All Files
            waitNoEvent(500);
             */

            nodeToBeDeleted = new Node(sourcePackagesNode, "folders." + folderToBeDeleted + "_delete");
            
            File[] files = originalDir.listFiles();
            log("=============== There is [" + files.length + "] number of files in directory = "+ originalDir.getPath());
            
            File copyFile;
            
            for(int i=0; i<files.length; i++) {
                copyFile = new File(copyDeleteDir,files[i].getName());
                log("================== Create file ="+copyFile.getPath());
                
                CommonUtilities.copyFile(files[i], copyFile);
            }
            
            /* no more present after retouche merge
            new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenu(Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/File")+"|"+Bundle.getStringTrimmed("org.netbeans.modules.javacore.Bundle","LBL_RescanAction"),"|"); //File|Refresh All Files
            waitNoEvent(500);
             */
            
            nodeToBeDeleted.expand();
            
        }catch(Exception exc){
            log("========================================= \nImpossible create files: "+exc.getMessage());
            exc.printStackTrace(getLog());
            log("\n=========================================");
        }
    }

    @Override
    public void shutdown() {
        new RestoreWindowAction().performAPI(projectTab);
        repaintManager().resetRegionFilters();
    }

}
