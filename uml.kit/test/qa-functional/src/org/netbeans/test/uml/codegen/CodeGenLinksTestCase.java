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
 * CodeGenLinksTestCase.java
 *
 * Created on February 2, 2007, 6:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.uml.codegen;

import org.netbeans.test.umllib.project.JavaProject;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.project.elem.IArgumentElem;
import org.netbeans.test.umllib.project.elem.IClassElem;
import org.netbeans.test.umllib.project.elem.IInterfaceElem;
import org.netbeans.test.umllib.project.elem.IPackageElem;
import org.netbeans.test.umllib.project.elem.VisibilityType;
import org.netbeans.test.umllib.project.elem.impl.ArgumentElem;
import org.netbeans.test.umllib.project.elem.impl.AttributeElem;
import org.netbeans.test.umllib.project.elem.impl.ClassElem;
import org.netbeans.test.umllib.project.elem.impl.OperationElem;
import org.netbeans.test.umllib.project.elem.impl.PackageElem;
import org.netbeans.test.umllib.project.elem.impl.PrimitiveType;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.umllib.project.elem.IJavaElem;
import org.netbeans.test.umllib.project.elem.impl.InterfaceElem;
import org.netbeans.test.umllib.project.verifier.elem.ReflectionElementVerifier;
import org.netbeans.test.umllib.project.verifier.elem.TextElementVerifier;

/**
 *
 * @author Alexandr Scherbatiy
 */


public class CodeGenLinksTestCase extends CodeGenerationTestCase {
    
    /** Creates a new instance of GenCodeLinksTestCase */
    public CodeGenLinksTestCase(String name) {
        super(name);
    }
    
    
    //*
     
    // =================================================================== //
    // ======================  Implementation Link  ====================== //
    // =================================================================== //
     
    public void testImplementationLinkGeneration(){
     
        JavaProject javaProject = JavaProject.createProject(getNextJavaProjectName(), ProjectType.JAVA_APPLICATION, true, false);
        UMLProject umlProject = new UMLProject(UML_PROJECT_NAME, ProjectType.UML_JAVA_PLATFORM_MODEL);
     
     
        Node umlNode = new Node(umlProject.getProjectNode(), "Model|links|implementation");
        umlProject.generateCode(umlNode, javaProject);
     
        compileJavaProject(getJavaProjectName());
     
    }

    public IInterfaceElem  getImplementationLinkInterface(){
        IPackageElem linkPackage = new PackageElem("links");
        IPackageElem pack = new PackageElem("implementation", linkPackage);
     
        IInterfaceElem interfaceElem = new InterfaceElem("Interface1", pack);
     
        interfaceElem.getOperationList().add(new OperationElem("run", PrimitiveType.VOID, VisibilityType.PUBLIC));
        return interfaceElem;

        
    }
     
    public IClassElem  getImplementationLinkClass(){

        IPackageElem linkPackage = new PackageElem("links");
        IPackageElem pack = new PackageElem("implementation", linkPackage);
     
     
        IClassElem classElem = new ClassElem("Class1", pack);
        classElem.getSuperInterfaceList().add(getImplementationLinkInterface());
     
        classElem.getOperationList().add(new OperationElem("run", PrimitiveType.VOID, VisibilityType.PUBLIC));
        return classElem;

    }
    
    
    //  ===          Class1 implements Interface1       ====
    
    public void testImplementationLinkTextInterface(){
        IInterfaceElem elem = getImplementationLinkInterface();        
        assertVerification(new TextElementVerifier(getEditorOperator(elem), elem));        
    }
    
    public void testImplementationLinkTextClass(){
        IClassElem elem = getImplementationLinkClass();        
        assertVerification(new TextElementVerifier(getEditorOperator(elem), elem));        
    }
    
    public void testImplementationLinkReflectionClass(){
     
        JavaProject javaProject = new JavaProject(getJavaProjectName());
     
        Class cls = javaProject.getJavaClass("links.implementation.Class1");
        IClassElem classElem = getImplementationLinkClass();
     
     
        ReflectionElementVerifier elementVerifier = new ReflectionElementVerifier(cls, classElem);
        assertVerification(elementVerifier);
     
     
    }
   // */
    
