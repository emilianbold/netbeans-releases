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
 * <h3>HTML Elements and Layout</h3>
 * 
 * <p> The <code>Property</code> component was written to be used within the
 *     <code>PropertySheetSection</code> component, which is in turn used within
 *     the context of a <code>PropertySheet</code> component.  The
 *     <code>Property</code> component allows you to encapsulate a logic
 *     "property" and help you lay it out on the page.  A "property" has a number
 *     of configuration options, including: the property content; an optional
 *     label; the ability to stretch the property to include the label area (in
 *     addition to the content area of the "property"; the ability to mark a
 *     property required; and the ability to associate help text with the property
 *     to inform your end user how to interact with the property.</p>
 * 
 * <p> Help text can be provided for each property by supplying the
 *     <code>helpText</code> attribute.  This attribute may be a literal String
 *     or a <code>ValueBinding</code> expression.  The help text will appear
 *     below the content of the "property".  Optionally, the helpText may also
 *     be provided as a facet named "helpText".  This allows advanced users to
 *     have more control over the types of content provided in the helpText
 *     area.</p>
 * 
 * <p> The label may be provided via the <code>label</code> attribute.  The label
 *     will be rendered to the left of the content area of the "property".  The
 *     label area will not exist if the <code>overlapLabel</code> attribute is set
 *     to true.  Optionally advanced users may provide a label facet named
 *     "label".  This allows developers to have more control over the content of
 *     the label area.</p>
 * 
 * <p> The <code>labelAlign</code> attribute can use used to specify "left" or
 *     "right" alignment of the label table cell.</p>
 * 
 * <p> Setting the <code>noWrap</code> attribute to true specifies that the label
 *     should not be wraped to a new line.</p>
 * 
 * <p> The <code>overlapLabel</code> attribute causes the content of the property
 *     to be stretched into the label area as well as the content area.  This may
 *     be useful for titles which should span the entire width, or other cases
 *     where you need the whole width of the <code>PropertySheet</code>.</p>
 * 
 * <h3>Client Side Javascript Functions</h3>
 * 
 * <p> None.</p>
 * 
 * <h3>Examples</h3>
 * 
 * <h4>Example 1</h4> 
 * 
 * <p>The label is specified via the label attribute. The components of
 *     the content area are specified as children of the
 *     <code>ui:property</code> tag.  </p> 
 * 
 * <pre> 
 *    &lt;ui:property id="prop1" label="Log file name: "&gt;           
 *        &lt;ui:textField id="logfile" required="true"/&gt;
 *        &lt;h:message id="logfile_error" for="logfile" showDetail="true"/&gt; 
 *    &lt;/ui:property&gt;
 * </pre>
 * 
 * <h4>Example 2</h4> 
 * 
 * <p>The components of the content area are specified as children of the
 * <code>ui:property</code> tag. The label is specified via a label
 * facet, to label the dropDown in the content area.  </p>
 * 
 * <pre>            
 *    &lt;ui:property id="prop2"&gt;    
 *        &lt;f:facet name="label"&gt;
 *            &lt;ui:label id="prop2label" 
 *                      text="Select update frequency"
 *                      for="unit"/&gt;
 *        &lt;/f:facet&gt;   
 *        &lt;ui:textField id="frequency" 
 *                      text="#{Logger.frequency.number}"
 *                      label="Every "/&gt;
 *        &lt;ui:dropDown id="unit" 
 *                     selected="#{Logger.frequency.unit}"  
 *                     items="#{Logger.frequency.units}"  
 *                     required="true"/&gt;
 *        &lt;h:message id="msg1"_msg4b" for="frequency" showDetail="true"/&gt; 
 *        &lt;h:message id="msg2" for="unit" showDetail="true"/&gt; 
 *    &lt;/ui:property&gt;
 * </pre> 
 * 
 * <h4>Example 3</h4> 
 * 
 * <p>The components of the content area are specified inside a
 *     PanelGroup child component. </p>
 * <pre> 
 *    &lt;ui:property id="prop3" label="Admin Server URI"&gt; 
 *        &lt;ui:panelGroup id="pg"&gt;
 *            &lt;ui:textField id="uri" text="#{Server.uri}" required="true"/&gt;
 *            &lt;h:message id="msg3" for="uri" showDetail="true"/&gt; 
 *        &lt;/ui:panelGroup&gt;                               
 *    &lt;/ui:property&gt;
 * </pre> 
 * 
 * 
 * <h4>Example 4</h4> 
 * 
 * 
 * <p>The components of the content area are specified inside a
 *     PanelGroup inside a facet.</p>           
 * 
 * <pre> 
 *    &lt;ui:property id="prop3" label="Admin Server URI"&gt; 
 *        &lt;f:facet name="content"&gt;
 *            &lt;ui:panelGroup id="pg"&gt;
 *                &lt;ui:textField id="uri" text="#{Server.uri}" required="true"/&gt;
 *                &lt;h:message id="msg3" for="uri" showDetail="true"/&gt; 
 *            &lt;/ui:panelGroup&gt;                               
 *        &lt;/f:facet&gt;                       
 *    &lt;/ui:property&gt;
 * 
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class PropertyBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>PropertyBase</code>.</p>
     */
    public PropertyBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Property");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Property";
    }

    // disabled
    private boolean disabled = false;
    private boolean disabled_set = false;

    /**
 * <p>Flag indicating that the user is not permitted to activate this
 *         component, and that the component's value will not be submitted with the
 *         form.</p>
     */
    public boolean isDisabled() {
        if (this.disabled_set) {
            return this.disabled;
        }
        ValueBinding _vb = getValueBinding("disabled");
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
 * <p>Flag indicating that the user is not permitted to activate this
 *         component, and that the component's value will not be submitted with the
 *         form.</p>
     * @see #isDisabled()
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        this.disabled_set = true;
    }

    // helpText
    private String helpText = null;

    /**
 * <p>The help text will appear below the content of the "property".
 * 	  Optionally, the helpText may also be provided as a facet named
 * 	  "helpText".  This allows advanced users to have more control over
 * 	  the types of content provided in the helpText area.</p>
     */
    public String getHelpText() {
        if (this.helpText != null) {
            return this.helpText;
        }
        ValueBinding _vb = getValueBinding("helpText");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The help text will appear below the content of the "property".
 * 	  Optionally, the helpText may also be provided as a facet named
 * 	  "helpText".  This allows advanced users to have more control over
 * 	  the types of content provided in the helpText area.</p>
     * @see #getHelpText()
     */
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    // label
    private String label = null;

    /**
 * <p>Use this attribute to specify the text of the label of this
 *       property. The <code>for</code> attribute of the label will be
 *       the first input element in the content area of this component. 
 *       To label a different component, use the label facet instead.</p>
     */
    public String getLabel() {
        if (this.label != null) {
            return this.label;
        }
        ValueBinding _vb = getValueBinding("label");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Use this attribute to specify the text of the label of this
 *       property. The <code>for</code> attribute of the label will be
 *       the first input element in the content area of this component. 
 *       To label a different component, use the label facet instead.</p>
     * @see #getLabel()
     */
    public void setLabel(String label) {
        this.label = label;
    }

    // labelAlign
    private String labelAlign = null;

    /**
 * <p>Specifies the label alignment for the label of this component.  The
 * 	label itself may be added via the label property or the label facet.
 * 	The value will typically be "left" or "right".</p>
     */
    public String getLabelAlign() {
        if (this.labelAlign != null) {
            return this.labelAlign;
        }
        ValueBinding _vb = getValueBinding("labelAlign");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Specifies the label alignment for the label of this component.  The
 * 	label itself may be added via the label property or the label facet.
 * 	The value will typically be "left" or "right".</p>
     * @see #getLabelAlign()
     */
    public void setLabelAlign(String labelAlign) {
        this.labelAlign = labelAlign;
    }

    // noWrap
    private boolean noWrap = false;
    private boolean noWrap_set = false;

    /**
 * <p>Specifies if the label component should not wrap.  The label itself
 * 	may be added via the label property or the label facet.</p>
     */
    public boolean isNoWrap() {
        if (this.noWrap_set) {
            return this.noWrap;
        }
        ValueBinding _vb = getValueBinding("noWrap");
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
 * <p>Specifies if the label component should not wrap.  The label itself
 * 	may be added via the label property or the label facet.</p>
     * @see #isNoWrap()
     */
    public void setNoWrap(boolean noWrap) {
        this.noWrap = noWrap;
        this.noWrap_set = true;
    }

    // overlapLabel
    private boolean overlapLabel = false;
    private boolean overlapLabel_set = false;

    /**
 * <p>This indicates whether the property should overlap into the label
 * 	  area or not.  Default: false -- do not extend into the label area.</p>
     */
    public boolean isOverlapLabel() {
        if (this.overlapLabel_set) {
            return this.overlapLabel;
        }
        ValueBinding _vb = getValueBinding("overlapLabel");
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
 * <p>This indicates whether the property should overlap into the label
 * 	  area or not.  Default: false -- do not extend into the label area.</p>
     * @see #isOverlapLabel()
     */
    public void setOverlapLabel(boolean overlapLabel) {
        this.overlapLabel = overlapLabel;
        this.overlapLabel_set = true;
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
        this.disabled = ((Boolean) _values[1]).booleanValue();
        this.disabled_set = ((Boolean) _values[2]).booleanValue();
        this.helpText = (String) _values[3];
        this.label = (String) _values[4];
        this.labelAlign = (String) _values[5];
        this.noWrap = ((Boolean) _values[6]).booleanValue();
        this.noWrap_set = ((Boolean) _values[7]).booleanValue();
        this.overlapLabel = ((Boolean) _values[8]).booleanValue();
        this.overlapLabel_set = ((Boolean) _values[9]).booleanValue();
        this.style = (String) _values[10];
        this.styleClass = (String) _values[11];
        this.visible = ((Boolean) _values[12]).booleanValue();
        this.visible_set = ((Boolean) _values[13]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[14];
        _values[0] = super.saveState(_context);
        _values[1] = this.disabled ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.disabled_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.helpText;
        _values[4] = this.label;
        _values[5] = this.labelAlign;
        _values[6] = this.noWrap ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.noWrap_set ? Boolean.TRUE : Boolean.FALSE;
        _values[8] = this.overlapLabel ? Boolean.TRUE : Boolean.FALSE;
        _values[9] = this.overlapLabel_set ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = this.style;
        _values[11] = this.styleClass;
        _values[12] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
