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


/*
 * CLD_ImplementationLinkTests.java
 *
 * Created on May 13, 2005, 4:19 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.uml.classdiagram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.classdiagram.utils.CLDUtils;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.exceptions.UnexpectedElementSelectionException;
import org.netbeans.test.umllib.vrf.LinkVerifier;

/**
 *
 * @author Administrator
 */
public class CLD_ImplementationLinkTests extends ClassDiagramTestCase {
    
    private static String prName = "UMLProjectForImplementationLink";
    private static String dpdName = "ClassDiagramForImplementationLink";
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    /** Need to be defined because of JUnit */
    public CLD_ImplementationLinkTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.classdiagram.CLD_ImplementationLinkTests.class);
        return suite;
    }
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // junit.textui.TestRunner.run(suite());
        // run only selected test case
        junit.textui.TestRunner.run(new org.netbeans.test.uml.classdiagram.CLD_ImplementationLinkTests("testCopyAndPasteClassElement"));
    }

//------------------------------------------------------------------------------    
    
    public void testDeleteByPopup() throws NotFoundException {
        boolean result = verifier.checkDeleteByPopup(ElementTypes.CLASS, ElementTypes.INTERFACE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
    public void testDeleteByShortcut() throws NotFoundException {
        boolean result = verifier.checkDeleteByShortcut(ElementTypes.CLASS, ElementTypes.INTERFACE);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
    public void testSelectAllByPopup() throws NotFoundException {
        boolean result = verifier.checkSelectAllByPopup(ElementTypes.CLASS, ElementTypes.INTERFACE, LinkTypes.IMPLEMENTATION);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }

    public void testInvertSelection() throws NotFoundException {
        boolean result = verifier.checkInvertSelection(ElementTypes.CLASS, ElementTypes.INTERFACE, LinkTypes.IMPLEMENTATION);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }

    public void testFindSourceElement() throws NotFoundException {
        try{
            boolean result = verifier.checkFindSourceElement(ElementTypes.CLASS, ElementTypes.INTERFACE);
            if (!result){
                fail("Test failed. Details in log file.");
            }
        }catch(UnexpectedElementSelectionException e){
            parseFindSourceTargetElement(e);
        }
    }

    public void testFindTargetElement() throws NotFoundException {
        try{
            boolean result = verifier.checkFindTargetElement(ElementTypes.CLASS, ElementTypes.INTERFACE);
            if (!result){
                fail("Test failed. Details in log file.");
            }
        }catch(UnexpectedElementSelectionException e){
            parseFindSourceTargetElement(e);
        }
    }
    
    public void testRedirectSourceElement() throws NotFoundException {
        boolean result = verifier.checkRedirectSourceElement(ElementTypes.CLASS, ElementTypes.INTERFACE, ElementTypes.CLASS);
        if (!result){
            fail("Test failed. Details in log file.");
        }
    }
    
    public void testRedirectTargetElement() throws NotFoundException {
        boolean result = verifier.checkRedirectTargetElement(ElementTypes.CLASS, ElementTypes.INTERFACE, ElementTypes.INTERFACE);
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
        
        diagram = CLDUtils.openDiagram(prName, dpdName, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + dpdName + "', project '" + prName + "'.");
        }
        verifier =  new LinkVerifier(diagram, LinkTypes.IMPLEMENTATION, "CLD_", getLog());
    }
   
    private LinkVerifier verifier = null;
}
