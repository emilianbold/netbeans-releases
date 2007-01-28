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
package org.netbeans.modules.visualweb.insync;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * This is a class to provide a way for us to debug modifications to an identity map, by being able to set
 * breakpoints.
 *
 * @author ea149956
 *
 */
public class DebugIdentityHashMap extends IdentityHashMap {

    private static final long serialVersionUID = -3172709935724005807L;

    public DebugIdentityHashMap() {
        super();
    }

    public DebugIdentityHashMap(int expectedMaxSize) {
        super(expectedMaxSize);
    }

    public DebugIdentityHashMap(Map m) {
        super(m);
    }

    public Object put(Object key, Object value) {
        Object result = super.put(key, value);
        return result;
    }

    public Object remove(Object key) {
        Object result = super.remove(key);
        return result;
    }

}
