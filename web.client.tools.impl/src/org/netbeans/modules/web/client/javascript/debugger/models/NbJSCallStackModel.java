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

package org.netbeans.modules.web.client.javascript.debugger.models;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.modules.web.client.javascript.debugger.api.NbJSContextProviderWrapper;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.javascript.debugger.filesystem.URLFileObject;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerState;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSSource;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSFactory;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSEditorUtil;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public class NbJSCallStackModel implements TreeModel, NodeModel,
        NodeActionsProvider, TableModel {

    public static final String CALL_STACK =
            "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame"; // NOI18N
    public static final String CURRENT_CALL_STACK =
            "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame"; // NOI18N

    private final NbJSDebugger debugger;    
    private class JSDebuggerEventListenerImpl implements JSDebuggerEventListener {
        public void onDebuggerEvent(JSDebuggerEvent debuggerEvent) {
            fireTreeChanges();
        }
    }

    private JSDebuggerEventListener debuggerListener;
    private final List<ModelListener> listeners;
    private final Action GO_TO_SOURCE;

    public NbJSCallStackModel(final ContextProvider contextProvider) {
        debugger = NbJSContextProviderWrapper.getContextProviderWrapper(contextProvider).getNbJSDebugger();
        
        listeners = new CopyOnWriteArrayList<ModelListener>();
        // Add listener to JSDebugger
        debuggerListener = new JSDebuggerEventListenerImpl();
        this.debugger.addJSDebuggerEventListener(WeakListeners.create(
                JSDebuggerEventListener.class,
                debuggerListener,
                this.debugger));
        GO_TO_SOURCE = NbJSEditorUtil.createDebuggerGoToAction(this.debugger);
    }

    // TreeModel implementation ................................................

    public Object getRoot() {
        return ROOT;
    }

    public Object[] getChildren(Object parent, int from, int to)
            throws UnknownTypeException {
        if (parent == ROOT) {
            JSDebuggerState state = debugger.getState();
            switch (state.getState()) {
                case SUSPENDED :
                    return debugger.getCallStackFrames();
                default:
                    return new Object[0];
            }
        } else {
            throw new UnknownTypeException(parent);
        }
    }

    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            JSDebuggerState state = debugger.getState();
            switch (state.getState()) {
                case SUSPENDED :
                    return false;
                default:
                    return true;
            }
        } else if (node instanceof JSCallStackFrame) {
            return true;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            JSDebuggerState state = debugger.getState();
            switch (state.getState()) {
                case SUSPENDED :
                    return debugger.getCallStackFrames().length;
                default:
                    return 0;
            }        
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }

    public void fireTreeChanges() {
        for (ModelListener listener : listeners) {
            listener.modelChanged(new ModelEvent.TreeChanged(this));
        }
    }

    // NodeModel implementation ................................................

    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return NbBundle.getMessage(NbJSCallStackModel.class, "CTL_CallstackModel.Column.Name.Name");
        } else if (node instanceof JSCallStackFrame) {
            JSCallStackFrame frame = ((JSCallStackFrame) node);
            String displayName = frame.getDisplayName();
            return debugger.isSelectedFrame(frame) ? "<html><b>" + displayName + "</b></html>" : displayName; // NOI18N
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof JSCallStackFrame) {
            if (debugger.isSelectedFrame((JSCallStackFrame) node)) {
                return CURRENT_CALL_STACK;
            } else {
                return CALL_STACK;
            }
        } else if (node == ROOT) {
            return null;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public String getShortDescription(Object node)
            throws UnknownTypeException {
        if (node == ROOT) {
            return NbBundle.getMessage(NbJSCallStackModel.class, "CTL_CallstackModel.Column.Name.Desc");
        } else if (node instanceof JSCallStackFrame) {
            return ((JSCallStackFrame) node).getDisplayName();
        } else {
            throw new UnknownTypeException(node);
        }
    }

    // NodeActionsProvider implementation ......................................

    public void performDefaultAction(Object node)
            throws UnknownTypeException {
        if (node instanceof JSCallStackFrame) {
            JSCallStackFrame frame = (JSCallStackFrame)node;
            Line line = NbJSEditorUtil.getLine(debugger, frame);
            NbJSEditorUtil.showLine(line, true);
            debugger.selectFrame(frame);
            fireTreeChanges();
            return;
        }
    }

    public Action[] getActions(Object node)
            throws UnknownTypeException {
        if (node instanceof JSCallStackFrame ) {
            return new Action [] {GO_TO_SOURCE,COPY_TO_CLBD_ACTION};
        }
        return new Action[]{};
        
    }

    // TableModel implementation ...............................................

    public Object getValueAt(Object node, String columnID)
            throws UnknownTypeException {
        if (node instanceof JSCallStackFrame) {
            if ( columnID.equals(Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID) ){
                JSCallStackFrame frame = (JSCallStackFrame) node;
                JSSource source = JSFactory.createJSSource(frame.getLocation().getURI().toString());
                FileObject fileObject = debugger.getFileObjectForSource(source);
                return (fileObject instanceof URLFileObject ? 
                    fileObject.getPath() : FileUtil.getFileDisplayName(fileObject)) + ":" + frame.getLineNumber();
            } else if (columnID.equals(ResolvedLocationColumnModel.RESOLVED_LOCATION_COLUMN_ID) ) {
                JSCallStackFrame frame = (JSCallStackFrame) node;
                return frame.getLocation().getDisplayName();
            }
        } 
        throw new UnknownTypeException("Unknown Type Node: " + node + " or columnID: " + columnID);
    }

    public boolean isReadOnly(Object node, String columnID) throws
            UnknownTypeException {
        if ( node instanceof JSCallStackFrame ){
            if (columnID.equals(Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID) ||
                    columnID.equals(ResolvedLocationColumnModel.RESOLVED_LOCATION_COLUMN_ID) ) {
                return true;
            }
        } 
        throw new UnknownTypeException(node);
    }

    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        throw new UnknownTypeException(node);
    }
    
    private final Action COPY_TO_CLBD_ACTION = new AbstractAction(NbBundle
            .getMessage(NbJSCallStackModel.class,
                    "CTL_CallstackAction_Copy2CLBD_Label")) {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            stackToCLBD();
        }
    };

    private void stackToCLBD() {
        // JPDAThread t = debugger.getCurrentThread();
        StringBuffer frameStr = new StringBuffer(50);
        JSCallStackFrame[] stack = debugger.getCallStackFrames();
        if (stack != null) {
            int i, k = stack.length;

            for (i = 0; i < k; i++) {
                // frameStr.append(stack[i].getNameSpace());
                // frameStr.append(".");
                frameStr.append(stack[i].getFunctionName());
                String sourceName = stack[i].getURI().toString();
                frameStr.append("(");
                frameStr.append(sourceName);
                int line = stack[i].getLineNumber();
                if (line > 0) {
                    frameStr.append(":");
                    frameStr.append(line);
                }
                frameStr.append(")");
                if (i != k - 1)
                    frameStr.append('\n');
            }
        }
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
