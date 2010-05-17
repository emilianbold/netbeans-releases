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

package org.netbeans.modules.bpel.debugger.ui.threads;

import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.Position;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.pem.ProcessExecutionModel.Branch;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 *
 * @author Kirill Sorokin
 */
public class ThreadsNodeModel implements NodeModel, Constants {
    
    private BpelDebugger myDebugger;
    
    public ThreadsNodeModel(
            final ContextProvider contextProvider) {
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
    }
    
    /**{@inheritDoc}*/
    public String getDisplayName(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return NbBundle.getMessage(
                    ThreadsNodeModel.class, 
                    "CTL_Column_Name"); // NOI18N
        }
        
        if (object instanceof ThreadsTreeModel.Dummy) {
            return NbBundle.getMessage(
                    ThreadsNodeModel.class, 
                    "CTL_Empty_Model"); // NOI18N
        }
        
        if (object instanceof Branch) {
            return ((Branch) object).getId();
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public String getShortDescription(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return NbBundle.getMessage(
                ThreadsNodeModel.class, 
                "CTL_Column_Name_Tooltip"); // NOI18N
        }
        
        return getDisplayName(object);
    }
    
    /**{@inheritDoc}*/
    public String getIconBase(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return ""; // NOI18N
        }
        
        if (object instanceof ThreadsTreeModel.Dummy) {
            return null;
        }
        
        if (object instanceof Branch) {
            final ProcessInstance instance = 
                    myDebugger.getCurrentProcessInstance();
            final Position position = 
                    myDebugger.getCurrentPosition();
                    
            if ((instance != null) && (position != null)) {
                final Branch branch = instance.
                        getProcessExecutionModel().getCurrentBranch();
                
                if (((Branch) object).equals(branch)) {
                    return CURRENT_ICON;
                }
            }
            
            if (((Branch) object).getState() == Branch.State.ACTIVE) {
                return ACTIVE_ICON;
            }
            
            if (((Branch) object).getState() == Branch.State.COMPLETED) {
                return COMPLETED_ICON;
            }
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
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String ICONS_ROOT = 
            "org/netbeans/modules/bpel/debugger/ui/" + // NOI18N
            "resources/image/threads/"; // NOI18N
    
    public static final String ACTIVE_ICON =
            ICONS_ROOT + "active"; // NOI18N
    
    public static final String CURRENT_ICON =
            ICONS_ROOT + "current"; // NOI18N
    
    public static final String COMPLETED_ICON =
            ICONS_ROOT + "completed"; // NOI18N
}
