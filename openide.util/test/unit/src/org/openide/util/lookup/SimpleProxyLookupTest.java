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
