/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.debug.callstackviewfilterring.actions;

import javax.swing.Action;
import org.netbeans.modules.j2ee.debug.callstackviewfilterring.CallStackFilter;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Libor Kotouc
 */
public class HiddenFramesNodeActionsProvider implements NodeActionsProvider {
    
    /** Creates a new instance of HiddenFramesNodeActionsProvider */
    public HiddenFramesNodeActionsProvider() {
    }

    public void performDefaultAction(Object node) throws UnknownTypeException {
    }

    public Action[] getActions(Object node) throws UnknownTypeException {
        if (!(node instanceof CallStackFilter.HiddenFrames))
            throw new UnknownTypeException (node);
        
        return new Action[0];
    }

    public void removeModelListener(ModelListener l) {
    }

    public void addModelListener(ModelListener l) {
    }
     
}
