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
 * <b>Param</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="param">
 *     <ref name="param.attlist"/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="param"&gt;
 *     &lt;ref name="param.attlist"/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Param {
    public static final String VALUETYPE_DATA = "data";
    public static final String VALUETYPE_REF = "ref";
    public static final String VALUETYPE_OBJECT = "object";

    private String id_;
    private String name_;
    private String value_;
    private String valuetype_;
    private String type_;

    /**
     * Creates a <code>Param</code>.
     *
     */
    public Param() {
        name_ = "";
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
     * Gets the String property <b>value</b>.
     *
     * @return String
     */
    public String getValue() {
        return (value_);
    }

    /**
     * Sets the String property <b>value</b>.
     *
     * @param value
     */
    public void setValue(String value) {
        this.value_ = value;
    }

    /**
     * Gets the String property <b>valuetype</b>.
     *
     * @return String
     */
    public String getValuetype() {
        return (valuetype_);
    }

    /**
     * Sets the String property <b>valuetype</b>.
     *
     * @param valuetype
     */
    public void setValuetype(String valuetype) {
        this.valuetype_ = valuetype;
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

}
