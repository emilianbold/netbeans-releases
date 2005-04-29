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

package org.openide.filesystems;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 * URL mapper is often invoked from inside the lookup. That is why
 * it needs to be ready to survive strange states.
 *
 * Trying to mimic IZ 44365.
 *
 * @author Jaroslav Tulach
 */
public class URLMapperLookupTest extends NbTestCase {
    
    public URLMapperLookupTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.URLMapperLookupTest$Lkp");
        
        super.setUp();
        
        assertEquals ("Our lookup is registered", Lkp.class, org.openide.util.Lookup.getDefault().getClass());
    }
    
    public void testIfIAskForAnItemThatAsksURLMapperAndThenAskOnceMoreAllMappersAreAsked () 
    throws Exception {
        Object found = org.openide.util.Lookup.getDefault().lookup (QueryingPair.class);
        assertNotNull (found);
        
        MyUM.queried = null;
        java.net.URL url = new java.net.URL ("http://www.netbeans.org");
        URLMapper.findFileObject(url);
        
        assertEquals ("Really got the query thru", url, MyUM.queried);
    }

    /** This is a pair that as a part of its instanceOf method queries the URL resolver.
     */
    private static class QueryingPair extends org.openide.util.lookup.AbstractLookup.Pair {
        public boolean beBroken;
        
        public java.lang.String getId() {
            return getType ().toString();
        }

        public java.lang.String getDisplayName() {
            return getId ();
        }

        public java.lang.Class getType() {
            return getClass ();
        }

       protected boolean creatorOf(java.lang.Object obj) {
            return obj == this;
        }

        protected boolean instanceOf(java.lang.Class c) {
            if (beBroken) {
                beBroken = false;
                try {
                    assertNull ("is still null", MyUM.queried);
                    java.net.URL url = new java.net.URL ("http://www.netbeans.org");
                    URLMapper.findFileObject(url);
                    assertNull ("This query did not get thru", MyUM.queried);
                } catch (java.net.MalformedURLException ex) {
                    ex.printStackTrace();
                    fail ("No exceptions: " + ex.getMessage ());
                }
            }
            return c.isAssignableFrom(getType ());
        }

        public java.lang.Object getInstance() {
            return this;
        }
    }
    
    private static final class MyUM extends URLMapper {
        public static java.net.URL queried;
        
        public org.openide.filesystems.FileObject[] getFileObjects(java.net.URL url) {
            queried = url;
            return null;
        }

        public java.net.URL getURL(org.openide.filesystems.FileObject fo, int type) {
            return null;
        }
    }
     

    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private static org.openide.util.lookup.InstanceContent ic;
        
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            this.ic = ic;
        }

        protected void initialize() {
            // a small trick to make the InheritanceTree storage to be used
            // because if the amount of elements in small, the ArrayStorage is 
            // used and it does not have the same problems like InheritanceTree
            for (int i = 0; i < 1000; i++) {
                ic.add (new Integer (i));
            }

            QueryingPair qp = new QueryingPair();
            ic.addPair (qp);
            ic.add (new MyUM ());

            
            qp.beBroken = true;
        }

    } // end of Lkp
}
