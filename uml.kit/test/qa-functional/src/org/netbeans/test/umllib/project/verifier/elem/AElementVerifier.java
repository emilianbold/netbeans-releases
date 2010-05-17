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
 * AElementVerifier.java
 *
 * Created on April 17, 2007, 1:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.project.verifier.elem;

import java.util.List;
import org.netbeans.test.umllib.project.elem.IAttributeElem;
import org.netbeans.test.umllib.project.elem.IClassElem;
import org.netbeans.test.umllib.project.elem.IInterfaceElem;
import org.netbeans.test.umllib.project.elem.IJavaElem;
import org.netbeans.test.umllib.project.elem.IOperationElem;
import org.netbeans.test.umllib.project.verifier.TestVerifier;

/**
 *
 * @author <A HREF="mailto:sunflower@netbeans.org">Alexandr Scherbatiy</A>
 */

public abstract class AElementVerifier implements TestVerifier{
    private String message = "";
    private boolean result = true;
    
    private IJavaElem javaElem;
    
    
    
    public AElementVerifier(IJavaElem javaElem){
        this.javaElem = javaElem;
        this.message += "[Class: " + javaElem.getFullName() + "]";
        System.out.println("---------------------------------------------------------");
        System.out.println("[Elem]");
        System.out.println(javaElem);
        System.out.println("---------------------------------------------------------");
        
    }
    
    public void verify() {
        
        verifyName(javaElem);
        
        switch(javaElem.getType()){
            case CLASS: verifyClassElem( (IClassElem) javaElem ); break;
            case INTERFACE: verifyInterfaceElem( (IInterfaceElem) javaElem ); break;
            // ...
        }
        
        verifyNestedElem(javaElem.getNestedElemList());
        
    }
    
    
    protected void verifyClassElem(IClassElem classElem) {
        verifySuperClass(classElem.getSuperClass());
        verifySuperInterfaceList(classElem.getSuperInterfaceList());
        verifyAttributeList(classElem.getAttributeList());
        verifyOperationList(classElem.getOperationList());
    }
    
    protected void verifyInterfaceElem(IInterfaceElem interfaceElem) {
        verifySuperInterfaceList(interfaceElem.getSuperInterfaceList());
        verifyAttributeList(interfaceElem.getAttributeList());
        verifyOperationList(interfaceElem.getOperationList());
    }
    
    private void verifyNestedElem(List<IJavaElem> javaElemList) {
        for(IJavaElem javaElem: javaElemList){
            verifyNestedElem(javaElem);
        }
    }
    
    private void verifyAttributeList(List<IAttributeElem> attributeList) {
        for(IAttributeElem attributeElem: attributeList){
            verifyAttribute(attributeElem);
        }
    }
    private void verifyOperationList(List<IOperationElem> operationList) {
        for(IOperationElem operationElem: operationList){
            verifyOperation(operationElem);
        }
    }
    
    protected abstract void verifyName(IJavaElem javaElem);
    
    protected abstract void verifySuperClass(IClassElem superClass);
    protected abstract void verifySuperInterfaceList(List<IInterfaceElem> superInterfaceList);
    
    protected abstract void verifyAttribute(IAttributeElem attributeElem);
    protected abstract void verifyOperation(IOperationElem operationElem);
    protected abstract void verifyNestedElem(IJavaElem javaElem) ;
    
    
    public boolean getResult() {
        return result;
    }
    
    public String getMessage() {
        return message;
    }
    
    
    
    protected void appendMessage(String msg){
        result = false;
        message += "[ (-) " + msg +  " ]";
        
    }
}
