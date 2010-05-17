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


/*
 * CodeGenerationTestCase.java
 *
 * Created on February 2, 2007, 6:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.uml.codegen;


import org.netbeans.jellytools.EditorOperator;
import org.netbeans.test.umllib.project.JavaProject;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.project.elem.IJavaElem;
import org.netbeans.test.umllib.project.elem.impl.PredefinedType;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.Debug;
import org.netbeans.test.umllib.util.PopupConstants;
import org.netbeans.test.umllib.util.Utils;
import org.netbeans.test.umllib.util.Utils;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.project.elem.IPackageElem;
import org.netbeans.test.umllib.project.elem.impl.PackageElem;
import org.netbeans.test.umllib.project.verifier.TestVerifier;

/**
 *
 * @author Alexandr Scherbatiy
 */

public class CodeGenerationTestCase extends UMLTestCase{
    
    
    
    
    protected static final String UML_PROJECT_NAME = "CodeGenerationProject-Model";
    protected static final String JAVA_PROJECT_NAME = "CodeGenerationProject-Java";
    
    
    private static int id = 0;
    
    protected IPackageElem  java = new PackageElem("java");
    protected IPackageElem  javaLang = new PackageElem("lang", java);
    protected IPackageElem  javaIO = new PackageElem("io", java);
    
    protected IJavaElem OBJECT = PredefinedType.getClassElem(PredefinedType.JAVA_LANG_OBJECT);
    protected IJavaElem STRING = PredefinedType.getClassElem(PredefinedType.JAVA_LANG_STRING);
    protected IJavaElem CLASS  = PredefinedType.getClassElem(PredefinedType.JAVA_LANG_CLASS);

    protected IJavaElem INTEGER  = PredefinedType.getClassElem(PredefinedType.JAVA_LANG_INTEGER);
    
    
    private static boolean openProjectFlag = false;
    
    
    /** Creates a new instance of CodeGenerationTestCase */
    public CodeGenerationTestCase(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        Debug.showLog(this);
        Project.openProject(XTEST_PROJECT_DIR + "/Projects-CodeGeneration/" +  UML_PROJECT_NAME);
    }
    
    
    protected void tearDown() throws Exception {
        super.tearDown();
        Utils.tearDown();
    }
    
    
    
    protected void compileJavaProject(String projectName){
        
        compileJavaProject(projectName, 0);
    }
    
    protected void compileJavaProject(String projectName, int bugNumber){
        
        Debug.showLog(this);
        
        System.out.println("Test Compilation!!!");
        JavaProject javaProject = new JavaProject(projectName);
        javaProject.build();
        //sleep(6000);
        
        
        
        
        
        int maxTime = 150;
        //System.out.println("start: " + System.currentTimeMillis());
        while(true){
            JavaProject.Output output = javaProject.getOutput();
            String outputText = output.getText();
            
            assertNotNull("Output is Null!!!", output);
            //System.out.println("Output: \n" + output.getText());
            
            if(outputText.contains(JavaProject.BUILD_SUCCESSFUL)){
                break;
            }
            
            if(outputText.contains(JavaProject.BUILD_FAILED) || (maxTime < 0)){
                //System.out.println("end  : " + System.currentTimeMillis());
                if(bugNumber == 0){
                    fail("Build Fail: " + outputText);
                }else{
                    failByBug(bugNumber, "Build Fail: " + outputText);
                    
                }
            }
            sleep(100);
            maxTime--;
        }
        
    }
    
    public static String getJavaProjectName(){
        return JAVA_PROJECT_NAME  + id;
    }
    
    
    public static String getNextJavaProjectName(){
        id++;
        return getJavaProjectName();
    }
    
    protected static void openProject(String projectPath){
        
        System.out.println("WORK DIR: \"" +  Utils.WORK_DIR + "\"");
        System.out.println("path: \"" + projectPath + "\"");
        
        String fullPath = Utils.WORK_DIR + projectPath;
        System.out.println("full path: \"" + fullPath + "\"");
        
        if(!openProjectFlag){
            Project.openProject(fullPath);
            openProjectFlag = true;
        }
        
    }
    
    protected EditorOperator getEditorOperator(IJavaElem javaElem){
        
        JavaProject javaProject = new JavaProject(getJavaProjectName());
        
        String path = "";
        
        /*
        StringTokenizer tokenizer = new StringTokenizer(javaElem.getFullName(),".");
         
        path += tokenizer.nextToken();
         
        while(tokenizer.hasMoreTokens()){
            path += "|" + tokenizer.nextToken();
        }
         */
        
        path += javaElem.getPackage().getFullName();
        path += "|" + javaElem.getName();
        
        path += ".java";
        
        System.out.println("path = \"" + path + "\"");
        Node javaProjectNode = new Node(javaProject.getSrcNode(),path);
        
        javaProjectNode.performPopupActionNoBlock(PopupConstants.OPEN);
        sleep(2000);
        EditorOperator editorOperator = new EditorOperator(javaElem.getName() + ".java");
        
        return editorOperator;
    }
    
    
       public void generateProject(String umlNodePath){
     
        JavaProject javaProject = JavaProject.createProject(getNextJavaProjectName(), ProjectType.JAVA_APPLICATION, true, false);
        UMLProject umlProject = new UMLProject(UML_PROJECT_NAME, ProjectType.UML_JAVA_PLATFORM_MODEL);
     
     
        Node umlNode = new Node(umlProject.getProjectNode(), "Model|" + umlNodePath);
        umlProject.generateCode(umlNode, javaProject);
     
        compileJavaProject(getJavaProjectName());
     
    }
    
    public void assertVerification(TestVerifier testVerifier){
        testVerifier.verify();
        assertTrue(testVerifier.getMessage(), testVerifier.getResult());
    }
    
    protected void closeJavaProject(){
        new JavaProject(getJavaProjectName()).close();
    }
 
    
    protected void sleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
}

