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
package org.netbeans.modules.bpel.debugger.ui.watch;

import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.variables.NamedValueHost;
import org.netbeans.modules.bpel.debugger.ui.util.VariablesUtil;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.w3c.dom.Node;

/**
 * 
 * @author Vladimir Yaroslavskiy
 * @author Kirill Sorokin
 */
public class WatchesNodeModel implements NodeModel, Constants {
    public static final String WATCH_ICON =
        "org/netbeans/modules/debugger/resources/watchesView/Watch";
    
    final ContextProvider myContextProvider;
    final BpelDebugger myDebugger;
    final VariablesUtil myHelper;
    
    /**{@inheritDoc}*/
    public WatchesNodeModel(
            final ContextProvider contextProvider) {
        
        myContextProvider = contextProvider;
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
        myHelper = new VariablesUtil(myDebugger);
    }
    
    /**{@inheritDoc}*/
    public String getDisplayName(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return NbBundle.getBundle(WatchesNodeModel.class).
                getString ("CTL_Watch_Column_Name_Name");  // NOI18N
        }
        
        if (object instanceof BpelWatch) {
            return ((BpelWatch) object).getExpression();
        }
        
        if (object instanceof NamedValueHost) {
            return myHelper.getDisplayName(object);
        }
        
        if (object instanceof Node) {
            return myHelper.getDisplayName(object);
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public String getShortDescription(
            final Object object) throws UnknownTypeException {
        
        return getDisplayName(object);
    }
    
    /**{@inheritDoc}*/
    public String getIconBase(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return WATCH_ICON;
        }
        
        if (object instanceof BpelWatch) {
            return WATCH_ICON;
        }
        
        if (object instanceof NamedValueHost) {
            return myHelper.getIconBase(object);
        }
        
        if (object instanceof Node) {
            return myHelper.getIconBase(object);
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public void addModelListener(
            final ModelListener listener) {
        // does nothing
    }

    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listener) {
        // does nothing
    }
}
