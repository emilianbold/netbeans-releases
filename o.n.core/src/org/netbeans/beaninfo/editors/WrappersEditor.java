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

package org.netbeans.beaninfo.editors;

import java.beans.*;
import java.text.MessageFormat;
import org.netbeans.core.UIExceptions;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * Abstract class represents Editor for Wrappers of 8 known primitive types
 * (Byte, Short, Integer, Long, Boolean, Float, Double, Character)
 *
 * @author  Josef Kozak
 */
public abstract class WrappersEditor implements ExPropertyEditor {

    protected PropertyEditor pe = null;
    
    public WrappersEditor(Class type) {
        super();
        pe = PropertyEditorManager.findEditor(type);
    }
    
    public void setValue(Object newValue) throws IllegalArgumentException {
        pe.setValue(newValue);
    }
    
    public Object getValue() {
	return pe.getValue();
    }        
    
    public boolean isPaintable() {
	return pe.isPaintable();
    }

    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
        pe.paintValue(gfx, box);
    }        
    
    public String getAsText () {
        if ( pe.getValue() == null )
            return "null";              // NOI18N
        return pe.getAsText();
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if ( "null".equals( text ) )    // NOI18N
            return;
        try {
            pe.setAsText(text);
        } catch (Exception e) {
            //Reasonable to assume any exceptions from core/jdk editors are legit
            IllegalArgumentException iae = new IllegalArgumentException (e.getMessage());
            String msg = e.getLocalizedMessage();
            if (msg == null || e.getMessage().equals(msg)) {
                msg = MessageFormat.format(
                NbBundle.getMessage(
                    WrappersEditor.class, "FMT_EXC_GENERIC_BAD_VALUE"), text); //NOI18N
            }
            UIExceptions.annotateUser(iae, iae.getMessage(), msg, e,
                                     new java.util.Date());
            throw iae;
        }
    }
    
    public String[] getTags() {
	return pe.getTags();
    }
    
    public java.awt.Component getCustomEditor() {
	return pe.getCustomEditor();
    }

    public boolean supportsCustomEditor() {
	return pe.supportsCustomEditor();
    }
  
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        pe.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pe.removePropertyChangeListener(listener);
    }    
    
    public void attachEnv(PropertyEnv env) {
        //Delegate if the primitive editor is an ExPropertyEditor -
        //boolean and int editors will be
        if (pe instanceof ExPropertyEditor) {
            ((ExPropertyEditor) pe).attachEnv (env);
        }
    }
}
