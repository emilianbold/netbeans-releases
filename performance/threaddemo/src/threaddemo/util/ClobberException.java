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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.util;

/**
 * Exception thrown when changes to an original model are about to
 * be clobbered by modifications to a derived model, in lieu of
 * clobbering them.
 * @author Jesse Glick
 * @see TwoWaySupport
 */
public final class ClobberException extends RuntimeException {

    private final TwoWaySupport s;

    private final Object oldValue, derivedDelta;

    ClobberException(TwoWaySupport s, Object oldValue, Object derivedDelta) {
        assert s != null;
        this.s = s;
        this.oldValue = oldValue;
        assert derivedDelta != null;
        this.derivedDelta = derivedDelta;
    }
    
    /**
     * Get the associated two-way support.
     * @return the support
     */
    public TwoWaySupport getTwoWaySupport() {
        return s;
    }
    
    /**
     * Get the old value of the derived model before the attempted clobber.
     * @return the old value, or null if it was never calculated
     */
    public Object getOldValue() {
        return oldValue;
    }
    
    /**
     * Get the attempted change to the derived model.
     * @return the derived delta
     */
    public Object getDerivedDelta() {
        return derivedDelta;
    }
    
}
