/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.masterfs;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.versioning.*;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class FileVCSTest extends VCSFilesystemTestFactory {
    private String workDirPath;

    public FileVCSTest(Test test) {
        super(test);
        VCSFileProxy.createFileProxy(new File("")); // init APIAccessor
    }

    @Override
    protected FileObject createFolder(String path) throws IOException {
        FileObject fo;
        File f = new File(workDirPath, path);
        if(!f.exists()) {
            f.mkdirs();
            fo = FileUtil.toFileObject(f);
        } else {
            fo = FileUtil.toFileObject(f);
        }
        return fo;
    }
    
    @Override
    protected FileObject createFile(String path) throws IOException {
        FileObject fo;
        File f = new File(workDirPath, path);
        if(!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
            fo = FileUtil.toFileObject(f);
            f.delete();
        } else {
            fo = FileUtil.toFileObject(f);
        }
        return fo;
    }

    @Override
    protected void setReadOnly(FileObject fo) throws IOException {
        File f = FileUtil.toFile(fo);
        assertNotNull(f);
        f.setReadOnly();
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(VCSOwnerCacheTestCase.class);
        suite.addTestSuite(VCSOwnerTestCase.class);
        suite.addTestSuite(VCSInterceptorTestCase.class);
        suite.addTestSuite(VCSAnnotationProviderTestCase.class);
        return new FileVCSTest(suite);
    }
    
    @Override
    protected void setUp() throws Exception {
        workDirPath = System.getProperty("work.dir");//NOI18N
        workDirPath = (workDirPath != null) ? workDirPath : System.getProperty("java.io.tmpdir");//NOI18N
        if(!workDirPath.isEmpty()) {
            File f = new File(workDirPath);
            deleteRecursively(f);
        }
    }

    @Override
    protected void tearDown() throws Exception {     
        if(!workDirPath.isEmpty()) {
            File f = new File(workDirPath);
            deleteRecursively(f);
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }    
}
