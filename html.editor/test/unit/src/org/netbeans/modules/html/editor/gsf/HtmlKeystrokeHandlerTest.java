/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor.gsf;

import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;

/**
 *
 * @author marekfukala
 */
public class HtmlKeystrokeHandlerTest extends TestBase {

    public HtmlKeystrokeHandlerTest() {
        super(HtmlKeystrokeHandlerTest.class.getName());
    }

    public void testEmptyFile() throws ParseException {
        assertLogicalRanges("|", new int[][]{}); //no range
    }

    public void testWholeDocumentRange() throws ParseException {
        assertLogicalRanges("   |   ", new int[][]{{0,6}}); //no range
        //                   012 3456
    }

    public void testBasic() throws ParseException {
        assertLogicalRanges("<a>te|xt</a>", new int[][]{{3,7},{0,11}});
        //                   01234 5678901

        assertLogicalRanges("<a> <b> te|xt </b> </a>", new int[][]{{8,12}, {7,13},{4,17}, {0,22}});
        //                   0123456789 0123456789012
        //                   0          1         2

        //test tag with attribute
        assertLogicalRanges("<div><div align='c|enter'/></div>", new int[][]{{5,26},{0,32}});
        //                   012345678901234567 890123456789012

    }


    private void assertLogicalRanges(String sourceText, int[][] expectedRangesLeaveToRoot) throws ParseException {
         //find caret position in the source text
        StringBuffer content = new StringBuffer(sourceText);

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";

        //remove the pipe
        content.deleteCharAt(pipeOffset);
        sourceText = content.toString();

        //fatal parse error on such input, AST root == null
        Document doc = getDocument(sourceText);
        Source source = Source.create(doc);
        final Result[] _result = new Result[1];
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                _result[0] = resultIterator.getParserResult();
            }
        });

        Result result = _result[0];
        assertNotNull(result);
        assertTrue(result instanceof HtmlParserResult);

        HtmlParserResult htmlResult = (HtmlParserResult)result;
        assertNotNull(htmlResult.root());
        assertEquals(0, htmlResult.getDiagnostics().size()); //no errors

        KeystrokeHandler handler = getPreferredLanguage().getKeystrokeHandler();
        assertNotNull(handler);

        List<OffsetRange> ranges = handler.findLogicalRanges(htmlResult, pipeOffset);
        assertNotNull(ranges);

        StringBuffer buf = new StringBuffer();
        for(OffsetRange or : ranges) {
            buf.append("{" + or.getStart() + ", " + or.getEnd() + "}, ");
        }

        assertEquals("Unexpected number of logical ranges (" + buf.toString() + ")", expectedRangesLeaveToRoot.length, ranges.size());

        for(int i = 0; i < ranges.size(); i++) {
            OffsetRange or = ranges.get(i);

            int expectedStart = expectedRangesLeaveToRoot[i][0];
            int expectedEnd = expectedRangesLeaveToRoot[i][1];

            assertEquals("Invalid logical range (" + or.toString() + ") start offset", expectedStart, or.getStart());
            assertEquals("Invalid logical range (" + or.toString() + ") end offset", expectedEnd, or.getEnd());
        }

    }

    


}