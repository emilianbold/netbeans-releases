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

package org.netbeans.lib.editor.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.io.NbMarshalledObject;


/**
 * Test of ListenerList correctness.
 *
 * @author mmetelka
 */
public class ListenerListTest extends NbTestCase {
    
    private static final boolean debug = false;
    
    public ListenerListTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testAddAndRemoveListeners() {
        ListenerListCouple couple = new ListenerListCouple();
        L l1 = new L();
        L l11 = new L();
        L l2 = new L();
        L l21 = new L();
        L l3 = new L();
        couple.add(l1);
        couple.add(l2);
        couple.add(l3);
        couple.add(l21);
        couple.add(l11);
        couple.remove(l1);
        couple.remove(l2);
        couple.remove(l2);
        couple.remove(l21);
        couple.remove(l3);
        couple.add(l3);
    }
    
    public void testSerialization() throws Exception {
        ListenerList<EventListener> ll = new ListenerList<EventListener>();
        ll.add(new L());
        ll.add(new L());
        ll.add(new L());
        
        NbMarshalledObject mo = new NbMarshalledObject(ll);
        ListenerList sll = (ListenerList)mo.get();
        EventListener[] lla = ll.getListeners();
        EventListener[] slla = sll.getListeners();
        assertEquals(lla.length, slla.length);
        for (int i = lla.length - 1; i >= 0; i--) {
            assertEquals(lla[i], slla[i]);
        }
    }
    
    private static final class L implements EventListener, Serializable {
        
        static final long serialVersionUID = 12345L;
        
        static int cntr; // static counter
        
        private int id = cntr++; // should be restored during deserialization
        
        private int notified;
        
        public void notifyChange() {
            notified++;
        }
        
        public int getNotified() {
            return notified;
        }
        
        public int hashCode() {
            return id;
        }
        
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o instanceof L)
                return (id == ((L)o).id);
            return false;
        }
        
    }

    private static final class ListenerListCouple {
        
        ListenerList<EventListener> listenerList;
        
        List<EventListener> imitation;
        
        public ListenerListCouple() {
            listenerList = new ListenerList<EventListener>();
            imitation = new ArrayList<EventListener>();
        }
        
        public void add(EventListener listener) {
            listenerList.add(listener);
            imitation.add(listener);
            checkListsEqual();
        }
        
        public void remove(EventListener listener) {
            listenerList.remove(listener);
            imitation.remove(listener);
            checkListsEqual();
        }
        
        public void checkListsEqual() {
            // Check the same listeners are stored in imitation
            assertEquals(imitation.size(), listenerList.getListenerCount());
            int i = listenerList.getListenerCount() - 1;
            EventListener[] listeners = listenerList.getListeners();
            for (EventListener l : imitation) {
                assertSame(l, listeners[i--]);
            }
        }
        
    }
    
}
