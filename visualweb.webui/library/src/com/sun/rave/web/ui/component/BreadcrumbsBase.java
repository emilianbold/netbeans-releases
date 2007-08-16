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
 * Render a breadcrumb or parentage path. <br>
 * <h3>HTML Elements and Layout</h3>
 * This tag will render a breadcrumb (or parentage path) on a page. A breadcrumb or parentage path is a set of hyperlinks, displayed on a page to show the user's location within an application, and the physical or logical path to a page. The breadcrumbs can be used to navigate to other locations within the application.
 * <h3>Client Side Javascript Functions</h3>
 * none.
 * <h3>Examples</h3>
 * The pages which comprise breadcrumbs can be specified as child components of the breadcrumbs or by using the <code>pages</code> attribute. If the <code>pages</code> attribute is used, the value must an EL expression that identifies an array of <code>com.sun.rave.web.ui.Hyperlink</code>.<br>
 * Breadcrumbs must be used within a <code>&lt;ui:form&gt;</code> tag.
 * <h4>Example 1:</h4>
 * Using child components:<br>
 * <code>
 * <pre>
 *       &lt;ui:breadcrumbs id="breadcrumbs1"&gt;
 *         &lt;ui:hyperlink url="http://google.com" label="Google"/&gt;
 *         &lt;ui:hyperlink url="http://yahoo.com" label="Yahoo"/&gt;
 *         &lt;ui:hyperlink url="http://sun.com" label="Sun"/&gt;
 *       &lt;/ui:breadcrumbs&gt;
 * </pre>
 * </code>
 * <h4>Example 2:</h4>
 * Using a value binding:<br>
 * <code>
 * <pre>
 *       &lt;ui:breadcrumbs id="breadcrumbs2" pages="#{FieldBean.pagelist}" /&gt;
 * </pre>
 * </code>
 * <br>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class BreadcrumbsBase extends javax.faces.component.UICommand {

    /**
     * <p>Construct a new <code>BreadcrumbsBase</code>.</p>
     */
    public BreadcrumbsBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Breadcrumbs");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Breadcrumbs";
    }

    // pages
    private com.sun.rave.web.ui.component.Hyperlink[] pages = null;

    /**
 * <p>Value binding expression that points to an array of UIComponents containing
 *          the information for the pages in the breadcrumbs.</p>
     */
    public com.sun.rave.web.ui.component.Hyperlink[] getPages() {
        if (this.pages != null) {
            return this.pages;
        }
        ValueBinding _vb = getValueBinding("pages");
        if (_vb != null) {
            return (com.sun.rave.web.ui.component.Hyperlink[]) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Value binding expression that points to an array of UIComponents containing
 *          the information for the pages in the breadcrumbs.</p>
     * @see #getPages()
     */
    public void setPages(com.sun.rave.web.ui.component.Hyperlink[] pages) {
        this.pages = pages;
    }

    // style
    private String style = null;

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueBinding _vb = getValueBinding("style");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     * @see #getStyle()
     */
    public void setStyle(String style) {
        this.style = style;
    }

    // styleClass
    private String styleClass = null;

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueBinding _vb = getValueBinding("styleClass");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     * @see #getStyleClass()
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    // tabIndex
    private int tabIndex = Integer.MIN_VALUE;
    private boolean tabIndex_set = false;

    /**
 * <p>The position of this component in the tabbing order sequence</p>
     */
    public int getTabIndex() {
        if (this.tabIndex_set) {
            return this.tabIndex;
        }
        ValueBinding _vb = getValueBinding("tabIndex");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
 * <p>The position of this component in the tabbing order sequence</p>
     * @see #getTabIndex()
     */
    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
        this.tabIndex_set = true;
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
        this.pages = (com.sun.rave.web.ui.component.Hyperlink[]) _values[1];
        this.style = (String) _values[2];
        this.styleClass = (String) _values[3];
        this.tabIndex = ((Integer) _values[4]).intValue();
        this.tabIndex_set = ((Boolean) _values[5]).booleanValue();
        this.visible = ((Boolean) _values[6]).booleanValue();
        this.visible_set = ((Boolean) _values[7]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[8];
        _values[0] = super.saveState(_context);
        _values[1] = this.pages;
        _values[2] = this.style;
        _values[3] = this.styleClass;
        _values[4] = new Integer(this.tabIndex);
        _values[5] = this.tabIndex_set ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
