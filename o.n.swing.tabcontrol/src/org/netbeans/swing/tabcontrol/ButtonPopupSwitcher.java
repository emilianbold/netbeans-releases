/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractButton;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import org.netbeans.swing.popupswitcher.SwitcherTable;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;

/**
 * Represents Popup for "Document switching" which is shown after an user clicks
 * the down-arrow button in tabcontrol displayer.
 *
 * @author mkrauskopf
 */
final class ButtonPopupSwitcher
        implements MouseInputListener, AWTEventListener {
    
    /**
     * Reference to the popup object currently showing the default instance, if
     * it is visible
     */
    private static Popup popup;
    
    /**
     * Reference to the focus owner when addNotify was called.  This is the
     * component that received the mouse event, so it's what we need to listen
     * on to update the selected cell as the user drags the mouse
     */
    private Component invokingComponent = null;
    
    /**
     * Time of invocation, used to determine if a mouse release is delayed long
     * enough from a mouse press that it should close the popup, instead of
     * assuming the user wants move-and-click behavior instead of
     * drag-and-click behavior
     */
    private long invocationTime = -1;
    
    /** Indicating whether a popup is shown? */
    private static boolean shown;
    
    private SwitcherTable pTable;
    
    private int x;
    private int y;
    
    /**
     * Creates and shows the popup with given <code>items</code>. When user
     * choose an item <code>SwitcherTableItem.Activable.activate()</code> is
     * called. So what exactly happens depends on the concrete 
     * <code>SwitcherTableItem.Activable</code> implementation. A popup appears
     * on <code>x</code>, <code>y</code> coordinates.
     */
    public static void selectItem(SwitcherTableItem[] items, int x, int y) {
        ButtonPopupSwitcher switcher
                = new ButtonPopupSwitcher(items, x, y);
        switcher.doSelect();
    }
    
    /** Creates a new instance of TabListPanel */
    private ButtonPopupSwitcher(SwitcherTableItem items[],
            int x,
            int y) {
        this.pTable = new SwitcherTable(items, y);
        this.x = x - (int) pTable.getPreferredSize().getWidth();
        this.y = y + 1;
    }
    
    private void doSelect() {
        EventObject eo = EventQueue.getCurrentEvent();
        if (eo != null && eo.getSource() instanceof Component) {
            invokingComponent = (Component) eo.getSource();
            invokingComponent.addMouseListener(this);
            invokingComponent.addMouseMotionListener(this);
        }
        pTable.addMouseListener(this);
        pTable.addMouseMotionListener(this);
        
        Toolkit.getDefaultToolkit().addAWTEventListener(this,
                AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.KEY_EVENT_MASK);
        popup = PopupFactory.getSharedInstance().getPopup(
                invokingComponent, pTable, x, y);
        popup.show();
        shown = true;
        invocationTime = System.currentTimeMillis();
    }
    
    /**
     * Returns true if popup is displayed.
     *
     * @return True if a popup was closed.
     */
    public static boolean isShown() {
        return shown;
    }
    
    /**
     * Clean up listners and hide popup.
     */
    private synchronized void hideCurrentPopup() {
        pTable.removeMouseListener(this);
        pTable.removeMouseMotionListener(this);
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if (invokingComponent != null) {
            invokingComponent.removeMouseListener(this);
            invokingComponent.removeMouseMotionListener(this);
            invokingComponent = null;
        }
        if (popup != null) {
            // Issue 41121 - use invokeLater to allow any pending event
            // processing against the popup contents to run before the popup is
            // hidden
            SwingUtilities.invokeLater(new PopupHider(popup));
            popup = null;
            shown = false;
        }
    }
    
    /**
     * Runnable which hides the popup in a subsequent event queue loop.  This
     * is to avoid problems with BasicToolbarUI, which will try to process
     * events on the component after it has been hidden and throw exceptions.
     *
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=41121
     */
    private class PopupHider implements Runnable {
        private Popup toHide;
        public PopupHider(Popup popup) {
            toHide = popup;
        }
        
        public void run() {
            toHide.hide();
            toHide = null;
        }
    }
    
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        // It may have occured on the button that invoked the tabtable
        if (e.getSource() != this) {
            p = SwingUtilities.convertPoint((Component) e.getSource(), p, pTable);
        }
        if (pTable.contains(p)) {
            int row = pTable.rowAtPoint(p);
            int col = pTable.columnAtPoint(p);
            pTable.changeSelection(row, col, false, false);
        } else {
            pTable.clearSelection();
        }
        e.consume();
    }
    
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        p = SwingUtilities.convertPoint((Component) e.getSource(), p, pTable);
        if (pTable.contains(p)) {
            SwitcherTableItem item = pTable.getSelectedItem();
            if (item != null) {
                item.activate();
                hideCurrentPopup();
                e.consume();
            }
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == invokingComponent) {
            long time = System.currentTimeMillis();
            if (time - invocationTime > 500) {
                mousePressed(e);
            }
        }
        e.consume();
    }
    
    public void mouseClicked(MouseEvent e) {
        e.consume();
    }
    
    public void mouseEntered(MouseEvent e) {
        mouseDragged(e);
        e.consume();
    }
    
    public void mouseExited(MouseEvent e) {
        pTable.clearSelection();
        e.consume();
    }
    
    //MouseMotionListener
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
        e.consume();
    }
    
    /**
     * Was mouse upon the popup table when mouse action had been taken.
     */
    private boolean onSwitcherTable(MouseEvent e) {
        Point p = e.getPoint();
        p = SwingUtilities.convertPoint((Component) e.getSource(), p, pTable);
        return pTable.contains(p);
    }
    
    /**
     * Popup should be closed under some circumstances. Namely when mouse is
     * pressed or released outside of popup or when key is pressed during the
     * time popup is visible.
     */
    public void eventDispatched(AWTEvent event) {
        if (event.getSource() == this) {
            return;
        }
        if (event instanceof MouseEvent) {
            if (event.getID() == MouseEvent.MOUSE_RELEASED) {
                long time = System.currentTimeMillis();
                // check if button was just slowly clicked
                if (time - invocationTime > 500) {
                    if (!onSwitcherTable((MouseEvent) event)) {
                        // Don't take any chances
                        hideCurrentPopup();
                    }
                }
            } else if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                if (!onSwitcherTable((MouseEvent) event)) {
                    // Don't take any chances
                    if (event.getSource() != invokingComponent) {
                        // If it's the invoker, don't do anything - it will
                        // generate another call to invoke(), which will do the
                        // hiding - if we do it here, it will get shown again
                        // when the button processes the event
                        hideCurrentPopup();
                    }
                }
            }
        } else if (event instanceof KeyEvent) {
            if (event.getID() == KeyEvent.KEY_PRESSED) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                hideCurrentPopup();
            }
        }
    }
}
