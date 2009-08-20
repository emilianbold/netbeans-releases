/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.debugger.gdb.models;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.cnd.debugger.gdb.GdbCallStackFrame;
import org.netbeans.modules.cnd.debugger.common.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.disassembly.Disassembly;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


/**
 * @author   Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class CallStackActionsProvider implements NodeActionsProvider {
    
    private final Action MAKE_CURRENT_ACTION = Models.createAction(
        NbBundle.getBundle(CallStackActionsProvider.class).getString("CTL_CallstackAction_MakeCurrent_Label"), // NOI18N
        new Models.ActionPerformer() {
            public boolean isEnabled(Object node) {
                // TODO: Check whether is not current - API change necessary
                return true;
            }
            public void perform(Object[] nodes) {
                makeCurrent((GdbCallStackFrame) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE);
	
    private final Action POP_TO_HERE_ACTION = Models.createAction(
        NbBundle.getBundle(CallStackActionsProvider.class).getString("CTL_CallstackAction_PopToHere_Label"),
        new Models.ActionPerformer() {
            public boolean isEnabled(Object node) {
                // TODO: Check whether this frame is deeper then the top-most
                return true;
            }
            public void perform(Object[] nodes) {
                popToHere((GdbCallStackFrame) nodes[0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
    private final GdbDebugger debugger;

    public CallStackActionsProvider(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, GdbDebugger.class);
    }
    
    public Action[] getActions(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
	    return new Action[0];
	}
        if (!(node instanceof GdbCallStackFrame)) {
	    throw new UnknownTypeException(node);
	}
        
        return new Action[] { MAKE_CURRENT_ACTION, POP_TO_HERE_ACTION };
    }
    
    public void performDefaultAction(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
	    return;
	}
        if (node instanceof GdbCallStackFrame) {
            makeCurrent((GdbCallStackFrame) node);
            return;
        }
        throw new UnknownTypeException(node);
    }

    public void addModelListener(ModelListener l) {
    }

    public void removeModelListener(ModelListener l) {
    }

    private void popToHere(final GdbCallStackFrame frame) {
        if (!frame.isValid()) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                       "ERR_InvalidCallStackFrame"))); // NOI18N
        } else {
            debugger.getGdbProxy().stack_select_frame(frame.getFrameNumber() - 1);
            debugger.getGdbProxy().exec_finish();
            makeCurrent(frame);
        }
    }
    
    private void makeCurrent(final GdbCallStackFrame frame) {
        if (debugger.getCurrentCallStackFrame() != frame) {
            if (frame.isValid()) {
                frame.makeCurrent();
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                           "ERR_InvalidCallStackFrame"))); // NOI18N
            }
	} else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
		    EditorContextBridge.showSource(frame, Disassembly.isInDisasm());
                }
            });
	}
    }
}
