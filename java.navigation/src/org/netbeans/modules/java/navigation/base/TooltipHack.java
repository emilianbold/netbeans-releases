/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.navigation.base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

/** 
 * Hack to invoke tooltip on given component on given position immediatelly
 * at a request.
 *
 * XXX - hack is not reliable, could stop working in future JDK releases.
 * Navigator should better handle tooltips totally itself,
 * without Swing TooltipManager, to get rid of such hacks.
 *
 * @author Dafe Simonek
 */
public final class TooltipHack implements ActionListener {

    private static TooltipHack instance;
    
    /** holds previous dismiss tooltip value */
    private static int prevDismiss = -1;
    
    private TooltipHack() {
    }

    /** Hack to invoke tooltip on given JComponent, with given dismiss delay.
     * Triggers <br>
     * <code>comp.getToolTipText(MouseEvent)</code> and 
     * <code>comp.getToolTipLocation(MouseEvent)</code> with fake mousemoved 
     * MouseEvent, set to given coordinates.
     */
    public static void invokeTip (JComponent comp, int x, int y, int dismissDelay) {
        final ToolTipManager ttm = ToolTipManager.sharedInstance();
        final int prevInit = ttm.getInitialDelay();
        prevDismiss = ttm.getDismissDelay();
        ttm.setInitialDelay(0);
        ttm.setDismissDelay(dismissDelay);
        
        MouseEvent fakeEvt = new MouseEvent(
                comp, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 
                0, x, y, 0, false);
        ttm.mouseMoved(fakeEvt);
        
        ttm.setInitialDelay(prevInit);
        Timer timer = new Timer(20, instance());
        timer.setRepeats(false);
        timer.start();
    }
    
    /** impl of ActionListener, reacts on timer and restores Dismiss value.
     * Don't call from outside classes.
     */
    public void actionPerformed(ActionEvent e) {
        if (prevDismiss > 0) {
            ToolTipManager.sharedInstance().setDismissDelay(prevDismiss);
            prevDismiss = -1;
        }
    }
    
    private static TooltipHack instance () {
        if (instance == null) {
            instance = new TooltipHack();
        }
        return instance;
    }
    
}
