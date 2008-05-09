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

import java.util.Collections;
import java.util.Set;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.01
 */
abstract class Action extends ActionsProviderSupport
    implements PropertyChangeListener
{
    Action(ContextProvider provider, Object action) {
        myAction = action;
        myDebugger = provider.lookupFirst(null, BpelDebugger.class);
        myDebugger.addPropertyChangeListener(BpelDebugger.PROP_STATE, this);
    }
    
    public Set getActions() {
        return Collections.singleton(myAction);
    }

    public void propertyChange(PropertyChangeEvent event) {
        checkEnabled();
    }
    
    public boolean isEnabled(Object action) {
        checkEnabled();
        return super.isEnabled(action);
    }
    
    protected BpelDebugger getDebugger() {
        return myDebugger;
    }

    private void checkEnabled() {
        setEnabled(myAction, true);
    }

    private Object myAction;
    private BpelDebugger myDebugger;
}
