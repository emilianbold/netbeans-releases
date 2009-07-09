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
package org.netbeans.modules.cnd.folding;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.editor.Analyzer;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.test.CndBaseTestCase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class APTFoldingProviderTestCase extends CndBaseTestCase {

    private static final boolean TRACE = false;

    /**
     * Creates a new instance of ModelImplBaseTestCase
     */
    public APTFoldingProviderTestCase(String testName) {
        super(testName);
    }

    public void testIfdefFolding() throws Exception {
        performTest("ifdef.cc");
    }

    public void testSimpleFolding() throws Exception {
        performTest("simpleFolding.cc");
    }

    public void testErrorDirective() throws Exception {
        performTest("error_directive.cc");
    }

    public void testLastIncludes() throws Exception {
        performTest("lastIncludes.cc");
    }

    public void testMixedPrepocDirectives() throws Exception {
        performTest("mixedPreprocDirectives.cc");
    }

    private void performTest(String source) throws Exception {
        if (TRACE) {
            System.out.println(getWorkDir());
        }
        File testSourceFile = getDataFile(source);
        char[] text = Analyzer.loadFile(testSourceFile.getAbsolutePath());
        Reader reader = new StringReader(new String(text));
        APTFoldingProvider provider = new APTFoldingProvider();
        List<CppFoldRecord> folds = provider.parse(source, reader);
        Collections.sort(folds, FOLD_COMPARATOR);
        for (CppFoldRecord fold : folds) {
            ref(fold.toString());
        }
        compareReferenceFiles();
    }
    private static Comparator<CppFoldRecord> FOLD_COMPARATOR = new Comparator<CppFoldRecord>() {

        public int compare(CppFoldRecord o1, CppFoldRecord o2) {
            int start1 = o1.getStartLine();
            int start2 = o2.getStartLine();
            if (start1 == start2) {
                return o1.getStartOffset() - o2.getStartOffset();
            } else {
                return start1 - start2;
            }
        }
    };
}
