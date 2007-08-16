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
 * <p> Use the <code>ui:panelGroup</code> tag to display a group of components
 * 	that are separated by a common separator.  This tag is often useful for
 * 	providing a consistently formatted layout of a group of components.</p>
 *     <h3>HTML Elements and Layout</h3>
 *     <p>	By default the PanelGroup component is rendered with a
 * 	<code><span></code> element surrounding the group of child
 * 	components.  You can change the rendered element to a
 * 	<code><div></code> by setting the <code>block</code> attribute to
 * 	<code>true</code>.  When a <code><div></code> is used, the
 * 	panel group is displayed on a new line.  Note that if you use a
 * 	component in the <code>ui:panelGroup</code> tag that renders a block
 * 	element such as a <code><p></code>, that component is always
 * 	displayed on its own line.  The behavior of child block elements is
 * 	independent of the setting of the block attribute.</p>
 * 
 *     <p> PanelGroup is a NamingContainer.</p>
 * 
 *     <h3>Theme Identifiers</h3>
 * 
 *     <p> Not Applicable </p>
 * 
 *     <h3>Client Side Javascript Functions</h3>
 * 
 *     <p> Not Applicable </p>
 * 
 *     <h3>Examples:</h3>
 * 
 *     <h4>Example 1: Buttons in a panelGroup that uses default separator</h4>
 * 
 *     <p>	The default separator is a return character.</p>
 * 
 *     <code>
 * 	<ui:panelGroup id="myPanelGroup1"><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button1" text="Button 1" /><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button2" text="Button 2" /><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button3" text="Button 3" /><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button4" text="Button 4" /><br />
 * 	</ui:panelGroup>
 *     </code>
 * 
 *     <h4>Example 2: Specifying a separator with the <code>separator</code>
 * 	attribute</h4>
 * 
 *     <p>	This example uses the separator attribute to specify a separator
 * 	consisting of a pipe character surrounded by spaces.</p>
 * 
 *     <code>
 * 	<ui:panelGroup id="myPanelGroup2" separator=" | "><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button1" text="Button 1" /><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button2" text="Button 2" /><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button3" text="Button 3" /><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button4" text="Button 4" /><br />
 * 	</ui:panelGroup>
 *     </code>
 * 
 *     <h4>Example 3: Specifying a separator with the <code>separator</code>
 * 	facet</h4>
 * 
 *     <p>	This example shows how to use the <code>separator</code> facet and
 * 	the <code>block</code> attribute.</p>
 * 
 *     <code>
 * 	<ui:panelGroup id="myPanelGroup3" block="true"><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <facet name="separator"><br />
 * 		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * 		<h:outputText value="==" /><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    </f:facet><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button1" text="Button 1" /><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button2" text="Button 2" /><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button3" text="Button 3" /><br />
 * 	    &nbsp;&nbsp;&nbsp;&nbsp;
 * 	    <ui:button id="button4" text="Button 4" /><br />
 * 	</ui:panelGroup>
 *     </code>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class PanelGroupBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>PanelGroupBase</code>.</p>
     */
    public PanelGroupBase() {
        super();
        setRendererType("com.sun.rave.web.ui.PanelGroup");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.PanelGroup";
    }

    // block
    private boolean block = false;
    private boolean block_set = false;

    /**
 * <p>By default, the panelGroup component is rendered on the same line as the component
 *         that comes before it and the component that follows, in a flow layout.  If the block attribute
 *         is set to true, the panelGroup component is rendered on its own line. The components
 *         before it and after it are on different lines. The block attribute has no effect on the 
 *         panelGroup component's children.</p>
     */
    public boolean isBlock() {
        if (this.block_set) {
            return this.block;
        }
        ValueBinding _vb = getValueBinding("block");
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
 * <p>By default, the panelGroup component is rendered on the same line as the component
 *         that comes before it and the component that follows, in a flow layout.  If the block attribute
 *         is set to true, the panelGroup component is rendered on its own line. The components
 *         before it and after it are on different lines. The block attribute has no effect on the 
 *         panelGroup component's children.</p>
     * @see #isBlock()
     */
    public void setBlock(boolean block) {
        this.block = block;
        this.block_set = true;
    }

    // separator
    private String separator = null;

    /**
 * <p>The String of characters that should be inserted between each UIComponent that is 
 *       a child of this component. If this attribute is not specified, then a newline will be
 *       inserted between each component.</p>
     */
    public String getSeparator() {
        if (this.separator != null) {
            return this.separator;
        }
        ValueBinding _vb = getValueBinding("separator");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The String of characters that should be inserted between each UIComponent that is 
 *       a child of this component. If this attribute is not specified, then a newline will be
 *       inserted between each component.</p>
     * @see #getSeparator()
     */
    public void setSeparator(String separator) {
        this.separator = separator;
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
        this.block = ((Boolean) _values[1]).booleanValue();
        this.block_set = ((Boolean) _values[2]).booleanValue();
        this.separator = (String) _values[3];
        this.style = (String) _values[4];
        this.styleClass = (String) _values[5];
        this.visible = ((Boolean) _values[6]).booleanValue();
        this.visible_set = ((Boolean) _values[7]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[8];
        _values[0] = super.saveState(_context);
        _values[1] = this.block ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.block_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.separator;
        _values[4] = this.style;
        _values[5] = this.styleClass;
        _values[6] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
