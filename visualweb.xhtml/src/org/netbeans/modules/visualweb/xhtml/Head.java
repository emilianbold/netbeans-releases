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
