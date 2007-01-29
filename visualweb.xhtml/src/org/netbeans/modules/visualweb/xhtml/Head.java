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
 * <b>Head</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="head">
 *     <ref name="head.attlist"/>
 *     <ref name="head.content"/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="head"&gt;
 *     &lt;ref name="head.attlist"/&gt;
 *     &lt;ref name="head.content"/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Head {
    public static final String DIR_LTR = "ltr";
    public static final String DIR_RTL = "rtl";

    private String xmlLang_;
    private String lang_;
    private String dir_;
    private String profile_;
    private Title title_;
    private Base base_;
    private Isindex isindex_;

    /**
     * Creates a <code>Head</code>.
     *
     */
    public Head() {
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
     * Gets the String property <b>profile</b>.
     *
     * @return String
     */
    public String getProfile() {
        return (profile_);
    }

    /**
     * Sets the String property <b>profile</b>.
     *
     * @param profile
     */
    public void setProfile(String profile) {
        this.profile_ = profile;
    }

    /**
     * Gets the Title property <b>title</b>.
     *
     * @return Title
     */
    public Title getTitle() {
        return (title_);
    }

    /**
     * Sets the Title property <b>title</b>.
     *
     * @param title
     */
    public void setTitle(Title title) {
        this.title_ = title;
    }

    /**
     * Gets the Base property <b>base</b>.
     *
     * @return Base
     */
    public Base getBase() {
        return (base_);
    }

    /**
     * Sets the Base property <b>base</b>.
     *
     * @param base
     */
    public void setBase(Base base) {
        this.base_ = base;
    }

    /**
     * Gets the Isindex property <b>isindex</b>.
     *
     * @return Isindex
     */
    public Isindex getIsindex() {
        return (isindex_);
    }

    /**
     * Sets the Isindex property <b>isindex</b>.
     *
     * @param isindex
     */
    public void setIsindex(Isindex isindex) {
        this.isindex_ = isindex;
    }

}
