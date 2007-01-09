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

package org.netbeans.modules.java.platform.queries;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipOutputStream;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author Tomas Zezula
 */
public class PlatformSourceForBinaryQueryTest extends NbTestCase {
    
    public PlatformSourceForBinaryQueryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {        
        super.setUp();        
        clearWorkDir();
    }
    
    
    public void testUnregisteredPlatform() throws Exception {        
        File wd = getWorkDir();
        FileObject wdo = FileUtil.toFileObject(wd);
        assertNotNull(wdo);
        FileObject p1 = wdo.createFolder("platform1");
        FileObject fo = p1.createFolder("jre");
        fo = fo.createFolder("lib");
        FileObject rt1 = fo.createData("rt.jar");
        FileObject src1 = FileUtil.getArchiveRoot(createSrcZip (p1));  
        
        FileObject p2 = wdo.createFolder("platform2");
        fo = p2.createFolder("jre");
        fo = fo.createFolder("lib");
        FileObject rt2 = fo.createData("rt.jar");        
        
        PlatformSourceForBinaryQuery q = new PlatformSourceForBinaryQuery ();
        
        SourceForBinaryQuery.Result result = q.findSourceRoots(FileUtil.getArchiveRoot(rt1.getURL()));
        assertEquals(1, result.getRoots().length);
        assertEquals(src1, result.getRoots()[0]);
        
        result = q.findSourceRoots(FileUtil.getArchiveRoot(rt2.getURL()));
        assertNull(result);
        
    }
    
    private static FileObject createSrcZip (FileObject pf) throws Exception {
        File f = new File (FileUtil.toFile(pf),"src.zip");
        ZipOutputStream zf = new ZipOutputStream (new FileOutputStream(f));
        ZipEntry e = new ZipEntry ("Test.java");
        zf.putNextEntry(e);
        zf.write("class Test {}".getBytes());
        zf.close();
        return FileUtil.toFileObject(f);
    }
    
}
