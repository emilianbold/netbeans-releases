/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import javax.swing.ActionMap;
import junit.framework.Assert;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Utilities for actions tests.
 * @author Jesse Glick
 */
public class ActionsInfraHid implements ContextGlobalProvider {
    
    public ActionsInfraHid() {}

    private static final ActionMap EMPTY_MAP = new ActionMap();
    private static ActionMap currentMap = EMPTY_MAP;

    private static final AMLookup amLookup = new AMLookup();
    
    public Lookup createGlobalContext() {
        return amLookup;
    }

    private static Lookup.Result amResult;
    static {
        try {
            amResult = Utilities.actionsGlobalContext().lookupResult(ActionMap.class);
            Assert.assertEquals(Collections.singleton(EMPTY_MAP), new HashSet(amResult.allInstances()));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void setActionMap(ActionMap newMap) {
        if (newMap == null) {
            newMap = EMPTY_MAP;
        }
        currentMap = newMap;
        amLookup.refresh();
        Assert.assertEquals(Collections.singleton(currentMap), new HashSet(amResult.allInstances()));
    }
    
    private static final class AMLookup extends ProxyLookup {
        public AMLookup() {
            refresh();
        }
        public void refresh() {
            //System.err.println("AM.refresh; currentMap = " + currentMap);
            setLookups(new Lookup[] {
                Lookups.singleton(currentMap),
            });
        }
    }
    
    /** Prop listener that will tell you if it gets a change.
     */
    public static final class WaitPCL implements PropertyChangeListener {
        /** whether a change has been received, and if so count */
        public int gotit = 0;
        /** optional property name to filter by (if null, accept any) */
        private final String prop;
        public WaitPCL(String p) {
            prop = p;
        }
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            if (prop == null || prop.equals(evt.getPropertyName())) {
                gotit++;
                notifyAll();
            }
        }
        public boolean changed() {
            return changed(1500);
        }
        public synchronized boolean changed(int timeout) {
            if (gotit > 0) {
                return true;
            }
            try {
                wait(timeout);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return gotit > 0;
        }
    }
    
    // Stolen from RequestProcessorTest.
    public static void doGC() {
        doGC(10);
    }
    public static void doGC(int count) {
        ArrayList l = new ArrayList(count);
        while (count-- > 0) {
            System.gc();
            System.runFinalization();
            l.add(new byte[1000]);
        }
    }

}
