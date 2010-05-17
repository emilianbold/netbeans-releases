/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.sun.rave.web.ui.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * <span style="text-decoration: line-through;"></span><span
 *  style="text-decoration: line-through;"></span><span
 *  style="color: rgb(0, 0, 0);">Use the <code>ui:html</code>
 * tag to create an <code>&lt;html&gt;</code>
 * element in the rendered
 * HTML page. The </span><code
 *  style="color: rgb(0, 0, 0);">ui:html</code><span
 *  style="color: rgb(0, 0, 0);"> tag must be used as a
 * child of the <code>ui:page</code>
 * tag, following </span><code
 *  style="color: rgb(0, 0, 0);"></code><span
 *  style="color: rgb(0, 0, 0);">immediately after the <code>ui:page</code>
 * tag.&nbsp; This tag is required for pages that are not in a portal
 * enviroment and not subviews.<br>
 * </span><br
 *  style="color: rgb(0, 0, 0);">
 * <span
 *  style="font-weight: bold; color: rgb(0, 0, 0);"></span>
 * <h3 style="color: rgb(0, 0, 0);">HTML
 * Elements and Layout</h3>
 * <span
 *  style="text-decoration: line-through; color: rgb(0, 0, 0);"></span><span
 *  style="color: rgb(0, 0, 0);">The rendered HTML page
 * includes an </span><code
 *  style="color: rgb(0, 0, 0);">&lt;html&gt;</code><span
 *  style="color: rgb(0, 0, 0);">
 * tag that uses attributes indicated by the attributes you specify with
 * the </span><code
 *  style="color: rgb(0, 0, 0);">ui:html</code><span
 *  style="color: rgb(0, 0, 0);"> tag in the JSP page. </span><br
 *  style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Theme
 * Identifiers</h3>
 * <span style="color: rgb(0, 0, 0);">None.</span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Client
 * Side Javascript Functions</h3>
 * <span style="color: rgb(0, 0, 0);">None.
 * </span>
 * <h3 style="color: rgb(0, 0, 0);">Examples</h3>
 * <h4 style="color: rgb(0, 0, 0);">Example
 * 1: Using a <code>ui:html</code>
 * tag<br>
 * </h4>
 * <code style="color: rgb(0, 0, 0);">&lt;ui:page&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:html&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
 * &lt;ui:head id="blah"
 * title="hyperlink test page" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;
 * &lt;ui:form id="form1"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; &lt;ui:hyperlink&nbsp;
 * id="hyperlinkSubmitsPage"&nbsp; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * label="#{HyperlinkBean.label}" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * action="#{HyperlinkBean.determineWhatToDoFunction}" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;
 * &lt;/ui:form&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;
 * &lt;/ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:html&gt;<br>
 * &lt;/ui:page&gt;</code><br
 *  style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);"><br>
 * </span><span
 *  style="color: rgb(0, 0, 0);"></span><code
 *  style="color: rgb(0, 0, 0);"><br>
 * </code>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class HtmlBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>HtmlBase</code>.</p>
     */
    public HtmlBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Html");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Html";
    }

    // lang
    private String lang = null;

    /**
 * <p>Sets the language code for this document</p>
     */
    public String getLang() {
        if (this.lang != null) {
            return this.lang;
        }
        ValueBinding _vb = getValueBinding("lang");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Sets the language code for this document</p>
     * @see #getLang()
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    // xmlns
    private String xmlns = null;

    /**
 * <p>Defines the XML namespace attribute.  Default value is: 
 *         http://www.w3.org/1999/xhtml</p>
     */
    public String getXmlns() {
        if (this.xmlns != null) {
            return this.xmlns;
        }
        ValueBinding _vb = getValueBinding("xmlns");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return "http://www.w3.org/1999/xhtml";
    }

    /**
 * <p>Defines the XML namespace attribute.  Default value is: 
 *         http://www.w3.org/1999/xhtml</p>
     * @see #getXmlns()
     */
    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.lang = (String) _values[1];
        this.xmlns = (String) _values[2];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[3];
        _values[0] = super.saveState(_context);
        _values[1] = this.lang;
        _values[2] = this.xmlns;
        return _values;
    }

}
