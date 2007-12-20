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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import org.openide.nodes.Node;

/**
 * A Cookie that indicates the direction of the operation containing
 * this cookie in its Lookup. All operations have a direction associated
 * with them, either pointing to the right, or pointing to the left.
 *
 * @author  Nathan Fiedler
 */
public class DirectionCookie implements Node.Cookie {
    /** True if this cookie represents a right-sided operation. */
    private boolean rightSided;

    /**
     * Creates a new instance of DirectionCookie.
     *
     * @param  rightSided  true if this is a right-sided cookie, false for left.
     */
    public DirectionCookie(boolean rightSided) {
        this.rightSided = rightSided;
    }

    /**
     * Returns true to indicate that the associated operation is
     * left-sided, and false for right-sided. This merely returns the
     * opposite of whatever the isRightSided() method returns.
     *
     * @return  true if this is a left-sided cookie.
     */
    public boolean isLeftSided() {
        return !rightSided;
    }

    /**
     * Returns true to indicate that the associated operation is
     * right-sided, and false for left-sided.
     *
     * @return  true if this is a right-sided cookie.
     */
    public boolean isRightSided() {
        return rightSided;
    }

    /**
     * Set the right-sided value for this cookie.
     *
     * @param  rightSided  true if this is a right-sided cookie, false for left.
     */
    public void setRightSided(boolean rightSided) {
        this.rightSided = rightSided;
    }
}
