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

package org.apache.tools.ant.module.nodes;

import java.io.IOException;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.event.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.openide.ErrorManager;

final class AntProjectChildren extends Children.Keys/*<TargetLister.Target>*/ implements ChangeListener, Comparator/*<TargetLister.Target>*/ {
    
    private static Collator SORTER = Collator.getInstance();
    
    private final AntProjectCookie cookie;
    private SortedSet/*<TargetLister.Target>*/ allTargets;
    
    public AntProjectChildren (AntProjectCookie cookie) {
        super ();
        this.cookie = cookie;
    }
    
    protected void addNotify () {
        super.addNotify ();
        refreshKeys(true);
        cookie.addChangeListener (this);
    }

    protected void removeNotify () {
        super.removeNotify ();
        setKeys(Collections.EMPTY_SET);
        synchronized (this) {
            allTargets = null;
        }
        cookie.removeChangeListener (this);
    }

    private void refreshKeys(boolean createKeys) {
        try {
            Set/*<TargetLister.Target>*/ _allTargets = TargetLister.getTargets(cookie);
            synchronized (this) {
                if (allTargets == null && !createKeys) {
                    // Aynch refresh after removeNotify; ignore. (#44428)
                    return;
                }
                allTargets = new TreeSet(this);
                allTargets.addAll(_allTargets);
                Iterator it = allTargets.iterator();
                while (it.hasNext()) {
                    TargetLister.Target t = (TargetLister.Target) it.next();
                    if (t.isOverridden()) {
                        // Don't include these.
                        it.remove();
                    }
                }
            }
            setKeys(allTargets);
        } catch (IOException e) {
            // XXX should mark the project node as being somehow in error
            AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            setKeys(Collections.EMPTY_SET);
        }
    }
    
    protected Node[] createNodes (Object key) {
        TargetLister.Target t = (TargetLister.Target) key;
        return new Node[] {new AntTargetNode(cookie, t, allTargets)};
    }
    
    public void stateChanged (ChangeEvent ev) {
        refreshKeys(false);
    }
    
    public int compare(Object o1, Object o2) {
        TargetLister.Target t1 = (TargetLister.Target) o1;
        TargetLister.Target t2 = (TargetLister.Target) o2;
        int x = SORTER.compare(t1.getName(), t2.getName());
        if (x != 0 || t1 == t2) {
            return x;
        } else {
            // #44491: was not displaying overridden targets.
            return System.identityHashCode(t1) - System.identityHashCode(t2);
        }
    }
    
}
