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
 * <b>Iframe</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="iframe">
 *     <ref name="iframe.attlist"/>
 *     <ref name="Flow.model"/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="iframe"&gt;
 *     &lt;ref name="iframe.attlist"/&gt;
 *     &lt;ref name="Flow.model"/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:09 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Iframe {
    public static final String FRAMEBORDER_1 = "1";
    public static final String FRAMEBORDER_0 = "0";
    public static final String SCROLLING_YES = "yes";
    public static final String SCROLLING_NO = "no";
    public static final String SCROLLING_AUTO = "auto";
    private String id_;
    private String classValue_;
    private String title_;
    private String style_;
    private String longdesc_;
    private String src_;
    private String frameborder_;
    private String width_;
    private String height_;
    private String marginwidth_;
    private String marginheight_;
    private String scrolling_;
    private String name_;
    /**
     * Creates a <code>Iframe</code>.
     *
     */
    public Iframe() {
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
     * Gets the String property <b>longdesc</b>.
     *
     * @return String
     */
    public String getLongdesc() {
        return (longdesc_);
    }
    /**
     * Sets the String property <b>longdesc</b>.
     *
     * @param longdesc
     */
    public void setLongdesc(String longdesc) {
        this.longdesc_ = longdesc;
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
     * Gets the String property <b>frameborder</b>.
     *
     * @return String
     */
    public String getFrameborder() {
        return (frameborder_);
    }
    /**
     * Sets the String property <b>frameborder</b>.
     *
     * @param frameborder
     */
    public void setFrameborder(String frameborder) {
        this.frameborder_ = frameborder;
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
    /**
     * Gets the String property <b>marginwidth</b>.
     *
     * @return String
     */
    public String getMarginwidth() {
        return (marginwidth_);
    }
    /**
     * Sets the String property <b>marginwidth</b>.
     *
     * @param marginwidth
     */
    public void setMarginwidth(String marginwidth) {
        this.marginwidth_ = marginwidth;
    }
    /**
     * Gets the String property <b>marginheight</b>.
     *
     * @return String
     */
    public String getMarginheight() {
        return (marginheight_);
    }
    /**
     * Sets the String property <b>marginheight</b>.
     *
     * @param marginheight
     */
    public void setMarginheight(String marginheight) {
        this.marginheight_ = marginheight;
    }
    /**
     * Gets the String property <b>scrolling</b>.
     *
     * @return String
     */
    public String getScrolling() {
        return (scrolling_);
    }
    /**
     * Sets the String property <b>scrolling</b>.
     *
     * @param scrolling
     */
    public void setScrolling(String scrolling) {
        this.scrolling_ = scrolling;
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
}
