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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.tree.TreeModel;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
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

        public static Test suite() {
            NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
            NewProjectTest.class
        ).clusters(".*").enableModules(".*");
            
        conf = conf.addTest("testNewProjectCategories"); 
        conf = conf.addTest("testNewProjectsJava");
        conf = conf.addTest("testNewProjectsJavaWeb");
        conf = conf.addTest("testNewProjectsJavaEE");
        conf = conf.addTest("testNewProjectsJavaME");
        //conf = conf.addTest("testNewProjectsUML"); 30/7/2008 - uml removed from build
        conf = conf.addTest("testNewProjectsSOA");
        conf = conf.addTest("testNewProjectsRuby");
        conf = conf.addTest("testNewProjectsCpp");
        conf = conf.addTest("testNewProjectsNetBeansModules");
        conf = conf.addTest("testNewProjectsGroovy");
        conf = conf.addTest("testNewProjectsPHP");
        
        return NbModuleSuite.create(conf);
            
        
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
    
    private static final String CATEGORIES_GOLDEN_FILE = "newprojects-Categories.txt";
    
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
        // select the Java category - workaround for failing test because of slow load of UI
        String standardLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard");
        npwo.selectCategory(standardLabel);

        String goldenfile = getDataDir().getPath()+File.separator + "permanentUI" + File.separator+"newproject"+File.separator+CATEGORIES_GOLDEN_FILE;
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
    
    public void testNewProjectsJavaWeb(){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        assertResults = oneCategoryTest("Java Web", "Java_Web", npwo);
        npwo.cancel();
        assertTrue(assertResults.assertString, assertResults.assertValue);                      
    }
    
    public void testNewProjectsJavaEE(){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        assertResults = oneCategoryTest("Java EE", "Java_EE", npwo);
        npwo.cancel();
        assertTrue(assertResults.assertString, assertResults.assertValue);                      
    }

    public void testNewProjectsJavaME(){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        assertResults = oneCategoryTest("Java ME", "Java_ME", npwo);
        npwo.cancel();
        assertTrue(assertResults.assertString, assertResults.assertValue);                      
    }


//UML was removed from daily builds    
//    public void testNewProjectsUML(){
//        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
//        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
//        assertResults = oneCategoryTest("UML", npwo);
//        npwo.cancel();
//        assertTrue(assertResults.assertString, assertResults.assertValue);
//    }

    public void testNewProjectsSOA(){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        assertResults = oneCategoryTest("SOA", npwo);
        npwo.cancel();
        assertTrue(assertResults.assertString, assertResults.assertValue);                      
    }    
    
    public void testNewProjectsRuby(){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        assertResults = oneCategoryTest("Ruby", npwo);
        npwo.cancel();
        assertTrue(assertResults.assertString, assertResults.assertValue);                      
    }
    
    public void testNewProjectsCpp(){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        assertResults = oneCategoryTest("C/C++","Cpp", npwo);
        npwo.cancel();
        assertTrue(assertResults.assertString, assertResults.assertValue);                      
    } 
    
    public void testNewProjectsNetBeansModules(){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        assertResults = oneCategoryTest("NetBeans Modules","NetBeans_Modules", npwo);
        npwo.cancel();
        assertTrue(assertResults.assertString, assertResults.assertValue); 
    }
    
    public void testNewProjectsGroovy(){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        assertResults = oneCategoryTest("Groovy", npwo);
        npwo.cancel();
        assertTrue(assertResults.assertString, assertResults.assertValue);                      
    }
    
    public void testNewProjectsPHP(){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        assertResults = oneCategoryTest("PHP", npwo);
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
    
    /**
     * For categories with simple names, which can be used as filename of the golden file.
     * @param categoryName - name of the category = name of the godlen file
     * @param newProjectOperator
     * @return
     */
    private ComparationReturnValues oneCategoryTest(String categoryName, NewProjectWizardOperator newProjectOperator){
        return oneCategoryTest(categoryName,categoryName,newProjectOperator);
    }
    
    /**
     * This method should be used when category is too complicated and couldn't be used as golden file's filename.
     * @param categoryName
     * @param goldenFileName
     * @param newProjectOperator
     * @return
     */
    private ComparationReturnValues oneCategoryTest(String categoryName, String goldenFileName, NewProjectWizardOperator newProjectOperator){
        ComparationReturnValues assertResults = new ComparationReturnValues(true,"");
        boolean assertValue = true;
        String assertString = "";
        String goldenfile = getCategoryGoldenFile(goldenFileName);
        ArrayList<String> permanentProjects = Utilities.parseFileByLines(goldenfile);        
        newProjectOperator.selectCategory(categoryName);        
        JListOperator jlo = newProjectOperator.lstProjects();
        ArrayList<String> actualProjects = getProjectsList(jlo);
        
        System.out.println("======== Permanent UI projects in this category: ========");
        for (int i = 1; i < permanentProjects.size(); i++) { // skip the category name
            System.out.println(permanentProjects.get(i));            
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
        String dataDir = "";
        try {
            dataDir = getDataDir().getCanonicalPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return dataDir + File.separator + "permanentUI" + File.separator + "newproject" + File.separator+ categoryName + ".txt";
    }

    
    
}