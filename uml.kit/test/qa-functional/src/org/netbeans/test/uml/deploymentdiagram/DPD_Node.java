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


package org.netbeans.test.uml.deploymentdiagram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.deploymentdiagram.utils.DPDUtils;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.vrf.DiagramElementVerifier;

/**
 *
 * @author yaa
 * @spec UML/DeploymentDiagram.xml
 */
public class DPD_Node extends UMLTestCase {
    
    private static String prName = "UMLProjectDPD5";
    private static String dpdName = "DPD";
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    /** Need to be defined because of JUnit */
    public DPD_Node(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.deploymentdiagram.DPD_Node.class);
        return suite;
    }

/**
 * @caseblock Node Symbol
 * @usecase Copy and Paste a Node symbol on a diagram by popup menu
 */
    public void testCopyAndPasteByPopup(){
        boolean result = verifier.checkCopyPasteByPopup();
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Copy and Paste a Node symbol on a diagram by shortcuts
 */
    public void testCopyAndPasteByShortcut(){
        boolean result = verifier.checkCopyPasteByShortcut();
        if (!result){
            fail( "Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Cut and Paste a Node symbol on a diagram by popup menu
 */
    public void testCutAndPasteByPopup(){
        boolean result = verifier.checkCutPasteByPopup();
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Cut and Paste a Node symbol on a diagram by shortcuts
 */
    public void testCutAndPasteByShortcut(){
        boolean result = verifier.checkCutPasteByShortcut();
        if (!result){
            fail( "Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Delete a UML Node symbol from a diagram by popup menu
 */
    public void testDeleteByPopup() throws NotFoundException {
        boolean result = verifier.checkDeleteByPopup();
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Delete a UML Node symbol from a diagram by shortcut
 */
    public void testDeleteByShortcut() throws NotFoundException {
        boolean result = verifier.checkDeleteByShortcut();
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }

/**
 * @caseblock Node Symbol
 * @usecase Lock edit of a Node
 */
    public void testLockEdit() throws NotFoundException {
        boolean result = verifier.checkLockEdit();
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Select all on a diagram by popup menu of a Node
 */
    public void testSelectAllByPopup() throws NotFoundException {
        boolean result = verifier.checkSelectAllByPopup(new ElementTypes[]{ElementTypes.INTERFACE, ElementTypes.ARTIFACT});
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Select all on a diagram by shortcut
 */
    public void testSelectAllByShortcut() throws NotFoundException {
        boolean result = verifier.checkSelectAllByShortcut(new ElementTypes[]{ElementTypes.INTERFACE, ElementTypes.ARTIFACT});
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Select All Similar Elements of a Node
 */
    public void testSelectAllSimilar() throws NotFoundException {
        boolean result = verifier.checkSelectAllSimilar(new ElementTypes[]{ElementTypes.NODE, ElementTypes.COMPONENT});
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Invert selection of a Node
 */
    public void testInvertSelection() throws NotFoundException {
        boolean result = verifier.checkInvertSelection(new ElementTypes[]{ElementTypes.COMPONENT}, new ElementTypes[]{ElementTypes.INTERFACE, ElementTypes.PACKAGE});
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Hide Children one level of a Node
 */
    public void testHideChildrenOneLevel() throws NotFoundException {
        boolean result = verifier.checkHideChildrenOneLevel(2, 2, LinkTypes.DEPENDENCY, ElementTypes.NODE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Hide Children All levels of a Node
 */
    public void testHideChildrenAllLevels() throws NotFoundException {
        boolean result = verifier.checkHideChildrenAllLevels(2, 2, LinkTypes.DEPENDENCY, ElementTypes.NODE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Hide Parents one level of a Node
 */
    public void testHideParentsOneLevel() throws NotFoundException {
        boolean result = verifier.checkHideParentsOneLevel(2, 2, LinkTypes.DEPENDENCY, ElementTypes.NODE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Node Symbol
 * @usecase Hide Parents All levels of a Node
 */
    public void testHideParentsAllLevels() throws NotFoundException {
        boolean result = verifier.checkHideParentsAllLevels(2, 2, LinkTypes.DEPENDENCY, ElementTypes.NODE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }   
    
/**
 * @caseblock Node Symbol
 * @usecase Show Children one level of a Node
 */
    public void testShowChildrenOneLevel() throws NotFoundException {
        boolean result = verifier.checkShowChildrenOneLevel(2, 2, LinkTypes.DEPENDENCY, ElementTypes.NODE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }   
    
/**
 * @caseblock Node Symbol
 * @usecase Show Children All levels of a Node
 */
    public void testShowChildrenAllLevels() throws NotFoundException {
        boolean result = verifier.checkShowChildrenAllLevels(2, 2, LinkTypes.DEPENDENCY, ElementTypes.NODE);
        if (!result){
            fail(78350, "Test failed. Details in log file.");
        }
    }   
    
/**
 * @caseblock Node Symbol
 * @usecase Show Parents one level of a Node
 */
    public void testShowParentsOneLevel() throws NotFoundException {
        boolean result = verifier.checkShowParentsOneLevel(2, 2, LinkTypes.DEPENDENCY, ElementTypes.NODE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }   
    
/**
 * @caseblock Node Symbol
 * @usecase Show Parents All levels of a Node
 */
    public void testShowParentsAllLevels() throws NotFoundException {
        boolean result = verifier.checkShowParentsAllLevels(2, 2, LinkTypes.DEPENDENCY, ElementTypes.NODE);
        if (!result){
            fail(78350, "Test failed. Details in log file.");
        }
    }   
    
/**
 * @caseblock Node Symbol
 * @usecase Border color of a Node
 */
    public void testBorderColor() throws NotFoundException {
        boolean result = verifier.checkBorderColor(255, 0, 0);
        if (!result){
            fail( "Test failed. Details in log file.");
        }
    }   
    
/**
 * @caseblock Node Symbol
 * @usecase Background color of a Node
 */
    public void testBackgroundColor() throws NotFoundException {
        boolean result = verifier.checkBackgroundColor(0, 255, 0);
        if (!result){
            fail( "Test failed. Details in log file.");
        }
    }   
    
/**
 * @caseblock Node Symbol
 * @usecase Text font of a Node
 */
    public void testFont() throws NotFoundException {
        boolean result = verifier.checkFont();
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }   

/**
 * @caseblock Node Symbol
 * @usecase Text color of a Node
 */
    public void testFontColor() throws NotFoundException {
        boolean result = verifier.checkFontColor(100,100,100);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }   
    
//------------------------------------------------------------------------------
    
    protected void setUp() throws FileNotFoundException{
        System.out.println("########  "+getName()+"  #######");
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
        JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
        JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 5000);
        
        OUT_LOG_FILE = workDir + File.separator + "jout_" + getName() + ".log";
        ERR_LOG_FILE = workDir + File.separator + "jerr_" + getName() + ".log";
        
        myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
        myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
        JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
        
        diagram = DPDUtils.openDiagram(prName, dpdName, NewDiagramWizardOperator.DEPLOYMENT_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + dpdName + "', project '" + prName + "'.");
        }
        verifier =  new DiagramElementVerifier(diagram, ElementTypes.NODE, "", getLog());
    }
   
    public void tearDown() throws FileNotFoundException, IOException{
        org.netbeans.test.umllib.util.Utils.tearDown();
        try{
            JDialogOperator dlgError = new JDialogOperator("Unexpected Exception");
            JTextAreaOperator textarea = new JTextAreaOperator(dlgError);
            String str = textarea.getDisplayedText();
            int pos = str.indexOf("\n");
            if(pos != -1){str = str.substring(1, pos-1);}
            dlgError.close();
            fail(" " + str);
        }catch(TimeoutExpiredException e1){
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
    
    private DiagramOperator diagram = null;
    private DiagramElementVerifier verifier = null;
}
