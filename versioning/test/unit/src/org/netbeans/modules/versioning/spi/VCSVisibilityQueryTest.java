/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.spi;

import java.io.IOException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;

import java.io.File;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.spi.testvcs.TestVCS;
import org.netbeans.modules.versioning.spi.testvcs.TestVCSVisibilityQuery;

/**
 * Versioning SPI unit tests of VCSVisibilityQuery.
 * 
 * @author Tomas Stupka
 */
public class VCSVisibilityQueryTest extends NbTestCase {
    

    public VCSVisibilityQueryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", System.getProperty("data.root.dir") + "/userdir");
        super.setUp();
    }

    public void testVQ() throws FileStateInvalidException, IOException {
        File folder = new File(getWorkDir(), TestVCS.VERSIONED_FOLDER_SUFFIX);
        folder.mkdirs();
        
        File visible = new File(folder, "this-file-is-visible");
        visible.createNewFile();
        FileObject visibleFO = FileUtil.toFileObject(visible);
        assertTrue(VisibilityQuery.getDefault().isVisible(visible));
        assertTrue(VisibilityQuery.getDefault().isVisible(visibleFO));

        File invisible = new File(folder, "this-file-is-" + TestVCSVisibilityQuery.INVISIBLE_FILE_SUFFIX);
        invisible.createNewFile();
        FileObject invisibleFO = FileUtil.toFileObject(invisible);
        assertFalse(VisibilityQuery.getDefault().isVisible(invisible));
        assertFalse(VisibilityQuery.getDefault().isVisible(invisibleFO));
    }
}
