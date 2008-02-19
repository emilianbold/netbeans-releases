/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.netbeans.modules.bpel.design.BWGraphics2D;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.DiagramViewLayout;
import org.netbeans.modules.bpel.design.DiagramFontUtil;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.MouseHandler;
import org.netbeans.modules.bpel.design.NameEditor;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.components.DiagramButton;
import org.netbeans.modules.bpel.design.decoration.components.GlassPane;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.ConnectionManager;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.PartnerlinkPattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.design.selection.PlaceHolderManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey
 */
public abstract class DiagramView extends JPanel implements Autoscroll {
    
    private DesignView designView;
    private PlaceHolderManager placeholderManager;
    private NameEditor nameEditor;
    private MouseHandler mouseHandler;

    public DiagramView(DesignView designView) {
        super(new DiagramViewLayout());
        this.designView = designView;


        placeholderManager =
                new PlaceHolderManager(this);

        nameEditor = new NameEditor(this);
        mouseHandler = new MouseHandler(this);

    }

    public abstract FBounds getContentSize();

    public abstract Iterator<Pattern> getPatterns();

    public abstract void getPlaceholders(Pattern draggedPattern, List<PlaceHolder> placeHolders);

    public DesignView getDesignView() {
        return designView;
    }

    public PlaceHolderManager getPlaceholderManager() {
        return this.placeholderManager;

    }

    public NameEditor getNameEditor() {
        return nameEditor;
    }

    protected void paintComponent(Graphics g) {
       // long start = System.currentTimeMillis();
        super.paintComponent(g);
        paintContent(g, getDesignView().getCorrectedZoom(), false);
       // System.out.println("Paint (" + (System.currentTimeMillis() - start) + " ms):" + this );
    }

   
    private void paintContent(Graphics g, double zoom, boolean printMode) {

        Pattern root = getDesignView().getModel().getRootPattern();

        Graphics2D g2 = GUtils.createGraphics(g);
        Graphics2D g2bw = new BWGraphics2D(g2);

        if (!printMode) {
            Point p = convertDiagramToScreen(new FPoint(0, 0));
            g2.translate(p.x, p.y);
        }

        g2.scale(zoom, zoom);

        if (root != null) {
            Rectangle clipBounds = g2.getClipBounds();

            double exWidth = LayoutManager.HSPACING * zoom;
            double exHeight = LayoutManager.VSPACING * zoom;

            FBounds exClipBounds = new FBounds(clipBounds.x - exWidth,
                    clipBounds.y - exHeight,
                    clipBounds.width + 2 * exWidth,
                    clipBounds.height + 2 * exHeight);

            g2.setFont(DiagramFontUtil.getFont());

            paintPattern(g2, g2bw, root, exClipBounds, printMode);
            paintPatternConnections(g2, g2bw, root, printMode);

            if (!printMode) {
                placeholderManager.paint(g2);
            //  FIXME              flowLinkTool.paint(g2);
//                ghost.paint(g2);
            }
        }
    }

    public FPoint convertScreenToDiagram(Point point) {
        return convertScreenToDiagram(point, designView.getCorrectedZoom());
    }

    public Point convertDiagramToScreen(FPoint point) {
        return convertDiagramToScreen(point, designView.getCorrectedZoom());
    }

    //    public void scrollToFocusVisible(Pattern pattern) {
//        scrollRectToVisible(getFocusAreaBounds(pattern));
//    }
    public void scrollPatternToView(Pattern pattern) {
        if (pattern == null) {
            return;
        }
        /**
         * Get the position of selected node and scroll view to make
         * the corresponding pattern visible
         **/
        FBounds bounds = pattern.getBounds();

        Point screenTL = convertDiagramToScreen(bounds.getTopLeft());
        Point screenBR = convertDiagramToScreen(bounds.getBottomRight());

        int x1 = Math.max(0, screenTL.x - 8);
        int y1 = Math.max(0, screenTL.y - 32);

        int x2 = Math.min(getWidth(), screenBR.x + 8);
        int y2 = Math.min(getHeight(), screenBR.y + 8);

        int w = Math.max(1, x2 - x1);
        int h = Math.max(1, y2 - y1);

        scrollRectToVisible(new Rectangle(x1, y1, w, h));

    }