    // =================================================================== //
    // ======================  Generalization Link  ====================== //
    // =================================================================== //
     
     
    public void testGeneralizationLinkGeneration(){
     
        JavaProject javaProject = JavaProject.createProject(getNextJavaProjectName(), ProjectType.JAVA_APPLICATION, true, false);
        UMLProject umlProject = new UMLProject(UML_PROJECT_NAME, ProjectType.UML_JAVA_PLATFORM_MODEL);
     
        Node umlNode = new Node(umlProject.getProjectNode(), "Model|links|generalization");
        umlProject.generateCode(umlNode, javaProject);
     
        compileJavaProject(getJavaProjectName());
     
    }

    public IClassElem  getGeneralizationLinkSuperClass(){

        IPackageElem linkPackage = new PackageElem("links");
        IPackageElem pack = new PackageElem("generalization", linkPackage);
     
        IClassElem elem = new ClassElem("Class1", pack);
     
        return elem;

    }
    
    public IClassElem  getGeneralizationLinkClass(){

        IPackageElem linkPackage = new PackageElem("links");
        IPackageElem pack = new PackageElem("generalization", linkPackage);    
     
        IClassElem elem = new ClassElem("Class2", pack, getGeneralizationLinkSuperClass());
        return elem;

    }
    
    
    //  ===          Class2 extends Class1       ====
     
    public void testGeneralizationLinkTextClasses(){
        IJavaElem elem = getGeneralizationLinkClass();        
        assertVerification(new TextElementVerifier(getEditorOperator(elem), elem));        
    }
    
    public void testGeneralizationLinkReflectionClasses(){     
        assertVerification(new ReflectionElementVerifier(getGeneralizationLinkClass(), getJavaProjectName()));
    }
     
     
    //  ===          Interface2 extends Interface1        ====
     
    public void testGeneralizationLinkInterfaces(){
     
        JavaProject javaProject = new JavaProject(getJavaProjectName());
     
        Class cls = javaProject.getJavaClass("links.generalization.Interface2");
     
     
        IPackageElem linkPackage = new PackageElem("links");
        IPackageElem pack = new PackageElem("generalization", linkPackage);
     
        // Super Class
        IInterfaceElem interfaceElem1 = new InterfaceElem("Interface1", pack);
        IInterfaceElem interfaceElem2 = new InterfaceElem("Interface2", pack, interfaceElem1);
     
     
        ReflectionElementVerifier elementVerifier = new ReflectionElementVerifier(cls, interfaceElem2);
        assertVerification(elementVerifier);
     
    }
     
//*     
    
    // =================================================================== //
    // ======================  Aggregation    Link  ====================== //
    // =================================================================== //
     
    public void testAggregationLinkGeneration(){
     
        JavaProject javaProject = JavaProject.createProject(getNextJavaProjectName(), ProjectType.JAVA_APPLICATION, true, false);
        UMLProject umlProject = new UMLProject(UML_PROJECT_NAME, ProjectType.UML_JAVA_PLATFORM_MODEL);
     
     
        Node umlNode = new Node(umlProject.getProjectNode(), "Model|links|aggregation");
        umlProject.generateCode(umlNode, javaProject);
     
        compileJavaProject(getJavaProjectName());
     
    }
     
    //  ===          Class1 {  Class2 mClass2;  }       ====
     
    public void testAggregationLinkClasses(){
     
        JavaProject javaProject = new JavaProject(getJavaProjectName());
     
        Class cls = javaProject.getJavaClass("links.aggregation.Class1");
     
     
        IPackageElem linkPackage = new PackageElem("links");
        IPackageElem pack = new PackageElem("aggregation", linkPackage);
     
        // attribute type
        IClassElem classElem2 = new ClassElem("Class2", pack);
     
        IClassElem classElem1 = new ClassElem("Class1", pack);
     
        classElem1.getAttributeList().add(new AttributeElem("mClass2", classElem2, VisibilityType.PRIVATE));
        classElem1.getOperationList().add(new OperationElem("getClass2", classElem2, VisibilityType.PUBLIC));
        classElem1.getOperationList().add(new OperationElem("setClass2", PrimitiveType.VOID, VisibilityType.PUBLIC, new IArgumentElem[]{ new ArgumentElem("val", classElem2)}));
     
     
        ReflectionElementVerifier elementVerifier = new ReflectionElementVerifier(cls, classElem1);
        assertVerification(elementVerifier);
     
    }
    //*
    // =================================================================== //
    // ======================     Nested Link       ====================== //
    // =================================================================== //
     
