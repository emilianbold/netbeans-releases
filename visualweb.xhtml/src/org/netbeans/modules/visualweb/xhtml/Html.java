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
 * <b>Html</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="html">
 *       <ref name="html.attlist"/>
 *       <ref name="head"/>
 *       <ref name="frameset"/>
 *     </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="html"&gt;
 *       &lt;ref name="html.attlist"/&gt;
 *       &lt;ref name="head"/&gt;
 *       &lt;ref name="frameset"/&gt;
 *     &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Html {
    public static final String DIR_LTR = "ltr";
    public static final String DIR_RTL = "rtl";

    private String version_;
    private String xmlLang_;
    private String lang_;
    private String dir_;
    private Head head_;
    private IFramesetChoice frameset_;

    /**
     * Creates a <code>Html</code>.
     *
     */
    public Html() {
    }

    /**
     * Gets the String property <b>version</b>.
     *
     * @return String
     */
    public String getVersion() {
        return (version_);
    }

    /**
     * Sets the String property <b>version</b>.
     *
     * @param version
     */
    public void setVersion(String version) {
        this.version_ = version;
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
     * Gets the Head property <b>head</b>.
     *
     * @return Head
     */
    public Head getHead() {
        return (head_);
    }

    /**
     * Sets the Head property <b>head</b>.
     *
     * @param head
     */
    public void setHead(Head head) {
        this.head_ = head;
    }

    /**
     * Gets the IFramesetChoice property <b>frameset</b>.
     *
     * @return IFramesetChoice
     */
    public IFramesetChoice getFrameset() {
        return (frameset_);
    }

    /**
     * Sets the IFramesetChoice property <b>frameset</b>.
     *
     * @param frameset
     */
    public void setFrameset(IFramesetChoice frameset) {
        this.frameset_ = frameset;
    }

}
