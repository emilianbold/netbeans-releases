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

package org.netbeans.modules.xml.schema.ui.basic;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaNodeFactory;
import org.netbeans.modules.xml.schema.ui.nodes.StructuralSchemaNodeFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.xam.ui.column.LinkPanel;
import org.netbeans.modules.xml.xam.ui.column.Column;
import org.netbeans.modules.xml.xam.ui.column.ColumnView;
import org.netbeans.modules.xml.xam.ui.column.BasicColumnView;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Displays the XML schema in a series of columns, each usually
 * containing a tree representing a subset of the schema model.
 * 
 * <p><em>Note: SchemaColumnsView, like all NbColumnView subclasses, has its
 * own JScrollPane, you do not need to place it in a JScrollPane.</em></p>
 * 
 * @author Todd Fast, todd.fast@sun.com
 * @author Nathan Fiedler
 * @author Jeri Lockhart
 */
public class SchemaColumnsView extends JPanel
        implements ColumnView, PropertyChangeListener {
    /** silence compiler warnings */
    static final long serialVersionUID = 1L;
    /** Where the columns are shown. */
    private transient BasicColumnView columnView;
    /** Where the bread crumbs live. */
    private transient LinkPanel breadCrumbs;
    private transient SchemaModel schemaModel;
    private transient SchemaNodeFactory factory;
    private ViewType viewType;
    private transient Lookup lookup;
    private Class<? extends EventListener> columnListenerClass;
    private transient EventListener columnListener;
    public static final String PROP_COLUMN_ADDED = "column_added";

    public static enum ViewType {
        /** The developer will set the node factory directly */
        CUSTOM,
        /** Use the categorized node factory */
        CATEGORIZED,
        /** Use the structural node factory */
        STRUCTURAL;
    }

    /**
     * Constructs a new instance of SchemaColumnsView.
     * 
     * @param model     schema model being displayed.
     * @param viewType  type of column view.
     * @param lookup    from whence services are found.
     */
    public SchemaColumnsView(SchemaModel model, ViewType viewType,
            Lookup lookup) {
        super(new BorderLayout());
        breadCrumbs = new LinkPanel(this);
        add(breadCrumbs, BorderLayout.NORTH);
        columnView = new BasicColumnView();
        add(columnView, BorderLayout.CENTER);
        this.schemaModel = model;
        this.lookup = lookup;
        _setViewType(viewType);
    }

    /**
     * Set the schema node factory.
     *
     * @param  factory  new schema node factory.
     */
    public void setNodeFactory(SchemaNodeFactory factory) {
        _setNodeFactory(factory);
    }

    /**
     * Private version of <code>setNodeFactory()</code> so it can be safely
     * called from the constructor.
     *
     * @param  factory  new schema node factory.
     */
    private void _setNodeFactory(SchemaNodeFactory factory) {
        this.factory = factory;
        clearColumns();
        Node rootNode = factory.createRootNode();
        SchemaColumn rootPanel = createColumnComponent(rootNode, true);
        appendColumn(rootPanel);
    }

    /**
     * Construct a default column component for the given schema component.
     *
     * @param  node      root node of the schema component.
     * @param  showRoot  true to show the root of the tree.
     */
    protected SchemaColumn createColumnComponent(Node node, boolean showRoot) {
        SchemaColumn panel = new SchemaColumn(this, node, showRoot);
        addColumnListener(panel);
        return panel;
    }

    /**
     * Add the pre-defined column listener to the given column.
     *
     * @param  column  SchemaColumn to which the listener is added.
     */
    public void addColumnListener(SchemaColumn column){
        if (!(columnListener == null && columnListenerClass == null)) {
            try {
                // Look for the method, e.g. addPropertyChangeListener.
                Method addListenerMethod = SchemaColumn.class.getMethod(
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

    /**
     * Private version of <code>setViewType()</code> so it can be safely
     * called from the constructor.
     *
     * @param  type  the type of schema column view.
     */
    private void _setViewType(ViewType type) {
        if (viewType != type) {
            viewType = type;
            SchemaNodeFactory factory = createNodeFactory(type);
            if (factory != null) {
                _setNodeFactory(factory);
            }
        }
    }

    /**
     * Create the SchemaNodeFactory appropriate for the given view type.
     *
     * @param  type  the type of schema column view.
     */
    private SchemaNodeFactory createNodeFactory(ViewType type) {
        SchemaNodeFactory factory;
        switch (type) {
            case CATEGORIZED:
                factory = new CategorizedSchemaNodeFactory(schemaModel, lookup);
                break;
            case STRUCTURAL:
                factory = new StructuralSchemaNodeFactory(schemaModel, lookup);
                break;
            default:
                factory = null;
                break;
        }
        return factory;
    }

    public void propertyChange(PropertyChangeEvent event) {
        Object src = event.getSource();
        if (event.getPropertyName().equals(Column.PROP_TITLE) &&
                src instanceof Column) {
            // Update the link title to reflect the change.
            breadCrumbs.updateLink((Column) src);
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
            rootNode = factory.createRootNode();
            clearColumns();
            currentColumn = createColumnComponent(rootNode,false);
            appendColumn(currentColumn);
            currentExplorer = ExplorerManager.find(currentColumn.getComponent());
        }
        List<Node> path = UIUtilities.findPathFromRoot(rootNode,sc);
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

    /**
     * Remove our listeners from the columns, starting with the given
     * column and moving toward the right.
     *
     * @param  col  first column from which to remove listeners.
     */
    private void removeListeners(Column col) {
        while (col != null) {
            col.removePropertyChangeListener(this);
            col = getNextColumn(col);
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
        removeListeners(getFirstColumn());
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
        removeListeners(getNextColumn(column));
        int index = columnView.getColumnIndex(column);
        columnView.removeColumnsAfter(column);
        // Remove the links after this column.
        breadCrumbs.truncateLinks(index + 1);
    }

    public void scrollToColumn(Column column, boolean synchronous) {
        columnView.scrollToColumn(column, synchronous);
    }
}
