/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.execution;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

/**
*
* @author Ales Novak
*/
final class WindowTable extends HashMap<Window,TaskThreadGroup> {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1494996298725028533L;

    /** window listener */
    private WindowListener winListener;

    /** maps ThreadGroup:ArrayList, ArrayList keeps windows */
    private HashMap<ThreadGroup,ArrayList<Window>> windowMap;

    /**
    *default constructor
    */
    public WindowTable () {
        super(16);
        windowMap = new HashMap<ThreadGroup,ArrayList<Window>>(16);
        winListener = new WindowAdapter() {
                          public void windowClosed(WindowEvent ev) {
                              Window win;
                              removeWindow(win = (Window)ev.getSource());
                              win.removeWindowListener(this);
                          }
                      };
    }

    public synchronized void putTaskWindow(Window win, TaskThreadGroup grp) {
        ArrayList<Window> vec;
        if ((vec = windowMap.get(grp)) == null) {
            vec = new ArrayList<Window>();
            windowMap.put(grp, vec);
        }
        vec.add(win);
        win.addWindowListener(winListener);
        super.put(win, grp);
    }

    public TaskThreadGroup getThreadGroup(Window win) {
        return super.get(win);
    }

    /** closes windows opened by grp ThreadGroup */
    void closeGroup(ThreadGroup grp) {
        Window win;
        ArrayList<Window> vec = windowMap.get(grp);
        if (vec == null) return;
        Iterator<Window> ee = vec.iterator();
        while (ee.hasNext()) {
            (win = ee.next()).setVisible(false);
            remove(win);
            if (win != getSharedOwnerFrame()) {
                win.dispose();
            }
        }
        windowMap.remove(grp);
    }
    
    // XXX todo nasty hack into Swing
    private static java.awt.Frame shOwnerFrame;
    private static java.awt.Frame getSharedOwnerFrame() {
        if (shOwnerFrame != null) {
            return shOwnerFrame;
        }
        
        try {
            Class swUtil = Class.forName("javax.swing.SwingUtilities"); // NOI18N
            java.lang.reflect.Method getter = swUtil.getDeclaredMethod("getSharedOwnerFrame", new Class[] {}); // NOI18N
            getter.setAccessible(true);
            
            shOwnerFrame = (java.awt.Frame) getter.invoke(null, new Object[] {});
        } catch (Exception e) {
            // do nothing
        }
        
        return shOwnerFrame;
    }

    /** return true if the ThreadGroup has any windows */
    boolean hasWindows(ThreadGroup grp) {
        ArrayList<Window> vec = windowMap.get(grp);
        if ((vec == null) || (vec.size() == 0) || hiddenWindows(vec)) {
            return false;
        }
        return true;
    }

    /**
    * @param vec is a ArrayList of windows
    * @param grp is a ThreadGroup that belongs to the ArrayList
    * @return true if all windows in the ArrayList vec are invisible
    */
    private boolean hiddenWindows(ArrayList<Window> vec) {
        Iterator<Window> ee = vec.iterator();
        Window win;
        while (ee.hasNext()) {
            win = ee.next();
            if (win.isVisible()) return false;
        }
        // windows will be removed later
        return true;
    }

    /** removes given window */
    private void removeWindow(Window win) {
        Object obj = get(win); // obj is threadgroup
        if (obj == null) return;
        remove(win);
        ArrayList<Window> vec = windowMap.get(obj);
        if (vec == null) return;
        vec.remove(win);
    }
}
