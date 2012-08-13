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

package org.netbeans.modules.web.javascript.debugger.callstack;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.web.javascript.debugger.ViewModelSupport;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.text.Line;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "CTL_CallstackAction_Copy2CLBD_Label=Copy Stack"
})
@DebuggerServiceRegistration(path="javascript-debuggerengine/CallStackView", types={ NodeActionsProvider.class })
public final class CallStackActionsModel extends ViewModelSupport implements 
        NodeActionsProvider {

    private Debugger debugger;    

    private Action GO_TO_SOURCE;
    
    public CallStackActionsModel(final ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, Debugger.class);
        GO_TO_SOURCE = MiscEditorUtil.createDebuggerGoToAction();
    }

    // NodeActionsProvider implementation ......................................

    @Override
    public void performDefaultAction(Object node)
            throws UnknownTypeException {
        if (node instanceof CallFrame) {
            CallFrame frame = (CallFrame)node;
            Line line = MiscEditorUtil.getLine(frame.getScript().getURL(), frame.getLineNumber());
            MiscEditorUtil.showLine(line, true);
        }
    }

    @Override
    public Action[] getActions(Object node)
            throws UnknownTypeException {
        if (node instanceof CallFrame ) {
            return new Action [] {GO_TO_SOURCE/*, COPY_TO_CLBD_ACTION*/};
        }
        return new Action[]{};
        
    }

    private final Action COPY_TO_CLBD_ACTION = new AbstractAction(
            Bundle.CTL_CallstackAction_Copy2CLBD_Label()) {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            stackToCLBD();
        }
    };

    private void stackToCLBD() {
        // JPDAThread t = debugger.getCurrentThread();
        StringBuilder frameStr = new StringBuilder(50);
//        JSCallStackFrame[] stack = debugger.getCallStackFrames();
//        if (stack != null) {
//            int i, k = stack.length;
//
//            for (i = 0; i < k; i++) {
//                // frameStr.append(stack[i].getNameSpace());
//                // frameStr.append(".");
//                frameStr.append(stack[i].getFunctionName());
//                String sourceName = stack[i].getURI().toString();
//                frameStr.append("(");
//                frameStr.append(sourceName);
//                int line = stack[i].getLineNumber();
//                if (line > 0) {
//                    frameStr.append(":");
//                    frameStr.append(line);
//                }
//                frameStr.append(")");
//                if (i != k - 1)
//                    frameStr.append('\n');
//            }
//        }
        Clipboard systemClipboard = getClipboard();
        Transferable transferableText = new StringSelection(frameStr.toString());
        systemClipboard.setContents(transferableText, null);
    }
        
    private static Clipboard getClipboard() {
        Clipboard clipboard = org.openide.util.Lookup.getDefault().lookup(
                Clipboard.class);
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return clipboard;
    }

}
