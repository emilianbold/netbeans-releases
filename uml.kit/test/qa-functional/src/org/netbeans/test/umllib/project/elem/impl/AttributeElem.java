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

import java.util.LinkedList;
import java.util.List;
import org.netbeans.test.umllib.project.elem.IAttributeElem;
import org.netbeans.test.umllib.project.elem.IJavaElem;
import org.netbeans.test.umllib.project.elem.ModifierType;
import org.netbeans.test.umllib.project.elem.VisibilityType;

/**
 *
 * @author andromeda
 */
public class AttributeElem implements IAttributeElem {
    
    IJavaElem type;
    VisibilityType visibility;
    String name;
    String defaultValue;
    
    List<ModifierType> modifierList = new LinkedList<ModifierType>();
    
    
    /** Creates a new instance of OperationElem */
    public AttributeElem(String name, IJavaElem type) {
        this(name, type, VisibilityType.PRIVATE);
    }
    
    public AttributeElem(String name, IJavaElem type, VisibilityType visibility) {
        this(name, type, visibility, new ModifierType[]{}, "");
    }
    public AttributeElem(String name, IJavaElem type, VisibilityType visibility, String defaultValue) {
        this(name, type, visibility,new ModifierType[]{}, defaultValue);
    }
    
    public AttributeElem(String name, IJavaElem type, VisibilityType visibility, ModifierType[] modifier, String defaultValue) {
        this.name = name;
        this.type = type;
        this.visibility = visibility;
        this.defaultValue = defaultValue;
        
        for(ModifierType mdf: modifier){
	    modifierList.add(mdf);
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
        String str = "";
        str += getVisibility() + " ";
        
        for(ModifierType modifier: getModifierList()){
            str += modifier + " ";
        }
        
        str += getType().getFullName() +" " + getName() ;
        return str;
        
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public List<ModifierType> getModifierList() {
        return modifierList;
    }
    
    
    
    
}
