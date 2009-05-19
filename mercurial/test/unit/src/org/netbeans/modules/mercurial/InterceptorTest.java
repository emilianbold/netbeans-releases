/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.mercurial;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class InterceptorTest extends AbstractHgTest {

    public InterceptorTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // create
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        System.setProperty("netbeans.user", "/tmp/hgtest");
    }

    public void testGetAttributeRefreh() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        FileObject fo = FileUtil.toFileObject(file);
        Runnable attr = (Runnable) fo.getAttribute("ProvidedExtensions.Refresh");
        assertNotNull(attr);

        attr.run();
        // XXX check status
    }

    public void testGetAttributeWrong() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        FileObject fo = FileUtil.toFileObject(file);
        String attr = (String) fo.getAttribute("peek-a-boo");
        assertNull(attr);
    }

    public void testGetAttributeNotCloned() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        FileObject fo = FileUtil.toFileObject(file);
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNull(attr);
    }

    public void testGetAttributeClonedRoot() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkDir());

        FileObject fo = FileUtil.toFileObject(cloned);
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(getWorkDir().getAbsolutePath(), attr);
    }

    public void testGetAttributeCloned() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkDir());

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(getWorkDir().getAbsolutePath(), attr);
    }

    public void testGetAttributeClonedOnlyPush() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkDir());

        String defaultPush = "http://a.repository.far.far/away";
        new HgConfigFiles(cloned).removeProperty(HgConfigFiles.HG_PATHS_SECTION, HgConfigFiles.HG_DEFAULT_PULL);
        new HgConfigFiles(cloned).removeProperty(HgConfigFiles.HG_PATHS_SECTION, HgConfigFiles.HG_DEFAULT_PULL_VALUE);
        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PUSH, defaultPush);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPush, attr);
    }

    public void testGetAttributeClonedPull() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkDir());

        String defaultPull = "http://a.repository.far.far/away";
        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PULL, defaultPull);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPull, attr);
    }

    public void testGetAttributeClonedPullWithCredentials() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkDir());

        String defaultPull = "http://so:secure@a.repository.far.far/away";
        String defaultPullReturned = "http://a.repository.far.far/away";

        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PULL, defaultPull);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPullReturned, attr);
    }

}
