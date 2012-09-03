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
package org.netbeans.modules.web.livehtml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.web.domdiff.DiffTest;
import org.netbeans.modules.web.livehtml.filter.FilteredAnalysis;

/**
 *
 * @author petr-podzimek
 */
public class StackTraceRevisionFilterTest extends NbTestCase {

    private Analysis analysis;
    private FilteredAnalysis filteredAnalysis;

    public StackTraceRevisionFilterTest() {
        super(StackTraceRevisionFilterTest.class.getName());
    }

    @Test
    public void test() {
        assertEquals(8, analysis.getTimeStampsCount());

        assertEquals(4, filteredAnalysis.getTimeStampsCount());

        List<Integer> result = new ArrayList<Integer>();
        filteredAnalysis.getGroupByStackTraceRevisions(result, Collections.<String>emptyList());
        List<Integer> expected = Arrays.asList(new Integer[]{3,4,5,6});
        assertEquals(expected, result);
    }

    @Before
    public void setUp() throws IOException {
        AnalysisStorage.isUnitTesting = true;

        File f = new File(getWorkDir(), String.valueOf(System.currentTimeMillis()));
        f.mkdirs();
        analysis = new Analysis(f);

        // Test is based on SimpleLiveHTMLTest/index.html actions: 1 3 2
        HtmlSource source00 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r00.content");
        String stackTrace00 = TestUtilities.getContent(getDataDir(), "filter/test001-r00.stacktrace");
        analysis.storeDocumentVersion("00", source00.getSourceCode().toString(), stackTrace00, true);

        HtmlSource source01 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r01.content");
        analysis.storeDocumentVersion("01", source01.getSourceCode().toString(), "[{}]", false);

        HtmlSource source02 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r02.content");
        String stackTrace02 = TestUtilities.getContent(getDataDir(), "filter/test001-r02.stacktrace");
        analysis.storeDocumentVersion("02", source02.getSourceCode().toString(), stackTrace02, true);

        HtmlSource source03 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r03.content");
        analysis.storeDocumentVersion("03", source03.getSourceCode().toString(), "[{}]", false);

        HtmlSource source04 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r04.content");
        String stackTrace04 = TestUtilities.getContent(getDataDir(), "filter/test001-r04.stacktrace");
        analysis.storeDocumentVersion("04", source04.getSourceCode().toString(), stackTrace04, true);

        HtmlSource source05 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r05.content");
        String stackTrace05 = TestUtilities.getContent(getDataDir(), "filter/test001-r05.stacktrace");
        analysis.storeDocumentVersion("05", source05.getSourceCode().toString(), stackTrace05, true);

        HtmlSource source06 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r06.content");
        String stackTrace06 = TestUtilities.getContent(getDataDir(), "filter/test001-r06.stacktrace");
        analysis.storeDocumentVersion("06", source06.getSourceCode().toString(), stackTrace06, true);

        HtmlSource source07 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r07.content");
        String stackTrace07 = TestUtilities.getContent(getDataDir(), "filter/test001-r07.stacktrace");
        analysis.storeDocumentVersion("07", source07.getSourceCode().toString(), stackTrace07, true);

        HtmlSource source08 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r08.content");
        String stackTrace08 = TestUtilities.getContent(getDataDir(), "filter/test001-r08.stacktrace");
        analysis.storeDocumentVersion("08", source08.getSourceCode().toString(), stackTrace08, true);

        HtmlSource source09 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r09.content");
        analysis.storeDocumentVersion("09", source09.getSourceCode().toString(), "[{}]", false);

        HtmlSource source10 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r10.content");
        String stackTrace10 = TestUtilities.getContent(getDataDir(), "filter/test001-r10.stacktrace");
        analysis.storeDocumentVersion("10", source10.getSourceCode().toString(), stackTrace10, true);

        HtmlSource source11 = DiffTest.getHtmlSource(getDataDir(), "filter/test001-r11.content");

        filteredAnalysis = new FilteredAnalysis(null, true, false, analysis);
    }

}
