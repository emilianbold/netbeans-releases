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

package com.sun.rave.faces.component;


import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;


/**
 * <p>JavaServer Faces component that enables an application to dynamically
 * adjust the character encoding of a response, based on the current view's
 * <code>Locale</code> setting.
 */

public class EncodingComponent extends UIComponentBase {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Create a new {@link EncodingComponent} with default properties.</p>
     */
    public EncodingComponent() {

        super();
        setRendererType("com.sun.rave.faces.Encoding");                    //NOI18N

    }


    // ------------------------------------------------------ Instance Variables


    private String value = null;


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the component family to which this component belongs.</p>
     */
    public String getFamily() {

        return ("com.sun.rave.faces.Encoding");                            //NOI18N

    }


    /**
     * <p>Return the character encoding value to be used for this response.</p>
     */
    public String getValue() {

        ValueBinding vb = getValueBinding("value");                   //NOI18N
        if (vb != null) {
            return (String) vb.getValue(getFacesContext());
        } else {
            return this.value;
        }

    }


    /**
     * <p>Set the character encoding value to use for this response.</p>
     *
     * @param value New character encoding value
     */
    public void setValue(String value) {

        this.value = value;

    }


    // ---------------------------------------------------------- Public Methods


    // ----------------------------------------------------- UIComponent Methods


    // ---------------------------------------------------- StateManager Methods


    /**
     * <p>Restore the state of this component from the specified object.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param state State object from which to restore our state
     */
    public void restoreState(FacesContext context, Object state) {

        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        value = (String) values[1];

    }


    /**
     * <p>Return an object representing the saved state of this component.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    public Object saveState(FacesContext context) {

        Object values[] = new Object[2];
        values[0] = super.saveState(context);
        values[1] = value;
	return values;

    }


}
