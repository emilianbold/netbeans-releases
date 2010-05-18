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
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.findreplaceassociate.utils.FRAUtils;
import org.netbeans.test.umllib.FindDialogOperator;
import org.netbeans.test.umllib.ReplaceDialogOperator;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.testcases.UMLTestCase;


/**
 *
 * @author yaa
 * @spec uml/FindReplaceAssociate.xml
 */
public class Replace extends UMLTestCase {
    private static String prName2 = "UMLProject2";
    
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    private EventTool eventTool = new EventTool();
    private static boolean isNotInitialized = true;
    public LinkedList testData = null;
    
    
    /** Need to be defined because of JUnit */
    public Replace(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.findreplaceassociate.Replace.class);
        return suite;
    }
    
    public void testReplace_MC_CS_ElementName(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "ClassA", true, false, false);
        dlg.setReplaceString("ClassNewA");
        if (!dlg.checkReplace(FindDialogOperator.SearchTargetCriteria.NAME, testData, "ClassA", "ClassNewA", true, false)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }

    public void testReplace_MC_CI_ElementName(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "cLaSsB", false, false, false);
        dlg.setReplaceString("ClassNewB");
        if (!dlg.checkReplace(FindDialogOperator.SearchTargetCriteria.NAME, testData, "cLaSsB", "ClassNewB", false, false)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }
 
    public void testReplace_MC_CS_ElementAlias(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "AliasClass", true, false, true);
        dlg.setReplaceString("AliasClass1");
        if (!dlg.checkReplace(FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "AliasClass", "AliasClass1", true, false)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }

    public void testReplace_MC_CI_ElementAlias(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "AlIaScLaSs", false, false, true);
        dlg.setReplaceString("AliasClass2");
        if (!dlg.checkReplace(FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "AlIaScLaSs", "AliasClass2", false, false)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }

    public void testReplace_MC_CS_DiagramName(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "DClass2", true, false, false);
        dlg.setReplaceString("NewDClass2");
        if (!dlg.checkReplace(FindDialogOperator.SearchTargetCriteria.NAME, testData, "DClass2", "NewDClass2", true, false)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }

    public void testReplace_MC_CI_DiagramName(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "DcLaSs2", false, false, false);
        dlg.setReplaceString("NewDClass2");
        if (!dlg.checkReplace(FindDialogOperator.SearchTargetCriteria.NAME, testData, "DcLaSs2", "NewDClass2", false, false)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }

    public void testReplace_MC_CS_DiagramAlias(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "DClass3", true, false, true);
        dlg.setReplaceString("NewDClass3Alias");
        if (!dlg.checkReplace(FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "DClass3", "NewDClass3Alias", true, false)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }

    public void testReplace_MC_CI_DiagramAlias(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "DcLaSs3", false, false, true);
        dlg.setReplaceString("NewDClass3Alias");
        if (!dlg.checkReplace(FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "DcLaSs3", "NewDClass3Alias", false, false)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }

    public void testReplace_MC_CS_ProjectName(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, prName2, true, false, false);
        if (dlg.getSearchResults().getRowCount() != 0){
            dlg.clickClose();
            fail("Found project element");
        }else{
            dlg.clickClose();
        }
    }

    public void testReplace_MC_CI_ProjectName(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, prName2.toLowerCase(), false, false, false);
        if (dlg.getSearchResults().getRowCount() != 0){
            dlg.clickClose();
            fail("Found project element");
        }else{
            dlg.clickClose();
        }
    }

    public void testReplace_MWWO_ElementName(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "ClassH", true, true, false);
        dlg.setReplaceString("ClassNewH");
        if (!dlg.checkReplaceAll(FindDialogOperator.SearchTargetCriteria.NAME, testData, "ClassH", "ClassNewH", true, true)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }

    public void testReplace_MWWO_ElementAlias(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "ClassI", true, true, true);
        dlg.setReplaceString("ClassNewI");
        if (!dlg.checkReplaceAll(FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "ClassI", "ClassNewI", true, true)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }

    public void testReplace_MWWO_DiagramName(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "DClassA", true, true, false);
        dlg.setReplaceString("DClassA1");
        if (!dlg.checkReplace(FindDialogOperator.SearchTargetCriteria.NAME, testData, "DClassA", "DClassA1", true, true)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }

    public void testReplace_MWWO_DiagramAlias(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, "DClassA1", true, true, true);
        dlg.setReplaceString("DClassAlias1");
        if (!dlg.checkReplace(FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "DClassA1", "AClassAlias1", true, true)){
            fail("Replace results are incorrect");
        }
        dlg.clickClose();
    }

    public void testReplace_MWWO_ProjectName(){
        ReplaceDialogOperator dlg = ReplaceDialogOperator.invoke(getLog());
        dlg.doFindElements(prName2, prName2, true, true, false);
        if (dlg.getSearchResults().getRowCount() != 0){
            dlg.clickClose();
            fail("Found project element");
        }else{
            dlg.clickClose();
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
            testData = FRAUtils.getTestData2();
            Project.openProject(FRAUtils.FRA_XTEST_PROJECT_DIR+"/"+prName2);
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
       
