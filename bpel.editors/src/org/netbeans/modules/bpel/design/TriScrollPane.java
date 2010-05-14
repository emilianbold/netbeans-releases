package org.netbeans.modules.bpel.design;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ViewportLayout;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class TriScrollPane extends JScrollPane implements ActionListener, 
        AdjustmentListener
{

    private JComponent left;
    private JComponent right;
    private JComponent center;
    private JComponent handToolPanel;
    private JComponent overlayPanel;
    private SideComponent leftComponent;
    private SideComponent rightComponent;
    private boolean layoutNow = false;
    private ArrayList<ScrollListener> scrollListeners = new ArrayList<ScrollListener>(1);
    private JToggleButton thumbToggleButton;
    private ThumbnailView thumbnailView;
    
    private JScrollPane thumbScrollPane;

    public TriScrollPane(JComponent center, JComponent left,
            JComponent right, 
            JComponent handToolPanel, 
            JComponent overlayPanel) 
    {
        super(center);

        setBorder(null);

        this.handToolPanel = handToolPanel;
        this.overlayPanel = overlayPanel;
        
        this.left = left;
        this.right = right;
        this.center = center;
        
        left.setBackground(SIDE_BACKGROUND);
        right.setBackground(SIDE_BACKGROUND);

        leftComponent = new SideComponent(left);
        rightComponent = new SideComponent(right);

        thumbToggleButton = new ThumbToggleButton();
        thumbToggleButton.addActionListener(this);
        
        thumbnailView = new ThumbnailView();
        
        thumbScrollPane = new JScrollPane(thumbnailView, 
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        thumbScrollPane.getViewport().setLayout(new ThumbnailViewportLayout());
        
        getHorizontalScrollBar().addAdjustmentListener(this);
        getVerticalScrollBar().addAdjustmentListener(this);
        
        add(thumbToggleButton);
        add(leftComponent, 0);
        add(rightComponent, 0);
        add(thumbScrollPane, 0);
        
        if (overlayPanel != null) {
            add(overlayPanel, 0);
        }
        
        if (handToolPanel != null) {
            add(handToolPanel, 0);
        }

        getVerticalScrollBar().setUnitIncrement(10);
        getHorizontalScrollBar().setUnitIncrement(10);
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        getViewport().addChangeListener(myScrollListener);
        leftComponent.getViewport().addChangeListener(myScrollListener);
        rightComponent.getViewport().addChangeListener(myScrollListener);
        
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                TOGGLE_THUMBNAIL_KEYSTROKE, TOGGLE_THUMBNAIL);
        getActionMap().put(TOGGLE_THUMBNAIL, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                thumbToggleButton.setSelected(!thumbToggleButton
                        .isSelected());
                TriScrollPane.this.actionPerformed(e);
            }
        });        
    }
    
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (thumbnailView.isIgnoreViewScroll()) return;
        
        if (thumbScrollPane.isVisible()) {
            thumbnailView.revalidate();
            thumbnailView.repaint();
        }
    }    
    
    public void actionPerformed(ActionEvent e) {
        thumbScrollPane.setVisible(thumbToggleButton.isSelected() 
                && (getHorizontalScrollBar().isVisible() 
                || getVerticalScrollBar().isVisible()));
    }
    
    public int getLeftPreferredWidth() {
        return (left.isVisible()) ? leftComponent.getPreferredSize().width : 0;
    }

    public int getRightPreferredWidth() {
        return (right.isVisible()) ? rightComponent.getPreferredSize().width : 0;
    }
    
    public void addScrollListener(ScrollListener l) {
        scrollListeners.add(l);
    }

    public void removeScrollListener(ScrollListener l) {
        scrollListeners.remove(l);
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public void doLayout() {
        super.doLayout();

        boolean leftVisible = left.isVisible();
        boolean rightVisible = right.isVisible();

        int leftWidth = (!leftVisible) ? 0
                : leftComponent.getPreferredSize().width;
        int rightWidth = (!rightVisible) ? 0
                : rightComponent.getPreferredSize().width;
        
        Rectangle viewportBounds = getViewport().getBounds();

        leftComponent.setVisible(leftVisible);
        if (leftVisible) {
            leftComponent.setBounds(viewportBounds.x, viewportBounds.y, 
                    leftWidth, viewportBounds.height);
        }

        rightComponent.setVisible(rightVisible);
        if (rightVisible) {
            rightComponent.setBounds(viewportBounds.x + viewportBounds.width 
                    - rightWidth, viewportBounds.y, 
                    rightWidth, viewportBounds.height);
        }
        
        if (overlayPanel != null) {
            overlayPanel.setBounds(viewportBounds);
        }
        
        if (handToolPanel != null) {
            handToolPanel.setBounds(viewportBounds);
        }
        
        Rectangle rect = viewportBounds.getBounds();
        Insets insets = getViewport().getInsets();

        rect.x += insets.left;
        rect.width -= insets.left + insets.right;

        rect.y += insets.top;
        rect.height -= insets.top + insets.bottom;

        int size = (int) Math.round(
                Math.min(ASPECT_MAX_UNIT,
                THUMBNAIL_K * Math.min(
                        rect.getWidth() / ASPECT_RATIO_X,
                        rect.getHeight() / ASPECT_RATIO_Y)));

        int thumbnailViewWidth = size * ASPECT_RATIO_X;
        int thumbnailViewHeight = size * ASPECT_RATIO_Y;
        
        JScrollBar hsb = getHorizontalScrollBar();
        JScrollBar vsb = getVerticalScrollBar();
        
        boolean hsbVisible = hsb.isVisible();
        boolean vsbVisible = vsb.isVisible();
        
        if (hsbVisible || vsbVisible) {
            thumbToggleButton.setVisible(true);
            
            int thumbToggleButtonWidth = (vsbVisible) 
                    ? vsb.getWidth()
                    : vsb.getPreferredSize().width;
            int thumbToggleButtonHeight = (hsbVisible)
                    ? hsb.getHeight()
                    : hsb.getPreferredSize().height;
            
            int thumbToggleButtonX = (vsbVisible) 
                    ? vsb.getX()
                    : hsb.getX() + hsb.getWidth() - thumbToggleButtonWidth;
            
            int thumbToggleButtonY = (hsbVisible) 
                    ? hsb.getY()
                    : vsb.getY() + vsb.getHeight() - thumbToggleButtonHeight;
            
            if (!hsbVisible || !vsbVisible) {
                if (hsbVisible) {
                    hsb.setBounds(hsb.getX(), hsb.getY(), hsb.getWidth() 
                            - thumbToggleButtonWidth, hsb.getHeight());
                }
                
                if (vsbVisible) {
                    vsb.setBounds(vsb.getX(), vsb.getY(), vsb.getWidth(),
                            vsb.getHeight() - thumbToggleButtonHeight);
                }
            }
            
            int thumbnailViewX = rect.x + rect.width - thumbnailViewWidth 
                    - rightWidth - VIEW_MARGIN;
            int thumbnailViewY = rect.y + rect.height - thumbnailViewHeight 
                    - VIEW_MARGIN;
            
            thumbScrollPane.setBounds(thumbnailViewX, thumbnailViewY, 
                    thumbnailViewWidth, thumbnailViewHeight);
            thumbScrollPane.setVisible(thumbToggleButton.isSelected());
            thumbToggleButton.setBounds(thumbToggleButtonX, thumbToggleButtonY,
                    thumbToggleButtonWidth, thumbToggleButtonHeight);
        } else {
            thumbToggleButton.setVisible(false);
            thumbScrollPane.setVisible(false);
        }

    }

    public JComponent getComponent(Point pt) {
        if (rightComponent.getBounds().contains(pt)) {
            return right;
        } else if (leftComponent.getBounds().contains(pt)) {
            return left;
        } else if (viewport.getBounds().contains(pt)) {
            return center;
        } else {
            return null;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();

        boolean leftVisible = left.isVisible();
        boolean rightVisible = right.isVisible();

        int leftWidth = (!leftVisible) ? 0
                : leftComponent.getPreferredSize().width;
        int rightWidth = (!rightVisible) ? 0
                : rightComponent.getPreferredSize().width;

        size.width = Math.max(size.width, leftWidth + 120 + rightWidth);

        return size;
    }

    private static class SideComponent extends JScrollPane implements
            ChangeListener, MouseWheelListener, ActionListener {

        JButton scrollUpButton;
        JButton scrollDownButton;
        Timer scrollTimer;
        int scrollTimerDirection = 0;

        SideComponent(JComponent content) {
            super(content);
            setOpaque(false);
            setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
            setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
            setBorder(null);
            getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
            getViewport().setOpaque(false);

            scrollUpButton = new UpButton();
            scrollDownButton = new DownButton();

            add(scrollUpButton, 0);
            add(scrollDownButton, 0);

            getViewport().addChangeListener(this);
            addMouseWheelListener(this);
            
            scrollUpButton.getModel().addChangeListener(this);
            scrollDownButton.getModel().addChangeListener(this);

            scrollTimer = new Timer(100, this);
        }

        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();

            size.width = Math.max(size.width, BUTTON_SIZE + BUTTON_PADDING * 2);
            size.height = Math.max(size.height, 
                    2 * BUTTON_SIZE + BUTTON_PADDING * 2 + BUTTON_MARGIN);

            return size;
        }

        public void doLayout() {
            super.doLayout();

            int x = 0;
            int y = 0;
            int w = getWidth();
            int h = getHeight();

            int buttonX = x + (w - BUTTON_SIZE) / 2;

            scrollUpButton.setBounds(buttonX, y + BUTTON_PADDING,
                    BUTTON_SIZE, BUTTON_SIZE);

            scrollDownButton.setBounds(buttonX,
                    y + h - BUTTON_SIZE - BUTTON_PADDING,
                    BUTTON_SIZE, BUTTON_SIZE);

            updateButtonsVisibility();
        }

        private void updateButtonsVisibility() {
            JViewport viewport = getViewport();

            Rectangle viewRect = viewport.getViewRect();
            Dimension viewSize = viewport.getViewSize();

            scrollUpButton.setVisible(viewRect.y > 0);
            scrollDownButton.setVisible(viewRect.y + viewRect.height < viewSize.height);
        }

        private void scroll(int scrollUnits) {
            if (scrollUnits == 0) {
                return;
            }

            int delta = scrollUnits * 10;

            JViewport viewport = getViewport();

            Point viewPosition = viewport.getViewPosition();

            int yMax = viewport.getViewSize().height - viewport.getExtentSize().height;

            viewPosition.y = Math.max(0, Math.min(viewPosition.y + delta, yMax));

            viewport.setViewPosition(viewPosition);
        }

        public boolean isOptimizedDrawingEnabled() {
            return false;
        }

        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == getViewport()) {
                updateButtonsVisibility();
            } else if (e.getSource() == scrollUpButton.getModel()) {
                if (scrollUpButton.getModel().isPressed()) {
                    scrollTimerDirection = -3;
                    scroll(scrollTimerDirection);
                    scrollTimer.restart();
                } else {
                    scrollTimer.stop();
                }
            } else if (e.getSource() == scrollDownButton.getModel()) {
                if (scrollDownButton.getModel().isPressed()) {
                    scrollTimerDirection = 3;
                    scroll(scrollTimerDirection);
                    scrollTimer.restart();
                } else {
                    scrollTimer.stop();
                }
            }
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            scroll(e.getUnitsToScroll());
        }

        public void actionPerformed(ActionEvent e) {
            scroll(scrollTimerDirection);
        }
    }

    private abstract static class ScrollButton extends JButton {

        ScrollButton() {
            setContentAreaFilled(false);
            setRolloverEnabled(true);
            setBorderPainted(false);
            setOpaque(false);
            setFocusable(false);
        }

        public boolean contains(int x, int y) {
            double rx = 0.5 * getWidth();
            double ry = 0.5 * getHeight();

            double px = x - rx;
            double py = y - ry;

            return px * px / rx / rx + py * py / ry / ry <= 1.0;
        }

        public void paintComponent(Graphics g) {
            int w = getWidth();
            int h = getHeight();

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);

            ButtonModel model = getModel();
            Shape icon = createIconShape();

            if (model.isPressed()) {
                g2.setPaint(BUTTON_PRESSED_FILL);
            } else if (model.isRollover()) {
                g2.setPaint(BUTTON_ROLLOVER_FILL);
            } else {
                g2.setPaint(BUTTON_FILL);
            }
            g2.translate(0.5, 0.5);
            g2.fillOval(0, 0, w - 1, h - 1);
            g2.translate(-0.5, -0.5);

            if (model.isRollover()) {
                g2.setPaint(ICON_COLOR_ROLLOVER);
            } else {
                g2.setPaint(ICON_COLOR);
            }
            g2.fill(icon);

            if (model.isPressed()) {
                g2.setPaint(BUTTON_PRESSED_STROKE);
            } else if (model.isRollover()) {
                g2.setPaint(BUTTON_ROLLOVER_STROKE);
            } else {
                g2.setPaint(BUTTON_STROKE);
            }
            g2.translate(0.5, 0.5);
            g2.drawOval(0, 0, w - 1, h - 1);
            g2.translate(-0.5, -0.5);
            g2.draw(icon);


            g2.dispose();
        }

        public Dimension getPreferredSize() {
            return new Dimension(BUTTON_SIZE, BUTTON_SIZE);
        }

        abstract Shape createIconShape();
    }

    private static class UpButton extends ScrollButton {

        Shape createIconShape() {
            ButtonModel model = getModel();

            float cx = 0.5f * getWidth();
            float cy = 0.5f * getHeight();

            float r = Math.min(cx, cy) - 6.5f;
            float x = r * COS_30;
            float y = r * SIN_30;

            GeneralPath gp = new GeneralPath();
            gp.moveTo(cx, cy - r);
            gp.lineTo(cx + x, cy + y);
            gp.lineTo(cx - x, cy + y);
            gp.closePath();

            return gp;
        }
    }

    private static class DownButton extends ScrollButton {

        Shape createIconShape() {
            ButtonModel model = getModel();

            float cx = 0.5f * getWidth();
            float cy = 0.5f * getHeight();

            float r = -(Math.min(cx, cy) - 6.5f);
            float x = r * COS_30;
            float y = r * SIN_30;

            GeneralPath gp = new GeneralPath();
            gp.moveTo(cx, cy - r);
            gp.lineTo(cx + x, cy + y);
            gp.lineTo(cx - x, cy + y);
            gp.closePath();

            return gp;
        }
    }
    private static final Border LEFT_SIDE_BORDER = new Border() {

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 1);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height) {
            Color oldColor = g.getColor();

            int x1 = x + width - 1;
            int y1 = y + height - 1;

            g.setColor(c.getBackground().darker());
            g.drawLine(x1, y, x1, y1);
            g.setColor(oldColor);
        }
    };
    private static final Border RIGHT_SIDE_BORDER = new Border() {

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 1, 0, 0);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color oldColor = g.getColor();

            int y1 = y + height - 1;

            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(x, y, x, y1);
            g.setColor(oldColor);
        }
    };
    
    private ChangeListener myScrollListener  = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() != getViewport()) {
                center.repaint();
            }
            
            for (ScrollListener l : scrollListeners) {
                l.viewScrolled(null);
            }
        }
    };
    

    public interface ScrollListener {
        void viewScrolled(JComponent view);
    }
    
    private static class ThumbToggleButton extends JToggleButton {
        public ThumbToggleButton() {
            setToolTipText(NbBundle.getMessage(TriScrollPane.class, 
                    "TOOLTIP_ShowThumbnailView")); // NOI18N
            setFocusable(false);
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            int x = 4;
            int y = 4;
            int w = getWidth() - 8;
            int h = getHeight() - 8;

            if (getUI() instanceof MetalToggleButtonUI) {
                w++;
                h++;
            }
            
            g.setColor(new Color(0xB3D0F2));
            g.fillRect(x + w / 2, y + h / 2, w - w / 2 - 1, h - h / 2 - 1);
            
            g.setColor(new Color(0x4876C6));
            g.drawRect(x + w / 2, y + h / 2, w - w / 2 - 1, h - h / 2 - 1);
            
            g.setColor(new Color(0x0C3C9D));
            g.drawRect(x, y, w - 1, h - 1);
        }
    }
    
    
    private static class ThumbnailView extends JPanel implements MouseListener, 
            MouseMotionListener, ActionListener 
    {
        private Dimension cachedPreferredSize;
        private Rectangle cachedViewRectangle;
        private Rectangle cachedViewVisibleRectangle;
        private Point2D cachedViewVisibleRectangleCenter;
        private double cachedKX;
        private double cachedKY;
        
        private double mouseDX;
        private double mouseDY;
        
        private boolean ignoreViewScroll = false;
        
        private MouseEvent lastMouseEvent = null;
        private Point lastMouseOnScreenPosition = null;
        
        private Timer timer;
        
        
        public ThumbnailView() {
            addMouseListener(this);
            addMouseMotionListener(this);
            timer = new Timer(100, this);
            setFocusable(false);
        }
        
        
        public boolean isIgnoreViewScroll() {
            return ignoreViewScroll;
        }
        
        
        public Dimension calculatePreferredSize() {
            JViewport viewport = (JViewport) getParent();
            Dimension viewportSize = viewport.getSize();
            Insets viewportInsets = viewport.getInsets();
            
            int w = viewportSize.width - viewportInsets.left 
                    - viewportInsets.right;
            int h = viewportSize.height - viewportInsets.top 
                    - viewportInsets.bottom;

            Dimension viewSize = getView().getSize();
            Rectangle visibleRect = getView().getVisibleRect();
            
            double viewWidthK = (double) w / viewSize.width;
            double viewHeightK = (double) h / viewSize.height;
            double viewK = Math.min(viewWidthK, viewHeightK);
            
            double viewportK = Math.min(
                    w * VIEWPORT_K_X / visibleRect.width, 
                    h * VIEWPORT_K_Y / visibleRect.height);

            double k = (viewK >= viewportK) ? viewK : viewportK;
            
            cachedPreferredSize = new Dimension(
                    (int) Math.round(viewSize.width * k), 
                    (int) Math.round(viewSize.height * k));
            
            return new Dimension(cachedPreferredSize);
        }
        
        
        private Rectangle calculateViewRectangle() {
            int w = cachedPreferredSize.width;
            int h = cachedPreferredSize.height;
            
            int xOffset = (getWidth() - w) / 2;
            int yOffset = (getHeight() - h) / 2;
            
             cachedViewRectangle = new Rectangle(xOffset, yOffset, w, h);
             
             return new Rectangle(cachedViewRectangle);
        }
        
        
        private Rectangle convertViewVisibleRectangle(Rectangle viewVisibleRect) 
        {
            JComponent view = getView();
            Dimension viewSize = view.getSize();
            
            double kx = (double) cachedPreferredSize.width / viewSize.width;
            double ky = (double) cachedPreferredSize.height / viewSize.height;
            
            int x1 = (int) Math.round(viewVisibleRect.getMinX() * kx);
            int y1 = (int) Math.round(viewVisibleRect.getMinY() * ky);
            int x2 = (int) Math.round(viewVisibleRect.getMaxX() * kx);
            int y2 = (int) Math.round(viewVisibleRect.getMaxY() * ky);
            
            return new Rectangle(x1, y1, x2 - x1, y2 - y1);
        }
        
        
        private Rectangle calculateViewVisibleRectangle() {
            JComponent view = getView();
            Dimension viewSize = view.getSize();
            
            double kx = (double) cachedPreferredSize.width / viewSize.width;
            double ky = (double) cachedPreferredSize.height / viewSize.height;
            
            Rectangle viewVisibleRect = view.getVisibleRect();
            
            int x1 = (int) Math.round(viewVisibleRect.getMinX() * kx);
            int y1 = (int) Math.round(viewVisibleRect.getMinY() * ky);
            int x2 = (int) Math.round(viewVisibleRect.getMaxX() * kx);
            int y2 = (int) Math.round(viewVisibleRect.getMaxY() * ky);
            
            cachedKX = kx;
            cachedKY = ky;
            
            cachedViewVisibleRectangleCenter = new Point2D.Double(
                    viewVisibleRect.getCenterX() * kx, 
                    viewVisibleRect.getCenterY() * ky);
            
            cachedViewVisibleRectangle = new Rectangle(x1, y1, 
                    x2 - x1, y2 - y1);
            
            return new Rectangle(cachedViewVisibleRectangle);
        }
        
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Rectangle viewRect = new Rectangle(cachedViewRectangle);
            Rectangle viewVisibleRect = new Rectangle(cachedViewVisibleRectangle);
            
            g.translate(viewRect.x, viewRect.y);
            
            JComponent view = getView();
            Color background = view.getBackground();
            
            g.setColor((background == null) ? Color.WHITE : background);
            g.fillRect(0, 0, viewRect.width, viewRect.height);

            if (view instanceof Thumbnailable) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.scale(cachedKX, cachedKY);
                ((Thumbnailable) view).paintThumbnail(g2);
                g2.dispose();
            }
            
            g.setColor(FILL_COLOR);
            g.fillRect(viewVisibleRect.x, viewVisibleRect.y, 
                    viewVisibleRect.width, viewVisibleRect.height);
            g.setColor(STROKE_COLOR);
            g.drawRect(viewVisibleRect.x, viewVisibleRect.y, 
                    viewVisibleRect.width - 1, viewVisibleRect.height - 1);
        }
        
        
        public void centralize() {
            Rectangle viewRect = calculateViewRectangle();
            Rectangle viewVisibleRect = calculateViewVisibleRectangle();
            
            int cx = viewRect.x + viewVisibleRect.x + viewVisibleRect.width / 2;
            int cy = viewRect.y + viewVisibleRect.y + viewVisibleRect.height / 2;
            
            JViewport viewport = (JViewport) getParent();
            Dimension extentSize = viewport.getExtentSize();
            
            int x0 = cx - extentSize.width / 2;
            int y0 = cy - extentSize.height / 2;
            
            if (x0 + extentSize.width > getWidth()) {
                x0 = getWidth() - extentSize.width;
            }
            
            if (y0 + extentSize.height > getHeight()) {
                y0 = getHeight() - extentSize.height;
            }
            
            if (x0 < 0) x0 = 0;
            if (y0 < 0) y0 = 0;
            
            viewport.setViewPosition(new Point(x0, y0));
        }        
        
        
        private JScrollPane getScrollPane() {
            return (JScrollPane) getParent().getParent().getParent();
        }
        
        
        private JViewport getViewport() {
            return getScrollPane().getViewport();
        }
        
        
        private JComponent getView() {
            return (JComponent) getViewport().getView();
        }
        
        
        public void addLayoutComponent(String name, Component comp) {}
        public void removeLayoutComponent(Component comp) {}


        public Dimension getPreferredSize() {
            return calculatePreferredSize();
        }
        

        public Dimension getMinimumSize() {
            return calculatePreferredSize();
        }

        
        public void doLayout() {}

        
        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            p.x -= cachedViewRectangle.x;
            p.y -= cachedViewRectangle.y;
            
            if (!cachedViewVisibleRectangle.contains(p)) {
                JComponent view = getView();
                
                Rectangle visibleRect = view.getVisibleRect();

                visibleRect.x = (int) Math.round(p.x / cachedKX 
                        - 0.5 * visibleRect.width);
                visibleRect.y = (int) Math.round(p.y / cachedKY 
                        - 0.5 * visibleRect.height);
                
                ignoreViewScroll = true;
                view.scrollRectToVisible(visibleRect);
                ignoreViewScroll = false;
                
                scrollRectToVisible(calculateViewVisibleRectangle());
                
                mouseDX = 0.0;
                mouseDY = 0.0;
            } else {
                mouseDX = p.x - cachedViewVisibleRectangleCenter.getX();
                mouseDY = p.y - cachedViewVisibleRectangleCenter.getY();
            }
            
            lastMouseEvent = null;
            lastMouseOnScreenPosition = null;
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }

        
        public void mouseReleased(MouseEvent e) {
            lastMouseEvent = null;
            lastMouseOnScreenPosition = null;
            timer.stop();
            
            Point p = e.getPoint();
            SwingUtilities.convertPointToScreen(p, this);
            
            centralize();
            
            SwingUtilities.convertPointFromScreen(p, this);
            updateCursor(p);
        }
        

        public void mouseDragged(MouseEvent e) {
            timer.stop();
            
            storeLastDragEvent(e);
            
            Point p = e.getPoint();
            fitPointInRect(p, getVisibleRect());
            
            JComponent view = getView();
            Rectangle visibleRect = view.getVisibleRect();
            
            int newX = (int) Math.round((p.x - cachedViewRectangle.x - mouseDX) 
                    / cachedKX - 0.5 * visibleRect.width);
            int newY = (int) Math.round((p.y - cachedViewRectangle.y - mouseDY) 
                    / cachedKY - 0.5 * visibleRect.height);
            
            if (visibleRect.x != newX || visibleRect.y != newY) {
                visibleRect.x = newX;
                visibleRect.y = newY;
                
                ignoreViewScroll = true;
                view.scrollRectToVisible(visibleRect);
                ignoreViewScroll = false;

                scrollRectToVisible(calculateViewVisibleRectangle());
            }
            
            timer.start();
        }

        
        public void mouseMoved(MouseEvent e) {
            updateCursor(e.getPoint());
        }
        
        
        private void updateCursor(Point point) {
            Cursor newCursor;
            if (point == null) {
                newCursor = Cursor.getDefaultCursor();
            } else {
                point.x -= cachedViewRectangle.x;
                point.y -= cachedViewRectangle.y;
                
                if (cachedViewVisibleRectangle.contains(point)) {
                    newCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
                } else {
                    newCursor = Cursor.getDefaultCursor();
                }
            }
            
            if (!newCursor.equals(getCursor())) {
                setCursor(newCursor);
            }
        }

        
        public void actionPerformed(ActionEvent e) {
            if (lastMouseEvent == null) {
                timer.stop();
                return;
            }
            
            if (e.getWhen() < lastMouseEvent.getWhen() + 50) {
                return;
            }
            
            Point p = new Point(lastMouseOnScreenPosition);
            SwingUtilities.convertPointFromScreen(p, this);
            
            mouseDragged(new MouseEvent(this, 
                    lastMouseEvent.getID(), 
                    e.getWhen(), 
                    lastMouseEvent.getModifiers(), 
                    p.x, p.y, 
                    lastMouseEvent.getClickCount(), 
                    lastMouseEvent.isPopupTrigger()));
        }
        

        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseClicked(MouseEvent e) {}
        
        
        private void storeLastDragEvent(MouseEvent event) {
            lastMouseEvent = event;
            lastMouseOnScreenPosition = event.getPoint();
            SwingUtilities.convertPointToScreen(lastMouseOnScreenPosition, this);
        }
    }
    
    private static class ThumbnailViewportLayout extends ViewportLayout {
        public void layoutContainer(Container parent) {
            JViewport viewport = (JViewport) parent;
            ThumbnailView thumbnailView = (ThumbnailView) viewport.getView();
            
            super.layoutContainer(parent);
            
            thumbnailView.centralize();
        }
    }    
    
    public interface Thumbnailable {
        public void paintThumbnail(Graphics g);
    }

    private static void fitPointInRect(Point point, Rectangle rect) {
        point.x = Math.max(rect.x, Math.min(point.x, rect.x + rect.width - 1));
        point.y = Math.max(rect.y, Math.min(point.y, rect.y + rect.height - 1));
    }
    
    private static final int BUTTON_SIZE = 23;
    private static final int BUTTON_PADDING = 4;
    private static final int BUTTON_MARGIN = 4;
    private static final Color BUTTON_FILL = new Color(0x88CCCCCC, true);
    private static final Color BUTTON_STROKE = new Color(0x88888888, true);
    private static final Color BUTTON_ROLLOVER_FILL = new Color(0xCCCCCC);
    private static final Color BUTTON_ROLLOVER_STROKE = new Color(0x888888);
    private static final Color BUTTON_PRESSED_FILL = new Color(0xBBBBBB);
    private static final Color BUTTON_PRESSED_STROKE = new Color(0x888888);
    private static final Color ICON_COLOR = new Color(0x88FFFFFF, true);
    private static final Color ICON_COLOR_ROLLOVER = new Color(0xFFFFFF);
    private static final float COS_30 = (float) Math.cos(Math.PI / 6);
    private static final float SIN_30 = (float) Math.sin(Math.PI / 6);
    
    private static final Color SIDE_BACKGROUND = new Color(0x44000000 | (new Color(0xFCFAF5).darker().getRGB() & 0xFFFFFF), true);
    
    private static final double VIEWPORT_K_X = 0.8;
    private static final double VIEWPORT_K_Y = 0.5;
    
    private static final double THUMBNAIL_K = 0.4;
    private static final int ASPECT_RATIO_X = 3;
    private static final int ASPECT_RATIO_Y = 4;
    private static final int ASPECT_MAX_UNIT = 80;
    private static final int ASPECT_MIN_UNIT = 20;
    
    private static final int VIEW_MARGIN = 4;
    private static final Color STROKE_COLOR = new Color(0x66000000, true);
    private static final Color FILL_COLOR = new Color(0x11000000, true);
    
    private static final String TOGGLE_THUMBNAIL = "toggle_thumbnail";
    private static final KeyStroke TOGGLE_THUMBNAIL_KEYSTROKE = KeyStroke
            .getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK);
}
