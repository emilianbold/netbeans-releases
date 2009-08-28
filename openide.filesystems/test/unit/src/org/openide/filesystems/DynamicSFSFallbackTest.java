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

package org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

/** Test layering of filesystems installed via lookup.
 *
 * @author Jaroslav Tulach
 */
public class DynamicSFSFallbackTest extends NbTestCase {
    public DynamicSFSFallbackTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        MyFS1.dir = new File(getWorkDir(), "dir1");
        MyFS2.dir = new File(getWorkDir(), "dir2");
        MockServices.setServices(MyFS1.class, MyFS2.class);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public static final class MyFS1 extends LocalFileSystem {
        static File dir;

        public MyFS1() throws Exception {
            dir.mkdirs();
            setRootDirectory(dir);
            getRoot().setAttribute("fallback", Boolean.TRUE);

            FileObject fo1 = FileUtil.createData(getRoot(), "test/data.txt");
            fo1.setAttribute("one", 1);
            write(fo1, "fileone");
            FileObject fo11 = FileUtil.createData(getRoot(), "test-fs-is-there.txt");
            write(fo11, "hereIam");
        }
    }
    public static final class MyFS2 extends LocalFileSystem {
        static File dir;

        public MyFS2() throws Exception {
            dir.mkdirs();
            setRootDirectory(dir);
            FileObject fo1 = FileUtil.createData(getRoot(), "test/data.txt");
            fo1.setAttribute("two", 1);
            write(fo1, "two");
        }
    }

    public void testDynamicSystemsCanAlsoBeBehindLayers() throws Exception {
        FileObject global = FileUtil.getConfigFile("test/data.txt");
        assertEquals("Second file system takes preceedence", "two", global.asText());
        assertTrue("Still valid", global.isValid());

        FileObject fo = FileUtil.getConfigFile("test-fs-is-there.txt");
        assertNotNull("File found: " + Arrays.toString(FileUtil.getConfigRoot().getChildren()), fo);
        assertEquals("Text is correct", "hereIam", fo.asText());
    }
    
    private static void write(FileObject fo, String txt) throws IOException {
        OutputStream os = fo.getOutputStream();
        os.write(txt.getBytes());
        os.close();
    }
    
}
