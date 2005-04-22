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
public class LookupsProxyTest extends AbstractLookupBaseHid
implements AbstractLookupBaseHid.Impl {
    public LookupsProxyTest(java.lang.String testName) {
        super(testName, null);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite (LookupsProxyTest.class));
    }
    
    /** Creates an lookup for given lookup. This class just returns 
     * the object passed in, but subclasses can be different.
     * @param lookup in lookup
     * @return a lookup to use
     */
    public Lookup createLookup (final Lookup lookup) {
        return org.openide.util.lookup.Lookups.proxy (
            new Lookup.Provider () {
                public Lookup getLookup () {
                    return lookup;
                }
            }
        );
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
        Changer ch = new Changer (Lookup.EMPTY);
        
        Lookup lookup = Lookups.proxy(ch);
        Lookup.Result res = lookup.lookup (new Lookup.Template (Object.class));
        
        LL ll = new LL ();
        res.addLookupListener (ll);
        Collection allRes = res.allInstances ();

        ch.setLookup (new AbstractLookup (new InstanceContent ())); // another empty lookup
        lookup.lookup (Object.class); // does the refresh
        
        assertEquals("Replacing an empty by empty does not generate an event", 0, ll.getCount());
        
        InstanceContent content = new InstanceContent ();
        AbstractLookup del = new AbstractLookup (content);
        content.add (this);
        ch.setLookup (del);
        lookup.lookup (Object.class);
        
        if (ll.getCount () != 1) {
            fail ("Changing lookups with different content generates an event");
        }
        
        ch.setLookup (del);
        lookup.lookup (Object.class);
        
        if (ll.getCount () != 0) {
           fail ("Not changing the lookups does not generate any event");
        }
    }
    
    private static final class Changer implements Lookup.Provider {
        private Lookup lookup;
        
        public Changer (Lookup lookup) {
            setLookup (lookup);
        }
        
        public void setLookup (Lookup lookup) {
            this.lookup = lookup;
        }
        
        public Lookup getLookup() {
            return lookup;
        }
    }
}
