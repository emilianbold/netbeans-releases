package org.netbeans.modules.bpel.design;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author anjeleevich
 */
public class TriScrollPane extends JScrollPane {

    private JComponent left;
    private JComponent right;
    private JComponent center;
    private JComponent handToolPanel;
    private JComponent overlayPanel;
    private SideComponent leftComponent;
    private SideComponent rightComponent;
    private boolean layoutNow = false;
    private ArrayList<ScrollListener> scrollListeners = new ArrayList<ScrollListener>(1);

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

        add(leftComponent, 0);
        add(rightComponent, 0);
        
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

//    public int getWidth() {
//        if (layoutNow) {
//            int leftWidth = (!left.isVisible()) ? 0
//                    : leftComponent.getPreferredSize().width;
//            int rightWidth = (!right.isVisible()) ? 0
//                    : rightComponent.getPreferredSize().width;
//
//            return super.getWidth() - leftWidth - rightWidth;
//        }
//
//        return super.getWidth();
//    }

    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public void doLayout() {
//        layoutNow = true;
        super.doLayout();
//        layoutNow = false;

        boolean leftVisible = left.isVisible();
        boolean rightVisible = right.isVisible();

        int leftWidth = (!leftVisible) ? 0
                : leftComponent.getPreferredSize().width;
        int rightWidth = (!rightVisible) ? 0
                : rightComponent.getPreferredSize().width;
        
//        System.out.println("LW=" + leftWidth);
//        System.out.println("RW=" + rightWidth);
//
//        JScrollBar vsb = getVerticalScrollBar();
//        JScrollBar hsb = getHorizontalScrollBar();
//
//        Rectangle viewportBounds = getViewport().getBounds();
//        
//        if (handToolPanel != null) {
//            handToolPanel.setBounds(viewportBounds.x, viewportBounds.y, 
//                    leftWidth + viewportBounds.width + rightWidth,
//                    viewportBounds.height);
//        }
//        
//        if (overlayPanel != null) {
//            overlayPanel.setBounds(viewportBounds.x, viewportBounds.y, 
//                    leftWidth + viewportBounds.width + rightWidth,
//                    viewportBounds.height);
//        }
//        
//        
//        Rectangle vsbBounds = (vsb.isVisible()) ? vsb.getBounds() : null;
//        Rectangle hsbBounds = (hsb.isVisible()) ? hsb.getBounds() : null;
//
//        int vsbOffset = leftWidth + rightWidth;
//
//        if (leftWidth != 0) {
//            viewportBounds.x += leftWidth;
//            viewport.setBounds(viewportBounds);
//        }
//
//        if (vsbOffset != 0) {
//            if (vsbBounds != null) {
//                vsbBounds.x += vsbOffset;
//                vsb.setBounds(vsbBounds);
//            }
//
//            if (hsbBounds != null) {
//                hsbBounds.width += vsbOffset;
//                hsb.setBounds(hsbBounds);
//            }
//        }

        Rectangle viewportBounds = getViewport().getBounds();
        
//        System.out.println("viewportBounds=" + viewportBounds);
        
        if (leftVisible) {
            leftComponent.setVisible(true);
            leftComponent.setBounds(viewportBounds.x, viewportBounds.y, 
                    leftWidth, viewportBounds.height);
        }

        if (rightVisible) {
            rightComponent.setVisible(true);
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

//        System.out.println("LB=" + leftComponent.getBounds());
//        System.out.println("RB=" + rightComponent.getBounds());
    }

    public JComponent getComponent(Point pt) {
        if (rightComponent.getBounds().contains(pt)) {
            return right;
        } else if (viewport.getBounds().contains(pt)) {
            return center;
        } else if (leftComponent.getBounds().contains(pt)) {
            return left;
        } else {
            return null;
        }
    }

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
}
