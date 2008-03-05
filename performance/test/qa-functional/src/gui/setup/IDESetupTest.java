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

package gui.setup;

import gui.Utilities;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.BuildProjectAction;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.jellytools.RuntimeTabOperator;

import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Test suite that actually does not perform any test but sets up user directory
 * for UI responsiveness tests
 *
 * @author  mmirilovic@netbeans.org
 */
public class IDESetupTest extends org.netbeans.jellytools.JellyTestCase {
    
    public IDESetupTest(java.lang.String testName) {
        super(testName);
    }
    
    public void openDataProject() {
        Utilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir") + java.io.File.separator + "PerformanceTestData");
    }
    
    public void openWebProject() {
        Utilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir") + java.io.File.separator + "PerformanceTestWebApplication");
    }
    
    public void openFoldersProject() {
        Utilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir") + java.io.File.separator + "PerformanceTestFoldersData");
    }
    
    public void openNBProject() {
        Utilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir") + java.io.File.separator + "SystemProperties");
    }
    
    /**
     * Close Welcome.
     */
    public void closeWelcome(){
        Utilities.closeWelcome();
    }
    
    /**
     * Close BluePrints.
     */
    public void closeBluePrints(){
        Utilities.closeBluePrints();
    }
    
    /**
     * Close All Documents.
     */
    public void closeAllDocuments(){
        Utilities.closeAllDocuments();
    }
    
    /**
     * Close Memory Toolbar.
     */
    public void closeMemoryToolbar(){
        Utilities.closeMemoryToolbar();
    }

    public void testAddAppServer() {

        String appServerPath = System.getProperty("com.sun.aas.installRoot");
        String addServerMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"); // Add Server...
        String addServerInstanceDialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title"); //"Add Server Instance"
        String serverItem = "Tomcat 6.0";
        String nextButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT");
        String finishButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH");

        RuntimeTabOperator rto = RuntimeTabOperator.invoke();        
        JTreeOperator runtimeTree = rto.tree();
        
        long oldTimeout = runtimeTree.getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 60000);
        
        TreePath path = runtimeTree.findPath("Servers");
        runtimeTree.selectPath(path);
        
        try {
            //log("Let's check whether GlassFish V2 is already added");
            runtimeTree.findPath("Servers|Tomcat 6.0");
        } catch (TimeoutExpiredException tee) {
            //log("There is no GlassFish V2 node so we'll add it");
            
            new JPopupMenuOperator(runtimeTree.callPopupOnPath(path)).pushMenuNoBlock(addServerMenuItem);
            NbDialogOperator addServerInstanceDialog = new NbDialogOperator(addServerInstanceDialogTitle);
            new JListOperator(addServerInstanceDialog,1).selectItem(serverItem);
            new JButtonOperator(addServerInstanceDialog,nextButtonCaption).push();
            new JTextFieldOperator(addServerInstanceDialog,1).enterText(appServerPath);
            new JCheckBoxOperator(addServerInstanceDialog,1).changeSelection(false);
            new JButtonOperator(addServerInstanceDialog,finishButtonCaption).push();
        }
        
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", oldTimeout);

    }


}
