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
 * <body onload="asd();" bgcolor="white">
 * <span style="color: rgb(0, 0, 0);">Use the </span><code
 * style="color: rgb(0, 0, 0);">ui:head</code><span
 * style="color: rgb(0, 0, 0);"> tag to provide information about the
 * document, to be used in the <code>&lt;head&gt;</code> element of the
 * rendered HTML page.&nbsp; The </span><code style="color: rgb(0, 0, 0);">ui:head</code><span
 * style="color: rgb(0, 0, 0);"> tag must be placed immediately after
 * the <code>ui:html</code> tag, within the <code>ui:page</code> tag.
 * &nbsp; The following tags can be
 * used as children of the <code>ui:head</code> tag:</span><br
 * style="color: rgb(0, 0, 0);">
 * <ul style="color: rgb(0, 0, 0);">
 * <li><a href="link.html"><code>ui:link</code></a></li>
 * <li><a href="meta.html"><code>ui:meta</code></a></li>
 * <li><code><a href="script.html">ui:script</a></code></li>
 * </ul>
 * <h3 style="color: rgb(0, 0, 0);">HTML Elements and Layout</h3>
 * <span style="text-decoration: line-through; color: rgb(0, 0, 0);"></span>
 * <p style="color: rgb(0, 0, 0);">The <code>&lt;head&gt;</code>
 * element is rendered in the HTML page, and can include information that
 * is valid for inclusion in the HTML <code>&lt;head&gt;</code> element.
 * For example, if you use the <code>ui:link</code> tag in the <code>ui:head</code>
 * tag, you can provide the URL to a style sheet. The style sheet
 * link&nbsp; will be rendered as a <code>&lt;link&gt;</code> element in
 * the <code>&lt;head&gt;</code> element of the HTML page.&nbsp; In
 * addition this tag will output the appropriate stylesheets for the
 * included components on the page.<br>
 * </p>
 * <span style="color: rgb(0, 0, 0);">The
 * browser does not display the information in the <code>&lt;head&gt;</code>
 * element to the user, with the exception of the title that is used in
 * the browser's title bar.&nbsp;&nbsp; </span>
 * <h3 style="color: rgb(0, 0, 0);">Client Side Javascript Functions</h3>
 * <span style="color: rgb(0, 0, 0);">None. </span>
 * <h3 style="color: rgb(0, 0, 0);">Example</h3>
 * <b style="color: rgb(0, 0, 0);">Example 1: Create a head tag </b><br
 * style="color: rgb(0, 0, 0);">
 * <code style="color: rgb(0, 0, 0);"><br>
 * &lt;ui:page&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:html&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; </code><code
 * style="color: rgb(0, 0, 0);">&lt;ui:head
 * title="Name of the
 * page..title is required"&gt;<br>
 * </code><code style="color: rgb(0, 0, 0);">&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp; &nbsp; &nbsp;&nbsp;
 * &lt;ui:link url="/relativepath/x.css" /&gt;</code><code
 * style="color: rgb(0, 0, 0);"><br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp; &lt;ui:script url="/relativepath/x.js" /&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; </code><code
 * style="color: rgb(0, 0, 0);">&lt;/ui:head&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; </code><code
 * style="color: rgb(0, 0, 0);">&lt;ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; </code><code
 * style="color: rgb(0, 0, 0);">....your
 * page content....<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; </code><code
 * style="color: rgb(0, 0, 0);">&lt;/ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp; </code><code style="color: rgb(0, 0, 0);">&lt;/ui:html&gt;</code>
 * <code style="color: rgb(0, 0, 0);">&lt;/ui:page&gt;</code><span
 * style="color: rgb(0, 0, 0);">&nbsp;</span><small><i><br>
 * <br>
 * </i></small>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class HeadBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>HeadBase</code>.</p>
     */
    public HeadBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Head");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Head";
    }

    // defaultBase
    private boolean defaultBase = false;
    private boolean defaultBase_set = false;

    /**
 * <p>Flag (true or false) indicating that a default html base tag should be
 *         shown or not.  Changing this attribute could cause ui:anchor to not work
 *         properly.  The default value is false.</p>
     */
    public boolean isDefaultBase() {
        if (this.defaultBase_set) {
            return this.defaultBase;
        }
        ValueBinding _vb = getValueBinding("defaultBase");
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
 * <p>Flag (true or false) indicating that a default html base tag should be
 *         shown or not.  Changing this attribute could cause ui:anchor to not work
 *         properly.  The default value is false.</p>
     * @see #isDefaultBase()
     */
    public void setDefaultBase(boolean defaultBase) {
        this.defaultBase = defaultBase;
        this.defaultBase_set = true;
    }

    // profile
    private String profile = null;

    /**
 * <p>A space separated list of URL's that contains meta data information 
 *         about the page</p>
     */
    public String getProfile() {
        if (this.profile != null) {
            return this.profile;
        }
        ValueBinding _vb = getValueBinding("profile");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>A space separated list of URL's that contains meta data information 
 *         about the page</p>
     * @see #getProfile()
     */
    public void setProfile(String profile) {
        this.profile = profile;
    }

    // title
    private String title = null;

    /**
 * <p>Title of the document to be displayed in the browser title bar.</p>
     */
    public String getTitle() {
        if (this.title != null) {
            return this.title;
        }
        ValueBinding _vb = getValueBinding("title");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Title of the document to be displayed in the browser title bar.</p>
     * @see #getTitle()
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.defaultBase = ((Boolean) _values[1]).booleanValue();
        this.defaultBase_set = ((Boolean) _values[2]).booleanValue();
        this.profile = (String) _values[3];
        this.title = (String) _values[4];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[5];
        _values[0] = super.saveState(_context);
        _values[1] = this.defaultBase ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.defaultBase_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.profile;
        _values[4] = this.title;
        return _values;
    }

}
