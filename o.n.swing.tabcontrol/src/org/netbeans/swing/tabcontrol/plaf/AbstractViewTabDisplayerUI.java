/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.event.ListDataEvent;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabDisplayerUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.swing.tabcontrol.LocationInformer;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.openide.util.NbBundle;

/**
 * Basic UI class for view tabs - non scrollable tabbed displayer, which shows all
 * tabs equally sized, proportionally. This class is independent on specific
 * L&F, acts as base class for specific L&F descendants.
 * <p>
 * XXX eventually this class should be deleted and a subclass of BasicTabDisplayer can be used;
 * currently this is simply a port of the original code to the new API. Do not introduce any new
 * subclasses of this.
 *
 * @author Dafe Simonek
 *
 */
public abstract class AbstractViewTabDisplayerUI extends TabDisplayerUI {

    private TabDataModel dataModel;

    private ViewTabLayoutModel layoutModel;

    private FontMetrics fm;

    private Font txtFont;

    protected Controller controller;
    
    protected static IconLoader iconCache = new IconLoader();
    
    protected PinButton pinButton;

    /** Pin action */
    private final Action pinAction = new PinAction();
    private static final String PIN_ACTION = "pinAction";

    public AbstractViewTabDisplayerUI (TabDisplayer displayer) {
        super (displayer);
        displayer.setLayout(null);
    }

    public void installUI(JComponent c) {
        super.installUI(c);
        ToolTipManager.sharedInstance().registerComponent(displayer);
        controller = createController();
        dataModel = displayer.getModel();
        layoutModel = new ViewTabLayoutModel(dataModel, displayer);
        dataModel.addChangeListener (controller);
        dataModel.addComplexListDataListener(controller);
        displayer.addPropertyChangeListener (controller);
        selectionModel.addChangeListener (controller);
        displayer.addMouseListener(controller);
        displayer.addMouseMotionListener(controller);
        LocationInformer locInfo = displayer.getLocationInformer();
        if (locInfo != null) {
            pinButton = createPinButton();
        }
        installPinButton();
    }
    
