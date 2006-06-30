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

package org.netbeans.modules.java.freeform;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.ant.freeform.TestBase;
import org.openide.filesystems.FileUtil;

/**
 * Tests Javadoc reporting.
 * @author Jesse Glick
 */
public class JavadocQueryTest extends TestBase {

    public JavadocQueryTest(String name) {
        super(name);
    }

    private URL classes1Dir, classes1Jar, classes2Dir, javadoc1Dir, javadoc2Zip;
    
    protected void setUp() throws Exception {
        super.setUp();
        classes1Dir = asDir("classes1");
        classes1Jar = asJar("classes1.jar");
        classes2Dir = asDir("classes2");
        javadoc1Dir = asDir("javadoc1");
        javadoc2Zip = asJar("javadoc2.zip");
    }
    
    private URL asDir(String path) throws Exception {
        URL u = simple2.helper().resolveFile(path).toURI().toURL();
        String us = u.toExternalForm();
        if (us.endsWith("/")) {
            return u;
        } else {
            return new URL(us + "/");
        }
    }
    
    private URL asJar(String path) throws Exception {
        return FileUtil.getArchiveRoot(simple2.helper().resolveFile(path).toURI().toURL());
    }
    
    private List/*<URL>*/ javadocFor(URL binary) {
        return Arrays.asList(JavadocForBinaryQuery.findJavadoc(binary).getRoots());
    }
    
    public void testFindJavadoc() throws Exception {
        List/*<URL>*/ both = Arrays.asList(new URL[] {javadoc1Dir, javadoc2Zip});
        assertEquals("both Javadoc found for " + classes1Dir, both, javadocFor(classes1Dir));
        assertEquals("both Javadoc found for " + classes1Jar, both, javadocFor(classes1Jar));
        assertEquals("no Javadoc found for " + classes2Dir, Collections.EMPTY_LIST, javadocFor(classes2Dir));
    }

    // XXX testChangeFiring?
    
}
