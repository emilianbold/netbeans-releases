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
 * Always indicates a state change, not just a return to the same state.
 * @author Jesse Glick
 * @see TwoWaySupport
 */
public abstract class TwoWayEvent extends EventObject {
    
    private TwoWayEvent(TwoWaySupport s) {
        super(s);
        assert s != null;
    }
    
    /**
     * Get the associated two-way support.
     * @return the support
     */
    public TwoWaySupport getTwoWaySupport() {
        return (TwoWaySupport)getSource();
    }
    
    /**
     * Event indicating a derived value has been produced.
     */
    public static final class Derived extends TwoWayEvent {
        
        private final Object oldValue, newValue, underlyingDelta;
        
        Derived(TwoWaySupport s, Object oldValue, Object newValue, Object underlyingDelta) {
            super(s);
            this.oldValue = oldValue;
            assert newValue != null;
            this.newValue = newValue;
            this.underlyingDelta = underlyingDelta;
        }

        /**
         * Get the old value of the derived model.
         * @return the old value, or null if it was never calculated
         */
        public Object getOldValue() {
            return oldValue;
        }
        
        /**
         * Get the new value of the derived model.
         * @return the new value
         */
        public Object getNewValue() {
            return newValue;
        }
        
        /**
         * Get the change to the underlying model that triggered this derivation.
         * Only applicable in case the derived model had been invalidated and
         * was stale before this derivation.
         * @return the invalidating change to the underlying model, or null if
         *         the derived model is simply being computed for the first time
         */
        public Object getUnderlyingDelta() {
            return underlyingDelta;
        }
        
    }
    
    /**
     * Event indicating a derived model has been invalidated.
     */
    public static final class Invalidated extends TwoWayEvent {
        
        private final Object oldValue, underlyingDelta;
        
        Invalidated(TwoWaySupport s, Object oldValue, Object underlyingDelta) {
            super(s);
            assert oldValue != null;
            this.oldValue = oldValue;
            assert underlyingDelta != null;
            this.underlyingDelta = underlyingDelta;
        }
        
        /**
         * Get the old value of the derived model that is now invalid.
         * @return the old value
         */
        public Object getOldValue() {
            return oldValue;
        }
        
        /**
         * Get the change to the underlying model that triggered this invalidation.
         * @return the invalidating change to the underlying model
         */
        public Object getUnderlyingDelta() {
            return underlyingDelta;
        }
        
    }
    
    /**
     * Event indicating the derived model was changed and the underlying model recreated.
     */
    public static final class Recreated extends TwoWayEvent {
        
        private final Object oldValue, newValue, derivedDelta;
        
        Recreated(TwoWaySupport s, Object oldValue, Object newValue, Object derivedDelta) {
            super(s);
            assert oldValue != null;
            this.oldValue = oldValue;
            assert newValue != null;
            this.newValue = newValue;
            assert derivedDelta != null;
            this.derivedDelta = derivedDelta;
        }
        
        /**
         * Get the old value of the derived model that is now invalid.
         * @return the old value
         */
        public Object getOldValue() {
            return oldValue;
        }
        
        /**
         * Get the new value of the derived model.
         * @return the new value
         */
        public Object getNewValue() {
            return newValue;
        }
        
        /**
         * Get the change to the derived model that should be applied to the underlying
         * model as well.
         * @return the delta to the derived model
         */
        public Object getDerivedDelta() {
            return derivedDelta;
        }
        
    }
    
    /**
     * Event indicating changes in the underlying model were clobbered by changes to
     * the derived model.
     */
    public static final class Clobbered extends TwoWayEvent {
        
        private final Object oldValue, newValue, derivedDelta;
        
        Clobbered(TwoWaySupport s, Object oldValue, Object newValue, Object derivedDelta) {
            super(s);
            this.oldValue = oldValue;
            assert newValue != null;
            this.newValue = newValue;
            assert derivedDelta != null;
            this.derivedDelta = derivedDelta;
        }
        
        /**
         * Get the old value of the derived model that is now invalid.
         * @return the old value, or null if it was never calculated
         */
        public Object getOldValue() {
            return oldValue;
        }
        
        /**
         * Get the new value of the derived model.
         * @return the new value
         */
        public Object getNewValue() {
            return newValue;
        }
        
        /**
         * Get the change to the derived model that should be applied to the underlying
         * model as well whether it is applicable or not.
         * @return the delta to the derived model
         */
        public Object getDerivedDelta() {
            return derivedDelta;
        }
        
    }

    /**
     * Event indicating the reference to the derived model was garbage collected.
     */
    public static final class Forgotten extends TwoWayEvent {
        
        Forgotten(TwoWaySupport s) {
            super(s);
        }
        
    }
    
    /**
     * Event indicating an attempted derivation failed with an exception.
     * The underlying model is thus considered to be in an inconsistent state.
     */
    public static final class Broken extends TwoWayEvent {
        
        private final Object oldValue, underlyingDelta;
        
        private final Exception exception;
        
        Broken(TwoWaySupport s, Object oldValue, Object underlyingDelta, Exception exception) {
            super(s);
            this.oldValue = oldValue;
            this.underlyingDelta = underlyingDelta;
            assert exception != null;
            this.exception = exception;
        }
        
        /**
         * Get the old value of the derived model that is now invalid.
         * @return the old value, or null if it was never calculated
         */
        public Object getOldValue() {
            return oldValue;
        }
        
        /**
         * Get the change to the underlying model that triggered this derivation.
         * Only applicable in case the derived model had been invalidated and
         * was stale before this derivation.
         * @return the invalidating change to the underlying model, or null if
         *         the derived model is simply being computed for the first time
         */
        public Object getUnderlyingDelta() {
            return underlyingDelta;
        }
        
        /**
         * Get the exception encountered when trying to derive a new model.
         * @return the exception that prevented a new derived model from being created
         */
        public Exception getException() {
            return exception;
        }
        
    }
    
}
