/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.test;

import org.netbeans.modules.cnd.test.BaseTestSuite;

/**
 * IMPORTANT NOTE:
 * If This class is not compiled with the notification about not resolved
 * BaseTestSuite class => cnd/core tests are not compiled
 * 
 * To solve this problem compile or run tests for cnd/core
 */

/**
 * base class for modelimpl module tests suite
 * @author Vladimir Voskresensky
 */
public class ModelImplBaseTestSuite extends BaseTestSuite {
    
    /**
     * Constructs an empty TestSuite.
     */
    public ModelImplBaseTestSuite() {
        super();
    }

    /**
     * Constructs a TestSuite from the given class. Adds all the methods
     * starting with "test" as test cases to the suite.
     *
     */
    public ModelImplBaseTestSuite(Class theClass) {       
        super(theClass);
    }

    /**
     * Constructs an empty TestSuite.
     */
    public ModelImplBaseTestSuite(String name) {
        super(name);
    }
}
