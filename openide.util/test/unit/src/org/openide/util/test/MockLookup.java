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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.test;

import java.lang.reflect.Field;
import static junit.framework.Assert.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Mock implementation of system default lookup suitable for use in unit tests.
 * The initial value just contains classpath services.
 */
public class MockLookup extends ProxyLookup {

    private static MockLookup DEFAULT;
    private static boolean making = false;

    static {
        making = true;
        try {
            System.setProperty("org.openide.util.Lookup", MockLookup.class.getName());
            if (Lookup.getDefault().getClass() != MockLookup.class) {
                // Someone else initialized lookup first. Try to force our way.
                Field defaultLookup = Lookup.class.getDeclaredField("defaultLookup");
                defaultLookup.setAccessible(true);
                defaultLookup.set(null, null);
            }
            assertEquals(MockLookup.class, Lookup.getDefault().getClass());
        } catch (Exception x) {
            throw new ExceptionInInitializerError(x);
        } finally {
            making = false;
        }
    }

    /** Do not call this directly! */
    public MockLookup() {
        assertTrue(making);
        assertNull(DEFAULT);
        DEFAULT = this;
        setInstances();
    }

    /**
     * Sets the global default lookup with zero or more delegate lookups.
     * Caution: if you don't include Lookups.metaInfServices, you may have trouble,
     * e.g. {@link #makeScratchDir} will not work.
     * Most of the time you should use {@link #setInstances} instead.
     */
    public static void setLookup(Lookup... lookups) {
        DEFAULT.setLookups(lookups);
    }

    /**
     * Sets the global default lookup with some fixed instances.
     * Will also include (at a lower priority) a {@link ClassLoader},
     * and services found from <code>META-INF/services/*</code> in the classpath.
     */
    public static void setInstances(Object... instances) {
        ClassLoader l = MockLookup.class.getClassLoader();
        setLookup(Lookups.fixed(instances), Lookups.metaInfServices(l), Lookups.singleton(l));
    }

}
