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

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;

/**
 *
 * @author Vladimir Kvashin
 */
public class DirectoryStorageTestCase extends NativeExecutionBaseTestCase {

    public DirectoryStorageTestCase(String testName) {
        super(testName);
    }

    public void testDirectoryStorage() throws Exception {
        File file = File.createTempFile("directoryStorage", ".dat");
        try {
            DirectoryStorage ds1 = new DirectoryStorage(file);
            DirEntry entry1;
            final String name = "name";
            final String cacheName = "name.cache";
            final String access = "-rwxrwxrwx";
            final String user = "vk";
            final String group = "staff";
            final int size = 1024;
            final String timestamp = "t i m e s t a m p";
            final String link = null;
            entry1 = new DirEntryImpl(name, cacheName, access, user, group, size, timestamp, link);
            ds1.testAddEntry(entry1);
            ds1.store();
            DirectoryStorage ds2 = new DirectoryStorage(file);
            ds2.load();
            DirEntry entry2 = ds2.getEntry(entry1.getName());
            assertNotNull("No entry restored for " + entry1.getName(), entry2);
            assertEquals("Name", name, entry2.getName());
            assertEquals("Cache", cacheName, entry2.getCache());
            assertEquals("Access", access.substring(1), entry2.getAccessAsString());
//            assertEquals("User", user, entry2.getUser());
//            assertEquals("Group", group, entry2.getGroup());
            assertEquals("Size", size, entry2.getSize());
            assertEquals("Timestamp", timestamp, entry2.getTimestamp());
            assertEquals("Link", link, entry2.getLinkTarget());
        } finally {
            file.delete();
        }
    }

//    public void testReadWriteAccess() throws Exception {
//
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwx------", "vk", "other", true, true, true));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwx---rwx", "vk", "other", true, true, true));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-r--------", "vk", "other", true, false, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "--w-------", "vk", "other", false, true, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "---x------", "vk", "other", false, false, true));
//
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwxrwx---", "vk", "other", true, true, true));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwxrwxrwx", "vk", "other", true, true, true));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-r--rwx---", "vk", "other", true, false, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "--w-rwx---", "vk", "other", false, true, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "---xrwx---", "vk", "other", false, false, true));
//
//
//        doTestReadWriteAccess(new TestData("vk", "staff", "-r-xrwx---", "vk", "other", true, false, true));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rw-------", "vk", "other", true, true, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rw-rwx---", "vk", "other", true, true, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-r-x------", "vk", "other", true, false, true));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-r-xrwx---", "vk", "other", true, false, true));
//
//
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwxrwx---", "xx", "other", false, false, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwxrwxr--", "xx", "other", true, false, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-------r--", "xx", "other", true, false, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwxrwxrw-", "xx", "other", true, true, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-------rw-", "xx", "other", true, true, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwxrwx-w-", "xx", "other", false, true, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "--------w-", "xx", "other", false, true, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "---------x", "xx", "other", false, false, true));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-------r-x", "xx", "other", true, false, true));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-------rwx", "xx", "other", true, true, true));
//
//
//        doTestReadWriteAccess(new TestData("vk", "staff", "----rwx---", "xx", "staff", true, true, true));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwxrwx---", "xx", "staff", true, true, true));
//        doTestReadWriteAccess(new TestData("vk", "staff", "----r-----", "xx", "staff", true, false, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-----w----", "xx", "staff", false, true, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "------x---", "xx", "staff", false, false, true));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwxr-----", "xx", "staff", true, false, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwx-w----", "xx", "staff", false, true, false));
//        doTestReadWriteAccess(new TestData("vk", "staff", "-rwx--x---", "xx", "staff", false, false, true));
//    }
//    
//    private void doTestReadWriteAccess(TestData td) throws Exception {
//        DirEntry entry;
//        entry = new DirEntryImpl("name", "name.cache", td.fileAccess, td.fileUser, td.fileGroup, 1024, "t i m e s t a m p", null);
//        assertEquals(
//                "CanRead differs for \"" + td.fileAccess + ' ' + td.fileUser + ' ' + td.fileGroup + "\" for " + td.testUser + ' ' + td.testGroup,
//                td.canRead, entry.canRead(td.testUser, td.testGroup));
//        assertEquals(
//                "CanWrite differs for \"" + td.fileAccess + ' ' + td.fileUser + ' ' + td.fileGroup + "\" for " + td.testUser + ' ' + td.testGroup,
//                td.canWrite, entry.canWrite(td.testUser, td.testGroup));
//        assertEquals(
//                "CanExecute differs for \"" + td.fileAccess + ' ' + td.fileUser + ' ' + td.fileGroup + "\" for " + td.testUser + ' ' + td.testGroup,
//                td.canExecute, entry.canExecute(td.testUser, td.testGroup));
//    }
//
//    private static class TestData {
//        public final String fileUser;
//        public final String fileGroup;
//        public final String fileAccess;
//        public final String testUser;
//        public final String testGroup;
//        public final boolean canRead;
//        public final boolean canWrite;
//        public final boolean canExecute;
//        public TestData(String fileUser, String fileGroup, String fileAccess, String testUser, String testGroup,
//                boolean canRead, boolean canWrite, boolean canExecute) {
//            this.fileUser = fileUser;
//            this.fileGroup = fileGroup;
//            this.fileAccess = fileAccess;
//            this.testUser = testUser;
//            this.testGroup = testGroup;
//            this.canRead = canRead;
//            this.canWrite = canWrite;
//            this.canExecute = canExecute;
//        }
//
//    }
}