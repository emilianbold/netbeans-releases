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
package org.netbeans.modules.javafx2.editor.parser;

import org.netbeans.modules.javafx2.editor.parser.FxModelBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import javax.swing.text.Document;
import junit.framework.TestSuite;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.javafx2.editor.FXMLCompletionTestBase;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.sax.XMLLexerParserTest;
import org.netbeans.modules.javafx2.editor.sax.XmlLexerParser;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author sdedic
 */
public class FxmlParserTest extends FXMLCompletionTestBase {
    private DataObject  sourceDO;
    private Document    document;
    private TokenHierarchy hierarchy;
    private String fname;

    public FxmlParserTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File dataDir = getDataDir();
        fname = getName().replace("test", "");
        File f = new File(dataDir, FxmlParserTest.class.getName().
                replaceAll("\\.", "/") + "/" + fname + ".fxml");
        
        File w = new File(getWorkDir(), f.getName());
        InputStream is = new FileInputStream(f);
        OutputStream os = new FileOutputStream(w);
        FileUtil.copy(is, os);
        os.close();
        is.close();
        FileObject fo = FileUtil.toFileObject(w);
        sourceDO = DataObject.find(fo);
        document = ((EditorCookie)sourceDO.getCookie(EditorCookie.class)).openDocument();
        hierarchy = TokenHierarchy.get(document);
    }

    @Override
    protected void tearDown() throws Exception {
        if (document != null) {
            ((EditorCookie)sourceDO.getCookie(EditorCookie.class)).close();
        }
        super.tearDown();
    }
    
    public void testParserInvocation() throws Exception {
        Source fxmlSource = Source.create(document);
        
        ParserManager.parse(Collections.singleton(fxmlSource), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                
                if (result instanceof FxmlParserResult) {
                    FxmlParserResult fxResult = (FxmlParserResult)result;
                    System.out.println(fxResult);
                }
            }
        });
    }
    
    public static TestSuite suite() {
        TestSuite s = new TestSuite();
        s.addTest(new FxmlParserTest("testUnresolvedThings"));
        return s;
    }

    public void testUnresolvedThings() throws Exception {
        Source fxmlSource = Source.create(document);
        
        ParserManager.parse(Collections.singleton(fxmlSource), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                
                FxmlParserResult fxResult = (FxmlParserResult)result;
                System.out.println(fxResult);
            }
        });
    }


    private void assertContents(StringBuilder sb) throws IOException {
        File out = new File(getWorkDir(), fname + ".parsed");
        FileWriter wr = new FileWriter(out);
        wr.append(sb);
        wr.close();
        
        assertFile(out, getGoldenFile(fname + ".pass"), new File(getWorkDir(), fname + ".diff"));
    }
    
    private FxModelBuilder builder;
    
    private StringBuilder report = new StringBuilder();

    private void defaultTestContents() throws Exception {
        XmlLexerParser parser = new XmlLexerParser(hierarchy);
        builder = new FxModelBuilder();
        parser.setContentHandler(builder);
        parser.parse();

        StringBuilder sb = report;
        sb.append("\n\n");
        for (ErrorMark em : parser.getErrors()) {
            sb.append(em).append("\n");
        }
        for (ErrorMark em : builder.getErrors()) {
            sb.append(em).append("\n");
        }
        
        assertContents(sb);
    }
}
