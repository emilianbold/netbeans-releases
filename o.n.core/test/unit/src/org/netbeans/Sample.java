/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import junit.framework.AssertionFailedError;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import java.io.InputStream;
import java.lang.reflect.*;

/** Sample class to pass to PatchByteCodeTest to see what changes can be done.
 */
class Sample extends Object {
    private static Object member;
    private final Object field = null;
    
    public Sample () {
    }

    protected synchronized void member (Object x) {
    }

    private final Object method () {
        return null;
    }

    protected static void staticmethod () {
    }
}
