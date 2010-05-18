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
import org.netbeans.test.umllib.project.elem.IClassElem;
import org.netbeans.test.umllib.project.elem.IPackageElem;
import org.netbeans.test.umllib.project.elem.VisibilityType;
import org.netbeans.test.umllib.project.elem.impl.AttributeElem;
import org.netbeans.test.umllib.project.elem.impl.ClassElem;
import org.netbeans.test.umllib.project.elem.impl.PackageElem;
import org.netbeans.test.umllib.project.elem.impl.PrimitiveType;
import org.netbeans.test.umllib.project.verifier.TestVerifier;
import org.netbeans.test.umllib.project.verifier.elem.ReflectionElementVerifier;
import org.netbeans.test.umllib.project.verifier.elem.TextElementVerifier;

/**
 *
 * @author Alexandr Scherbatiy
 *
 */

public class CodeGenAttributesTestCase extends CodeGenerationTestCase {
    
    
    IPackageElem parentPackage = new PackageElem("attributes");

    
    
    /** Creates a new instance CodeGenElementsTestCase_1 */
    public CodeGenAttributesTestCase(String name) {
        super(name);
    }
    
    
    // =================================================================== //
    // ======================  Class  Element       ====================== //
    // =================================================================== //
    
    public void testAttributesGeneration(){
        generateProject(parentPackage.getName());
    }
    
    public IClassElem  getInitializedPrimitiveDataTypes(){
        
        IClassElem elem = new ClassElem("InitializedPrimitiveDataTypes", parentPackage);
        
        elem.getAttributeList().add(new AttributeElem("b", PrimitiveType.BYTE, VisibilityType.PRIVATE, "1"));
        elem.getAttributeList().add(new AttributeElem("s", PrimitiveType.SHORT, VisibilityType.PRIVATE, "10"));
        elem.getAttributeList().add(new AttributeElem("n", PrimitiveType.INT, VisibilityType.PRIVATE, "Math.max(3,4)"));
        elem.getAttributeList().add(new AttributeElem("l", PrimitiveType.LONG, VisibilityType.PRIVATE, "100"));
        elem.getAttributeList().add(new AttributeElem("f", PrimitiveType.FLOAT, VisibilityType.PRIVATE, "1e10f"));
        elem.getAttributeList().add(new AttributeElem("d", PrimitiveType.DOUBLE, VisibilityType.PRIVATE, "1.0001"));
        elem.getAttributeList().add(new AttributeElem("flag", PrimitiveType.BOOLEAN, VisibilityType.PRIVATE, "true"));
        elem.getAttributeList().add(new AttributeElem("c", PrimitiveType.CHAR, VisibilityType.PRIVATE, "'c'"));

        return elem;
    }
    
    public void testInitializedPrimitiveDataTypesText(){
        
        IClassElem elem = getInitializedPrimitiveDataTypes();
        
        TestVerifier elementVerifier = new TextElementVerifier(getEditorOperator(elem), elem);
        assertVerification(elementVerifier);
        
    }
    
    public void testInitializedPrimitiveDataTypesClass(){
        JavaProject javaProject = new JavaProject(getJavaProjectName());
 
        IClassElem elem = getInitializedPrimitiveDataTypes();
 
        Class cls = javaProject.getJavaClass(elem.getFullName());
        
        ReflectionElementVerifier elementVerifier = new ReflectionElementVerifier(cls, elem);
        assertVerification(elementVerifier);
 
    }
    
    public IClassElem getInitializedObjectDataTypesElem(){

        IClassElem elem = new ClassElem("InitializedObjectDataTypes", parentPackage);
        
        
        elem.getAttributeList().add(new AttributeElem("str", STRING, VisibilityType.PRIVATE, "\"Hello world!\""));
        elem.getAttributeList().add(new AttributeElem("obj", OBJECT, VisibilityType.PRIVATE, "new Object()"));
        elem.getAttributeList().add(new AttributeElem("cls", CLASS, VisibilityType.PRIVATE, "InitializedObjectDataTypes.class"));

        return elem;
    }
    
    public void testInitializedObjectDataTypesText(){
        
        
        IClassElem elem = getInitializedObjectDataTypesElem();
        
        TestVerifier elementVerifier = new TextElementVerifier(getEditorOperator(elem), elem);
        assertVerification(elementVerifier);
        
    }
    
    public void testInitializedObjectDataTypesClass(){
        
        JavaProject javaProject = new JavaProject(getJavaProjectName());
 
        IClassElem elem = getInitializedObjectDataTypesElem();
 
        Class cls = javaProject.getJavaClass(elem.getFullName());
 
        ReflectionElementVerifier elementVerifier = new ReflectionElementVerifier(cls, elem);
        assertVerification(elementVerifier);
 
    }
    
    
    
    
    
}
