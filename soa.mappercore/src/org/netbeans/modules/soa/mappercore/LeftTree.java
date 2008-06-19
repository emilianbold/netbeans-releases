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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.search.Navigation;
import org.netbeans.modules.soa.mappercore.utils.ScrollPaneWrapper;
import org.netbeans.modules.soa.mappercore.utils.Utils;
import org.openide.util.NbBundle;

/**
 * @author anjeleevich
 */
public class LeftTree extends JTree implements
        AdjustmentListener, TreeExpansionListener,
        Autoscroll 
{

    private Mapper mapper;
    private LeftTreeEventHandler eventHandler;
    public JComponent scrollPaneWrapper;
    public JScrollPane scrollPane;
    private boolean printMode = false;

    /** Creates a new instance of LeftTree */
    public LeftTree(Mapper mapper) {
        super((TreeModel) null);
        this.mapper = mapper;

        // vlv: print
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        putClientProperty("print.weight", new Integer(0)); // NOI18N

        scrollPane = new JScrollPane(this,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(this);
        
        // vlv
        //scrollPaneWrapper = new ScrollPaneWrapper(scrollPane);
        scrollPaneWrapper = new Navigation(this, scrollPane, new ScrollPaneWrapper(scrollPane));
        
        addTreeExpansionListener(this);
        setCellRenderer(new DefaultLeftTreeCellRenderer(mapper));
        eventHandler = new LeftTreeEventHandler(this);

        this.setRootVisible(false);
        this.setShowsRootHandles(true);

        //
        // Add the mouse listener for popup menu
        MouseListener pupupMouseListener = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Object source = e.getSource();
                    assert source instanceof JTree;
                    TreePath path = ((JTree) source).getPathForLocation(
                            e.getX(), e.getY());
                    if (path != null) {
                        Object lastComp = path.getLastPathComponent();
                        JPopupMenu popup = LeftTree.this.mapper.getContext().
                                getLeftPopupMenu(LeftTree.this.mapper.getModel(),
                                lastComp);
                        //
                        if (popup != null) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }
        };
        //
        this.addMouseListener(pupupMouseListener);
        
        ToolTipManager.sharedInstance().registerComponent(this);
        
        InputMap iMap = getInputMap();
        ActionMap aMap = getActionMap();
        
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK),
                "press-right-control");
        aMap.put("press-right-control", new RightControlAction());
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK), "show-popupMenu");
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), "show-popupMenu");
        aMap.put("show-popupMenu", new ShowPopupMenuAction());
        
        ViewTooltips.register(this);
        
        getAccessibleContext().setAccessibleName(NbBundle
                .getMessage(LeftTree.class, "ACSN_LeftTree")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle
                .getMessage(LeftTree.class, "ACSD_LeftTree")); // NOI18N
    }
    
    public void registrAction(MapperKeyboardAction action) {
        InputMap iMap = getInputMap();
        ActionMap aMap = getActionMap();

        String actionKey = action.getActionKey();
        aMap.put(actionKey, action);

        KeyStroke[] shortcuts = action.getShortcuts();
        if (shortcuts != null) {
            for (KeyStroke s : shortcuts) {
                iMap.put(s, actionKey);
            }
        }
    }
    
    @Override
    public String getToolTipText(MouseEvent event) {
        MapperModel model = getMapper().getModel();
        MapperContext context = getMapper().getContext();
        
        if (model == null || context == null) {
            return null;
        }
        
        TreePath treePath = getPathForLocation(event.getX(), event.getY());
        if (treePath == null) {
            return null;
        }
        
        Object value = treePath.getLastPathComponent();
        if (value == null) {
            return null;
        }
        
        return context.getLeftToolTipText(model, value);
    }
    

    public int yToMapper(int y) {
        for (Component c = this; c != mapper; c = c.getParent()) {
            y += c.getY();
        }

        return y;
    }

    public int yFromMapper(int y) {
        for (Component c = this; c != mapper; c = c.getParent()) {
            y -= c.getY();
        }

        return y;
    }
 
    public int getCenterY(TreePath treePath) {
        Rectangle bounds = getRowBounds(getRowForPath(treePath));
        
        while (bounds == null) {
            treePath = treePath.getParentPath();
            bounds = getRowBounds(getRowForPath(treePath));
        }

        return bounds.y + bounds.height / 2;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JComponent getView() {
        return scrollPaneWrapper;
    }

    public Mapper getMapper() {
        return mapper;
    }

    MapperNode getRoot() {
        return mapper.getRoot();
    }

    MapperModel getMapperModel() {
        return mapper.getFilteredModel();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width += mapper.getStepSize();
        return size;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Map<TreePath, Set<Link>> edgeTreePathes = getConnectedTreePathes();

        if (!edgeTreePathes.isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                       
            Object[][] edgePathes = new Object[edgeTreePathes.size()][];

            int x2 = getWidth() - 1;

            int edgePathIndex = 0;

            for (TreePath edgeTreePath : edgeTreePathes.keySet()) {
                edgePathes[edgePathIndex++] = edgeTreePath.getPath();
            }
            
            SelectionModel selectionModel = getMapper().getSelectionModel();
            TreePath rightTreePath = selectionModel.getSelectedPath();
            
            int rowCount = getRowCount();

            for (int row = 0; row < rowCount; row++) {
                Set<Link> connectedEdges = connectedEdges(row, getPathForRow(row),
                        edgeTreePathes, edgePathes);

                if (connectedEdges != null && connectedEdges.size() > 0) {
                    Rectangle rowBounds = getRowBounds(row);

                    int x1 = rowBounds.x + rowBounds.width;
                    int y = rowBounds.y + rowBounds.height / 2;
                    
                    Set<Link> linksForTreePath = edgeTreePathes.get(getPathForRow(row));
                    Color linkColor = MapperStyle.LINK_COLOR_UNSELECTED_NODE;
                    boolean hasSelectedLink = false;
                    
                    for (Link link : connectedEdges) {
                        if (selectionModel.isSelected(rightTreePath, link) &&
                                mapper.getNode(rightTreePath, true).isVisibleGraph()) {
                            linkColor = MapperStyle.SELECTION_COLOR;
                            hasSelectedLink = true;
                            break;
                        }
                        
                        if (linkColor != MapperStyle.LINK_COLOR_SELECTED_NODE) {
                            if (selectionModel.getSelectedGraph() == link.getGraph() 
                                    || parentPathIsSelected(mapper.
                                    getRightTreePathForLink(link).getParentPath()))  
                            {
                                linkColor = MapperStyle.LINK_COLOR_SELECTED_NODE;
                             
                                linkColor = MapperStyle.LINK_COLOR_SELECTED_NODE;
                            }
                        }
                        
                    }
                    
                    g2.setPaint(linkColor);
                    Stroke oldStroke = g2.getStroke();
                    if (linksForTreePath == null ||
                            connectedEdges.size() > linksForTreePath.size()) {

                        if (hasSelectedLink) {
                            g2.setStroke(Mapper.DASHED_SELECTED_STROKE);
                        } else {
                            g2.setStroke(Mapper.DASHED_STROKE);
                        }
                        g2.drawLine(x1, y, x2, y);

                    } else {
                        if (hasSelectedLink) {
                            g2.setStroke(MapperStyle.SELECTION_STROKE);
                        }
                        g2.drawLine(x1, y, x2, y);
                    }
                    g2.setStroke(oldStroke);
                }
            }

            g2.dispose();
        }

        getMapper().getLinkTool().paintLeftTree(this, g);
    }
    
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        super.repaint(tm, x, y, width, height);
        if (mapper == null) { return; }
        
        Canvas canvas = mapper.getCanvas();
        if (canvas == null) { return; }
        
        canvas.repaint();
    }

