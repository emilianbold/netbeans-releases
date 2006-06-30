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

