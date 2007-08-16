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
package com.sun.rave.web.ui.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * <h3>NOT FOR DEVELOPER USE - base renderer class for ui:calendar and ui:scheduler</h3>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class CalendarMonthBase extends javax.faces.component.UIOutput {

    /**
     * <p>Construct a new <code>CalendarMonthBase</code>.</p>
     */
    public CalendarMonthBase() {
        super();
        setRendererType("com.sun.rave.web.ui.CalendarMonth");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.CalendarMonth";
    }

    // popup
    private boolean popup = false;

    /**
 * <p>Flag determining whether the component should be rendered in its
 *       popup version (as used by Calendar), or in the
 *       inline version used by Scheduler.</p>
     */
    public boolean isPopup() {
        return this.popup;
    }

    /**
 * <p>Flag determining whether the component should be rendered in its
 *       popup version (as used by Calendar), or in the
 *       inline version used by Scheduler.</p>
     * @see #isPopup()
     */
    public void setPopup(boolean popup) {
        this.popup = popup;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.popup = ((Boolean) _values[1]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[2];
        _values[0] = super.saveState(_context);
        _values[1] = this.popup ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
