/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.FormatOption;

/**
 * Action which "stepi" one assembly instruction
 */
public class EvaluateAction extends CallableSystemAction implements StateListener {

    private NativeDebugger debugger;

    static final long serialVersionUID = -8705899978543961455L;

    public EvaluateAction() {
        debugger = DebuggerManager.get().currentDebugger();
        if (debugger != null)
            debugger.addStateListener(this);
    }

    // interface CallableSystemAction
    protected boolean asynchronous() {
	return false;
    } 

    // interface CallableSystemAction
    public void performAction() {
	NativeDebugger debugger = DebuggerManager.get().currentDebugger();
	if (debugger != null) {
            // 6574620
            String selectedStr = EditorBridge.getCurrentSelection();
            if (selectedStr == null)
                debugger.exprEval(FormatOption.EMPTY, ""); // NOI18N
            else
                debugger.exprEval(FormatOption.EMPTY, selectedStr);
	}

    }
    

    // interface SystemAction
    public String getName() {
	return Catalog.get("LBL_EvaluateAction"); // NOI18N
    }
    

    // interface SystemAction
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("Welcome_fdide_home"); // NOI18N
    }


    // interface SystemAction
    protected String iconResource () {
        return "org/netbeans/modules/cnd/debugger/common2/icons/" +		// NOI18N
	       "evaluate_expression.png";			// NOI18N
    }


    // interface SystemAction
    protected void initialize() {
	super.initialize();
	// 6640192
	setEnabled(false);
    }    

    // interface StateListener
    public void update(State state) {
	if (state == null) {
	    setEnabled(false);
	} else {
	    setEnabled(state.isLoaded && state.isListening());
	}
    }
}
