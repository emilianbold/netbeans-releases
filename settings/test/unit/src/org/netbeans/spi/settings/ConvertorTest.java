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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.settings;

import java.io.*;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author  Jan Pokorsky
 */
public class ConvertorTest extends NbTestCase {

    FileSystem fs;
    FileObject contextFO;

    /** Creates a new instance of EnvTest */
    public ConvertorTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File work = getWorkDir();
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(work);
        contextFO = lfs.getRoot().createData("context", "settings");
        fs = lfs;
    }
    
    public void testFindWriterContext() throws Exception {
        Reader r = new java.io.InputStreamReader(contextFO.getInputStream());
        Reader cr = org.netbeans.modules.settings.ContextProvider.createReaderContextProvider(r, contextFO);
        Lookup l = Convertor.findContext(cr);
        assertNotNull(l);
        FileObject src = (FileObject) l.lookup(FileObject.class);
        assertNotNull(src);
        assertEquals(contextFO.getPath(), src.getPath());
    }
    
    public void testFindReaderContext() throws Exception {
        org.openide.filesystems.FileLock lock = contextFO.lock();
        try {
            Writer w = new java.io.OutputStreamWriter(contextFO.getOutputStream(lock));
            Writer cw = org.netbeans.modules.settings.ContextProvider.createWriterContextProvider(w, contextFO);
            Lookup l = Convertor.findContext(cw);
            assertNotNull(l);
            FileObject src = (FileObject) l.lookup(FileObject.class);
            assertNotNull(src);
            assertEquals(contextFO.getPath(), src.getPath());
        } finally {
            lock.releaseLock();
        }
    }
    
}
