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

package org.netbeans.modules.soa.mappercore.demo;

import java.awt.datatransfer.Transferable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.MapperStyle;
import org.netbeans.modules.soa.mappercore.icons.NumberIcon2D;
import org.netbeans.modules.soa.mappercore.icons.PlusIcon2D;
import org.netbeans.modules.soa.mappercore.icons.StringIcon2D;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.Function;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Operation;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.mappercore.utils.GraphLayout;

/**
 *
 * @author anjeleevich
 */
public class DemoMapperModel extends DefaultTreeModel implements MapperModel {
    
    private Map<Object, Graph> graphs = new HashMap<Object, Graph>();
    
    private Map<TreePath, TreeSourcePin> treeEdgeSources 
            = new HashMap<TreePath, TreeSourcePin>();
    
    private TreeModel leftTreeModel;
    
    private Graph stubGraph;

    public DemoMapperModel() {
        super(new DefaultMutableTreeNode("Root Node"));
        
        leftTreeModel = createDefaultTreeModel();
        
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
        DefaultMutableTreeNode music = new DefaultMutableTreeNode("Music");
        DefaultMutableTreeNode musicClassic = new DefaultMutableTreeNode("Classic");
        DefaultMutableTreeNode musicClassicVagner = new DefaultMutableTreeNode("Richard Wagner");
        DefaultMutableTreeNode musicClassicBeethoven = new DefaultMutableTreeNode("Ludwig van Beethoven");
        DefaultMutableTreeNode musicRockAndRoll = new DefaultMutableTreeNode("Rock and Roll");
        DefaultMutableTreeNode musicRockAndRollTRex = new DefaultMutableTreeNode("T-Rex");
        DefaultMutableTreeNode fruits = new DefaultMutableTreeNode("Fruits");
        
        musicClassic.add(musicClassicVagner);
        musicClassic.add(musicClassicBeethoven);
        
        musicRockAndRoll.add(musicRockAndRollTRex);
        
        music.add(musicClassic);
        music.add(musicRockAndRoll);
        
        root.add(music);
        root.add(fruits);
        
        graphs.put(musicClassicBeethoven, createSamleGraph1());
        graphs.put(musicRockAndRoll, createSamleGraph2());
        
        stubGraph = new Graph(this, null);
    }
    
    
    public TreeModel getLeftTreeModel() {
        return leftTreeModel;
    }
    
    
    public void addChild(TreePath path, String childName) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
                .getLastPathComponent();
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(childName);
        
        int index = node.getChildCount();
        node.add(child);
        
