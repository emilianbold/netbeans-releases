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
package com.sun.rave.web.ui.model;


/**
 * <p>Specialized version of {@link Markup} that automatically surrounds
 * any accumulated markup in this element with the required prolog and
 * epilogue strings for an embedded script element.</p>
 */

public class ScriptMarkup extends Markup {


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The CDATA wrapping flag for this markup.</p>
     */
    private boolean cdata = false;


    // ------------------------------------------------------------- Properties


    /**
     * <p>Return the current state of CDATA wrapping for this markup.</p>
     */
    public boolean isCdata() {

        return this.cdata;

    }


    /**
     * <p>Set the new state of CDATA wrapping for this markup.</p>
     *
     * @param cdata New wrapping flag
     */
    public void setCdata(boolean cdata) {

        this.cdata = cdata;

    }



    /**
     * <p>Return the accumulated markup for this element, surrounded by the
     * required prolog and epilog strings for an embedded script element.</p>
     */
    public String getMarkup() {

        StringBuffer sb = new StringBuffer
            ("<script type=\"text/javascript\">"); //NOI18N
        if (isCdata()) {
            sb.append("<![CDATA["); //NOI18N
        }
        sb.append("\n"); //NOI18N
        sb.append(super.getMarkup());
        sb.append("\n"); //NOI18N
        if (isCdata()) {
            sb.append("]]>"); //NOI18N
        }
        sb.append("</script>\n"); //NOI18N
        return sb.toString();

    }


}
