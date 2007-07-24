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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabDisplayerUI;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.openide.windows.TopComponent;

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
    
    private Component controlButtons;

    protected Controller controller;
    
    private TabControlButton btnClose;
    private TabControlButton btnAutoHidePin;
    private TabControlButton btnMaximizeRestore;
    
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
        installControlButtons();
        dataModel.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                showHidePinButton();
                if( null != dataModel )
                    dataModel.removeChangeListener( this );
            }
        });
    }
    
    void showHidePinButton() {
        Component tabComponent = null;
        int selIndex = Math.max( 0, displayer.getSelectionModel().getSelectedIndex() );
        if( selIndex >= 0 && selIndex < displayer.getModel().size() ) {
            TabData tab = displayer.getModel().getTab( selIndex );
            tabComponent = tab.getComponent();
        }
        btnAutoHidePin.setVisible( tabComponent != null 
                && !TabDisplayer.ORIENTATION_INVISIBLE.equals( displayer.getWinsysInfo().getOrientation( tabComponent ) ) );
    }
    
    protected void installControlButtons() {
        if( null != getControlButtons() )
            displayer.add( getControlButtons() );
    }
    
    private static final int ICON_X_PAD = 1;
    
    /**
     * @return A component that holds all control buttons (maximize/restor, 
     * slide/pin, close) that are displayed in the active tab or null if
     * control buttons are not supported.
     */
    protected Component getControlButtons() {
        if( null == controlButtons ) {
            JPanel buttonsPanel = new JPanel( null );
            buttonsPanel.setOpaque( false );

            int width = 0;
            int height = 0;
            //create maximize/restore button
//            if( null != displayer.getWinsysInfo() ) {
//                btnMaximizeRestore = TabControlButtonFactory.createMaximizeRestoreButton( displayer );
//                buttonsPanel.add( btnMaximizeRestore );
//                Icon icon = btnMaximizeRestore.getIcon();
//                btnMaximizeRestore.setBounds( 0, 0, icon.getIconWidth(), icon.getIconHeight() );
//                width += icon.getIconWidth();
//            }

            //create autohide/pin button
            if( null != displayer.getWinsysInfo() ) {
                btnAutoHidePin = TabControlButtonFactory.createSlidePinButton( displayer );
                buttonsPanel.add( btnAutoHidePin );

                Icon icon = btnAutoHidePin.getIcon();
                if( 0 != width )
                    width += ICON_X_PAD;
                btnAutoHidePin.setBounds( width, 0, icon.getIconWidth(), icon.getIconHeight() );
                width += icon.getIconWidth();
            }

            //create close button
            btnClose = TabControlButtonFactory.createCloseButton( displayer );
            buttonsPanel.add( btnClose );

            Icon icon = btnClose.getIcon();
            if( 0 != width )
                width += ICON_X_PAD;
            btnClose.setBounds( width, 0, icon.getIconWidth(), icon.getIconHeight() );
            width += icon.getIconWidth();
            height = icon.getIconHeight();
            
            Dimension size = new Dimension( width, height );
            buttonsPanel.setMinimumSize( size );
            buttonsPanel.setSize( size );
            buttonsPanel.setPreferredSize( size );
            buttonsPanel.setMaximumSize( size );
            
            controlButtons = buttonsPanel;
        }
        return controlButtons;
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
        if (controlButtons != null) {
            displayer.remove(controlButtons);
            controlButtons = null;
        }
        layoutModel = null;
        selectionModel = null;
        dataModel = null;
        controller = null;
    }

    protected Controller createController() {
        return new Controller();
    }

    public void paint(Graphics g, JComponent c) {

        ColorUtil.setupAntialiasing(g);

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
	public ViewTabState () {}
	
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

    /**
     * Listen to mouse events and handles selection behaviour and close icon
     * button behaviour.
     */
    protected class Controller extends MouseAdapter
            implements MouseMotionListener, ChangeListener, PropertyChangeListener, ComplexListDataListener {

        /**
         * true when selection is changed as a result of mouse press
         */
        private boolean selectionChanged;

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
         * @param p Mouse point location
         * @return True if the point is in the control buttons panel.
         */
        public boolean inControlButtonsRect( Point p ) {
            if( null != controlButtons ) {
                Point p2 = SwingUtilities.convertPoint(displayer, p, controlButtons);
                return controlButtons.contains(p2);
            }
            return false;
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
                    Component tc = getDataModel().getTab(i).getComponent();
                    if( null != tc && tc instanceof TopComponent
                        && !((TopComponent)tc).isAncestorOf( KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner() ) ) {
                        ((TopComponent)tc).requestActive();
                    }
                }
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
            Point p = e.getPoint();
            int i = getLayoutModel().indexOfPoint(p.x, p.y);
            if ((i != -1) && e.isPopupTrigger()) {
                //Post a popup menu show request
                shouldPerformAction(TabDisplayer.COMMAND_POPUP_REQUEST, i, e);
            }
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

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
        }
    } // end of Controller
    
    private class PinAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if( null != btnAutoHidePin ) {
                btnAutoHidePin.performAction( null );
            }
        }
    }
}
