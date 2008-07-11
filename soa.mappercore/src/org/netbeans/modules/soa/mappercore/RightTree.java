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
import java.awt.ComponentOrientation;
import java.awt.Container;
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
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneLayout;
import javax.swing.ToolTipManager;
import javax.swing.ViewportLayout;
import javax.swing.border.Border;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.graphics.VerticalGradient;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.utils.ScrollPaneWrapper;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class RightTree extends MapperPanel implements
        FocusListener, Autoscroll {

    private RightTreeEventHandler eventHandler;
    private JScrollPane scrollPane;
    private ScrollPaneWrapper scrollPaneWrapper;
    private CellRendererPane cellRendererPane;
    private JLabel childrenLabel;
    private RightTreeCellRenderer treeCellRenderer = new DefaultRightTreeCellRenderer();
    private ActionListener actionEscape;
        
    RightTree(Mapper mapper) {
        super(mapper);

        // vlv: print
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        putClientProperty("print.weight", new Integer(2)); // NOI18N
        
        setBackground(Color.WHITE);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane
                .HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane
                .VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setLayout(new RightScrollPaneLayout());
        scrollPane.getViewport().setLayout(new RightViewportLayout());
        scrollPane.setViewportView(this);
        scrollPane.setRowHeaderView(new RowHeader());
        scrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, new BottomLeftCorner());
        scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        scrollPaneWrapper = new ScrollPaneWrapper(scrollPane);

        cellRendererPane = new CellRendererPane();

        childrenLabel = new ChildrenLabel();
        cellRendererPane.add(childrenLabel);

        add(cellRendererPane);
        addFocusListener(this);

        eventHandler = new RightTreeEventHandler(this);

        InputMap iMap = getInputMap();
        ActionMap aMap = getActionMap();

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "press-moveSelectionDown");
        aMap.put("press-moveSelectionDown", new MoveSelectionDown());

        ToolTipManager.sharedInstance().registerComponent(this);
        actionEscape = getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "press-moveSelectionUp");
        aMap.put("press-moveSelectionUp", new MoveSelectionUp());

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), "press-moveSelectionDown+Control");
        aMap.put("press-moveSelectionDown+Control", new MoveSelectionDown());

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), "press-moveSelectionUp+Control");
        aMap.put("press-moveSelectionUp+Control", new MoveSelectionUp());

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "press-left-expand");
        aMap.put("press-left-expand", new PressLeftExpand());

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "press-right-collapse");
        aMap.put("press-right-collapse", new PressRightCollapse());

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), "press-left-expandGraph");
        aMap.put("press-left-expandGraph", new PressLeftExpandGraph());

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK), "press-right-collapseGraph");
        aMap.put("press-right-collapseGraph", new PressRightCollapseGraph());

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK), "auto-scroll-down");
        aMap.put("auto-scroll-down", new AutoScrollDown());

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK), "auto-scroll-up");
        aMap.put("auto-scroll-up", new AutoScrollUp());

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK), "auto-scroll-left");
        aMap.put("auto-scroll-left", new AutoScrollLeft());

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK), "auto-scroll-right");
        aMap.put("auto-scroll-right", new AutoScrollRight());
        
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK), "show-popupMenu");
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), "show-popupMenu");
        aMap.put("show-popupMenu", new ShowPopupMenuAction());
        
        ViewTooltips.register(this);
        
        getAccessibleContext().setAccessibleName(NbBundle
                .getMessage(RightTree.class, "ACSN_RightTree")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle
                .getMessage(RightTree.class, "ACSD_RightTree")); // NOI18N
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

    public ActionListener getActionEscape() {
        return actionEscape;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        MapperModel model = getMapper().getModel();
        MapperContext context = getMapper().getContext();

        if (model == null || context == null) {
            return null;
        }

        MapperNode node = getNodeAt(event.getY());
        if (node == null) {
            return null;
        }

        TreePath treePath = node.getTreePath();
        if (treePath == null) {
            return null;
        }

        Object value = treePath.getLastPathComponent();
        if (value == null) {
            return null;
        }

        return context.getRightToolTipText(model, value);
    }

    JLabel getChildrenLabel() {
        return childrenLabel;
    }

    Component getCellRendererComponent(MapperNode node) {
        Mapper mapper = getMapper();
        RightTreeCellRenderer renderer = mapper.getRightTreeCellRenderer();
        Component c = renderer.getTreeCellRendererComponent(mapper,
                node.getValue(), false, node.isExpanded(), node.isLeaf(), 0, false);
        cellRendererPane.add(c);
        return c;

    }

    CellRendererPane getCellRendererPane() {
        return cellRendererPane;
    }

    RightTreeCellRenderer getTreeCellRenderer() {
        return treeCellRenderer;
    }

    JScrollPane getScrollPane() {
        return scrollPane;
    }

    JComponent getView() {
        return scrollPaneWrapper;
    }

    MapperContext getContext() {
        return getMapper().getContext();
    }

    @Override
    public Dimension getPreferredSize() {
        Mapper mapper = getMapper();

        Dimension size = mapper.getPreferredTreeSize();

        if (size != null) {
            size.width += mapper.getStepSize() * 3 / 2 + 1;
            return size;
        }

        return new Dimension(10, 10);
    }

    @Override
    public void doLayout() {
        Mapper mapper = getMapper();

        mapper.validateNodes();

        cellRendererPane.setBounds(0, 0, getWidth(), getHeight());

        clearCellRendererPane();
    }
    
    @Override
    public void print(Graphics g) {
        Mapper mapper = getMapper();
        mapper.setPrintMode(true);
        super.print(g);
        mapper.setPrintMode(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Mapper mapper = getMapper();

        mapper.resetRepaintSceduled();

        MapperNode root = getRoot();
        int step = mapper.getStepSize();

        if (root != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            int width = getWidth();
            paintNode(root, g2, 0, width);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(MapperStyle.LINK_COLOR_SELECTED_NODE);
            paintNodeEdges(root, g2, 0, width, step);
            g2.dispose();
        }

        clearCellRendererPane();
        getLinkTool().paintRightTree(this, g);
    }

    private void clearCellRendererPane() {
        for (int i = cellRendererPane.getComponentCount() - 1; i >= 0; i--) {
            Component c = cellRendererPane.getComponent(i);
            if (c != childrenLabel) {
                cellRendererPane.remove(i);
            }
        }
    }

    private void paintNode(MapperNode node, Graphics2D g2, int y0, int width) {
        Mapper mapper = getMapper();

        final int indent = node.getIndent();
        final int height = node.getHeight();
        final int contentHeight = node.getContentHeight();
        final int contentCenterY = y0 + node.getContentCenterY();
        final int labelWidth = node.getLabelWidth();
        final int labelHeight = node.getLabelHeight();
        final int labelX = width - indent - labelWidth;
        final int labelY = y0 + (contentHeight - 1 - labelHeight) / 2;

        final boolean leaf = node.isLeaf();
        final boolean expanded = node.isExpanded();

        if (node.isSelected() && !mapper.getPrintMode()) {
            VerticalGradient gradient = (hasFocus())
                    ? Mapper.SELECTED_BACKGROUND_IN_FOCUS
                    : Mapper.SELECTED_BACKGROUND_NOT_IN_FOCUS;
            gradient.paintGradient(this, g2, 0, y0, width,
                    ((leaf) ? height : ((expanded) ? contentHeight : height)) - 1);
        }

        if (leaf) {
            // leaf
            if (node.mustDrawLine()) {
                int lineY = y0 + height - 1;
                g2.setColor(Mapper.ROW_SEPARATOR_COLOR);
                g2.drawLine(0, lineY, width - 1, lineY);
            }
        } else if (expanded) {
            // expanded
            if (node.mustDrawLine()) {
                int lineY = y0 + contentHeight - 1;
                g2.setColor(Mapper.ROW_SEPARATOR_COLOR);
                g2.drawLine(0, lineY, width - 1, lineY);
            }

            int count = node.getChildCount();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    MapperNode child = node.getChild(i);
                    paintNode(child, g2, y0 + child.getY(), width);
                }

                int hLineX2 = width - indent - mapper.getRightIndent();
                int hLineX1 = hLineX2 - mapper.getLeftIndent();

                int vLineY1;

                if (node.getParent() != null) {
                    vLineY1 = y0 + (contentHeight - 1 - labelHeight) / 2 + labelHeight;
                } else {
                    MapperNode firstChild = node.getChild(0);
                    vLineY1 = y0 + firstChild.getContentCenterY() + firstChild.getY();
                }


                MapperNode lastChild = node.getChild(count - 1);

                int vLineY2 = y0 + lastChild.getContentCenterY() + lastChild.getY();

                g2.setColor(mapper.getTreeLineColor());
                g2.drawLine(hLineX2, vLineY1, hLineX2, vLineY2);

                for (int i = 0; i < count; i++) {
                    MapperNode child = node.getChild(i);
                    int hLineY = y0 + child.getY() + child.getContentCenterY();
                    g2.drawLine(hLineX1, hLineY, hLineX2, hLineY);

                    if (!child.isLeaf()) {
                        Icon icon = (child.isCollapsed())
                                ? mapper.getCollapsedIcon()
                                : mapper.getExpandedIcon();

                        icon.paintIcon(mapper.getRightTree(), g2,
                                hLineX2 - icon.getIconWidth() / 2,
                                hLineY - icon.getIconHeight() / 2);
                    }
                }
            }
        } else {
            // collapsed
            int lineY1 = y0 + contentHeight - 1;
            int lineY2 = y0 + height - 1;

            if (lineY1 != lineY2) {
                if (node.mustDrawDottedLine()) {
                    Stroke oldStroke = g2.getStroke();
                    g2.setColor(Mapper.ROW_SEPARATOR_COLOR);
                    g2.setStroke(Mapper.DASHED_ROW_SEPARATOR_STROKE);
                    g2.drawLine(0, lineY1, width - 1, lineY1);
                    g2.setStroke(oldStroke);
                }

                JLabel childrenLabel = getChildrenLabel();
                Dimension childrenLabelSize = childrenLabel.getPreferredSize();

                childrenLabel.setBounds(0, 0, childrenLabelSize.width,
                        childrenLabelSize.height);

                int childrenLabelX = width - indent - childrenLabelSize.width - mapper.getTotalIndent();
                int childrenLabelY = y0 + contentHeight + (height - contentHeight - 1 - childrenLabelSize.height) / 2;

                int vLineY1 = y0 + (contentHeight - 1 - labelHeight) / 2 + labelHeight;
                int vLineY2 = y0 + contentHeight + (height - contentHeight - 1) / 2;
                int hLineX2 = width - indent - mapper.getRightIndent();
                int hLineX1 = width - indent - mapper.getTotalIndent();

                g2.setColor(mapper.getTreeLineColor());
                g2.drawLine(hLineX2, vLineY1, hLineX2, vLineY2);
                g2.drawLine(hLineX1, vLineY2, hLineX2, vLineY2);

                g2.translate(childrenLabelX, childrenLabelY);
                childrenLabel.paint(g2);
                g2.translate(-childrenLabelX, -childrenLabelY);
            }
            if (node.mustDrawLine()) {
                g2.setColor(Mapper.ROW_SEPARATOR_COLOR);
                g2.drawLine(0, lineY2, width - 1, lineY2);
            }
        }

        Component c = getCellRendererComponent(node);

        c.setBounds(0, 0, labelWidth, labelHeight);
        g2.translate(labelX, labelY);
        c.paint(g2);
        g2.translate(-labelX, -labelY);
    }

    private void paintNodeEdges(MapperNode node, Graphics2D g2, int y0,
            int width, int step) {
        Mapper mapper = getMapper();
        SelectionModel selectionModel = getSelectionModel();
        TreePath treePath = node.getTreePath();
        
        final int contentHeight = node.getContentHeight();
        final int height = node.getHeight();
        final boolean leaf = node.isLeaf();
        final boolean expanded = node.isExpanded();
        Color color = null;
        
        Graph graph = node.getGraph();

        boolean hasEdge = false;
        boolean edgeIsSelected = false;
        
        Link link = null;
        if (graph != null) {
            link = graph.getOutgoingLink();
        }
        
        if (link != null) {
            hasEdge = getCanvas().getRendererContext().paintLink(treePath, link);
            edgeIsSelected = selectionModel.isSelected(treePath, link);
        }
        
        boolean hasChildEdges = false;
        
        if (leaf) {

        } else if (expanded) {
            int childCount = node.getChildCount();

            for (int i = 0; i < childCount; i++) {
                MapperNode child = node.getChild(i);
                paintNodeEdges(child, g2, y0 + child.getY(), width, step);
            }
        } else {
            Set<Graph> graphs = node.getChildGraphs();
            if (graphs != null) {
                for (Graph g : graphs) {
                    if (g.hasOutgoingLinks()) {
                        hasChildEdges = true;
                        break;
                    }
                }
            }
        }
        Stroke oldStroke = g2.getStroke();

        if (!mapper.getPrintMode()) {
            if (edgeIsSelected && node.isGraphExpanded()) {
                color = MapperStyle.SELECTION_COLOR;
                g2.setStroke(MapperStyle.SELECTION_STROKE);
            } else if (selectionModel.isSelected(treePath)) {
                color = MapperStyle.LINK_COLOR_SELECTED_NODE;
            } else {
                color = MapperStyle.LINK_COLOR_UNSELECTED_NODE;
            }
        } else {
            color = MapperStyle.LINK_COLOR_UNSELECTED_NODE;
        }
        
        if (contentHeight < height) {
            if (hasEdge) {
                int y = y0 + (contentHeight - 1) / 2;
                int x = width - node.getIndent() - node.getLabelWidth();
                paintEdge(g2, y, x, color, step, false);
            }

            if (hasChildEdges) {
                if (selectionModel.isSelected(treePath) && !mapper.getPrintMode()) {
                    color = MapperStyle.LINK_COLOR_SELECTED_NODE;
                } else {
                    color = MapperStyle.LINK_COLOR_UNSELECTED_NODE;
                }
                int y = y0 + contentHeight + (height - contentHeight - 1) / 2;
                int x = width - node.getIndent() - childrenLabel.getPreferredSize().width - mapper.getTotalIndent();
                paintEdge(g2, y, x, color, step, true);
            }
        } else {
            if (hasEdge || hasChildEdges) {
                int y = y0 + (contentHeight - 1) / 2;
                int x = width - node.getIndent() - node.getLabelWidth() - 1;
                paintEdge(g2, y, x, color, step, hasChildEdges && !hasEdge);
            }
        }
        g2.setStroke(oldStroke);
    }

    private void paintEdge(Graphics2D g2, int y, int x, Color color,int step, boolean dashed) {
        int cx = x - step / 2;
        g2.setColor(color);
        if (dashed) {
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(Mapper.DASHED_STROKE);
            g2.drawLine(cx, y, 0, y);
            g2.setStroke(oldStroke);
        } else {
            g2.drawLine(0, y, cx, y);
        }

        Link.paintTargetDecoration(g2, new Point(x, y), color, step);
    }

    public void focusGained(FocusEvent e) {
        repaint();
    }

    public void focusLost(FocusEvent e) {
        repaint();
    }

    private class RowHeader extends JPanel implements MouseListener {

        public RowHeader() {
            setOpaque(true);
            addMouseListener(this);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = getMapper().getPreferredTreeSize();
            if (size != null) {
                size.width = 16;
                return size;
            }

            return new Dimension(16, 10);
        }

        @Override
        public void doLayout() {
            getMapper().validateNodes();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            getMapper().resetRepaintSceduled();

            int w = getWidth();
            int h = getHeight();

            MapperNode root = getRoot();
            if (root != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                paintNode(root, g2, 0, w - 1);
                g2.dispose();
            }

            g.setColor(Color.GRAY);
            g.drawLine(w - 1, 0, w - 1, h - 1);
        }

        private void paintNode(MapperNode node, Graphics2D g2,
                int y0, int width) {
            final int contentHeight = node.getContentHeight();
            final int height = node.getHeight();

            final boolean leaf = node.isLeaf();
            final boolean expanded = node.isExpanded();

            g2.setColor(Mapper.RIGHT_TREE_HEADER_COLOR);
            if (leaf) {
                // leaf
                if (node.mustDrawLine()) {
                    int lineY = y0 + height - 1;
                    g2.drawLine(0, lineY, width - 1, lineY);
                }
            } else if (expanded) {
                // expanded
                if (node.mustDrawLine()) {
                    int lineY = y0 + contentHeight - 1;
                    g2.drawLine(0, lineY, width - 1, lineY);
                }

                int count = node.getChildCount();
                for (int i = 0; i < count; i++) {
                    MapperNode child = node.getChild(i);
                    paintNode(child, g2, y0 + child.getY(), width);
                }
            } else {
                // collapsed
                if (node.mustDrawLine()) {
                    int lineY1 = y0 + contentHeight - 1;
                    int lineY2 = y0 + height - 1;

                    if (node.mustDrawDottedLine()) {
                        Stroke oldStroke = g2.getStroke();
                        g2.setStroke(Mapper.DASHED_ROW_SEPARATOR_STROKE);
                        g2.drawLine(0, lineY1, width, lineY1);
                        g2.setStroke(oldStroke);
                    }
                    g2.drawLine(0, lineY2, width - 1, lineY2);
                }
            }

            Graph graph = node.getGraph();

            if (graph != null && !graph.isEmptyOrOneLink()) {
                int cy = y0 + node.getContentCenterY();
                int cx = width / 2;
                g2.setColor(Color.WHITE);
                g2.fillRect(cx - 3, cy - 3, 7, 7);

                g2.setColor(Mapper.RIGHT_TREE_HEADER_COLOR);
                g2.drawRect(cx - 4, cy - 4, 8, 8);
                g2.drawLine(cx - 2, cy, cx + 2, cy);

                if (node.isGraphCollapsed()) {
                    g2.drawLine(cx, cy - 2, cx, cy + 2);
                }
            }
        }

        public void mousePressed(MouseEvent e) {
            if (!RightTree.this.hasFocus()) {
                RightTree.this.requestFocusInWindow();
            }

            if (e.isPopupTrigger()) {
                return;
            }
            int y = e.getY();
            int x = e.getX();

            MapperNode node = getNodeAt(y);
            if (node != null) {
                y = node.yToNode(y);

                int cy = node.getContentCenterY();

                boolean select = true;

                if (Math.abs(cy - y) <= 8) {
                    Graph graph = node.getGraph();
                    if (graph != null && !graph.isEmptyOrOneLink()) {
                        getMapper().setExpandedGraphState(node.getTreePath(),
                                !node.isGraphExpanded());
                        getLinkTool().done();
                        select = false;
                    }
                }

                if (select) {
                    getMapper().setSelectedNode(node);
                }
            }
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }

    private class BottomLeftCorner extends JPanel {

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1, 1);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            int w = getWidth();
            int h = getHeight();

            g.setColor(Color.GRAY);
            g.drawLine(0, 0, w - 1, 0);
            g.drawLine(w - 1, 0, w - 1, h - 1);
        }
    }

    private class ChildrenLabel extends JLabel {

        public ChildrenLabel() {
            super("  ...  ");
            setOpaque(true);
            setBackground(Color.WHITE);
            setBorder(new ChildrenLabelBorder());
        }
    }

    private class ChildrenLabelBorder implements Border {

        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height) {
            Color oldColor = g.getColor();
            g.setColor(getMapper().getTreeLineColor());
            g.drawRect(x, y, width - 1, height - 1);
            g.setColor(oldColor);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }

        public boolean isBorderOpaque() {
            return true;
        }
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
                    scrollPane.getVerticalScrollBar().getUnitIncrement();
        }
        if (cursorLocn.y < insets.top) {
            r.y = insets.top - 16 -
                    scrollPane.getVerticalScrollBar().getUnitIncrement();
        }
        if (cursorLocn.x > getWidth() - insets.right) {
            r.x = getWidth() - insets.right + 16 +
                    scrollPane.getHorizontalScrollBar().getUnitIncrement();
        }
        if (cursorLocn.x < insets.left) {
            r.x = insets.left - 16 -
                    scrollPane.getHorizontalScrollBar().getUnitIncrement();
        }
        scrollRectToVisible(r);
    }

    private class MoveSelectionUp extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            Mapper mapper = RightTree.this.getMapper();
            SelectionModel selectionModel = getSelectionModel();
            TreePath currentTreePath = selectionModel.getSelectedPath();
            if (currentTreePath != null && currentTreePath != mapper.getRoot().getTreePath()) {
                MapperNode currentNode = mapper.getNode(currentTreePath, true);
                MapperNode prevNode = currentNode.getPrevVisibleNode();
                if (prevNode != null && prevNode != mapper.getRoot()) {
                    selectionModel.setSelected(prevNode.getTreePath());
                }
            } else if (mapper.getRoot() != null && mapper.getRoot().getChildCount() > 0) {
                mapper.setSelectedNode(mapper.getRoot().getChild(0));
            }
        }
    }

    private class MoveSelectionDown extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            Mapper mapper = RightTree.this.getMapper();
            SelectionModel selectionModel = getSelectionModel();
            TreePath currentTreePath = selectionModel.getSelectedPath();
            if (currentTreePath != null) {
                MapperNode currentNode = mapper.getNode(currentTreePath, true);
                MapperNode nextNode = currentNode.getNextVisibleNode();
                if (nextNode != null) {
                    selectionModel.setSelected(nextNode.getTreePath());
                }
            } else if (mapper.getRoot() != null && mapper.getRoot().getChildCount() > 0) {
                mapper.setSelectedNode(mapper.getRoot().getChild(0));
            }
        }
    }

    private class PressLeftExpand extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            Mapper mapper = RightTree.this.getMapper();
            SelectionModel selectionModel = getSelectionModel();
            TreePath currentTreePath = selectionModel.getSelectedPath();
            if (currentTreePath != null) {
                MapperNode currentNode = mapper.getNode(currentTreePath, true);
                if (currentNode.isLeaf()) {
                    if (currentNode.getNextVisibleNode() != null) {
                        mapper.setSelectedNode(currentNode.getNextVisibleNode());
                    }
                } else if (currentNode.isCollapsed()) {
                    mapper.expandNode(currentNode);
                } else if (currentNode.getChildCount() > 0) {
                    mapper.setSelectedNode(currentNode.getChild(0));
                }
            } else if (mapper.getRoot() != null && mapper.getRoot().getChildCount() > 0) {
                mapper.setSelectedNode(mapper.getRoot().getChild(0));
            }
        }
    }

    private class PressRightCollapse extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            Mapper mapper = RightTree.this.getMapper();
            SelectionModel selectionModel = getSelectionModel();
            TreePath treePath = selectionModel.getSelectedPath();
            MapperNode node = mapper.getNode(treePath, true);
            if (treePath != null && treePath != mapper.getRoot().getTreePath()) {
                if (node.isExpanded() && !node.isLeaf()) {
                    mapper.collapseNode(node);
                } else if (node.getParent() != mapper.getRoot()) {
                    mapper.setSelectedNode(node.getParent());
                }
            } else if (mapper.getRoot() != null && mapper.getRoot().getChildCount() > 0) {
                mapper.setSelectedNode(mapper.getRoot().getChild(0));
            }
        }
    }

    private class PressLeftExpandGraph extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            TreePath treePath = getSelectionModel().getSelectedPath();
            if (treePath == null) {
                return;
            }

            Mapper mapper = RightTree.this.getMapper();
            MapperNode node = mapper.getNode(treePath, true);
            if (node.getGraph() == null) {
                return;
            }

            if (node.isGraphCollapsed()) {
                mapper.setExpandedGraphState(node.getTreePath(), true);
            } else {
                getCanvas().requestFocusInWindow();
                Graph graph = node.getGraph();
                if (graph.hasOutgoingLinks()) {
                    List<Link> links = graph.getLinks();
                    for (Link l : links) {
                        if (l.getTarget() instanceof Graph) {
                            mapper.getSelectionModel().setSelected(treePath, l);
                            break;
                        }
                    }
                    return;
                }
                List<Vertex> verteces = node.getGraph().getVerteces();
                if (verteces == null || verteces.size() <= 0) {
                    return;
                }

                Vertex vertex = node.getGraph().getPrevVertex(null);
                mapper.getSelectionModel().setSelected(treePath, vertex);
            }
        }
    }

    private class PressRightCollapseGraph extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            Mapper mapper = RightTree.this.getMapper();
            TreePath treePath = getSelectionModel().getSelectedPath();
            if (treePath != null) {
                MapperNode node = mapper.getNode(treePath, true);
                Graph graph = node.getGraph();
                if (graph != null && !graph.isEmptyOrOneLink() 
                        && node.isGraphExpanded()) 
                {
                    mapper.setExpandedGraphState(treePath, false);
                    getLinkTool().done();
                }
            }
        }
    }    

    private class AutoScrollDown extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            if (scrollPane.getViewport() == null) {
                return;
            }

            Insets insets = getAutoscrollInsets();
            int x = getScrollPane().getViewport().getViewRect().x;
            Rectangle r = new Rectangle(x, 0, 1, 1);
            r.y = getHeight() - insets.bottom + 16 +
                    scrollPane.getVerticalScrollBar().getUnitIncrement();
            RightTree.this.scrollRectToVisible(r);
        }
    }

    private class AutoScrollUp extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            if (scrollPane.getViewport() == null) {
                return;
            }

            Insets insets = getAutoscrollInsets();
            int x = getScrollPane().getViewport().getViewRect().x;
            Rectangle r = new Rectangle(x, 0, 1, 1);
            r.y = insets.top - 16 -
                    scrollPane.getVerticalScrollBar().getUnitIncrement();
            RightTree.this.scrollRectToVisible(r);
        }
    }

    private class AutoScrollLeft extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            if (scrollPane.getViewport() == null) {
                return;
            }

            Insets insets = getAutoscrollInsets();
            int y = getScrollPane().getViewport().getViewRect().y;
            Rectangle r = new Rectangle(0, y, 1, 1);
            r.x = insets.left - 16 -
                    scrollPane.getHorizontalScrollBar().getUnitIncrement();
            RightTree.this.scrollRectToVisible(r);
        }
    }

    private class AutoScrollRight extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            if (scrollPane.getViewport() == null) {
                return;
            }

            Insets insets = getAutoscrollInsets();
            int y = getScrollPane().getViewport().getViewRect().y;
            Rectangle r = new Rectangle(0, y, 1, 1);
            r.x = getWidth() - insets.right + 16 +
                    scrollPane.getHorizontalScrollBar().getUnitIncrement();
            RightTree.this.scrollRectToVisible(r);
        }
    }

    private static class RightScrollPaneLayout extends ScrollPaneLayout {
        @Override
        public void layoutContainer(Container target) {
            JScrollPane scrollPane = (JScrollPane) target;
            JViewport viewport = scrollPane.getViewport();
            
            Dimension viewSize = viewport.getViewSize();
            Dimension extentSize = viewport.getExtentSize();
            Point viewPosition = viewport.getViewPosition();
            
            int offset = Math.max(0, 
                    viewSize.width - (viewPosition.x + extentSize.width));

            super.layoutContainer(target);
            
            viewSize = viewport.getViewSize();
            extentSize = viewport.getExtentSize();
            viewPosition = viewport.getViewPosition();
            
            viewPosition.x = Math.max(0, 
                    viewSize.width - extentSize.width - offset);
            
            viewport.setViewPosition(viewPosition);
        }
    }
    
    private static class RightViewportLayout extends ViewportLayout {
        @Override
        public void layoutContainer(Container target) {
            JViewport viewport = (JViewport) target;
            
            Dimension viewSize = viewport.getViewSize();
            Dimension extentSize = viewport.getExtentSize();
            Point viewPosition = viewport.getViewPosition();
            
            int offset = Math.max(0, 
                    viewSize.width - (viewPosition.x + extentSize.width));

            super.layoutContainer(target);
            
            viewSize = viewport.getViewSize();
            extentSize = viewport.getExtentSize();
            viewPosition = viewport.getViewPosition();
            
            viewPosition.x = Math.max(0, 
                    viewSize.width - extentSize.width - offset);
            
            viewport.setViewPosition(viewPosition);
        }
    }
    
    private class ShowPopupMenuAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            RightTree tree = RightTree.this;
            MapperContext context = tree.getContext();
            MapperModel model = tree.getMapper().getModel();
            if (context == null || model == null) { return; }

            TreePath treePath = tree.getSelectionModel().getSelectedPath();
            if (treePath == null) { return; }

            MapperNode node = tree.getMapper().getNode(treePath, true);
            if (node == null) { return; }
            
            Object value = treePath.getLastPathComponent();
            if (value == null) { return; }
            
            JPopupMenu menu = context.getRightPopupMenu(model, value);
            if (menu != null) {
                menu.show(tree, 0, node.yToView(node.getContentCenterY()
                        - node.getContentHeight() / 2));
            }
        }
    }
}
