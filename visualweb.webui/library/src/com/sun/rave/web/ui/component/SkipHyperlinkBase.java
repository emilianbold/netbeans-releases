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
 * Use the <code>ui:skipLink</code>
 * tag to render a single-pixel transparent image (not visible within the browser page) which is hyperlinked to an anchor beyond the section to skip. This tag is used to achieve 508-compliance (paragraph o). It is designed to be used by components such as masthead, tabs, calendar and other components with repetitive links.
 * <br>
 * <h3>HTML Elements and Layout</h3>
 * <p>The rendered HTML page displays an image hyperlink at the top, followed by an anchor at the end of the region to skip.
 * </p>
 * <h3>Theme Identifiers</h3>
 * None.
 * <h3>Client-side JavaScript functions</h3>
 * None.
 * <h3>Examples</h3>
 * <b>Example 1: An example showing how to skip over the masthead:</b><br>
 * <code>
 *   &lt;ui:skipHyperlink id="skip1234" description="skip over the masthead" &gt;
 * 	&lt;ui:masthead id=masthead1" productImageURL="../images/webconsole.png"
 *                       productImageDescription="Java Web Console" 
 *                       userInfo="test_user" serverInfo="test_server" /&gt;
 *   &lt;/ui: skipHyperlink&gt;
 * </code>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class SkipHyperlinkBase extends javax.faces.component.UICommand {

    /**
     * <p>Construct a new <code>SkipHyperlinkBase</code>.</p>
     */
    public SkipHyperlinkBase() {
        super();
        setRendererType("com.sun.rave.web.ui.SkipHyperlink");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.SkipHyperlink";
    }

    // description
    private String description = null;

    /**
 * <p>Textual description of the purpose of this skip hyperlink, including a description of the section that is being skipped.</p>
     */
    public String getDescription() {
        if (this.description != null) {
            return this.description;
        }
        ValueBinding _vb = getValueBinding("description");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Textual description of the purpose of this skip hyperlink, including a description of the section that is being skipped.</p>
     * @see #getDescription()
     */
    public void setDescription(String description) {
        this.description = description;
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

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.description = (String) _values[1];
        this.style = (String) _values[2];
        this.styleClass = (String) _values[3];
        this.tabIndex = ((Integer) _values[4]).intValue();
        this.tabIndex_set = ((Boolean) _values[5]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[6];
        _values[0] = super.saveState(_context);
        _values[1] = this.description;
        _values[2] = this.style;
        _values[3] = this.styleClass;
        _values[4] = new Integer(this.tabIndex);
        _values[5] = this.tabIndex_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
