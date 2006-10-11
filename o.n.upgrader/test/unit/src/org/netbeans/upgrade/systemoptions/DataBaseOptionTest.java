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

package org.netbeans.upgrade.systemoptions;

/**
 * @author Radek Matous
 */
public class DataBaseOptionTest extends BasicTestForImport {
    public DataBaseOptionTest(String testName) {
        super(testName, "org-netbeans-modules-db-explorer-DatabaseOption.settings");
    }

    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/db");
    }
        
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
        "autoConn", "debugMode"
        });
    }

    public void testAutoConn() throws Exception {
        assertPropertyType("autoConn", "java.lang.Boolean");
        assertProperty("autoConn", "false");
    }

    public void testDebugMode() throws Exception {
        assertPropertyType("debugMode", "java.lang.Boolean");
        assertProperty("debugMode", "true");
    }        
}
