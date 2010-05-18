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
import org.netbeans.test.umllib.project.elem.IOperationElem;
import org.netbeans.test.umllib.project.elem.IPackageElem;
import org.netbeans.test.umllib.project.elem.VisibilityType;
import org.netbeans.test.umllib.project.elem.impl.ArgumentElem;
import org.netbeans.test.umllib.project.elem.impl.ClassElem;
import org.netbeans.test.umllib.project.elem.impl.DataTypeElem;
import org.netbeans.test.umllib.project.elem.impl.OperationElem;
import org.netbeans.test.umllib.project.elem.impl.PackageElem;
import org.netbeans.test.umllib.project.elem.impl.PrimitiveType;
import org.netbeans.test.umllib.project.verifier.TestVerifier;
import org.netbeans.test.umllib.project.verifier.elem.ReflectionElementVerifier;
import org.netbeans.test.umllib.project.verifier.elem.TextElementVerifier;

/**
 *
 * @author Alexandr Scherbatiy
 * @email  sunflower@netbeans.org
 *
 */

public class CodeGenOperationTestCase extends CodeGenerationTestCase {
    
    
    IPackageElem parentPackage = new PackageElem("operations");
        
    
    /** Creates a new instance CodeGenElementsTestCase_1 */
    public CodeGenOperationTestCase(String name) {
        super(name);
    }
    
    
    // =================================================================== //
    // ======================  Class  Element       ====================== //
    // =================================================================== //
    
    public void testOperationGeneration(){
        generateProject(parentPackage.getName());
    }
    
    public IClassElem  getExceptionListClass(){
        
        IClassElem elem = new ClassElem("Exceptions", parentPackage);
        
        // public void exceptionOp () throws Exception
        IOperationElem op = new OperationElem("exceptionOp", PrimitiveType.VOID, VisibilityType.PUBLIC);
        op.getExceptionList().add(new DataTypeElem("Exception"));
        elem.getOperationList().add(op);
        
        // public double throwableOp () throws Throwable
        op = new OperationElem("throwableOp", PrimitiveType.DOUBLE, VisibilityType.PUBLIC);
        op.getExceptionList().add(new DataTypeElem("Throwable"));
        elem.getOperationList().add(op);
        
        // public String mixedExceptionsOp () throws java.io.IOException, Exception, ClassCastException, java.util.IllegalFormatException
        op = new OperationElem("mixedExceptionsOp", STRING, VisibilityType.PUBLIC);
        op.getExceptionList().add(new DataTypeElem("java.io.IOException"));
        op.getExceptionList().add(new DataTypeElem("Exception"));
        op.getExceptionList().add(new DataTypeElem("ClassCastException"));
        op.getExceptionList().add(new DataTypeElem("java.util.IllegalFormatException"));  
        elem.getOperationList().add(op);
        
        // public java.io.File ioExceptionsOp (java.io.Reader reader) throws java.io.IOException
        //op = new OperationElem("ioExceptionsOp", new ClassElem("File", javaIO), VisibilityType.PUBLIC);
        op = new OperationElem("ioExceptionsOp", new DataTypeElem("java.io.File"), VisibilityType.PUBLIC);
        op.getArgumentList().add(new ArgumentElem("reader", new DataTypeElem("java.io.Reader")));
        op.getExceptionList().add(new DataTypeElem("java.io.IOException"));
        elem.getOperationList().add(op);
        
        
        
        return elem;
    }
    
    public void testOperationExceptionListText(){
        
        IClassElem elem = getExceptionListClass();
        
        TestVerifier elementVerifier = new TextElementVerifier(getEditorOperator(elem), elem);
        assertVerification(elementVerifier);
        
    }
    
    public void testOperationExceptionListClass(){
        JavaProject javaProject = new JavaProject(getJavaProjectName());
        
        IClassElem elem = getExceptionListClass();
        
        Class cls = javaProject.getJavaClass(elem.getFullName());
        
        ReflectionElementVerifier elementVerifier = new ReflectionElementVerifier(cls, elem);
        assertVerification(elementVerifier);
        
    }
    
    
    
    
    
}
