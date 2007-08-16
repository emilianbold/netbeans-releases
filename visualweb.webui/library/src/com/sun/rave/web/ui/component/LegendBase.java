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
 * Use the <code>ui:legend</code>
 * tag to display legend, typically at the top of a page.
 * <br>
 * The legend tag has one facet:<br>
 * &nbsp;&nbsp;&nbsp;<code>legendImage</code> - this allows the developer to place their own image and/or text in the legend.<br>
 * <h3>HTML Elements and Layout</h3>
 * A legend consists of an icon/image followed by explanatory text describing the icon/image.
 * <h3>Client Side Javascript Functions</h3>
 * none.
 * <h3>Examples</h3>
 * <b>Example 1: An example showing the default (indicates required field) legend:</b> <br>
 * <code>
 * &lt;ui:legend id="legend1" /&gt;
 * </code>
 * <br><br>
 * <b>Example 2: An example showing a custom icon, text, styles:</b> <br>
 * <pre>
 * <code>&lt;ui:legend id="legend2" text="Critical Alarms" 
 * &nbsp;&nbsp;&nbsp;&nbsp;style="position: absolute; left:100px; color:red; font-weight:bold"/&gt;
 * &nbsp;&nbsp;&lt;f:facet name="legendImage"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;ui:image id="alarmimage" icon="ALARM_CRITICAL_SMALL" alt="Critical Alarm"/&gt;
 * &nbsp;&nbsp;&lt;/f:facet&gt;
 * &lt;/ui:legend&gt;
 * </code>
 * </pre>
 * <br>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class LegendBase extends javax.faces.component.UIOutput {

    /**
     * <p>Construct a new <code>LegendBase</code>.</p>
     */
    public LegendBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Legend");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Legend";
    }

    // position
    private String position = null;

    /**
 * <p>Specifies the position of the legend. Valid values are: "right" (the default) and "left".</p>
     */
    public String getPosition() {
        if (this.position != null) {
            return this.position;
        }
        ValueBinding _vb = getValueBinding("position");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Specifies the position of the legend. Valid values are: "right" (the default) and "left".</p>
     * @see #getPosition()
     */
    public void setPosition(String position) {
        this.position = position;
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

    // text
    private String text = null;

    /**
 * <p>The explanatory text that is displayed in the legend. If not specified, the required field legend text is displayed.</p>
     */
    public String getText() {
        if (this.text != null) {
            return this.text;
        }
        ValueBinding _vb = getValueBinding("text");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The explanatory text that is displayed in the legend. If not specified, the required field legend text is displayed.</p>
     * @see #getText()
     */
    public void setText(String text) {
        this.text = text;
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
        this.position = (String) _values[1];
        this.style = (String) _values[2];
        this.styleClass = (String) _values[3];
        this.text = (String) _values[4];
        this.visible = ((Boolean) _values[5]).booleanValue();
        this.visible_set = ((Boolean) _values[6]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[7];
        _values[0] = super.saveState(_context);
        _values[1] = this.position;
        _values[2] = this.style;
        _values[3] = this.styleClass;
        _values[4] = this.text;
        _values[5] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
