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