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
import org.netbeans.test.umllib.vrf.LinkVerifier;

/**
 *
 * @author yaa
 * @spec UML/DeploymentDiagram.xml
 */
public class DPD_ImplementationLink extends UMLTestCase {
    
    private static String prName = "UMLProjectDPD8";
    private static String dpdName = "DPD";
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    /** Need to be defined because of JUnit */
    public DPD_ImplementationLink(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.deploymentdiagram.DPD_ImplementationLink.class);
        return suite;
    }

//------------------------------------------------------------------------------    
    
/**
 * @caseblock Implementation link
 * @usecase Delete a Implementation link from a diagram by popup menu
 */
    public void testDeleteByPopup() throws NotFoundException {
        boolean result = verifier.checkDeleteByPopup(ElementTypes.COMPONENT, ElementTypes.INTERFACE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Implementation link
 * @usecase Delete a Implementation link from a diagram by shortcut
 */
    public void testDeleteByShortcut() throws NotFoundException {
        boolean result = verifier.checkDeleteByShortcut(ElementTypes.COMPONENT, ElementTypes.INTERFACE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Implementation link
 * @usecase Select all on a diagram by popup menu of a Implementation link
 */
    public void testSelectAllByPopup() throws NotFoundException {
        boolean result = verifier.checkSelectAllByPopup(ElementTypes.COMPONENT, ElementTypes.INTERFACE, LinkTypes.DEPENDENCY);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }

/**
 * @caseblock Implementation link
 * @usecase Invert selection of a Implementation link
 */
    public void testInvertSelection() throws NotFoundException {
        boolean result = verifier.checkInvertSelection(ElementTypes.COMPONENT, ElementTypes.INTERFACE, LinkTypes.DEPENDENCY);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }

/**
 * @caseblock Implementation link
 * @usecase Find source element of a Implementation link
 */
    public void testFindSourceElement() throws NotFoundException {
        boolean result = verifier.checkFindSourceElement(ElementTypes.COMPONENT, ElementTypes.INTERFACE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }

/**
 * @caseblock Implementation link
 * @usecase Find target element of a Implementation link
 */
    public void testFindTargetElement() throws NotFoundException {
        boolean result = verifier.checkFindTargetElement(ElementTypes.COMPONENT, ElementTypes.INTERFACE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Implementation link
 * @usecase Redirect source element of a Implementation link
 */
    public void testRedirectSourceElement() throws NotFoundException {
        boolean result = verifier.checkRedirectSourceElement(ElementTypes.COMPONENT, ElementTypes.INTERFACE, ElementTypes.COMPONENT);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
/**
 * @caseblock Implementation link
 * @usecase Redirect target element of a Implementation link
 */
    public void testRedirectTargetElement() throws NotFoundException {
        boolean result = verifier.checkRedirectTargetElement(ElementTypes.COMPONENT, ElementTypes.INTERFACE, ElementTypes.INTERFACE);
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
        verifier =  new LinkVerifier(diagram, LinkTypes.IMPLEMENTATION, "", getLog());
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
    private LinkVerifier verifier = null;
}
