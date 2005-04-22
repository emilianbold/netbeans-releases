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
import java.io.Serializable;

public class AbstractLookupTest extends AbstractLookupBaseHid implements AbstractLookupBaseHid.Impl {
    public AbstractLookupTest(java.lang.String testName) {
        super(testName, null);
    }
    
    // 
    // Impl of AbstractLookupBaseHid.Impl
    // 
    
    /** Creates the initial abstract lookup.
     */
    public Lookup createInstancesLookup (InstanceContent ic) {
        return new AbstractLookup (ic, new InheritanceTree ());
    }
    
    /** Creates an lookup for given lookup. This class just returns 
     * the object passed in, but subclasses can be different.
     * @param lookup in lookup
     * @return a lookup to use
     */
    public Lookup createLookup (Lookup lookup) {
        return lookup;
    }

    public void clearCaches () {
    }    
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(AbstractLookupTest.class));
    }
    
    static class LkpResultCanBeGargageCollectedAndClearsTheResult extends AbstractLookup {
        public int cleared;
        public int dirty;

        synchronized boolean cleanUpResult (Template t) {
            boolean res = super.cleanUpResult (t);
            if (res) {
                cleared++;
            } else {
                dirty++;
            }

            notifyAll ();

            return res;
        }
    }
    public void testResultCanBeGargageCollectedAndClearsTheResult () throws Exception {
        LkpResultCanBeGargageCollectedAndClearsTheResult lkp = new LkpResultCanBeGargageCollectedAndClearsTheResult ();
        assertSize ("24 for AbstractLookup, 8 for two ints", 32, lkp);
        synchronized (lkp) {
            Lookup.Result res = lkp.lookup (new Lookup.Template (getClass ()));
            res.allItems();
            
            WeakReference ref = new WeakReference (res);
            res = null;
            assertGC ("Reference can get cleared", ref);
         
            // wait till we 
            while (lkp.cleared == 0 && lkp.dirty == 0) {
                lkp.wait ();
            }
            
            assertEquals ("No dirty cleanups", 0, lkp.dirty);
            assertEquals ("One final cleanup", 1, lkp.cleared);
        }
        //assertSize ("Everything has been cleaned to original size", 32, lkp);
        
    }
    
    public void testPairCannotBeUsedInMoreThanOneLookupAtOnce () throws Exception {
        /** Simple pair with no data */
        class EmptyPair extends AbstractLookup.Pair {
            protected boolean creatorOf(Object obj) { return false; }
            public String getDisplayName() { return "Empty"; }
            public String getId() { return "empty"; }
            public Object getInstance() { return null; }
            public Class getType() { return Object.class; }
            protected boolean instanceOf(Class c) { return c == getType (); }
        } // end of EmptyPair
        
        AbstractLookup.Content c1 = new AbstractLookup.Content ();
        AbstractLookup.Content c2 = new AbstractLookup.Content ();
        AbstractLookup l1 = new AbstractLookup (c1);
        AbstractLookup l2 = new AbstractLookup (c2);
        
        EmptyPair empty = new EmptyPair ();
        c1.addPair (empty);
        Lookup.Result res = l1.lookup (new Lookup.Template (Object.class));
        assertEquals (
            "Pair is really found", empty, 
            res.allItems ().iterator().next ()
        );
        try {
            c2.addPair (empty);
            fail ("It should not be possible to add pair to two lookups");
        } catch (IllegalStateException ex) {
            // ok, exception is fine
        }
        assertEquals (
            "L2 is still empty", Collections.EMPTY_LIST, 
            new ArrayList (l2.lookup (new Lookup.Template (Object.class)).allItems ())
        );
    }
    
    public void testInitializationCanBeDoneFromAnotherThread () {
        class MyLkp extends AbstractLookup implements Runnable {
            private InstanceContent ic;
            private boolean direct;
            
            public MyLkp (boolean direct) {
                this (direct, new InstanceContent ());
            }
                
            private MyLkp (boolean direct, InstanceContent ic) {
                super (ic);
                this.direct = direct;
                this.ic = ic;
            }
            
            protected void initialize () {
                if (direct) {
                    run ();
                } else {
                    RequestProcessor.getDefault().post (this).waitFinished ();
                }
            }
            
            public void run () {
                ic.add (this);
                ic.remove (this);
                ic.set (Collections.nCopies(10, this), null);
                ic.set (Collections.EMPTY_LIST, null);
                ic.add (AbstractLookupTest.this);
            }
        }
        
        assertEquals ("The test should be there", this, new MyLkp (true).lookup (Object.class));
        assertEquals ("and in async mode as well", this, new MyLkp (false).lookup (Object.class));
    }
    
    public void testBeforeLookupIsCalled () {
        class BeforeL extends AbstractLookup {
            public ArrayList list = new ArrayList ();
            public String toAdd;
            public InstanceContent ic;
            
            public BeforeL () {
                this (new InstanceContent ());
            }
            
            private BeforeL (InstanceContent c) {
                super (c);
                this.ic = c;
            }
        
            protected void beforeLookup (Template t) {
                if (toAdd != null) {
                    list.add (0, new SerialPair (toAdd));
                    setPairs (list);
                } else {
                    ic.add (new Integer (1));
                }
            }
        }
        
        BeforeL lookup = new BeforeL ();
        
        lookup.toAdd = "First";
        assertEquals ("First if found", "First", lookup.lookup (String.class));
        
        lookup.toAdd = "2";
        assertEquals ("2 is not first", "2", lookup.lookup (String.class));
        
        Lookup.Result res = lookup.lookup (new Lookup.Template (Object.class));
        for (int i = 3; i < 20; i++) {
            lookup.toAdd = String.valueOf (i);
            assertEquals (i + " items are now there", i, res.allInstances ().size ());
        }
        for (int i = 20; i < 35; i++) {
            lookup.toAdd = String.valueOf (i);
            assertEquals (i + " items are now there", i, res.allItems ().size ());
        }
        
        assertEquals ("Just strings are there now", 1, res.allClasses ().size ());
        lookup.toAdd = null; // this will add integer
        assertEquals ("Two classes now", 2, res.allClasses ().size ());
    }
}
