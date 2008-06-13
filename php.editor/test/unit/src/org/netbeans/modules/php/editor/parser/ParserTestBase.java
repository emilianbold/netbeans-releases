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

package org.netbeans.modules.php.editor.parser;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java_cup.runtime.Symbol;
import org.netbeans.modules.gsf.GsfTestBase;
import org.netbeans.modules.php.editor.lexer.PHPLexerUtils;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class ParserTestBase extends GsfTestBase {

    public ParserTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    protected void performFileParserTest(String filename) throws Exception {
        // parse the file
        String result = parseFile(new File(getDataDir(), "testfiles/" + filename + ".php"));
        
        String fullClassName = this.getClass().getName();
        String goldenFileDir = fullClassName.replace('.', '/');
        
        // try to find golden file
        String goldenFolder = getDataSourceDir().getAbsolutePath() + "/goldenfiles/" + goldenFileDir + "/";
        File goldenFile = new File(goldenFolder + filename + ".pass");
        if (!goldenFile.exists()) {
            // if doesn't exist, create it
            FileObject goldenFO = touch(goldenFolder, filename + ".pass");
            copyStringToFileObject(goldenFO, result);
        }
        else {
            // if exist, compare it. 
            goldenFile = getGoldenFile(filename + ".pass");
            FileObject resultFO = touch(getWorkDir(), filename + ".result");
            copyStringToFileObject(resultFO, result);
            assertFile(FileUtil.toFile(resultFO), goldenFile, getWorkDir());
        }
    }
    
    private static final String parseFile(File testFile) throws Exception {
        StringBuffer result = new StringBuffer();
        String content = PHPLexerUtils.getFileContent(testFile);
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(content));
        
        Symbol symbol;
        result.append("<testresult testFile='").append(testFile.getName()).append("'>\n");
        result.append("    <scanner>\n");
        do {
            symbol = scanner.next_token();
            result.append("        <token id='").append(Utils.getASTScannerTokenName(symbol.sym)).append("' start='");
            result.append(symbol.left).append("' end='").append(symbol.right + "'>\n");
            result.append("            <text>");
            result.append(PHPLexerUtils.getXmlStringValue(content.substring(symbol.left, symbol.right)));
            result.append("</text>\n");
            result.append("        </token>\n");
        } while (symbol.sym != ASTPHP5Symbols.EOF);
        result.append("    </scanner>\n");
        
        scanner.reset(new FileReader(testFile));
        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
        Symbol root = parser.parse();
        if (root != null){
            Program rootnode = (Program)root.value;
        
            result.append((new PrintASTVisitor()).printTree(rootnode, 1));
        }
        result.append("</testresult>\n");
        return result.toString();
    }
}
