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
package org.netbeans.junit;

import junit.framework.*;

/**
 * A Decorator to set up and tear down additional fixture state.
 * Subclass TestSetup and insert it into your tests when you want
 * to set up additional state once before the tests are run.
 */
public class NbTestSetup extends NbTestDecorator {

	public NbTestSetup(Test test) {
		super(test);
	}
               
        
	public void run(final TestResult result) {
		Protectable p= new Protectable() {
			public void protect() throws Exception {
				setUp();
				basicRun(result);
				tearDown();
			}
		};
		result.runProtected(this, p);
	}
        
	/**
	 * Sets up the fixture. Override to set up additional fixture
	 * state.
	 */
	protected void setUp() throws Exception {
	}
	/**
	 * Tears down the fixture. Override to tear down the additional
	 * fixture state.
	 */
	protected void tearDown() throws Exception {
	}
}