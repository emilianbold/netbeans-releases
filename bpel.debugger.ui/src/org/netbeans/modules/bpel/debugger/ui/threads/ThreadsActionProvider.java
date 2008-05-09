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

import javax.swing.Action;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.Position;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.pem.ProcessExecutionModel.Branch;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 * @author Vladimir Yaroslavskiy
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class ThreadsActionProvider implements NodeActionsProvider {
    
    private BpelDebugger myDebugger;
    
    private final Action MAKE_CURRENT_ACTION = Models.createAction(
        MAKE_CURRENT_ACTION_NAME,
        new Models.ActionPerformer() {
            public boolean isEnabled(
                    final Object object) {
                
                if (object instanceof Branch) {
                    return canMakeCurrentBranch((Branch) object);
                }
                
                return false;
            }
            
            public void perform(
                    final Object[] objects) {
                
                if (objects[0] instanceof Branch) {
                    makeCurrentBranch((Branch) objects[0]);
                }
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    /**{@inheritDoc}*/
    public ThreadsActionProvider(
            final ContextProvider contextProvider) {
        
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
    }
    
    /**{@inheritDoc}*/
    public void performDefaultAction(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return;
        }
        
        if (object instanceof Branch) {
            makeCurrentBranch((Branch) object);
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public Action[] getActions(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return new Action[0];
        }
        
        if (object instanceof Branch) {
            return new Action[] {
                MAKE_CURRENT_ACTION
            };
        }
        
        throw new UnknownTypeException(object);
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void makeCurrentBranch(
            final Branch branch) {
        
        final ProcessInstance instance = myDebugger.getCurrentProcessInstance();
        
        instance.getProcessExecutionModel().setCurrentBranch(branch.getId());
    }
    
    private boolean canMakeCurrentBranch(
            final Branch branch) {
        final ProcessInstance currentInstance = 
                myDebugger.getCurrentProcessInstance();
        final Position currentPosition = 
                myDebugger.getCurrentPosition();
        
        if (branch.getState() == Branch.State.COMPLETED) {
            return false;
        }
        
        if ((currentInstance == null) || (currentPosition == null)) {
            return false;
        }
        
        final Branch currentBranch =
                currentInstance.getProcessExecutionModel().getCurrentBranch();
        
        if ((currentBranch != null) && currentBranch.equals(branch)) {
            return false;
        }
        
        return true;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String MAKE_CURRENT_ACTION_NAME = NbBundle.getMessage(
          ThreadsActionProvider.class,
          "CTL_Threads_Action_Make_Current"); // NOI18N
}
