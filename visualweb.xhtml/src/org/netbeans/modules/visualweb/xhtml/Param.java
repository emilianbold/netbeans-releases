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
