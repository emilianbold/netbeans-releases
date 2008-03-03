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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.jpda.ui.actions;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ThreadReference;

import java.util.Collections;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;

import org.netbeans.modules.debugger.jpda.ui.SourcePath;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
public class MakeCallerCurrentActionProvider extends JPDADebuggerAction {
    
    private ContextProvider lookupProvider;

    
    public MakeCallerCurrentActionProvider (ContextProvider lookupProvider) {
        super (
            lookupProvider.lookupFirst(null, JPDADebugger.class)
        );
        this.lookupProvider = lookupProvider;
        getDebuggerImpl ().addPropertyChangeListener 
            (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_MAKE_CALLER_CURRENT);
    }

    public void doAction (Object action) {
        JPDAThread t = getDebuggerImpl ().getCurrentThread ();
        if (t == null) return;
        int i = getCurrentCallStackFrameIndex (getDebuggerImpl ());
        if (i >= (t.getStackDepth () - 1)) return;
        setCurrentCallStackFrameIndex (getDebuggerImpl (), ++i, lookupProvider);
    }
    
    protected void checkEnabled (int debuggerState) {
        if (debuggerState == getDebuggerImpl ().STATE_STOPPED) {
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            if (t != null) {
                int i = getCurrentCallStackFrameIndex (getDebuggerImpl ());
                setEnabled (
                    ActionsManager.ACTION_MAKE_CALLER_CURRENT,
                    i < (t.getStackDepth () - 1)
                );
                return;
            }
        }
        setEnabled (
            ActionsManager.ACTION_MAKE_CALLER_CURRENT,
            false
        );
    }
    
    static int getCurrentCallStackFrameIndex (JPDADebugger debuggerImpl) {
        try {
            JPDAThread t = debuggerImpl.getCurrentThread ();
            if (t == null) return -1;
            CallStackFrame csf = debuggerImpl.getCurrentCallStackFrame ();
            if (csf == null) return -1;
            CallStackFrame s[] = t.getCallStack ();
            int i, k = s.length;
            for (i = 0; i < k; i++)
                if (csf.equals (s [i])) return i;
        } catch (AbsentInformationException e) {
        }
        return -1;
    }
    
    static void setCurrentCallStackFrameIndex (
        JPDADebugger debuggerImpl,
        int index,
        final ContextProvider lookupProvider
    ) {
        try {
            JPDAThread t = debuggerImpl.getCurrentThread ();
            if (t == null) return;
            if (t.getStackDepth () <= index) return;
            final CallStackFrame csf = t.getCallStack (index, index + 1) [0];
            csf.makeCurrent ();
        } catch (AbsentInformationException e) {
        }
    }
}
