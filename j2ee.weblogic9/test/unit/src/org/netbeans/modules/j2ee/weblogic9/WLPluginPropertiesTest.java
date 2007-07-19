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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.weblogic9;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;


/**
 *
 * @author sherold
 */
public class WLPluginPropertiesTest extends NbTestCase {
    
    public WLPluginPropertiesTest(String testName) {
        super(testName);
    }
    
    public void testIsSupportedVersion() throws Exception {
        File baseFolder = getWorkDir();
        File libFolder = new File(baseFolder, "server/lib");
        libFolder.mkdirs();
        File file = new File(libFolder, "weblogic.jar");
        createJar(file, "Implementation-Version: 10.0.0.0");
        assertTrue(WLPluginProperties.isSupportedVersion(baseFolder));
        createJar(file, "Implementation-Version: 9.0.0.0");
        assertTrue(WLPluginProperties.isSupportedVersion(baseFolder));
        createJar(file, "Implementation-Version: 8.0.0.0");
        assertFalse(WLPluginProperties.isSupportedVersion(baseFolder));
        createJar(file, "Missing-Implementation-Version: 10.0.0.0");
        assertFalse(WLPluginProperties.isSupportedVersion(baseFolder));
    }
    
    private void createJar(File file, String manifestLine) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Manifest-Version: 1.0\n");
        stringBuilder.append(manifestLine + "\n");
        InputStream is = new ByteArrayInputStream(stringBuilder.toString().getBytes("UTF-8"));
        try {
            new JarOutputStream(new FileOutputStream(file), new Manifest(is)).close();
        } finally {
            is.close();
        }
    }

}
