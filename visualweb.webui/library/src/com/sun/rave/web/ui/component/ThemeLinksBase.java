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
 * Use this tag to render references to theme resource files on a
 *     page where the <code>ui:head</code> component cannot be used. 
 * 
 *     <h3>Configuring the <code>ui:themeLinks</code> Tag</h3>
 * 
 * <p>If no attributes are specified, the component renders a
 *     <code>link</code> to the CSS stylesheet class(es) and a
 *     <code>script</code> element with a reference to the JavaScript
 *     functions definition file required by the Sun Java Web UI
 *     Components. </p> 
 * 
 * <p>To suppress rendering of the <code>script</code> element, set the 
 * <code>javaScript</code> attribute value to false. </p> 
 * 
 * <p>To suppress rendering of the <code>link</code> element, set the 
 * <code>styleSheet</code> attribute value to false. </p> 
 * 
 * <p>To render a style element with an inline import of the stylesheet
 *     definitions, set the 
 * <code>styleSheetInline</code> attribute value to true. </p> 
 * 
 *     <h3>Example</h3>
 * 
 * <pre> 
 * &lt;head&gt;
 * &lt;title&gt;ThemeLinks test&lt;/title&gt;
 * &lt;ui:themeLinks styleSheetInline="true"/&gt;
 * &lt;/head&gt;
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class ThemeLinksBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>ThemeLinksBase</code>.</p>
     */
    public ThemeLinksBase() {
        super();
        setRendererType("com.sun.rave.web.ui.ThemeLinks");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.ThemeLinks";
    }

    // javaScript
    private boolean javaScript = false;
    private boolean javaScript_set = false;

    /**
 * <p>If the <code>javaScript</code> attribute is true, a <code>script</code>
 *       element with a reference to the JavaScript file that defines the
 *       client side behaviour of the Sun Java Web UI Components is
 *       rendered. The default value is true. This component is primarily
 * 	    intended for portlet 
 *       environments. In a web application, this functionality is
 *       automatically provided by the <code>ui:head</code>
 *       component.</p>
     */
    public boolean isJavaScript() {
        if (this.javaScript_set) {
            return this.javaScript;
        }
        ValueBinding _vb = getValueBinding("javaScript");
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
 * <p>If the <code>javaScript</code> attribute is true, a <code>script</code>
 *       element with a reference to the JavaScript file that defines the
 *       client side behaviour of the Sun Java Web UI Components is
 *       rendered. The default value is true. This component is primarily
 * 	    intended for portlet 
 *       environments. In a web application, this functionality is
 *       automatically provided by the <code>ui:head</code>
 *       component.</p>
     * @see #isJavaScript()
     */
    public void setJavaScript(boolean javaScript) {
        this.javaScript = javaScript;
        this.javaScript_set = true;
    }

    // styleSheet
    private boolean styleSheet = false;
    private boolean styleSheet_set = false;

    /**
 * <p>If the <code>styleSheet</code> attribute is true, a <code>link</code>
 *       element with a reference to the CSS stylesheet that defines the
 *       appearance of the Sun Java Web UI Components is
 *       rendered. The default value is true.
 *       This component is primarily intended for portlet
 *       environments. In a web application, this functionality is
 *       automatically provided by the <code>ui:head</code>
 *       component.</p>
     */
    public boolean isStyleSheet() {
        if (this.styleSheet_set) {
            return this.styleSheet;
        }
        ValueBinding _vb = getValueBinding("styleSheet");
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
 * <p>If the <code>styleSheet</code> attribute is true, a <code>link</code>
 *       element with a reference to the CSS stylesheet that defines the
 *       appearance of the Sun Java Web UI Components is
 *       rendered. The default value is true.
 *       This component is primarily intended for portlet
 *       environments. In a web application, this functionality is
 *       automatically provided by the <code>ui:head</code>
 *       component.</p>
     * @see #isStyleSheet()
     */
    public void setStyleSheet(boolean styleSheet) {
        this.styleSheet = styleSheet;
        this.styleSheet_set = true;
    }

    // styleSheetInline
    private boolean styleSheetInline = false;
    private boolean styleSheetInline_set = false;

    /**
 * <p>If the <code>styleSheetInline</code> attribute is true, the
 *       stylesheet that defines the appearance of the Sun Java Web UI
 *       Components is rendered inline. The default value is true.
 *       This component is primarily
 *       intended for portlet environments. In a web application, this
 *       functionality is automatically provided by the
 *       <code>ui:head</code> component.</p>
     */
    public boolean isStyleSheetInline() {
        if (this.styleSheetInline_set) {
            return this.styleSheetInline;
        }
        ValueBinding _vb = getValueBinding("styleSheetInline");
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
 * <p>If the <code>styleSheetInline</code> attribute is true, the
 *       stylesheet that defines the appearance of the Sun Java Web UI
 *       Components is rendered inline. The default value is true.
 *       This component is primarily
 *       intended for portlet environments. In a web application, this
 *       functionality is automatically provided by the
 *       <code>ui:head</code> component.</p>
     * @see #isStyleSheetInline()
     */
    public void setStyleSheetInline(boolean styleSheetInline) {
        this.styleSheetInline = styleSheetInline;
        this.styleSheetInline_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.javaScript = ((Boolean) _values[1]).booleanValue();
        this.javaScript_set = ((Boolean) _values[2]).booleanValue();
        this.styleSheet = ((Boolean) _values[3]).booleanValue();
        this.styleSheet_set = ((Boolean) _values[4]).booleanValue();
        this.styleSheetInline = ((Boolean) _values[5]).booleanValue();
        this.styleSheetInline_set = ((Boolean) _values[6]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[7];
        _values[0] = super.saveState(_context);
        _values[1] = this.javaScript ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.javaScript_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.styleSheet ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.styleSheet_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.styleSheetInline ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.styleSheetInline_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
