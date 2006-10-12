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

package org.netbeans.modules.j2ee.metadata;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Martin Adamek
 */
public class TestBase extends NbTestCase {

    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        assertEquals("Unable to set the default lookup!", Lkp.class, Lookup.getDefault().getClass());
    }

    public TestBase(String name) {
        super(name);
    }

    public static void setLookups(Object[] instances) {
        ((Lkp)Lookup.getDefault()).setLookups(instances);
    }

    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            setLookups(new Object[0]);
        }

        void setLookups(Object[] instances) {
            ClassLoader l = TestBase.class.getClassLoader();
            setLookups(new Lookup[] {
                Lookups.fixed(instances),
                Lookups.metaInfServices(l),
                Lookups.singleton(l)
            });
        }
    }
}
