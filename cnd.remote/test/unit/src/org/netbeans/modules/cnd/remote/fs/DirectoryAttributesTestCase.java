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

package org.netbeans.modules.cnd.remote.fs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.test.CndBaseTestCase;

/**
 *
 * @author Vladimir Kvashin
 */
public class DirectoryAttributesTestCase extends CndBaseTestCase {

    public DirectoryAttributesTestCase(String testName) {
        super(testName);
    }

    public void testDirectoryAttributesLoad() throws Exception {
        File file = File.createTempFile("test-directory-attributes-load", ".dat");
        try {
            DirectoryAttributes attrs = new DirectoryAttributes(file);
            AtomicReference<Exception> exception = new AtomicReference<Exception>();

            try {
                attrs.load();
            } catch (IOException e) {
                exception.set(e);
            }
            assertNotNull("reading file without a version should throw an exception", exception.get());
            exception.set(null);

            writeFile(file, "file=r");
            try {
                attrs.load();
            } catch (IOException e) {
                exception.set(e);
            }
            assertNotNull("reading file without a version should throw an exception", exception.get());
            exception.set(null);

            writeFile(file, "VERSION=123");
            try {
                attrs.load();
            } catch (IOException e) {
                exception.set(e);
            }
            assertNotNull("reading file with unsupported version should throw an exception", exception.get());
            exception.set(null);

            writeFile(file, "VERSION=xxx");
            try {
                attrs.load();
            } catch (IOException e) {
                exception.set(e);
            }
            assertNotNull("reading file with invalid version should throw an exception", exception.get());
            exception.set(null);

            writeFile(file, "VERSIONxxx");
            try {
                attrs.load();
            } catch (IOException e) {
                exception.set(e);
            }
            assertNotNull("reading file with invalid version should throw an exception", exception.get());
            exception.set(null);

            writeFile(file, "VERSION=1");
            try {
                attrs.load();
            } catch (IOException e) {
                exception.set(e);
            }
            if (exception.get() != null) {
                throw exception.get();
            }
            //assertNull("exception while loading a valid file", exception.get());

            writeFile(file, "VERSION=1\nfile1=+");
            try {
                attrs.load();
            } catch (IOException e) {
                exception.set(e);
            }
            assertNotNull("reading invalid format should throw an exception", exception.get());
            exception.set(null);

            writeFile(file, "VERSION=1\nfile1 w");
            try {
                attrs.load();
            } catch (IOException e) {
                exception.set(e);
            }
            assertNotNull("reading invalid format should throw an exception", exception.get());
            exception.set(null);

            writeFile(file, "VERSION=1\nqw");
            try {
                attrs.load();
            } catch (IOException e) {
                exception.set(e);
            }
            assertNotNull("reading invalid format should throw an exception", exception.get());
            exception.set(null);

            writeFile(file, "VERSION=1\nfile_w=w\nfile_r=r\n\n");
            try {
                attrs.load();
            } catch (IOException e) {
                exception.set(e);
            }
            if (exception.get() != null) {
                throw exception.get();
            }
            //assertNull("exception while loading a valid file", exception.get());

            assertTrue("file_w should be writebale", attrs.isWritable("file_w"));
            assertFalse("file_r should not be writebale", attrs.isWritable("file_r"));
        } finally {
            file.delete();
        }
    }

    public void testDirectoryAttributesSetGet() throws Exception {
        File file = File.createTempFile("test-directory-attributes-set-get", ".dat");
        DirectoryAttributes attrs = new DirectoryAttributes(file);
        setOrCheck(attrs, true);
        setOrCheck(attrs, false);
    }

    private void setOrCheck(DirectoryAttributes attrs, boolean set) throws Exception {
        String file_r = "file_r";
        String file_w = "file_w";
        String file_r_with_a_space = "file r with a space";
        String file_w_with_a_space = "file w with a space";
        String inexistent_file = "inexistent file";

        attrs.setWritable(file_w, true);
        attrs.setWritable(file_w_with_a_space, true);
        attrs.setWritable(file_r, true);
        attrs.setWritable(file_r_with_a_space, true);

        assertTrue(attrs.isWritable(file_w));
        assertTrue(attrs.isWritable(file_w_with_a_space));
        assertTrue(attrs.isWritable(file_r));
        assertTrue(attrs.isWritable(file_r_with_a_space));
        assertTrue(attrs.isWritable(file_r_with_a_space));
        assertTrue(attrs.isWritable(inexistent_file));
    }

    public void testDirectoryAttributesSave() throws Exception {
        File file = File.createTempFile("test-directory-attributes-save", ".dat");
        try {
            DirectoryAttributes attrs = new DirectoryAttributes(file);
            AtomicReference<Exception> exception = new AtomicReference<Exception>();

            setOrCheck(attrs, true);
            try {
                attrs.store();
            } catch (IOException e) {
                exception.set(e);
            }
            if (exception.get() != null) {
                throw exception.get();
            }
            //assertNull("exception while saving a file", exception.get());

            try {
                attrs.load();
            } catch (IOException e) {
                exception.set(e);
            }
            if (exception.get() != null) {
                throw exception.get();
            }
            //assertNull("exception while loading a file", exception.get());

            setOrCheck(attrs, false);
        } finally {
            file.delete();
        }
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(DirectoryAttributesTestCase.class);
    }

}
