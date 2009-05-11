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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * 
 * @author Jiri Skrivanek
 */
public class FileObjTest extends NbTestCase {

    public FileObjTest(String testName) {
        super(testName);
    }

    /** Tests it is not possible to create duplicate FileObject for the same path.
     * - create FO1
     * - create FO2
     * - delete FO1 => FO1 is invalid now
     * - rename FO2 to FO1
     * - rename FO1 to FO1 => FO1 still invalid
     * - try to write to FO1.getOutputStream() => it should not be possible because FO1 is still invalid
     */
    public void testDuplicateFileObject130998() throws IOException {
        clearWorkDir();
        FileObject testFolder = FileUtil.toFileObject(getWorkDir());
        FileObject fileObject1 = testFolder.createData("fileObject1");
        FileObject fileObject2 = testFolder.createData("fileObject2");
        fileObject1.delete();
        assertFalse("fileObject1 should be invalid after delete.", fileObject1.isValid());

        FileLock lock = fileObject2.lock();
        fileObject2.rename(lock, fileObject1.getName(), null);
        lock.releaseLock();
        assertTrue("fileObject2 should be valid.", fileObject2.isValid());

        lock = fileObject1.lock();
        fileObject1.rename(lock, fileObject1.getName(), null);
        lock.releaseLock();
        assertFalse("fileObject1 should remain invalid after rename.", fileObject1.isValid());

        try {
            fileObject1.getOutputStream();
            fail("Should not be possible to get OutputStream on invalid FileObject.");
        } catch (Exception e) {
            // OK - fileObject1 is invalid
        }
    }
}
