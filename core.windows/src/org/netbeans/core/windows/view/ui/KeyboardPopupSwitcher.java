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

package org.netbeans.core.windows.view.ui;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.core.windows.actions.RecentViewListAction;
import org.netbeans.swing.popupswitcher.SwitcherTable;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Utilities;

/**
 * Represents Popup for "Keyboard document switching" which is shown after
 * pressing Ctrl+Tab (or alternatively Ctrl+`).
 * If an user releases a <code>releaseKey</code> in <code>TIME_TO_SHOW</code> ms
 * the popup won't show at all. Instead immediate switching will happen.
 *
 * @author mkrauskopf
 */
public final class KeyboardPopupSwitcher implements AWTEventListener {
    
    /** Number of milliseconds to show popup if interruption didn't happen. */
    private static final int TIME_TO_SHOW = 200;
    
    /**
     * Reference to the popup object currently showing the default instance, if
     * it is visible
     */
    private static Popup popup;
    
    /** Indicating whether a popup is shown? */
    private static boolean shown;
    
    /**
     * Invoke popup after a specified time. Can be interrupter if an user
     * releases <code>triggerKey</code> key in that time.
     */
    private static Timer invokerTimer;
    
    /**
     * Safely indicating whether a <code>invokerTimer</code> is running or not.
     * isRunning() method doesn't work for us in all cases.
     */
    private static boolean invokerTimerRunning;
    
    /**
     * Counts the number of <code>triggerKey</code> hits before the popup is
     * shown. If the <code>triggerKey</code> is pressed more than twice the
     * popup will be shown immediately.
     */
    private static int hits;
    
    /**
     * Current items to be shown in a popup. It is <code>static</code>, since
     * there can be only one popup list at time.
     */
    private static SwitcherTableItem[] items;
    
    private SwitcherTable pTable;
    
    private static int triggerKey; // e.g. TAB
    private static int reverseKey = KeyEvent.VK_SHIFT;
    private static int releaseKey; // e.g. CTRL
    
    private int x;
    private int y;
    
    /** Indicates whether an item to be selected is previous or next one. */
    private boolean fwd = true;
    
    private static AWTEventListener interruper = new Interrupter();
    
    /**
     * Creates and shows the popup with given <code>items</code>. When user
     * selects an item <code>SwitcherTableItem.Activable.activate()</code> is
     * called. So what exactly happens depends on the concrete
     * <code>SwitcherTableItem.Activable</code> implementation.
     * Selection is made when user releases a <code>releaseKey</code> passed on
     * as a parameter. If user releases the <code>releaseKey</code> before a
     * specified time (<code>TIME_TO_SHOW</code>) expires the popup won't show
     * at all and switch to the last used document will be performed
     * immediately.
     *
     * A popup appears on <code>x</code>, <code>y</code> coordinates.
     */
    public static void selectItem(SwitcherTableItem items[], int releaseKey,
            int triggerKey) {
        // reject multiple invocations
        if (invokerTimerRunning) {
            return;
        }
        KeyboardPopupSwitcher.items = items;
        KeyboardPopupSwitcher.releaseKey = releaseKey;
        KeyboardPopupSwitcher.triggerKey = triggerKey;
        attachInterrupter();
        invokerTimer = new Timer(TIME_TO_SHOW, new PopupInvoker());
        invokerTimer.setRepeats(false);
        invokerTimer.start();
        invokerTimerRunning = true;
    }
    
    /**
     * Prevents showing a popup if an user relelase the <code>releaseKey</code>
     * in time specified by <code>invokerTimer</code> (which is 200ms by
     * default).
     */
    private static class Interrupter implements AWTEventListener {
        public void eventDispatched(AWTEvent ev) {
            if (ev.getID() != KeyEvent.KEY_RELEASED && invokerTimerRunning) {
                return;
            }
            KeyEvent kev = ((KeyEvent) ev);
            int keyCode = kev.getKeyCode();
            if (keyCode == releaseKey) {
                // if an user releases Ctrl-Tab before the time to show
                // popup expires, don't show the popup at all and switch to
                // the last used document immediately
                cleanupInterrupter();
                hits = 0;
                AbstractAction rva = new RecentViewListAction();
                rva.actionPerformed(new ActionEvent(ev.getSource(),
                        ActionEvent.ACTION_PERFORMED,
                        "immediately")); // NOI18N
                kev.consume();
            } else if (keyCode == triggerKey
                    && kev.getModifiers() == InputEvent.CTRL_MASK) {
                // count number of trigger key hits before popup is shown
                hits++;
                kev.consume();
                if (hits > 1) {
                    cleanupInterrupter();
                    KeyboardPopupSwitcher switcher = new KeyboardPopupSwitcher(hits);
                    switcher.showPopup();
                }
            }
        }
    }
    
    /**
     * Prevent attaching of more than one <code>interruper</code>
     * AWTEventListener.
     */
    private static boolean isIntAttached;
    
    private static void attachInterrupter() {
        if (!isIntAttached) {
            Toolkit.getDefaultToolkit().addAWTEventListener(interruper,
                    AWTEvent.KEY_EVENT_MASK);
            isIntAttached = true;
        }
    }
    
