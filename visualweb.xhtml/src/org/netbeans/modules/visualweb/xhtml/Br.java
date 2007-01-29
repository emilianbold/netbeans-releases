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
 * <b>Br</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="br">
 *     <ref name="br.attlist"/>
 *     <empty/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="br"&gt;
 *     &lt;ref name="br.attlist"/&gt;
 *     &lt;empty/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:09 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Br {
    public static final String CLEAR_LEFT = "left";
    public static final String CLEAR_ALL = "all";
    public static final String CLEAR_RIGHT = "right";
    public static final String CLEAR_NONE = "none";
    private String id_;
    private String classValue_;
    private String title_;
    private String style_;
    private String clear_;
    /**
     * Creates a <code>Br</code>.
     *
     */
    public Br() {
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
     * Gets the String property <b>clear</b>.
     *
     * @return String
     */
    public String getClear() {
        return (clear_);
    }
    /**
     * Sets the String property <b>clear</b>.
     *
     * @param clear
     */
    public void setClear(String clear) {
        this.clear_ = clear;
    }
}
