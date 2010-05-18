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
package com.sun.jsfcl.std.property;

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;
import java.beans.PropertyEditorSupport;

/**
 * For editing the NumberConverter property - type. The possible type can be number, currency, percent
 *
 * @author cao
 * @deprecated
 */
public class NumberConverterTypePropertyEditor extends PropertyEditorSupport implements PropertyEditor2 {
    
    private String[] types = new String[] {"number", "currency", "percent"};
    private DesignProperty designProperty;
    private int selected = 0;
    
    /** Creates a new instance of NumberConverterTypePropertyEditor */
    public NumberConverterTypePropertyEditor() {
    }
    
    /**
     * Implementation of design time API - PropertyEditor2
     */
    public void setDesignProperty( DesignProperty designProperty ) {
        this.designProperty = designProperty;
    }
    
   /** 
    * Allows the bean builder to tell the property editor the value
    * that the user has entered/selected
    */
    public void setAsText(String text) throws IllegalArgumentException {
        
        // If the passed in type is invalid, then default back to what was before
        for( selected = 0; selected < types.length && !types[selected].equals(text); selected++);
        
        if (selected == types.length)
            selected = 0;
        
        super.setValue( types[selected] );
    }
    
    /** 
     * Tells the bean builder current selected type
     */
    public String getAsText() {
        return types[selected];
    }
    
    /** 
     * Allows the bean builder to pass the current property
     * value to the property editor
     */
    public void setValue(Object value) {
        selected = 0;
        if( value != null ) {
            for( int i=0;  i < types.length; i++ ) {
                if (value.equals(types[i])) {
                    selected = i;
                    break;
                }
            }
        }
        
        super.setValue( value );
    }
    
    /** 
     * Tells the bean builder the value for the property 
     */
    public Object getValue() {
        return types[selected];
    }
    
    /**
     * The possible values for this property
     */
    public String[] getTags() {
        return types;
    }
    
    /** 
     * Get the initialization code for the property 
     */
    public String getJavaInitializationString() {
	return "\"" + types[selected] + "\"";
    }
}
