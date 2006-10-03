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

package org.netbeans.api.java.queries;

import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Jesse Glick
 */
public class SourceLevelQueryTest extends NbTestCase {

    public SourceLevelQueryTest(String n) {
        super(n);
    }

    private static String LEVEL;
    private FileObject f;

    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(SLQ.class);
        LEVEL = null;
        f = FileUtil.createMemoryFileSystem().getRoot();
    }

    public void testBasicUsage() throws Exception {
        assertNull(SourceLevelQuery.getSourceLevel(f));
        LEVEL = "1.3";
        assertEquals("1.3", SourceLevelQuery.getSourceLevel(f));
        LEVEL = "1.5";
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(f));
        MockServices.setServices();
        assertNull(SourceLevelQuery.getSourceLevel(f));
    }

    public void testRobustness() throws Exception {
        // #83994: should only return well-formed source levels.
        LEVEL = "${default.javac.source}";
        assertNull(SourceLevelQuery.getSourceLevel(f));
    }

    public static final class SLQ implements SourceLevelQueryImplementation {

        public SLQ() {}

        public String getSourceLevel(FileObject javaFile) {
            return LEVEL;
        }

    }

}
