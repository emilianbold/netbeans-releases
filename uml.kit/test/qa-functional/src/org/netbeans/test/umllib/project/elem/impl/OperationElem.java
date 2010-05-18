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
 * OperationElem.java
 *
 * Created on January 23, 2007, 5:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.project.elem.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.test.umllib.project.elem.IArgumentElem;
import org.netbeans.test.umllib.project.elem.IJavaElem;
import org.netbeans.test.umllib.project.elem.IOperationElem;
import org.netbeans.test.umllib.project.elem.ModifierType;
import org.netbeans.test.umllib.project.elem.VisibilityType;

/**
 *
 * @author andromeda
 */
public class OperationElem implements IOperationElem {
    
    IJavaElem type;
    VisibilityType visibility;
    String name;
    
    List<ModifierType> modifierList = new LinkedList<ModifierType>();
    List<IArgumentElem> argumentList = new LinkedList<IArgumentElem>();
    List<IJavaElem>  exceptionList  = new LinkedList<IJavaElem>();
    
    public OperationElem(String name){
        this(name, PrimitiveType.VOID);
    }
    
    
    /** Creates a new instance of OperationElem */
    public OperationElem(String name, IJavaElem type) {
        this(name, type, VisibilityType.PACKAGE );
    }
    
    public OperationElem(String name, IJavaElem type, VisibilityType visibility) {
        this(name, type, visibility, new IArgumentElem[]{});
    }
    public OperationElem(String name, IJavaElem type, VisibilityType visibility, IArgumentElem[] argumentElem) {
        this(name, new ModifierType[]{}, type, visibility, argumentElem);
    }
    
    public OperationElem(String name, ModifierType[] modifier, IJavaElem type, VisibilityType visibility, IArgumentElem[] argumentElem) {
        this.name = name;
        this.type = type;
        this.visibility = visibility;
        
        for(ModifierType mdf: modifier){
            modifierList.add(mdf);
        }
        for(IArgumentElem argElem: argumentElem){
            argumentList.add(argElem);
        }
    }
    
    
    public String getName() {
        return name;
    }
    
    public VisibilityType getVisibility() {
        return visibility;
    }
    
    public IJavaElem getType() {
        return type;
    }
    
    
    
    public String toString() {
        return getSignature();
    }
    
    public List<IArgumentElem> getArgumentList() {
        return argumentList;
    }
    
    public List<ModifierType> getModifierList() {
        return modifierList;
    }
    
    public List<IJavaElem> getExceptionList() {
        return exceptionList;
    }
    
    public String getSignature() {
     
        String operation = "";
        operation += getVisibility() + " ";
        
        operation += JavaElem.getGenericSignatureName(getType()) + " ";
        
        operation += getName();
        
        operation +=" (";
        
        List<IArgumentElem> argumentList = getArgumentList();
        
        if(argumentList.size() != 0){
            IArgumentElem argumentElem =  argumentList.get(0);
            
            operation += JavaElem.getGenericSignatureName(argumentElem.getType()) + " " + argumentElem.getName();
            
            for(int i=1; i < argumentList.size(); i++){
                argumentElem =  argumentList.get(1);
                operation += ", " + argumentElem.getType().getName() + " " + argumentElem.getName();
            }
            
        }
        operation +=")";
                
                
        Iterator<IJavaElem> exceptionIter = exceptionList.iterator();
        
        if(exceptionIter.hasNext()){
            operation += " throws " + exceptionIter.next().getFullName();
        }
        
        while(exceptionIter.hasNext()){
            operation += ", " +  exceptionIter.next().getFullName();
        }
        
        return operation;
        
    }
    
    
    
}
