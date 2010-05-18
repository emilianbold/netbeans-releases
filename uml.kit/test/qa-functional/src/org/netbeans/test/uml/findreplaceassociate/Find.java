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
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.testcases.UMLTestCase;


/**
 *
 * @author yaa
 * @spec uml/FindReplaceAssociate.xml
 */
public class Find extends UMLTestCase {
    private static String prName1 = "UMLProject1";
    
    
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    public LinkedList testData = null;
    
    private static boolean isNotInitialized = true;
    private EventTool eventTool = new EventTool();
    
    /** Need to be defined because of JUnit */
    public Find(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.findreplaceassociate.Find.class);
        return suite;
    }
    
    public void testFind_MC_CS_ElementName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "Class", true, false, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME, testData, "Class", true, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CI_ElementName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "LaSsA", false, false, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME, testData, "LaSsA", false, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CS_ElementDescription(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindDescriptions(prName1, "ClasA", true, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.DESCRIPTION, testData, "ClasA", true, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CI_ElementDescription(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindDescriptions(prName1, "cLasA", false, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.DESCRIPTION, testData, "cLasA", false, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CS_ElementNameAndAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "ClssA", true, false, true);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME_AND_ALIAS, testData, "ClssA", true, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CI_ElementNameAndAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "caSsA", false, false, true);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME_AND_ALIAS, testData, "caSsA", false, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CS_DiagramName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "DClass", true, false, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME, testData, "DClass", true, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CI_DiagramName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "DcLaS", false, false, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME, testData, "DcLaS", false, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CS_DiagramDescription(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindDescriptions(prName1, "DClassA", true, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.DESCRIPTION, testData, "DClassA", true, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CI_DiagramDescription(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindDescriptions(prName1, "DcLaSsA", false, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.DESCRIPTION, testData, "DcLaSsA", false, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CS_DiagramNameAndAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "sA", true, false, true);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME_AND_ALIAS, testData, "sA", true, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CI_DiagramNameAndAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "DcLaSsA", false, false, true);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME_AND_ALIAS, testData, "DcLaSsA", false, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CS_ProjectName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, prName1, true, false, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME, testData, prName1, true, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MC_CI_ProjectName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, prName1.toUpperCase(), false, false, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME, testData, prName1.toLowerCase(), false, false)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MWWO_ElementName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "ClassA", true, true, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME, testData, "ClassA", true, true)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MWWO_ElementDescription(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindDescriptions(prName1, "ClassA", true, true);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.DESCRIPTION, testData, "ClassA", true, true)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MWWO_ElementNameAndAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "ClassA", true, true, true);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME_AND_ALIAS, testData, "ClassA", true, true)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MWWO_DiagramName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "DClassA", true, true, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME, testData, "DClassA", true, true)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MWWO_DiagramDescription(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindDescriptions(prName1, "DClassA", true, true);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.DESCRIPTION, testData, "DClassA", true, true)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MWWO_DiagramNameAndAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, "DClassA", true, true, true);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME_AND_ALIAS, testData, "DClassA", true, true)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_MWWO_ProjectName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindElements(prName1, prName1, true, true, false);
        eventTool.waitNoEvent(1000);
        if (!dlg.checkSearchResult(FindDialogOperator.SearchTargetCriteria.NAME, testData, prName1, true, true)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
// ------ find XPath expressions --------
    
    public void testFind_XPath_ClassName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Class[@name=\"ClassA\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.CLASS, FindDialogOperator.SearchTargetCriteria.NAME, testData, "ClassA")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_ProjectName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Project[@name=\"" + prName1 + "\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.PROJECT, FindDialogOperator.SearchTargetCriteria.NAME, testData, prName1)){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_InterfaceName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Interface[@name=\"Interface1\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.INTERFACE, FindDialogOperator.SearchTargetCriteria.NAME, testData, "Interface1")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_PackageName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Package[@name=\"pkg1\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.PACKAGE, FindDialogOperator.SearchTargetCriteria.NAME, testData, "pkg1")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_AttributeName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Attribute[@name=\"data\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.ATTRIBUTE, FindDialogOperator.SearchTargetCriteria.NAME, testData, "data")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_OperationName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Operation[@name=\"test\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.OPERATION, FindDialogOperator.SearchTargetCriteria.NAME, testData, "test")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_ParameterName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Parameter[@name=\"val\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.PARAMETER, FindDialogOperator.SearchTargetCriteria.NAME, testData, "val")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_GeneralizationName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Generalization[@name=\"Gen1\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.GENERALIZATION, FindDialogOperator.SearchTargetCriteria.NAME, testData, "Gen1")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_ImplementationName(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Implementation[@name=\"Impl1\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.IMPLEMENTATION, FindDialogOperator.SearchTargetCriteria.NAME, testData, "Impl1")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_ClassAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Class[@alias=\"class\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.CLASS, FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "class")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_InterfaceAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Interface[@alias=\"interface\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.INTERFACE, FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "interface")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_PackageAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Package[@alias=\"package\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.PACKAGE, FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "package")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_AttributeAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Attribute[@alias=\"attribute\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.ATTRIBUTE, FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "attribute")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_OperationAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Operation[@alias=\"operation\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.OPERATION, FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "operation")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_ParameterAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Parameter[@alias=\"parameter\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.PARAMETER, FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "parameter")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_GeneralizationAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Generalization[@alias=\"generalization\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.GENERALIZATION, FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "generalization")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
            dlg.clickClose();
        }
    }
    
    public void testFind_XPath_ImplementationAlias(){
        FindDialogOperator dlg = FindDialogOperator.invoke(getLog());
        dlg.doFindXPath(prName1, "//UML:Implementation[@alias=\"implementation\"]");
        eventTool.waitNoEvent(1000);
        if (!dlg.checkXPathSearchResult(FindDialogOperator.SearchTarget.IMPLEMENTATION, FindDialogOperator.SearchTargetCriteria.ALIAS, testData, "implementation")){
            dlg.clickClose();
            fail("Search results are incorrect");
        } else {
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
        
        testData = FRAUtils.getTestData1();
        if (isNotInitialized){
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

