/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.api;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.client.tools.api.JSLocation;
import org.netbeans.modules.web.client.tools.api.NbJSLocation;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSURILocation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author quynguyen
 */
public class IdentityURLMapperTest extends NbTestCase {
    private static final String DOCUMENT_ROOT = "web";
    private static final String URL_BASE = "http://localhost:8080/WebApplication01";
    private static final int DEFAULT_LINE_NO = 37;
    
    private File baseDir;
    private String urlBase;
    
    public IdentityURLMapperTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        baseDir = new File(getWorkDir(), DOCUMENT_ROOT);
        baseDir.mkdirs();
        
        FileObject baseFO = FileUtil.toFileObject(baseDir);
        assertNotNull("Could not convert file " + baseDir.getAbsolutePath() + " to FileObject",baseFO);
        
        urlBase = URL_BASE;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        FileUtil.toFileObject(baseDir).delete();
    }

    private FileObject createFileObject(File dir, String relativePath) throws IOException {
        File newFile = new File(dir, relativePath);
        return FileUtil.createData(newFile);
    }
    
//    /**
//     * Test of getJSLocation method, of class IdentityURLMapper.
//     */
//    public void testGetJSLocation() throws IOException {
//        System.out.println("getJSLocation");
//
//        String welcomeRoot = "/index.html";
//        FileObject baseFO = FileUtil.toFileObject(baseDir);
//
//        IdentityURLMapper mapper = new IdentityURLMapper(urlBase, baseFO, welcomeRoot);
//
//        String[] filenames = { "index.html", "script.js", "data/index2.html", "data/script2.js" };
//
//        for (String filename : filenames) {
//            FileObject fo;
//
//            if (filename.length() > 0) {
//                fo = createFileObject(baseDir, filename);
//            }else {
//                fo = FileUtil.toFileObject(baseDir);
//            }
//
//            NbJSLocation foLocation = new NbJSFileObjectLocation(fo, DEFAULT_LINE_NO);
//            JSLocation result = mapper.getJSLocation(foLocation, null);
//            assertNotNull("URL Mapper could not find mapping for FileObject: " + FileUtil.getFileDisplayName(fo), result);
//        }
//    }
//
//    /**
//     * Test of getNbJSLocation method, of class IdentityURLMapper.
//     */
//    public void testGetNbJSLocation() throws IOException, URISyntaxException {
//        System.out.println("getNbJSLocation");
//
//        String welcomeRoot = "/index.html";
//        FileObject baseFO = FileUtil.toFileObject(baseDir);
//
//        createFileObject(baseDir, "/index.html");
//        IdentityURLMapper mapper = new IdentityURLMapper(urlBase, baseFO, welcomeRoot);
//        String[] filenames = { "index.html", "script.js", "data/index2.html", "data/script2.js", "" };
//
//        for (String filename : filenames) {
//            FileObject fo;
//            if (filename.length() > 0) {
//                fo = createFileObject(baseDir, filename);
//            }else {
//                fo = createFileObject(baseDir, welcomeRoot);
//            }
//
//
//            String urlSpec = urlBase + "/" + filename;
//            URL newUrl = new URL(urlSpec);
//            JSLocation location = new JSURILocation(newUrl.toURI(), DEFAULT_LINE_NO);
//
//            NbJSLocation result = mapper.getNbJSLocation(location, null);
//            assertNotNull("URL Mapper could not find mapping for URL: " + newUrl.toExternalForm(), result);
//        }
//    }
//
//    /**
//     * Test of fileObjectToUrl method, of class IdentityURLMapper.
//     */
//    public void testFileObjectToUrl() throws IOException {
//        System.out.println("fileObjectToUrl");
//
//        String welcomeRoot = "/index.html";
//        FileObject baseFO = FileUtil.toFileObject(baseDir);
//
//        IdentityURLMapper mapper = new IdentityURLMapper(urlBase, baseFO, welcomeRoot);
//        String[] filenames = { "index.html", "script.js", "data/index2.html", "data/script2.js"};
//
//        for (String filename : filenames) {
//            FileObject fo = createFileObject(baseDir, filename);
//            URL result = mapper.fileObjectToUrl(fo);
//
//            assertNotNull("Mapper returned null when URL was expected", result);
//            assertEquals("Unexpected result: " + result.toExternalForm(), result.toExternalForm(), urlBase + "/" + filename);
//        }
//    }
//
//    /**
//     * Test of urlToFO method, of class IdentityURLMapper.
//     */
//    public void testUrlToFO() throws IOException {
//        System.out.println("urlToFO");
//
//        String welcomeRoot = "/index.html";
//        FileObject baseFO = FileUtil.toFileObject(baseDir);
//
//        IdentityURLMapper mapper = new IdentityURLMapper(urlBase, baseFO, welcomeRoot);
//        String[] filenames = { "index.html", "script.js", "data/index2.html", "data/script2.js"};
//
//        for (String filename : filenames) {
//            FileObject fo = createFileObject(baseDir, filename);
//
//            String urlSpec = urlBase + "/" + filename;
//            URL newUrl = new URL(urlSpec);
//
//            FileObject result = mapper.urlToFO(newUrl);
//
//            assertNotNull("Could not find FileObject for URL : " + urlSpec, result);
//            assertEquals("File path does not match input: " + urlSpec, fo, result);
//        }
//    }
//
//    /**
//     * Test of hasSupportedMIMEType method, of class IdentityURLMapper.
//     */
//    public void testHasSupportedMIMEType() throws IOException {
//        System.out.println("hasSupportedMIMEType");
//        String welcomeRoot = "/index.html";
//        FileObject baseFO = FileUtil.toFileObject(baseDir);
//
//        IdentityURLMapper mapper = new IdentityURLMapper(urlBase, baseFO, welcomeRoot);
//        String[] filenames = { "index.html", "script.js", "data/index2.html", "data/script2.js"};
//
//        for (String filename : filenames) {
//            FileObject fo = createFileObject(baseDir, filename);
//            assertTrue("Rejected MIME type for supported file: " +
//                    FileUtil.getFileDisplayName(fo) + " = " + fo.getMIMEType(), mapper.hasSupportedMIMEType(fo));
//        }
//    }
}