    /** Stop invoker timer and dettach interrupter listener. */
    private static void cleanupInterrupter() {
        invokerTimerRunning = false;
        if (invokerTimer != null) {
            invokerTimer.stop();
        }
        if (isIntAttached) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(interruper);
            isIntAttached = false;
        }
    }
    
    /**
     * Serves to <code>invokerTimer</code>. Shows popup after specified time.
     */
    private static class PopupInvoker implements ActionListener {
        /** Timer just hit the specified time_to_show */
        public void actionPerformed(ActionEvent e) {
            if (invokerTimerRunning) {
                cleanupInterrupter();
                KeyboardPopupSwitcher switcher = new KeyboardPopupSwitcher(hits);
                switcher.showPopup();
            }
        }
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
     * Creates a new instance of KeyboardPopupSwitcher with initial selection
     * set to <code>initialSelection</code>.
     */
    private KeyboardPopupSwitcher(int initialSelection) {
        pTable = new SwitcherTable(items);
        // Compute coordinates for popup to be displayed in center of screen
        Dimension popupDim = pTable.getPreferredSize();
        Rectangle screen = Utilities.getUsableScreenBounds();
        this.x = screen.x + ((screen.width / 2) - (popupDim.width / 2));
        this.y = screen.x + ((screen.height / 2) - (popupDim.height / 2));
        // Set initial selection if there are at least two items in table
        int cols = pTable.getColumnCount();
        int rows = pTable.getRowCount();
        assert cols > 0 : "There aren't any columns in the KeyboardPopupSwitcher's table"; // NOI18N
        assert rows > 0 : "There aren't any rows in the KeyboardPopupSwitcher's table"; // NOI18N
        changeTableSelection((rows > initialSelection) ? initialSelection :
            initialSelection, 0);
    }
    
    private void showPopup() {
        if (!isShown()) {
            Toolkit.getDefaultToolkit().addAWTEventListener(this,
                    AWTEvent.KEY_EVENT_MASK);
            popup = PopupFactory.getSharedInstance().getPopup(null, pTable, x, y);
            popup.show();
            shown = true;
        }
    }
    
    // XXX Have a look how is the key events handled in JPopupMenu+its UI,
    // There should be used probably another mechanism then AWTEventListener
    // -> input and action map, if possible.
    public void eventDispatched(AWTEvent ev) {
        int code;
        switch (ev.getID()) {
            case KeyEvent.KEY_RELEASED:
                code = ((KeyEvent) ev).getKeyCode();
                if (code == reverseKey) {
                    fwd = true;
                } else if (code == triggerKey) {
                    int lastRowIdx = pTable.getRowCount() - 1;
                    int lastColIdx = pTable.getColumnCount() - 1;
                    int selRow = pTable.getSelectedRow();
                    int selCol = pTable.getSelectedColumn();
                    int row = selRow;
                    int col = selCol;
                    
                    // MK initial alg.
                    if (fwd) {
                        if (selRow >= lastRowIdx) {
                            row = 0;
                            col = (selCol >= lastColIdx ? 0 : ++col);
                        } else {
                            row++;
                            if (pTable.getValueAt(row, col) == null) {
                                row = 0;
                                col = 0;
                            }
                        }
                    } else {
                        if (selRow == 0) {
                            if (selCol == 0) {
                                col = lastColIdx;
                                row = pTable.getLastValidRow();
                            } else {
                                col--;
                                row = lastRowIdx;
                            }
                        } else {
                            row--;
                        }
                    }
                    if (row >= 0 && col >= 0) {
                        changeTableSelection(row, col);
                    }
                } else if (code == KeyEvent.VK_ESCAPE) { // XXX see above
                    cancelSwiching();
                } else if (code == releaseKey) {
                    performSwitching();
                }
                ((KeyEvent) ev).consume();
                break;
            case KeyEvent.KEY_PRESSED:
                if (((KeyEvent) ev).getKeyCode() == reverseKey) {
                    fwd = false;
                }
                break;
        }
    }
    
    /** Changes table selection and sets status bar appropriately */
    private void changeTableSelection(int row, int col) {
        pTable.changeSelection(row, col, false, false);
        String statusText = pTable.getSelectedItem().getDescription();
        StatusDisplayer.getDefault().setStatusText(
                statusText != null ? statusText : "");
    }
    
    /**
     * Cancels the popup if present, causing it to close without the active
     * document being changed.
     */
    private void cancelSwiching() {
        hideCurrentPopup();
        StatusDisplayer.getDefault().setStatusText("");
    }
    
    /** Switch to the currently selected document and close the popup. */
    private void performSwitching() {
        if (popup != null) {
            pTable.getSelectedItem().activate();
        }
        cancelSwiching();
    }
    
    private synchronized void hideCurrentPopup() {
        if (popup != null) {
            // Issue 41121 - use invokeLater to allow any pending ev
            // processing against the popup contents to run before the popup is
            // hidden
            SwingUtilities.invokeLater(new PopupHider(popup));
        }
    }
    
    /**
     * Runnable which hides the popup in a subsequent ev queue loop. This is
     * to avoid problems with BasicToolbarUI, which will try to process events
     * on the component after it has been hidden and throw exceptions.
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
            shown = false;
            hits = 0;
            Toolkit.getDefaultToolkit().removeAWTEventListener(KeyboardPopupSwitcher.this);
        }
    }
}
