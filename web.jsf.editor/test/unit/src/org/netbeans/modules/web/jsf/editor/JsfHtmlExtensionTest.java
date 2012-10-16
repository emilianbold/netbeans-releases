/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.html.editor.completion.HtmlCompletionTestSupport;
import org.netbeans.modules.html.editor.completion.HtmlCompletionTestSupport.Match;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;


/**
 *
 * @author marekfukala
 */
public class JsfHtmlExtensionTest extends TestBaseForTestProject {
    
    public JsfHtmlExtensionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    

    public void testAttributeValueCompletion() throws BadLocationException, ParseException {
        testCC("<h:selectManyCheckbox layout=\"|\"/>", new String[]{"pageDirection", "lineDirection"}, Match.EXACT);
        testCC("<f:ajax immediate=\"|\"/>", new String[]{"true", "false"}, Match.EXACT);
    }
    
    protected void testCC(String testText, String[] expected, Match matchType) throws BadLocationException, ParseException {
        testCC(testText, expected, matchType, -1);
    }
    
    /**
     * The testText will be inserted into the body of testWebProject/web/cctest.xhtml and then the completion will be called.
     * In the case you need more imports, modify the template or make the support generic (no template based)
     */
    protected void testCC(String testText, String[] expected, Match matchType, int expectedAnchor) throws BadLocationException, ParseException {
        //load the testing template
        FileObject file = getTestFile("testWebProject/web/cctest.xhtml");
        Document doc = getDocument(file);
        
        StringBuilder content = new StringBuilder(doc.getText(0, doc.getLength()));
        final int documentPipeIndex = content.indexOf("|");
        assertFalse(documentPipeIndex < 0);
        
        //remove the pipe
        content.deleteCharAt(documentPipeIndex);
        
        //insert test text, extract the pipe first
        content.insert(documentPipeIndex, testText);
        
        Document testdoc = getDocument(content.toString(), JsfUtils.XHTML_MIMETYPE);
        
        HtmlCompletionTestSupport.assertItems(
                testdoc, 
                expected, 
                matchType, 
                expectedAnchor);
    }
    
}
