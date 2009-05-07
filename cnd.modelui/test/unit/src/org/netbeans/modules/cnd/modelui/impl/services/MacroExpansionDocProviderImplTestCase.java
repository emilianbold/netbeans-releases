/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.modelui.impl.services;

import java.io.File;
import java.io.PrintStream;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionDocProvider;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Tests for MacroExpansionDocProviderImpl
 *
 * @author Nick Krasilnikov
 */
public class MacroExpansionDocProviderImplTestCase extends TraceModelTestBase {

    public MacroExpansionDocProviderImplTestCase(String testName) {
        super(testName);
    }

    public void testFile1() throws Exception {
        performTest("file1.cc"); // NOI18N
    }

    public void testFile1_2() throws Exception {
        performTest("file1.cc", 5, 13, 5, 21); // NOI18N
    }

    public void testCodeExpansion() throws Exception {
        performTest("file1.cc", 10, 13, "CONSTANT + 1"); // NOI18N
    }

    ////////////////////////////////////////////////////////////////////////////
    // general staff
    @Override
    protected void postSetUp() throws Exception {
        super.postSetUp();
        log("postSetUp is preparing project"); // NOI18N
        initParsedProject();
        log("postSetUp finished project preparing"); // NOI18N
        log("Test " + getName() + "started"); // NOI18N
    }

    @Override
    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        String path = args[0];
        FileImpl currentFile = getFileImpl(new File(path));

        assertNotNull("Csm file was not found for " + path, currentFile); // NOI18N

        if (params.length == 0) {
            // Test whole file
            CsmMacroExpansionDocProvider mp = new MacroExpansionDocProviderImpl();

            String objectSource = currentFile.getName().toString();

            BaseDocument doc = getBaseDocument(getDataFile(objectSource));
            assertNotNull(doc);
            int startOffset = 0;
            int endOffset = doc.getLength();

            String res = mp.expand(doc, startOffset, endOffset);
            assertNotNull(res);

            streamOut.println(res);

        } else if (params.length == 4) {
            // Test part of file

            CsmMacroExpansionDocProvider mp = new MacroExpansionDocProviderImpl();

            String objectSource = currentFile.getName().toString();

            int startLine = (Integer) params[0];
            int startColumn = (Integer) params[1];

            int endLine = (Integer) params[2];
            int endColumn = (Integer) params[3];

            BaseDocument doc = getBaseDocument(getDataFile(objectSource));
            assertNotNull(doc);
            int startOffset = CndCoreTestUtils.getDocumentOffset(doc, startLine, startColumn);
            int endOffset = CndCoreTestUtils.getDocumentOffset(doc, endLine, endColumn);

            String res = mp.expand(doc, startOffset, endOffset);
            assertNotNull(res);

            streamOut.println(res);
         } else if (params.length == 3) {
            // Test expansion of code in specified context

            CsmMacroExpansionDocProvider mp = new MacroExpansionDocProviderImpl();

            String objectSource = currentFile.getName().toString();

            int line = (Integer) params[0];
            int column = (Integer) params[1];

            String code = (String) params[2];

            BaseDocument doc = getBaseDocument(getDataFile(objectSource));
            assertNotNull(doc);
            int offset = CndCoreTestUtils.getDocumentOffset(doc, line, column);

            String res = mp.expand(doc, offset, code);
            assertNotNull(res);

            streamOut.println(res);
        } else {
            assert true; // Bad test params
        }
    }

    protected BaseDocument getBaseDocument(File testSourceFile) throws Exception {
        FileObject testFileObject = FileUtil.toFileObject(testSourceFile);
        assertNotNull("Unresolved test file " + testSourceFile, testFileObject);//NOI18N
        DataObject testDataObject = DataObject.find(testFileObject);
        assertNotNull("Unresolved data object for file " + testFileObject, testDataObject);//NOI18N
        BaseDocument doc = CndCoreTestUtils.getBaseDocument(testDataObject);
        assertNotNull("Unresolved document for data object " + testDataObject, testDataObject);//NOI18N
        return doc;
    }

    private void performTest(String source, int startLine, int startColumn, int endLine, int endColumn) throws Exception {
        super.performTest(source, getName(), null, startLine, startColumn, endLine, endColumn);
    }

    private void performTest(String source, int line, int column, String code) throws Exception {
        super.performTest(source, getName(), null, line, column, code);
    }

    @Override
    protected void performTest(String source) throws Exception {
        super.performTest(source, getName(), null);
    }

}
