/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.junit;

/**
 * Instance filter contract.
 * 
 * @author Petr Kuzel
 */
public interface MemoryFilter {

    /** 
     * Decides non-destructively wheter given instance pass
     * custom criteria. Implementation must not alter
     * JVM heap and it must return the same result if
     * it gets some instance multiple times. And
     * it must be very fast.
     *
     * @return <code>true</code> if passed instance is not accepted. 
     *
     * <p>E.g.:
     * <code>return obj instanceof java.lang.ref.Reference</code>
     */
    boolean reject(Object obj);
}

