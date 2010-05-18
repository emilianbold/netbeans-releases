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
 * CodeGenElementsTestCase.java
 *
 * Created on February 20, 2007, 4:39 PM
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
import org.netbeans.test.umllib.project.elem.IPackageElem;
import org.netbeans.test.umllib.project.elem.VisibilityType;
import org.netbeans.test.umllib.project.elem.impl.ArgumentElem;
import org.netbeans.test.umllib.project.elem.impl.AttributeElem;
import org.netbeans.test.umllib.project.elem.impl.ClassElem;
import org.netbeans.test.umllib.project.elem.impl.OperationElem;
import org.netbeans.test.umllib.project.elem.impl.PackageElem;
import org.netbeans.test.umllib.project.elem.impl.PrimitiveType;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.umllib.project.elem.IInterfaceElem;
import org.netbeans.test.umllib.project.elem.ModifierType;
import org.netbeans.test.umllib.project.elem.impl.InterfaceElem;
import org.netbeans.test.umllib.project.verifier.elem.ReflectionElementVerifier;
import org.netbeans.test.umllib.project.verifier.elem.TextElementVerifier;
/**
 *
 * @author Alexandr Scherbatiy
 */

public class CodeGenElementsTestCase extends CodeGenerationTestCase {
    
    
    
    
    
    /** Creates a new instance of GenCodeLinksTestCase */
    public CodeGenElementsTestCase(String name) {
        super(name);
    }
    
    //*
    
    // =================================================================== //
    // ======================  Class  Element       ====================== //
    // =================================================================== //
    
    public void testClassElementGeneration(){
        
        
        JavaProject javaProject = JavaProject.createProject(getNextJavaProjectName(), ProjectType.JAVA_APPLICATION, true, false);
        UMLProject umlProject = new UMLProject(UML_PROJECT_NAME, ProjectType.UML_JAVA_PLATFORM_MODEL);
        
        
        Node umlNode = new Node(umlProject.getProjectNode(), "Model|elements|pclass");
        umlProject.generateCode(umlNode, javaProject);
        
        compileJavaProject(getJavaProjectName());
        
        
    }
    
    public IClassElem  getClassElem(){
        
        IPackageElem elementPackage = new PackageElem("elements");
        IPackageElem pack = new PackageElem("pclass", elementPackage);
        
        
        IClassElem elem = new ClassElem("Class1", pack);
        
        
        elem.getAttributeList().add(new AttributeElem("attribute1", OBJECT, VisibilityType.PRIVATE));
        
        
        elem.getOperationList().add(new OperationElem("getAttribute1", OBJECT, VisibilityType.PUBLIC));
        elem.getOperationList().add(new OperationElem("setAttribute1", PrimitiveType.VOID, VisibilityType.PUBLIC, new IArgumentElem[] {new ArgumentElem("val", OBJECT)}));
        elem.getOperationList().add(new OperationElem("operation1", STRING, VisibilityType.PUBLIC));
        
        return elem;
    }
    
    public void testClassElementText(){
              
        IClassElem elem = getClassElem();
        
        assertVerification(new TextElementVerifier(getEditorOperator(elem), elem));
        
    }
    
    public void testClassElementClass(){
        JavaProject javaProject = new JavaProject(getJavaProjectName());
        
        IClassElem elem = getClassElem();
        
        Class cls = javaProject.getJavaClass(elem.getFullName());
           
        assertVerification(new ReflectionElementVerifier(cls, elem));
        
    }
    
    //*/
     
     
    // =================================================================== //
    // ======================  Interface  Element   ====================== //
    // =================================================================== //
     
    
    
    public void testInterfaceElementGeneration(){
     
     
        JavaProject javaProject = JavaProject.createProject(getNextJavaProjectName(), ProjectType.JAVA_APPLICATION, true, false);
        UMLProject umlProject = new UMLProject(UML_PROJECT_NAME, ProjectType.UML_JAVA_PLATFORM_MODEL);
     
     
        Node umlNode = new Node(umlProject.getProjectNode(), "Model|elements|pinterface");
        umlProject.generateCode(umlNode, javaProject);
     
        compileJavaProject(getJavaProjectName(), 82262);
     
     
    }

    public IInterfaceElem  getInterfaceElem(){
        
        IPackageElem elementPackage = new PackageElem("elements");
        IPackageElem pack = new PackageElem("pinterface", elementPackage);
     
     
        IInterfaceElem elem = new InterfaceElem("Interface1", pack);
     
     
        elem.getAttributeList().add(new AttributeElem("CONST1", PrimitiveType.INT, VisibilityType.PUBLIC, new ModifierType[] {ModifierType.STATIC, ModifierType.FINAL}, "1"));
     
        elem.getOperationList().add(new OperationElem("op1", PrimitiveType.VOID, VisibilityType.PUBLIC));
        
        return elem;
    }
    
    public void testInterfaceElementText(){
              
        IInterfaceElem elem = getInterfaceElem();
        
        assertVerification(new TextElementVerifier(getEditorOperator(elem), elem));
        
    }
    
    
    public void testInterfaceElementClass(){
        JavaProject javaProject = new JavaProject(getJavaProjectName());
     
        IInterfaceElem elem = getInterfaceElem();
        
        Class cls = javaProject.getJavaClass(elem.getFullName());
         
        assertVerification(new ReflectionElementVerifier(cls, elem));
     
    }
     
    //*/
    
    
}
