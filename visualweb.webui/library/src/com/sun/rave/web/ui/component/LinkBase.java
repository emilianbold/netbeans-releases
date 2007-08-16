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
 * <span style="text-decoration: line-through;"></span><span
 *  style="color: rgb(0, 0, 0);">Use the </span><code
 *  style="color: rgb(0, 0, 0);">ui:link</code><span
 *  style="color: rgb(0, 0, 0);"> tag to insert header
 * references to
 * other documents related to this HTML page. The <code>ui:link</code>
 * tag must be used inside the <code>ui:head</code>
 * tag, and can be used
 * multiple times for references to multiple documents. A common use for
 * the <code>ui:link</code>
 * tag is to link to an external stylesheet, but
 * it can also be used to provide information about the document's
 * relationship to other documents.&nbsp; There are a
 * number of useful defaults making it simpler to specify an HTML link
 * using this tag.
 * </span><br
 *  style="color: rgb(0, 0, 0);">
 * <br>
 * <strong></strong><br>
 * <h3>HTML Elements and Layout</h3>
 * <span style="color: rgb(0, 0, 0);">The
 * rendered HTML page contains </span><code
 *  style="color: rgb(0, 0, 0);">&lt;link&gt;</code><span
 *  style="color: rgb(0, 0, 0);"> element, along with any
 * attributes
 * specified through the <code>ui:link</code>
 * tag attributes.&nbsp; </span>
 * <h3 style="color: rgb(0, 0, 0);">Theme
 * Identifiers</h3>
 * <span style="color: rgb(0, 0, 0);">None.</span>
 * <h3 style="color: rgb(0, 0, 0);">Client
 * Side Javascript Functions</h3>
 * <span style="color: rgb(0, 0, 0);">None.</span>
 * <h3 style="color: rgb(0, 0, 0);">Example</h3>
 * <b style="color: rgb(0, 0, 0);">Example
 * 1: Create a context relative link to a stylesheet</b><br
 *  style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <code style="color: rgb(0, 0, 0);">&lt;ui:page&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:html&gt;<br>
 * &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
 * &lt;ui:head id="blah"
 * title="hyperlink test page"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp; <span
 *  style="font-weight: bold;">&lt;ui:link
 * url="/context-relative-path/tomyfile/stylesheet.css"&gt;</span><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;/ui:head&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;
 * &lt;/ui:body&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:html&gt;<br>
 * &lt;/ui:page&gt;</code>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class LinkBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>LinkBase</code>.</p>
     */
    public LinkBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Link");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Link";
    }

    // charset
    private String charset = null;

    /**
 * <p>Defines the character encoding (charset) of the target URL. Default 
 *         value is "ISO-8859-1".</p>
     */
    public String getCharset() {
        if (this.charset != null) {
            return this.charset;
        }
        ValueBinding _vb = getValueBinding("charset");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Defines the character encoding (charset) of the target URL. Default 
 *         value is "ISO-8859-1".</p>
     * @see #getCharset()
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    // media
    private String media = null;

    /**
 * <p>Specifies the type of display device for which the referenced document 
 *         is designed.  The media attribute is useful for specifying different 
 *         stylesheets for print and viewing on a screen.  The default value is 
 *         "screen".</p>
     */
    public String getMedia() {
        if (this.media != null) {
            return this.media;
        }
        ValueBinding _vb = getValueBinding("media");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Specifies the type of display device for which the referenced document 
 *         is designed.  The media attribute is useful for specifying different 
 *         stylesheets for print and viewing on a screen.  The default value is 
 *         "screen".</p>
     * @see #getMedia()
     */
    public void setMedia(String media) {
        this.media = media;
    }

    // rel
    private String rel = null;

    /**
 * <p>Defines the relationship between the current document and the 
 *          targeted document. Default is "stylesheet". Other possible values 
 *          are described at w3.org.</p>
     */
    public String getRel() {
        if (this.rel != null) {
            return this.rel;
        }
        ValueBinding _vb = getValueBinding("rel");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return "stylesheet";
    }

    /**
 * <p>Defines the relationship between the current document and the 
 *          targeted document. Default is "stylesheet". Other possible values 
 *          are described at w3.org.</p>
     * @see #getRel()
     */
    public void setRel(String rel) {
        this.rel = rel;
    }

    // type
    private String type = null;

    /**
 * <p>Specifies the MIME type of the target resource.  Default is: "text/css"</p>
     */
    public String getType() {
        if (this.type != null) {
            return this.type;
        }
        ValueBinding _vb = getValueBinding("type");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return "text/css";
    }

    /**
 * <p>Specifies the MIME type of the target resource.  Default is: "text/css"</p>
     * @see #getType()
     */
    public void setType(String type) {
        this.type = type;
    }

    // url
    private String url = null;

    /**
 * <p>The absolute or relative target URL of the resource.</p>
     */
    public String getUrl() {
        if (this.url != null) {
            return this.url;
        }
        ValueBinding _vb = getValueBinding("url");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The absolute or relative target URL of the resource.</p>
     * @see #getUrl()
     */
    public void setUrl(String url) {
        this.url = url;
    }

    // urlLang
    private String urlLang = null;

    /**
 * <p>Defines the ISO language code of the human language used in the target 
 *         URL file. For example, valid values might be en, fr, es.</p>
     */
    public String getUrlLang() {
        if (this.urlLang != null) {
            return this.urlLang;
        }
        ValueBinding _vb = getValueBinding("urlLang");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Defines the ISO language code of the human language used in the target 
 *         URL file. For example, valid values might be en, fr, es.</p>
     * @see #getUrlLang()
     */
    public void setUrlLang(String urlLang) {
        this.urlLang = urlLang;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.charset = (String) _values[1];
        this.media = (String) _values[2];
        this.rel = (String) _values[3];
        this.type = (String) _values[4];
        this.url = (String) _values[5];
        this.urlLang = (String) _values[6];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[7];
        _values[0] = super.saveState(_context);
        _values[1] = this.charset;
        _values[2] = this.media;
        _values[3] = this.rel;
        _values[4] = this.type;
        _values[5] = this.url;
        _values[6] = this.urlLang;
        return _values;
    }

}
