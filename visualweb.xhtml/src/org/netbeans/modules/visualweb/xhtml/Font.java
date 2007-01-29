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
 * <b>Font</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="font">
 *     <ref name="font.attlist"/>
 *     <ref name="Inline.model"/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="font"&gt;
 *     &lt;ref name="font.attlist"/&gt;
 *     &lt;ref name="Inline.model"/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:09 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Font {
    public static final String DIR_LTR = "ltr";
    public static final String DIR_RTL = "rtl";
    private String id_;
    private String classValue_;
    private String title_;
    private String style_;
    private String xmlLang_;
    private String lang_;
    private String dir_;
    private String size_;
    private String color_;
    private String face_;
    /**
     * Creates a <code>Font</code>.
     *
     */
    public Font() {
    }
    /**
     * Gets the String property <b>id</b>.
     *
     * @return String
     */
    public String getId() {
        return (id_);
    }
    /**
     * Sets the String property <b>id</b>.
     *
     * @param id
     */
    public void setId(String id) {
        this.id_ = id;
    }
    /**
     * Gets the String property <b>classValue</b>.
     *
     * @return String
     */
    public String getClassValue() {
        return classValue_;
    }
    /**
     * Sets the String property <b>classValue</b>.
     *
     * @param classValue
     */
    public void setClassValue(String classValue) {
        this.classValue_ = classValue;
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
     * Gets the String property <b>style</b>.
     *
     * @return String
     */
    public String getStyle() {
        return (style_);
    }
    /**
     * Sets the String property <b>style</b>.
     *
     * @param style
     */
    public void setStyle(String style) {
        this.style_ = style;
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
     * Gets the String property <b>size</b>.
     *
     * @return String
     */
    public String getSize() {
        return (size_);
    }
    /**
     * Sets the String property <b>size</b>.
     *
     * @param sizeValue
     */
    public void setSize(String sizeValue) {
        this.size_ = sizeValue;
    }
    /**
     * Gets the String property <b>color</b>.
     *
     * @return String
     */
    public String getColor() {
        return (color_);
    }
    /**
     * Sets the String property <b>color</b>.
     *
     * @param color
     */
    public void setColor(String color) {
        this.color_ = color;
    }
    /**
     * Gets the String property <b>face</b>.
     *
     * @return String
     */
    public String getFace() {
        return (face_);
    }
    /**
     * Sets the String property <b>face</b>.
     *
     * @param face
     */
    public void setFace(String face) {
        this.face_ = face;
    }
}
