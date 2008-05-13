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

package org.netbeans.modules.soa.mappercore;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionEvent;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionListener;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.GraphListener;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.mappercore.utils.MapperTreePath;
import org.netbeans.modules.soa.mappercore.utils.Utils;

/**
 *
 * @author anjeleevich
 */
public class SelectionModel extends MapperPropertyAccess {
    
    private MapperModel model = null;
    
    private TreePath selectedPath = null;
    private Graph selectedGraph = null;
    
    private List<GraphItem> selected = new ArrayList<GraphItem>();
    
    private ListenersImpl listenersImpl;
    private List<MapperSelectionListener> listeners 
            = new ArrayList<MapperSelectionListener>();
    
    
    public SelectionModel(Mapper mapper) {
        super(mapper);
        listenersImpl = new ListenersImpl();
        
        updateMapperModel();
        
        mapper.addPropertyChangeListener(listenersImpl);
        mapper.addRightTreeExpansionListener(listenersImpl);
    }
    
    
    public void addSelectionListener(MapperSelectionListener listener) {
        listeners.add(listener);
    }
    
    
    public void removeSelectionListener(MapperSelectionListener listener) {
        int index = listeners.lastIndexOf(listener);
        if (index >= 0) {
            listeners.remove(index);
        }
    }
    
    
    private void fireSelectionChanged() {
        int size = listeners.size();
        if (size > 0) {
            MapperSelectionEvent event = new MapperSelectionEvent(this);
            for (MapperSelectionListener l : listeners
                    .toArray(new MapperSelectionListener[size])) 
            {
                l.mapperSelectionChanged(event);
            }
        }
    }
    
    
    public GraphSubset getSelectedSubset() {
        if (selectedPath != null && selectedGraph != null) {
            return new GraphSubset(selectedPath, selectedGraph,
                    getSelectedVerteces(), getSelectedLinks());
        } 
        return null;
    }
    
    
    public List<Vertex> getSelectedVerteces() {
        return getSelectedImpl(Vertex.class);
    }
    
    
    public List<Link> getSelectedLinks() {
        return getSelectedImpl(Link.class);
    }
    
    
    public TreePath getSelectedPath() {
        return selectedPath;
    }
    
    
    public Graph getSelectedGraph() {
        return selectedGraph;
    }
    
    
    public VertexItem getSelectedVertexItem() {
        if (selected.size() == 1) {
            GraphItem graphItem = selected.get(0);
            if (graphItem instanceof VertexItem) {
                return (VertexItem) graphItem;
            }
        }
        return null;
    }
    
   
    public void setSelected(TreePath treePath) {
        if (setSelectedPathImpl(treePath)) {
            fireSelectionChanged();
        }
    }
    
    
    public void setSelected(TreePath treePath, GraphItem graphItem) {
        boolean changed = setSelectedPathImpl(treePath);
        
        if (selected.size() != 1 || selected.get(0) != graphItem) {
            selected.clear();
            selected.add(graphItem);
            changed = true;
        }
        
        if (changed) {
            fireSelectionChanged();
        }
    }
    
    
    public void switchSelected(TreePath treePath, GraphItem graphItem) {
        boolean changed = setSelectedPathImpl(treePath);
        
        if (graphItem instanceof VertexItem) {
            graphItem = ((VertexItem) graphItem).getVertex();
        }
        
        if (selected.contains(graphItem)) {
            selected.remove(graphItem);
            changed = true;
        } else {
            selected.add(graphItem);
            changed = true;
        }
        
        changed |= removeVertexItemsImpl();
        
        if (changed) {
            fireSelectionChanged();
        }
    }
    
    public void setSelected(TreePath treePath, GraphSubset graphSubset) {
        boolean changed = setSelectedPathImpl(treePath);
        selected.clear();

        for (int i = 0; i < graphSubset.getVertexCount(); i++) {
            Vertex vertex = graphSubset.getVertex(i);
            selected.add(vertex);
            changed = true;
        }

        for (int i = 0; i < graphSubset.getLinkCount(); i++) {
            Link link = graphSubset.getLink(i);
            selected.add(link);
            changed = true;
        }

        if (changed) {
            fireSelectionChanged();
        }
    }
    
    
    public void clearSelection(TreePath treePath) {
        boolean changed = setSelectedPathImpl(treePath);
        
        if (!selected.isEmpty()) {
            selected.clear();
            changed = true;
        }
        
        if (changed) {
            fireSelectionChanged();
        }
    }
    
    
    public void selectAll(TreePath treePath) {
        boolean changed = setSelectedPathImpl(treePath);
        
        if (selectedGraph != null) {
            Set<GraphItem> select = new HashSet<GraphItem>();
            for (int i = selectedGraph.getLinkCount() - 1; i >= 0; i--) {
                select.add(selectedGraph.getLink(i));
            }

            for (int i = selectedGraph.getVertexCount() - 1; i >= 0; i--) {
                select.add(selectedGraph.getVertex(i));
            }
            
            select.removeAll(selected);
            
            if (!select.isEmpty()) {
                selected.addAll(0, select);
                changed = true;
            }
            
            changed |= removeVertexItemsImpl();
        }
        
        if (changed) {
            fireSelectionChanged();
        }
    }
    
    
    private boolean removeVertexItemsImpl() {
        boolean changed = false;
        
        for (int i = selected.size() - 1; i >= 0; i--) {
            if (selected.get(i) instanceof VertexItem) {
                selected.remove(i);
                changed = true;
            }
        }
        
        return changed;
    }
    
    

