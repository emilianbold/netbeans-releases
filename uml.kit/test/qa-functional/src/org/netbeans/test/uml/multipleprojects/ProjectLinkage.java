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


package org.netbeans.test.uml.multipleprojects;
import java.awt.event.KeyEvent;
import java.io.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.multipleprojects.utils.MUPUtils;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.Utils;
import org.netbeans.test.umllib.testcases.UMLTestCase;


/**
 *
 * @author yaa
 * @spec uml/FindReplaceAssociate.xml
 */
public class ProjectLinkage extends UMLTestCase {
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    private EventTool eventTool = new EventTool();
    
    /** Need to be defined because of JUnit */
    public ProjectLinkage(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.multipleprojects.ProjectLinkage.class);
        return suite;
    }
    
    public void testMoveElementToTree(){
        String prName1 = "UMLProject1A";
        String prName2 = "UMLProject1B";
        String class1 = "C1";
        
        try{
            Utils.createJavaUMLProject(prName1, workDir);
            MUPUtils.createElement(prName1, class1, ElementTypes.CLASS.toString());
            Utils.createJavaUMLProject(prName2, workDir);
            
            if (!MUPUtils.moveElementInTree(prName1,  prName2, class1)){
                fail("Test fails on moving element");
            }
            if ((MUPUtils.selectElementInModelTree(prName1, class1) != null) ||
                (MUPUtils.selectElementInModelTree(prName2, class1) == null))
            {
                fail("Checking test results fail");
            }
        }catch(Exception e){
            fail("Test failed with unknown reason: " + e.getMessage());
        }
    }

    public void testImportElementToTree(){
        String prName1 = "UMLProject2A";
        String prName2 = "UMLProject2B";
        String class1 = "C2";
        
        try{
            Utils.createJavaUMLProject(prName1, workDir);
            MUPUtils.createElement(prName1, class1, ElementTypes.CLASS.toString());
            Utils.createJavaUMLProject(prName2, workDir);
            
            if (!MUPUtils.importElementInTree(prName1,  prName2, class1)){
                fail("Test fails on moving element");
            }
            if ((MUPUtils.selectElementInModelTree(prName1, class1) == null) ||
                (MUPUtils.selectElementInModelTree(prName2, class1) != null) ||
                (MUPUtils.selectElementInImportTree(prName2, prName1 + "|" + class1) == null))
            {
                fail("Checking test results fail");
            }
        }catch(Exception e){
            fail("Test failed with unknown reason: " + e.getMessage());
        }
    }
    
    public void testImportElementToDiagram(){
        String prName1 = "UMLProject3A";
        String prName2 = "UMLProject3B";
        String cld = "CLD3";
        String class1 = "C3";
        
        try{
            Utils.createJavaUMLProject(prName1, workDir);
            MUPUtils.createElement(prName1, class1, ElementTypes.CLASS.toString());
            Utils.createJavaUMLProject(prName2, workDir);
            DiagramOperator dia = MUPUtils.openDiagram(prName2, cld, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
            
            if (!MUPUtils.importElementToDiagram(prName1,  prName2, class1, cld)){
                fail("Test fails on moving element");
            }
            DiagramElementOperator el = new DiagramElementOperator(dia, class1);
            CompartmentOperator co = new CompartmentOperator(el, CompartmentTypes.PACKAGE_IMPORT_COMPARTMENT);
            if ((MUPUtils.selectElementInModelTree(prName1, class1) == null) ||
                (MUPUtils.selectElementInModelTree(prName2, class1) != null) ||
                (MUPUtils.selectElementInImportTree(prName2, prName1 + "|" + class1) == null) ||
                (!co.getName().equals("{ Imported from " + prName1 + " }")))
            {
                fail("Checking test results fail");
            }
        }catch(Exception e){
            fail("Test failed with unknown reason: " + e.getMessage());
        }
    }

    public void testMovePackageToTree(){
        String prName1 = "UMLProject4A";
        String prName2 = "UMLProject4B";
        String pkg = "pkg4";
        
        try{
            Utils.createJavaUMLProject(prName1, workDir);
            MUPUtils.createPackage(prName1, pkg);
            Utils.createJavaUMLProject(prName2, workDir);
            
            if (!MUPUtils.moveElementInTree(prName1,  prName2, pkg)){
                fail("Test fails on moving element");
            }
            if ((MUPUtils.selectElementInModelTree(prName1, pkg) != null) ||
                (MUPUtils.selectElementInModelTree(prName2, pkg) == null))
            {
                fail("Checking test results fail");
            }
        }catch(Exception e){
            fail("Test failed with unknown reason: " + e.getMessage());
        }
    }

    public void testImportPackageToTree(){
        String prName1 = "UMLProject5A";
        String prName2 = "UMLProject5B";
        String pkg = "pkg5";
        
        try{
            Utils.createJavaUMLProject(prName1, workDir);
            MUPUtils.createPackage(prName1, pkg);
            Utils.createJavaUMLProject(prName2, workDir);
            
            if (!MUPUtils.importElementInTree(prName1,  prName2, pkg)){
                fail("Test fails on moving element");
            }
            if ((MUPUtils.selectElementInModelTree(prName1, pkg) == null) ||
                (MUPUtils.selectElementInModelTree(prName2, pkg) != null) ||
                (MUPUtils.selectElementInImportTree(prName2, prName1 + "|" + pkg) == null))
            {
                fail("Checking test results fail");
            }
        }catch(Exception e){
            fail("Test failed with unknown reason: " + e.getMessage());
        }
    }
    
    public void testImportPackageToDiagram(){
        String prName1 = "UMLProject6A";
        String prName2 = "UMLProject6B";
        String cld = "CLD6";
        String pkg = "pkg6";
        
        try{
            Utils.createJavaUMLProject(prName1, workDir);
            MUPUtils.createPackage(prName1, pkg);
            Utils.createJavaUMLProject(prName2, workDir);
            DiagramOperator dia = MUPUtils.openDiagram(prName2, cld, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
            
            if (!MUPUtils.importElementToDiagram(prName1,  prName2, pkg, cld)){
                fail("Test fails on moving element");
            }
            DiagramElementOperator el = new DiagramElementOperator(dia, pkg);
            CompartmentOperator co = new CompartmentOperator(el, CompartmentTypes.PACKAGE_IMPORT_COMPARTMENT);
            if ((MUPUtils.selectElementInModelTree(prName1, pkg) == null) ||
                (MUPUtils.selectElementInModelTree(prName2, pkg) != null) ||
                (MUPUtils.selectElementInImportTree(prName2, prName1 + "|" + pkg) == null) ||
                (!co.getName().equals("{ Imported from " + prName1 + " }")))
            {
                fail("Checking test results fail");
            }
        }catch(Exception e){
            fail("Test failed with unknown reason: " + e.getMessage());
        }
    }

    public void testDeleteImportedElementFromTree(){
        String prName1 = "UMLProject7A";
        String prName2 = "UMLProject7B";
        String class1 = "C7";
        
        try{
            Utils.createJavaUMLProject(prName1, workDir);
            MUPUtils.createElement(prName1, class1, ElementTypes.CLASS.toString());
            Utils.createJavaUMLProject(prName2, workDir);
            
            if (!MUPUtils.importElementInTree(prName1,  prName2, class1)){
                fail("Test fails on importing element");
            }
            
            Node node = MUPUtils.selectElementInImportTree(prName2, prName1 + "|" + class1);
            MUPUtils.pushKey(KeyEvent.VK_DELETE);
//            node.callPopup().pushMenuNoBlock(PopupConstants.DELETE);
            
            new JButtonOperator(new JDialogOperator("Delete Imported Element"), "Yes").pushNoBlock();
            
            Thread.sleep(1500);
            
            if (MUPUtils.selectElementInModelTree(prName1, class1) == null){
                fail("Original element '" + class1 + "' not found (only import should be deleted)");
            }
            if (MUPUtils.selectElementInImportTree(prName2, prName1 + "|" + class1) != null){
                fail("Import not deleted");
            }
        }catch(Exception e){
            fail("Test failed with unknown reason: " + e.getMessage());
        }
    }

    public void testDeleteImportedPackageFromTree(){
        String prName1 = "UMLProject8A";
        String prName2 = "UMLProject8B";
        String pkg = "pkg8";
        
        try{
            Utils.createJavaUMLProject(prName1, workDir);
            MUPUtils.createPackage(prName1, pkg);
            Utils.createJavaUMLProject(prName2, workDir);
            
            if (!MUPUtils.importElementInTree(prName1,  prName2, pkg)){
                fail("Test fails on importing package");
            }
            
            Node node = MUPUtils.selectElementInImportTree(prName2, prName1 + "|" + pkg);
            
            new Thread(new Runnable() {
                public void run() {
                    long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
                    try{
                        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 10000);
                        new JButtonOperator(new JDialogOperator("Deleting a Package"), "Yes").pushNoBlock();                    
                    }catch(Exception e){}
                    finally{
                        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);            
                    }
                }
            }).start();
            
            MUPUtils.pushKey(KeyEvent.VK_DELETE);
//            node.callPopup().pushMenuNoBlock(PopupConstants.DELETE);
            new JButtonOperator(new JDialogOperator("Delete Imported Element"), "Yes").pushNoBlock();
            
            Thread.sleep(1500);
            
            if (MUPUtils.selectElementInModelTree(prName1, pkg) == null){
                fail("Original package '" + pkg + "' not found (only import should be deleted)");
            }
            if (MUPUtils.selectElementInImportTree(prName2, prName1 + "|" + pkg) != null){
                fail("Import not deleted");
            }
        }catch(Exception e){
            fail("Test failed with unknown reason: " + e.getMessage());
        }
    }
    
