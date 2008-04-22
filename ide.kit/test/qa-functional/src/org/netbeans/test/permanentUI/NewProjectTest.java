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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.test.permanentUI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.tree.TreeModel;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.permanentUI.utils.Utilities;

/**
 *
 * @author Lukas Hasik, Petr Chytil
 */
public class NewProjectTest extends JellyTestCase{
    /** Need to be defined because of JUnit */
    public NewProjectTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new NewProjectTest("testNewProjectCategories"));
        suite.addTest(new NewProjectTest("testNewProjectsJava"));
//        suite.addTest(new NewProjectTest("testNewProjectsJava"));
//        suite.addTest(new NewProjectTest("testNewProjectsJava"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new IDEValidation("testMainMenu"));
    }
    
    /** Setup called before every test case. */
    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");

    }
    
    /** Tear down called after every test case. */
    @Override
    public void tearDown() {
    }
    
    private static final String CATEGORIES_GOLDEN_FILE = "data/newprojects-Categories.txt";
    
    private class ComparationReturnValues{

        public ComparationReturnValues(boolean assertValue, String assertString) {
            this.assertString = assertString;
            this.assertValue = assertValue;
        }
        
        public boolean assertValue;
        public String assertString;
    }
    
    
    private ArrayList<String> getChildren(TreeModel tree, Object root, String spaces){
        int categoriesCount = tree.getChildCount(root); 
        ArrayList<String> returnList = new ArrayList<String>();
        
        for(int i = 0; i<= categoriesCount-1;i++){
            Object actualChild = tree.getChild(root, i);
            returnList.add(spaces + actualChild.toString());
            
            if(!tree.isLeaf(actualChild)){
                
                spaces = "+-" + spaces; 
                returnList.addAll(getChildren(tree, actualChild, spaces));
                spaces = spaces.substring(2);
                
            }
        }
        return returnList;
    }
    
    /**
     * tests the that the File > New Project categories match 
     * http://wiki.netbeans.org/NewProjectWizard
     */
    public void testNewProjectCategories() {
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();                        
        String goldenfile = this.getClass().getResource(CATEGORIES_GOLDEN_FILE).getFile();
        ArrayList<String> permanentCategories = Utilities.parseFileByLinesLeaveSpaces(goldenfile);
        System.out.println("======== Permanent UI Categories: ========");
        for(String actual: permanentCategories){
            System.out.println(actual);
        }
       
        JTreeOperator categoriesOperator = npwo.treeCategories();        
        TreeModel categoriesTree = npwo.treeCategories().getModel();
        Object categoriesRoot = categoriesTree.getRoot();
        
        ArrayList<String> actualCategories = getChildren(categoriesTree , categoriesRoot ,"");
        
        System.out.println("======== Actual Categories: ========");
        for(String actual: actualCategories){
            System.out.println(actual);
        }
        
        Iterator<String> itPermanentCategories = permanentCategories.iterator();
        Iterator<String> itActualCategories = actualCategories.iterator();
                                
        assertResults = compareStringArrays(itPermanentCategories, itActualCategories);

        npwo.cancel();
        assertTrue(assertResults.assertString,assertResults.assertValue);
    }
    
    
    public void testNewProjectsJava(){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        assertResults = oneCategoryTest("Java", npwo);
        npwo.cancel();
        assertTrue(assertResults.assertString, assertResults.assertValue);        
    }
    
    private ComparationReturnValues compareStringArrays(Iterator<String> itPermanentArrayList,Iterator<String> itArrayList){
            boolean assertvalue = true;
            String assertString = "";                       
            while (itPermanentArrayList.hasNext() || itArrayList.hasNext()) {
            if(itPermanentArrayList.hasNext()){
                if (itArrayList.hasNext()) { //both are not null
                    String perm = itPermanentArrayList.next();
                    String real = itArrayList.next();
                    if (!Utilities.trimTextLine(perm).equals(Utilities.trimTextLine(real))) {
                        assertString += "compare failed: " + perm + " vs. " + real + "\n";
                        assertvalue = false;
                    }
                } else { //real is null, permanent is present
                    String perm = itPermanentArrayList.next();
                    assertString += "compare failed: "+ perm +" vs. -missing line in New Project dialog-\n";
                    assertvalue = false;
                }
            } else if(itArrayList.hasNext()){ //permanent item is null
                String real = itArrayList.next();
                assertString += "compare failed: -empty line in permanent UI- vs. " + real + "\n";
                assertvalue = false;
            } else { // both are null
                assertString += "BOTH ITEMS ARE NULL. THIS STATE SHOULDN'T HAPPEN\n";
                assertvalue = false;
            }
            
            
        }
        return new ComparationReturnValues(assertvalue, assertString);
    }
    
    private ComparationReturnValues oneCategoryTest(String categoryName, NewProjectWizardOperator newProjectOperator){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        boolean assertValue = true;
        String assertString = "";
        String goldenfile = this.getClass().getResource(getCategoryGoldenFile(categoryName)).getFile();
        ArrayList<String> permanentProjects = Utilities.parseFileByLines(goldenfile);        
        newProjectOperator.selectCategory(categoryName);        
        JListOperator jlo = newProjectOperator.lstProjects();
        ArrayList<String> actualProjects = getProjectsList(jlo);
        
        System.out.println("======== Permanent UI projects in this category: ========");
        for(String actual: permanentProjects){
            System.out.println(actual);
        }
        
        System.out.println("======== Actual projects in this category: ========");
        for(String actual: actualProjects){
            System.out.println(actual);
        }
        
        Iterator<String> itPermanentProjects = permanentProjects.iterator();
        Iterator<String> itProjects = actualProjects.iterator();
        try {
            itPermanentProjects.next(); // skip the category name in the file
        } catch (NoSuchElementException e) {
            assertString += "file is empty\n"; // file is empty
            assertValue = false;
        }
        assertResults = compareStringArrays(itPermanentProjects, itProjects);                         
        assertResults.assertString = assertString + assertResults.assertString;
        assertResults.assertValue = assertValue && assertResults.assertValue;        
        return  assertResults;
    }
    
    private ArrayList<String> getProjectsList(JListOperator projectsListOperator){
        ArrayList<String> projectsList = new ArrayList<String>();
        int catSize = projectsListOperator.getLastVisibleIndex();
        for (int j = 0; j <= catSize; j++) {
            projectsList.add(projectsListOperator.getModel().getElementAt(j).toString());
        }
        return projectsList;
    
    }
    
    private String getCategoryGoldenFile(String categoryName) {
        return "data/newproject-" + categoryName + ".txt";
    }

    
    
}