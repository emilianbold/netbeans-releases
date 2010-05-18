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
 * <b>Td</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="td">
 *       <ref name="td.attlist"/>
 *       <ref name="Flow.model"/>
 *     </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="td"&gt;
 *       &lt;ref name="td.attlist"/&gt;
 *       &lt;ref name="Flow.model"/&gt;
 *     &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Td {
    public static final String DIR_LTR = "ltr";
    public static final String DIR_RTL = "rtl";
    public static final String SCOPE_ROW = "row";
    public static final String SCOPE_COL = "col";
    public static final String SCOPE_ROWGROUP = "rowgroup";
    public static final String SCOPE_COLGROUP = "colgroup";
    public static final String ALIGN_LEFT = "left";
    public static final String ALIGN_CENTER = "center";
    public static final String ALIGN_RIGHT = "right";
    public static final String ALIGN_JUSTIFY = "justify";
    public static final String ALIGN_CHAR = "char";
    public static final String VALIGN_TOP = "top";
    public static final String VALIGN_MIDDLE = "middle";
    public static final String VALIGN_BOTTOM = "bottom";
    public static final String VALIGN_BASELINE = "baseline";
    public static final String NOWRAP_NOWRAP = "nowrap";
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
    private String abbr_;
    private String axis_;
    private String headers_;
    private String scope_;
    private String rowspan_;
    private String colspan_;
    private String align_;
    private String char_;
    private String charoff_;
    private String valign_;
    private String nowrap_;
    private String bgcolor_;
    private String width_;
    private String height_;
    /**
     * Creates a <code>Td</code>.
     *
     */
    public Td() {
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
     * Gets the String property <b>abbr</b>.
     *
     * @return String
     */
    public String getAbbr() {
        return (abbr_);
    }
    /**
     * Sets the String property <b>abbr</b>.
     *
     * @param abbr
     */
    public void setAbbr(String abbr) {
        this.abbr_ = abbr;
    }
    /**
     * Gets the String property <b>axis</b>.
     *
     * @return String
     */
    public String getAxis() {
        return (axis_);
    }
    /**
     * Sets the String property <b>axis</b>.
     *
     * @param axis
     */
    public void setAxis(String axis) {
        this.axis_ = axis;
    }
    /**
     * Gets the String property <b>headers</b>.
     *
     * @return String[]
     */
    public String getHeaders() {

        return headers_;
    }
    /**
     * Sets the String property <b>headers</b>.
     *
     * @param headers
     */
    public void setHeaders(String headers) {

        this.headers_ = headers;
    }
    /**
     * Gets the String property <b>scope</b>.
     *
     * @return String
     */
    public String getScope() {
        return (scope_);
    }
    /**
     * Sets the String property <b>scope</b>.
     *
     * @param scope
     */
    public void setScope(String scope) {
        this.scope_ = scope;
    }
    /**
     * Gets the String property <b>rowspan</b>.
     *
     * @return String
     */
    public String getRowspan() {
        return (rowspan_);
    }
    /**
     * Sets the String property <b>rowspan</b>.
     *
     * @param rowspan
     */
    public void setRowspan(String rowspan) {
        this.rowspan_ = rowspan;
    }
    /**
     * Gets the String property <b>colspan</b>.
     *
     * @return String
     */
    public String getColspan() {
        return (colspan_);
    }
    /**
     * Sets the String property <b>colspan</b>.
     *
     * @param colspan
     */
    public void setColspan(String colspan) {
        this.colspan_ = colspan;
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
     * Gets the String property <b>char</b>.
     *
     * @return String
     */
    public String getChar() {
        return (char_);
    }
    /**
     * Sets the String property <b>char</b>.
     *
     * @param charValue
     */
    public void setChar(String charValue) {
        this.char_ = charValue;
    }
    /**
     * Gets the String property <b>charoff</b>.
     *
     * @return String
     */
    public String getCharoff() {
        return (charoff_);
    }
    /**
     * Sets the String property <b>charoff</b>.
     *
     * @param charoff
     */
    public void setCharoff(String charoff) {
        this.charoff_ = charoff;
    }
    /**
     * Gets the String property <b>valign</b>.
     *
     * @return String
     */
    public String getValign() {
        return (valign_);
    }
    /**
     * Sets the String property <b>valign</b>.
     *
     * @param valign
     */
    public void setValign(String valign) {
        this.valign_ = valign;
    }
    /**
     * Gets the String property <b>nowrap</b>.
     *
     * @return String
     */
    public String getNowrap() {
        return (nowrap_);
    }
    /**
     * Sets the String property <b>nowrap</b>.
     *
     * @param nowrap
     */
    public void setNowrap(String nowrap) {
        this.nowrap_ = nowrap;
    }
    /**
     * Gets the String property <b>bgcolor</b>.
     *
     * @return String
     */
    public String getBgcolor() {
        return (bgcolor_);
    }
    /**
     * Sets the String property <b>bgcolor</b>.
     *
     * @param bgcolor
     */
    public void setBgcolor(String bgcolor) {
        this.bgcolor_ = bgcolor;
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
}
