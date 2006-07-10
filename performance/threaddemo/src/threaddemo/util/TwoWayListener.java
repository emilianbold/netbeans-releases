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

import java.util.EventListener;

/**
 * Listener for changes in the status of a two-way support.
 * @author Jesse Glick
 * @see TwoWaySupport
 */
public interface TwoWayListener<DM, UMD, DMD> extends EventListener {

    /**
     * Called when a new derived value has been produced.
     * May have been a result of a synchronous or asynchronous derivation.
     * @param evt the associated event with more information
     */
    void derived(TwoWayEvent.Derived<DM, UMD, DMD> evt);

    /**
     * Called when a derived model has been invalidated.
     * @param evt the associated event with more information
     */
    void invalidated(TwoWayEvent.Invalidated<DM, UMD, DMD> evt);
    
    /**
     * Called when the derived model was changed and the underlying model recreated.
     * @param evt the associated event with more information
     */
    void recreated(TwoWayEvent.Recreated<DM, UMD, DMD> evt);
    
    /**
     * Called when changes in the underlying model were clobbered by changes to
     * the derived model.
     * @param evt the associated event with more information
     */
    void clobbered(TwoWayEvent.Clobbered<DM, UMD, DMD> evt);
    
    /**
     * Called when the reference to the derived model was garbage collected.
     * @param evt the associated event
     */
    void forgotten(TwoWayEvent.Forgotten<DM, UMD, DMD> evt);
    
    /**
     * Called when an attempted derivation failed with an exception.
     * The underlying model is thus considered to be in an inconsistent state.
     * @param evt the associated event with more information
     */
    void broken(TwoWayEvent.Broken<DM, UMD, DMD> evt);
    
}
