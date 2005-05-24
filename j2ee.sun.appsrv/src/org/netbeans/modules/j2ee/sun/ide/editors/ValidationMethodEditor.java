/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.editors;

import java.awt.Component;
import java.beans.PropertyEditorSupport;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

public class ValidationMethodEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {

    public static String curr_Sel;
    public String[] choices = { 
                 "auto-commit", // NOI18N
                 "meta-data", // NOI18N
                 "table" // NOI18N
                 };
 
   public ValidationMethodEditor() {
	curr_Sel = null;
   }

    public String getAsText () {
	return curr_Sel;
    }

    public void setAsText (String string) throws IllegalArgumentException {
       if((string==null)||(string.equals(""))) // NOI18N
           throw new IllegalArgumentException ();
        else
	    curr_Sel = string;
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
	return choices;
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


