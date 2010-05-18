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
 * <b>Table</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="table">
 *       <ref name="table.attlist"/>
 *       <optional>
 * 	<ref name="caption"/>
 *       </optional>
 *       <choice>
 * 	<zeroOrMore>
 * 	  <ref name="col"/>
 * 	</zeroOrMore>
 * 	<zeroOrMore>
 * 	  <ref name="colgroup"/>
 * 	</zeroOrMore>
 *       </choice>
 *       <choice>
 * 	<group>
 * 	  <optional>
 * 	    <ref name="thead"/>
 * 	  </optional>
 * 	  <optional>
 * 	    <ref name="tfoot"/>
 * 	  </optional>
 * 	  <oneOrMore>
 * 	    <ref name="tbody"/>
 * 	  </oneOrMore>
 * 	</group>
 * 	<oneOrMore>
 * 	  <ref name="tr"/>
 * 	</oneOrMore>
 *       </choice>
 *     </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="table"&gt;
 *       &lt;ref name="table.attlist"/&gt;
 *       &lt;optional&gt;
 * 	&lt;ref name="caption"/&gt;
 *       &lt;/optional&gt;
 *       &lt;choice&gt;
 * 	&lt;zeroOrMore&gt;
 * 	  &lt;ref name="col"/&gt;
 * 	&lt;/zeroOrMore&gt;
 * 	&lt;zeroOrMore&gt;
 * 	  &lt;ref name="colgroup"/&gt;
 * 	&lt;/zeroOrMore&gt;
 *       &lt;/choice&gt;
 *       &lt;choice&gt;
 * 	&lt;group&gt;
 * 	  &lt;optional&gt;
 * 	    &lt;ref name="thead"/&gt;
 * 	  &lt;/optional&gt;
 * 	  &lt;optional&gt;
 * 	    &lt;ref name="tfoot"/&gt;
 * 	  &lt;/optional&gt;
 * 	  &lt;oneOrMore&gt;
 * 	    &lt;ref name="tbody"/&gt;
 * 	  &lt;/oneOrMore&gt;
 * 	&lt;/group&gt;
 * 	&lt;oneOrMore&gt;
 * 	  &lt;ref name="tr"/&gt;
 * 	&lt;/oneOrMore&gt;
 *       &lt;/choice&gt;
 *     &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Table {
    public static final String FRAME_VOID = "void";
    public static final String FRAME_ABOVE = "above";
    public static final String FRAME_BELOW = "below";
    public static final String FRAME_HSIDES = "hsides";
    public static final String FRAME_LHS = "lhs";
    public static final String FRAME_RHS = "rhs";
    public static final String FRAME_VSIDES = "vsides";
    public static final String FRAME_BOX = "box";
    public static final String FRAME_BORDER = "border";
    public static final String RULES_NONE = "none";
    public static final String RULES_GROUPS = "groups";
    public static final String RULES_ROWS = "rows";
    public static final String RULES_COLS = "cols";
    public static final String RULES_ALL = "all";
    public static final String DIR_LTR = "ltr";
    public static final String DIR_RTL = "rtl";
    public static final String ALIGN_LEFT = "left";
    public static final String ALIGN_ALL = "all";
    public static final String ALIGN_RIGHT = "right";
    public static final String ALIGN_NONE = "none";
    private String width_;
    private String border_;
    private String frame_;
    private String name_;
    private String rules_;
    private String cellspacing_;
    private String cellpadding_;
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
    private String summary_;
    private String align_;
    private String bgcolor_;
    private Caption caption_;
    /**
     * Creates a <code>Table</code>.
     *
     */
    public Table() {
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
     * Gets the String property <b>frame</b>.
     *
     * @return String
     */
    public String getFrame() {
        return (frame_);
    }
    /**
     * Sets the String property <b>frame</b>.
     *
     * @param frame
     */
    public void setFrame(String frame) {
        this.frame_ = frame;
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
     * Gets the String property <b>rules</b>.
     *
     * @return String
     */
    public String getRules() {
        return (rules_);
    }
    /**
     * Sets the String property <b>rules</b>.
     *
     * @param rules
     */
    public void setRules(String rules) {
        this.rules_ = rules;
    }
    /**
     * Gets the String property <b>cellspacing</b>.
     *
     * @return String
     */
    public String getCellspacing() {
        return (cellspacing_);
    }
    /**
     * Sets the String property <b>cellspacing</b>.
     *
     * @param cellspacing
     */
    public void setCellspacing(String cellspacing) {
        this.cellspacing_ = cellspacing;
    }
    /**
     * Gets the String property <b>cellpadding</b>.
     *
     * @return String
     */
    public String getCellpadding() {
        return (cellpadding_);
    }
    /**
     * Sets the String property <b>cellpadding</b>.
     *
     * @param cellpadding
     */
    public void setCellpadding(String cellpadding) {
        this.cellpadding_ = cellpadding;
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
    public String getStyleClass() {
        return classValue_;
    }
    /**
     * Sets the String property <b>classValue</b>.
     *
     * @param classValue
     */
    public void setStyleClass(String classValue) {
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
     * Gets the String property <b>summary</b>.
     *
     * @return String
     */
    public String getSummary() {
        return (summary_);
    }
    /**
     * Sets the String property <b>summary</b>.
     *
     * @param summary
     */
    public void setSummary(String summary) {
        this.summary_ = summary;
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
     * Gets the Caption property <b>caption</b>.
     *
     * @return Caption
     */
    public Caption getCaption() {
        return (caption_);
    }
    /**
     * Sets the Caption property <b>caption</b>.
     *
     * @param caption
     */
    public void setCaption(Caption caption) {
        this.caption_ = caption;
    }
}
