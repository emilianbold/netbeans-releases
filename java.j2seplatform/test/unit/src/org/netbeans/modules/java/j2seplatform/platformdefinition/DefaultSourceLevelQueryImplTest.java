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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import junit.framework.*;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.core.startup.layers.ArchiveURLMapper;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.MasterURLMapper;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Zezula
 */
public class DefaultSourceLevelQueryImplTest extends NbTestCase implements Lookup.Provider {
    
    private Lookup lookup;
    
    public DefaultSourceLevelQueryImplTest(String testName) {
        super(testName);
        TestUtil.setLookup(Lookups.proxy(this));
    }

    protected void setUp() throws Exception {
        this.clearWorkDir();
    }

    protected void tearDown() throws Exception {
    }

    public void testGetSourceLevel() throws Exception {
        JavaPlatform jp = JavaPlatformManager.getDefault().getDefaultPlatform();
        assertNotNull ("Default platform is null",jp);  //NOI18N
        FileObject root = FileUtil.toFileObject(this.getWorkDir());
        assertNotNull ("Cannot convert File to FileObject, missing master-fs?",root);    //NOI18N
        FileObject javaFile = createTestFile (root,"test","Test.java","package test;\n class Test {}");    //NOI18N
        DefaultSourceLevelQueryImpl instance = new DefaultSourceLevelQueryImpl();        
        String result = instance.getSourceLevel(javaFile);
        assertEquals("DefaultSourceLevelQueryImpl returned wrong source level",jp.getSpecification().getVersion().toString(), result);  //NOI18N
    }        
    
    public Lookup getLookup () {
        if (this.lookup == null) {
            this.lookup = Lookups.fixed (
                new Object[] {
                    new ArchiveURLMapper (),
                    new MasterURLMapper(),
                    new JavaPlatformProviderImpl(),
                });
        }
        return this.lookup;
    }
    
    private FileObject createTestFile (FileObject root, String path, String fileName, String content) throws IOException {
        FileObject pkg = FileUtil.createFolder(root, path);
        assertNotNull (pkg);
        FileObject data = pkg.createData(fileName);
        FileLock lock = data.lock();
        try {
            PrintWriter out = new PrintWriter (new OutputStreamWriter (data.getOutputStream(lock)));
            try {
                out.println (content);
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        return data;
    }
    
}
