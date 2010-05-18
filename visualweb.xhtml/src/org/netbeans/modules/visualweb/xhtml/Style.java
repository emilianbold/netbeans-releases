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
