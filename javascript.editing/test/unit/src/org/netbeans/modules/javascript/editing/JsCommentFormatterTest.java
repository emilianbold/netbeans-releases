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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.javascript.editing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.gsf.api.CompilationInfo;

/**
 *
 * @author Martin Adamek
 */
public class JsCommentFormatterTest extends JsTestBase {

    public JsCommentFormatterTest(String testName) {
        super(testName);
    }
    
    public void testGetSummary() throws Exception {
        JsCommentFormatter jsComment = new JsCommentFormatter(getComments());
        String summary = jsComment.getSummary();
        
        String expected = 
                "Applies bla bla bla bla\n" +
                "foo foo bar bar";
        assertEquals(expected, summary);
    }
    
    public void testTags() throws Exception {
        JsCommentFormatter jsComment = new JsCommentFormatter(getComments());
        
        List<String> tags = jsComment.getParams();
        List<String> expected = Arrays.asList(
                "<i>{Mixed}</i> <b>el</b> The bla bla bla",
                "<i>{Object/Array}</i> <b>values</b> The bla bla bloo (i.e. {0}) or an\n" +
                "object (i.e. {foo: 'bar'})",
                "<i>{Boolean}</i> <b>returnElement</b> (optional) true to return a Axt.Element (defaults to blii)"
                );
        
        assertEquals(expected, tags);

        String returnTag = jsComment.getReturn();
        String expectedReturn = "{HTMLElement/Axt.Element} The new node or Alement";
        assertEquals(expectedReturn, returnTag);

        
    }

    private List<String> getComments() throws Exception {
        CompilationInfo info = getInfo("testfiles/types2.js");
        String caretLine = "    insertBeeefore: func^tion(el, values, returnElement){";
        FunctionAstElement element = ElementUtilitiesTest.getFunctionAstElement(info, caretLine);
        return ElementUtilities.getComments(info, element);
    }

    public void testEmpty() throws Exception {
        List<String> comments = Collections.emptyList();
        // Regression test for 151016: StringIndexOutOfBoundsException: String index out of range: -1
        JsCommentFormatter jsComment = new JsCommentFormatter(comments);

        List<String> tags = jsComment.getParams();
        List<String> expected = Arrays.asList("");

        assertEquals(expected, tags);
    }


    /* TODO: Test formatting this - it currently ends up with @return on the same line!
 * Create a new table caption object or return an
 * existing one.
 * @return HTMLElement A CAPTION element.
 * @type HTMLElement
createCaption: function() {
        */
}
