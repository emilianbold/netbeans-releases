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
 * Base component for UI components that allow the user to make a
 *     selection from a list of options using an HTML select element.
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class ListSelectorBase extends com.sun.rave.web.ui.component.Selector {

    /**
     * <p>Construct a new <code>ListSelectorBase</code>.</p>
     */
    public ListSelectorBase() {
        super();
        setRendererType("com.sun.rave.web.ui.ListSelectorRenderer");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.ListSelector";
    }

    // labelOnTop
    private boolean labelOnTop = false;
    private boolean labelOnTop_set = false;

    /**
 * <p>If this attribute is true, the label is rendered above the
 *       component. If it is false, the label is rendered next to the
 *       component. The default is false.</p>
     */
    public boolean isLabelOnTop() {
        if (this.labelOnTop_set) {
            return this.labelOnTop;
        }
        ValueBinding _vb = getValueBinding("labelOnTop");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>If this attribute is true, the label is rendered above the
 *       component. If it is false, the label is rendered next to the
 *       component. The default is false.</p>
     * @see #isLabelOnTop()
     */
    public void setLabelOnTop(boolean labelOnTop) {
        this.labelOnTop = labelOnTop;
        this.labelOnTop_set = true;
    }

    // rows
    private int rows = Integer.MIN_VALUE;
    private boolean rows_set = false;

    /**
 * <p>The number of items to display. The default value is 12.</p>
     */
    public int getRows() {
        if (this.rows_set) {
            return this.rows;
        }
        ValueBinding _vb = getValueBinding("rows");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return 12;
    }

    /**
 * <p>The number of items to display. The default value is 12.</p>
     * @see #getRows()
     */
    public void setRows(int rows) {
        this.rows = rows;
        this.rows_set = true;
    }

    // separators
    private boolean separators = false;
    private boolean separators_set = false;

    /**
 * <p>Flag indicating that items corresponding to 
 *       <code>com.sun.rave.web.ui.model.Option</code> that are defined 
 *       inside a <code>com.sun.rave.web.ui.model.OptionGroup</code> should be
 *       surrounded by separators inside the list. The default value is
 *       true. If false, no separators are shown. To manually specify the
 *       location of separators, set this flag to false and place
 *       instances of <code>com.sun.rave.web.ui.model.Separator</code> between
 *       the relevant <code>com.sun.rave.web.ui.model.Option</code> instances
 *       when specifying the <code>items</code> attribute.</p>
     */
    public boolean isSeparators() {
        if (this.separators_set) {
            return this.separators;
        }
        ValueBinding _vb = getValueBinding("separators");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return true;
    }

    /**
 * <p>Flag indicating that items corresponding to 
 *       <code>com.sun.rave.web.ui.model.Option</code> that are defined 
 *       inside a <code>com.sun.rave.web.ui.model.OptionGroup</code> should be
 *       surrounded by separators inside the list. The default value is
 *       true. If false, no separators are shown. To manually specify the
 *       location of separators, set this flag to false and place
 *       instances of <code>com.sun.rave.web.ui.model.Separator</code> between
 *       the relevant <code>com.sun.rave.web.ui.model.Option</code> instances
 *       when specifying the <code>items</code> attribute.</p>
     * @see #isSeparators()
     */
    public void setSeparators(boolean separators) {
        this.separators = separators;
        this.separators_set = true;
    }

    // visible
    private boolean visible = false;
    private boolean visible_set = false;

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     */
    public boolean isVisible() {
        if (this.visible_set) {
            return this.visible;
        }
        ValueBinding _vb = getValueBinding("visible");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return true;
    }

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     * @see #isVisible()
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        this.visible_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.labelOnTop = ((Boolean) _values[1]).booleanValue();
        this.labelOnTop_set = ((Boolean) _values[2]).booleanValue();
        this.rows = ((Integer) _values[3]).intValue();
        this.rows_set = ((Boolean) _values[4]).booleanValue();
        this.separators = ((Boolean) _values[5]).booleanValue();
        this.separators_set = ((Boolean) _values[6]).booleanValue();
        this.visible = ((Boolean) _values[7]).booleanValue();
        this.visible_set = ((Boolean) _values[8]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[9];
        _values[0] = super.saveState(_context);
        _values[1] = this.labelOnTop ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.labelOnTop_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = new Integer(this.rows);
        _values[4] = this.rows_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.separators ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.separators_set ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[8] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
