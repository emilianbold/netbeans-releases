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
package org.netbeans.modules.visualweb.designer.cssengine;


/**
 * This class provides a manager for padding-top, padding-left,
 * padding-right and padding-bottom.
 *
 * It's very similar to MarginManager, except margins can be 'auto'
 * and paddings can not.
 *
 * @author Tor Norbye
 */
public class PaddingManager extends NonautoableLengthManager {
    private String property;

    public PaddingManager(String property) {
        this.property = property;
    }

    public boolean isInheritedProperty() {
        return false;
    }

    public String getPropertyName() {
        return property;
    }

    protected int getOrientation() {
        // NOTE: BOTH horizontal and vertical paddings are relative
        // to the WIDTH of the containing block! So in particular,
        // padding-top: 50% is UNRELATED to the height of the containing
        // block! Therefore, I always return a horizontal orientation
        // so we compute relative to the block width.
        // This is specified in the CSS2 spec section 8.3.
        return HORIZONTAL_ORIENTATION;
    }
}
