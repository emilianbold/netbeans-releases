/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.spi.multiview;


/** 
 * Handles closing of the MultiView component globally. Each opened {@link org.netbeans.core.spi.multiview.MultiViewElement}
 * creates a {@link org.netbeans.core.spi.multiview.CloseOperationState} instance to notify the environment of it's internal state.
 * 
 * @author  Milos Kleint
 */
public interface CloseOperationHandler {

    /**
     * Perform the closeOperation on the opened elements in the multiview topcomponent.
     * Can resolve by itself just based on the states of the elements or ask the user for
     * the decision.
     * @param elements {@link org.netbeans.core.spi.multiview.CloseOperationState} instances of {@link org.netbeans.core.spi.multiview.MultiViewElement}s that cannot be
     * closed and require resolution.
     * @returns true if component can be close, false if it shall remain opened.
     */
    boolean resolveCloseOperation(CloseOperationState[] elements);
    
}