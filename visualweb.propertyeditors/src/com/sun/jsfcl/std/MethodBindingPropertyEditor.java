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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.jsfcl.std;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import javax.faces.application.Application;
import javax.faces.el.MethodBinding;

import org.openide.explorer.propertysheet.PropertyEnv;

import com.sun.faces.util.ConstantMethodBinding;
import com.sun.jsfcl.util.ComponentBundle;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;
import com.sun.rave.designtime.faces.FacesDesignContext;

/**
 * @deprecated
 */

public class MethodBindingPropertyEditor extends StringEditorWithNoCr implements PropertyEditor2 {

    protected static final ComponentBundle bundle = ComponentBundle.getBundle(
            MethodBindingPropertyEditor.class);
    //------------------------------------------------------------------------------- PropertyEditor

    /* (non-Javadoc)
     * @see java.beans.PropertyEditor#getValue()
     */
    public Object getValue() {
        Object value = super.getValue();
        //System.err.println("MBPE.getValue value:" + value);
        return value;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyEditor#setValue(java.lang.Object)
     */
    public void setValue(Object object) {
        superSetValue(object);
    }

    public void setAsText(String text) throws IllegalArgumentException {
        setValue(getCustomEditorValue(text));
    }

    public Object getCustomEditorValue(String string) {
        if (string.startsWith("#{")) { //NOI18N
            FacesDesignContext fctx = (FacesDesignContext)liveProperty.getDesignBean().getDesignContext();
            Application app = fctx.getFacesContext().getApplication();
            return app.createMethodBinding(string, new Class[] {});
        } else if (string.length() > 0) {
            return new ConstantMethodBinding(string);
        } else {
            return null;
        }
    }
    
    public void attachEnv(PropertyEnv env) {
        super.attachEnv(env);
//        instructions = bundle.getMessage("TXT_methodBindingInstructions"); // NOI18N
    }

        public String getAsText() {
        Object value = getValue();
        if (value instanceof MethodBinding) {
            MethodBinding mb = (MethodBinding)value;
            //System.err.println("MBPE.getAsText mb:" + mb.getExpressionString());
            if (mb instanceof ConstantMethodBinding) {
                return (String)mb.invoke(null, null); // Hack to get the outcome back out
            } else {
                return mb.getExpressionString();
            }
        }
        //System.err.println("MBPE.getAsText not mb:" + ((value == null) ? "null" : value.toString()));
        return (value == null) ? "" : value.toString(); //NOI18N
    }

    protected boolean forceOneline() {
        // Not working at moment, the UI does not size properly :(
        return false;
    }
    
    public String getJavaInitializationString() {
        return "\"" + getAsText() + "\""; //NOI18N
    }

    private DesignProperty liveProperty;

    public void setDesignProperty(DesignProperty liveProperty) {
        this.liveProperty = liveProperty;
    }

    protected boolean useOriginalShortDescriptionForInstructions() {
        return true;
    }

}
