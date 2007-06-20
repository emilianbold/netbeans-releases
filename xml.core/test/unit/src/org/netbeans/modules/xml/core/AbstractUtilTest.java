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
package org.netbeans.modules.xml.core;

import java.io.*;
import junit.framework.*;

public class AbstractUtilTest extends TestCase {
    
    public AbstractUtilTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testGetCallerPackage() {
        System.out.println("testGetCallerPackage");
//        try {
//            String pack = getClass().getPackage().getName();
//
//            assertTrue("Class package detection failed! " + testPackage(), testPackage().equals(pack));
//            assertTrue("Inner class package detection failed! " + Inner.testPackage(), Inner.testPackage().equals(pack));
//        } catch (Exception ex) {
//            ex.printStackTrace(new PrintWriter(System.out));
//        }
    }
    
//    private String testPackage() {
//        return AbstractUtilImpl.getCallerPackage();
//    }
//    
//    private class AbstractUtilImpl extends AbstractUtil {
//        
//    }
//    
//    private static class Inner {
//        static String testPackage() {
//            return AbstractUtilImpl.getCallerPackage();
//        }
//    }
}
