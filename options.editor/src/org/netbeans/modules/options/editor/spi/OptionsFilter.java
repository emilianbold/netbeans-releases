/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.options.editor.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.options.editor.FolderBasedController;
import org.netbeans.modules.options.editor.FolderBasedController.OptionsFilterAccessor;
import org.netbeans.spi.options.OptionsPanelController;

/**Allows to filter a tree based on user's input. Folder based {@link OptionsPanelController}s
 * that support filtering will put an instance of {@link TreeModelFilter} to the
 * lookup passed to the controller.
 *
 * @author Jan Lahoda
 * @since 1.19
 */
public final class OptionsFilter {

    private final Document doc;
    private final AtomicBoolean used;

    private OptionsFilter(Document doc, AtomicBoolean used) {
        this.doc = doc;
        this.used = used;
    }

    /**Install a filtering model to the given tree, using given model as the source
     * of the data.
     *
     * @param tree to which the model should be installed
     * @param source source {@link TreeModel} - the data to show will be gathered from this model
     * @param acceptor acceptor specifying whether the given original tree node should or should not
     *                 be visible for given user's filter
     */
    public void installFilteringModel(JTree tree, TreeModel source, Acceptor acceptor) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Not in AWT Event Dispatch Thread");
        }
        
        used.set(true);
        tree.setModel(new FilteringTreeModel(source, doc, acceptor));
    }

    public interface Acceptor {
        public boolean accept(Object originalTreeNode, String filterText);
    }

    private static final class FilteringTreeModel implements TreeModel, TreeModelListener, DocumentListener {

        private final TreeModel delegate;
        private final Document filter;
        private final Acceptor acceptor;
        private final Map<Object, List<Object>> category2Nodes = new HashMap<Object, List<Object>>();
        private final List<Object> categories;

        public FilteringTreeModel(TreeModel delegate, Document filter, Acceptor acceptor) {
            this.delegate = delegate;
            this.filter = filter;
            this.acceptor = acceptor;

            this.delegate.addTreeModelListener(this);
            this.filter.addDocumentListener(this);

            this.categories = new ArrayList<Object>(delegate.getChildCount(delegate.getRoot()));
            filter();
        }

        @Override
        public Object getRoot() {
            return delegate.getRoot();
        }

        @Override
        public Object getChild(Object parent, int index) {
            if (parent == getRoot()) {
                return categories.get(index);
            }

            return category2Nodes.get(parent).get(index);
        }

        @Override
        public int getChildCount(Object parent) {
            if (parent == getRoot()) {
                return categories.size();
            }

            return category2Nodes.get(parent).size();
        }

        @Override
        public boolean isLeaf(Object node) {
            return delegate.isLeaf(node);
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            delegate.valueForPathChanged(path, newValue);
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            if (parent == getRoot()) {
                return categories.indexOf(child);
            }

            return category2Nodes.get(parent).indexOf(child);
        }

        private final List<TreeModelListener> listeners = new LinkedList<TreeModelListener>();

        @Override
        public synchronized void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
        }

        @Override
        public synchronized void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }

        private synchronized Iterable<? extends TreeModelListener> getListeners() {
            return new LinkedList<TreeModelListener>(listeners);
        }

        void filter() {
            final String[] term = new String[1];

            filter.render(new Runnable() {
                public void run() {
                    try {
                        term[0] = filter.getText(0, filter.getLength());
                    } catch (BadLocationException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            });

            category2Nodes.clear();
            categories.clear();

            Object root = delegate.getRoot();

            for (int c = 0; c < delegate.getChildCount(root); c++) {
                Object cat = delegate.getChild(root, c);
                List<Object> filtered = new ArrayList<Object>(delegate.getChildCount(cat));

                for (int h = 0; h < delegate.getChildCount(cat); h++) {
                    Object hint = delegate.getChild(cat, h);

                    if (term[0].isEmpty() || acceptor.accept(hint, term[0])) {
                        filtered.add(hint);
                    }
                }

                if (term[0].isEmpty() || !filtered.isEmpty()) {
                    category2Nodes.put(cat, filtered);
                    categories.add(cat);
                }
            }

            for (TreeModelListener l : getListeners()) {
                l.treeStructureChanged(new TreeModelEvent(this, new Object[] {getRoot()}));
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            filter();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            filter();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {}

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            //XXX: does not change the source of the event.
            for (TreeModelListener l : getListeners()) {
                l.treeNodesChanged(e);
            }
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            throw new UnsupportedOperationException("Currently not supported.");
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            throw new UnsupportedOperationException("Currently not supported.");
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            throw new UnsupportedOperationException("Currently not supported.");
        }

    }

    static {
        FolderBasedController.setFilterAccessor(new OptionsFilterAccessor() {
            @Override
            public OptionsFilter create(Document doc, AtomicBoolean used) {
                return new OptionsFilter(doc, used);
            }
        });
    }
}
