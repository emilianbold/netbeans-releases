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

package org.netbeans.core.multiview;

import org.openide.util.WeakListeners;
import java.beans.*;
import java.util.*;
import javax.swing.ActionMap;

import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

class MultiViewTopComponentLookup extends Lookup {
    
    private MyProxyLookup proxy;
    private Lookup initial;
    
    public MultiViewTopComponentLookup(Lookup init) {
        super();
        initial = init;
        proxy = new MyProxyLookup(initial);
    }
    
    
    public void setElementLookup(Lookup look) {
        proxy.setElementLookup(look);
    }
    
    public Lookup.Item lookupItem(Lookup.Template template) {

        Lookup.Item retValue;
        if (template.getType() == ActionMap.class) {
            return initial.lookupItem(template);
        }
        // do something here??
        retValue = super.lookupItem(template);
        return retValue;
    }    
    
     
    public Object lookup(Class clazz) {
        if (clazz == ActionMap.class) {
            return initial.lookup(clazz);
        }
        Object retValue;
        
        retValue = proxy.lookup(clazz);
        return retValue;
    }
    
    public Lookup.Result lookup(Lookup.Template template) {
        if (template.getType() == ActionMap.class) {
            return initial.lookup(template);
        }
        Lookup.Result retValue;
        retValue = proxy.lookup(template);
        retValue = new ExclusionResult(retValue);
        return retValue;
    }
    
    /**
     * A lookup result excluding some instances.
     */
    private static final class ExclusionResult extends Lookup.Result implements LookupListener {
        
        private final Lookup.Result delegate;
        private final List listeners = new ArrayList(); // List<LookupListener>
        private Collection lastResults;
        
        public ExclusionResult(Lookup.Result delegate) {
            this.delegate = delegate;
        }
        
        public Collection allInstances() {
            // this shall remove duplicates??
            Set s = new HashSet(delegate.allInstances());
            return s;
        }
        
        public Set allClasses() {
            return delegate.allClasses(); // close enough
        }
        
        public Collection allItems() {
            // remove duplicates..
            Set s = new HashSet(delegate.allItems());
            Iterator it = s.iterator();
            Set instances = new HashSet();
            while (it.hasNext()) {
                Lookup.Item i = (Lookup.Item)it.next();
                if (instances.contains(i.getInstance())) {
                    it.remove();
                } else {
                    instances.add(i.getInstance());
                }
            }
            return s;
        }
        
        public void addLookupListener(LookupListener l) {
            synchronized (listeners) {
                if (listeners.isEmpty()) {
                    if (lastResults == null) {
                        lastResults = allInstances();
                    }
                    delegate.addLookupListener(this);
                }
                listeners.add(l);
            }
        }
        
        public void removeLookupListener(LookupListener l) {
            synchronized (listeners) {
                listeners.remove(l);
                if (listeners.isEmpty()) {
                    delegate.removeLookupListener(this);
                    lastResults = null;
                }
            }
        }
        
        public void resultChanged(LookupEvent ev) {
            synchronized (listeners) {
                Collection current = allInstances();
                boolean equal = lastResults != null && current != null && current.containsAll(lastResults) && lastResults.containsAll(current);
                if (equal) {
                    // the merged list is the same, ignore...
                    return ;
                }
                lastResults = current;
            }
                
            LookupEvent ev2 = new LookupEvent(this);
            LookupListener[] ls;
            synchronized (listeners) {
                ls = (LookupListener[])listeners.toArray(new LookupListener[listeners.size()]);
            }
            for (int i = 0; i < ls.length; i++) {
                ls[i].resultChanged(ev2);
            }
        }
        
    }
    
    private static class MyProxyLookup extends ProxyLookup {
        private Lookup initialLookup;
        public MyProxyLookup(Lookup initial) {
            super(new Lookup[] {initial});
            initialLookup = initial;
        }

        public void setElementLookup(Lookup look) {
            setLookups(new Lookup[] {initialLookup, look});
        }
    }
    
}
