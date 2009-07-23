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
package org.netbeans.modules.debugger.jpda.ui.models;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import javax.swing.Action;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ui.CodeEvaluator;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

public class EvaluatorTreeModel extends CachedChildrenTreeModel implements NodeActionsProviderFilter {

    public static final String HISTORY_NODE =
        "org/netbeans/modules/debugger/jpda/resources/history_node_16.png";

    public static final String HISTORY_ITEM =
        "org/netbeans/modules/debugger/jpda/resources/eval_history_item.png";

    private final Action PASTE_TO_EVALUATOR = Models.createAction (
        NbBundle.getBundle (EvaluatorTreeModel.class).getString ("CTL_PasteExprFromHistoryToEvaluator"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                CodeEvaluator.getInstance().pasteExpression(((ItemNode)nodes[0]).item.expr);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );

    private Collection<ModelListener> listeners = new HashSet<ModelListener>();

    EvaluatorListener evalListener = new EvaluatorListener();

    public EvaluatorTreeModel() {
        CodeEvaluator.addResultListener(evalListener);
    }

    public Object getRoot() {
        return TreeModel.ROOT;
    }

    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (TreeModel.ROOT.equals(node)) {
            return false;
        } else if (node instanceof SpecialNode) {
            return ((SpecialNode)node).isLeaf();
        }
        throw new UnknownTypeException(node.toString());
    }

    @Override
    protected Object[] computeChildren(Object node) throws UnknownTypeException {
        if (node instanceof SpecialNode) {
            return ((SpecialNode) node).getChildren(0, 0);
        }
        if (node == TreeModel.ROOT) {
            Variable result = CodeEvaluator.getResult();
            ArrayList items = CodeEvaluator.getHistory();
            int count = 0;
            if (result != null) {
                count++;
            }
            if (items.size() > 0) {
                count++;
            }
            Object[] children = new Object[count];
            int index = 0;
            if (result != null) {
                children[index++] = result;
            }
            if (items.size() > 0) {
                children[index] = new HistoryNode();
            }
            return children;
        }
        throw new UnknownTypeException(node.toString());
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (TreeModel.ROOT.equals(node)) {
            Variable result = CodeEvaluator.getResult();
            ArrayList items = CodeEvaluator.getHistory();
            int count = 0;
            if (result != null) {
                count++;
            }
            if (items.size() > 0) {
                count++;
            }
            return count;
        }
        if (node instanceof SpecialNode) {
            return ((SpecialNode)node).getChildrenCount();
        }
        return Integer.MAX_VALUE;
    }

    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add (l);
        }
    }

    public void removeModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }

    public void fireNodeChanged (Object node) {
        try {
            recomputeChildren();
        } catch (UnknownTypeException ex) {
            return;
        }
        ModelListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ModelListener[0]);
        }
        ModelEvent ev = new ModelEvent.NodeChanged(this, node);
        for (int i = 0; i < ls.length; i++) {
            ls[i].modelChanged (ev);
        }
    }

    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof ItemNode) {
            CodeEvaluator.getInstance().pasteExpression(((ItemNode)node).item.expr);
        } else {
            original.performDefaultAction(node);
        }
    }

    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof ItemNode) {
            return new Action[] {PASTE_TO_EVALUATOR};
        }
        return original.getActions(node);
    }

    // **************************************************************************

    abstract static class SpecialNode {

        abstract Object [] getChildren(int from, int to);

        abstract int getChildrenCount();

        abstract String getDisplayName();

        abstract String getValueAt(String columnID);

        abstract String getShortDescription();

        abstract String getIconBase();

        abstract boolean isLeaf();

    }

    static class HistoryNode extends SpecialNode {

        @Override
        Object [] getChildren(int from, int to) {
            ArrayList<CodeEvaluator.History.Item> items = CodeEvaluator.getHistory();
            ItemNode[] vals = new ItemNode[items.size()];
            for (int x = 0; x < items.size(); x++) {
                CodeEvaluator.History.Item item = items.get(x);
                vals[x] = new ItemNode(item);
            }
            return vals;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof HistoryNode;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return hash;
        }

        @Override
        String getDisplayName() {
            return NbBundle.getBundle(EvaluatorTreeModel.class).getString("MSG_EvaluatorHistoryFilterNode"); // NOI18N
        }

        @Override
        String getIconBase() {
            return HISTORY_NODE;
        }

        @Override
        boolean isLeaf() {
            return false;
        }

        @Override
        int getChildrenCount() {
            return CodeEvaluator.getHistory().size();
        }

        @Override
        String getShortDescription() {
            return NbBundle.getBundle(EvaluatorTreeModel.class).getString("CTL_EvaluatorHistoryNode"); // NOI18N
        }

        @Override
        String getValueAt(String columnID) {
            return ""; // NOI18N
        }

    }

    static class ItemNode extends SpecialNode {

        CodeEvaluator.History.Item item;

        protected ItemNode(CodeEvaluator.History.Item item) {
            this.item = item;
        }

        @Override
        Object [] getChildren(int from, int to) {
            return new Object[0];
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemNode)) return false;
            return item.equals(((ItemNode) o).item);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 31 * hash + (this.item != null ? this.item.hashCode() : 0);
            return hash;
        }

        @Override
        String getDisplayName() {
            return item.expr;
        }

        @Override
        String getIconBase() {
            return HISTORY_ITEM;
        }

        @Override
        boolean isLeaf() {
            return true;
        }

        @Override
        int getChildrenCount() {
            return 0;
        }

        @Override
        String getShortDescription() {
            return item.tooltip;
        }

        @Override
        String getValueAt(String columnID) {
            if (Constants.LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                return item.toString;
            } else if (Constants.LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
                return item.type;
            } else if (Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID)) {
                return item.value;
            }
            return ""; // NOI18N
        }

    }

    // **************************************************************************

    private class EvaluatorListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            fireNodeChanged(TreeModel.ROOT);
        }

    }

}
