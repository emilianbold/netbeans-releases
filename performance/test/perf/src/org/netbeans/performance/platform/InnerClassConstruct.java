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

package org.netbeans.performance.platform;

import org.netbeans.performance.Benchmark;

/**
 * The Benchmark measuring the difference between using public and
 * private constructor of the private inner class.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class InnerClassConstruct extends Benchmark {

    public InnerClassConstruct(String name) {
        super( name );
    }

    protected int getMaxIterationCount() {
	return Integer.MAX_VALUE;
    }

    /**
     * Pour into the call stack and then create an object.
     * Used as a reference to divide the time between recursive decline
     * and Exception creation.
     */
    public void testCreatePrivate() throws Exception {
        int count = getIterationCount();
    
        while( count-- > 0 ) {
	    new Priv();
        }
    }

    /**
     * Create an Exception deep in the call stack, filling its stack trace.
     */
    public void testCreatePublic() throws Exception {
        int count = getIterationCount();
    
        while( count-- > 0 ) {
            new Publ();
        }
    }

    public static void main( String[] args ) {
	simpleRun( InnerClassConstruct.class );
    }

    private final class Priv {}
    
    private final class Publ {
	public Publ() {}
    }

}
