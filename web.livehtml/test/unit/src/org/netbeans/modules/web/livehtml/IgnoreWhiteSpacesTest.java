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
public class IgnoreWhiteSpacesTest extends NbTestCase {

    private Analysis analysis;
    private FilteredAnalysis filteredAnalysis;

    public IgnoreWhiteSpacesTest() {
        super(IgnoreWhiteSpacesTest.class.getName());
    }

    @Test
    public void test() {
        assertEquals(11, analysis.getTimeStampsCount());

        assertEquals(5, filteredAnalysis.getTimeStampsCount());

        List<Integer> result = new ArrayList<Integer>();
        filteredAnalysis.getWhiteSpaceGroupedRevisions(result);
        List<Integer> expected = Arrays.asList(new Integer[]{2,3,4,5,7,9});
        assertEquals(expected, result);
    }

    @Before
    @Override
    public void setUp() throws IOException {
        AnalysisStorage.isUnitTesting = true;

        File f = new File(getWorkDir(), String.valueOf(System.currentTimeMillis()));
        f.mkdirs();
        analysis = new Analysis(f);

        // Test is based on hand made changes in golden files - see comments
        HtmlSource source1 = DiffTest.getHtmlSource(getDataDir(), "filter/test003-r01.content");
        analysis.storeDocumentVersion("0", source1.getSourceCode().toString(), "[{}]", true); // initial Revision
        analysis.storeDocumentVersion("1", source1.getSourceCode().toString(), "[{}]", true); // first Revision
        HtmlSource source2 = DiffTest.getHtmlSource(getDataDir(), "filter/test003-r02.content");
        analysis.storeDocumentVersion("2", source2.getSourceCode().toString(), "[{}]", true); // second Revision + white space detected
        analysis.storeDocumentVersion("3", source2.getSourceCode().toString(), "[{}]", true); // white space detected
        analysis.storeDocumentVersion("4", source2.getSourceCode().toString(), "[{}]", true); // white space detected
        analysis.storeDocumentVersion("5", source2.getSourceCode().toString(), "[{}]", true); // white space detected
        HtmlSource source3 = DiffTest.getHtmlSource(getDataDir(), "filter/test003-r03.content");
        analysis.storeDocumentVersion("6", source3.getSourceCode().toString(), "[{}]", true); // trird Revision
        HtmlSource source4 = DiffTest.getHtmlSource(getDataDir(), "filter/test003-r04.content");
        analysis.storeDocumentVersion("7", source4.getSourceCode().toString(), "[{}]", true); // white space detected
        HtmlSource source5 = DiffTest.getHtmlSource(getDataDir(), "filter/test003-r05.content");
        analysis.storeDocumentVersion("8", source5.getSourceCode().toString(), "[{}]", true); // fourth Revision
        HtmlSource source6 = DiffTest.getHtmlSource(getDataDir(), "filter/test003-r06.content");
        analysis.storeDocumentVersion("9", source6.getSourceCode().toString(), "[{}]", true); // white space detected
        analysis.storeDocumentVersion("10", source6.getSourceCode().toString(), "[{}]", true); // white space detected

        filteredAnalysis = new FilteredAnalysis(null, false, true, analysis);
    }

}
