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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.rubyforge.debugcommons.model.RubyVariable;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_VALUE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_VALUE_COLUMN_ID;

/**
 * @author Martin Krauskopf
 */
public final class WatchesModel extends VariablesModel {
    
    public static final String WATCH =
            "org/netbeans/modules/debugger/resources/watchesView/watch_16.png"; // NOI18N
    
    private WatchesListener listener;
    
    public WatchesModel(final ContextProvider contextProvider) {
        super(contextProvider);
    }
    
    // TreeModel implementation ................................................
    
    @Override
    public Object[] getChildren(Object parent, int from, int to)
            throws UnknownTypeException {
        checkListener(parent);
        if (parent == ROOT) {
            return DebuggerManager.getDebuggerManager().getWatches();
        } else if (parent instanceof Watch) {
            RubyVariable var = resolveVariable((Watch) parent);
            return var == null ? new Object[0] : super.getChildren(var, from, to);
        } else {
            return super.getChildren(parent, from, to);
        }
    }
    
    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        } else if (node instanceof Watch) {
            RubyVariable var = resolveVariable((Watch) node);
            return var == null ? true : super.isLeaf(var);
        } else {
            return super.isLeaf(node);
        }
    }
    
    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        checkListener(node);
        if (node == ROOT) {
            return DebuggerManager.getDebuggerManager().getWatches().length;
        } else if (node instanceof Watch) {
            RubyVariable var = resolveVariable((Watch) node);
            return var == null ? 0 : super.getChildrenCount(var);
        } else {
            return super.getChildrenCount(node);
        }
    }
    
    // NodeModel implementation ................................................
    
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return getMessage("CTL_CallstackModel.Column.Name.Name");
        } else if (node instanceof Watch) {
            return ((Watch) node).getExpression();
        } else {
            return super.getDisplayName(node);
        }
    }
    
    @Override
    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node == ROOT || node instanceof Watch) {
            return WATCH;
        } else {
            return super.getIconBaseWithExtension(node);
        }
    }
    
    @Override
    public String getShortDescription(Object node)
            throws UnknownTypeException {
        if (node == ROOT) {
            return getMessage("CTL_CallstackModel.Column.Name.Desc");
        } else if (node instanceof Watch) {
            RubyVariable var = resolveVariable((Watch) node);
            return var == null ? getMessage("CTL_WatchesModel.Unknown.Evaluation") : super.getShortDescription(var);
        } else {
            return super.getShortDescription(node);
        }
    }
    
    
    // TableModel implementation ...............................................
    
    @Override
    public Object getValueAt(Object node, String columnID) throws
            UnknownTypeException {
        if(node instanceof Watch) {
            RubyVariable var = resolveVariable((Watch) node);
            if (var == null) {
                return getMessage("CTL_WatchesModel.Unknown.Evaluation");
            }
            if (WATCH_VALUE_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(var, LOCALS_VALUE_COLUMN_ID);
            } else if(WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(var, LOCALS_TYPE_COLUMN_ID);
            }
        } else {
            if (WATCH_VALUE_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(node, LOCALS_VALUE_COLUMN_ID);
            } else if(WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(node, LOCALS_TYPE_COLUMN_ID);
            } else if(WATCH_TO_STRING_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(node, LOCALS_TO_STRING_COLUMN_ID);
            }
        }
        throw new UnknownTypeException(node);
    }
    
    @Override
    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        return true;
    }
    
    @Override
    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        throw new UnknownTypeException(node);
    }

    private synchronized void checkListener(Object node) {
        if (listener == null && (node == ROOT || node instanceof Watch)) {
            listener = new WatchesListener(this);
        }
    }

    private void fireWatchPropertyChanged(Watch watch, String propertyName) {
        for (ModelListener listener : listeners) {
            listener.modelChanged(new ModelEvent.NodeChanged(this, watch));
        }
    }

    private RubyVariable resolveVariable(final Watch watch) {
        String expr = watch.getExpression();
        return rubySession.inspectExpression(expr);
    }

    private static class WatchesListener extends DebuggerManagerAdapter implements PropertyChangeListener {

        private WeakReference<WatchesModel> modelRef;

        public WatchesListener(WatchesModel watchesModel) {
            modelRef = new WeakReference<WatchesModel>(watchesModel);
            DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_WATCHES, this);
            Watch[] watches = DebuggerManager.getDebuggerManager().getWatches();
            for (Watch watch : watches) {
                watch.addPropertyChangeListener(this);
            }
        }

        private WatchesModel getModel() {
            WatchesModel model = modelRef.get();
            if (model == null) {
                DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_WATCHES, this);
                Watch[] watches = DebuggerManager.getDebuggerManager().getWatches();
                for (Watch watch : watches) {
                    watch.addPropertyChangeListener(this);
                }
            }
            return model;
        }

        @Override
        public void watchAdded(Watch watch) {
            WatchesModel model = getModel();
            if (model == null) {
                return;
            }
            watch.addPropertyChangeListener(this);
            model.fireChanges();
        }

        @Override
        public void watchRemoved(Watch watch) {
            WatchesModel model = getModel();
            if (model == null) {
                return;
            }
            watch.removePropertyChangeListener(this);
            model.fireChanges();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            WatchesModel model = getModel();
            if (model == null) {
                return;
            }
            if (!(evt.getSource() instanceof Watch)) {
                return;
            }
            Watch w = (Watch) evt.getSource();
            model.fireWatchPropertyChanged(w, evt.getPropertyName());
        }
    }

    private static String getMessage(final String key) {
        return NbBundle.getMessage(WatchesModel.class, key);
    }
}
