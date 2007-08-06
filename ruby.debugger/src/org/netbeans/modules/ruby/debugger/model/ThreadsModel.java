/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.debugger.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Action;
import org.netbeans.modules.ruby.debugger.ContextProviderWrapper;
import org.netbeans.modules.ruby.debugger.RubySession;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.rubyforge.debugcommons.model.RubyThreadInfo;
import static org.netbeans.spi.debugger.ui.Constants.THREAD_STATE_COLUMN_ID;

/**
 * @author Martin Krauskopf
 */
public final class ThreadsModel implements TreeModel, TableModel, NodeModel, NodeActionsProvider {
    
    public static final String RUNNING =
            "org/netbeans/modules/debugger/resources/threadsView/SuspendedThread"; // NOI18N
    
    public static final String SUSPENDED =
            "org/netbeans/modules/debugger/resources/threadsView/RunningThread"; // NOI18N
    
    private ContextProviderWrapper contextProvider;
    private final RubySession rubySession;
    private final List<ModelListener> listeners;
    
    public ThreadsModel(ContextProvider contextProvider) {
        this.contextProvider = new ContextProviderWrapper(contextProvider);
        rubySession = this.contextProvider.getRubySession();
        listeners = new CopyOnWriteArrayList<ModelListener>();
    }
    
    // TreeModel implementation ................................................
    
    public Object getRoot() {
        return ROOT;
    }
    
    public Object[] getChildren(Object parent, int from, int to)
            throws UnknownTypeException {
        if (parent == ROOT) {
            return rubySession.getThreadInfos();
        } else {
            throw new UnknownTypeException(parent);
        }
    }
    
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        } else if (node instanceof RubyThreadInfo) {
            // TODO - consider subthreads?
            return true;
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return rubySession.getThreadInfos().length;
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
            return NbBundle.getMessage(ThreadsModel.class, "CTL_ThreadsModel.Column.Name.Name");
        } else if (node instanceof RubyThreadInfo) {
            RubyThreadInfo ti = (RubyThreadInfo) node;
            String threadName = "RubyThread - " + ti.getId(); // NOI18N
            return rubySession.isActiveThread(ti.getId()) ?
                "<html><b>" + threadName + "</b></html>" : threadName; // NOI18N
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node == ROOT || node instanceof RubyThreadInfo) {
            // XXX return icon based on a thread's status. Either SUSPENDED or
            // RUNNING depending on RubyThreadInfo's state
            return SUSPENDED;
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public String getShortDescription(Object node)
            throws UnknownTypeException {
        if (node == ROOT) {
            return NbBundle.getMessage(ThreadsModel.class, "CTL_ThreadsModel.Column.Name.Desc");
        } else if (node instanceof RubyThreadInfo) {
            return null; // XXX
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    // TableModel implementation ...............................................
    
    public Object getValueAt(Object node, String columnID) throws
            UnknownTypeException {
        if (node instanceof RubyThreadInfo && THREAD_STATE_COLUMN_ID.equals(columnID)) {
            return ((RubyThreadInfo) node).getStatus();
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public boolean isReadOnly(Object node, String columnID) throws
            UnknownTypeException {
        if (node instanceof RubyThreadInfo && THREAD_STATE_COLUMN_ID.equals(columnID)) {
            return true;
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        throw new UnknownTypeException(node);
    }
    
    // NodeActionsProvider implementation ......................................
    
    public void performDefaultAction(Object node)
            throws UnknownTypeException {
        if (node instanceof RubyThreadInfo) {
            RubyThreadInfo ti = (RubyThreadInfo) node;
            rubySession.switchThread(ti.getId(), contextProvider);
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public Action[] getActions(Object node)
            throws UnknownTypeException {
        return new Action [] {};
    }
    
}
