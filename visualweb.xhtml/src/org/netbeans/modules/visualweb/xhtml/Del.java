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
 * <b>Del</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="del">
 *     <ref name="del.attlist"/>
 *     <ref name="Inline.model"/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="del"&gt;
 *     &lt;ref name="del.attlist"/&gt;
 *     &lt;ref name="Inline.model"/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:09 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Del {
    private String cite_;
    private String datetime_;
    /**
     * Creates a <code>Del</code>.
     *
     */
    public Del() {
    }
    /**
     * Gets the String property <b>cite</b>.
     *
     * @return String
     */
    public String getCite() {
        return (cite_);
    }
    /**
     * Sets the String property <b>cite</b>.
     *
     * @param cite
     */
    public void setCite(String cite) {
        this.cite_ = cite;
    }
    /**
     * Gets the String property <b>datetime</b>.
     *
     * @return String
     */
    public String getDatetime() {
        return (datetime_);
    }
    /**
     * Sets the String property <b>datetime</b>.
     *
     * @param datetime
     */
    public void setDatetime(String datetime) {
        this.datetime_ = datetime;
    }
}
