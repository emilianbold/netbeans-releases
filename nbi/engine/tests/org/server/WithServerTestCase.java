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
package org.server;

import org.*;

/**
 *
 * @author Danila_Dugurov
 */
/**
 *this test case which invoke test server and generate test data, offcouse if wasn't generated yet.
 */
public class WithServerTestCase extends MyTestCase {
  
  private final DefaultServer server = new DefaultServer("testData", 8080);
  private final TestDataGenerator dataGenerator = new TestDataGenerator("testData");
  
  protected void setUp() throws Exception {
    super.setUp();
    dataGenerator.generateTestData();
    server.start();
  }
  
  protected void tearDown() throws Exception {
    server.stop();
    //dataGenerator.deleteTestData();
    //this method is depricated because test data rather big
    //and it's not good idea to delete and generate it after every test.
    super.tearDown();
  }
}
