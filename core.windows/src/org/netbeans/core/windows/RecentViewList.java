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


package org.netbeans.core.windows;

import java.util.Iterator;
import java.util.Set;
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
                // #69486: ensure all components are listed
                fillList(TopComponent.getRegistry().getOpened());
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

    /** Fills list of weak references with TCs that are in given
     * input list but are not yet contained in list of weak references.
     */ 
    private void fillList(Set openedTCs) {
        TopComponent curTC;
        WeakReference wr;
        for (Iterator it = openedTCs.iterator(); it.hasNext();) {
            curTC = (TopComponent) it.next();
            if (find(curTC) == null) {
                if (tcWeakList.size() > 1) {
                    wr = new WeakReference(curTC);
                    tcWeakList.add(1,wr);
                } else {
                    wr = new WeakReference(curTC);
                    tcWeakList.add(wr);
                }
            }
        }
    }
    
}
