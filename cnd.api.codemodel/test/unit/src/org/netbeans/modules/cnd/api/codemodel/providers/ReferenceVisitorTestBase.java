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
import java.util.Arrays;
import java.util.Collection;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.CMUnifiedSymbolResolution;
import org.netbeans.modules.cnd.api.codemodel.test.CMBaseTestCase;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Vladimir Kvashin
 */
public class ReferenceVisitorTestBase extends CMBaseTestCase {

    public ReferenceVisitorTestBase(String testName) {
        super(testName);
    }

    protected void performReferenceVisitorTest(String source, int lineIndex, int colIndex) throws Exception {
        performReferenceVisitorTest(source, lineIndex, colIndex, getName() + ".ref");
    }

    protected void performReferenceVisitorTest(final String source, final int lineIndex, final int colIndex, final String goldenFileName) throws Exception {
        performTest(new TestPerformer() {

            @Override
            public void perform(File... sourceFiles) throws Exception {
                CMUnifiedSymbolResolution usr = findReferencedUSR(sourceFiles[0], lineIndex, colIndex);
                assertNotNull("Null referenced usr", usr);

                Collection<CMIndex> indices = SPIUtilities.getIndices();
                assertNotNull("Null indices", indices);
                assertFalse("Empty indices", indices.isEmpty());

                TestIndexCallback callback = new TestIndexCallback();
                CMVisitQuery.visitReferences(Arrays.asList(usr), indices, callback);
                printTestIndexCallback(callback, true);

                compareReferenceFiles();
            }
        }, source);
    }

    protected void printVisitedIndex(final String source, final int lineIndex, final int colIndex) throws Exception {
        performTest(new TestPerformer() {

            @Override
            public void perform(File... sourceFiles) throws Exception {
                CMUnifiedSymbolResolution usr = findReferencedUSR(sourceFiles[0], lineIndex, colIndex);
                assertNotNull("Null referenced usr", usr);

                Collection<CMIndex> indices = SPIUtilities.getIndices();
                assertNotNull("Null indices", indices);
                assertFalse("Empty indices", indices.isEmpty());

                TestIndexCallback callback = new TestIndexCallback();
                CMVisitQuery.VisitOptions visitOptions = CMVisitQuery.VisitOptions.valueOf(
                        CMVisitQuery.VisitOptions.IndexFunctionLocalSymbols,
                        CMVisitQuery.VisitOptions.IndexImplicitTemplateInstantiations);
                for (CMIndex index : indices) {
                    CMVisitQuery.visitIndex(index, callback, visitOptions);
                }
                printTestIndexCallback(callback, false);
            }
        }, source);
    }

    protected void printVisitedCursors(final CMVisitQuery.CursorAndRangeVisitor testVisitor, String... sources) throws Exception {
        performTest(new TestPerformer() {

            @Override
            public void perform(File... sourceFiles) throws Exception {

                TestPrintingCursorAndRangeVisitor visitor =
                        new TestPrintingCursorAndRangeVisitor(testVisitor,System.out);

                CMVisitQuery.VisitOptions visitOptions = CMVisitQuery.VisitOptions.valueOf(
                        CMVisitQuery.VisitOptions.IndexFunctionLocalSymbols,
                        CMVisitQuery.VisitOptions.IndexImplicitTemplateInstantiations);

                for (File file : sourceFiles) {
                    URI uri = Utilities.toURI(file);
                    CMVisitQuery.visitCursorsInFile(SPIUtilities.getTranslationUnits(uri), uri, visitor);
                }
            }
        }, sources);
    }
}
