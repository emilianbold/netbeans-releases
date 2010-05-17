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
package org.netbeans.modules.python.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.python.api.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class PythonUtilsTest extends PythonTestBase {

    public PythonUtilsTest(String name) {
        super(name);
    }

    public void testGetOffsetByLineCol() throws Exception {
        assertEquals(5, PythonUtils.getOffsetByLineCol("fooooooooo\nHello\n", 0, 5));
    }

    public void testGetOffsetByLineCol2() throws Exception {
        assertEquals(5, PythonUtils.getOffsetByLineCol("fooooooooo", 0, 5));
    }

    public void testGetOffsetByLineCol3() throws Exception {
        assertEquals(2, PythonUtils.getOffsetByLineCol("o\nHello\n", 1, 0));
    }

    public void testGetOffsetByLineCol4() throws Exception {
        assertEquals(4, PythonUtils.getOffsetByLineCol("o\nHello\n", 1, 2));
    }

    public void testGetOffsetByLineCol5() throws Exception {
        assertEquals(8, PythonUtils.getOffsetByLineCol("o\nHello\n", 2, 0));
    }

    public void testGetOffsetByLineCol6() throws Exception {
        assertEquals(8, PythonUtils.getOffsetByLineCol("o\nHello\n", 50, 0));
    }

    public void testGetOffsetByLineCol7() throws Exception {
        assertEquals(2, PythonUtils.getOffsetByLineCol("o\nHello\n", 1, -1));
    }
    public void checkRoots(FileObject[] expected, FileObject[] path) throws Exception {
        List<FileObject> pythonPath = Arrays.asList(path);

        List<FileObject> roots = Util.findUniqueRoots(pythonPath);
        List<String> list = new ArrayList<String>();
        for (FileObject root : roots) {
            list.add(FileUtil.getFileDisplayName(root));
        }
        Collections.sort(list);
        List<FileObject> expectedList = Arrays.asList(expected);
        Collections.sort(list);

        List<String> elist = new ArrayList<String>();
        for (FileObject root : expectedList) {
            elist.add(FileUtil.getFileDisplayName(root));
        }
        Collections.sort(elist);

        assertEquals(elist.toString(), list.toString());
    }

    public void testFindUniqueRoots() throws Exception {
        FileObject path1 = getTestFile("testfiles/package/subpackage1");
        FileObject path2 = getTestFile("testfiles/package/subpackage2");
        FileObject path3 = getTestFile("testfiles/toppkg");
        FileObject path4 = getTestFile("testfiles/package");
        FileObject path5 = getTestFile("testfiles/");
        FileObject path6 = getTestFile("testfiles/toppkg/medpkg/lowpkg");

        checkRoots(
                new FileObject[]{path5},
                new FileObject[]{path1, path2, path3, path4, path5, path6});

        checkRoots(
                new FileObject[]{path4, path3},
                new FileObject[]{path6, path3, path4});

        checkRoots(
                new FileObject[]{path1, path2},
                new FileObject[]{path1, path2});

        checkRoots(
                new FileObject[]{path6},
                new FileObject[]{path6});

        checkRoots(
                new FileObject[]{path1, path3},
                new FileObject[]{path1, path3, path6});
    }
}
