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

package org.openide.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Vector;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.ObjectChangeListener;
import org.netbeans.junit.NbTestCase;

public class WeakListenerTest extends NbTestCase {
    
    public WeakListenerTest(String testName) {
        super(testName);
    }
    
    public void testPrivateRemoveMethod() throws Exception {
        PropChBean bean = new PropChBean();
        Listener listener = new Listener();
        PropertyChangeListener weakL = new PrivatePropL(listener, bean);
        WeakReference ref = new WeakReference(listener);
        
        bean.addPCL(weakL);
        
        listener = null;
        assertGC("Listener wasn't GCed", ref);
        
        ref = new WeakReference(weakL);
        weakL = null;
        assertGC("WeakListener wasn't GCed", ref);
    }
    
    private static final class Listener
            implements PropertyChangeListener, ObjectChangeListener {
        public int cnt;
        
        public void propertyChange(PropertyChangeEvent ev) {
            cnt++;
        }
        
        public void namingExceptionThrown(NamingExceptionEvent evt) {
            cnt++;
        }
        
        public void objectChanged(NamingEvent evt) {
            cnt++;
        }
    } // end of Listener
    
    private static class PropChBean {
        private Vector listeners = new Vector();
        private void addPCL(PropertyChangeListener l) { listeners.add(l); }
        private void removePCL(PropertyChangeListener l) { listeners.remove(l); }
    } // End of PropChBean class
    
    private static class PrivatePropL extends WeakListener implements PropertyChangeListener {
        
        public PrivatePropL(PropertyChangeListener orig, Object source) {
            super(PropertyChangeListener.class, orig);
            setSource(source);
        }
        
        protected String removeMethodName() {
            return "removePCL"; // NOI18N
        }
        
        // ---- PropertyChangeListener implementation
        
        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeListener l = (PropertyChangeListener) super.get(evt);
            if (l != null) l.propertyChange(evt);
        }
    } // End of PrivatePropL class
}
