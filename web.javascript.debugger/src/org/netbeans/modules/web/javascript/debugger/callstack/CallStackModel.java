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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;

import org.netbeans.modules.web.javascript.debugger.ViewModelSupport;
import org.netbeans.modules.web.javascript.debugger.annotation.CallStackAnnotation;
import org.netbeans.modules.web.javascript.debugger.annotation.CurrentLineAnnotation;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "CTL_CallstackModel_Column_Name_Name=Name",
    "CTL_CallstackAction_Copy2CLBD_Label=Copy Stack"
})
public final class CallStackModel extends ViewModelSupport implements TreeModel, NodeModel,
        NodeActionsProvider, TableModel, Debugger.Listener {

    public static final String CALL_STACK =
            "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame"; // NOI18N
    public static final String CURRENT_CALL_STACK =
            "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame"; // NOI18N

    private Debugger debugger;    

    private Action GO_TO_SOURCE;
    
    private AtomicReference<List<? extends CallFrame>> stackTrace = 
            new AtomicReference<List<? extends CallFrame>>(new ArrayList<CallFrame>());
    private AtomicReference<? extends CallFrame>  myCurrentStack;
    
    private List<Annotation> annotations = new ArrayList<Annotation>();
    
    public CallStackModel(final ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, Debugger.class);
        debugger.addListener(this);
        GO_TO_SOURCE = MiscEditorUtil.createDebuggerGoToAction();
        // update now:
        setStackTrace(debugger.isSuspended() ? debugger.getCurrentCallStack() : new ArrayList<CallFrame>());
        updateAnnotations();
    }

    public void setStackTrace(List<? extends CallFrame> stackTrace) {
        List<CallFrame> l = new ArrayList<CallFrame>();
        this.stackTrace = new AtomicReference<List<? extends CallFrame>>(l);
        for (CallFrame cf : stackTrace) {
            if (cf.getScript() != null) {
                l.add(cf);
            }
        }
        if (stackTrace.size() > 0) {
            myCurrentStack = new AtomicReference<CallFrame>(l.get(0));
        } else {
            myCurrentStack = null;
        }
    }
    
    // TreeModel implementation ................................................

    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public Object[] getChildren(Object parent, int from, int to)
            throws UnknownTypeException {
        if (parent == ROOT) {
            List<? extends CallFrame> list = stackTrace.get();
            if ( list == null ){
                return new Object[0];
            }
            else {
                if ( from >= list.size() ) {
                    return new Object[0];
                }
                int end = Math.min( list.size(), to);
                List<? extends CallFrame> stack = list.subList( from , end );
                return stack.toArray();
            }
        }
        
        throw new UnknownTypeException(parent);
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        } else if (node instanceof CallFrame) {
            return true;
        }
        
        throw new UnknownTypeException(node);
    }

    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            List<? extends CallFrame> list = stackTrace.get();
            if ( list == null ){
                return 0;
            }
            else {
                return list.size();
            }
        }
        
        throw new UnknownTypeException(node);
    }

    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return Bundle.CTL_CallstackModel_Column_Name_Name();
        } else if (node instanceof CallFrame) {
            CallFrame frame = (CallFrame)node;
            return frame.getFunctionName();
        } else {
            throw new UnknownTypeException(node);
        }
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof CallFrame) {
            CallFrame curStack = myCurrentStack.get();
            if( curStack == node) {
                return CURRENT_CALL_STACK;
            }
            else {
                return CALL_STACK;
            }
        }
        else if (node == ROOT) {
            return null;
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public String getShortDescription(Object node)
            throws UnknownTypeException {
        if (node instanceof CallFrame) {
            CallFrame frame = (CallFrame)node;
            return frame.getScript().getURL() + ":" + (frame.getLineNumber()+1);
        }
        return null;
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

    // TableModel implementation ...............................................

    @Override
    public Object getValueAt(Object node, String columnID)
            throws UnknownTypeException {
        if (node instanceof CallFrame) {
            CallFrame frame = (CallFrame) node;
            if ( columnID.equals(Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID) ){
                String file = frame.getScript().getURL();
                int index = file.lastIndexOf("/");
                if (index != -1) {
                    file = file.substring(index+1);
                }
                return file + ":" + (frame.getLineNumber()+1);
            }
        } 
        throw new UnknownTypeException("Unknown Type Node: " + node + " or columnID: " + columnID);
    }

    @Override
    public boolean isReadOnly(Object node, String columnID) throws
            UnknownTypeException {
        if ( node instanceof CallFrame ){
            if (columnID.equals(Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID)) {
                return true;
            }
        } 
        throw new UnknownTypeException(node);
    }

    @Override
    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        throw new UnknownTypeException(node);
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

    private void updateAnnotations() {
        for (Annotation ann : annotations) {
            ann.detach();
        }
        annotations.clear();
        boolean first = true;
        for (CallFrame cf : stackTrace.get()) {
            final Line line = MiscEditorUtil.getLine(cf.getScript().getURL(), cf.getLineNumber());
            if (line == null) {
                first = false;
                continue;
            }
            Annotation anno;
            if (first) {
                anno = new CurrentLineAnnotation(line);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        line.show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FRONT);
                    }
                });
            } else {
                anno = new CallStackAnnotation(line);
            }
            annotations.add(anno);
            first = false;
        }
    }

    @Override
    public void paused(List<CallFrame> callStack, String reason) {
        setStackTrace(callStack);
        updateAnnotations();
        refresh();
    }

    @Override
    public void resumed() {
        setStackTrace(new ArrayList<CallFrame>());
        updateAnnotations();
        refresh();
    }

    @Override
    public void reset() {
    }

}