    protected void installPinButton() {
        if (pinButton != null) {
            displayer.add(pinButton);
            pinButton.addActionListener(controller);
        }
    }

    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        ToolTipManager.sharedInstance().unregisterComponent(displayer);
        displayer.removePropertyChangeListener (controller);
        dataModel.removeChangeListener(controller);
        dataModel.removeComplexListDataListener(controller);
        selectionModel.removeChangeListener(controller);
        displayer.removeMouseListener(controller);
        displayer.removeMouseMotionListener(controller);
        if (pinButton != null) {
            displayer.remove(pinButton);
            pinButton.removeActionListener(controller);
            pinButton = null;
        }
        layoutModel = null;
        selectionModel = null;
        dataModel = null;
        controller = null;
    }

    protected abstract Controller createController();

    public void paint(Graphics g, JComponent c) {
        if (ColorUtil.shouldAntialias()) {
            ColorUtil.setupAntialiasing(g);
        }        
        TabData tabData;
        int x, y, width, height;
        String text;

        for (int i = 0; i < dataModel.size(); i++) {
            // gather data
            tabData = dataModel.getTab(i);
            x = layoutModel.getX(i);
            y = layoutModel.getY(i);
            width = layoutModel.getW(i);
            height = layoutModel.getH(i);
            text = tabData.getText();
            // perform paint
            if (g.hitClip(x, y, width, height)) {
                paintTabBackground(g, i, x, y, width, height);
                paintTabContent(g, i, text, x, y, width, height);
                paintTabBorder(g, i, x, y, width, height);
            }
        }
    }

    protected final TabDataModel getDataModel() {
        return dataModel;
    }

    public final TabLayoutModel getLayoutModel() {
        return layoutModel;
    }

    protected final TabDisplayer getDisplayer() {
        return displayer;
    }

    protected final SingleSelectionModel getSelectionModel() {
        return selectionModel;
    }

    public Controller getController() {
        return controller;
    }

    protected final boolean isSelected(int index) {
        return selectionModel.getSelectedIndex() == index;
    }

    protected final boolean isActive() {
        return displayer.isActive();
    }

    protected final boolean isFocused(int index) {
        return isSelected(index) && isActive();
    }

    protected final SingleSelectionModel createSelectionModel() {
        return new DefaultTabSelectionModel (displayer.getModel());
    }

    public String getCommandAtPoint(Point p) {
        return controller.inCloseIconRect(p) != -1 ? TabDisplayer.COMMAND_CLOSE :
                TabDisplayer.COMMAND_SELECT;
    }

    public int dropIndexOfPoint(Point p) {
        int result = 0;
        for (int i=0; i < displayer.getModel().size(); i++) {
            int x = getLayoutModel().getX(i);
            int w = getLayoutModel().getW(i);
            if (p.x >= x && p.x <= x + w) {
                if (i == displayer.getModel().size() - 1) {
                    if (p.x > x + (w / 2)) {
                        result = displayer.getModel().size();
                        break;
                    } else {
                        result = i;
                        break;
                    }
                } else {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Specifies font to use for text and font metrics. Subclasses may override
     * to specify their own text font
     */
    protected Font getTxtFont() {
        if (txtFont == null) {
            txtFont = (Font) UIManager.get("windowTitleFont");
            if (txtFont == null) {
                txtFont = new Font("Dialog", Font.PLAIN, 11);
            } else if (txtFont.isBold()) {
                // don't use deriveFont() - see #49973 for details
                txtFont = new Font(txtFont.getName(), Font.PLAIN, txtFont.getSize());
            }
        }
        return txtFont;
    }

    protected final FontMetrics getTxtFontMetrics() {
        if (fm == null) {
            JComponent control = getDisplayer();
            fm = control.getFontMetrics(getTxtFont());
        }
        return fm;
    }

    protected abstract void paintTabContent(Graphics g, int index, String text,
                                            int x, int y, int width,
                                            int height);

    protected abstract void paintTabBorder(Graphics g, int index, int x, int y,
                                           int width, int height);

    protected abstract void paintTabBackground(Graphics g, int index, int x,
                                               int y, int width, int height);



    /**
     * Utility to return y-axis centered icon position in given tab
     */
    protected final int getCenteredIconY(Icon icon, int index) {
        TabLayoutModel tlm = getLayoutModel();
        int y = tlm.getY(index);
        int h = tlm.getH(index);
        int iconHeight = icon.getIconHeight();
        return y + (Math.max(0, h / 2 - iconHeight / 2));
    }
    
    
    /** Utility method to access pin button instance conveniently */
    protected final PinButton configurePinButton (int index) {
        if (pinButton == null) {
            return null;
        }
        LocationInformer locInfo = getDisplayer().getLocationInformer();
        if (locInfo == null) {
            return null;
        }
        Object orientation = locInfo.getOrientation(getDisplayer().getModel().getTab(index).getComponent());
        pinButton.setOrientation(orientation);
        return pinButton;
    }
    
    /** Subclasses should create and return pin button instance, parametrized
     * to given orientation
     * @see PinButton
     */ 
    // XXX - change back to abstract after implementing in all LFs
    protected /*abstract*/ PinButton createPinButton () {
        Map normalIcons = new HashMap(6);
        normalIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-east.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-west.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-south.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-center.gif");
        Map pressedIcons = new HashMap(6);
        pressedIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-east.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-west.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-south.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-center.gif");
        Map rolloverIcons = new HashMap(6);
        rolloverIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-east.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-west.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-south.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-center.gif");
        return new PinButton(normalIcons, pressedIcons, rolloverIcons);
    }

    /** Reaction to pin button / pin shortcut toggle. Does nothing itself,but
     * produces event for outer window system.
     */
    protected void performPinAction() {
        // pin button only active on selected index, so this is safe here
        int index = getSelectionModel().getSelectedIndex();
        PinButton pinB = configurePinButton(index);
        if (pinB != null) {
            if (TabDisplayer.ORIENTATION_CENTER.equals(pinB.getOrientation())) {
                shouldPerformAction(TabDisplayer.COMMAND_DISABLE_AUTO_HIDE, index, null);
            } else {
                shouldPerformAction(TabDisplayer.COMMAND_ENABLE_AUTO_HIDE, index, null);
            }
            // XXX - what to do if action was not consumed? nothing?
        }
    }
    
    /** Registers shortcut for enable/ disable auto-hide functionality */
    public void unregisterShortcuts(JComponent comp) {
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
            remove(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                                InputEvent.CTRL_DOWN_MASK));
        comp.getActionMap().remove(PIN_ACTION);
    }

    /** Registers shortcut for enable/ disable auto-hide functionality */
    public void registerShortcuts(JComponent comp) {
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
            put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                                InputEvent.CTRL_DOWN_MASK), PIN_ACTION);
        comp.getActionMap().put(PIN_ACTION, pinAction);
    }
    
    public Polygon getExactTabIndication(int index) {
        // TBD - the same code is copied in ScrollableTabsUI, should be shared
        // if will not differ
//        GeneralPath indication = new GeneralPath();
        JComponent control = getDisplayer();
        int height = control.getHeight();

        TabLayoutModel tlm = getLayoutModel();

        int tabXStart = tlm.getX(index);

        int tabXEnd = tabXStart + tlm.getW(index);

        int[] xpoints = new int[4];
        int[] ypoints = new int[4];
        xpoints[0] = tabXStart;
        ypoints[0] = 0;
        xpoints[1] = tabXEnd;
        ypoints[1] = 0;
        xpoints[2] = tabXEnd;
        ypoints[2] = height - 1;
        xpoints[3] = tabXStart;
        ypoints[3] = height - 1;

        return new EqualPolygon(xpoints, ypoints);
    }

    public Polygon getInsertTabIndication(int index) {
        EqualPolygon indication = new EqualPolygon();
        JComponent control = getDisplayer();
        int height = control.getHeight();
        int width = control.getWidth();
        TabLayoutModel tlm = getLayoutModel();

        int tabXStart;
        int tabXEnd;
        if (index == 0) {
            tabXStart = 0;
            tabXEnd = tlm.getW(0) / 2;
        } else if (index >= getDataModel().size()) {
            tabXStart = tlm.getX(index - 1) + tlm.getW(index - 1) / 2;
            tabXEnd = tabXStart + tlm.getW(index - 1);
            if (tabXEnd > width) {
                tabXEnd = width;
            }
        } else {
            tabXStart = tlm.getX(index - 1) + tlm.getW(index - 1) / 2;
            tabXEnd = tlm.getX(index) + tlm.getW(index) / 2;
        }

        indication.moveTo(tabXStart, 0);
        indication.lineTo(tabXEnd, 0);
        indication.lineTo(tabXEnd, height - 1);
        indication.lineTo(tabXStart, height - 1);
        return indication;
    }

    /**
     * Loader for icons. Caches loaded icons using hash map.
     */
    final static class IconLoader {
        /* mapping <String, Icon> from resource paths to icon objects, used as cache */
        private Map paths2Icons;

        /**
         * Finds and returns icon instance from cache, if present. Otherwise
         * loads icon using given resource path and stores icon into cache for
         * next access.
         *
         * @return icon image
         */
        public Icon obtainIcon(String iconPath) {
            if (paths2Icons == null) {
                paths2Icons = new HashMap(6);
            }
            Icon icon = (Icon) paths2Icons.get(iconPath);
            if (icon == null) {
                // not yet in cache, load and store
                Image image = loadImage(iconPath);
                if (image == null) {
                    throw new IllegalArgumentException("Icon with resource path: "
                                                       + iconPath
                                                       + " can't be loaded, probably wrong path.");
                }
                icon = new ImageIcon(image);
                paths2Icons.put(iconPath, icon);
            }
            return icon;
        }

    } // end of IconLoader

    private static Image loadImage(String path) {
        try {
            URL url = AbstractViewTabDisplayerUI.class.getResource("/"+path);
            //Apple Bug ID# 3737894 - some transparent gifs incorrectly loaded 
            //with ImageIO

            //return ImageIO.read(url);
            return Toolkit.getDefaultToolkit().createImage(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Paints the rectangle occupied by a tab into an image and returns the result */
    public Image createImageOfTab(int index) {
        TabData td = displayer.getModel().getTab(index);
        
        JLabel lbl = new JLabel(td.getText());
        int width = lbl.getFontMetrics(lbl.getFont()).stringWidth(td.getText());
        int height = lbl.getFontMetrics(lbl.getFont()).getHeight();
        width = width + td.getIcon().getIconWidth() + 6;
        height = Math.max(height, td.getIcon().getIconHeight()) + 5;
        
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                        .getDefaultScreenDevice().getDefaultConfiguration();
        
        BufferedImage image = config.createCompatibleImage(width, height);
        Graphics2D g = image.createGraphics();
        g.setColor(lbl.getForeground());
        g.setFont(lbl.getFont());
        td.getIcon().paintIcon(lbl, g, 0, 0);
        g.drawString(td.getText(), 18, height / 2);
        
        
        return image;
    }

    public Rectangle getTabRect(int index, Rectangle destination) {
        if (destination == null) {
            destination = new Rectangle();
        }
        if (index < 0 || index > displayer.getModel().size()) {
            destination.setBounds (0,0,0,0);
            return destination;
        }
        destination.x = layoutModel.getX(index);
        destination.width = layoutModel.getW(index);
        destination.height = layoutModel.getH(index);
        destination.y = Math.min (0, displayer.getHeight() - destination.height);
        return destination;
    }
    
    public int tabForCoordinate(Point p) {
        int max = displayer.getModel().size();
        if (max == 0 || p.y > displayer.getHeight() || p.y < 0 || p.x < 0 || 
            p.x > displayer.getWidth()) {
                
            return -1;
        }
        
        for (int i=0; i < max; i++) {
            int left = layoutModel.getX(i);
            int right = left + layoutModel.getW(i);
            if (p.x > left && p.x < right) {
                return i;
            }
        }
        
        return -1;
    }
    
    protected int createRepaintPolicy () {
        return TabState.REPAINT_SELECTION_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ON_SELECTION_CHANGE
                | TabState.REPAINT_ON_MOUSE_ENTER_TAB
                | TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON
                | TabState.REPAINT_ON_MOUSE_PRESSED;
    }
    
    protected final TabState tabState = new ViewTabState();
    
    private class ViewTabState extends TabState {
        public int getRepaintPolicy(int tab) {
            return createRepaintPolicy();
        }
        
        public void repaintAllTabs() {
            displayer.repaint();
        }
        
        public void repaintTab (int tab) {
            if (tab < 0 || tab >= displayer.getModel().size()) {
                //This can happen because we can be notified
                //of a change on a tab that has just been removed
                //from the model
                return;
            }
            Rectangle r = getTabRect(tab, null);
            displayer.repaint(r);
        }
    }
    
    /**
     * Determine if the tab should be flashing
     */
    protected boolean isAttention (int tab) {
        return (tabState.getState(tab) & TabState.ATTENTION) != 0;
    }
    

    protected void requestAttention (int tab) {
        tabState.addAlarmTab(tab);
    }    
    
    protected void cancelRequestAttention (int tab) {
        tabState.removeAlarmTab(tab);
    }
    
    public String getTooltipForButtons(Point point) {
        if (getController().inPinButtonRect(point)) {
            return NbBundle.getMessage(AbstractViewTabDisplayerUI.class, "AutoHideButton.tooltip");
        }
        if (getController().inCloseIconRect(point) != -1) {
            return NbBundle.getMessage(AbstractViewTabDisplayerUI.class, "CloseButton.tooltip");
        }
        return null;
    }    

    /**
     * Listen to mouse events and handles selection behaviour and close icon
     * button behaviour.
     */
    abstract class Controller extends MouseAdapter
            implements MouseMotionListener, ChangeListener, PropertyChangeListener, ActionListener, ComplexListDataListener {

        //XXX should be able to replace most of this class with 
        //tabState - we're already using it to manage the blinking state
                
        /**
         * index of tab whose close icon currently pressed, -1 otherwise
         */
        // TBD - should be part of model, not controller
        private int closePressed = -1;
        /**
         * index of tab whose close icon active area contains current mouse
         * pointer, false otherwise
         */
        // TBD - should be part of model, not controller
        private int mouseInCloseButton = -1;
        /**
         * true when selection is changed as a result of mouse press
         */
        private boolean selectionChanged;

        /**
         * Subclasses should override this method by detecting if given point is
         * contained in close icon.
         *
         * @return index of tab which close icon area contains given point, -1
         *         if point is outside any close icon area.
         */
        protected abstract int inCloseIconRect(Point point);

        protected abstract boolean inPinButtonRect(Point point);
        
        protected boolean shouldReact(MouseEvent e) {
            boolean isLeft = SwingUtilities.isLeftMouseButton(e);
            return isLeft;
        }

        public void stateChanged (ChangeEvent ce) {
            displayer.repaint();
        }

        public void propertyChange (PropertyChangeEvent pce) {
            if (TabDisplayer.PROP_ACTIVE.equals (pce.getPropertyName())) {
                displayer.repaint();
            }
        }

        /**
         * Performs button action, default impl removes the tab. Subclasses can
         * alter this by overriding.
         */
        protected void performAction(MouseEvent e) {
            if (shouldPerformAction (TabDisplayer.COMMAND_CLOSE, mouseInCloseButton, e)) {
                //In NetBeans winsys, this should never be called - TabbedHandler will
                //consume the event when it is re-propagated from the TabbedContainer
                getDataModel().removeTab(mouseInCloseButton);
            }
        }


        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            int i = getLayoutModel().indexOfPoint(p.x, p.y);
            tabState.setPressed(i);
            SingleSelectionModel sel = getSelectionModel();
            selectionChanged = i != sel.getSelectedIndex();
            // invoke possible selection change
            if ((i != -1) || !selectionChanged) {
                boolean change = shouldPerformAction(TabDisplayer.COMMAND_SELECT,
                    i, e);
                if (change) {
                    getSelectionModel().setSelectedIndex(i);
                    tabState.setSelected(i);
                }
            } 
            // update pressed state
            if (shouldReact(e) && !selectionChanged) {
                setClosePressed(inCloseIconRect(e.getPoint()));
            }
            if ((i != -1) && e.isPopupTrigger()) {
                //Post a popup menu show request
                shouldPerformAction(TabDisplayer.COMMAND_POPUP_REQUEST, i, e);
            }
        }

        public void mouseClicked (MouseEvent e) {
            if (e.getClickCount() >= 2 && !e.isPopupTrigger()) {
                Point p = e.getPoint();
                int i = getLayoutModel().indexOfPoint(p.x, p.y);
                SingleSelectionModel sel = getSelectionModel();
                selectionChanged = i != sel.getSelectedIndex();
                // invoke possible selection change
                if ((i != -1) || !selectionChanged) {
                boolean change = shouldPerformAction(TabDisplayer.COMMAND_SELECT,
                    i, e);
                    if (change) {
                        getSelectionModel().setSelectedIndex(i);
                    }
                }
                if (i != -1) {
                    //Post a maximize request
                    shouldPerformAction(TabDisplayer.COMMAND_MAXIMIZE, i, e);
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            // close button must not be active when selection change was
            // triggered by mouse press
            tabState.setPressed(-1);
            if (shouldReact(e) && !selectionChanged) {
                setClosePressed(-1);
                Point point = e.getPoint();
                if ((mouseInCloseButton = inCloseIconRect(point)) >= 0) {
                    performAction(e);
                    // reset rollover effect after action is complete
                    setMouseInCloseButton(point);
                }
            }
            Point p = e.getPoint();
            int i = getLayoutModel().indexOfPoint(p.x, p.y);
            if ((i != -1) && e.isPopupTrigger()) {
                //Post a popup menu show request
                shouldPerformAction(TabDisplayer.COMMAND_POPUP_REQUEST, i, e);
            }
        }

        public void mouseMoved(MouseEvent e) {
            setMouseInCloseButton(e.getPoint());
        }

        public void mouseDragged(MouseEvent e) {
            setClosePressed(inCloseIconRect(e.getPoint()));
            setMouseInCloseButton(e.getPoint());
        }

        public void mouseExited(MouseEvent e) {
            setMouseInCloseButton(e.getPoint());
        }

        /**
         * @return true if close icon is pressed at the time of calling this
         *         method, false otherwise
         */
        public int isClosePressed() {
            return closePressed;
        }

        /**
         * @return true if mouse pointer is in close icon active area at the
         *         time of calling this method, false otherwise
         */
        public int isMouseInCloseButton() {
            return mouseInCloseButton;
        }
        

        /**
         * Sets state of close button to pressed or released. Updates visual
         * state properly.
         */
        protected void setClosePressed(int pressed) {
            if (closePressed == pressed) {
                return;
            }
            int oldValue = closePressed;
            closePressed = pressed;
            if (closePressed == -1) {
                // press ended
                TabLayoutModel tlm = getLayoutModel();
                getDisplayer().repaint(tlm.getX(oldValue),
                                     tlm.getY(oldValue),
                                     tlm.getW(oldValue),
                                     tlm.getH(oldValue));

            } else if (oldValue == -1) {
                // press started
                TabLayoutModel tlm = getLayoutModel();
                getDisplayer().repaint(tlm.getX(closePressed),
                                     tlm.getY(closePressed),
                                     tlm.getW(closePressed),
                                     tlm.getH(closePressed));
            } else {
                // rare situation, two tabs need repaint, so repaint all
                getDisplayer().repaint();
            }
        }

        /**
         * Sets state of mouse in close button value. Requests repaint of visual
         * state properly.
         */
        protected void setMouseInCloseButton(Point location) {
            int isNow = inCloseIconRect(location);
            if (mouseInCloseButton == isNow || dataModel.size() == 0) {
                return;
            }
            // sync of indexes
            int oldValue = mouseInCloseButton;
            mouseInCloseButton = isNow;
            tabState.setCloseButtonContainsMouse(isNow);
            if (isNow == -1) {
                // exit from close area
                TabLayoutModel tlm = getLayoutModel();
                getDisplayer().repaint(tlm.getX(oldValue),
                                     tlm.getY(oldValue),
                                     tlm.getW(oldValue),
                                     tlm.getH(oldValue));

            } else if (oldValue == -1) {
                // enter into close area
                TabLayoutModel tlm = getLayoutModel();
                getDisplayer().repaint(tlm.getX(isNow), tlm.getY(isNow),
                                     tlm.getW(isNow), tlm.getH(isNow));
            } else {
                // rare situation, two tabs need repaint, so repaint all
                getDisplayer().repaint();
            }
        }
        
        /** Implementation of ActionListener. Reacts to pin button clicks
         */
        public void actionPerformed(ActionEvent e) {
            performPinAction();
        }

        public void indicesAdded(ComplexListDataEvent e) {
            tabState.indicesAdded(e);
        }

        /**
         * Elements have been removed at the indices specified by the event's
         * getIndices() value
         *
         * @param e The event
         */
        public void indicesRemoved(ComplexListDataEvent e) {
            tabState.indicesRemoved(e);
        }

        /**
         * Elements have been changed at the indices specified by the event's
         * getIndices() value.  If the changed data can affect display width (such
         * as a text change or a change in icon size), the event's
         * <code>isTextChanged()</code> method will return true.
         *
         * @param e The event
         */
        public void indicesChanged(ComplexListDataEvent e) {
            tabState.indicesChanged(e);
        }
        
        public void intervalAdded (ListDataEvent evt) {
            tabState.intervalAdded(evt);
        }
        
        public void intervalRemoved (ListDataEvent evt) {
            tabState.intervalRemoved(evt);
        }
        
        public void contentsChanged(ListDataEvent evt) {
            tabState.contentsChanged(evt);
        }
    } // end of Controller
    

    /** Implementation of Pin button, its look is dependent on orientation
     * and can be set using setOrientation method.
     */
    protected static class PinButton extends JButton {
        
        private Map pressedIcons, rolloverIcons, regularIcons;
        
        private Object orientation;
        
        public PinButton (Map regularIcons, Map pressedIcons, Map rolloverIcons) {
            super();
            this.regularIcons = regularIcons;
            this.pressedIcons = pressedIcons;
            this.rolloverIcons = rolloverIcons;
            setFocusable(false);
            setContentAreaFilled(false);
            setRolloverEnabled(rolloverIcons != null);
            setOrientation(TabDisplayer.ORIENTATION_CENTER);
            setToolTipText(NbBundle.getMessage(AbstractViewTabDisplayerUI.class, "AutoHideButton.tooltip"));
        }
        
        public void updateUI() {
            super.updateUI();
            setFocusable(false);
            setContentAreaFilled(false);
            setBorder (BorderFactory.createEmptyBorder());
        }
        
        public Object getOrientation () {
            return orientation;
        }
        
        public void setOrientation (Object orientation) {
            this.orientation = orientation;
            if (orientation != TabDisplayer.ORIENTATION_INVISIBLE) {
                Icon icon = iconCache.obtainIcon((String)regularIcons.get(orientation));
                setIcon(icon);
                setSize(icon.getIconWidth(), icon.getIconHeight());
                if (pressedIcons != null) {
                    setPressedIcon(iconCache.obtainIcon((String)regularIcons.get(orientation)));
                }
                if (rolloverIcons != null) {
                    setRolloverIcon(iconCache.obtainIcon((String)rolloverIcons.get(orientation)));
                }
            } else {
                setIcon(null);
                setPressedIcon(null);
                setSize(0,0);
                setRolloverIcon(null);
            }
        }
        
    } // end of PinButton

    /** Executes enable / disable auto-hide mode */
    private final class PinAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            performPinAction();
        }
    } // end of PinAction
    
    
}