    protected void paintPattern(Graphics2D g2, Graphics2D g2bw, Pattern pattern,
            FBounds clipBounds, boolean printMode) {

        if (!pattern.getBounds().isIntersects(clipBounds)) {
            return;
        }

        Decoration decoration = getDesignView().getDecoration(pattern);


        if (pattern instanceof CompositePattern) {
            CompositePattern composite = (CompositePattern) pattern;

            BorderElement border = composite.getBorder();

            Graphics2D g = (decoration.hasDimmed() && !printMode) ? g2bw : g2;


            if (decoration.hasGlow() && !printMode) {
                decoration.getGlow().paint(g2, composite.createOutline());
            }

            if (border != null) {
                border.paint(g);
            }

            for (VisualElement e : composite.getElements()) {
                e.paint(g);
            }

            for (Pattern p : composite.getNestedPatterns()) {
                paintPattern(g2, g2bw, p, clipBounds, printMode);
            }

            if (decoration.hasStroke() && !printMode) {
                decoration.getStroke().paint(g2, composite.createSelection());
            }
        } else {
            Graphics2D g = (decoration.hasDimmed() && !printMode) ? g2bw : g2;

            if (decoration.hasGlow() && !printMode) {
                decoration.getGlow().paint(g2, pattern.createOutline());
            }

            for (VisualElement e : pattern.getElements()) {
                e.paint(g);
            }

            if (decoration.hasStroke() && !printMode) {
                decoration.getStroke().paint(g2, pattern.createSelection());
            }
        }
    }

    protected void paintPatternConnections(Graphics2D g2, Graphics2D g2bw,
            Pattern pattern, boolean printMode) {
        if (pattern == null) {
            return;
        }

        if (pattern instanceof CompositePattern) {
            CompositePattern composite = (CompositePattern) pattern;
            for (Pattern p : composite.getNestedPatterns()) {
                paintPatternConnections(g2, g2bw, p, printMode);
            }
        }

        Graphics2D g = (getDesignView().getDecoration(pattern).hasDimmed() && !printMode) ? g2bw : g2;

        for (Connection c : pattern.getConnections()) {
            c.paint(g);
        }
    }

    public Point convertPointToParent(FPoint point) {
        Point result = convertDiagramToScreen(point);
        Component c = this;
        while (c != getDesignView()) {
            result.x += c.getX();
            result.y += c.getY();
            c = c.getParent();
        }
        return result;
    }

    public FPoint convertPointFromParent(Point point) {
        Component c = this;
        Point result = new Point(point);
        while (c != getDesignView()) {
            result.x -= c.getX();
            result.y -= c.getY();
            c = c.getParent();
        }

        return this.convertScreenToDiagram(result);

    }

    public Insets getAutoscrollInsets() {
        Rectangle outer = getBounds();
        Rectangle inner = getParent().getBounds();
        return new Insets(inner.y - outer.y + AUTOSCROLL_INSETS,
                inner.x - outer.x + AUTOSCROLL_INSETS,
                outer.height - inner.height - inner.y + outer.y + AUTOSCROLL_INSETS,
                outer.width - inner.width - inner.x + outer.x + AUTOSCROLL_INSETS);
    }

    public void autoscroll(Point location) {
        JScrollPane scroller = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);

