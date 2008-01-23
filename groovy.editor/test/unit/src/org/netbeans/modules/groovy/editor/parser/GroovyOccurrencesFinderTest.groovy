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

package org.netbeans.modules.groovy.editor.parser;

import java.util.Map;
import org.netbeans.api.gsf.CompilationInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.gsf.ColoringAttributes;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
class GroovyOccurrencesFinderTest extends GroovyTestBase {
	
    GroovyOccurrencesFinderTest(String testName) {
        super(testName)
    }
    
    void testVariableOccurences() {
        checkOccurrences("""
        class Hello {
            def name
            def age
            def sayHello(|>name<|) {
                |>name<| = |>name<| + 'Man'
                println |>n^ame<|
                println age
            }
        }
        """)
    }
    
    private void checkOccurrences(String golden) {
        
        def sourceWithCaret = golden.replace('|>', '').replace('<|', '')
        def pureSource = sourceWithCaret.replace('^', '')
        def goldenWithoutCaret = golden.replace('^', '')
        def caretOffset = sourceWithCaret.indexOf('^')
        
        copyStringToFileObject(testFO, pureSource)
        
        CompilationInfo info = getInfo(testFO)

        GroovyOccurrencesFinder finder = new GroovyOccurrencesFinder()
        finder.setCaretPosition(caretOffset)
        finder.run(info)
        Map<OffsetRange, ColoringAttributes> occurrences = finder.getOccurrences()

        String annotatedSource = annotate(info.getDocument(), occurrences, caretOffset)
        assertEquals(annotatedSource, goldenWithoutCaret + '\n') // why there is additional newline in annotatedSource?
    }

    private String annotate(BaseDocument doc, Map<OffsetRange, ColoringAttributes> highlights, int caretOffset) {
        Set<OffsetRange> ranges = highlights.keySet()
        StringBuilder sb = new StringBuilder()
        String text = doc.getText(0, doc.getLength())
        def starts = [:]
        def ends = [:]
        ranges.each { range ->
            starts[range.start] = range
            ends[range.end] = range
        }

        int index = 0
        int length = text.length()
        while (index < length) {
            int lineStart = Utilities.getRowStart(doc, index)
            int lineEnd = Utilities.getRowEnd(doc, index)

            OffsetRange lineRange = new OffsetRange(lineStart, lineEnd)
            (lineStart..lineEnd).each {
                if (starts.containsKey(it)) {
                    sb.append('|>')
                    OffsetRange range = starts.get(it)
                    ColoringAttributes ca = highlights.get(range)
                    assertEquals("MARK_OCCURRENCES", ca.name())
                }
                if (ends.containsKey(it)) {
                    sb.append('<|')
                }
                sb.append(text.charAt(it))
            }
            index = lineEnd + 1;
        }
        return sb.toString()
    }

}

