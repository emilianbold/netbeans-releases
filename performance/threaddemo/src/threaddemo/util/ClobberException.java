/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
