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

import java.util.EventObject;

/**
 * Event indicating something happened to a two-way support.
 * @author Jesse Glick
 * @see TwoWaySupport
 */
public final class TwoWayEvent extends EventObject {
    
    public static final int DERIVED = 0;
    public static final int INVALIDATED = 1;
    public static final int RECREATED = 2;
    public static final int CLOBBERED = 3;
    
    private final int type;
    private final boolean firstDerivation;
    private final Object delta;
    
    TwoWayEvent(TwoWaySupport s, int type, boolean firstDerivation, Object delta) {
        super(s);
        this.type = type;
        this.firstDerivation = firstDerivation;
        this.delta = delta;
    }
    
    public TwoWaySupport getTwoWaySupport() {
        return (TwoWaySupport)getSource();
    }
    
    public int getType() {
        return type;
    }
    
    public boolean isPreviouslyDerived() {
        if (type != DERIVED && type != CLOBBERED) throw new IllegalArgumentException();
        return !firstDerivation;
    }
    
    public Object getDelta() {
        return delta;
    }
    
}
