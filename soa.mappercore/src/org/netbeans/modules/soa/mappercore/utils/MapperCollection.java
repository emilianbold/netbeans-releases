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

package org.netbeans.modules.soa.mappercore.utils;

import java.util.Map;
import java.util.Set;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;

/**
 *
 * @author anjeleevich
 */
public abstract class MapperCollection {
    
    private MapperCollection() {}
    
    public abstract boolean contains(TreePath treePath, Vertex vertex);
    public abstract boolean contains(TreePath treePath, VertexItem vertexItem);
    
    
    public static MapperCollection create() {
        return EMPTY_COLLECTION;
    }
    
    
    public static MapperCollection createVerteces(TreePath treePath, 
            Set<Vertex> verteces)
    {
        return (verteces == null || verteces.isEmpty()) 
                ? EMPTY_COLLECTION
                : new VertecesCollection(treePath, verteces);
    }
    

    public static MapperCollection createVertexItems(TreePath treePath, 
            Set<VertexItem> vertexItems)
    {
        return (vertexItems == null || vertexItems.isEmpty()) 
                ? EMPTY_COLLECTION
                : new SingleGraphVertexItemCollection(treePath, vertexItems);
    }
    
    
    public static MapperCollection create(Map<TreePath, Set<VertexItem>> 
            vertexItems)
    {
        if (vertexItems == null || vertexItems.isEmpty()) return 
                EMPTY_COLLECTION;
        if (vertexItems.size() == 1) {
            return new SingleGraphVertexItemCollection(
                    vertexItems.keySet().iterator().next(),
                    vertexItems.values().iterator().next());
        }
    
        return new MultiGraphVertexItemsCollection(vertexItems);
    }
    
    
    private static class VertecesCollection extends MapperCollection {
        TreePath treePath;
        Set<Vertex> verteces;
        
        VertecesCollection(TreePath treePath, Set<Vertex> verteces) {
            this.treePath = treePath;
            this.verteces = verteces;
        }

        public boolean contains(TreePath treePath, Vertex vertex) {
            return Utils.equal(this.treePath, treePath) 
                    && verteces.contains(vertex);
       }
        
        public boolean contains(TreePath treePath, VertexItem vertexItem) {
            return false;
        }
    }
    
    
    private static class MultiGraphVertexItemsCollection 
            extends MapperCollection 
    {
        Map<TreePath, Set<VertexItem>> vertexItemsMap;
    
        MultiGraphVertexItemsCollection(Map<TreePath, Set<VertexItem>> 
                vertexItemsMap)
        {
            this.vertexItemsMap = vertexItemsMap;
        }

        public boolean contains(TreePath treePath, VertexItem vertexItem) {
            if (treePath == null) return false;
            
            Set<VertexItem> vertexItems = vertexItemsMap.get(treePath);
            return (vertexItems != null) && vertexItems.contains(vertexItem);
        }

        public boolean contains(TreePath treePath, Vertex vertex) {
            return false;
        }
    }
    
    
    private static class SingleGraphVertexItemCollection
            extends MapperCollection
    {
        TreePath treePath;
        Set<VertexItem> vertexItems;

        SingleGraphVertexItemCollection(TreePath treePath, 
                Set<VertexItem> vertexItems)
        {
            this.treePath = treePath;
            this.vertexItems = vertexItems;
        }
        
        
        public boolean contains(TreePath treePath, Vertex vertex) {
            return false;
        }
        

        public boolean contains(TreePath treePath, VertexItem vertexItem) {
            return Utils.equal(this.treePath, treePath)
                    && vertexItems.contains(vertexItem);
        }
    }
    
    
    
    private static final MapperCollection EMPTY_COLLECTION 
            = new MapperCollection() 
    {
        public boolean contains(TreePath treePath, Vertex vertex) {
            return false;
        }
        
        public boolean contains(TreePath treePath, VertexItem vertexItem) {
            return false;
        }
    };
}
