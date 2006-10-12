/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.editors;

import java.awt.*;
import java.beans.*;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.*;

public class Int0Editor extends PropertyEditorSupport implements EnhancedPropertyEditor {

    public String prev = "null"; // NOI18N
    private String curValue;
    private String errorMessage;

    public Int0Editor() {
	curValue = null;
	
    }

    public String getAsText () {
        if (curValue==null || curValue.equals("")) {// NOI18N
            curValue = prev;
        }
        if (errorMessage != null) {
           // String title = NbBundle.getMessage(Int0Editor.class, "TTL_Input_Error");
            errorMessage = null;
        }
        return curValue;
    }

    public String checkValid(String string) {
        if (EditorUtils.isValidInt0(string))
            return null;  //no error message
        else 
            return NbBundle.getMessage(Int0Editor.class, "MSG_RangeForInt0");      
    }
    
    public void setAsText (String string) throws IllegalArgumentException {
        prev = curValue;
        if((string==null)||(string.equals(""))) {// NOI18N
           return;
        }

        errorMessage = checkValid(string);
        if (errorMessage == null) {
            prev = curValue;
            curValue = string;
            firePropertyChange();
        }
        else 
            curValue = prev;
    }
    
    public void setValue (Object v) {
        if(!(v.equals(""))){ // NOI18N
           prev = (String)v;
        }   
        curValue = (String)v;
    
    }

    public Object getValue () {
       return curValue;
    }

    public Component getInPlaceCustomEditor () {
        return null;
    }

    public boolean hasInPlaceCustomEditor () {
        return false;
    }

    public boolean supportsEditingTaggedValues () {
        return false;
    }
                   
}


  
      
  
