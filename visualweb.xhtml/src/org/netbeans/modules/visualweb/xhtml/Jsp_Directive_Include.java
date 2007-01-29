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
 * JavaBean for the "jsp:directive.include" tag
 *
 * @author Tor Norbye
 */
public class Jsp_Directive_Include {

    /**
     * Creates a <code>Jsp_Directive_Include</code>.
     */
    public Jsp_Directive_Include() {
    }

    /**
     * Gets the String property <b>file</b>.
     *
     * @return String
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the String property <b>file</b>.
     *
     * @param file
     */
    public void setFile(String file) {
        this.file = file;
    }

    // Note - there's no style attribute!

    private String file;
}
