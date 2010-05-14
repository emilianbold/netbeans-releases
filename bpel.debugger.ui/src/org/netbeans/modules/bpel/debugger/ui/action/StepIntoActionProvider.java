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

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.bpel.debugger.api.Position;
import org.netbeans.modules.bpel.debugger.ui.
  action.BpelActionsProviderSupport.PositionListener;
import org.netbeans.spi.debugger.ContextProvider;


/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.01
 */
public class StepIntoActionProvider extends BpelActionsProviderSupport {

    /**{@inheritDoc}*/
    public StepIntoActionProvider(ContextProvider provider) {
        super(provider, ActionsManager.ACTION_STEP_INTO);
        
        // tie this action into the position listener - josh
        getDebugger().addPropertyChangeListener(new PositionListener(this));
        setEnabled(getDebugger().getCurrentPosition() != null);
    }

    /**{@inheritDoc}*/
    public void doAction(Object action) {
        getDebugger().stepInto();
    }
    
    @Override
    protected void positionChanged(Position oldPosition, Position newPosition) {
        setEnabled(newPosition != null);
    }
}
