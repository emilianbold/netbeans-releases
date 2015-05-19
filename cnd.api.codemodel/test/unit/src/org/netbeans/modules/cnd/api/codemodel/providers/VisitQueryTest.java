/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.providers;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;
import org.netbeans.modules.cnd.api.codemodel.CMSourceRange;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.api.codemodel.test.CMBaseTestCase;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery.CursorVisitor;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author mtishkov
 */
public class VisitQueryTest extends CMBaseTestCase {

    public VisitQueryTest(String testName) {
        super(testName);
    }

    /**
     * Test of getCompletion method, of class CMCompletionQuery.
     */
    public void testSimpleCompletion_1() throws Exception {
        performTest("cc_simple_1.cc", 9, 7, null, -1, "cc_simple.ref", null, null, false);
    }

    protected void performTest(final String source, final int lineIndex, final int colIndex,
            final String textToInsert, int offsetAfterInsertion, final String goldenFileName,
            final String toPerformItemRE, final String goldenFileName2, final boolean tooltip) throws Exception {

        performTest(new TestPerformer() {
            @Override
            public void perform(File... sourceFiles) throws Exception {
                URI uri = Utilities.toURI(sourceFiles[0]);
                CMVisitQuery.visitCursorsInFile(SPIUtilities.getTranslationUnits(uri), uri, new CMVisitQuery.CursorAndRangeVisitor() {
                    @Override
                    public boolean isCancelled() {
                        return false;
                    }

                    @Override
                    public boolean visit(CMCursor cur, CMSourceRange curRange) {
                        //print info
                        System.out.println("cursor:" + cur.getDisplayName() + " curRange=[" + curRange.getStart().getOffset() + "," + curRange.getEnd().getOffset() + "]");
                        return true;
                    }
                });
                Collection<CMTranslationUnit> translationUnits = SPIUtilities.getTranslationUnits(uri);
                for (CMTranslationUnit tu : translationUnits) {
                    System.out.println("tu=" + tu.getMainFilePath());
                    final CMCursor rootCursor = tu.getRootCursor();
                    System.out.println("will take root cursor");
                    System.out.println("rootCursor=" + rootCursor.getUSR());
                    CMVisitQuery.visitCursorChildren(rootCursor, new CMVisitQuery.CursorVisitor() {
                        @Override
                        public CursorVisitor.ChildVisitRequest visit(CMCursor cursor, CMCursor parent) {
                            System.out.println("XXXcursor:" + cursor + " parentCursor=" + parent);
                            return CursorVisitor.ChildVisitRequest.Continue;
                        }
                    });
                    CMVisitQuery.visitDiagnostics(uri, new CMVisitQuery.DiagnosticsCallback() {
                        @Override
                        public boolean isCancelled() {
                            return false;
                        }

                        @Override
                        public void onDiagnostics(Iterable<CMDiagnostic> diagnostics) {
                            for (CMDiagnostic cMDiagnostic : diagnostics) {

                                System.out.println("CMDiagnostic=" + cMDiagnostic.getFormattedText());
                            }
                        }
                    });
                    TestIndexCallback callback = new TestIndexCallback();
                    CMVisitQuery.visitTranslationUnit(tu, callback, CMVisitQuery.VisitOptions.None);
                    printTestIndexCallback(callback, false);
                }
            }
        }, source);
    }
}
