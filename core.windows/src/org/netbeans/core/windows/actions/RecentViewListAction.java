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


package org.netbeans.core.windows.actions;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.RecentViewListDlg;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

/**
 * Invokes Recent View List
 *
 * @author  Marek Slama
 */
public final class RecentViewListAction extends AbstractAction
implements PropertyChangeListener {
    
    /** Creates a new instance of RecentViewListAction */
    public RecentViewListAction () {
        putValue(NAME, NbBundle.getMessage(RecentViewListAction.class, "CTL_RecentViewListAction"));
        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        updateEnabled();
    }
    
    public void actionPerformed(ActionEvent evt) {
        TopComponent[] tcs = WindowManagerImpl.getInstance().getRecentViewList();
        if (tcs.length == 0) {
            return;
        }

        if(!(evt.getSource() instanceof javax.swing.JMenuItem)) {
        // XXX Show dialog only if the action was invoked by shortcut (not from menu).
            Object accelerator = getValue(ACCELERATOR_KEY);
            KeyStroke keyStroke = accelerator instanceof KeyStroke ? (KeyStroke)accelerator : null;
            
            if(keyStroke != null) {
                int triggerKey = keyStroke.getKeyCode();
                int reverseKey = KeyEvent.VK_SHIFT;
                int releaseKey = 0;

                int modifiers = keyStroke.getModifiers();
                if((InputEvent.CTRL_MASK & modifiers) != 0) {
                    releaseKey = KeyEvent.VK_CONTROL;
                } else if((InputEvent.ALT_MASK & modifiers) != 0) {
                    releaseKey = KeyEvent.VK_ALT;
                } else if((InputEvent.META_MASK & modifiers) != 0) {
                    releaseKey = KeyEvent.META_MASK;
                }
                
                if(releaseKey != 0) {
                    if (!RecentViewListDlg.isShown()) {
                        Frame owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow()
                            instanceof Frame ? 
                            (Frame) KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() 
                            : WindowManager.getDefault().getMainWindow();
                        int sel = tcs.length == 1 ? 0 : 1;
                        RecentViewListDlg.invoke(owner, tcs, sel, triggerKey, reverseKey, releaseKey);
                    }
                    return;
                }
            }
        }

        if(tcs.length > 1) {
            TopComponent tc = tcs[1];
            // #37226 Unmaximized the other mode if needed.
            WindowManagerImpl wm = WindowManagerImpl.getInstance();
            ModeImpl mode = (ModeImpl)wm.findMode(tc);
            if(mode != null && mode != wm.getMaximizedMode()) {
                wm.setMaximizedMode(null);
            }

            tc.requestActive();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }

    /** Only here for fix #41477:, called from layer.xml:
     * For KDE on unixes, Ctrl+TAB is occupied by OS,
     * so we also register Ctrl+BACk_QUOTE as recent view list action shortcut.
     * For other OS's, Ctrl+TAB is the only default, because we create link
     * not pointing to anything by returning null
     */
    public static String getStringRep4Unixes() {
        if (Utilities.isUnix()) {
            return "Actions/Window/org-netbeans-core-windows-actions-RecentViewListAction.instance"; //NOI18N
        }
        return null;
    }
    
    private void updateEnabled() {
        for(Iterator it = WindowManagerImpl.getInstance().getModes().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next();
            if(!mode.getOpenedTopComponents().isEmpty()) {
                setEnabled(true);
                return;
            }
        }
        setEnabled(false);
    }
}

