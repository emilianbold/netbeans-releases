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

/**
 * Default NOP implementation. It's dangerous because it
 * understands all requests so it's explictly managed by
 * the Turbo query and used as a fallback.
 *
 * @author Petr Kuzel
 */
final class DefaultTurboProvider implements TurboProvider {

    private static final TurboProvider DEFAULT_INSTANCE = new DefaultTurboProvider();

    public static TurboProvider getDefault() {
        return DEFAULT_INSTANCE;
    }

    private DefaultTurboProvider() {
    }

    public boolean recognizesAttribute(String name) {
        return true;
    }

    public boolean recognizesEntity(Object key) {
        return true;
    }

    public Object readEntry(Object key, String name, MemoryCache memoryCache) {
        return null;
    }

    public boolean writeEntry(Object key, String name, Object value) {
        return true;
    }

    public String toString() {
        return "DefaultTurboProvider a NOP implementation";  // NOI18N
    }
}
