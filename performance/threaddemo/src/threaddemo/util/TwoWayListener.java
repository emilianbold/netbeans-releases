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

import java.util.EventListener;

/**
 * Listener for changes in the status of a two-way support.
 * @author Jesse Glick
 * @see TwoWaySupport
 */
public interface TwoWayListener extends EventListener {
    
    /**
     * Called when a new derived value has been produced.
     * May have been a result of a synchronous or asynchronous derivation.
     * @param evt the associated event with more information
     */
    void derived(TwoWayEvent.Derived evt);
    
    /**
     * Called when a derived model has been invalidated.
     * @param evt the associated event with more information
     */
    void invalidated(TwoWayEvent.Invalidated evt);
    
    /**
     * Called when the derived model was changed and the underlying model recreated.
     * @param evt the associated event with more information
     */
    void recreated(TwoWayEvent.Recreated evt);
    
    /**
     * Called when changes in the underlying model were clobbered by changes to
     * the derived model.
     * @param evt the associated event with more information
     */
    void clobbered(TwoWayEvent.Clobbered evt);
    
    /**
     * Called when the reference to the derived model was garbage collected.
     * @param evt the associated event
     */
    void forgotten(TwoWayEvent.Forgotten evt);
    
    /**
     * Called when an attempted derivation failed with an exception.
     * The underlying model is thus considered to be in an inconsistent state.
     * @param evt the associated event with more information
     */
    void broken(TwoWayEvent.Broken evt);
    
}
