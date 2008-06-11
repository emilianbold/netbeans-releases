/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.highlight.error;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;

/**
 * 
 * @author Vladimir Kvashin
 */
public class ErrorHighlightingBaseTestCase extends ProjectBasedTestCase {

    public ErrorHighlightingBaseTestCase(String testName) {
        super(testName, true);
    }

    protected final void performTest(String source) throws Exception {
        String datafileName = source + ".dat";
        File testSourceFile = getDataFile(source);
        File workDir = getWorkDir();
        File output = new File(workDir, datafileName);
        PrintStream out = new PrintStream(output);

        File dataDir = getDataDir();
        
//        File goldenFile = getGoldenFile(datafileName);
//        CndCoreTestUtils.copyToWorkDir(goldenFile); // NOI18N
        File goldenFile = getGoldenFile(datafileName);
        //CndCoreTestUtils.copyToFile(getGoldenFile(), goldenFile);
        
        CsmFile csmFile = getCsmFile(testSourceFile);
        BaseDocument doc = getBaseDocument(testSourceFile);
        Collection<CsmErrorInfo> errorInfos = CsmErrorProvider.getDefault().getErrors(doc, csmFile);
        for (CsmErrorInfo info : errorInfos) {
            String txt = String.format("%s %s [%d-%d]: %s", info.getSeverity(), source, info.getStartOffset(), info.getEndOffset(), info.getMessage());
            out.printf("%s\n", txt);
            System.out.printf("%s\n", txt);
        }

        //compareReferenceFiles();    
        compareReferenceFiles(datafileName, datafileName);
    }   
}
