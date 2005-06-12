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

package org.openide.util.lookup;

import org.openide.util.*;

import java.lang.ref.WeakReference;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;

/** Runs all NbLookupTest tests on ProxyLookup and adds few additional.
 */
public class ProxyLookupTest extends AbstractLookupBaseHid
implements AbstractLookupBaseHid.Impl {
    public ProxyLookupTest(java.lang.String testName) {
        super(testName, null);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite (ProxyLookupTest.class));
    }
    
    /** Creates an lookup for given lookup. This class just returns 
     * the object passed in, but subclasses can be different.
     * @param lookup in lookup
     * @return a lookup to use
     */
    public Lookup createLookup (Lookup lookup) {
        return new ProxyLookup (new Lookup[] { lookup });
    }
    
    public Lookup createInstancesLookup (InstanceContent ic) {
        return new AbstractLookup (ic);
    }
    

    public void clearCaches () {
    }    
    
    
    /** Check whether setLookups method does not fire when there is no
     * change in the lookups.
     */
    public void testProxyListener () {
        ProxyLookup lookup = new ProxyLookup (new Lookup[0]);
        Lookup.Result res = lookup.lookup (new Lookup.Template (Object.class));
        
        LL ll = new LL ();
        res.addLookupListener (ll);
        Collection allRes = res.allInstances ();
        
        lookup.setLookups (new Lookup[0]);
        
        if (ll.getCount () != 0) {
           fail ("Calling setLookups (emptyarray) fired a change");
        }
        
        Lookup del = new AbstractLookup ();
        lookup.setLookups (new Lookup[] { del });
        
        if (ll.getCount () != 1) {
            fail ("Changing lookups did not generate an event");
        }
        
        lookup.setLookups (new Lookup[] { del });
        
        if (ll.getCount () != 0) {
           fail ("Calling setLookups (thesamearray) fired a change");
        }
    }

    public void testSetLookups () throws Exception {
        AbstractLookup a1 = new AbstractLookup (new InstanceContent ());
        AbstractLookup a2 = new AbstractLookup (new InstanceContent ());
        
        InstanceContent i3 = new InstanceContent ();
        i3.add (i3);
        AbstractLookup a3 = new AbstractLookup (i3);

        final ProxyLookup p = new ProxyLookup (new Lookup[] { a1, a2 });
        final Lookup.Result res1 = p.lookup (new Lookup.Template (Object.class));
        Collection c1 = res1.allInstances();
        
        Lookup.Result res2 = p.lookup (new Lookup.Template (String.class));
        Collection c2 = res2.allInstances ();
        
        
        assertTrue ("We need two results", res1 != res2);

        final Object blocked = new Object ();

        class L extends Object implements LookupListener {
            public void resultChanged (LookupEvent ev) {
                try {
                    res1.removeLookupListener(this);
                    
                    // waiting for second thread to start #111#
                    blocked.wait ();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    fail ("An exception occured ");
                }
            }
        }
        
        final L listener1 = new L ();
        res1.addLookupListener (listener1);
        

        Runnable newLookupSetter = new Runnable() {
            public void run () {
                synchronized (blocked) {
                    try {
                        p.setLookups (new Lookup[0]);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        fail ("setLookups failed.");
                    } finally {
                        // starts the main thread #111#
                        blocked.notify ();
                    }
                }
            }
        };
        
        synchronized (blocked) {
            new Thread (newLookupSetter).start ();
            
            p.setLookups (new Lookup[] { a1, a2, a3 });
        }
    }
    
    public void testProxyLookupTemplateCaching(){
        Lookup lookups[] = new Lookup[1];
        doProxyLookupTemplateCaching(lookups, false);
    }
    
    public void testProxyLookupTemplateCachingOnSizeTwoArray() {
        Lookup lookups[] = new Lookup[2];
        lookups[1] = Lookup.EMPTY;
        doProxyLookupTemplateCaching(lookups, false);
    }
    public void testProxyLookupShallNotAllowModificationOfGetLookups(){
        Lookup lookups[] = new Lookup[1];
        doProxyLookupTemplateCaching(lookups, true);
    }
    
    public void testProxyLookupShallNotAllowModificationOfGetLookupsOnSizeTwoArray() {
        Lookup lookups[] = new Lookup[2];
        lookups[1] = Lookup.EMPTY;
        doProxyLookupTemplateCaching(lookups, true);
    }
    
    /** Index 0 of lookups will be modified, the rest is up to the 
     * setup code.
     */
    private void doProxyLookupTemplateCaching(Lookup[] lookups, boolean reget) {
        // Create MyProxyLookup with one lookup containing the String object
        InstanceContent ic = new InstanceContent();
        ic.add(new String("Hello World")); //NOI18N
        lookups[0] = new AbstractLookup(ic);
        ProxyLookup proxy = new ProxyLookup(lookups);
        if (reget) {
            lookups = proxy.getLookups();
        }
        
        // Performing template lookup for String object
        Lookup.Result result = proxy.lookup(new Lookup.Template(String.class, null, null));
        int stringTemplateResultSize = result.allInstances().size();
        assertEquals ("Ensure, there is only one instance of String.class in proxyLookup:", //NOI18N
                1, stringTemplateResultSize);
        
        // Changing lookup in proxy lookup, now it will contain 
        // StringBuffer Object instead of String
        InstanceContent ic2 = new InstanceContent();
        ic2.add(new Integer(1234567890));
        lookups[0] = new AbstractLookup(ic2);
        proxy.setLookups(lookups);
        
        assertEquals ("the old result is updated", 0, result.allInstances().size());

        // Instance of String.class should not appear in proxyLookup
        Lookup.Result r2 = proxy.lookup(new Lookup.Template(String.class, null, null));
        assertEquals ("Instance of String.class should not appear in proxyLookup:", //NOI18N
                0, r2.allInstances().size());

        Lookup.Result r3 = proxy.lookup(new Lookup.Template(Integer.class, null, null));
        assertEquals ("There is only one instance of Integer.class in proxyLookup:", //NOI18N
                1, r3.allInstances().size());
        
    }
}
