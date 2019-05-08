/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerImpl;
import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=StateListener.class)
public class GoToSourceAction extends CallableSystemAction implements StateListener {

    private final String menu_name;

    public GoToSourceAction() {
	menu_name=Catalog.get("Dis_ACT_Go_To_Source"); // NOI18N
    }

    // interface SystemAction
    @Override
    public String getName() {
	return menu_name;
    }

    // interface SystemAction
    @Override
    protected void initialize() {
        super.initialize();
        setEnabled(false);
    }

    // interface StateListener
    @Override
    public void update(State state) {
        boolean enable;
        if (!state.isLoaded) {
            enable = false;
        } else {
            enable = state.isListening();
        }

        setEnabled(enable);
    }
    
    // interface CallableSystemAction
    @Override
    public void performAction() {
        NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
	if (debugger instanceof NativeDebuggerImpl) {
            ((NativeDebuggerImpl)debugger).showCurrentSource();
        }
    }

    // interface CallableSystemAction
    @Override
    public boolean asynchronous() {
	return false;
    }

    // interface SystemAction
    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    // interface SystemAction
    @Override
    protected String iconResource() {
        return null;
    }
}
