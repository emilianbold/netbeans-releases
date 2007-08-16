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
 * <span style="text-decoration: line-through; "></span><span
 * >Use the <code>ui:anchor</code> tag
 * to create HTML anchors in the rendered HTML page. You can then use <code>ui:hyperlink</code>
 * tags that jump to the locations of the anchors in the page.</span><br
 * >
 * <h3 >HTML Elements and Layout</h3>
 * <span style="text-decoration: line-through; "></span><span
 * >The <code>ui:anchor</code> tag
 * creates an <code>&lt;a&gt;</code> element in the rendered HTML page.
 * The name and id attributes of the <code>&lt;a&gt;</code> element are
 * both set to the value of the id attribute specified with the <code>ui:anchor</code>
 * tag. The name attibute is included to maintain compatibility with older
 * browsers.</span><br >
 * <h3 >Client Side Javascript Functions</h3>
 * <span >None.&nbsp; </span><br
 * >
 * <h3 >Examples</h3>
 * <h4 >Example 1: Create an anchor</h4>
 * <code >&lt;ui:anchor id="anchor1" /&gt;
 * <br>
 * <br>
 * </code><span >This generates an anchor,
 * with id and name set to the same
 * value. </span><br >
 * <span >&nbsp;&nbsp; </span><br
 * >
 * <code >&lt;a id="anchor1" name="anchor1"
 * /&gt;
 * </code><br >
 * <span style="font-style: italic; "><span
 * style="text-decoration: line-through;"></span></span>
 * <h4 >Example 2: Create a hyperlink to
 * that will go to the anchor above<br>
 * </h4>
 * <code >&lt;ui:hyperlink id="gotoAnchor1"
 * url="#anchor1" /&gt;<br>
 * </code>
 * <h4 >Example 3: Create a <span
 * style="text-decoration: line-through;"></span>context&nbsp;<span
 * style="text-decoration: line-through;"></span> relative hyperlink to
 * go to
 * the anchor
 * in <span style="text-decoration: line-through;"></span>Example 1</h4>
 * <code >&lt;ui:hyperlink id="gotoAnchor1"
 * url="/faces/hyperlink.jsp#anchor1" /&gt;<br>
 * </code><span ><br>
 * Note:&nbsp; In the <code>url</code>
 * attribute, you must specify a path that maps to the correct servlet.
 * However, you do not need the context. In this example, the </span><code
 * >/faces</code> <span
 * > part of the path maps to the servlet
 * through the JSF FacesServlet as defined in the web.xml.</span>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class AnchorBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>AnchorBase</code>.</p>
     */
    public AnchorBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Anchor");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Anchor";
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

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.style = (String) _values[1];
        this.styleClass = (String) _values[2];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[3];
        _values[0] = super.saveState(_context);
        _values[1] = this.style;
        _values[2] = this.styleClass;
        return _values;
    }

}
