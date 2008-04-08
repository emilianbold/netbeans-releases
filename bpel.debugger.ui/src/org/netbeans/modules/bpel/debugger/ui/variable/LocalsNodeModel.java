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

package org.netbeans.modules.bpel.debugger.ui.variable;

import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.ui.util.VariablesUtil;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class LocalsNodeModel implements NodeModel, Constants {
    private BpelDebugger myDebugger;
    private VariablesUtil myHelper;
    
    public LocalsNodeModel(
            final ContextProvider contextProvider) {
        
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
        myHelper = new VariablesUtil(myDebugger);
    }
    /**{@inheritDoc}*/
    public String getDisplayName(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return NbBundle.getMessage(
                    LocalsNodeModel.class, 
                    "CTL_Variable_Column_Name"); // NOI18N
        }
        
        if (object instanceof LocalsTreeModel.Dummy) {
            return NbBundle.getMessage(
                    LocalsNodeModel.class, 
                    "CTL_Empty_Model"); // NOI18N
        }
        
        return myHelper.getDisplayName(object);
    }
    
    /**{@inheritDoc}*/
    public String getShortDescription(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return NbBundle.getMessage(
                LocalsNodeModel.class, 
                "CTL_Variable_Column_Name_Tooltip"); // NOI18N
        }
        
        return getDisplayName(object);
    }
    
    /**{@inheritDoc}*/
    public String getIconBase(
            final Object object) throws UnknownTypeException {
        
        if (object instanceof LocalsTreeModel.Dummy) {
            return null;
        }
        
        return myHelper.getIconBase(object);
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
