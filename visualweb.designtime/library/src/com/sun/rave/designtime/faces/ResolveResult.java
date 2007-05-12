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

package com.sun.rave.designtime.faces;

import com.sun.rave.designtime.DesignBean;

/**
 * This class wraps the return value from FacesDesignContext.resolveBindingExprToBean(String expr).
 * The passed expression will be resolved to the deepest DesignBean instance within the context.
 * Any remaining expression will be returned in the 'remainder' property of this object.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see FacesDesignContext#resolveBindingExprToBean(String)
 */
public class ResolveResult {

    /**
     * Constructs a ResolveResult with the specified DesignBean and remainder expression.
     *
     * @param designBean The deepest resolved DesignBean
     * @param remainder The remaining expression from the resolving process
     */
    public ResolveResult(DesignBean designBean, String remainder) {
        this.designBean = designBean;
        this.remainder = remainder;
    }

    /**
     * Constructs a ResolveResult with the specified remainder expression.
     *
     * @param remainder The remaining expression from the resolving process
     */
    public ResolveResult(String remainder) {
        this.designBean = null;
        this.remainder = remainder;
    }

    /**
     * protected storage for the 'designBean' property
     */
    protected DesignBean designBean;

    /**
     * Rertuns the deepest resolved DesignBean
     *
     * @return The deepest resolved DesignBean
     */
    public DesignBean getDesignBean() {
        return this.designBean;
    }

    /**
     * protected storage for the 'remainder' property
     */
    protected String remainder;

    /**
     * Rertuns the remaining expression that did could not be resolved to a DesignBean
     *
     * @return The remaining expression that did could not be resolved to a DesignBean
     */
    public String getRemainder() {
        return this.remainder;
    }

    public String toString() {
        return "[RR bean:[" + designBean + "] \"" + remainder + "\"]"; // NOI18N
    }
}
