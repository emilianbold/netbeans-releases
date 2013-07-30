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
package org.netbeans.modules.php.editor.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.PHPCodeCompletionTestBase;
import org.netbeans.modules.php.editor.actions.FixUsesAction.Options;
import org.netbeans.modules.php.editor.actions.ImportData.ItemVariant;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class FixUsesPerformerTest extends PHPCodeCompletionTestBase {
    private Index index;

    public FixUsesPerformerTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        QuerySupport querySupport = QuerySupportFactory.get(Arrays.asList(createSourceClassPathsForTest()));
        index = ElementQueryFactory.createIndexQuery(querySupport);
    }

    public void testIssue210093_01() throws Exception {
        String[] selections = new String[] {"\\Issue\\Martin\\Pondeli"};
        Options options = new Options(false, false, true, false);
        performTest("function testFail(\\Issue\\Martin\\Pond^eli $param) {}", selections, true, options);
    }

    public void testIssue210093_02() throws Exception {
        String[] selections = new String[] {"\\Issue\\Martin\\Pondeli"};
        Options options = new Options(false, false, false, false);
        performTest("function testFail(\\Issue\\Martin\\Pond^eli $param) {}", selections, true, options);
    }

    public void testIssue211566_01() throws Exception {
        String[] selections = new String[] {"\\Foo\\Bar\\Baz"};
        Options options = new Options(false, false, false, false);
        performTest("new \\Foo\\Bar\\B^az(); //HERE", selections, true, options);
    }

    public void testIssue211566_02() throws Exception {
        String[] selections = new String[] {"\\Foo\\Bar\\Baz"};
        Options options = new Options(false, false, true, false);
        performTest("new \\Foo\\Bar\\B^az(); //HERE", selections, true, options);
    }

    public void testIssue214699() throws Exception {
        String[] selections = new String[] {"\\Foo\\Bar\\ClassName", "\\Baz\\Bat\\ClassName", "\\Fom\\Bom\\ClassName"};
        performTest("$a = new ClassName();^//HERE", selections);
    }

    public void testIssue211585_01() throws Exception {
        String[] selections = new String[] {"\\Foo\\Bar\\ClassName", "\\Baz\\Bat\\ClassName", "\\Fom\\Bom\\ClassName"};
        Options options = new Options(false, false, true, true);
        performTest("$a = new ClassName();^//HERE", selections, false, options);
    }

    public void testIssue211585_02() throws Exception {
        String[] selections = new String[] {"\\Fom\\Bom\\ClassName", "\\Foo\\Bar\\ClassName", "\\Baz\\Bat\\ClassName"};
        Options options = new Options(false, false, true, true);
        performTest("$a = new ClassName();^//HERE", selections, false, options);
    }

    public void testIssue233527() throws Exception {
        String[] selections = new String[] {"\\NS1\\NS2\\SomeClass", "\\NS1\\NS2\\SomeClass"};
        Options options = new Options(false, false, true, true);
        performTest("public function test(SomeClass $a) {^", selections, false, options);
    }

    private String getTestResult(final String fileName, final String caretLine, final String[] selections, final boolean removeUnusedUses, final Options options) throws Exception {
        FileObject testFile = getTestFile(fileName);

        Source testSource = getTestSource(testFile);

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }
        final String[] result = new String[1];
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {

            @Override
            public void run(final ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                if (r != null) {
                    assertTrue(r instanceof ParserResult);
                    PHPParseResult phpResult = (PHPParseResult)r;
                    Map<String, List<UsedNamespaceName>> usedNames = new UsedNamesComputer(phpResult, caretOffset).computeNames();
                    NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(phpResult.getModel().getFileScope(), caretOffset);
                    Options currentOptions = options;
                    Document document = phpResult.getSnapshot().getSource().getDocument(false);
                    if (currentOptions == null) {
                        CodeStyle codeStyle = CodeStyle.get(document);
                        currentOptions = new FixUsesAction.Options(codeStyle);
                    }
                    ImportData importData = new ImportDataCreator(usedNames, index, namespaceScope.getNamespaceName(), currentOptions).create();
                    final List<ItemVariant> properSelections = new ArrayList<ItemVariant>();
                    for (String selection : selections) {
                        properSelections.add(new ItemVariant(selection, ItemVariant.UsagePolicy.CAN_BE_USED));
                    }
                    importData.caretPosition = caretOffset;
                    FixUsesPerformer fixUsesPerformer = new FixUsesPerformer(phpResult, importData, properSelections, removeUnusedUses, currentOptions);
                    fixUsesPerformer.perform();
                    result[0] = document.getText(0, document.getLength());
                }
            }
        });
        return result[0];
    }

    private void performTest(final String caretLine, final String[] selections) throws Exception {
        performTest(caretLine, selections, true, null);
    }

    private void performTest(final String caretLine, final String[] selections, final boolean removeUnusedUses) throws Exception {
        performTest(caretLine, selections, removeUnusedUses, null);
    }

    private void performTest(final String caretLine, final String[] selections, final boolean removeUnusedUses, final Options options) throws Exception {
        String exactFileName = getTestPath();
        String result = getTestResult(exactFileName, caretLine, selections, removeUnusedUses, options);
        assertDescriptionMatches(exactFileName, result, false, ".fixUses");
    }

    protected FileObject[] createSourceClassPathsForTest() {
        final File folder = new File(getDataDir(), getTestFolderPath());
        return new FileObject[]{FileUtil.toFileObject(folder)};
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), getTestFolderPath()))
            })
        );
    }

    private String getTestFolderPath() {
        return "testfiles/actions/" + getTestName();//NOI18N
    }

    private String getTestPath() {
        return getTestFolderPath() + "/" + getName() + ".php";//NOI18N
    }

    private String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

}
