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
public abstract class TwoWayEvent extends EventObject {
    
    private TwoWayEvent(TwoWaySupport s) {
        super(s);
    }
    
    public TwoWaySupport getTwoWaySupport() {
        return (TwoWaySupport)getSource();
    }
    
    public static final class Derived extends TwoWayEvent {
        
        private final Object oldValue, newValue, underlyingDelta;
        
        Derived(TwoWaySupport s, Object oldValue, Object newValue, Object underlyingDelta) {
            super(s);
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.underlyingDelta = underlyingDelta;
        }
        
        public Object getOldValue() {
            return oldValue;
        }
        
        public Object getNewValue() {
            return newValue;
        }
        
        public Object getUnderlyingDelta() {
            return underlyingDelta;
        }
        
    }
    
    public static final class Invalidated extends TwoWayEvent {
        
        private final Object oldValue, underlyingDelta;
        
        Invalidated(TwoWaySupport s, Object oldValue, Object underlyingDelta) {
            super(s);
            this.oldValue = oldValue;
        }
        
        public Object getOldValue() {
            return oldValue;
        }
        
        public Object getUnderlyingDelta() {
            return underlyingDelta;
        }
        
    }
    
    public static final class Recreated extends TwoWayEvent {
        
        private final Object oldValue, newValue, derivedDelta;
        
        Recreated(TwoWaySupport s, Object oldValue, Object newValue, Object derivedDelta) {
            super(s);
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.derivedDelta = derivedDelta;
        }
        
        public Object getOldValue() {
            return oldValue;
        }
        
        public Object getNewValue() {
            return newValue;
        }
        
        public Object getDerivedDelta() {
            return derivedDelta;
        }
        
    }
    
    public static final class Clobbered extends TwoWayEvent {
        
        private final Object oldValue, newValue, derivedDelta;
        
        Clobbered(TwoWaySupport s, Object oldValue, Object newValue, Object derivedDelta) {
            super(s);
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.derivedDelta = derivedDelta;
        }
        
        public Object getOldValue() {
            return oldValue;
        }
        
        public Object getNewValue() {
            return newValue;
        }
        
        public Object getDerivedDelta() {
            return derivedDelta;
        }
        
    }
    
    public static final class Forgotten extends TwoWayEvent {
        
        Forgotten(TwoWaySupport s) {
            super(s);
        }
        
    }
    
}
