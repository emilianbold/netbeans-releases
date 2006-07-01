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