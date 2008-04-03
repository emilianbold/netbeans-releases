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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;

/**
 *
 * @author Tor Norbye
 */
public class JsRenameHandlerTest extends JsTestBase {
    
    public JsRenameHandlerTest(String testName) {
        super(testName);
    }

    private String annotate(Document doc, Set<OffsetRange> ranges) throws Exception {
        if (ranges.size() == 0) {
            return "Requires Interactive Refactoring\n";
        }
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                sb.append("|>");
            }
            if (ends.containsKey(i)) {
                sb.append("<|");
            }
            sb.append(text.charAt(i));
        }
        // Only print lines with result
        String[] lines = sb.toString().split("\n");
        sb = new StringBuilder();
        int lineno = 1;
        for (String line : lines) {
            if (line.indexOf("|>") != -1) {
                sb.append(Integer.toString(lineno));
                sb.append(": ");
                sb.append(line);
                sb.append("\n");
            }
            lineno++;
        }
        
        return sb.toString();
    }

    private void checkRenameSections(String relFilePath, String caretLine) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        JsRenameHandler handler = new JsRenameHandler();

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = info.getText().indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
        }

        String annotatedSource;
        if (handler.isRenameAllowed(info, caretOffset, null)) {
            Set<OffsetRange> renameRegions = handler.getRenameRegions(info, caretOffset);
            annotatedSource = annotate(info.getDocument(), renameRegions);
        } else {
            annotatedSource = "Refactoring not allowed here\n";
        }

        assertDescriptionMatches(relFilePath, annotatedSource, true, ".rename");
    }

    public void testRename1() throws Exception {
        checkRenameSections("testfiles/rename.js", "x^xx");
    }

    public void testRename2() throws Exception {
        checkRenameSections("testfiles/rename.js", "function a^aa() {");
    }

    public void testRename3() throws Exception {
        checkRenameSections("testfiles/rename.js", "var y^yy");
    }

    public void testRename4() throws Exception {
        checkRenameSections("testfiles/rename.js", "b^bb:");
    }

    public void testRename5() throws Exception {
        checkRenameSections("testfiles/rename.js", "function(p^pp)");
    }

    public void testRename6() throws Exception {
        checkRenameSections("testfiles/rename.js", "alert(p^pp)");
    }

    public void testRename7() throws Exception {
        checkRenameSections("testfiles/rename.js", "al^ert(ppp)");
    }
    
    public void testRename8() throws Exception {
        checkRenameSections("testfiles/rename.js", "funct^ion");
    }
}
