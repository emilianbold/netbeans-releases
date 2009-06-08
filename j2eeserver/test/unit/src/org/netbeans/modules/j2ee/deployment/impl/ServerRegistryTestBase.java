/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;

/**
 *
 * @author Pavel Buzek
 */
public class ServerRegistryTestBase extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
    }

    protected ServerRegistryTestBase (String name) {
        super (name);
    }

    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            super(new Lookup[] {
                Lookups.fixed(new Object[] {"repo"}, new Conv()),
                Lookups.metaInfServices(Lkp.class.getClassLoader()),
                Lookups.singleton(Lkp.class.getClassLoader()),
            });
        }
        private static final class Conv implements InstanceContent.Convertor {
            public Conv() {}
            public Object convert(Object obj) {
                assert obj == "repo";
                try {
                    return new Repo();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            public String displayName(Object obj) {
                return obj.toString();
            }
            public String id(Object obj) {
                return obj.toString();
            }
            public Class type(Object obj) {
                assert obj == "repo";
                return Repository.class;
            }
        }
    }
    private File scratchF;

    private void mkdir(String path) {
//        System.out.println ("mkdir:"+path);
        new File(scratchF, path.replace('/', File.separatorChar)).mkdirs();
    }
    protected boolean runInEQ() {
        return true;
    }
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        scratchF = getWorkDir();
        mkdir("system/J2EE/InstalledServers");
        mkdir("system/J2EE/DeploymentPlugins");
        System.setProperty("SYSTEMDIR", new File(scratchF, "system").getAbsolutePath());
        FileObject sfs = FileUtil.getConfigRoot();
        assertNotNull("no default FS", sfs);
        FileObject j2eeFolder = sfs.getFileObject("J2EE");
        assertNotNull("have J2EE", j2eeFolder);
//        Enumeration enumDest = sfs.getChildren (true);
//        while (enumDest.hasMoreElements()) {
//            FileObject f = (FileObject) enumDest.nextElement();
//            System.out.println ("    dest file:" + f.getPath ());
//        }
    }

    protected static File getProjectAsFile(NbTestCase test, String projectFolderName) throws IOException {
        File f = new File(test.getDataDir(), projectFolderName);
        if (!f.exists()) {
            // maybe it's zipped
            File archive = new File(test.getDataDir(), projectFolderName + ".zip");
            if (archive.exists() && archive.isFile()) {
                unZip(archive, test.getWorkDir());
                f = new File(test.getWorkDir(), projectFolderName);
            }
        }
        NbTestCase.assertTrue("project directory has to exists: " + f, f.exists());
        FileUtil.refreshFor(test.getDataDir());
        return f;
    }

    private static void unZip(File archive, File destination) throws IOException {
        if (!archive.exists()) {
            throw new FileNotFoundException(archive + " does not exist.");
        }
        ZipFile zipFile = new ZipFile(archive);
        Enumeration<? extends ZipEntry> all = zipFile.entries();
        while (all.hasMoreElements()) {
            extractFile(zipFile, all.nextElement(), destination);
        }
    }

    private static void extractFile(ZipFile zipFile, ZipEntry e, File destination) throws IOException {
        String zipName = e.getName();
        if (zipName.startsWith("/")) {
            zipName = zipName.substring(1);
        }
        if (zipName.endsWith("/")) {
            return;
        }
        int ix = zipName.lastIndexOf('/');
        if (ix > 0) {
            String dirName = zipName.substring(0, ix);
            File d = new File(destination, dirName);
            if (!(d.exists() && d.isDirectory())) {
                if (!d.mkdirs()) {
                    NbTestCase.fail("Warning: unable to mkdir " + dirName);
                }
            }
        }
        FileOutputStream os = new FileOutputStream(destination.getAbsolutePath() + "/" + zipName);
        InputStream is = zipFile.getInputStream(e);
        int n = 0;
        byte[] buff = new byte[8192];
        while ((n = is.read(buff)) > 0) {
            os.write(buff, 0, n);
        }
        is.close();
        os.close();
    }

     private static final class Repo extends Repository {

        public Repo() throws Exception {
            super(mksystem());
        }

        private static FileSystem mksystem() throws Exception {
            LocalFileSystem lfs = new LocalFileSystem();
            lfs.setRootDirectory(new File(System.getProperty("SYSTEMDIR")));
            java.net.URL layerFile = Repo.class.getClassLoader().getResource ("org/netbeans/tests/j2eeserver/plugin/layer.xml");
            assert layerFile != null;
            XMLFileSystem layer = new XMLFileSystem (layerFile);
            FileSystem layers [] = new FileSystem [] {lfs, layer};
            MultiFileSystem mfs = new MultiFileSystem (layers);
            return mfs;
        }

    }

}
