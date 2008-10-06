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

package org.netbeans.modules.refactoring.spi;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Becicka
 */
public class BackupFacilityTest extends NbTestCase {
    
    public BackupFacilityTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        f = FileUtil.toFileObject(getDataDir()).getFileObject("projects/default/src/defaultpkg/Main.java");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetDefault() {
        assertNotNull(BackupFacility.getDefault());
    }

    static BackupFacility.Handle transactionId;
    FileObject f;
    public void testBackup() throws Exception {
        transactionId = BackupFacility.getDefault().backup(f);
    }

    public void testRestore() throws Exception {
        f.delete();
        assertFalse(f.isValid());
        transactionId.restore();
        FileObject newone = FileUtil.toFileObject(FileUtil.toFile(f));
        assertTrue(newone.isValid());
    }

    public void testClear() throws IOException {
        BackupFacility.getDefault().clear();
        try {
            transactionId.restore();
        } catch (IllegalArgumentException iae) {
            return;
        }
        fail("clear failed");
    }

}
