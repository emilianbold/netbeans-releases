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


package org.netbeans.core.windows;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Instance of this class keeps list of (weak references to) recently activated TopComponents.
 *
 * @author  Marek Slama
 */
final class RecentViewList implements PropertyChangeListener {
    
    private static RecentViewList instance;
    
    /** List of TopComponents (weak references are used). First is most recently
     * activated. */
    private List tcWeakList = new ArrayList(20);
    
    public RecentViewList (WindowManager wm) {
        // Starts listening on Registry to be notified about activated TopComponent.
        wm.getRegistry().addPropertyChangeListener(this);
    }

    
    /** Used to get array for view and for persistence */
    public TopComponent [] getTopComponents() {
        List tcList = new ArrayList(tcWeakList.size());
        clean();
        for (int i = 0; i < tcWeakList.size(); i++) {
            WeakReference w = (WeakReference) tcWeakList.get(i);
            TopComponent tc = (TopComponent) w.get();
            if ((tc != null) && tc.isOpened()) {
                tcList.add(tc);
            }
        }
        return (TopComponent []) tcList.toArray(new TopComponent[tcList.size()]);
    }
    
    /** Used to set initial values from persistence */
    public void setTopComponents(TopComponent [] tcs) {
        tcWeakList.clear();
        for (int i = 0; i < tcs.length; i++) {
            WeakReference wr = new WeakReference(tcs[i]);
            tcWeakList.add(wr);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            
            TopComponent tc = (TopComponent) evt.getNewValue();
            if (tc != null) {
                //Update list
                clean();
                WeakReference w = find(tc);
                if (w != null) {
                    //Rearrange, put to first place
                    tcWeakList.remove(w);
                    tcWeakList.add(0,w);
                } else {
                    WeakReference wr = new WeakReference(tc);
                    tcWeakList.add(0,wr);
                }
            }
        }
    }
    
    /** Clean gc'ed TopComponents from list */
    private void clean () {
        int i = 0;
        while (i < tcWeakList.size()) {
            WeakReference w = (WeakReference) tcWeakList.get(i);
            TopComponent tc = (TopComponent) w.get();
            //TopComponent was gc'ed
            if (tc == null) {
                tcWeakList.remove(w);
            } else {
                i++;
            }
        }
    }
    
    /** Returns weak reference to given TopComponent if present.
     * Otherwise returns null. */
    private WeakReference find (TopComponent tc) {
        for (int i = 0; i < tcWeakList.size(); i++) {
            WeakReference w = (WeakReference) tcWeakList.get(i);
            TopComponent c = (TopComponent) w.get();
            if (tc == c) {
                return w;
            }
        }
        return null;
    }
    
}
