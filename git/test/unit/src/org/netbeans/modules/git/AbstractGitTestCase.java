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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitClientFactory;
import org.netbeans.libs.git.GitException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author ondra
 */
public abstract class AbstractGitTestCase extends NbTestCase {

    protected File repositoryLocation;

    public AbstractGitTestCase (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDir().getParentFile().getAbsolutePath());
        super.setUp();
        repositoryLocation = new File(getWorkDir(), "work");
        clearWorkDir();
        getClient(repositoryLocation).init();
        File repositoryMetadata = new File(repositoryLocation, ".git");
        assertTrue(repositoryMetadata.exists());
    }
    
    protected File createFolder(String name) throws IOException {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject folder = wd.createFolder(name);
        return FileUtil.toFile(folder);
    }

    protected File createFolder(File parent, String name) throws IOException {
        FileObject parentFO = FileUtil.toFileObject(parent);
        FileObject folder = parentFO.createFolder(name);
        return FileUtil.toFile(folder);
    }

    protected File createFile(File parent, String name) throws IOException {
        FileObject parentFO = FileUtil.toFileObject(parent);
        FileObject fo = parentFO.createData(name);
        return FileUtil.toFile(fo);
    }

    protected File createFile(String name) throws IOException {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject fo = wd.createData(name);
        return FileUtil.toFile(fo);
    }

    protected void write(File file, String str) throws IOException {
        FileWriter w = null;
        try {
            w = new FileWriter(file);
            w.write(str);
            w.flush();
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    protected FileStatusCache getCache () {
        return Git.getInstance().getFileStatusCache();
    }

    protected GitClient getClient (File repositoryLocation) throws GitException {
        return GitClientFactory.getInstance(null).getClient(repositoryLocation);
    }
}
