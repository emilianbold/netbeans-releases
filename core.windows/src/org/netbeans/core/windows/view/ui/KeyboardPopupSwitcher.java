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

package org.netbeans.core.windows.view.ui;

import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.AbstractAction;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.actions.RecentViewListAction;
import org.netbeans.swing.popupswitcher.SwitcherTable;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * Represents Popup for "Keyboard document switching" which is shown after
 * pressing Ctrl+Tab (or alternatively Ctrl+`).
 * If an user releases a <code>releaseKey</code> in <code>TIME_TO_SHOW</code> ms
 * the popup won't show at all. Instead immediate switching will happen.
 *
 * @author mkrauskopf
 */
public final class KeyboardPopupSwitcher implements WindowFocusListener {
    
    /** Number of milliseconds to show popup if interruption didn't happen. */
    private static final int TIME_TO_SHOW = 200;
    
    /** Singleton */
    private static KeyboardPopupSwitcher instance;
    
    /**
     * Reference to the popup object currently showing the default instance, if
     * it is visible
     */
    private static JWindow popup;
    
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
     * shown (without first <code>triggerKey</code> press).
     * If the <code>triggerKey</code> is pressed more than twice the
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
        
    /**
     * Tries to process given <code>KeyEvent</code> and returns true is event
     * was successfully processed/consumed.
     */
    public static boolean processShortcut(KeyEvent kev) {
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        // don't perform when focus is dialog
        if (!wmi.getMainWindow().isFocused() &&
            !wmi.isSeparateWindow(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow())) {
            return false;
        }

        boolean isCtrlTab = kev.getKeyCode() == KeyEvent.VK_TAB &&
                kev.getModifiers() == InputEvent.CTRL_MASK;
        boolean isCtrlShiftTab = kev.getKeyCode() == KeyEvent.VK_TAB &&
                kev.getModifiers() == (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
        if (KeyboardPopupSwitcher.isShown()) {
            assert instance != null;
            instance.processKeyEvent(kev);
            // be sure that events is not processed further when popup is shown
            kev.consume();
            return true;
        }
        if ((isCtrlTab || isCtrlShiftTab)) { // && !KeyboardPopupSwitcher.isShown()
            if (KeyboardPopupSwitcher.isAlive()) {
                KeyboardPopupSwitcher.processInterruption(kev);
            } else {
                AbstractAction rva = new RecentViewListAction();
                rva.actionPerformed(new ActionEvent(kev.getSource(),
                        ActionEvent.ACTION_PERFORMED, "C-TAB", kev.getModifiers()));
                return true;
            }
            // consume all ctrl-(shift)-tab to avoid confusion about
            // Ctrl-Tab events since those events are dedicated to document
            // switching only
            kev.consume();
            return true;
        }
        if (kev.getKeyCode() == KeyEvent.VK_CONTROL && KeyboardPopupSwitcher.isAlive()) {
            KeyboardPopupSwitcher.processInterruption(kev);
            return true;
        }
        return false;
    }
    
    /**
     * Creates and shows the popup with given <code>items</code>. When user
     * selects an item <code>SwitcherTableItem.Activatable.activate()</code> is
     * called. So what exactly happens depends on the concrete
     * <code>SwitcherTableItem.Activatable</code> implementation.
     * Selection is made when user releases a <code>releaseKey</code> passed on
     * as a parameter. If user releases the <code>releaseKey</code> before a
     * specified time (<code>TIME_TO_SHOW</code>) expires the popup won't show
     * at all and switch to the last used document will be performed
     * immediately.
     *
     * A popup appears on <code>x</code>, <code>y</code> coordinates.
     */
    public static void selectItem(SwitcherTableItem items[], int releaseKey,
            int triggerKey, boolean forward) {
        // reject multiple invocations
        if (invokerTimerRunning) {
            return;
        }
        KeyboardPopupSwitcher.items = items;
        KeyboardPopupSwitcher.releaseKey = releaseKey;
        KeyboardPopupSwitcher.triggerKey = triggerKey;
        invokerTimer = new Timer(TIME_TO_SHOW, new PopupInvoker(forward));
        invokerTimer.setRepeats(false);
        invokerTimer.start();
        invokerTimerRunning = true;
    }
    
    /** Stop invoker timer and detach interrupter listener. */
    private static void cleanupInterrupter() {
        invokerTimerRunning = false;
        if (invokerTimer != null) {
            invokerTimer.stop();
        }
    }
    
    /**
     * Serves to <code>invokerTimer</code>. Shows popup after specified time.
     */
    private static class PopupInvoker implements ActionListener {
        private boolean forward;
        public PopupInvoker( boolean forward ) {
            this.forward = forward;
        }
        /** Timer just hit the specified time_to_show */
        public void actionPerformed(ActionEvent e) {
            if (invokerTimerRunning) {
                cleanupInterrupter();
                instance = new KeyboardPopupSwitcher( forward ? hits + 1 : items.length - hits - 1, forward);
                instance.showPopup();
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
     * Indicate whether a popup will be or is shown. <em>Will be</em> means
     * that a popup was already triggered by first Ctrl-Tab but TIME_TO_SHOW
     * wasn't expires yet. <em>Is shown</em> means that a popup is really
     * already shown on the screen.
     */
    private static boolean isAlive() {
        return invokerTimerRunning || shown;
    }
    
    /**
     * Creates a new instance of KeyboardPopupSwitcher with initial selection
     * set to <code>initialSelection</code>.
     */
    private KeyboardPopupSwitcher(int initialSelection, boolean forward) {
        this.fwd = forward;
        pTable = new SwitcherTable(items);
        // Compute coordinates for popup to be displayed in center of screen
        Dimension popupDim = pTable.getPreferredSize();
        Rectangle screen = Utilities.getUsableScreenBounds();
        this.x = screen.x + ((screen.width / 2) - (popupDim.width / 2));
        this.y = screen.y + ((screen.height / 2) - (popupDim.height / 2));
        // Set initial selection if there are at least two items in table
        int cols = pTable.getColumnCount();
        int rows = pTable.getRowCount();
        assert cols > 0 : "There aren't any columns in the KeyboardPopupSwitcher's table"; // NOI18N
        assert rows > 0 : "There aren't any rows in the KeyboardPopupSwitcher's table"; // NOI18N
        changeTableSelection((rows > initialSelection && initialSelection >= 0) ? initialSelection :
            initialSelection, 0);
    }
    
    private void showPopup() {
        if (!isShown()) {
            // set popup to be always on top to be in front of all
            // floating separate windows
            popup = new JWindow();
            popup.setAlwaysOnTop(true);
            popup.getContentPane().add(pTable);
            popup.setLocation(x, y);
            popup.pack();
            popup.setVisible(true);
            // #82743 - on JDK 1.5 popup steals focus from main window for a millisecond,
            // so we have to delay attaching of focus listener
            SwingUtilities.invokeLater(new Runnable() {
                public void run () {
                    WindowManager.getDefault().getMainWindow().
                            addWindowFocusListener( KeyboardPopupSwitcher.this );
                }
            });
            shown = true;
        }
    }
    
    /**
     * Prevents showing a popup if a user releases the <code>releaseKey</code>
     * in time specified by <code>invokerTimer</code> (which is 200ms by
     * default).
     */
    private static void processInterruption(KeyEvent kev) {
        int keyCode = kev.getKeyCode();
        if (keyCode == releaseKey && kev.getID() == KeyEvent.KEY_RELEASED) {
            // if an user releases Ctrl-Tab before the time to show
            // popup expires, don't show the popup at all and switch to
            // the last used document immediately
            cleanupInterrupter();
            hits = 0;
            AbstractAction rva = new RecentViewListAction();
            rva.actionPerformed(new ActionEvent(kev.getSource(),
                    ActionEvent.ACTION_PERFORMED,
                    "immediately")); // NOI18N
            kev.consume();
        // #88931: Need to react to KEY_PRESSED, not KEY_RELEASED, to not miss the hit    
        } else if (keyCode == triggerKey
                && kev.getModifiers() == InputEvent.CTRL_MASK
                && kev.getID() == KeyEvent.KEY_PRESSED) {
            // count number of trigger key hits before popup is shown
            hits++;
            kev.consume();
            cleanupInterrupter();
            instance = new KeyboardPopupSwitcher(hits + 1, true);
            instance.showPopup();
        }
    }
    
    /** Handles given <code>KeyEvent</code>. */
    private void processKeyEvent(KeyEvent kev) {
        switch (kev.getID()) {
            case KeyEvent.KEY_PRESSED:
                int code = kev.getKeyCode();
                if (code == reverseKey) {
                    fwd = false;
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
                }
                kev.consume();
                break;
            case KeyEvent.KEY_RELEASED:
				code = kev.getKeyCode();
                if (code == reverseKey) {
                    fwd = true;
                    kev.consume();
                } else if (code == KeyEvent.VK_ESCAPE) { // XXX see above
                    cancelSwitching();
                } else if (code == releaseKey) {
                    performSwitching();
                }
                break;
                }
        }
    
    /** Changes table selection and sets status bar appropriately */
    private void changeTableSelection(int row, int col) {
        pTable.changeSelection(row, col, false, false);
        // #95111: Defense agaist random selection failure
        SwitcherTableItem item = pTable.getSelectedItem();
        if (item != null) {
            String statusText = item.getDescription();
            StatusDisplayer.getDefault().setStatusText(
                    statusText != null ? statusText : "");
        }
    }
    
    /**
     * Cancels the popup if present, causing it to close without the active
     * document being changed.
     */
    private void cancelSwitching() {
        hideCurrentPopup();
        StatusDisplayer.getDefault().setStatusText("");
    }
    
    /** Switch to the currently selected document and close the popup. */
    private void performSwitching() {
        if (popup != null) {
            // #90007: selection may be null if mouse is involved
            SwitcherTableItem item = pTable.getSelectedItem();
            if (item != null) {
                item.activate();
            }
        }
        cancelSwitching();
    }
    
    private synchronized void hideCurrentPopup() {
        if (popup != null) {
            // Issue 41121 - use invokeLater to allow any pending ev
            // processing against the popup contents to run before the popup is
            // hidden
            SwingUtilities.invokeLater(new PopupHider(popup));
        }
    }

    public void windowGainedFocus(WindowEvent e) {
    }

    public void windowLostFocus(WindowEvent e) {
        //remove the switcher when the main window is deactivated, 
        //e.g. user pressed Ctrl+Esc on MS Windows which opens the Start menu
        if (e.getOppositeWindow() != popup) {
            cancelSwitching();
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
        private JWindow toHide;
        public PopupHider(JWindow popup) {
            toHide = popup;
        }
        
        public void run() {
            toHide.setVisible(false);
            shown = false;
            hits = 0;
            // part of #82743 fix
            WindowManager.getDefault().getMainWindow().removeWindowFocusListener( KeyboardPopupSwitcher.this );
        }
    }
}