    private <T extends GraphItem> List<T> getSelectedImpl(Class<T> type) {
        List<T> result = new ArrayList<T>(selected.size());
        for (Object o : selected) {
            if (type.isInstance(o)) {
                result.add(type.cast(o));
            }
        }
        return result;
    }
    
    
    private boolean setSelectedPathImpl(TreePath treePath) {
        if (!Utils.equal(this.selectedPath, treePath)) {
            selected.clear();
            
            this.selectedPath = treePath;

            updateGraph(false);
            
            return true;
        }
        
        return false;
    }

    

    public boolean isSelected(TreePath treePath) {
        return Utils.equal(selectedPath, treePath);
    }
    
    
    public boolean isSelected(TreePath treePath, GraphItem graphItem) {
        return isSelected(treePath) && selected.contains(graphItem);
    }
    
    
    private void updateMapperModel() {
        MapperModel oldModel = this.model;
        MapperModel newModel = getMapperModel();
        
        if (oldModel != newModel) {
            if (oldModel != null) {
                oldModel.removeTreeModelListener(listenersImpl);
            }
            
            if (newModel != null) {
                newModel.addTreeModelListener(listenersImpl);
            }
            
            this.model = newModel;
    
            setSelectedPathImpl(null);
        }
    }
    
    
    private void updateGraph(boolean fireEvent) {
        Graph oldGraph = selectedGraph;
        Graph newGraph = (model != null && selectedPath != null) 
                ? model.getGraph(selectedPath) : null;
        
        if (oldGraph != newGraph) {
            if (oldGraph != null) {
                oldGraph.removeGraphListener(listenersImpl);
            }
            
            if (newGraph != null) {
                newGraph.addGraphListener(listenersImpl);
            }
            
            selectedGraph = newGraph;
            selected.clear();
            
            if (fireEvent) {
                fireSelectionChanged();
            }
        }
    }
    
    
    private void updateSelectedItems() {
        boolean changed = false;
        
        if (selectedGraph != null && !selected.isEmpty()) {
            Set<GraphItem> difference = new HashSet<GraphItem>(selected);
            
            for (int i = selectedGraph.getVertexCount() - 1; i >= 0; i--) {
                difference.remove(selectedGraph.getVertex(i));
            }

            for (int i = selectedGraph.getLinkCount() - 1; i >= 0; i--) {
                difference.remove(selectedGraph.getLink(i));
            }
            
            for (GraphItem item : difference) {
                if (item instanceof VertexItem) {
                    VertexItem vertexItem = (VertexItem) item;
                    Vertex vertex = vertexItem.getVertex();
                    
                    if (vertex.getItemIndex(vertexItem) < 0) {
                        selected.remove(item);
                        changed = true;
                    } else if (vertex.getGraph() != selectedGraph) {
                        selected.remove(item);
                        changed = true;
                    }
                } else {
                    selected.remove(item);
                    changed = true;
                }
            }
        }
        
        if (changed) {
            fireSelectionChanged();
        }
    }

    
    private class ListenersImpl implements GraphListener, TreeModelListener, 
            PropertyChangeListener, TreeExpansionListener 
    {
        public void graphBoundsChanged(Graph graph) {
            updateSelectedItems();
        }

        public void graphLinksChanged(Graph graph) {
            updateSelectedItems();
        }

        public void graphContentChanged(Graph graph) {
            updateSelectedItems();
        }

        
        public void treeNodesChanged(TreeModelEvent e) {
            TreePath parentPath = e.getTreePath();
            Object[] children = e.getChildren();
            
            for (int i = 0; i < children.length; i++) {
                TreePath changedPath = new MapperTreePath(parentPath, 
                        children[i]);
                if (Utils.equal(changedPath, selectedPath)) {
                    updateGraph(true);
                }
            }
        }
        

        public void treeNodesInserted(TreeModelEvent e) {}
        

        public void treeNodesRemoved(TreeModelEvent e) {
            TreePath parentPath = e.getTreePath();
            Object parent = parentPath.getLastPathComponent();
            
            Object[] children = e.getChildren();
            
            for (int i = 0; i < children.length; i++) {
                Object removedChild = children[i];
                TreePath removedPath = new MapperTreePath(parentPath, 
                        removedChild);

                boolean changeSelection = false;

                for (TreePath path = selectedPath; path != null; 
                        path = path.getParentPath())
                {
                    if (Utils.equal(path, removedPath)) {
                        changeSelection = true;
                        break;
                    }
                }
                
                if (changeSelection) {
                    MapperModel model = getMapperModel();
                    int count = model.getChildCount(parent);
                    int newSelectedIndex = Math.min(e.getChildIndices()[i], 
                            count - 1);
                    
                    if (newSelectedIndex < 0) {
                        setSelected(parentPath);
                    } else {
                        setSelected(new MapperTreePath(parentPath, 
                                model.getChild(parent, newSelectedIndex)));
                    }
                }
            }
        }
        

        public void treeStructureChanged(TreeModelEvent e) {
//            validateSelection();
        }
        

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == getMapper()
                    && evt.getPropertyName() == Mapper.MODEL_PROPERTY)
            {
                updateMapperModel();
            }
        }

        
        public void treeExpanded(TreeExpansionEvent event) {}

        
        public void treeCollapsed(TreeExpansionEvent event) {
            TreePath path = event.getPath();
            TreePath selectedPath = SelectionModel.this.selectedPath;
            
            if (selectedPath != null) {
                selectedPath = selectedPath.getParentPath();
                while (selectedPath != null) {
                    if (Utils.equal(selectedPath, path)) {
                        setSelectedPathImpl(path);
                        return;
                    }
                    selectedPath = selectedPath.getParentPath();
                }
            }
        }
    }
}
