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

package org.netbeans.modules.projectimport.eclipse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;

/**
 * @author Martin Krauskopf
 */
public final class WorkspaceParserTest extends NbTestCase {
    
    public WorkspaceParserTest(String testName) {
        super(testName);
    }
    
    public void testGetLocation() throws Exception {
        String tempFilePath = new File(System.getProperty("java.io.tmpdir"), "tmp").getAbsolutePath();
        System.out.println(tempFilePath);
        assertRightPath(tempFilePath, tempFilePath);
        assertRightPath("URI//file:" + tempFilePath, tempFilePath);
        assertRightPath("URI//whatever:" + tempFilePath, tempFilePath); 
    }
    
    private void assertRightPath(final String rawPath, final String expectedPath) throws IOException {
        byte[] pathB = rawPath.getBytes();
        byte[] locationContent = new byte[18 + pathB.length];
        locationContent[17] = (byte) pathB.length;
        System.arraycopy(pathB, 0, locationContent, 18, pathB.length);
        ByteArrayInputStream bis = new ByteArrayInputStream(locationContent);
        assertEquals("right path", expectedPath, WorkspaceParser.getLocation(bis).getAbsolutePath());
    }
    
}
