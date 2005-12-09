/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.lookup;

import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;

/**
 *
 * @author Jan Lahoda
 */
public class SimpleProxyLookupTest extends NbTestCase {
    
    public SimpleProxyLookupTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void test69810() throws Exception {
        Lookup.Template t = new Lookup.Template(String.class);
        SimpleProxyLookup spl = new SimpleProxyLookup(new Provider() {
            public Lookup getLookup() {
                return Lookups.fixed(new Object[] {"test1", "test2"});
            }
        });
        
        assertGC("", new WeakReference(spl.lookup(t)));
        
        spl.lookup(new Lookup.Template(Object.class)).allInstances();
    }
    
}
