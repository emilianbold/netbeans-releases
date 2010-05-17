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
 * CodeGenMixedGenericTestCase.java
 *
 * Created on April 25, 2007, 4:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.uml.codegen;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.umllib.project.JavaProject;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.project.elem.IClassElem;
import org.netbeans.test.umllib.project.elem.IInterfaceElem;
import org.netbeans.test.umllib.project.elem.IJavaElem;
import org.netbeans.test.umllib.project.elem.IPackageElem;
import org.netbeans.test.umllib.project.elem.impl.ClassElem;
import org.netbeans.test.umllib.project.elem.impl.DataTypeElem;
import org.netbeans.test.umllib.project.elem.impl.DerivationClassifierElem;
import org.netbeans.test.umllib.project.elem.impl.InterfaceElem;
import org.netbeans.test.umllib.project.elem.impl.PackageElem;
import org.netbeans.test.umllib.project.verifier.elem.ReflectionElementVerifier;
import org.netbeans.test.umllib.project.verifier.elem.TextElementVerifier;

/**
 *
 * @author Alexandr Scherbatiy sunflower@netbeans.org
 */

public class CodeGenMixedGenericTestCase extends CodeGenerationTestCase{
    
    IPackageElem linkPackage = new PackageElem("mixed");
    IPackageElem pack = new PackageElem("mgeneric", linkPackage);
    
    
    /** Creates a new instance of CodeGenMixedGenericTestCase */
    public CodeGenMixedGenericTestCase(String name) {
        super(name);
    }
    
    
    // =================================================================== //
    // ======================  Generate Code        ====================== //
    // =================================================================== //
    
    public void testGenericLinkGeneration(){
        
        JavaProject javaProject = JavaProject.createProject(getNextJavaProjectName(), ProjectType.JAVA_APPLICATION, true, false);
        UMLProject umlProject = new UMLProject(UML_PROJECT_NAME, ProjectType.UML_JAVA_PLATFORM_MODEL);
        
        
        Node umlNode = new Node(umlProject.getProjectNode(), "Model|mixed|mgeneric");
        umlProject.generateCode(umlNode, javaProject);
        
        compileJavaProject(getJavaProjectName());
        
    }
        
    
    public IInterfaceElem  getI1Interface(){
        IInterfaceElem elem = new InterfaceElem("I1", pack);
        return elem;
    }
    
    public IInterfaceElem  getI2Interface(){
        IInterfaceElem elem = new InterfaceElem("I2", pack);
        return elem;
    }
    
    public IClassElem  getBClass(){
        IClassElem elem = new ClassElem("B", pack );
        return elem;
    }
    
    public DerivationClassifierElem  getBDerivationClassifier(){
        return new DerivationClassifierElem(getBClass(), new IJavaElem[] {new DataTypeElem("Integer"), new DataTypeElem("Integer")} );
        
    }
    
    public IClassElem  getAClass(){
                
        IClassElem elem = new ClassElem("A", pack,  getBDerivationClassifier());
        
        elem.getSuperInterfaceList().add(new DerivationClassifierElem(getI1Interface(),new IJavaElem[] {INTEGER}));
        elem.getSuperInterfaceList().add(new DerivationClassifierElem(getI2Interface(),new IJavaElem[] {INTEGER}));
        
        return elem;
        
    }
    
    //  ===  class A < T1, T2 > extends B<Integer,Integer> implements I1<Integer>, I2<Integer>  ====
    
    public void testGeneralizationLinkTextClasses(){
        IJavaElem elem = getAClass();
        assertVerification(new TextElementVerifier(getEditorOperator(elem), elem));
    }
    
    public void testGeneralizationLinkReflectionClasses(){
        assertVerification(new ReflectionElementVerifier(getAClass(), getJavaProjectName()));
    }
    
}
