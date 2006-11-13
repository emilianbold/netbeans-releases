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

package org.openide.filesystems;

import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class NoAWTTest extends NbTestCase {
    
    public NoAWTTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    protected Level logLevel() {
        return Level.ALL;
    }
    
    public void testInitializeFileUtil() throws Exception {
        CharSequence awt = Log.enable("", Level.ALL);
        
        Class<?> fu = FileUtil.class;
        Class.forName(fu.getName(), true, getClass().getClassLoader());
        
        if (awt.toString().toLowerCase().indexOf("awt") >= 0) {
            fail("Do not even try to access AWT when initializing anything in filesystems:\n" + awt);
        }
    }
}
