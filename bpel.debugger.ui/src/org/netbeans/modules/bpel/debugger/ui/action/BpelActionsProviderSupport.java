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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;

import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.Position;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;



/**
 * Wrapper around ActionsProviderSupport to provide for convenience methods
 * to subclasses.
 *
 * @author Josh Sandusky
 */
public abstract class BpelActionsProviderSupport extends ActionsProviderSupport {
    
    protected ContextProvider mLookupProvider;
    private BpelDebugger mDebugger;
    private Object mAction;
    
    
    public BpelActionsProviderSupport(ContextProvider lookupProvider, Object action) {
        mLookupProvider = lookupProvider;
        mAction = action;
        mDebugger = 
            mLookupProvider.lookupFirst(null, BpelDebugger.class);
        setEnabled(true);
    }
    
    public void setEnabled(boolean isEnabled) {
        super.setEnabled(mAction, isEnabled);
    }
    
    public Set getActions() {
        return Collections.singleton(mAction);
    }
    
    public BpelDebugger getDebugger() {
        return mDebugger;
    }
    
    protected void positionChanged(Position oldPosition, Position newPosition) {
        // by default, do nothing
    }
    
    
    /**
     * Listens on the active break position. If it has changed, the associated
     * action is enabled or disabled.
     */
    static class PositionListener implements PropertyChangeListener {

        private BpelActionsProviderSupport mProvider;
        
        
        public PositionListener(BpelActionsProviderSupport provider) {
            mProvider = provider;
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName() == BpelDebugger.PROP_CURRENT_POSITION) {
                mProvider.positionChanged(
                        (Position) e.getOldValue(),
                        (Position) e.getNewValue());
            }
        }
    }
}
