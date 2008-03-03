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
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.util.NbBundle;


/**
 * @author   Gordon Prieur
 */
public class ThreadsActionsProvider implements NodeActionsProvider {
        
    private GdbDebugger    debugger;
    private ContextProvider  lookupProvider;
    
    private final Action MAKE_CURRENT_ACTION = Models.createAction(
        NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_ThreadsAction_MakeCurrent_Label"), // NOI18N
        new Models.ActionPerformer() {
            public boolean isEnabled(Object node) {
                return true;
            }
            public void perform(Object[] nodes) {
                makeCurrent(nodes[0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );


    public ThreadsActionsProvider(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = null;
    }
    
    private GdbDebugger getDebugger() {
        if (debugger == null) {
            debugger = lookupProvider.lookupFirst(null, GdbDebugger.class);
        }
        return debugger;
    }
    
    public Action[] getActions(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
	    return new Action[0];
	} else {
            return new Action[] { MAKE_CURRENT_ACTION };
        }
    }
    
    public void performDefaultAction(Object node) {
        if (node == TreeModel.ROOT) {
	    return;
	}
        makeCurrent(node);
    }
    
    private void makeCurrent(Object node) {
        getDebugger().setCurrentThread(node.toString().trim());
    }
}
