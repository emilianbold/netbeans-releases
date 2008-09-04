/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.editor.ext.html.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.test.TestBase;

/**
 *
 * @author tomslot
 */
public class SyntaxTreeTest extends TestBase {
    private static final LanguagePath languagePath = LanguagePath.get(HTMLTokenId.language());
    
    public SyntaxTreeTest(){
        super("SyntaxTreeTest");
    }
    
    public void testTrivialCase() throws Exception {
        testSyntaxTree("trivial.html");
    }

    public void testEmptyTags() throws Exception{
        testSyntaxTree("emptyTags.html");
    }

    public void testList() throws Exception{
        testSyntaxTree("list.html");
    }

    public void testTable() throws Exception{
        testSyntaxTree("table.html");
    }

    public void testTagCrossing() throws Exception{
        testSyntaxTree("tagCrossing.html");
    }

    public void testMissingEndTag() throws Exception{
        testSyntaxTree("missingEndTag.html");
    }
    
    public void testIssue145821() throws Exception{
        testSyntaxTree("issue145821.html");
    }

    public void testIssue127786() throws Exception {
        testSyntaxTree("issue127786.html");
    }
    
    public void testIssue129347() throws Exception {
        testSyntaxTree("issue129347.html");
    }
    
    public void testIssue129654() throws Exception {
        testSyntaxTree("issue129654.html");
    }
    
    private void testSyntaxTree(String testCaseName) throws Exception {
        System.out.println("testSyntaxTree(" + testCaseName + ")");
        
        String documentContent = readStringFromFile(new File(
                getTestFilesDir(), testCaseName));
        
        BaseDocument doc = createDocument();
        doc.insertString(0, documentContent, null);
        SyntaxParser parser = SyntaxParser.get(doc, languagePath);
        parser.forceParse();
        AstNode root = SyntaxTree.makeTree(parser.elements());
        getRef().print(root.toString());
        compareReferenceFiles();
    }
    
    
    private String readStringFromFile(File file) throws IOException {
        StringBuffer buff = new StringBuffer();
        
        BufferedReader rdr = new BufferedReader(new FileReader(file));
        
        String line;
        
        try{
            while ((line = rdr.readLine()) != null){
                buff.append(line + "\n");
            }
        } finally{
            rdr.close();
        }
        
        return buff.toString();
    }
    
    private File getTestFilesDir(){
        return new File(new File(getDataDir(), "input"), "SyntaxTreeTest");
    }
}
