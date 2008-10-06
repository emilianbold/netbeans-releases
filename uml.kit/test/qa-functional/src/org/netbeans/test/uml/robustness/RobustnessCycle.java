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


package org.netbeans.test.uml.robustness;
import java.io.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.robustness.utils.RUtils;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.vrf.GenericVerifier;

/**
 *
 * @author yaa
 * @spec uml/UMLRobustness.xml
 */
public class RobustnessCycle extends UMLTestCase {
    private static String prName = "UMLProject3";
    private static String cldName1 = "DClass1";
    private static String cldName2 = "DClass2";
    private static String cldName3 = "DClass3";
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    /** Need to be defined because of JUnit */
    public RobustnessCycle(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.robustness.RobustnessCycle.class);
        return suite;
    }
    
    public void setUp() throws FileNotFoundException{
        System.out.println("########  "+getName()+"  #######");
        JemmyProperties.setCurrentTimeout("LinkOperator.WaitLinkTime", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
        
        OUT_LOG_FILE = workDir + File.separator + "jout_" + getName() + ".log";
        ERR_LOG_FILE = workDir + File.separator + "jerr_" + getName() + ".log";
        
        myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
        myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
        JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
    }
    
    public void tearDown() throws FileNotFoundException, IOException{
        try{
            JDialogOperator dlgError = new JDialogOperator("Unexpected Exception");
            JTextAreaOperator textarea = new JTextAreaOperator(dlgError);
            String str = textarea.getDisplayedText();
            int pos = str.indexOf("\n");
            if(pos != -1){str = str.substring(1, pos-1);}
            dlgError.close();
            fail(" " + str);
        }catch(TimeoutExpiredException e){}
        
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
        org.netbeans.test.umllib.util.Utils.tearDown();
    }
    
    public void testCLD_CycleOneGeneralization(){
        DiagramOperator diagram = RUtils.openDiagram(prName, cldName1, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + cldName1 + "', project '" + prName + "'.");
        }
        boolean resAll = true;
        
        for(int i = 0; i < RUtils.elementTypesCLD.length; i++){
            boolean res = RUtils.checkCycleOneElement(diagram, RUtils.elementTypesCLD[i], LinkTypes.GENERALIZATION);
            if(res){
                log("'" + RUtils.elementTypesCLD[i] + "' element can be linked by " + LinkTypes.GENERALIZATION + " link to itself");
                resAll = false;
            }
            closeAllModal();
            new GenericVerifier(diagram).safeDeleteAllElements();
        }
        assertTrue(93203, "Some element can be linked by " + LinkTypes.GENERALIZATION + " link to itself (see log)", resAll);
    }
    
    public void testCLD_CycleOneImplementation(){
        DiagramOperator diagram = RUtils.openDiagram(prName, cldName2, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + cldName2 + "', project '" + prName + "'.");
        }
        boolean resAll = true;
        
        for(int i = 0; i < RUtils.elementTypesCLD.length; i++){
            boolean res = RUtils.checkCycleOneElement(diagram, RUtils.elementTypesCLD[i], LinkTypes.IMPLEMENTATION);
            if(res){
                log("'" + RUtils.elementTypesCLD[i] + "' element can be linked by " + LinkTypes.IMPLEMENTATION + " link to itself");
                resAll = false;
            }
            closeAllModal();
            new GenericVerifier(diagram).safeDeleteAllElements();
        }
        assertTrue("Some element can be linked by " + LinkTypes.IMPLEMENTATION + " link to itself (see log)", resAll);
    }
    
    public void testCLD_CycleOneNestedLink(){
        DiagramOperator diagram = RUtils.openDiagram(prName, cldName3, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + cldName3 + "', project '" + prName + "'.");
        }
        boolean resAll = true;
        
        for(int i = 0; i < RUtils.elementTypesCLD.length; i++){
            boolean res = RUtils.checkCycleOneElement(diagram, RUtils.elementTypesCLD[i], LinkTypes.NESTED_LINK);
            if(res){
                log("'" + ElementTypes.CLASS + "' element can be linked by " + LinkTypes.NESTED_LINK + " link to itself");
                resAll = false;
            }
            closeAllModal();
            new GenericVerifier(diagram).safeDeleteAllElements();
        }
        assertTrue("Some element can be linked by " + LinkTypes.NESTED_LINK + " link to itself (see log)", resAll);
    }
}