    public void testNestedLinkGeneration(){
     
        JavaProject javaProject = JavaProject.createProject(getNextJavaProjectName(), ProjectType.JAVA_APPLICATION, true, false);
        UMLProject umlProject = new UMLProject(UML_PROJECT_NAME, ProjectType.UML_JAVA_PLATFORM_MODEL);
     
     
        Node umlNode = new Node(umlProject.getProjectNode(), "Model|links|nested");
        umlProject.generateCode(umlNode, javaProject);
     
        compileJavaProject(getJavaProjectName());
     
    }
     
    //  ===          class Outer {  class Inner{}   }       ====
     
    public void testNestedLinkClasses(){
     
        JavaProject javaProject = new JavaProject(getJavaProjectName());
     
        Class cls = javaProject.getJavaClass("links.nested.Outer");
     
     
        IPackageElem linkPackage = new PackageElem("links");
        IPackageElem pack = new PackageElem("nested", linkPackage);
     
        // attribute type
        IClassElem innerElem = new ClassElem("Inner", pack);
     
        IClassElem outerElem = new ClassElem("Outer", pack);
     
        outerElem.getNestedElemList().add(innerElem);
     
     
        ReflectionElementVerifier elementVerifier = new ReflectionElementVerifier(cls, outerElem);
        assertVerification(elementVerifier);
     
    }
     

    // =================================================================== //
    // ======================    Generic Link       ====================== //
    // =================================================================== //
    
    public void testGenericLinkGeneration(){
        
        JavaProject javaProject = JavaProject.createProject(getNextJavaProjectName(), ProjectType.JAVA_APPLICATION, true, false);
        UMLProject umlProject = new UMLProject(UML_PROJECT_NAME, ProjectType.UML_JAVA_PLATFORM_MODEL);
        
        
        Node umlNode = new Node(umlProject.getProjectNode(), "Model|links|generic");
        umlProject.generateCode(umlNode, javaProject);
        
        compileJavaProject(getJavaProjectName(), 78564 );
        
    }
    
    
    public IClassElem  getGenericClassElem(){
        
        IPackageElem elementPackage = new PackageElem("links");
        IPackageElem pack = new PackageElem("generic", elementPackage);
        
        
        IClassElem elem = new ClassElem("Class2", pack);
        IClassElem templateElem = new ClassElem("TemplateClass1", pack);
        templateElem.getTemplateParameterList().add(INTEGER);
        
        elem.getAttributeList().add(new AttributeElem("mTemplateClass1String", templateElem));
        
        
        elem.getOperationList().add(new OperationElem("getTemplateClass1String", templateElem, VisibilityType.PUBLIC));
        elem.getOperationList().add(new OperationElem("setTemplateClass1String", PrimitiveType.VOID, VisibilityType.PUBLIC, new IArgumentElem[] {new ArgumentElem("val", templateElem)}));
        
        return elem;
    }
    
    public void testGenericLinkAttributesText(){
        
        IClassElem elem = getGenericClassElem();
        
        assertVerification(new TextElementVerifier(getEditorOperator(elem), elem));
        
    }
    
    public void testGenericLinkAttributesClass(){
        JavaProject javaProject = new JavaProject(getJavaProjectName());
        
        IClassElem elem = getGenericClassElem();
        
        Class cls = javaProject.getJavaClass(elem.getFullName());
        
        assertVerification(new ReflectionElementVerifier(cls, elem));
        
    }
    
    //*/
    
}

