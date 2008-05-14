//package org.netbeans.modules.bpel.editors.multiview;
//
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.Container;
//import java.awt.Cursor;
//import java.awt.Dimension;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Insets;
//import java.awt.LayoutManager;
//import java.awt.Point;
//import java.awt.Rectangle;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.AdjustmentEvent;
//import java.awt.event.AdjustmentListener;
//import java.awt.event.KeyEvent;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.event.MouseMotionListener;
//import java.awt.geom.Point2D;
//import javax.swing.AbstractAction;
//import javax.swing.KeyStroke;
//import javax.swing.SwingUtilities;
//import javax.swing.Timer;
//import javax.swing.JComponent;
//import javax.swing.JPanel;
//import javax.swing.JScrollBar;
//import javax.swing.JScrollPane;
//import javax.swing.JToggleButton;
//import javax.swing.JViewport;
//import javax.swing.ScrollPaneLayout;
//import javax.swing.ViewportLayout;
//import javax.swing.plaf.metal.MetalToggleButtonUI;
//import org.openide.util.NbBundle;
//
///**
// * @author krjukov
// * @author anjeleevich
// * @author zgursky
// */
//public class ThumbScrollPane extends JScrollPane implements 
//        ActionListener, AdjustmentListener 
//{
//    private ThumbnailView thumbnailView;
//    private JScrollPane thumbnailViewScrollPane;
//    private ThumbToggleButton showThumbnailViewButton;
//    
//
//    public ThumbScrollPane(JComponent view) {
//        super(view);
//        setLayout(new ThumbScrollPaneLayout());
//        getViewport().setLayout(new ThumbViewportLayout());
//        
//        showThumbnailViewButton = new ThumbToggleButton();
//        showThumbnailViewButton.setFocusable(false);
//        showThumbnailViewButton.addActionListener(this);
//        thumbnailView = new ThumbnailView();
//        
//        thumbnailViewScrollPane = new JScrollPane(thumbnailView, 
//                JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
//                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        thumbnailViewScrollPane.setFocusable(false);
//        thumbnailViewScrollPane.getViewport().setLayout(new ThumbnailViewportLayout());
//        thumbnailViewScrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
//        thumbnailViewScrollPane.setWheelScrollingEnabled(false);
//        
//        setCorner(LOWER_RIGHT_CORNER, showThumbnailViewButton);
//        add(thumbnailViewScrollPane, 0);
//        
//        getVerticalScrollBar().addAdjustmentListener(this);
//        getHorizontalScrollBar().addAdjustmentListener(this);
//        
//        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
//                TOGGLE_THUMBNAIL_KEYSTROKE, TOGGLE_THUMBNAIL);
//        getActionMap().put(TOGGLE_THUMBNAIL, new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                if (!showThumbnailViewButton.isVisible()) return;
//                showThumbnailViewButton.setSelected(!showThumbnailViewButton
//                        .isSelected());
//                ThumbScrollPane.this.actionPerformed(e);
//            }
//        });
//    }
//
//    
//    public boolean isOptimizedDrawingEnabled() {
//        return !thumbnailView.isVisible();
//    }
//    
//
//    private JScrollPane getThumbnailViewScrollPane() {
//        return thumbnailViewScrollPane;
//    }
//    
//    
//    private ThumbnailView getThumbnailView() {
//        return thumbnailView;
//    }
//    
//    
//    private boolean isThumbnailViewShown() {
//        return showThumbnailViewButton.isSelected();
//    }
//
//    
//    public void actionPerformed(ActionEvent e) {
//        revalidate();
//        repaint();
//    }
//    
//    
//    public void adjustmentValueChanged(AdjustmentEvent e) {
//        if (thumbnailView.isIgnoreViewScroll()) return;
//        
//        if (isThumbnailViewShown()) {
//            thumbnailView.revalidate();
//            thumbnailView.repaint();
//        }
//    }
//    
//    
//    private static class ThumbnailView extends JPanel implements 
//            LayoutManager, MouseListener, MouseMotionListener, ActionListener 
//    {
//        private Dimension cachedPreferredSize;
//        private Rectangle cachedViewRectangle;
//        private Rectangle cachedViewVisibleRectangle;
//        private Point2D cachedViewVisibleRectangleCenter;
//        private double cachedKX;
//        private double cachedKY;
//        
//        private double mouseDX;
//        private double mouseDY;
//        
//        private boolean ignoreViewScroll = false;
//        
//        private MouseEvent lastMouseEvent = null;
//        private Point lastMouseOnScreenPosition = null;
//        
//        private Timer timer;
//        
//        
//        public ThumbnailView() {
//            setLayout(this);
//            addMouseListener(this);
//            addMouseMotionListener(this);
//            timer = new Timer(100, this);
//            setFocusable(false);
//        }
//        
//        
//        public boolean isIgnoreViewScroll() {
//            return ignoreViewScroll;
//        }
//        
//        
//        public Dimension calculatePreferredSize() {
//            JViewport viewport = (JViewport) getParent();
//            Dimension viewportSize = viewport.getSize();
//            Insets viewportInsets = viewport.getInsets();
//            
//            int w = viewportSize.width - viewportInsets.left 
//                    - viewportInsets.right;
//            int h = viewportSize.height - viewportInsets.top 
//                    - viewportInsets.bottom;
//
//            Dimension viewSize = getView().getSize();
//            Rectangle visibleRect = getView().getVisibleRect();
//            
//            double viewWidthK = (double) w / viewSize.width;
//            double viewHeightK = (double) h / viewSize.height;
//            double viewK = Math.min(viewWidthK, viewHeightK);
//            
//            double viewportK = Math.min(
//                    w * VIEWPORT_K_X / visibleRect.width, 
//                    h * VIEWPORT_K_Y / visibleRect.height);
//
//            double k = (viewK >= viewportK) ? viewK : viewportK;
//            
//            cachedPreferredSize = new Dimension(
//                    (int) Math.round(viewSize.width * k), 
//                    (int) Math.round(viewSize.height * k));
//            
//            return new Dimension(cachedPreferredSize);
//        }
//        
//        
//        private Rectangle calculateViewRectangle() {
//            int w = cachedPreferredSize.width;
//            int h = cachedPreferredSize.height;
//            
//            int xOffset = (getWidth() - w) / 2;
//            int yOffset = (getHeight() - h) / 2;
//            
//             cachedViewRectangle = new Rectangle(xOffset, yOffset, w, h);
//             
//             return new Rectangle(cachedViewRectangle);
//        }
//        
//        
//        private Rectangle convertViewVisibleRectangle(Rectangle viewVisibleRect) 
//        {
//            JComponent view = getView();
//            Dimension viewSize = view.getSize();
//            
//            double kx = (double) cachedPreferredSize.width / viewSize.width;
//            double ky = (double) cachedPreferredSize.height / viewSize.height;
//            
//            int x1 = (int) Math.round(viewVisibleRect.getMinX() * kx);
//            int y1 = (int) Math.round(viewVisibleRect.getMinY() * ky);
//            int x2 = (int) Math.round(viewVisibleRect.getMaxX() * kx);
//            int y2 = (int) Math.round(viewVisibleRect.getMaxY() * ky);
//            
//            return new Rectangle(x1, y1, x2 - x1, y2 - y1);
//        }
//        
//        
//        private Rectangle calculateViewVisibleRectangle() {
//            JComponent view = getView();
//            Dimension viewSize = view.getSize();
//            
//            double kx = (double) cachedPreferredSize.width / viewSize.width;
//            double ky = (double) cachedPreferredSize.height / viewSize.height;
//            
//            Rectangle viewVisibleRect = view.getVisibleRect();
//            
//            int x1 = (int) Math.round(viewVisibleRect.getMinX() * kx);
//            int y1 = (int) Math.round(viewVisibleRect.getMinY() * ky);
//            int x2 = (int) Math.round(viewVisibleRect.getMaxX() * kx);
//            int y2 = (int) Math.round(viewVisibleRect.getMaxY() * ky);
//            
//            cachedKX = kx;
//            cachedKY = ky;
//            
//            cachedViewVisibleRectangleCenter = new Point2D.Double(
//                    viewVisibleRect.getCenterX() * kx, 
//                    viewVisibleRect.getCenterY() * ky);
//            
//            cachedViewVisibleRectangle = new Rectangle(x1, y1, 
//                    x2 - x1, y2 - y1);
//            
//            return new Rectangle(cachedViewVisibleRectangle);
//        }
//        
//        
//        public void paintComponent(Graphics g) {
//            super.paintComponent(g);
//            
//            Rectangle viewRect = new Rectangle(cachedViewRectangle);
//            Rectangle viewVisibleRect = new Rectangle(cachedViewVisibleRectangle);
//            
//            g.translate(viewRect.x, viewRect.y);
//            
//            JComponent view = getView();
//            Color background = view.getBackground();
//            
//            g.setColor((background == null) ? Color.WHITE : background);
//            g.fillRect(0, 0, viewRect.width, viewRect.height);
//
//            if (view instanceof Thumbnailable) {
//                Graphics2D g2 = (Graphics2D) g.create();
//                g2.scale(cachedKX, cachedKY);
//                ((Thumbnailable) view).paintThumbnail(g2);
//                g2.dispose();
//
//                g.setColor(FILL_COLOR);
//                g.fillRect(viewVisibleRect.x, viewVisibleRect.y, 
//                        viewVisibleRect.width, viewVisibleRect.height);
//                g.setColor(STROKE_COLOR);
//                g.drawRect(viewVisibleRect.x, viewVisibleRect.y, 
//                        viewVisibleRect.width - 1, viewVisibleRect.height - 1);
//            }
//        }
//        
//        
//        public void centralize() {
//            Rectangle viewRect = calculateViewRectangle();
//            Rectangle viewVisibleRect = calculateViewVisibleRectangle();
//            
//            int cx = viewRect.x + viewVisibleRect.x + viewVisibleRect.width / 2;
//            int cy = viewRect.y + viewVisibleRect.y + viewVisibleRect.height / 2;
//            
//            JViewport viewport = (JViewport) getParent();
//            Dimension extentSize = viewport.getExtentSize();
//            
//            int x0 = cx - extentSize.width / 2;
//            int y0 = cy - extentSize.height / 2;
//            
//            if (x0 + extentSize.width > getWidth()) {
//                x0 = getWidth() - extentSize.width;
//            }
//            
//            if (y0 + extentSize.height > getHeight()) {
//                y0 = getHeight() - extentSize.height;
//            }
//            
//            if (x0 < 0) x0 = 0;
//            if (y0 < 0) y0 = 0;
//            
//            viewport.setViewPosition(new Point(x0, y0));
//        }        
//        
//        
//        private JScrollPane getScrollPane() {
//            return (JScrollPane) getParent().getParent().getParent();
//        }
//        
//        
//        private JViewport getViewport() {
//            return getScrollPane().getViewport();
//        }
//        
//        
//        private JComponent getView() {
//            return (JComponent) getViewport().getView();
//        }
//        
//        
//        public void addLayoutComponent(String name, Component comp) {}
//        public void removeLayoutComponent(Component comp) {}
//
//
//        public Dimension preferredLayoutSize(Container parent) {
//            return calculatePreferredSize();
//        }
//        
//
//        public Dimension minimumLayoutSize(Container parent) {
//            return calculatePreferredSize();
//        }
//
//        
//        public void layoutContainer(Container parent) {}
//
//        
//        public void mousePressed(MouseEvent e) {
//            Point p = e.getPoint();
//            p.x -= cachedViewRectangle.x;
//            p.y -= cachedViewRectangle.y;
//            
//            if (!cachedViewVisibleRectangle.contains(p)) {
//                JComponent view = getView();
//                
//                Rectangle visibleRect = view.getVisibleRect();
//
//                visibleRect.x = (int) Math.round(p.x / cachedKX 
//                        - 0.5 * visibleRect.width);
//                visibleRect.y = (int) Math.round(p.y / cachedKY 
//                        - 0.5 * visibleRect.height);
//                
//                ignoreViewScroll = true;
//                view.scrollRectToVisible(visibleRect);
//                ignoreViewScroll = false;
//                
//                scrollRectToVisible(calculateViewVisibleRectangle());
//                
//                mouseDX = 0.0;
//                mouseDY = 0.0;
//            } else {
//                mouseDX = p.x - cachedViewVisibleRectangleCenter.getX();
//                mouseDY = p.y - cachedViewVisibleRectangleCenter.getY();
//            }
//            
//            lastMouseEvent = null;
//            lastMouseOnScreenPosition = null;
//            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
//        }
//
//        
//        public void mouseReleased(MouseEvent e) {
//            lastMouseEvent = null;
//            lastMouseOnScreenPosition = null;
//            timer.stop();
//            
//            Point p = e.getPoint();
//            SwingUtilities.convertPointToScreen(p, this);
//            
//            centralize();
//            
//            SwingUtilities.convertPointFromScreen(p, this);
//            updateCursor(p);
//        }
//        
//
//        public void mouseDragged(MouseEvent e) {
//            timer.stop();
//            
//            storeLastDragEvent(e);
//            
//            Point p = e.getPoint();
//            fitPointInRect(p, getVisibleRect());
//            
//            JComponent view = getView();
//            Rectangle visibleRect = view.getVisibleRect();
//            
//            int newX = (int) Math.round((p.x - cachedViewRectangle.x - mouseDX) 
//                    / cachedKX - 0.5 * visibleRect.width);
//            int newY = (int) Math.round((p.y - cachedViewRectangle.y - mouseDY) 
//                    / cachedKY - 0.5 * visibleRect.height);
//            
//            if (visibleRect.x != newX || visibleRect.y != newY) {
//                visibleRect.x = newX;
//                visibleRect.y = newY;
//                
//                ignoreViewScroll = true;
//                view.scrollRectToVisible(visibleRect);
//                ignoreViewScroll = false;
//
//                scrollRectToVisible(calculateViewVisibleRectangle());
//            }
//            
//            timer.start();
//        }
//
//        
//        public void mouseMoved(MouseEvent e) {
//            updateCursor(e.getPoint());
//        }
//        
//        
//        private void updateCursor(Point point) {
//            Cursor newCursor;
//            if (point == null) {
//                newCursor = Cursor.getDefaultCursor();
//            } else {
//                point.x -= cachedViewRectangle.x;
//                point.y -= cachedViewRectangle.y;
//                
//                if (cachedViewVisibleRectangle.contains(point)) {
//                    newCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
//                } else {
//                    newCursor = Cursor.getDefaultCursor();
//                }
//            }
//            
//            if (!newCursor.equals(getCursor())) {
//                setCursor(newCursor);
//            }
//        }
//
//        
//        public void actionPerformed(ActionEvent e) {
//            if (lastMouseEvent == null) {
//                timer.stop();
//                return;
//            }
//            
//            if (e.getWhen() < lastMouseEvent.getWhen() + 50) {
//                return;
//            }
//            
//            Point p = new Point(lastMouseOnScreenPosition);
//            SwingUtilities.convertPointFromScreen(p, this);
//            
//            mouseDragged(new MouseEvent(this, 
//                    lastMouseEvent.getID(), 
//                    e.getWhen(), 
//                    lastMouseEvent.getModifiers(), 
//                    p.x, p.y, 
//                    lastMouseEvent.getClickCount(), 
//                    lastMouseEvent.isPopupTrigger()));
//        }
//        
//
//        public void mouseEntered(MouseEvent e) {}
//        public void mouseExited(MouseEvent e) {}
//        public void mouseClicked(MouseEvent e) {}
//        
//        
//        private void storeLastDragEvent(MouseEvent event) {
//            lastMouseEvent = event;
//            lastMouseOnScreenPosition = event.getPoint();
//            SwingUtilities.convertPointToScreen(lastMouseOnScreenPosition, this);
//        }
//    }
//    
//    
//    private static class ThumbToggleButton extends JToggleButton {
//        public ThumbToggleButton() {
//            setToolTipText(NbBundle.getMessage(ThumbScrollPane.class, 
//                    "TOOLTIP_ShowThumbnailView")); // NOI18N
//        }
//        
//        public void paintComponent(Graphics g) {
//            super.paintComponent(g);
//
//            int x = 4;
//            int y = 4;
//            int w = getWidth() - 8;
//            int h = getHeight() - 8;
//
//            if (getUI() instanceof MetalToggleButtonUI) {
//                w++;
//                h++;
//            }
//            
//            g.setColor(new Color(0xB3D0F2));
//            g.fillRect(x + w / 2, y + h / 2, w - w / 2 - 1, h - h / 2 - 1);
//            
//            g.setColor(new Color(0x4876C6));
//            g.drawRect(x + w / 2, y + h / 2, w - w / 2 - 1, h - h / 2 - 1);
//            
//            g.setColor(new Color(0x0C3C9D));
//            g.drawRect(x, y, w - 1, h - 1);
//        }
//    }
//    
//    
//    private static class ThumbScrollPaneLayout extends ScrollPaneLayout {
//        public void layoutContainer(Container parent) {
//            super.layoutContainer(parent);
//            
//            ThumbScrollPane thumbScrollPane = (ThumbScrollPane) parent;
//            JViewport viewport = thumbScrollPane.getViewport();
//
//            JScrollPane thumbnailViewScrollPane = thumbScrollPane
//                    .getThumbnailViewScrollPane();
//
//            JScrollBar hsb = getHorizontalScrollBar();
//            JScrollBar vsb = getVerticalScrollBar();
//            
//            JComponent corner = (JComponent) getCorner(LOWER_RIGHT_CORNER);
//            
//            Rectangle rect = viewport.getBounds();
//            Insets insets = viewport.getInsets();
//            
//            rect.x += insets.left;
//            rect.width -= insets.left + insets.right;
//            
//            rect.y += insets.top;
//            rect.height -= insets.top + insets.bottom;
//            
//            int size = (int) Math.round(
//                    Math.min(ASPECT_MAX_UNIT,
//                    THUMBNAIL_K * Math.min(
//                            rect.getWidth() / ASPECT_RATIO_X,
//                            rect.getHeight() / ASPECT_RATIO_Y)));
//            
//            int thumbnailViewWidth = size * ASPECT_RATIO_X;
//            int thumbnailViewHeight = size * ASPECT_RATIO_Y;
//            
//            boolean hsbVisible = hsb.isVisible();
//            boolean vsbVisible = vsb.isVisible();
//            
//            boolean canShowCorner = viewport.getView() instanceof Thumbnailable;
//            
//            if (size < ASPECT_MIN_UNIT) {
//                canShowCorner = false;
//            } if (!hsbVisible && !vsbVisible) {
//                canShowCorner = false;
//            } 
//
//            if (canShowCorner) {
//                thumbnailViewScrollPane.setVisible(thumbScrollPane.isThumbnailViewShown());
//                int x = rect.x + rect.width - thumbnailViewWidth - VIEW_MARGIN;
//                int y = rect.y + rect.height - thumbnailViewHeight - VIEW_MARGIN;
//                thumbnailViewScrollPane.setBounds(x, y, thumbnailViewWidth, 
//                        thumbnailViewHeight);
//                thumbnailViewScrollPane.getViewport().getView().invalidate();
//                
//                corner.setVisible(true);
//                
//                if (hsbVisible != vsbVisible) {
//                    if (hsbVisible) {
//                        Rectangle hsbBounds = hsb.getBounds();
//                        int cornerWidth = vsb.getPreferredSize().width;
//
//                        hsbBounds.width -= cornerWidth;
//
//                        hsb.setBounds(hsbBounds);
//                        corner.setBounds(hsbBounds.x + hsbBounds.width, 
//                                hsbBounds.y, cornerWidth, hsbBounds.height);
//                    } else { // vsbVisible
//                        Rectangle vsbBounds = vsb.getBounds();
//                        int cornerHeight = hsb.getPreferredSize().height;
//
//                        vsbBounds.height -= cornerHeight;
//                        vsb.setBounds(vsbBounds);
//                        corner.setBounds(vsbBounds.x, vsbBounds.y + vsbBounds.height, 
//                                vsbBounds.width, cornerHeight);
//                    }
//                }
//            } else {
//                corner.setVisible(false);
//                thumbnailViewScrollPane.setVisible(false);
//            }
//            
//            if (thumbScrollPane.isVisible()) {
//                thumbScrollPane.invalidate();
//            }
//        }
//
//
//        public void addLayoutComponent(String s, Component c) {
//            if (c instanceof ThumbnailView) return;
//            super.addLayoutComponent(s, c);
//        }
//
//        public void removeLayoutComponent(Component c) {
//            if (c instanceof ThumbnailView) return;
//            super.removeLayoutComponent(c);
//        }
//    }
//    
//    
//    private static class ThumbViewportLayout extends ViewportLayout {
//        public void layoutContainer(Container parent) {
//            super.layoutContainer(parent);
//            
//            ThumbnailView thumbnailView = ((ThumbScrollPane) parent.getParent())
//                    .getThumbnailView();
//            
//            if (thumbnailView.getParent().getParent().isVisible()) {
//                thumbnailView.invalidate();
//            }
//        }
//    }
//
//    
//    private static class ThumbnailViewportLayout extends ViewportLayout {
//        public void layoutContainer(Container parent) {
//            JViewport viewport = (JViewport) parent;
//            ThumbnailView thumbnailView = (ThumbnailView) viewport.getView();
//            
//            super.layoutContainer(parent);
//            
//            thumbnailView.centralize();
//        }
//    }
//    
//    
//    private static void fitPointInRect(Point point, Rectangle rect) {
//        point.x = Math.max(rect.x, Math.min(point.x, rect.x + rect.width - 1));
//        point.y = Math.max(rect.y, Math.min(point.y, rect.y + rect.height - 1));
//    }
//    
//    
//    public interface Thumbnailable {
//        public void paintThumbnail(Graphics g);
//    }
//
//    private static final double VIEWPORT_K_X = 0.8;
//    private static final double VIEWPORT_K_Y = 0.5;
//    
//    private static final double THUMBNAIL_K = 0.4;
//    private static final int ASPECT_RATIO_X = 3;
//    private static final int ASPECT_RATIO_Y = 4;
//    private static final int ASPECT_MAX_UNIT = 80;
//    private static final int ASPECT_MIN_UNIT = 20;
//    
//    private static final int VIEW_MARGIN = 4;
//    private static final Color STROKE_COLOR = new Color(0x66000000, true);
//    private static final Color FILL_COLOR = new Color(0x11000000, true);
//    
//    private static final String TOGGLE_THUMBNAIL = "toggle_thumbnail";
//    private static final KeyStroke TOGGLE_THUMBNAIL_KEYSTROKE = KeyStroke
//            .getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK);
//}
