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

package org.netbeans.spi.project.support.ant;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import org.netbeans.junit.NbTestCase;

/**
 * @author Jesse Glick
 */
public class FilterPropertyProviderTest extends NbTestCase {

    public FilterPropertyProviderTest(String name) {
        super(name);
    }

    public void testDelegatingPropertyProvider() throws Exception {
        AntBasedTestUtil.TestMutablePropertyProvider mpp = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        DPP dpp = new DPP(mpp);
        AntBasedTestUtil.TestCL l = new AntBasedTestUtil.TestCL();
        dpp.addChangeListener(l);
        assertEquals("initially empty", Collections.emptyMap(), dpp.getProperties());
        mpp.defs.put("foo", "bar");
        mpp.mutated();
        assertTrue("got a change", l.expect());
        assertEquals("now right contents", Collections.singletonMap("foo", "bar"), dpp.getProperties());
        AntBasedTestUtil.TestMutablePropertyProvider mpp2 = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        mpp2.defs.put("foo", "bar2");
        dpp.setDelegate_(mpp2);
        assertTrue("got a change from new delegate", l.expect());
        assertEquals("right contents from new delegate", Collections.singletonMap("foo", "bar2"), dpp.getProperties());
        mpp2.defs.put("foo", "bar3");
        mpp2.mutated();
        assertTrue("got a change in new delegate", l.expect());
        assertEquals("right contents", Collections.singletonMap("foo", "bar3"), dpp.getProperties());
        Reference<?> r = new WeakReference<Object>(mpp);
        mpp = null;
        assertGC("old delegates can be collected", r);
        r = new WeakReference<Object>(dpp);
        dpp = null; // but not mpp2
        assertGC("delegating PP can be collected when delegate is not", r); // #50572
    }

    private static final class DPP extends FilterPropertyProvider {
        public DPP(PropertyProvider pp) {
            super(pp);
        }
        public void setDelegate_(PropertyProvider pp) {
            setDelegate(pp);
        }
    }

}
