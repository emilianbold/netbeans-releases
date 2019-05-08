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
package org.netbeans.modules.cnd.loaders;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 */
public class MakeDataObjectTestCase extends NbTestCase {
    public MakeDataObjectTestCase(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testCMakeDataObject1() throws Exception {
        File newFile = new File(super.getWorkDir(), "CMakeLists.txt"); // NOI18N
        checkCMake(newFile, MIMENames.CMAKE_MIME_TYPE, CMakeDataObject.class);
    }

    public void testCMakeDataObject2() throws Exception {
        File newFile = new File(super.getWorkDir(), "qq.cmake"); // NOI18N
        checkCMake(newFile, MIMENames.CMAKE_INCLUDE_MIME_TYPE, CMakeIncludeDataObject.class);
    }

    private void checkCMake(File newFile, String mime, Class<?> cls) throws IOException, DataObjectNotFoundException {
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = CndFileUtils.toFileObject(newFile);
        assertNotNull("Not found file object for file" + newFile, fo);
        assertTrue("File object not valid for file" + newFile, fo.isValid());
        assertEquals("Not text/x-cmake mime type", mime, fo.getMIMEType());
        DataObject dob = DataObject.find(fo);
        assertTrue("data object is not recognized by default infrastructure:" + dob.getClass(), cls.isInstance(dob));
    }
}
