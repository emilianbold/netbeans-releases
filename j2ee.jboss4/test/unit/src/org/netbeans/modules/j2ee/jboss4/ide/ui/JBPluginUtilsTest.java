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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class JBPluginUtilsTest extends NbTestCase {

    public JBPluginUtilsTest(String testName) {
        super(testName);
    }

    public void testVersion() {
        JBPluginUtils.Version version = new JBPluginUtils.Version("4.1.1.update"); // NOI18N
        assertEquals("4", version.getMajorNumber()); // NOI18N
        assertEquals("1", version.getMinorNumber()); // NOI18N
        assertEquals("1", version.getMicroNumber()); // NOI18N
        assertEquals("update", version.getUpdate()); // NOI18N

        JBPluginUtils.Version versionCmp1 = new JBPluginUtils.Version("4.1.1.update"); // NOI18N
        assertEquals(version, versionCmp1);
        assertEquals(0, version.compareTo(versionCmp1));
        assertEquals(0, version.compareToIgnoreUpdate(versionCmp1));
        assertEquals(version.hashCode(), versionCmp1.hashCode());

        JBPluginUtils.Version versionCmp2 = new JBPluginUtils.Version("4.1.1"); // NOI18N
        assertTrue(version.compareTo(versionCmp2) > 0);
        assertEquals(0, version.compareToIgnoreUpdate(versionCmp2));
    }
}
