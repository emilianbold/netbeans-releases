/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
