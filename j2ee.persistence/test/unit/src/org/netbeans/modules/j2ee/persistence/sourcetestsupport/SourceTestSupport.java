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
package org.netbeans.modules.j2ee.persistence.sourcetestsupport;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 * A base class for unit tests.
 *
 * @author Erno Mononen
 */
public abstract class SourceTestSupport extends NbTestCase {
    
    public SourceTestSupport(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(FakeJavaDataLoaderPool.class, RepositoryImpl.class, ClassPathProviderImpl.class);
        clearWorkDir();
        initTemplates();
    }
    protected void tearDown() throws Exception{
        super.tearDown();
        getSystemFs().reset();
    }
    
    private RepositoryImpl.MultiFileSystemImpl getSystemFs(){
        return (RepositoryImpl.MultiFileSystemImpl)Repository.getDefault().getDefaultFileSystem();
    }
    
    private void initTemplates() throws Exception{
        RepositoryImpl.MultiFileSystemImpl systemFS = getSystemFs();
        FileObject interfaceTemplate = systemFS.getRoot().getFileObject("Templates/Classes/Interface.java");
        copyStringToFileObject(interfaceTemplate,
                "package Templates.Classes;" +
                "public interface Interface {\n" +
                "}");
        FileObject classTemplate = systemFS.getRoot().getFileObject("Templates/Classes/Class.java");
        copyStringToFileObject(classTemplate,
                "package Templates.Classes;" +
                "public class Class {\n" +
                "}");
    }
    
    protected void assertFile(FileObject result){
        assertFile( getGoldenFile(), FileUtil.toFile(result));
    }
    
    // temporary methods for debugging
    
    protected void print(FileObject fo) throws IOException {
        print(FileUtil.toFile(fo));
    }
    
    protected void print(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }
    
    protected FileObject copyStringToFileObject(FileObject fo, String content) throws Exception {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
            FileUtil.copy(is, os);
            return fo;
        } finally {
            os.close();
        }
    }
    
}