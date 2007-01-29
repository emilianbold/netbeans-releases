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
 * <b>Base</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="base">
 *     <ref name="base.attlist"/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="base"&gt;
 *     &lt;ref name="base.attlist"/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Base {
    private String href_;
    private String target_;

    /**
     * Creates a <code>Base</code>.
     *
     */
    public Base() {
        href_ = "";
    }

    /**
     * Gets the String property <b>href</b>.
     *
     * @return String
     */
    public String getHref() {
        return (href_);
    }

    /**
     * Sets the String property <b>href</b>.
     *
     * @param href
     */
    public void setHref(String href) {
        this.href_ = href;
    }

    /**
     * Gets the String property <b>target</b>.
     *
     * @return String
     */
    public String getTarget() {
        return (target_);
    }

    /**
     * Sets the String property <b>target</b>.
     *
     * @param target
     */
    public void setTarget(String target) {
        this.target_ = target;
    }
}