        if (scroller != null) {
            repaint();

            final int SPEED_FACTOR = 5;

            Insets scrollInsets = new Insets(12, 12, 12, 12);
            Rectangle r = getVisibleRect();

            int h_distance = 0;
            if (location.x <= r.x + AUTOSCROLL_INSETS) {
                h_distance = location.x - (r.x + AUTOSCROLL_INSETS);
            } else if (location.x >= r.x + r.width - AUTOSCROLL_INSETS) {
                h_distance = location.x - (r.x + r.width - AUTOSCROLL_INSETS);

            }
            if (h_distance != 0) {
                JScrollBar bar = scroller.getHorizontalScrollBar();
                bar.setValue(bar.getValue() + h_distance * SPEED_FACTOR);
            }


            int v_dist = 0;
            if (location.y <= r.y + AUTOSCROLL_INSETS) {
                v_dist = location.y - (r.y + AUTOSCROLL_INSETS);
            } else if (location.y >= r.y + r.height - scrollInsets.bottom) {
                v_dist = location.y - (r.y + r.height - scrollInsets.bottom);
            }
            if (v_dist != 0) {
                JScrollBar bar = scroller.getVerticalScrollBar();
                bar.setValue(bar.getValue() + v_dist * SPEED_FACTOR);
            }
        }
    }

    public String getToolTipText(MouseEvent event) {
        Point point = getMousePosition();

        if (point == null) {
            point = event.getPoint();
        }
        String result = null;
        VisualElement element = findElement(point);
        Pattern pattern = (element == null) ? null : element.getPattern();

        if (pattern != null) {
            if ((pattern instanceof PartnerlinkPattern) && !(element instanceof BorderElement)) {
                result = element.getText();
                result = ((result != null) ? result.trim() : "") + " " + NbBundle.getMessage(DesignView.class, "LBL_Operation"); // NOI18N
            } else {
                Node node = getDesignView().getNodeForPattern(pattern);

                if (node != null) {
                    result = node.getDisplayName();
                }

                if (result == null) {
                    result = pattern.getText();
                }
            }
        }

        if (result != null && result.trim().length() == 0) {
            result = null;
        }
        if (result == null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    ToolTipManager.sharedInstance().setEnabled(false);
                    ToolTipManager.sharedInstance().setEnabled(true);
                }
            });
        }
        return result;
    }
    //    public Point getToolTipLocation(MouseEvent event) {
//        Point p = getMousePosition();
//        
//        if (p != null) {
//            p.y += 20;
//        }
//        return p;
//    }
    public FPoint convertScreenToDiagram(Point point, double zoom) {
        double x = ((point.x - DiagramViewLayout.MARGIN_LEFT) / zoom) + LayoutManager.HMARGIN;

        double y = ((point.y - DiagramViewLayout.MARGIN_TOP) / zoom) + LayoutManager.VMARGIN;

        return new FPoint(x, y);
    }

    public Point convertDiagramToScreen(FPoint point, double zoom) {
        double x = (point.x - LayoutManager.HMARGIN) * zoom + DiagramViewLayout.MARGIN_LEFT;
        double y = (point.y - LayoutManager.VMARGIN) * zoom + DiagramViewLayout.MARGIN_TOP;

        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    public Pattern findPattern(Point point) {
        FPoint fPoint = convertScreenToDiagram(point);
        return findPattern(fPoint.x, fPoint.y);
    }

    public Pattern findPattern(double x, double y) {
        VisualElement element = findElement(x, y);
        return (element != null) ? element.getPattern() : null;
    }

    public VisualElement findElement(Point point) {
        FPoint fPoint = convertScreenToDiagram(point);
        return findElement(fPoint.x, fPoint.y);
    }

    public VisualElement findElement(double x, double y) {
        Iterator<Pattern> patterns = getPatterns();
        while (patterns.hasNext()) {
            Pattern pattern = patterns.next();
            VisualElement e = findElementInPattern(pattern, x, y);
            if (e != null){
                return e;
            }

        }
        return null;
    }

    protected VisualElement findElementInPattern(Pattern pattern, double x, double y) {
        for (VisualElement e : pattern.getElements()) {
            if (e.contains(x, y)) {
                return e;
            }
        }

        if (pattern instanceof CompositePattern) {
            BorderElement border = ((CompositePattern) pattern).getBorder();
            if (border != null && border.getShape().contains(x, y)) {
                return border;
            }
        }
        return null;

    }
    private static int AUTOSCROLL_INSETS = 20;
}