//------------------------------------------------------------------------------
    
    public void setUp() throws FileNotFoundException{
        System.out.println("########  "+getName()+"  #######");
        
        OUT_LOG_FILE = workDir + File.separator + "jout_" + getName() + ".log";
        ERR_LOG_FILE = workDir + File.separator + "jerr_" + getName() + ".log";
        
        myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
        myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
        JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
    }
    
    public void tearDown() throws FileNotFoundException, IOException, InterruptedException{
        org.netbeans.test.umllib.util.Utils.tearDown();
        long timeout = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        try{
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 2000);
            JDialogOperator dlgError = new JDialogOperator("Unexpected Exception");
            JTextAreaOperator textarea = new JTextAreaOperator(dlgError);
            String str = textarea.getDisplayedText();
            int pos = str.indexOf("\n");
            if(pos != -1){str = str.substring(1, pos-1);}
            dlgError.close();
            fail(" " + str);
        }catch(TimeoutExpiredException e){
        }finally{
            JemmyProperties.setCurrentTimeout("DiagramOperator.WaitDiagramOperator", timeout);
        }
        
        myIn = new BufferedReader(new FileReader(ERR_LOG_FILE));
        String line;
        do {
            line = myIn.readLine();
            if (line!=null && line.indexOf("Exception")!=-1){
                if ((line.indexOf("Unexpected Exception")==-1) &&
                    (line.indexOf("TimeoutExpiredException")==-1)){
                    //fail(line);
                }
            }
        } while (line != null);
    }
}
       
