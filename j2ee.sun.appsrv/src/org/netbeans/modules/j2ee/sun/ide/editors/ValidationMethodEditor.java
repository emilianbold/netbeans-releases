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

import java.awt.Component;
import java.beans.PropertyEditorSupport;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

public class ValidationMethodEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
    static String VALIDATION_TYPE = "validation"; //NOI18N
    static String TRANX_SUPPORT_TYPE = "TransactionSupport"; //NOI18N
    
    public static String editorType = VALIDATION_TYPE;
    public static String curr_Sel;
    public String[] choices = {
                 "auto-commit", // NOI18N
                 "meta-data", // NOI18N
                 "table" // NOI18N
                 };
    
    public String[] choicesTranx = {
                 "", // NOI18N
                 "XATransaction", // NOI18N
                 "LocalTransaction", // NOI18N
                 "NoTransaction"
                 };

   public ValidationMethodEditor() {
	curr_Sel = null;
   }
   
   public ValidationMethodEditor(String type) {
	curr_Sel = null;
        editorType = type;
   }

    public String getAsText () {
	return curr_Sel;
    }

    public void setAsText (String string) throws IllegalArgumentException {
        if (! editorType.equals(TRANX_SUPPORT_TYPE)) {
            if ((string == null) || (string.equals(""))) { // NOI18N
                throw new IllegalArgumentException();
            } else {
                curr_Sel = string;
            }
        } else {
            curr_Sel = string;
        }
        this.firePropertyChange();
    }

   public void setValue (Object val) {
       if (! (val instanceof String)) {
      	    throw new IllegalArgumentException ();
	}
        curr_Sel = (String) val;
    }

    public Object getValue () {
        return curr_Sel;
    }

    public String getJavaInitializationString () {
	return getAsText ();
    }

    public String[] getTags () {
        if(editorType.equals(TRANX_SUPPORT_TYPE)){
           return choicesTranx; 
        } else {
           return choices;
        }    
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


