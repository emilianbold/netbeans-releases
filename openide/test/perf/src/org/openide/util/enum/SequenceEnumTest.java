/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.util.enum;

public class SequenceEnumTest extends EnumBenchmark {
    
    public SequenceEnumTest( String name ) {
	super( name );
    }

    Object[] array;
    
    protected void preSetUp() {
	int arg = ((Integer)getArgument()).intValue();
	array = new Object[arg/2];
    }

    protected Object createInstance() {
	return new SequenceEnumeration( 
	    new ArrayEnumeration( array ),
	    new ArrayEnumeration( array )
	);
    }

    public static void main( String[] args ) {
        junit.textui.TestRunner.run( new junit.framework.TestSuite( SequenceEnumTest.class ) );
    }
}
