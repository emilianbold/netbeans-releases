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
 * CodeGenGenericLinksTestCase.java
 *
 * Created on April 17, 2007, 2:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.uml.codegen;

import org.netbeans.test.umllib.project.JavaProject;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.umllib.project.elem.IClassElem;
import org.netbeans.test.umllib.project.elem.IInterfaceElem;
import org.netbeans.test.umllib.project.elem.IJavaElem;
import org.netbeans.test.umllib.project.elem.IPackageElem;
import org.netbeans.test.umllib.project.elem.impl.ClassElem;
import org.netbeans.test.umllib.project.elem.impl.DerivationClassifierElem;
import org.netbeans.test.umllib.project.elem.impl.InterfaceElem;
import org.netbeans.test.umllib.project.elem.impl.PackageElem;
import org.netbeans.test.umllib.project.verifier.elem.ReflectionElementVerifier;
import org.netbeans.test.umllib.project.verifier.elem.TextElementVerifier;

/**
 *
 * @author sunflower
 */
public class CodeGenGenericLinksTestCase extends CodeGenerationTestCase{
 
        IPackageElem linkPackage = new PackageElem("links");
        IPackageElem pack = new PackageElem("generic", linkPackage);
    
    
    /** Creates a new instance of CodeGenGenericLinksTestCase */
    public CodeGenGenericLinksTestCase(String name) {
        super(name);
    }
    
    // =================================================================== //
    // ======================  Generate Code        ====================== //
    // =================================================================== //
     
    public void testGenericLinkGeneration(){
     
        JavaProject javaProject = JavaProject.createProject(getNextJavaProjectName(), ProjectType.JAVA_APPLICATION, true, false);
        UMLProject umlProject = new UMLProject(UML_PROJECT_NAME, ProjectType.UML_JAVA_PLATFORM_MODEL);
     
     
        Node umlNode = new Node(umlProject.getProjectNode(), "Model|links|generic");
        umlProject.generateCode(umlNode, javaProject);
     
        compileJavaProject(getJavaProjectName());
     
    }

    // =================================================================== //
    // ======================  Generalization Link  ====================== //
    // =================================================================== //

    public IClassElem  getGeneralizationLinkSuperClass(){
     
     
        IClassElem elem = new ClassElem("TemplateClass1", pack);
        
        return new DerivationClassifierElem(elem, new IJavaElem[] {INTEGER} );
        
    }
    public IClassElem  getGeneralizationLinkClass(){

     
        IClassElem elem = new ClassElem("Class1", pack, getGeneralizationLinkSuperClass() );
        return elem;

    }
    
    //  ===          Class1 extends TemplateClass1<String>        ====
     
    public void testGeneralizationLinkTextClasses(){
        IJavaElem elem = getGeneralizationLinkClass();        
        assertVerification(new TextElementVerifier(getEditorOperator(elem), elem));        
    }
    
    public void testGeneralizationLinkReflectionClasses(){     
        assertVerification(new ReflectionElementVerifier(getGeneralizationLinkClass(), getJavaProjectName()));
    }
    
    // =================================================================== //
    // ======================  Implementation Link  ====================== //
    // =================================================================== //
    
    
    public IInterfaceElem  getImplementationLinkSuperInterface(){
     
     
        IInterfaceElem elem = new InterfaceElem("TemplateInterfcae1", pack);
        
        return new DerivationClassifierElem(elem, new IJavaElem[] {INTEGER} );
        
    }
    public IClassElem  getImplementationLinkClass(){
     
        IClassElem elem = new ClassElem("Class3", pack ); //, getGeneralizationLinkSuperClass() );
        elem.getSuperInterfaceList().add(getImplementationLinkSuperInterface());
        
        return elem;

    }
    //  ===          Class3 implements TemplateInterface1<Integer>        ====
    
    public void testImplementationLinkTextClasses(){
        IJavaElem elem = getImplementationLinkClass();        
        assertVerification(new TextElementVerifier(getEditorOperator(elem), elem));        
    }
    
    public void testImplementationLinkReflectionClasses(){     
        assertVerification(new ReflectionElementVerifier(getImplementationLinkClass(), getJavaProjectName()));
    }
    
    
    
    
    // =================================================================== //
    // ======================  Aggregation  Link    ====================== //
    // =================================================================== //
    
}
