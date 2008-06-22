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

package org.netbeans.modules.web.client.javascript.debugger.models;

import static org.netbeans.spi.debugger.ui.Constants.THREAD_STATE_COLUMN_ID;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Action;

import org.netbeans.modules.web.client.javascript.debugger.api.NbJSContextProviderWrapper;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebuggerEvent;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebuggerEventListener;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebuggerState;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSSource;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSWindow;
import org.netbeans.modules.web.client.javascript.debugger.js.impl.JSFactory;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSEditorUtil;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public final class NbJSThreadsModel implements TreeModel, TableModel, NodeModel, NodeActionsProvider, JSDebuggerEventListener {

    private static final String WINDOW =
        "org/netbeans/modules/web/client/javascript/debugger/ui/resources/window"; // NOI18N
    private static final String FRAME =
        "org/netbeans/modules/web/client/javascript/debugger/ui/resources/frame"; // NOI18N
    
    private NbJSContextProviderWrapper contextProvider;
    private final NbJSDebugger debugger;
    private JSWindow[] windows;
    private final List<ModelListener> listeners;
    
    private class PropertyChangeListenerImpl implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(NbJSDebugger.PROPERTY_WINDOWS)) {
                setWindows((JSWindow[]) evt.getNewValue());
            }
        }
    }
    private PropertyChangeListener propertyChangeListener;
    
    private final Action GO_TO_ACTION;
    
    public NbJSThreadsModel(ContextProvider contextProvider) {
        this.contextProvider = NbJSContextProviderWrapper.getContextProviderWrapper(contextProvider);
        debugger = this.contextProvider.getNbJSDebugger();
        debugger.addJSDebuggerEventListener(WeakListeners.create(JSDebuggerEventListener.class, this, debugger));
        
        GO_TO_ACTION = NbJSEditorUtil.createDebuggerGoToAction(debugger);
        
        propertyChangeListener = new PropertyChangeListenerImpl();
        this.debugger.addPropertyChangeListener(WeakListeners.propertyChange(propertyChangeListener, debugger));

        listeners = new CopyOnWriteArrayList<ModelListener>();

        windows = debugger.getWindows();
    }
    
    private void setWindows(JSWindow[] windows) {
        this.windows = windows;
        fireChanges();
    }
    
    // TreeModel implementation ................................................

    public Object getRoot() {
        return ROOT;
    }

    public Object[] getChildren(Object parent, int from, int to)
            throws UnknownTypeException {
        if (parent == ROOT) {
            if (windows != null) {
                return windows;
            }
            return new Object[0];
        } else if (parent instanceof JSWindow) {
            JSWindow[] childWindows = ((JSWindow) parent).getChildren();
            return (childWindows == null ? JSWindow.EMPTY_ARRAY : childWindows);
        } 
        throw new UnknownTypeException(parent);
    }

    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        } else if (node instanceof JSWindow) {
            JSWindow[] childWindows = ((JSWindow) node).getChildren();
            return childWindows == null || childWindows.length == 0;
        } 
        throw new UnknownTypeException(node);
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            int childrenCount = 0;
            if (windows != null) {
                childrenCount += windows.length;
            }
            return childrenCount;
        } else if (node instanceof JSWindow) {
            JSWindow[] childWindows = ((JSWindow) node).getChildren();
            return (childWindows == null ? 0 : childWindows.length);
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

    public void fireChanges() {
        for (ModelListener listener : listeners) {
            listener.modelChanged(new ModelEvent.TreeChanged(this));
        }
    }

    // NodeModel implementation ................................................

    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return NbBundle.getMessage(NbJSThreadsModel.class, "CTL_ThreadsModel.Column.Name.Name");
        } else if (node instanceof JSWindow) {
            JSWindow jsWindow = (JSWindow) node;
            String windowName =  getWindowName(jsWindow);
            return jsWindow.isSuspended() ?
                "<html><b>" + windowName + "</b></html>" : windowName; // NOI18N
        } 
        throw new UnknownTypeException(node);
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return WINDOW;
        } else if ( node instanceof JSWindow ) {
            JSWindow jsWindow = (JSWindow) node;
            if (jsWindow.getParent() == null) {
                return WINDOW;
            } else {
                return FRAME;
            }
        } 
        throw new UnknownTypeException(node);
    }

    public String getShortDescription(Object node)
            throws UnknownTypeException {
        if (node == ROOT) {
            return NbBundle.getMessage(NbJSThreadsModel.class, "CTL_ThreadsModel.Column.Name.Desc");
        } else if (node instanceof JSWindow) {
            return getDisplayName(node);
        }
        throw new UnknownTypeException(node);
    }

    // TableModel implementation ...............................................

    public Object getValueAt(Object node, String columnID) throws
            UnknownTypeException {
        if (node instanceof JSWindow && THREAD_STATE_COLUMN_ID.equals(columnID)) {
            return debugger.getState();
        } 
        throw new UnknownTypeException(node);
    }

    public boolean isReadOnly(Object node, String columnID) throws
            UnknownTypeException {
//        if (node instanceof JSWindow && THREAD_STATE_COLUMN_ID.equals(columnID)) {
//            return true;
//        } else {
//            throw new UnknownTypeException(node);
//        }
        /* No Columns in the Threads Model should be writeable */
        return true;
    }

    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        throw new UnknownTypeException(node);
    }

    // NodeActionsProvider implementation ......................................

    public void performDefaultAction(Object node)
            throws UnknownTypeException {
       if ( node instanceof JSWindow ){
           JSWindow window = (JSWindow)node;
           String strURI = window.getURI();
           JSSource source = JSFactory.createJSSource(strURI);
           NbJSEditorUtil.openFileObject(debugger.getFileObjectForSource(source));
       }
    }

    public Action[] getActions(Object node)
            throws UnknownTypeException {
        if( node instanceof JSWindow ){
            return new Action[]{ GO_TO_ACTION };
        } 
        return new Action[]{};
    }

    private final static String getWindowName(final JSWindow jsWindow) {
        return (jsWindow.getParent() == null ? "Window - " : "Frame - ") + jsWindow.getURI();
    }
    
    public void onDebuggerEvent(JSDebuggerEvent debuggerEvent) {
        JSDebuggerState jsDebuggerState = debuggerEvent.getDebuggerState();
        NbJSDebugger debugger = (NbJSDebugger) debuggerEvent.getSource();
        switch (jsDebuggerState.getState()) {
        case DISCONNECTED:
            debugger.removeJSDebuggerEventListener(this);
            break;
        default:
        }

        fireChanges();
    }    
}
