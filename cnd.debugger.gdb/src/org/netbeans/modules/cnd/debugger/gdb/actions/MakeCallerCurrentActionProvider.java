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

package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.cnd.debugger.gdb.GdbCallStackFrame;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
* Representation of a debugging session.
*
* @author  Gordon Prieur (copied from Jan Jancura's JPDA implementation)
*/
public class MakeCallerCurrentActionProvider extends ActionsProviderSupport implements PropertyChangeListener {
    
    private GdbDebugger debugger;
    
    public MakeCallerCurrentActionProvider(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, GdbDebugger.class);
        debugger.addPropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }
    
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_MAKE_CALLER_CURRENT);
    }

    public void doAction(Object action) {
        int i = getCurrentCallStackFrameIndex(debugger);
        if (i < (debugger.getStackDepth() - 1)) {
	    setCurrentCallStackFrameIndex(debugger, i+1);
	}
    }
    
    private void checkEnabled(boolean stoppped) {
        if (stoppped) {
	    int i = getCurrentCallStackFrameIndex(debugger);
	    setEnabled(ActionsManager.ACTION_MAKE_CALLER_CURRENT, i < (debugger.getStackDepth() - 1));
        } else {
	    setEnabled(ActionsManager.ACTION_MAKE_CALLER_CURRENT, false);
	}
    }
    
    static int getCurrentCallStackFrameIndex(GdbDebugger debugger) {
	GdbCallStackFrame csf = debugger.getCurrentCallStackFrame();
        if (csf != null) {
            return csf.getFrameNumber();
        } else {
            return -1;
        }
    }
    
    static void setCurrentCallStackFrameIndex(GdbDebugger debugger, int index) {
	if (index < debugger.getStackDepth()) {
	    GdbCallStackFrame csf = debugger.getCallStack().get(index);
	    csf.makeCurrent();
	}
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
	checkEnabled(debugger.isStopped());
    }
}

