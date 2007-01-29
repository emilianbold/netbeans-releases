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
package org.netbeans.modules.visualweb.xhtml;


/**
 * <b>Script</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="script">
 *     <ref name="script.attlist"/>
 *     <text/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="script"&gt;
 *     &lt;ref name="script.attlist"/&gt;
 *     &lt;text/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Script {
    public static final String DEFER_DEFER = "defer";
    public static final String XMLSPACE_PRESERVE = "preserve";

    private String content_;
    private String charset_;
    private String type_;
    private String src_;
    private String defer_;
    private String xmlSpace_;
    private String language_;

    /**
     * Creates a <code>Script</code>.
     *
     */
    public Script() {
        type_ = "";
    }

    /**
     * Gets the String property <b>content</b>.
     *
     * @return String
     */
    public String getContent() {
        return (content_);
    }

    /**
     * Sets the String property <b>content</b>.
     *
     * @param content
     */
    public void setContent(String content) {
        this.content_ = content;
    }

    /**
     * Gets the String property <b>charset</b>.
     *
     * @return String
     */
    public String getCharset() {
        return (charset_);
    }

    /**
     * Sets the String property <b>charset</b>.
     *
     * @param charset
     */
    public void setCharset(String charset) {
        this.charset_ = charset;
    }

    /**
     * Gets the String property <b>type</b>.
     *
     * @return String
     */
    public String getType() {
        return (type_);
    }

    /**
     * Sets the String property <b>type</b>.
     *
     * @param type
     */
    public void setType(String type) {
        this.type_ = type;
    }

    /**
     * Gets the String property <b>src</b>.
     *
     * @return String
     */
    public String getSrc() {
        return (src_);
    }

    /**
     * Sets the String property <b>src</b>.
     *
     * @param src
     */
    public void setSrc(String src) {
        this.src_ = src;
    }

    /**
     * Gets the String property <b>defer</b>.
     *
     * @return String
     */
    public String getDefer() {
        return (defer_);
    }

    /**
     * Sets the String property <b>defer</b>.
     *
     * @param defer
     */
    public void setDefer(String defer) {
        this.defer_ = defer;
    }

    /**
     * Gets the String property <b>xmlSpace</b>.
     *
     * @return String
     */
    public String getXmlSpace() {
        return (xmlSpace_);
    }

    /**
     * Sets the String property <b>xmlSpace</b>.
     *
     * @param xmlSpace
     */
    public void setXmlSpace(String xmlSpace) {
        this.xmlSpace_ = xmlSpace;
    }

    /**
     * Gets the String property <b>language</b>.
     *
     * @return String
     */
    public String getLanguage() {
        return (language_);
    }

    /**
     * Sets the String property <b>language</b>.
     *
     * @param language
     */
    public void setLanguage(String language) {
        this.language_ = language;
    }

}
