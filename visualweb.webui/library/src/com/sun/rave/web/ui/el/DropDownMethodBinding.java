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
package com.sun.rave.web.ui.el;

import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;

/**
 * <p>Implementation of <code>MethodBinding</code> that always returns
 * the value specified to our constructor.</p>
 */
public class DropDownMethodBinding extends MethodBinding
  implements StateHolder {

    /**
     * <p>Zero arguments constructor for restoring state.</p>
     */
    public DropDownMethodBinding() {
    }

    /**
     * <p>The value to be returned when this method binding is
     * evaluated.</p>
     */
    private transient String value = null;

    public void setValue(String value) {
        this.value = value;
    }


    /**
     * <p>Return the appropriate constant value.</p>
     *
     * @param context <code>FacesContext</code> for this request
     * @param params Method parameters to pass in
     */
    public Object invoke(FacesContext context, Object params[]) {
        return value;
    }


    /**
     * <p>Return the expected return type class.</p>
     *
     * @param context <code>FacesContext</code> for this request
     */
    public Class getType(FacesContext context) {
        return String.class;
    }


    /**
     * <p>Return the expression string for this method binding.</p>
     */
    public String getExpressionString() {
        return this.value;
    }

    private boolean transientFlag = false;


    public boolean isTransient() {
        return this.transientFlag;
    }


    public void setTransient(boolean transientFlag) {
        this.transientFlag = transientFlag;
    }


    public void restoreState(FacesContext context, Object state) {
        this.value = (String) state;
    }


    public Object saveState(FacesContext context) {
        return this.value;
    }

    public String toString() {
        return "DropDownMethodBinding with value " + String.valueOf(value);
    }
}
