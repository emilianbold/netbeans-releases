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
 * <p>Use the <code>ui:form</code> tag 
 * to create an HTML <code>&lt;form&gt;</code> element. The form can be submitted 
 * via a button or hyperlink control (in which case an <code>ActionEvent</code> 
 * will be generated on the server), or via client side scripting.</p> 
 * <p>The <code>virtualFormsConfig</code> attribute can be used to configure 
 * virtual forms. 
 *  A <i>virtual form</i> defines a group of input components 
 * (&quot;participants&quot;) and submission components (&quot;submitters&quot;) 
 * on a page, such that when the user interacts with one of the submitters, the participants 
 * are processed exclusively while the remaining inputs on the page are ignored.  
 *  An <i>input component</i> is any 
 *  component that implements <code>EditableValueHolder</code>. A 
 *  <i>submission component</i> 
 *  is any component that causes the web page to be submitted (such as a button, 
 *  hyperlink, or any input component that submits the page via the 
 *  <code>common_timeoutSubmitForm</code> scripting function). 
 * <i>Processing </i>an input means converting and validating it, firing 
 * any value change events associated with the input, and mapping the input onto 
 * its binding target (if the component is bound). Virtual forms provide an 
 * alternative to the <code>immediate</code> property. 
 * They are more powerful than <code>immediate</code> because they let you 
 * specify  multiple groups of inputs to be selectively processed (that is, you 
 * can specify  multiple virtual forms on a page). They are also easier to use 
 * than  <code>immediate</code> because they do not alter 
 * the JavaServer Faces lifecycle.</p>
 * <br> 
 * <h3>HTML Elements and Layout</h3> 
 * The rendered HTML page contains an 
 * HTML <code>&lt;form&gt;</code> tag and its associated 
 * attributes. The rendered 
 * form includes a hidden field for use in 
 * determining which form submitted the page.</p><br> 
 * <h3>Client Side Javascript Functions</h3> 
 * None. 
 * <br> 
 * <h3>Examples</h3> 
 * <h4>Example 1: Using a Form<br> 
 * </h4> 
 * <code>&lt;ui:page&gt;<br> 
 * &nbsp;&nbsp;&nbsp; &lt;ui:html&gt;<br> 
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;ui:head id="head" 
 * title="Hyperlink Test Page" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; 
 * &lt;ui:form id="form1"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; &lt;ui:hyperlink&nbsp;
 * id="hyperlinkSubmitsPage"&nbsp; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * label="#{HyperlinkBean.label}" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * action="#{HyperlinkBean.determineWhatToDoFunction}" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
 * &lt;/ui:form&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp; &lt;/ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:html&gt;<br>
 * &lt;/ui:page&gt;</code><br>
 * <h4>Example 2: A Page with Three Virtual Forms<br>
 * </h4>
 * <code>&lt;ui:page&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:html&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &lt;ui:head id="head" 
 * title="Shipping and Billing Information" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; 
 * &lt;ui:form id="form1" virtualFormsConfig="shipping | shippingAddressTextField 
 * | updateShippingButton , creditCard | creditCardDropDown | creditCardDropDown , 
 * billing | billingAddressTextfield creditCardDropDown | updateBillingButton"&gt; 
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; 
 * &lt;ui:label for="shippingAddressTextField" id="shippingAddressLabel" 
 * style="left: 48px; top: 48px; position: absolute" text="Shipping Address:"/&gt; 
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; 
 * &lt;ui:textField id="shippingAddressTextField" required="true" 
 * style="left: 48px; top: 72px; position: absolute"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; 
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; 
 * &lt;ui:button id="updateShippingButton" style="left: 48px; top: 120px; 
 * position: absolute" text="Update Shipping Address"/&gt;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; 
 * &lt;ui:label id="creditCardLabel" for="creditCardDropDown" 
 * style="left: 48px; top: 192px; position: absolute" 
 * text="Credit Card to Use:"/&gt;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; 
 * &lt;ui:dropDown id="creditCardDropDown" 
 * items="#{SessionBean1.creditCards}" style="left: 48px; top: 216px; 
 * position: absolute"/&gt;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; 
 * &lt;ui:label id="billingAddressLabel" for="billingAddressTextfield" 
 * style="left: 48px; top: 264px; position: absolute" 
 * text="Credit Card Billing Address:"/&gt;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; 
 * &lt;ui:textField id="billingAddressTextfield" required="true" 
 * style="left: 48px; top: 288px; position: absolute"/&gt;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; 
 * &lt;ui:button id="updateBillingButton" style="left: 48px; top: 336px; 
 * position: absolute" text="Update Billing Address"/&gt;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; 
 * &lt;ui:button id="updateAllButton" style="left: 48px; top: 432px; position: 
 * absolute" text="Update All Information"/&gt;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
 * &lt;/ui:form&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp; &lt;/ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:html&gt;<br>
 * &lt;/ui:page&gt;</code><br>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class FormBase extends javax.faces.component.UIForm {

    /**
     * <p>Construct a new <code>FormBase</code>.</p>
     */
    public FormBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Form");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Form";
    }

    // autoComplete
    private boolean autoComplete = false;
    private boolean autoComplete_set = false;

    /**
 * <p>Use this non-XHTML compliant boolean attribute to turn off autocompletion 
 *       feature of Internet Explorer and Firefox browsers. Set to "false" to
 *       turn off completion.  The default is "true".</p>
     */
    public boolean isAutoComplete() {
        if (this.autoComplete_set) {
            return this.autoComplete;
        }
        ValueBinding _vb = getValueBinding("autoComplete");
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
 * <p>Use this non-XHTML compliant boolean attribute to turn off autocompletion 
 *       feature of Internet Explorer and Firefox browsers. Set to "false" to
 *       turn off completion.  The default is "true".</p>
     * @see #isAutoComplete()
     */
    public void setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
        this.autoComplete_set = true;
    }

    // enctype
    private String enctype = null;

    /**
 * <p>Use this attribute to set the content-type of the HTTP request
 *       generated by this form. You do not normally need to set this
 *       attribute. Its default value is
 *       application/x-www-form-urlencoded. If there is an upload tag 
 *       inside the form, the upload tag will modify the form's enctype
 *       attribute to multipart/form-data.</p>
     */
    public String getEnctype() {
        if (this.enctype != null) {
            return this.enctype;
        }
        ValueBinding _vb = getValueBinding("enctype");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return "application/x-www-form-urlencoded";
    }

    /**
 * <p>Use this attribute to set the content-type of the HTTP request
 *       generated by this form. You do not normally need to set this
 *       attribute. Its default value is
 *       application/x-www-form-urlencoded. If there is an upload tag 
 *       inside the form, the upload tag will modify the form's enctype
 *       attribute to multipart/form-data.</p>
     * @see #getEnctype()
     */
    public void setEnctype(String enctype) {
        this.enctype = enctype;
    }

    // internalVirtualForms
    private com.sun.rave.web.ui.component.Form.VirtualFormDescriptor[] internalVirtualForms = null;

    /**
 * <p>The virtual forms used "internally" by components (such as Table).
 *         Component authors can manipulate this set of virtual forms independent
 *         of the set exposed to developers. This set is only consulted after the
 *         set exposed to developers is consulted. A participating or submitting id
 *         can end in ":*" to indicate descendants. For example, table1:* can be
 *         used as a participating or submitting id to indicate all the descendants
 *         of table1.</p>
     */
    public com.sun.rave.web.ui.component.Form.VirtualFormDescriptor[] getInternalVirtualForms() {
        return this.internalVirtualForms;
    }

    /**
 * <p>The virtual forms used "internally" by components (such as Table).
 *         Component authors can manipulate this set of virtual forms independent
 *         of the set exposed to developers. This set is only consulted after the
 *         set exposed to developers is consulted. A participating or submitting id
 *         can end in ":*" to indicate descendants. For example, table1:* can be
 *         used as a participating or submitting id to indicate all the descendants
 *         of table1.</p>
     * @see #getInternalVirtualForms()
     */
    public void setInternalVirtualForms(com.sun.rave.web.ui.component.Form.VirtualFormDescriptor[] internalVirtualForms) {
        this.internalVirtualForms = internalVirtualForms;
    }

    // onClick
    private String onClick = null;

    /**
 * <p>Scripting code executed when a mouse click
 *     occurs over this component.</p>
     */
    public String getOnClick() {
        if (this.onClick != null) {
            return this.onClick;
        }
        ValueBinding _vb = getValueBinding("onClick");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse click
 *     occurs over this component.</p>
     * @see #getOnClick()
     */
    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

    // onDblClick
    private String onDblClick = null;

    /**
 * <p>Scripting code executed when a mouse double click
 *     occurs over this component.</p>
     */
    public String getOnDblClick() {
        if (this.onDblClick != null) {
            return this.onDblClick;
        }
        ValueBinding _vb = getValueBinding("onDblClick");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse double click
 *     occurs over this component.</p>
     * @see #getOnDblClick()
     */
    public void setOnDblClick(String onDblClick) {
        this.onDblClick = onDblClick;
    }

    // onKeyDown
    private String onKeyDown = null;

    /**
 * <p>Scripting code executed when the user presses down on a key while the
 *     component has focus.</p>
     */
    public String getOnKeyDown() {
        if (this.onKeyDown != null) {
            return this.onKeyDown;
        }
        ValueBinding _vb = getValueBinding("onKeyDown");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses down on a key while the
 *     component has focus.</p>
     * @see #getOnKeyDown()
     */
    public void setOnKeyDown(String onKeyDown) {
        this.onKeyDown = onKeyDown;
    }

    // onKeyPress
    private String onKeyPress = null;

    /**
 * <p>Scripting code executed when the user presses and releases a key while
 *     the component has focus.</p>
     */
    public String getOnKeyPress() {
        if (this.onKeyPress != null) {
            return this.onKeyPress;
        }
        ValueBinding _vb = getValueBinding("onKeyPress");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses and releases a key while
 *     the component has focus.</p>
     * @see #getOnKeyPress()
     */
    public void setOnKeyPress(String onKeyPress) {
        this.onKeyPress = onKeyPress;
    }

    // onKeyUp
    private String onKeyUp = null;

    /**
 * <p>Scripting code executed when the user releases a key while the
 *     component has focus.</p>
     */
    public String getOnKeyUp() {
        if (this.onKeyUp != null) {
            return this.onKeyUp;
        }
        ValueBinding _vb = getValueBinding("onKeyUp");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user releases a key while the
 *     component has focus.</p>
     * @see #getOnKeyUp()
     */
    public void setOnKeyUp(String onKeyUp) {
        this.onKeyUp = onKeyUp;
    }

    // onMouseDown
    private String onMouseDown = null;

    /**
 * <p>Scripting code executed when the user presses a mouse button while the
 *     mouse pointer is on the component.</p>
     */
    public String getOnMouseDown() {
        if (this.onMouseDown != null) {
            return this.onMouseDown;
        }
        ValueBinding _vb = getValueBinding("onMouseDown");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses a mouse button while the
 *     mouse pointer is on the component.</p>
     * @see #getOnMouseDown()
     */
    public void setOnMouseDown(String onMouseDown) {
        this.onMouseDown = onMouseDown;
    }

    // onMouseMove
    private String onMouseMove = null;

    /**
 * <p>Scripting code executed when the user moves the mouse pointer while
 *     over the component.</p>
     */
    public String getOnMouseMove() {
        if (this.onMouseMove != null) {
            return this.onMouseMove;
        }
        ValueBinding _vb = getValueBinding("onMouseMove");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user moves the mouse pointer while
 *     over the component.</p>
     * @see #getOnMouseMove()
     */
    public void setOnMouseMove(String onMouseMove) {
        this.onMouseMove = onMouseMove;
    }

    // onMouseOut
    private String onMouseOut = null;

    /**
 * <p>Scripting code executed when a mouse out movement
 *     occurs over this component.</p>
     */
    public String getOnMouseOut() {
        if (this.onMouseOut != null) {
            return this.onMouseOut;
        }
        ValueBinding _vb = getValueBinding("onMouseOut");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse out movement
 *     occurs over this component.</p>
     * @see #getOnMouseOut()
     */
    public void setOnMouseOut(String onMouseOut) {
        this.onMouseOut = onMouseOut;
    }

    // onMouseOver
    private String onMouseOver = null;

    /**
 * <p>Scripting code executed when the user moves the  mouse pointer into
 *     the boundary of this component.</p>
     */
    public String getOnMouseOver() {
        if (this.onMouseOver != null) {
            return this.onMouseOver;
        }
        ValueBinding _vb = getValueBinding("onMouseOver");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user moves the  mouse pointer into
 *     the boundary of this component.</p>
     * @see #getOnMouseOver()
     */
    public void setOnMouseOver(String onMouseOver) {
        this.onMouseOver = onMouseOver;
    }

    // onMouseUp
    private String onMouseUp = null;

    /**
 * <p>Scripting code executed when the user releases a mouse button while
 *     the mouse pointer is on the component.</p>
     */
    public String getOnMouseUp() {
        if (this.onMouseUp != null) {
            return this.onMouseUp;
        }
        ValueBinding _vb = getValueBinding("onMouseUp");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user releases a mouse button while
 *     the mouse pointer is on the component.</p>
     * @see #getOnMouseUp()
     */
    public void setOnMouseUp(String onMouseUp) {
        this.onMouseUp = onMouseUp;
    }

    // onReset
    private String onReset = null;

    /**
 * <p>Scripting code executed when this form is reset.</p>
     */
    public String getOnReset() {
        if (this.onReset != null) {
            return this.onReset;
        }
        ValueBinding _vb = getValueBinding("onReset");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when this form is reset.</p>
     * @see #getOnReset()
     */
    public void setOnReset(String onReset) {
        this.onReset = onReset;
    }

    // onSubmit
    private String onSubmit = null;

    /**
 * <p>Scripting code executed when this form is submitted.</p>
     */
    public String getOnSubmit() {
        if (this.onSubmit != null) {
            return this.onSubmit;
        }
        ValueBinding _vb = getValueBinding("onSubmit");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when this form is submitted.</p>
     * @see #getOnSubmit()
     */
    public void setOnSubmit(String onSubmit) {
        this.onSubmit = onSubmit;
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

    // target
    private String target = null;

    /**
 * <p>The form's target window.</p>
     */
    public String getTarget() {
        if (this.target != null) {
            return this.target;
        }
        ValueBinding _vb = getValueBinding("target");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The form's target window.</p>
     * @see #getTarget()
     */
    public void setTarget(String target) {
        this.target = target;
    }

    // virtualForms
    private com.sun.rave.web.ui.component.Form.VirtualFormDescriptor[] virtualForms = null;

    /**
 * <p>The virtual forms within this literal form, represented as an
 *         array of Form.VirtualFormDescriptor objects.  This property
 *         and the "virtualFormsConfig" property are automatically kept
 *         in-sync.</p>
     */
    public com.sun.rave.web.ui.component.Form.VirtualFormDescriptor[] getVirtualForms() {
        return this.virtualForms;
    }

    /**
 * <p>The virtual forms within this literal form, represented as an
 *         array of Form.VirtualFormDescriptor objects.  This property
 *         and the "virtualFormsConfig" property are automatically kept
 *         in-sync.</p>
     * @see #getVirtualForms()
     */
    public void setVirtualForms(com.sun.rave.web.ui.component.Form.VirtualFormDescriptor[] virtualForms) {
        this.virtualForms = virtualForms;
    }

    // virtualFormsConfig
    private String virtualFormsConfig = null;

    /**
 * <p>The configuration of the virtual forms within this literal form, represented as a String.
 *         Each virtual form is described by three parts, separated with pipe ("|") characters:
 *         the virtual form name, a space-separated list of component ids that participate in the 
 *         virtual form, and a space-separated list of component ids that submit the virtual form.
 *         Multiple such virtual form "descriptors" are separated by commas. The component ids may 
 *         be qualified (for instance, "table1:tableRowGroup1:tableColumn1:textField1").</p>
     */
    public String getVirtualFormsConfig() {
        return this.virtualFormsConfig;
    }

    /**
 * <p>The configuration of the virtual forms within this literal form, represented as a String.
 *         Each virtual form is described by three parts, separated with pipe ("|") characters:
 *         the virtual form name, a space-separated list of component ids that participate in the 
 *         virtual form, and a space-separated list of component ids that submit the virtual form.
 *         Multiple such virtual form "descriptors" are separated by commas. The component ids may 
 *         be qualified (for instance, "table1:tableRowGroup1:tableColumn1:textField1").</p>
     * @see #getVirtualFormsConfig()
     */
    public void setVirtualFormsConfig(String virtualFormsConfig) {
        this.virtualFormsConfig = virtualFormsConfig;
    }

    // visible
    private boolean visible = false;
    private boolean visible_set = false;

    /**
 * <p>Use the visible attribute to indicate whether the component should be 
 *     viewable by the user in the rendered HTML page.</p>
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
 *     viewable by the user in the rendered HTML page.</p>
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
        this.autoComplete = ((Boolean) _values[1]).booleanValue();
        this.autoComplete_set = ((Boolean) _values[2]).booleanValue();
        this.enctype = (String) _values[3];
        this.internalVirtualForms = (com.sun.rave.web.ui.component.Form.VirtualFormDescriptor[]) _values[4];
        this.onClick = (String) _values[5];
        this.onDblClick = (String) _values[6];
        this.onKeyDown = (String) _values[7];
        this.onKeyPress = (String) _values[8];
        this.onKeyUp = (String) _values[9];
        this.onMouseDown = (String) _values[10];
        this.onMouseMove = (String) _values[11];
        this.onMouseOut = (String) _values[12];
        this.onMouseOver = (String) _values[13];
        this.onMouseUp = (String) _values[14];
        this.onReset = (String) _values[15];
        this.onSubmit = (String) _values[16];
        this.style = (String) _values[17];
        this.styleClass = (String) _values[18];
        this.target = (String) _values[19];
        this.virtualForms = (com.sun.rave.web.ui.component.Form.VirtualFormDescriptor[]) _values[20];
        this.virtualFormsConfig = (String) _values[21];
        this.visible = ((Boolean) _values[22]).booleanValue();
        this.visible_set = ((Boolean) _values[23]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[24];
        _values[0] = super.saveState(_context);
        _values[1] = this.autoComplete ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.autoComplete_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.enctype;
        _values[4] = this.internalVirtualForms;
        _values[5] = this.onClick;
        _values[6] = this.onDblClick;
        _values[7] = this.onKeyDown;
        _values[8] = this.onKeyPress;
        _values[9] = this.onKeyUp;
        _values[10] = this.onMouseDown;
        _values[11] = this.onMouseMove;
        _values[12] = this.onMouseOut;
        _values[13] = this.onMouseOver;
        _values[14] = this.onMouseUp;
        _values[15] = this.onReset;
        _values[16] = this.onSubmit;
        _values[17] = this.style;
        _values[18] = this.styleClass;
        _values[19] = this.target;
        _values[20] = this.virtualForms;
        _values[21] = this.virtualFormsConfig;
        _values[22] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[23] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
