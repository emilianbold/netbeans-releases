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

package org.netbeans.modules.bpel.debugger.ui.process;

import javax.swing.Action;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.BpelProcess;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
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
public class ProcessesActionProvider implements NodeActionsProvider {
    
    private BpelDebugger myDebugger;
    
    
    private final Action MAKE_CURRENT_ACTION = Models.createAction(
        MAKE_CURRENT_ACTION_NAME,
        new Models.ActionPerformer() {
            public boolean isEnabled(
                    final Object object) {
                
                return canMakeCurrentProcessInstance(object);
            }
            
            public void perform(
                    final Object[] objects) {
                
                makeCurrentProcessInstance(objects[0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    private final Action RESUME_ACTION = Models.createAction(
        RESUME_ACTION_NAME,
        new Models.ActionPerformer() {
            public boolean isEnabled(
                    final Object object) {
                
                if (object instanceof ProcessInstance) {
                    return ((ProcessInstance) object).getState() == 
                            ProcessInstance.STATE_SUSPENDED;
                }
                
                return false;
            }
            
            public void perform(
                    final Object[] objects) {
                
                if (objects[0] instanceof ProcessInstance) {
                    ((ProcessInstance) objects[0]).resume();
                }
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    private final Action TERMINATE_ACTION = Models.createAction(
        TERMINATE_ACTION_NAME,
        new Models.ActionPerformer() {
            public boolean isEnabled(
                    final Object object) {
                
                if (object instanceof ProcessInstance) {
                    switch (((ProcessInstance) object).getState()) {
                        case ProcessInstance.STATE_COMPLETED:
                        case ProcessInstance.STATE_FAILED:
                        case ProcessInstance.STATE_TERMINATED:
                            return false;
                        default :
                            return true;
                    }
                }
                
                return false;
            }
            
            public void perform(
                    final Object[] objects) {
                
                if (objects[0] instanceof ProcessInstance) {
                    ((ProcessInstance) objects[0]).terminate();
                }
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    /**{@inheritDoc}*/
    public ProcessesActionProvider(
            final ContextProvider contextProvider) {
        
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
    }
    
    /**{@inheritDoc}*/
    public void performDefaultAction(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return;
        }
        
        if (object instanceof BpelProcess) {
            return;
        }
        
        if (object instanceof ProcessInstance) {
            if (canMakeCurrentProcessInstance(object)) {
                makeCurrentProcessInstance(object);
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public Action[] getActions(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return new Action[0];
        }
        
        if (object instanceof BpelProcess) {
            return new Action[0];
        }
        
        if (object instanceof ProcessInstance) {
            return new Action[] {
                MAKE_CURRENT_ACTION, 
                RESUME_ACTION, 
                TERMINATE_ACTION
            };
        }
        
        throw new UnknownTypeException(object);
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void makeCurrentProcessInstance(
            final Object object) {
        
        if (object instanceof ProcessInstance) {
            myDebugger.setCurrentProcessInstance((ProcessInstance) object);
        }
    }
    
    private boolean canMakeCurrentProcessInstance(
            final Object object) {
        
        if (object instanceof ProcessInstance) {
            return !object.equals(myDebugger.getCurrentProcessInstance());
        }
        
        return false;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String MAKE_CURRENT_ACTION_NAME = NbBundle.getMessage(
          ProcessesActionProvider.class,
          "CTL_Process_Action_Make_Current"); // NOI18N
    
    private static final String RESUME_ACTION_NAME = NbBundle.getMessage(
          ProcessesActionProvider.class, 
          "CTL_Process_Action_Resume"); // NOI18N
    
    private static final String TERMINATE_ACTION_NAME = NbBundle.getMessage(
          ProcessesActionProvider.class,
          "CTL_Process_Action_Terminate"); // NOI18N
}
