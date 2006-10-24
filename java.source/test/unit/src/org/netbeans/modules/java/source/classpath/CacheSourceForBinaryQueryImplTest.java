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

package org.netbeans.modules.java.source.classpath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import junit.framework.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class CacheSourceForBinaryQueryImplTest extends NbTestCase {
    
    FileObject[] srcRoots;
    ClasspathInfo cpInfo;
    CacheSourceForBinaryQueryImpl sfbq;
    
    public CacheSourceForBinaryQueryImplTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        this.clearWorkDir();
        File fwd = this.getWorkDir();
        FileObject wd = FileUtil.toFileObject(fwd);
        assertNotNull(wd);
        File cacheFolder = new File (fwd,"cache");  //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder (cacheFolder);
        this.srcRoots = new FileObject [2];
        this.srcRoots[0] = wd.createFolder("src1"); //NOI18N
        this.srcRoots[1] = wd.createFolder("src2"); //NOI18N
        ClassPath bootPath = ClassPathSupport.createClassPath(new URL[0]);
        ClassPath compilePath = ClassPathSupport.createClassPath(new URL[0]);
        ClassPath srcPath = ClassPathSupport.createClassPath(srcRoots);
        this.cpInfo = ClasspathInfo.create(bootPath,compilePath,srcPath);
        this.sfbq = new CacheSourceForBinaryQueryImpl ();
    }

    protected @Override void tearDown() throws Exception {
        this.cpInfo = null;
    }

    public void testFindSourceRoots() throws Exception {
        ClassPath outCp = this.cpInfo.getClassPath(ClasspathInfo.PathKind.OUTPUT);        
        assertNotNull(outCp);
        assertEquals(srcRoots.length,outCp.entries().size());
        Iterator<ClassPath.Entry> it = ((List<ClassPath.Entry>)outCp.entries()).iterator();
        for (int i=0; it.hasNext(); i++) {
            ClassPath.Entry entry = it.next();
            URL url = entry.getURL();
            SourceForBinaryQuery.Result result = this.sfbq.findSourceRoots(url);
            FileObject[] sourceRoots = result.getRoots();
            assertNotNull(sourceRoots);
            assertEquals(1,sourceRoots.length);
            assertEquals(srcRoots[i],sourceRoots[0]);
        }
    }
    
}
