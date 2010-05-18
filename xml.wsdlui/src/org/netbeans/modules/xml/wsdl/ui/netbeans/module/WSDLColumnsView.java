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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.netbeans.modules.xml.xam.ui.column.BasicColumnView;
import org.netbeans.modules.xml.xam.ui.column.Column;
import org.netbeans.modules.xml.xam.ui.column.ColumnView;
import org.netbeans.modules.xml.xam.ui.column.LinkPanel;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Displays the WSDL document in a series of columns, each containing a tree
 * representing a subset of the WSDL model.
 * 
 * <p><em>Note: WSDLColumnsView, like all NbColumnView subclasses, has its
 * own JScrollPane, you do not need to place it in a JScrollPane.</em></p>
 * 
 * @author Todd Fast, todd.fast@sun.com
 * @author Nathan Fiedler
 * @author Jeri Lockhart
 */
public class WSDLColumnsView extends JPanel implements ColumnView,
        /*HelpCtx.Provider,*/ PropertyChangeListener {
    /** silence compiler warnings */
    static final long serialVersionUID = 1L;
    /** Where the columns are shown. */
    private transient BasicColumnView columnView;
    /** Where the bread crumbs live. */
    private transient LinkPanel breadCrumbs;
    private transient WSDLModel wsdlModel;
    private transient Lookup lookup;
    private Class<? extends EventListener> columnListenerClass;
    private transient EventListener columnListener;
    public static final String PROP_COLUMN_ADDED = "column_added";

    /**
     * Constructs a new instance of WSDLColumnsView.
     * 
     * @param model     document model being displayed.
     * @param viewType  type of column view.
     * @param lookup    from whence services are found.
     */
    public WSDLColumnsView(WSDLModel model, Lookup lookup) {
        super(new BorderLayout());
        breadCrumbs = new LinkPanel(this);
        add(breadCrumbs, BorderLayout.NORTH);
        columnView = new BasicColumnView();
        add(columnView, BorderLayout.CENTER);
        this.wsdlModel = model;
        this.lookup = lookup;
        clearColumns();
        Node rootNode = NodesFactory.getInstance().create(model.getDefinitions());
        WSDLColumn column = createColumnComponent(rootNode, true);
        appendColumn(column);
    }

    /**
     * Construct a default column component for the given component.
     *
     * @param  node      root node of the component.
     * @param  showRoot  true to show the root of the tree.
     */
    protected WSDLColumn createColumnComponent(Node node, boolean showRoot) {
        WSDLColumn panel = new WSDLColumn(this, node, showRoot);
        addColumnListener(panel);
        return panel;
    }

    /**
     * Add the pre-defined column listener to the given column.
     *
     * @param  column  WSDLColumn to which the listener is added.
     */
    public void addColumnListener(WSDLColumn column){
        if (!(columnListener == null && columnListenerClass == null)) {
            try {
                // Look for the method, e.g. addPropertyChangeListener.
                Method addListenerMethod = WSDLColumn.class.getMethod(
                        "add" + columnListenerClass.getSimpleName(),
                        columnListenerClass); // NOI18N
                addListenerMethod.invoke(column, columnListener);
            } catch (Exception e) {
                // This is not expected to happen, but log it if it does.
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
    }

    /**
     * Set the column listener for this column view. The listener will
     * be added to each column instance that has a corresponding "add"
     * method (e.g. "addPropertyChangeListener()").
     *
     * @param  listenerClass  class of the listener.
     * @param  listener       listener to be added.
     */
    public <L extends EventListener> void setColumnListener(
            Class<L> listenerClass, L listener) {
        columnListenerClass = listenerClass;
        columnListener = listener;
    }

    /**
     * Retrieve the column listener, if one is defined.
     *
     * @return  the column listener, or null if none.
     */
    public EventListener getColumnListener() {
        return columnListener;
    }

    public void propertyChange(PropertyChangeEvent event) {
        Object src = event.getSource();
        if (event.getPropertyName().equals(Column.PROP_TITLE) &&
                src instanceof Column) {
            // Update the link title to reflect the change.
            breadCrumbs.updateLink((Column) src);
        }
    }

    public void showComponent(WSDLComponent wc) {
        Node rootNode = null;
        Column currentColumn = getFirstColumn();
        ExplorerManager currentExplorer = null;
        if (currentColumn instanceof ExplorerManager.Provider) {
            currentExplorer = ((ExplorerManager.Provider)currentColumn).
                    getExplorerManager();
        }
        if (currentExplorer != null) {
            rootNode = currentExplorer.getRootContext();
        } else {
            NodesFactory factory = NodesFactory.getInstance();
            Definitions defs = wsdlModel.getDefinitions();
            rootNode = factory.create(defs);
            clearColumns();
            currentColumn = createColumnComponent(rootNode,false);
            appendColumn(currentColumn);
            currentExplorer = ExplorerManager.find(currentColumn.getComponent());
        }
        List<Node> path = UIUtilities.findPathFromRoot(rootNode, wc);
        if (path.isEmpty()) {
            return;
        }
        // retain existing columns if appropriate
        int idx = 0;
        Column tmpColumn = currentColumn;
        ExplorerManager tmpExplorer = currentExplorer;
        for (Node node:path) {
            boolean found = false;
            while (tmpExplorer != null && tmpExplorer.getRootContext() == node) {
                found = true;
                currentColumn = tmpColumn;
                currentExplorer = tmpExplorer;
                tmpColumn = getNextColumn(tmpColumn);
                if (!(tmpColumn instanceof ExplorerManager.Provider)) {
                    break;
                }
                tmpExplorer = ((ExplorerManager.Provider)tmpColumn).
                        getExplorerManager();
            }
            if (found) {
                idx++;
            } else {
                // remove columns if needed
                removeColumnsAfter(currentColumn);
                try {
                    currentExplorer.setSelectedNodes(new Node[]{});
                } catch (PropertyVetoException ex) {
                }
                break;
            }
        }
        // add necessary columns
        if (idx < path.size() - 1) {
            List<Column> columns = new ArrayList<Column>();
            for (Node node : path.subList(idx, path.size() - 1)) {
                currentColumn = createColumnComponent(node, true);
                columns.add(currentColumn);
            }
            Column[] arr = columns.toArray(new Column[columns.size()]);
            appendColumns(arr);
        }
        // select node representing component
        if (currentColumn instanceof ExplorerManager.Provider) {
            currentExplorer = ((ExplorerManager.Provider)currentColumn).
                    getExplorerManager();
        }
        try {
            if (currentExplorer != null) {
                if (path.size() <= 1) {
                    currentExplorer.setSelectedNodes(
                            new Node[] { });
                } else {
                    currentExplorer.setSelectedNodes(
                            new Node[] { path.get(path.size() - 1) });
                }
            }
        } catch (PropertyVetoException ex) {
        }
    }

    public void showComponent(SchemaComponent sc) {
        Node rootNode = null;
        Column currentColumn = getFirstColumn();
        ExplorerManager currentExplorer = null;
        if(currentColumn instanceof ExplorerManager.Provider)
            currentExplorer = ((ExplorerManager.Provider)currentColumn).
                    getExplorerManager();
        if(currentExplorer!=null) {
            rootNode = currentExplorer.getRootContext();
        } else {
            NodesFactory factory = NodesFactory.getInstance();
            Definitions defs = wsdlModel.getDefinitions();
            rootNode = factory.create(defs);
            clearColumns();
            currentColumn = createColumnComponent(rootNode,false);
            appendColumn(currentColumn);
            currentExplorer = ExplorerManager.find(currentColumn.getComponent());
        }
        List<Node> path = UIUtilities.findPathFromRoot(rootNode, sc, wsdlModel);
        if(path.isEmpty()) return;
        // retain existing columns if appropriate
        int idx = 0;
        Column tmpColumn = currentColumn;
        ExplorerManager tmpExplorer = currentExplorer;
        for(Node node:path.subList(0,path.size()-1)) {
            boolean found = false;
            while(tmpExplorer!=null&&tmpExplorer.getRootContext()==node) {
                found = true;
                currentColumn = tmpColumn;
                currentExplorer = tmpExplorer;
                tmpColumn = getNextColumn(tmpColumn);
                if(!(tmpColumn instanceof ExplorerManager.Provider)) break;
                tmpExplorer = ((ExplorerManager.Provider)tmpColumn).
                        getExplorerManager();
            }
            if(found) idx++;
            else // remove columns if needed
            {
                removeColumnsAfter(currentColumn);
                try {
                    currentExplorer.setSelectedNodes(new Node[]{});
                } catch (PropertyVetoException ex) {
                }
                break;
            }
        }
        // add necessary columns
        if (idx < path.size() - 1) {
            List<Column> columns = new ArrayList<Column>();
            for (Node node : path.subList(idx, path.size() - 1)) {
                currentColumn = createColumnComponent(node, true);
                columns.add(currentColumn);
            }
            Column[] arr = columns.toArray(new Column[columns.size()]);
            appendColumns(arr);
        }
        // select node representing component
        if(currentColumn instanceof ExplorerManager.Provider)
            currentExplorer = ((ExplorerManager.Provider)currentColumn).
                    getExplorerManager();
        try {
            if(currentExplorer!=null)
                currentExplorer.setSelectedNodes(
                        new Node[]{path.get(path.size()-1)});
        } catch (PropertyVetoException ex) {
        }
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        columnView.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return columnView.requestFocusInWindow();
    }
    
    //
    // ColumnView implementation
    //

    public void appendColumn(Column column) {
        column.addPropertyChangeListener(this);
        columnView.appendColumn(column);
        breadCrumbs.appendLink(column);
        firePropertyChange(PROP_COLUMN_ADDED, null, column);
    }

    public void appendColumns(Column[] columns) {
        for (Column column : columns) {
            column.addPropertyChangeListener(this);
            breadCrumbs.appendLink(column);
            firePropertyChange(PROP_COLUMN_ADDED, null, column);
        }
        columnView.appendColumns(columns);
    }

    public void clearColumns() {
        columnView.clearColumns();
        breadCrumbs.clearLinks();
    }

    public int getColumnCount() {
        return columnView.getColumnCount();
    }

    public int getColumnIndex(Column column) {
        return columnView.getColumnIndex(column);
    }

    public Column getFirstColumn() {
        return columnView.getFirstColumn();
    }

    public Column getNextColumn(Column column) {
        return columnView.getNextColumn(column);
    }

    public void removeColumnsAfter(Column column) {
        int index = columnView.getColumnIndex(column);
        columnView.removeColumnsAfter(column);
        // Remove the links after this column.
        breadCrumbs.truncateLinks(index + 1);
    }

    public void scrollToColumn(Column column, boolean synchronous) {
        columnView.scrollToColumn(column, synchronous);
    }

// IZ 96828: suppress help for nodes, just use WSDL view help topic.
//    public HelpCtx getHelpCtx() {
//        return new HelpCtx(WSDLColumnsView.class);
//    }
}
