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
package org.netbeans.modules.turbo;

import java.util.List;

/**
 * SPI, extension point allowing third parties to redefine
 * attribute reading and writing. Must be registered
 * in default {@link org.openide.util.Lookup}.
 * <p>
 * It must generally deternime that they
 * support given entity key and attribute pair
 * and if so then perform action.
 * <p>
 * Two providers can theoreticaly clash and support
 * the same attribute for the same entity key. The
 * key point is that attribute meaning must be precisely
 * defined guaranteeing that two independent providers
 * respond with exactly same value hence making irrelevant
 * which one is actually choosen.
 * <p>
 * Providers should not cache results.
 *
 * @author Petr Kuzel
 */
public interface TurboProvider {

    /**
     * Reports if an attribute is supported by the implementation.
     */
    boolean recognizesAttribute(String name);

    /**
     * Reports if the entity identified by key is supported by the implementation.
     */
    boolean recognizesEntity(Object key);

    /**
     * Reads given attribute for given fileobject. No method
     * parameter may be referenced after method execution finishes.
     *
     * @param key identifies source entity, never <code>null</code>
     * @param name identifies requested attribute, never <code>null</code>
     * @param memoryCache can store speculative results
     * @return attribute value or <code>null</code> if it does not exist.
     */
    Object readEntry(Object key, String name, MemoryCache memoryCache);

    /**
     * Writes given attribute. No method
     * parameter may be referenced after method execution finishes.
     *
     * @param key identifies target entity, never <code>null</code>
     * @param name identifies attribute, never <code>null</code>
     * @param value actual attribute value that should be stored or <code>null</code> for removing it
     * @return <code>false</code> on write failure if provider denies the value. On I/O error it
     * returns <code>true</code>.
     */
    boolean writeEntry(Object key, String name, Object value);

    /**
     * Provides direct access to memory layer (without
     * delegating to providers layer, here source).
     */
    public static final class MemoryCache {

        private final boolean enabled;

        private final List speculative;

        private final Memory memory;

        private MemoryCache(boolean enabled) {
            this.enabled = enabled;
            speculative = null;
            memory = null;
        }

        private MemoryCache(Memory memory, List speculative) {
            enabled = true;
            this.memory = memory;
            this.speculative = speculative;
        }

        /**
         * Creates instance intercepting speculative results.
         * @param memory implementation
         * @param speculative add()s speculative results into it
         */
        static MemoryCache createDefault(Memory memory, List speculative) {
            return new MemoryCache(memory, speculative);
        }

        /** T9Y entry point. */
        static MemoryCache getTest() {
            return new MemoryCache(false);
        }

        /**
         * Writes speculative entry into memory layer.
         */
        public void cacheEntry(Object key, String name, Object value) {
            if (enabled == false) return;
            memory.put(key, name, value);
            if (speculative != null) speculative.add(new Object[] {key, name, value});
        }

        /** Return speculative results <code>[]{FileObject,String,Object}</code> silently inserted into memory */
        List getSpeculative() {
            return speculative;
        }
    }
}
