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

package org.netbeans.api.debugger;

import org.netbeans.api.debugger.test.TestLookupServiceFirst;
import org.netbeans.api.debugger.test.TestLookupServiceInterface;
import org.netbeans.api.debugger.test.TestLookupServiceSecond;

import java.net.Socket;
import java.util.*;
import java.io.Serializable;

/**
 * Tests lookup functionality.
 *
 * @author Maros Sandor
 */
public class LookupTest  extends DebuggerApiTestBase {

    public LookupTest(String s) {
        super(s);
    }

    public static class LookupContext {
    }

    public void testCompoundLookup() throws Exception {

        Object stringInstance = "stringInstace";
        HashMap hashMapInstance = new HashMap();
        Socket socketInstance = new Socket();
        TreeMap treeMapInstance = new TreeMap();
        StringBuffer sbInstance = new StringBuffer();
        Object [] instances = new Object [] {
            stringInstance,
            hashMapInstance,
            socketInstance,
            treeMapInstance,
            sbInstance
        };

        Object stringInstance2 = "stringInstace";
        HashMap hashMapInstance2 = new HashMap();
        Socket socketInstance2 = new Socket();
        TreeMap treeMapInstance2 = new TreeMap();
        StringBuffer sbInstance2 = new StringBuffer();
        Object [] instances2 = new Object [] {
            stringInstance2,
            hashMapInstance2,
            socketInstance2,
            treeMapInstance2,
            sbInstance2
        };

        Lookup l1 = new Lookup.Instance(instances);
        Lookup l2 = new Lookup.Instance(instances2);

        Lookup.Compound l = new Lookup.Compound(l1, l2);

        List services = l.lookup(null, String.class);
        assertEquals("Wrong number of objects in lookup", 2, services.size());
        assertContains("Wrong looked up object", stringInstance, services);
        assertContains("Wrong looked up object", stringInstance2, services);

        services = l.lookup(null, Class.class);
        assertEquals("Wrong number of objects in lookup", 0, services.size());

        services = l.lookup(null, Serializable.class);
        assertEquals("Wrong number of objects in lookup", 8, services.size());
        assertContains("Wrong looked up object", stringInstance, services);
        assertContains("Wrong looked up object", stringInstance2, services);
        assertContains("Wrong looked up object", sbInstance, services);
        assertContains("Wrong looked up object", sbInstance2, services);
        assertContains("Wrong looked up object", hashMapInstance, services);
        assertContains("Wrong looked up object", hashMapInstance2, services);
        assertContains("Wrong looked up object", treeMapInstance, services);
        assertContains("Wrong looked up object", treeMapInstance2, services);
    }

    public void testMetainfLookup() throws Exception {

        Lookup.MetaInf l = new Lookup.MetaInf("unittest");
        List list = l.lookup(null, TestLookupServiceFirst.class);
        assertEquals("Wrong looked up object", 1, list.size());
        assertInstanceOf("Wrong looked up object", list.get(0), TestLookupServiceFirst.class);

        Object o = l.lookupFirst(null, TestLookupServiceFirst.class);
        assertInstanceOf("Wrong looked up object", o, TestLookupServiceFirst.class);

        o = l.lookupFirst(null, TestLookupServiceInterface.class);
        assertInstanceOf("Wrong looked up object", o, TestLookupServiceSecond.class);

        o = l.lookupFirst(null, TestLookupServiceSecond.class);
        assertNull("Wrong looked up object", o);
    }

    public void testEmptyMetainfLookup() throws Exception {

        Lookup.MetaInf l = new Lookup.MetaInf("baddir");
        List list = l.lookup(null, TestLookupServiceFirst.class);
        assertEquals("Wrong looked up object", 0, list.size());

        Object o = l.lookupFirst(null, TestLookupServiceFirst.class);
        assertNull("Wrong looked up object", o);
    }

    public void testEmptyInstanceLookup() throws Exception {

        Object [] instances = new Object [0];
        Lookup l = new Lookup.Instance(instances);

        List services = l.lookup(null, Object.class);
        assertEquals("Wrong number of objects in lookup", 0, services.size());

        Object o = l.lookupFirst(null, Class.class);
        assertNull("Wrong looked up object", o);

        o = l.lookupFirst(null, Object.class);
        assertNull("Wrong looked up object", o);
    }

    public void testInstanceLookup() throws Exception {

        Object stringInstance = "stringInstace";
        HashMap hashMapInstance = new HashMap();
        Socket socketInstance = new Socket();
        TreeMap treeMapInstance = new TreeMap();
        StringBuffer sbInstance = new StringBuffer();

        Object [] instances = new Object [] {
            stringInstance,
            hashMapInstance,
            socketInstance,
            treeMapInstance,
            sbInstance
        };
        Lookup l = new Lookup.Instance(instances);

        List services = l.lookup(null, Object.class);
        assertEquals("Wrong number of objects in lookup", 5, services.size());
        assertContains("Object not present in lookup", stringInstance, services);
        assertContains("Object not present in lookup", hashMapInstance, services);
        assertContains("Object not present in lookup", socketInstance, services);
        assertContains("Object not present in lookup", treeMapInstance, services);
        assertContains("Object not present in lookup", sbInstance, services);

        services = l.lookup(null, CharSequence.class);
        assertEquals("Wrong number of objects in lookup", 2, services.size());
        assertContains("Object not present in lookup", stringInstance, services);
        assertContains("Object not present in lookup", sbInstance, services);

        services = l.lookup(null, Serializable.class);
        assertEquals("Wrong number of objects in lookup", 4, services.size());
        assertContains("Object not present in lookup", stringInstance, services);
        assertContains("Object not present in lookup", hashMapInstance, services);
        assertContains("Object not present in lookup", treeMapInstance, services);
        assertContains("Object not present in lookup", sbInstance, services);

        services = l.lookup(null, Class.class);
        assertEquals("Wrong number of objects in lookup", 0, services.size());

        Object o = l.lookupFirst(null, Class.class);
        assertNull("Wrong looked up object", o);

        o = l.lookupFirst(null, Socket.class);
        assertSame("Wrong looked up object", socketInstance, o);
    }

    private void assertContains(String msg, Object obj, Collection collection) {
        for (Iterator i = collection.iterator(); i.hasNext();) {
            if (i.next() == obj) return;
        }
        throw new AssertionError(msg + ": " + obj);
    }
}