        nodesWereInserted(node, new int[] { index });
    }
    
    
    public void remove(TreePath path) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path
                .getParentPath().getLastPathComponent();
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) path
                .getLastPathComponent();
        
        int index = parent.getIndex(child);
        parent.remove(index);
        
        nodesWereRemoved(parent, new int[] { index }, new Object[] { child });
    }

    
    public Graph getGraph(TreePath treePath) {
        Graph result = graphs.get(treePath.getLastPathComponent());
        return (result == null) ? stubGraph : result;
    }

    
    public boolean searchGraphsInside(TreePath treePath) {
        return !isLeaf(treePath.getLastPathComponent());
    }
    
    
    public TreeSourcePin getTreeSourcePin(TreePath treePath) {
        TreeSourcePin source = treeEdgeSources.get(treePath);
        
        if (source == null) {
            source = new TreeSourcePin(treePath);
            treeEdgeSources.put(treePath, source);
        }
        
        return source;
    }


    private Graph createSamleGraph1() {
        Graph graph = new Graph(this, null);
        
        Constant number1 = createNumber(1200);
        Constant number2 = createNumber(1600);
        Function func = createConcat();
        
        number1.setLocation(5, 0);
        number2.setLocation(-1, 5);
        func.setLocation(20, 4);

        Link link1 = new Link(number1, func.getItem(1));
//        Link link2 = new Link(number2, func.getItem(3));
        Link link3 = new Link(func, graph);

        graph.addVertex(number1);
        graph.addVertex(number2);
        graph.addVertex(func);
        graph.addLink(link1);
//        graph.addLink(link2);
        graph.addLink(link3);
        
        GraphLayout.layout(graph);
        
        return graph;
    }
    
    
    private Graph createSamleGraph2() {
        Graph graph = new Graph(this, null);

        Operation plus1 = createPlus();
        Operation plus2 = createPlus();

        plus1.setLocation(10, 4);
        
        Object[] path1 = { leftTreeModel.getRoot(), null, null };
        path1[1] = leftTreeModel.getChild(path1[0], 1);
        path1[2] = leftTreeModel.getChild(path1[1], 2);

        Object[] path2 = { leftTreeModel.getRoot(), null, null };
        path2[1] = leftTreeModel.getChild(path2[0], 2);
        path2[2] = leftTreeModel.getChild(path2[1], 1);

        Link link1 = new Link(plus1, graph);
        Link link2 = new Link(getTreeSourcePin(new TreePath(path1)), plus2.getItem(1));
        Link link3 = new Link(getTreeSourcePin(new TreePath(path2)), plus2.getItem(0));
        
        graph.addVertex(plus1);
        graph.addVertex(plus2);
        graph.addLink(link1);
        graph.addLink(link2);
        graph.addLink(link3);
        
        return graph;
    }
  
    
    private static Function createConcat() {
        Function function = new Function(null, null, "Concat", 
                "Returns String");
        function.addItem(new VertexItem(function, null));
        function.addItem(new VertexItem(function, null, "Huba", String.class));
        function.addItem(new VertexItem(function, null, null, String.class));
        return function;
    }
    
    
    private static Operation createPlus() {
        Operation operation = new Operation(null, null);
        operation.addItem(new VertexItem(operation, null, Number.class));
        operation.addItem(new VertexItem(operation, null, Number.class));
        return operation;
    }
    
    
    private static Constant createNumber(int value) {
        Constant constant = new Constant(null, null);
        constant.addItem(new VertexItem(constant, null, new Integer(value), Number.class));
        return constant;
    }
    
    
    private static TreeModel createDefaultTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("LeftTree");
	DefaultMutableTreeNode parent;

	parent = new DefaultMutableTreeNode("colors");
	root.add(parent);
	parent.add(new DefaultMutableTreeNode("blue"));
	parent.add(new DefaultMutableTreeNode("violet"));
	parent.add(new DefaultMutableTreeNode("red"));
	parent.add(new DefaultMutableTreeNode("yellow"));

	parent = new DefaultMutableTreeNode("sports");
	root.add(parent);
	parent.add(new DefaultMutableTreeNode("basketball"));
	parent.add(new DefaultMutableTreeNode("soccer"));
	parent.add(new DefaultMutableTreeNode("football"));
	parent.add(new DefaultMutableTreeNode("hockey"));

	parent = new DefaultMutableTreeNode("food");
	root.add(parent);
	parent.add(new DefaultMutableTreeNode("hot dogs"));
	parent.add(new DefaultMutableTreeNode("pizza"));
	parent.add(new DefaultMutableTreeNode("ravioli"));
	parent.add(new DefaultMutableTreeNode("bananas"));
        return new DefaultTreeModel(root);
    }

    public boolean canConnect(TreePath treePath, SourcePin source, 
            TargetPin target, TreePath oldTreePath, Link oldLink) 
    {
        if (oldLink != null) return false;
//        if (source instanceof TreeSourcePin) {
//            TreePath path = ((TreeSourcePin) source).getTreePath();
//            return !path.getLastPathComponent().toString().equals("colors");
//        }
//            
//        if (source instanceof Vertex) {
//            return ((Vertex) source).getOutgoingLink() == null;
//        }
//        
//        if (target instanceof VertexItem) {
//            return ((VertexItem) target).getIngoingLink() == null;
//        }
//        
//        if (target instanceof Graph) {
//            return !((Graph) target).hasIngoingLinks();
//        }
//        
//        return true;
        return true;
    }

    
    public void connect(TreePath treePath, SourcePin source, TargetPin target, 
            TreePath oldTreePath, Link oldLink) 
    {
        if (oldTreePath != null && oldLink != null) {
            oldLink.disconnect();
        }
        
        Object parent = treePath.getLastPathComponent();
        
        Graph graph = getGraph(treePath);
        
        Graph resultGraph;
        
        if (graph == stubGraph) {
            resultGraph = new Graph(this, null);
            target = resultGraph;
        } else {
            resultGraph = graph;
        }
        
        Link link = new Link(source, target);
        resultGraph.addLink(link);
        
        if (resultGraph != graph) {
            graphs.put(parent, resultGraph);
        }
        
        TreePath parentPath = treePath.getParentPath();
        
        if (parentPath != null) {
            Object parentParent = parentPath.getLastPathComponent();
            int index = getIndexOfChild(parentParent, parent);
            fireTreeNodesChanged(this, parentPath.getPath(), 
                    new int[] { index }, new Object[] { parent });
        } else {
            fireTreeNodesChanged(this, treePath.getPath(), null, null);
        }
   }

    
    public GraphSubset getGraphSubset(Transferable transferable) {
        Function function = new Function(null, null, "Drop Test", "Something");
        function.addItem(new VertexItem(function, "blah, blah"));
        return new GraphSubset(function);
    }

    
    public GraphSubset copy(TreePath treePath, GraphSubset graphSubset, int x, int y) {
        Object parent = treePath.getLastPathComponent();
        
        Graph graph = getGraph(treePath);
        
        Graph resultGraph;
        
        if (graph == stubGraph) {
            resultGraph = new Graph(this, null);
        } else {
            resultGraph = graph;
        }
        
        for (int i = graphSubset.getVertexCount() - 1; i >= 0; i--) {
            Vertex vertex = graphSubset.getVertex(i);
            vertex.setLocation(x, y);
            resultGraph.addVertex(vertex);
        }
        
        if (resultGraph != graph) {
            graphs.put(parent, resultGraph);
        }
        
        TreePath parentPath = treePath.getParentPath();
        
        if (parentPath != null) {
            Object parentParent = parentPath.getLastPathComponent();
            int index = getIndexOfChild(parentParent, parent);
            fireTreeNodesChanged(this, parentPath.getPath(), 
                    new int[] { index }, new Object[] { parent });
        } else {
            fireTreeNodesChanged(this, treePath.getPath(), null, null);
        }
        return null;
    }
    
    
    public void move(TreePath treePath, GraphSubset graphSubset, int x, int y) {
        copy(treePath, graphSubset, x, y);
    }

    
    public boolean canCopy(TreePath treePath, GraphSubset graphSubset) {
        return true;
    }


    public boolean canMove(TreePath treePath, GraphSubset graphSubset) {
        return true;
    }
    
    
    public void valueChanged(TreePath treePath, VertexItem vertexItem, 
            Object newValue)
    {
        vertexItem.setValue(newValue);
    }

    public boolean canEditInplace(VertexItem vItem) {
        return true;
    }

    public void delete(TreePath currentTreePath, GraphSubset graphGroup) {
        return;
    }

    public List<TreePath> findInLeftTree(String value) {
        return null;
    }

    public List<TreePath> findInRightTree(String value) {
        return null;
    }
}
