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
 * <b>Meta</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="meta">
 *     <ref name="meta.attlist"/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="meta"&gt;
 *     &lt;ref name="meta.attlist"/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Meta {
    public static final String DIR_LTR = "ltr";
    public static final String DIR_RTL = "rtl";

    private String xmlLang_;
    private String lang_;
    private String dir_;
    private String httpEquiv_;
    private String name_;
    private String content_;
    private String scheme_;

    /**
     * Creates a <code>Meta</code>.
     *
     */
    public Meta() {
    }

    /**
     * Gets the java.util.Locale property <b>xmlLang</b>.
     *
     * @return java.util.Locale
     */
    public String getXmlLang() {
        return (xmlLang_);
    }

    /**
     * Sets the java.util.Locale property <b>xmlLang</b>.
     *
     * @param xmlLang
     */
    public void setXmlLang(String xmlLang) {
        this.xmlLang_ = xmlLang;
    }

    /**
     * Gets the java.util.Locale property <b>lang</b>.
     *
     * @return java.util.Locale
     */
    public String getLang() {
        return (lang_);
    }

    /**
     * Sets the java.util.Locale property <b>lang</b>.
     *
     * @param lang
     */
    public void setLang(String lang) {
        this.lang_ = lang;
    }

    /**
     * Gets the String property <b>dir</b>.
     *
     * @return String
     */
    public String getDir() {
        return (dir_);
    }

    /**
     * Sets the String property <b>dir</b>.
     *
     * @param dir
     */
    public void setDir(String dir) {
        this.dir_ = dir;
    }

    /**
     * Gets the String property <b>httpEquiv</b>.
     *
     * @return String
     */
    public String getHttpEquiv() {
        return (httpEquiv_);
    }

    /**
     * Sets the String property <b>httpEquiv</b>.
     *
     * @param httpEquiv
     */
    public void setHttpEquiv(String httpEquiv) {
        this.httpEquiv_ = httpEquiv;
    }

    /**
     * Gets the String property <b>name</b>.
     *
     * @return String
     */
    public String getName() {
        return (name_);
    }

    /**
     * Sets the String property <b>name</b>.
     *
     * @param name
     */
    public void setName(String name) {
        this.name_ = name;
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
     * Gets the String property <b>scheme</b>.
     *
     * @return String
     */
    public String getScheme() {
        return (scheme_);
    }

    /**
     * Sets the String property <b>scheme</b>.
     *
     * @param scheme
     */
    public void setScheme(String scheme) {
        this.scheme_ = scheme;
    }

}
