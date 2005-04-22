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

public class AbstractLookupArrayStorageTest extends AbstractLookupBaseHid {
    public AbstractLookupArrayStorageTest(java.lang.String testName) {
        super(testName, null);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite ());
    }
    
    public static TestSuite suite () {
        NbTestSuite suite = new NbTestSuite ();
        suite.addTest (new PL (2));
        suite.addTest (new AL (1));
        suite.addTest (new AL (-1));
        suite.addTest (new PL (-1));
        suite.addTest (new AL (5));
        suite.addTest (new PL (3));
        suite.addTest (new AL (2000));
        suite.addTest (new PL (2000));
        return suite;
    }

    public static final class AL extends ArrayTestSuite {
        public AL (int trash) {
            super (trash);
        }
        
        public Lookup createLookup (Lookup lookup) {
            return lookup;
        }
        
        public void clearCaches () {
        }
        
    }
    
    public static final class PL extends ArrayTestSuite {
        public PL (int trash) {
            super (trash);
        }
        
        public Lookup createLookup (Lookup lookup) {
            return  new ProxyLookup (new Lookup[] { lookup });
        }
        
        public void clearCaches () {
        }
        
    }
    
    private static abstract class ArrayTestSuite extends NbTestSuite 
    implements AbstractLookupBaseHid.Impl {
        private int trash;
        
        public ArrayTestSuite (int trash) {
            super (AbstractLookupArrayStorageTest.class);
            this.trash = trash;
            
            int cnt = this.countTestCases();
            for (int i = 0; i < cnt; i++) {
                Object o = this.testAt (i);
                AbstractLookupBaseHid t = (AbstractLookupBaseHid)o;
                t.impl = this;
            }
        }
        
        public Lookup createInstancesLookup (InstanceContent ic) {
            if (trash == -1) {
                return new AbstractLookup (ic, new ArrayStorage ());
            } else {
                return new AbstractLookup (ic, new ArrayStorage (new Integer (trash)));
            }
        }
        
        
    }
}
