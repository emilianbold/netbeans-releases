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

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Action;
import org.netbeans.modules.ruby.debugger.ContextProviderWrapper;
import org.netbeans.modules.ruby.debugger.EditorUtil;
import org.netbeans.modules.ruby.debugger.RubySession;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.openide.util.NbBundle;
import org.rubyforge.debugcommons.model.RubyFrame;

/**
 * @author Martin Krauskopf
 */
public class CallStackModel implements TreeModel, NodeModel,
        NodeActionsProvider, TableModel {
    
    public static final String CALL_STACK =
            "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame"; // NOI18N
    public static final String CURRENT_CALL_STACK =
            "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame"; // NOI18N
    
    private final ContextProviderWrapper contextProvider;
    private final RubySession rubySession;
    private final List<ModelListener> listeners;
    
    public CallStackModel(final ContextProvider contextProvider) {
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
            return rubySession.getFrames();
        } else {
            throw new UnknownTypeException(parent);
        }
    }
    
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        } else if (node instanceof RubyFrame) {
            return true;
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return rubySession.getFrames().length;
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
            return NbBundle.getMessage(CallStackModel.class, "CTL_CallstackModel.Column.Name.Name");
        } else if (node instanceof RubyFrame) {
            RubyFrame frame = ((RubyFrame) node);
            String basename = new File(frame.getFile()).getName();
            String dn = basename + ':' + frame.getLine();
            return rubySession.isSelectedFrame(frame) ? "<html><b>" + dn + "</b></html>" : dn; // NOI18N
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof RubyFrame) {
            if (rubySession.isSelectedFrame((RubyFrame) node)) {
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
            return NbBundle.getMessage(CallStackModel.class, "CTL_CallstackModel.Column.Name.Desc");
        } else if (node instanceof RubyFrame) {
            return ((RubyFrame) node).getName();
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    // NodeActionsProvider implementation ......................................
    
    public void performDefaultAction(Object node)
            throws UnknownTypeException {
        if (node instanceof RubyFrame) {
            RubyFrame frame = (RubyFrame) node;
            EditorUtil.showLine(
                    EditorUtil.getLine(rubySession.resolveAbsolutePath(frame.getFile()),
                    frame.getLine() - 1));
            rubySession.selectFrame(frame);
            fireChanges();
            contextProvider.getVariablesModel().fireChanges();
            contextProvider.getWatchesModel().fireChanges();
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public Action[] getActions(Object node)
            throws UnknownTypeException {
        return new Action [] {};
    }
    
    // TableModel implementation ...............................................
    
    public Object getValueAt(Object node, String columnID) throws
            UnknownTypeException {
        if (columnID == Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID &&
                node instanceof RubyFrame) {
            RubyFrame frame = (RubyFrame) node;
            return frame.getFile();
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public boolean isReadOnly(Object node, String columnID) throws
            UnknownTypeException {
        if (columnID == Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID &&
                node instanceof RubyFrame) {
            return true;
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        throw new UnknownTypeException(node);
    }
    
}
