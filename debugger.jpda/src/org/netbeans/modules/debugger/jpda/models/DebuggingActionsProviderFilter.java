/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.debugger.jpda.models;

import javax.swing.Action;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.CheckDeadlocksAction;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.TreeModel;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author   Martin Entlicher
 */
public class DebuggingActionsProviderFilter implements NodeActionsProviderFilter {

    private RequestProcessor rp;

    private synchronized RequestProcessor getRP() {
        if (rp == null) {
            rp = new RequestProcessor("Debugging Actions", 1);
        }
        return rp;
    }

    private Action SUSPEND_ALL_ACTION = Models.createAction (
        NbBundle.getBundle(DebuggingActionsProviderFilter.class).getString("CTL_ThreadAction_Suspend_All_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return node == TreeModel.ROOT;
            }

            public void perform (Object[] nodes) {
                getRP().post(new Runnable() {
                    public void run() {
                        debugger.suspend();
                    }
                });
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );

    private Action RESUME_ALL_ACTION = Models.createAction (
        NbBundle.getBundle(DebuggingActionsProviderFilter.class).getString("CTL_ThreadAction_Resume_All_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return node == TreeModel.ROOT;
            }

            public void perform (Object[] nodes) {
                getRP().post(new Runnable() {
                    public void run() {
                        debugger.resume();
                    }
                });
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );

    private Action DEADLOCK_DETECT_ACTION = Models.createAction (
        CheckDeadlocksAction.getDisplayName(),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return node == TreeModel.ROOT;
            }

            public void perform (Object[] nodes) {
                getRP().post(new Runnable() {
                    public void run() {
                        CheckDeadlocksAction.checkForDeadlock(debugger);
                    }
                });
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );

    private JPDADebuggerImpl debugger;
    
    
    public DebuggingActionsProviderFilter (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst(null, JPDADebugger.class);
    }
    
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            return new Action [] {
                RESUME_ALL_ACTION,
                SUSPEND_ALL_ACTION,
                null,
                DEADLOCK_DETECT_ACTION
            };
        } else {
            return original.getActions(node);
        }
    }
    
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        original.performDefaultAction(node);
    }

}
