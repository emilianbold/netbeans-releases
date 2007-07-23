/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning.spi;

import junit.framework.TestCase;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;

import java.io.File;
import java.util.*;

/**
 * Versioning SPI unit tests of VCSAnnotator.
 * 
 * @author Maros Sandor
 */
public class VCSAnnotatorTest extends TestCase {
    
    private File dataRootDir;

    public VCSAnnotatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = new File(System.getProperty("data.root.dir"));
    }

    public void testAnnotator() throws FileStateInvalidException {
        FileObject fo = FileUtil.toFileObject(dataRootDir);
        FileSystem fs = fo.getFileSystem();
        FileSystem.Status status = fs.getStatus();
        
        Set<FileObject> sof = new HashSet<FileObject>();
        sof.add(fo);
        String annotatedName = status.annotateName("xxx", sof);
        assertEquals(annotatedName, "xxx");

        annotatedName = status.annotateName("annotate-me", sof);
        assertEquals(annotatedName, "annotate-me");
    }
}
