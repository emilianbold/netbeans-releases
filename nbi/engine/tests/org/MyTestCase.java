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

import java.io.File;
import java.io.FileInputStream;
import junit.framework.TestCase;
import java.util.logging.LogManager;
import org.netbeans.installer.utils.FileUtils;

/**
 *
 * @author Danila Dugurov
 */
public class MyTestCase extends TestCase {
  
  public static final File testWD = new File("testWD");
  public static final File testOutput = new File("testOutput");
  
  protected void setUp() throws Exception {
    super.setUp();
    testWD.mkdirs();
    testOutput.mkdirs();
  }
  
  protected void tearDown() throws Exception {
    FileUtils.deleteFile(testWD, true);
    FileUtils.deleteFile(testOutput, true);
  }
}
