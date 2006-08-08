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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author pzajac
 */
public class FixTestDependenciesTest extends NbTestCase {

    public FixTestDependenciesTest(String testName) {
        super(testName);
    }

    public void testSimple() throws IOException, Exception {
          File prjFile = copyFile("FixTestDependenciesProject.xml");
          File propertiesFile = copyFile("FixTestDependencies.properties");
          doFixProjectXml(propertiesFile, prjFile);
          doFixProjectXml(propertiesFile, copyFile("FixTestDependenciesProject2.xml"));

    }

    private void doFixProjectXml(final File propertiesFile, final File prjFile) throws Exception, IOException {

        PublicPackagesInProjectizedXMLTest.
                execute ("FixTestDependenciesTest.xml", new String[] {"-verbose",
                      "-Dtest.project.xml=" + prjFile.getPath(),
                      "-Dtest.properties.file=" + propertiesFile.getPath()});

        assertFile(copyFile("FixTestDependenciesProjectPass.xml"),prjFile);
        assertFile(copyFile("FixTestDependenciesPass.properties"),propertiesFile);
    }

    private File copyFile(String resourceName) throws IOException {
       InputStream is = getClass().getResourceAsStream(resourceName);
       byte buf[] = new byte[10000];
       File retFile = new File(getWorkDir(),resourceName);
       FileOutputStream fos = new FileOutputStream(retFile);
       int size;
       while ((size = is.read(buf)) > 0 ) {
           fos.write(buf,0,size);
       }
       is.close();
       fos.close();
       return retFile;
    }

}
