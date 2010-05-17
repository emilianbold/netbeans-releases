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


package org.netbeans.test.uml.findreplaceassociate;
import java.io.*;
import java.util.LinkedList;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.findreplaceassociate.utils.FRAUtils;
import org.netbeans.test.umllib.AssociateDialogOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.FindDialogOperator;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.LabelsAndTitles;

/**
 *
 * @author yaa
 * @spec uml/FindReplaceAssociate.xml
 */
public class Associate extends UMLTestCase {
    private static String prName1 = "UMLProject1";
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    private static boolean isNotInitialized = true;
    private EventTool eventTool = new EventTool();
    
    public LinkedList testData = null;
    
    /** Need to be defined because of JUnit */
    public Associate(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.findreplaceassociate.Associate.class);
        return suite;
    }
    
    public void testAssociate1(){
        Node node = FRAUtils.selectElementInProjectsTree(prName1, "ClassD");
        AssociateDialogOperator dlg = null;
        try{
            dlg = AssociateDialogOperator.invoke(node, getLog());
        }catch(Exception e){
            fail("Dialog with title '" + LabelsAndTitles.ASSOCIATE_DIALOG_TITLE + "' not found");
        }
        dlg.doFindElements(prName1, "ClassA", true, false, false);
        eventTool.waitNoEvent(1000);
        
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME, testData, "ClassA", true, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        }
        dlg.clickAssociateAll();
        eventTool.waitNoEvent(1000);
        dlg.clickClose();
    }
    
    public void testAssociate2(){
        DiagramOperator diagram = FRAUtils.openDiagram(prName1, "DClass0", NewDiagramWizardOperator.CLASS_DIAGRAM);
        if(testData==null)
             testData = FRAUtils.getTestData1();
        testData.add(new Object[]{"DClass0", "DClass0", "", FindDialogOperator.SearchTarget.DIAGRAM});
        try{
            DiagramElementOperator elem = diagram.putElementOnDiagram("ClassWWW", ElementTypes.CLASS);
            testData.add(new Object[]{"ClassWWW", "ClassWWW", "", FindDialogOperator.SearchTarget.CLASS});
            testData.add(new Object[]{"ClassWWW", "ClassWWW", "", FindDialogOperator.SearchTarget.OPERATION});
            AssociateDialogOperator dlg = null;
            try{
                dlg = AssociateDialogOperator.invoke(elem, getLog());
            }catch(Exception e){
                fail("Dialog with title '" + LabelsAndTitles.ASSOCIATE_DIALOG_TITLE + "' not found");
            }
            dlg.doFindElements(prName1, "Class", true, false, false);
            eventTool.waitNoEvent(1000);
            
            if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME, testData, "Class", true, false)){
                dlg.clickClose();
                fail("Search results are incorrect");
            }
            dlg.clickAssociateAll();
            eventTool.waitNoEvent(1000);
            dlg.clickClose();
        }catch(Exception e){
            fail("Unexpected exception: " + e.getMessage());
        }
        
    }
    
//------------------------------------------------------------------------------
    
    public void setUp() throws FileNotFoundException{
        System.out.println("########  "+getName()+"  #######");
        
        OUT_LOG_FILE = XTEST_WORK_DIR + File.separator + "jout_" + getName() + ".log";
        ERR_LOG_FILE = XTEST_WORK_DIR + File.separator + "jerr_" + getName() + ".log";
        
        myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
        myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
        JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
        
        if (isNotInitialized){
            testData = FRAUtils.getTestData1();
            Project.openProject(FRAUtils.FRA_XTEST_PROJECT_DIR+"/"+prName1);
            isNotInitialized=false;
        }
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

