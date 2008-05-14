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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package startup;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;


public class IDESetupTest extends JellyTestCase {
    
    public IDESetupTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  ########");
    }
    
    public void closeAllDocuments(){
        new CloseAllDocumentsAction().perform();
    }    
    
    
    /** 
     * Close Memory Toolbar. 
     */
    public void testCloseMemoryToolbar(){
        String MENU = 
            org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/View") + "|" +
            org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle","CTL_ToolbarsListAction") + "|" +
            org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Toolbars/Memory");
        
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        JMenuBarOperator menuBar = new JMenuBarOperator(mainWindow.getJMenuBar());
        JMenuItemOperator menuItem = menuBar.showMenuItem(MENU,"|");
        
        if(menuItem.isSelected())
            menuItem.push();
        else {
            menuItem.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
            mainWindow.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        }
        
    }
    
     public void testAddAppServer() {
        String path = System.getProperty("j2ee.appserver.path");
        if (path == null) {
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
                path = "E:\\space\\AppServer";
                //path =  "E:\\Sun\\AppServer-8.1_01_b04";
            } else {
                path = "/space/appserver";
            }
        }
        String username = System.getProperty("j2ee.appserver.username","admin");
        String password = System.getProperty("j2ee.appserver.password","adminadmin");
                    
        Node node = new Node(new RuntimeTabOperator().getRootNode(),"Servers");
        node.performPopupActionNoBlock("Add Server...");
        NbDialogOperator dialog = new NbDialogOperator("Add Server");
        new JComboBoxOperator(dialog).selectItem("Sun Java System Application Server");
        new JButtonOperator(dialog,"Next").push();
        
        //"Enter the Application Server location" or "Define Application Server Instance Properties"
       /* if (new JLabelOperator(dialog,1).getText().equalsIgnoreCase("Enter the Application Server location")) {
            new JTextFieldOperator(dialog).setText("");
            new JTextFieldOperator(dialog).typeText(path);
            new JButtonOperator(dialog,"Next").push();
        }*/
         if (new JLabelOperator(dialog,1).getText().equalsIgnoreCase("Platform Folder Location")) {
            new JTextFieldOperator(dialog).setText("");
            new JTextFieldOperator(dialog).typeText(path);
            new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
            new JButtonOperator(dialog,"Next").push();
        }
        new JTextFieldOperator(dialog,0).setText("");
        new JTextFieldOperator(dialog,1).setText("");
        new JTextFieldOperator(dialog,0).typeText(username);
        new JTextFieldOperator(dialog,1).typeText(password);
        new JButtonOperator(dialog,"Finish").push();
        
        
        new ProjectsTabOperator();
    }
    

    
    
}