//    @Override
//    public void print(Graphics g) {
//        printMode = true;
//        super.print(g);
//        printMode = false;
//    }

    @Override
    public int getY() {
        if (printMode) {
            return 0;
        }
        return super.getY();
    }
    
    public void setPrintMode(boolean printMode) {
        this.printMode = printMode;
    }

    private Set<Link> connectedEdges(int row, TreePath treePath,
            Map<TreePath, Set<Link>> edgeTreePathes, Object[][] edgePathes) {
        if (getModel().isLeaf(treePath.getLastPathComponent()) || isExpanded(row)) {
            return edgeTreePathes.get(treePath);
        }

        Set<Link> result = new HashSet<Link>();
        if (edgeTreePathes.get(treePath) != null) {
            result.addAll(edgeTreePathes.get(treePath));
        }
        
//        int treePathLength = treePath.getPathCount();

//        for (int i = edgePathes.length - 1; i >= 0; i--) {
//            if (pathStartsWith(edgePathes[i], treePath, treePathLength)) {
//                result |= (edgePathes[i].length == treePathLength)
//                        ? EDGE_CONNECTED
//                        : EDGE_CONNECTED_TO_CHILD;
//               
//            }

//            if (result == 3) {
//                return 3;
//            }
//        }
        
        for (TreePath childTreePath : edgeTreePathes.keySet()) {
            if (treePath.isDescendant(childTreePath)) {
                result.addAll(edgeTreePathes.get(childTreePath));
            }
        }
        
        return result;
    }

    private boolean parentPathIsSelected(TreePath rightTreePath) {
        if (rightTreePath.getParentPath() == null) return false;
        
        MapperNode node = mapper.getNode(rightTreePath, true);
        
        if (node.isExpanded()) return false;
        if (node.isSelected()) return true;
        
        return parentPathIsSelected(rightTreePath.getParentPath());
    }

    private boolean pathStartsWith(Object[] path, TreePath treePath,
            int treePathLength) {
        int pathLength = path.length;

        if (pathLength < treePathLength) {
            return false;
        }

        for (int j = treePathLength - 1; j >= 0; j--) {
            if (treePath.getLastPathComponent() != path[j]) {
                return false;
            }

            treePath = treePath.getParentPath();
        }

        return true;
    }

    public Map<TreePath, Set<Link>> getConnectedTreePathes() {
        MapperNode root = getRoot();
        MapperModel model = getMapperModel();

        Map<TreePath, Set<Link>> treePathes = new HashMap<TreePath, Set<Link>>();

        if (root != null && model != null) {
            Set<Graph> connectedGraphs = getRoot().getChildGraphs();

            Graph rootGraph = getRoot().getGraph();
            if (rootGraph != null) {
                connectedGraphs.add(rootGraph);
            }

            if (connectedGraphs != null) {
                List<Link> edges = new ArrayList<Link>();

                for (Graph graph : connectedGraphs) {
                    graph.getIngoingLinks(edges);

                    for (int j = edges.size() - 1; j >= 0; j--) {
                        Link edge = edges.get(j);
                        TreePath treePath = ((TreeSourcePin) edge.getSource()).getTreePath();
                        Set<Link> links = treePathes.get(treePath);
                        if (links != null) {
                            treePathes.get(treePath).add(edge);
                        } else {
                            links = new HashSet<Link>();
                            links.add(edge);
                            treePathes.put(treePath, links);
                        }
                    }

                    edges.clear();
                }
            }
        }

        return treePathes;
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        mapper.getCanvas().repaint();
    }

    public void treeExpanded(TreeExpansionEvent event) {
        mapper.getCanvas().repaint();
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        mapper.getCanvas().repaint();
    }

    @Override
    public String convertValueToText(Object value, boolean selected,
            boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        MapperContext context = mapper.getContext();
        MapperModel model = mapper.getModel();

        if (value == null || context == null || model == null) {
            return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
        }
        return context.getLeftDysplayText(model, value);
    }
    
    public Link getOutgoingLinkForPath(TreePath treePath) {
        MapperNode root = getRoot();
        MapperModel model = getMapperModel();
        Link link = null;
        
        if (root != null && model != null) {
            Set<Graph> connectedGraphs = getRoot().getChildGraphs();

            Graph rootGraph = getRoot().getGraph();
            if (rootGraph != null) {
                connectedGraphs.add(rootGraph);
            }

            if (connectedGraphs != null) {
                List<Link> edges = new ArrayList<Link>();

                for (Graph graph : connectedGraphs) {
                    edges = graph.getIngoingLinks();

                    for (int j = edges.size() - 1; j >= 0; j--) {
                        link = edges.get(j);
                        if (Utils.equal(treePath, ((TreeSourcePin) link.getSource()).getTreePath())) {
                        //if (treePath == ((TreeSourcePin) link.getSource()).getTreePath()) {    
                            return link;
                        }
                    }
                    edges.clear();
                }
            }
        }
        return null;
    }
    
    public List<Link> getOutgoingLinksForRow(int row) {
        TreePath treePath = getPathForRow(row);
        return getAllOutgoingLinksForTreePath(treePath);
    }
    
    private List<Link> getAllOutgoingLinksForTreePath(TreePath treePath) {
        List<Link> links = new ArrayList<Link>();
        links.add(getOutgoingLinkForPath(treePath));
        TreeModel model = getModel();
        TreePath treePath1;
                        
        for (int i = 0; i < model.getChildCount(treePath);) {
            treePath1 = (TreePath) model.getChild(treePath, i);
            if (!model.isLeaf(treePath1)) {
                 links.addAll(getAllOutgoingLinksForTreePath(treePath1));  
            } else {
                links.add(getOutgoingLinkForPath(treePath1));
            }
        }
            
        return links;
    }
    
    public int getParentsRowForPath(TreePath treePath) {
        int row = getRowForPath(treePath);
        if (row > -1) return row;
        
        return getParentsRowForPath(treePath.getParentPath());
    }

    public Insets getAutoscrollInsets() {
        Rectangle rect = scrollPane.getViewport().getViewRect();
        return new Insets(rect.y + 16, rect.x + 16,
                getHeight() - rect.y - rect.height + 16,
                getWidth() - rect.x - rect.width + 16);
    }

    public void autoscroll(Point cursorLocn) {

        if (scrollPane.getViewport() == null) {
            return;
        }

        Insets insets = getAutoscrollInsets();

        Rectangle r = new Rectangle(cursorLocn.x, cursorLocn.y, 1, 1);
        if (cursorLocn.y > getHeight() - insets.bottom) {
            r.y = getHeight() - insets.bottom + 16 +
                    2 * scrollPane.getVerticalScrollBar().getUnitIncrement();
        }
        if (cursorLocn.y < insets.top) {
             r.y = insets.top - 16 -
                   2 * scrollPane.getVerticalScrollBar().getUnitIncrement();
        }
        if (cursorLocn.x > getWidth() - insets.right) {
            r.x = getWidth() - insets.right + 16 + 
                   2 * scrollPane.getHorizontalScrollBar().getUnitIncrement();
        }
        if (cursorLocn.x < insets.left) {
            r.x = insets.left - 16 -
                   2 * scrollPane.getHorizontalScrollBar().getUnitIncrement();
        }
        scrollRectToVisible(r);
    }
    private static final int EDGE_CONNECTED = 1;
    private static final int EDGE_CONNECTED_TO_CHILD = 2;

    private class RightControlAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            Mapper mapper = LeftTree.this.getMapper();
            TreePath path = LeftTree.this.getSelectionPath();
            
            mapper.getCanvas().requestFocusInWindow();
            
            Link link = LeftTree.this.getOutgoingLinkForPath(path);
            if (link == null) return;
            
            path = mapper.getRightTreePathForLink(link);
            mapper.getSelectionModel().setSelected(path, link);    
        }
    }
    
    private class ShowPopupMenuAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            LeftTree tree = LeftTree.this;
            TreePath path = tree.getSelectionPath();
            if (path == null) { return; }
            
            int row = tree.getRowForPath(path);
            if (row < 0) { return; }
            
            Rectangle rect = tree.getRowBounds(row);
            Object lastComp = path.getLastPathComponent();
            if (lastComp == null) { return; }
            
            JPopupMenu popup = tree.mapper.getContext().
                    getLeftPopupMenu(tree.mapper.getModel(), lastComp);
                   
            if (popup != null) {
                popup.show(tree, rect.x, rect.y);
            }  
        }
        
    }
}
