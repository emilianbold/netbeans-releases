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
 * <p> The <code>PropertySheet</code> component is a <code>NamingContainer</code>
 *     used to layout <code>PropertySheetSection</code> components on a page.
 *     Each <code>PropertySheetSection</code> may in turn have any number of
 *     <code>Property</code> components within it.  This allows you to easily
 *     format a page with a number of input or read-only fields.
 *     <code>PropertySheetSection</code>s allow you to group <code>Property</code>
 *     components together and provide a <code>label</code> for the set of
 *     enclosed <code>Property</code>s.</p>
 * 
 * <p> The <code>PropertySheet</code> allows each
 *     <code>PropertySheetSection</code> to have an optional "jump link" from the
 *     top of the <code>PropertySheet</code> to each
 *     <code>PropertySheetSection</code> within the <code>PropertySheet</code>.
 *     This is accomplished by supplying the attribute <code>jumpLinks</code> with
 *     a value of true.  If not specified, this attribute defaults to false.</p>
 * 
 * <h3>Client Side Javascript Functions</h3>
 * 
 * <p> None.</p>
 * 
 * <h3>Example:</h3>
 * 
 * <h4>Example 1: Create a simple <code>PropertySheet</code> which contains 2 <code>PropertySheetSection</code> components each containing 2 <code>Property</code> components:</h4>
 * 
 * <p>
 *     <code>
 *     <pre>
 * 	&lt;ui:propertySheet id="propSheetExample1" jumpLinks="true"&gt;
 * 	    &lt;ui:propertySheetSection id="firstSection" label="Search Criteria"&gt;
 * 		&lt;ui:property id="Property1" label="Instance Name: " labelAlign="right" noWrap="true" overlapLabel="false"&gt;
 * 		    &lt;ui:dropDown id="servers" required="true" items="#{BackingFileChoice.servers}" /&gt;
 * 		    &lt;f:verbatim&gt;&amp;amp;nbsp;&amp;amp;nbsp;&amp;amp;nbsp;&amp;amp;nbsp;&amp;amp;nbsp;&lt;/f:verbatim&gt;
 * 		    &lt;ui:label id="logFileLabel" labelLevel="2" text="Log File: " /&gt;
 * 		    &lt;ui:dropDown id="logFile" items="#{BackingFileChoice.archivedLogFiles}" /&gt;
 * 		&lt;/ui:property&gt;
 * 		&lt;ui:property id="Property2" label="Log Level: " labelAlign="right" noWrap="true" overlapLabel="false" helpText="#{bundle.['log.level.help']}"&gt;
 * 		    &lt;f:facet name="content"&gt;
 * 			&lt;ui:dropDown id="logLevel" items="#{BackingFileChoice.logLevel}" /&gt;
 * 		    &lt;/f:facet&gt;
 * 		&lt;/ui:property&gt;
 * 	    &lt;/ui:propertySheetSection&gt;
 * 	    &lt;ui:propertySheetSection id="secondSection" label="Advanced Options"&gt;
 * 		&lt;ui:property id="Property3" label="Logger: " labelAlign="right" noWrap="true" overlapLabel="false" helpText="Select one or more module logs to view"&gt;
 * 		    &lt;ui:listbox id="logger" items="#{BackingFileChoice.loggers}" rows="5" /&gt;
 * 		&lt;/ui:property&gt;
 * 		&lt;ui:property id="Property4" noWrap="true" overlapLabel="false" helpText="Select one or more module logs to view"&gt;
 * 		    &lt;ui:checkbox id="limitLongLogs" label="Limit excessively long messages" /&gt;
 * 		&lt;/ui:property&gt;
 * 	    &lt;/ui:propertySheetSection&gt;
 * 	&lt;/ui:propertySheet&gt;
 *     </pre>
 *     </code>
 * </p>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class PropertySheetBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>PropertySheetBase</code>.</p>
     */
    public PropertySheetBase() {
        super();
        setRendererType("com.sun.rave.web.ui.PropertySheet");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.PropertySheet";
    }

    // jumpLinks
    private boolean jumpLinks = false;
    private boolean jumpLinks_set = false;

    /**
 * <p>	This boolean attribute allows you to control whether jump links
 * 		will be created at the top of this <code>PropertySheet</code>
 * 		or not.  The default is NOT to create the links -- setting this
 * 		attribute to "true" turns this feature on.</p>
     */
    public boolean isJumpLinks() {
        if (this.jumpLinks_set) {
            return this.jumpLinks;
        }
        ValueBinding _vb = getValueBinding("jumpLinks");
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
 * <p>	This boolean attribute allows you to control whether jump links
 * 		will be created at the top of this <code>PropertySheet</code>
 * 		or not.  The default is NOT to create the links -- setting this
 * 		attribute to "true" turns this feature on.</p>
     * @see #isJumpLinks()
     */
    public void setJumpLinks(boolean jumpLinks) {
        this.jumpLinks = jumpLinks;
        this.jumpLinks_set = true;
    }

    // requiredFields
    private String requiredFields = null;

    /**
 * <p>This property indicates whether a required field legend should be
 * 	  displayed at the top right-hand side of the property sheet.</p>
     */
    public String getRequiredFields() {
        if (this.requiredFields != null) {
            return this.requiredFields;
        }
        ValueBinding _vb = getValueBinding("requiredFields");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>This property indicates whether a required field legend should be
 * 	  displayed at the top right-hand side of the property sheet.</p>
     * @see #getRequiredFields()
     */
    public void setRequiredFields(String requiredFields) {
        this.requiredFields = requiredFields;
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
        this.jumpLinks = ((Boolean) _values[1]).booleanValue();
        this.jumpLinks_set = ((Boolean) _values[2]).booleanValue();
        this.requiredFields = (String) _values[3];
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
        _values[1] = this.jumpLinks ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.jumpLinks_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.requiredFields;
        _values[4] = this.style;
        _values[5] = this.styleClass;
        _values[6] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
