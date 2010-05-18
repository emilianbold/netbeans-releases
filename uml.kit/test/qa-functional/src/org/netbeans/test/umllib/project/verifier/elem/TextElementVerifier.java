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
 * TextElementVerifier.java
 *
 * Created on April 17, 2007, 1:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.project.verifier.elem;

import java.util.Iterator;
import java.util.List;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.test.umllib.project.elem.IArgumentElem;
import org.netbeans.test.umllib.project.elem.IAttributeElem;
import org.netbeans.test.umllib.project.elem.IClassElem;
import org.netbeans.test.umllib.project.elem.IInterfaceElem;
import org.netbeans.test.umllib.project.elem.IJavaElem;
import org.netbeans.test.umllib.project.elem.IOperationElem;
import org.netbeans.test.umllib.project.elem.ModifierType;
import org.netbeans.test.umllib.project.elem.impl.JavaElem;

/**
 *
 * @author <A HREF="mailto:sunflower@netbeans.org">Alexandr Scherbatiy</A>
 */
public class TextElementVerifier extends AElementVerifier{
    
    private String message = "";
    private boolean result = true;
    
    private IJavaElem javaElem;
    private EditorOperator editorOperator;
    
    
    public TextElementVerifier(EditorOperator editorOperator, IJavaElem javaElem){
        super(javaElem);
        this.editorOperator = editorOperator;
        System.out.println("-----------  Text Verifier  -----------------");
        System.out.println(editorOperator.getText());
        System.out.println("---------------------------------------------");
        
    }
    
    protected void verifyName(IJavaElem javaElem) {
        if(!editorOperator.contains(javaElem.getName())){
            appendMessage("name : \"" + javaElem.getName() + "\" not found!");
        }
        
    }
    
    
    protected void verifySuperClass(IClassElem superClass) {
        if(superClass != null && !superClass.getName().equals("Object")){
            String superClassName = JavaElem.getGenericSignatureName(superClass);
            System.out.println("[check] super class: \"extends " + superClassName +"\"");
            if(!editorOperator.contains("extends " + superClassName)){
                appendMessage("super class: \"" + superClassName + "\" not found!");
            }
            
        }
    }
    
    protected void verifySuperInterfaceList(List<IInterfaceElem> superInterfaceList) {
        
        if(superInterfaceList != null && superInterfaceList.size() != 0){
            String implement = "";
            
            Iterator<IInterfaceElem> iter = superInterfaceList.iterator();
            
            if(iter.hasNext()){
                implement += JavaElem.getGenericSignatureName(iter.next());
            }
            
            while(iter.hasNext()){
                implement += ", " + JavaElem.getGenericSignatureName(iter.next());
                
            }
            System.out.println("[check] super interfaces: \"" + implement +"\"");
            
            if(!editorOperator.contains("implements " + implement)){
                appendMessage("super interfaces: \"" + implement + "\" not found!");
            }
            
        }
        
    }
    
    
    protected void verifyAttribute(IAttributeElem attributeElem) {
        
        System.out.println("[attribute]: \"" + attributeElem);
        
        String attr = "";
        attr += attributeElem.getVisibility() + " ";
        
        for(ModifierType modifier: attributeElem.getModifierList()){
            attr+= modifier + " ";
        }
        
        
        
        attr += JavaElem.getGenericSignatureName(attributeElem.getType()) + " ";
        
        
        attr += attributeElem.getName();
        
        String defaulValue = attributeElem.getDefaultValue();
        
        if(!"".equals(defaulValue)){
            attr += " = " + defaulValue;
        }
        
        attr += ";";
        
        System.out.println("[attr     ]: \"" + attr);
        
        if(!editorOperator.contains(attr)){
            appendMessage("Attribute: \"" + attr + "\" not found!");
        }
        
        
    }
    
    protected void verifyOperation(IOperationElem operationElem) {
        
        System.out.println("[operation]: \"" + operationElem);
        String signature = operationElem.getSignature();
        
        if(!editorOperator.contains(signature)){
            appendMessage("Operation: \"" + signature + "\" not found!");
        }
        
        
    }

    protected void verifyNestedElem(IJavaElem javaElem) {
        
        String nestedElemSignature = javaElem.getSignature();
        
        if(!editorOperator.contains(nestedElemSignature)){
            appendMessage("Nested Elem: \"" + nestedElemSignature + "\" not found!");
        }
        
    }
    
    
}
