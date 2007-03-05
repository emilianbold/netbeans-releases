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
package com.sun.rave.web.ui.component;


public class ThemeLinks extends ThemeLinksBase {
    /**
     * Holds value of property javaScript.
     */
    private boolean javaScript = true;

    /**
     * Getter for property javaScript.
     * @return Value of property javaScript.
     */
    public boolean isJavaScript() {

        return this.javaScript;
    }

    /**
     * Setter for property javaScript.
     * @param javaScript New value of property javaScript.
     */
    public void setJavaScript(boolean javaScript) {

        this.javaScript = javaScript;
    }

    /**
     * Holds value of property styleSheetLink.
     */
    private boolean styleSheetLink = true;

    /**
     * Getter for property styleSheetLink.
     * @return Value of property styleSheetLink.
     */
    public boolean isStyleSheetLink() {

        return this.styleSheetLink;
    }

    /**
     * Setter for property styleSheetLink.
     * @param styleSheetLink New value of property styleSheetLink.
     */
    public void setStyleSheetLink(boolean styleSheetLink) {

        this.styleSheetLink = styleSheetLink;
    }
    
}
