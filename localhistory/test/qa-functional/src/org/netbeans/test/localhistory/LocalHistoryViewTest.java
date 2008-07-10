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
/*
 * LocalHistoryViewTest.java
 *
 * Created on February 2, 2007, 1:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author peter
 */
package org.netbeans.test.localhistory;

import java.io.File;
import java.io.PrintStream;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.localhistory.operators.OutlineViewOperator;
import org.netbeans.test.localhistory.operators.ShowLocalHistoryOperator;
import org.netbeans.test.localhistory.utils.TestKit;



/**
 * @author pvcs
 */
public class LocalHistoryViewTest extends JellyTestCase {
    
    public static final String TMP_PATH = "/tmp";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator; 
    
    /** Creates a new instance of LocalHistoryViewTest */
    public LocalHistoryViewTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {        
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### "+getName()+" ###");
        
    }
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(LocalHistoryViewTest.class).addTest(
        "testLocalHistoryInvoke").enableModules(".*").clusters(".*"));
    }
    
    public void testLocalHistoryInvoke() throws Exception {
//        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
//        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
//        TestKit.closeProject(PROJECT_NAME);
//        
//        new File(TMP_PATH).mkdirs();
//        projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);
//        ProjectSupport.waitScanFinished();
        openDataProjects(PROJECT_NAME);
        Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");    
        
        node.performPopupAction("Open");
        EditorOperator eo = new EditorOperator("Main.java");
        eo.deleteLine(2);
        eo.saveDocument();

        ShowLocalHistoryOperator slho = ShowLocalHistoryOperator.invoke(node);
        slho.verify();
        
        slho.performPopupAction(1, "Revert from History");
        Thread.sleep(1000);
        int versions=slho.getVersionCount();
        assertEquals("1. Wrong number of versions!", 2, versions);
        
        slho.performPopupAction(2, "Delete from History");        
        Thread.sleep(500);

        versions=slho.getVersionCount();
        assertEquals("2. Wrong number of versions!", 1, versions);
        
        eo.insert("// modification //", 11, 1);
        eo.save();
        
        Thread.sleep(500);
        versions=slho.getVersionCount();
        assertEquals("3. Wrong number of versions!", 2, versions);
        slho.close();
        
        TestKit.createNewPackage(PROJECT_NAME, "NewPackage");
        TestKit.createNewElement(PROJECT_NAME, "NewPackage", "NewClass");
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "NewPackage|NewClass.java");
        node.performPopupAction("Open");
        eo = new EditorOperator("NewClass.java");
        eo.deleteLine(5);
        eo.insert(os_name, 12, 1);
        eo.saveDocument();
        String fileContent=eo.getText();
        
        slho = ShowLocalHistoryOperator.invoke(node);
        Thread.sleep(500);
        versions = slho.getVersionCount();
        assertEquals("4. Wrong number of versions!", 1, versions);
        slho.close();
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "NewPackage");
        node.performPopupActionNoBlock("Delete");
        NbDialogOperator dialog = new NbDialogOperator("Confirm Object Deletion");
        dialog.yes();
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "");
        node.performPopupAction("Local History|Revert Deleted");

        node = new Node(new SourcePackagesNode(PROJECT_NAME), "NewPackage|NewClass.java");
        slho = ShowLocalHistoryOperator.invoke(node);        
        Thread.sleep(500);
        versions = slho.getVersionCount();
        assertEquals("5. Wrong number of versions!", 2, versions);
        node.performPopupAction("Open");
        eo = new EditorOperator("NewClass.java");
        assertEquals("Content of file differs after revert!", fileContent, eo.getText());
        eo.deleteLine(5);
        eo.insert(os_name, 12, 1);
        eo.save();        
        Thread.sleep(500);
        versions=slho.getVersionCount();
        assertEquals("6. Wrong number of versions!", 3, versions);
        closeOpenedProjects();
    }
}
