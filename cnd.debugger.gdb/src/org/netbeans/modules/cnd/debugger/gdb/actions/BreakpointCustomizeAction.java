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

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.gdb.models.BreakpointsActionsProvider;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Customize action for line breakpoint, which is available from the gutter popup.
 *
 * @author Martin Entlicher
 */
public class BreakpointCustomizeAction extends NodeAction {
    
    /** Creates a new instance of BreakpointCustomizeAction */
    public BreakpointCustomizeAction() {
    }
    
    static LineBreakpoint getCurrentLineBreakpoint() {
        String currentURLStr = EditorContextBridge.getContext().getCurrentURL();
        if (currentURLStr == null) {
            return null;
        }
        URL currentURL;
        try {
            currentURL = new URL(currentURLStr);
        } catch (MalformedURLException muex) {
            return null;
        }
        int lineNumber = EditorContextBridge.getContext().getCurrentLineNumber();
        if (lineNumber < 0) {
            return null;
        }
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < bs.length; i++) {
            if (bs[i] instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) bs[i];
                URL url;
                try {
                    url = new URL(lb.getURL());
                } catch (MalformedURLException muex) {
                    continue;
                }
                if (currentURL.equals(url)) {
                    if (lineNumber == lb.getLineNumber()) {
                        return lb;
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public boolean isEnabled() { // overriden, because orig impl caches the nodes
        return enable(null);
    }

    protected boolean enable(Node[] activatedNodes) {
        GdbBreakpoint b = getCurrentLineBreakpoint();
        return b != null;
    }

    public String getName() {
        return NbBundle.getMessage(BreakpointCustomizeAction.class, "CTL_customize");
    }

    protected void performAction(Node[] activatedNodes) {
        GdbBreakpoint b = getCurrentLineBreakpoint();
        if (b == null) {
            return;
        }
        BreakpointsActionsProvider.customize(b);
    }
    
    @Override
    protected boolean asynchronous() {
        return false; //This action should run in AWT.
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
}
