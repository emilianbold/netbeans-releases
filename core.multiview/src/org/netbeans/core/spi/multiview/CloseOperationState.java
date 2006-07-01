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

import javax.swing.Action;


/**
 * instances of this class describe the MultiViewElement's state when the component is
 * about to be closed. (See {@link org.netbeans.core.spi.multiview.MultiViewElement#canCloseElement})
 * New instances are created by {@link org.netbeans.core.spi.multiview.MultiViewFactory#createUnsafeCloseState} factory method.
 */
public final class CloseOperationState {

    /**
     * Singleton instance of a close operation state, to be used whenever {@link org.netbeans.core.spi.multiview.MultiViewElement} is in consistent state
     * and can be safely closed.
     */
    public static final CloseOperationState STATE_OK = MultiViewFactory.createSafeCloseState();
    
    private boolean canClose;
    private String id;
    private Action proceedAction;
    private Action discardAction;
    
    
    CloseOperationState(boolean close, String warningId, Action proceed, Action discard) {
        canClose = close;
        proceedAction = proceed;
        discardAction = discard;
        id = warningId;
    }
    
    /**
     * The return value denotes wheather the {@link org.netbeans.core.spi.multiview.MultiViewElement} can be closed or not.
     * @return can close element or not
     */
    
    public boolean canClose() {
        return canClose;
    }
    
    /**
     * A preferably unique id of the reason why the element cannot be closed.
     * {@link org.netbeans.core.spi.multiview.CloseOperationHandler} implementation can use it when deciding about UI shown or action taken.
     */
    
    public String getCloseWarningID() {
        return id;
    }
    
    /**
     * Action is used when user wants to complete the closing of the component without loosing changed data.
     * Used by {@link org.netbeans.core.spi.multiview.CloseOperationHandler}.
     * @return action which will be triggered when user confirms changes
     */
    public Action getProceedAction() {
        return proceedAction;
    }

    /**
     * Action is used when user wants to complete the closing of the component and discard any changes.
     * Used by {@link org.netbeans.core.spi.multiview.CloseOperationHandler}.
     * @return action which will be triggered when user discards changes
     */
    
    public Action getDiscardAction() {
        return discardAction;
    }
    
    
}

