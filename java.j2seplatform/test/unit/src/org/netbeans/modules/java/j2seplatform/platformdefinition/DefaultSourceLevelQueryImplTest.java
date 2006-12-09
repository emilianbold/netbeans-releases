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
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Tomas Zezula
 */
public class DefaultSourceLevelQueryImplTest extends NbTestCase {
    
    public DefaultSourceLevelQueryImplTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        MockServices.setServices(new Class[] {JavaPlatformProviderImpl.class});
    }

    public void testGetSourceLevel() throws Exception {
        FileObject root = FileUtil.toFileObject(this.getWorkDir());
        assertNotNull ("Cannot convert File to FileObject, missing master-fs?",root);    //NOI18N
        FileObject javaFile = createTestFile (root,"test","Test.java","package test;\n class Test {}");    //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(javaFile));
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
