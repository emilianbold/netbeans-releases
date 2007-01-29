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
 * <b>Style</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="style">
 *     <ref name="style.attlist"/>
 *     <text/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="style"&gt;
 *     &lt;ref name="style.attlist"/&gt;
 *     &lt;text/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Style {
    public static final String DIR_LTR = "ltr";
    public static final String DIR_RTL = "rtl";
    public static final String XMLSPACE_PRESERVE = "preserve";

    private String content_;
    private String title_;
    private String xmlLang_;
    private String lang_;
    private String dir_;
    private String type_;
    private String media_;
    private String xmlSpace_;

    /**
     * Creates a <code>Style</code>.
     *
     */
    public Style() {
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
     * Gets the String property <b>title</b>.
     *
     * @return String
     */
    public String getTitle() {
        return (title_);
    }

    /**
     * Sets the String property <b>title</b>.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title_ = title;
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
     * Gets the String property <b>media</b>.
     *
     * @return String
     */
    public String getMedia() {
        return (media_);
    }

    /**
     * Sets the String property <b>media</b>.
     *
     * @param media
     */
    public void setMedia(String media) {
        this.media_ = media;
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

}
