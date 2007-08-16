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
 * <span style="color: rgb(0, 0, 0);">Use
 * the <code>ui:iconHyperlink</code>
 * tag to display a clickable icon image from the current theme in the
 * rendered HTML page. The <code>iconHyperlink</code>
 * component is
 * essentially a subclass of the <code>imageHyperlink</code>
 * component.
 * The <code>ui:iconHyperlink</code>
 * tag allows you to use an
 * icon (a small image) from the current theme. Currently the list of
 * icons that you can use is not publicly supported, but the icon names
 * are specified in the <code>/com/sun/rave/web/ui/suntheme/SunTheme.properties</code>
 * file. The names are listed as resource keys of the format <code>image.ICON_NAME.</code>
 * Use only the part of the key that follows <code>image.
 * </code>For
 * example, if the key is <code>image.ALARM_CRITICAL_SMALL</code>,
 * you
 * should use <code>ALARM_CRITICAL_SMALL
 * </code>as the specified
 * icon name in the <code>ui:iconHyperlink</code>
 * tag. In the near future a
 * supported list will be published.&nbsp; This tag is based on a <code>ui:hyperlink</code>
 * tag and functions the same way.&nbsp; The main difference is this
 * tag will format an image with a surrounding hyperlink.&nbsp; See
 * the <code>ui:hyperlink</code>
 * tag for more examples on using a hyperlink</span>
 * <p style="color: rgb(0, 0, 0);">The
 * iconHyperlink component
 * can be also be used to submit forms. If the action attribute is used,
 * the form is submitted. If the
 * url attribute is used, the link is a normal hyperlink that sends the
 * browser to a new location.</p>
 * <h3 style="color: rgb(0, 0, 0);">HTML
 * Elements and Layout<br>
 * </h3>
 * <p style="color: rgb(0, 0, 0);">The
 * rendered HTML page displays....<br>
 * </p>
 * <h3 style="color: rgb(0, 0, 0);">Theme
 * Identifiers</h3>
 * <span style="color: rgb(0, 0, 0);">None.</span>
 * <h3 style="color: rgb(0, 0, 0);">Client-side
 * JavaScript functions<br>
 * </h3>
 * <p style="color: rgb(0, 0, 0);">
 * None. <br>
 * </p>
 * <h3 style="color: rgb(0, 0, 0);">Examples</h3>
 * <h4 style="color: rgb(0, 0, 0);">Example
 * 1: Create an IconHyperlink using <span
 *  style="text-decoration: line-through;">showing</span>
 * the required
 * indicator <br>
 * </h4>
 * <span style="color: rgb(0, 0, 0);">
 * </span><code
 *  style="color: rgb(0, 0, 0);">&lt;ui:iconHyperlink
 * id="iconhyperlinktest1" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * icon="LABEL_REQUIRED_ICON"
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * action="#{HyperlinkBean.getRequiredHelp}" /&gt;
 * <br>
 * </code><code
 *  style="color: rgb(0, 0, 0);"><a>
 * </a></code>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class IconHyperlinkBase extends com.sun.rave.web.ui.component.ImageHyperlink {

    /**
     * <p>Construct a new <code>IconHyperlinkBase</code>.</p>
     */
    public IconHyperlinkBase() {
        super();
        setRendererType("com.sun.rave.web.ui.IconHyperlink");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.IconHyperlink";
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[1];
        _values[0] = super.saveState(_context);
        return _values;
    }

}
