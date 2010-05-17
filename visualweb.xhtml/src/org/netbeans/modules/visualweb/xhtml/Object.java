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
package org.netbeans.modules.visualweb.xhtml;
/**
 * <b>Object</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="object">
 *     <ref name="object.attlist"/>
 *     <!-- No restrictions on mixed content in TREX. -->
 *     <zeroOrMore>
 *       <ref name="param"/>
 *     </zeroOrMore>
 *     <ref name="Flow.model"/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="object"&gt;
 *     &lt;ref name="object.attlist"/&gt;
 *     &lt;!-- No restrictions on mixed content in TREX. --&gt;
 *     &lt;zeroOrMore&gt;
 *       &lt;ref name="param"/&gt;
 *     &lt;/zeroOrMore&gt;
 *     &lt;ref name="Flow.model"/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Object {
    public static final String DIR_LTR = "ltr";
    public static final String DIR_RTL = "rtl";
    public static final String DECLARE_DECLARE = "declare";
    public static final String ALIGN_TOP = "top";
    public static final String ALIGN_MIDDLE = "middle";
    public static final String ALIGN_BOTTOM = "bottom";
    public static final String ALIGN_LEFT = "left";
    public static final String ALIGN_RIGHT = "right";
    private String id_;
    private String classValue_;
    private String title_;
    private String style_;
    private String xmlLang_;
    private String lang_;
    private String dir_;
    private String onclick_;
    private String ondblclick_;
    private String onmousedown_;
    private String onmouseup_;
    private String onmouseover_;
    private String onmousemove_;
    private String onmouseout_;
    private String onkeypress_;
    private String onkeydown_;
    private String onkeyup_;
    private String declare_;
    private String classid_;
    private String codebase_;
    private String data_;
    private String type_;
    private String codetype_;
    private String archive_;
    private String standby_;
    private String height_;
    private String width_;
    private String name_;
    private String tabindex_;
    private String usemap_;
    private String align_;
    private String border_;
    private String hspace_;
    private String vspace_;

    /**
     * Creates a <code>Object</code>.
     *
     */
    public Object() {
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
     * Gets the String property <b>onclick</b>.
     *
     * @return String
     */
    public String getOnclick() {
        return (onclick_);
    }
    /**
     * Sets the String property <b>onclick</b>.
     *
     * @param onclick
     */
    public void setOnclick(String onclick) {
        this.onclick_ = onclick;
    }
    /**
     * Gets the String property <b>ondblclick</b>.
     *
     * @return String
     */
    public String getOndblclick() {
        return (ondblclick_);
    }
    /**
     * Sets the String property <b>ondblclick</b>.
     *
     * @param ondblclick
     */
    public void setOndblclick(String ondblclick) {
        this.ondblclick_ = ondblclick;
    }
    /**
     * Gets the String property <b>onmousedown</b>.
     *
     * @return String
     */
    public String getOnmousedown() {
        return (onmousedown_);
    }
    /**
     * Sets the String property <b>onmousedown</b>.
     *
     * @param onmousedown
     */
    public void setOnmousedown(String onmousedown) {
        this.onmousedown_ = onmousedown;
    }
    /**
     * Gets the String property <b>onmouseup</b>.
     *
     * @return String
     */
    public String getOnmouseup() {
        return (onmouseup_);
    }
    /**
     * Sets the String property <b>onmouseup</b>.
     *
     * @param onmouseup
     */
    public void setOnmouseup(String onmouseup) {
        this.onmouseup_ = onmouseup;
    }
    /**
     * Gets the String property <b>onmouseover</b>.
     *
     * @return String
     */
    public String getOnmouseover() {
        return (onmouseover_);
    }
    /**
     * Sets the String property <b>onmouseover</b>.
     *
     * @param onmouseover
     */
    public void setOnmouseover(String onmouseover) {
        this.onmouseover_ = onmouseover;
    }
    /**
     * Gets the String property <b>onmousemove</b>.
     *
     * @return String
     */
    public String getOnmousemove() {
        return (onmousemove_);
    }
    /**
     * Sets the String property <b>onmousemove</b>.
     *
     * @param onmousemove
     */
    public void setOnmousemove(String onmousemove) {
        this.onmousemove_ = onmousemove;
    }
    /**
     * Gets the String property <b>onmouseout</b>.
     *
     * @return String
     */
    public String getOnmouseout() {
        return (onmouseout_);
    }
    /**
     * Sets the String property <b>onmouseout</b>.
     *
     * @param onmouseout
     */
    public void setOnmouseout(String onmouseout) {
        this.onmouseout_ = onmouseout;
    }
    /**
     * Gets the String property <b>onkeypress</b>.
     *
     * @return String
     */
    public String getOnkeypress() {
        return (onkeypress_);
    }
    /**
     * Sets the String property <b>onkeypress</b>.
     *
     * @param onkeypress
     */
    public void setOnkeypress(String onkeypress) {
        this.onkeypress_ = onkeypress;
    }
    /**
     * Gets the String property <b>onkeydown</b>.
     *
     * @return String
     */
    public String getOnkeydown() {
        return (onkeydown_);
    }
    /**
     * Sets the String property <b>onkeydown</b>.
     *
     * @param onkeydown
     */
    public void setOnkeydown(String onkeydown) {
        this.onkeydown_ = onkeydown;
    }
    /**
     * Gets the String property <b>onkeyup</b>.
     *
     * @return String
     */
    public String getOnkeyup() {
        return (onkeyup_);
    }
    /**
     * Sets the String property <b>onkeyup</b>.
     *
     * @param onkeyup
     */
    public void setOnkeyup(String onkeyup) {
        this.onkeyup_ = onkeyup;
    }
    /**
     * Gets the String property <b>declare</b>.
     *
     * @return String
     */
    public String getDeclare() {
        return (declare_);
    }
    /**
     * Sets the String property <b>declare</b>.
     *
     * @param declare
     */
    public void setDeclare(String declare) {
        this.declare_ = declare;
    }
    /**
     * Gets the String property <b>classid</b>.
     *
     * @return String
     */
    public String getClassid() {
        return (classid_);
    }
    /**
     * Sets the String property <b>classid</b>.
     *
     * @param classid
     */
    public void setClassid(String classid) {
        this.classid_ = classid;
    }
    /**
     * Gets the String property <b>codebase</b>.
     *
     * @return String
     */
    public String getCodebase() {
        return (codebase_);
    }
    /**
     * Sets the String property <b>codebase</b>.
     *
     * @param codebase
     */
    public void setCodebase(String codebase) {
        this.codebase_ = codebase;
    }
    /**
     * Gets the String property <b>data</b>.
     *
     * @return String
     */
    public String getData() {
        return (data_);
    }
    /**
     * Sets the String property <b>data</b>.
     *
     * @param data
     */
    public void setData(String data) {
        this.data_ = data;
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
     * Gets the String property <b>codetype</b>.
     *
     * @return String
     */
    public String getCodetype() {
        return (codetype_);
    }
    /**
     * Sets the String property <b>codetype</b>.
     *
     * @param codetype
     */
    public void setCodetype(String codetype) {
        this.codetype_ = codetype;
    }
    /**
     * Gets the String property <b>archive</b>.
     *
     * @return String
     */
    public String getArchive() {
        return (archive_);
    }
    /**
     * Sets the String property <b>archive</b>.
     *
     * @param archive
     */
    public void setArchive(String archive) {
        this.archive_ = archive;
    }
    /**
     * Gets the String property <b>standby</b>.
     *
     * @return String
     */
    public String getStandby() {
        return (standby_);
    }
    /**
     * Sets the String property <b>standby</b>.
     *
     * @param standby
     */
    public void setStandby(String standby) {
        this.standby_ = standby;
    }
    /**
     * Gets the String property <b>height</b>.
     *
     * @return String
     */
    public String getHeight() {
        return (height_);
    }
    /**
     * Sets the String property <b>height</b>.
     *
     * @param height
     */
    public void setHeight(String height) {
        this.height_ = height;
    }
    /**
     * Gets the String property <b>width</b>.
     *
     * @return String
     */
    public String getWidth() {
        return (width_);
    }
    /**
     * Sets the String property <b>width</b>.
     *
     * @param width
     */
    public void setWidth(String width) {
        this.width_ = width;
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
     * Gets the String property <b>tabindex</b>.
     *
     * @return String
     */
    public String getTabindex() {
        return (tabindex_);
    }
    /**
     * Sets the String property <b>tabindex</b>.
     *
     * @param tabindex
     */
    public void setTabindex(String tabindex) {
        this.tabindex_ = tabindex;
    }
    /**
     * Gets the String property <b>usemap</b>.
     *
     * @return String
     */
    public String getUsemap() {
        return (usemap_);
    }
    /**
     * Sets the String property <b>usemap</b>.
     *
     * @param usemap
     */
    public void setUsemap(String usemap) {
        this.usemap_ = usemap;
    }
    /**
     * Gets the String property <b>align</b>.
     *
     * @return String
     */
    public String getAlign() {
        return (align_);
    }
    /**
     * Sets the String property <b>align</b>.
     *
     * @param align
     */
    public void setAlign(String align) {
        this.align_ = align;
    }
    /**
     * Gets the String property <b>border</b>.
     *
     * @return String
     */
    public String getBorder() {
        return (border_);
    }
    /**
     * Sets the String property <b>border</b>.
     *
     * @param border
     */
    public void setBorder(String border) {
        this.border_ = border;
    }
    /**
     * Gets the String property <b>hspace</b>.
     *
     * @return String
     */
    public String getHspace() {
        return (hspace_);
    }
    /**
     * Sets the String property <b>hspace</b>.
     *
     * @param hspace
     */
    public void setHspace(String hspace) {
        this.hspace_ = hspace;
    }
    /**
     * Gets the String property <b>vspace</b>.
     *
     * @return String
     */
    public String getVspace() {
        return (vspace_);
    }
    /**
     * Sets the String property <b>vspace</b>.
     *
     * @param vspace
     */
    public void setVspace(String vspace) {
        this.vspace_ = vspace;
    }
}
