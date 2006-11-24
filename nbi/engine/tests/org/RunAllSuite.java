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
 *
 * $Id$
 */
package org;

import junit.framework.TestSuite;
import org.connection.ConnectionConfiguratorTest;
import org.connection.ProxySelectorTest;
import org.connection.ProxyTest;
import org.util.DomVisitorTest;

/**
 *
 * @author Danila_Dugurov
 */
public class RunAllSuite extends TestSuite {
    
    public RunAllSuite() {
        addTestSuite(ProxyTest.class);
        addTestSuite(ProxySelectorTest.class);
        addTestSuite(ConnectionConfiguratorTest.class);
        addTestSuite(DomVisitorTest.class);
        //addTestSuite(WindowsRegistryTest.class);
        //todo: dinamic add test case without manual registration
    }
    //this done only becouse without it netbeans faild to run tests
      public void testFake() {}
}
