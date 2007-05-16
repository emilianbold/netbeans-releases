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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor.mimelookup.test;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Mock implementation of MimeLookup. Initially the mime lookup is empty. You
 * will have to register <code>MockMimeLookup.class</code> through <code>MockServices</code>.
 * 
 * <p>Example:
 * 
 * <pre>
 * MockServics.setServices(MockMimeLookup.class);
 *
 * MimePath mimePath = MimePath.parse("text/x-abc");
 * MockMimeLookup.setInstances(mimePath, new TestFoldManagerFactory());
 * 
 * FoldManagerFactory f = MimeLookup.getLookup(mimePath).lookup(FoldManagerFactory.class);
 * </pre>
 */
public final class MockMimeLookup implements MimeDataProvider {

    private static final Map<MimePath, Lkp> MAP = new HashMap<MimePath, Lkp>();
    
    /**
     * Sets the lookup for <code>mimePath</code> with zero or more delegate lookups.
     * 
     * @param mimePath The mime path to set the lookup for.
     * @param lookups The delegate lookups.
     */
    public static void setLookup(MimePath mimePath, Lookup... lookups) {
        Lkp toUpdate = null;
        
        synchronized (MAP) {
            Lkp lkp = MAP.get(mimePath);
            if (lkp == null) {
                lkp = new Lkp(lookups);
                MAP.put(mimePath, lkp);
            } else {
                toUpdate = lkp;
            }
        }
        
        if (toUpdate != null) {
            toUpdate.set(lookups);
        }
    }
    
    /**
     * Sets the lookup for <code>mimePath</code> with some fixed instances.
     * 
     * @param mimePath The mime path to set the lookup for.
     * @param instances The instances to set.
     */
    public static void setInstances(MimePath mimePath, Object... instances) {
        setLookup(mimePath, Lookups.fixed(instances));
    }

    /** Don't use this directly. */
    public MockMimeLookup() {
    }
    
    /** You can call it, but it's probably not what you want. */
    public Lookup getLookup(MimePath mimePath) {
        synchronized (MAP) {
            return MAP.get(mimePath);
        }
    }
    
    private static final class Lkp extends ProxyLookup {
        
        public Lkp(Lookup... lookups) {
            super(lookups);
        }
        
        public void set(Lookup... lookups) {
            super.setLookups(lookups);
        }
    }
}
