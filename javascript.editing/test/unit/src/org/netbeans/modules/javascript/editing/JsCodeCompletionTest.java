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

package org.netbeans.modules.javascript.editing;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.text.Caret;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Completable.QueryType;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class JsCodeCompletionTest extends JsTestBase {
    
    public JsCodeCompletionTest(String testName) {
        super(testName);
    }

    private String describe(String caretLine, NameKind kind, QueryType type, List<CompletionProposal> proposals) {
        StringBuilder sb = new StringBuilder();
        sb.append("Results for " + caretLine + " with queryType=" + type + " and nameKind=" + kind);
        sb.append("\n");

        // Sort to make test more stable
        Collections.sort(proposals, new Comparator<CompletionProposal>() {

            public int compare(CompletionProposal p1, CompletionProposal p2) {
                // Smart items first
                if (p1.isSmart() != p2.isSmart()) {
                    return p1.isSmart() ? -1 : 1;
                }

                if (p1.getKind() != p2.getKind()) {
                    return p1.getKind().compareTo(p2.getKind());
                }
                
                if (!p1.getLhsHtml().equals(p2.getLhsHtml())) {
                    return p1.getLhsHtml().compareTo(p2.getLhsHtml());
                }

                String p1Rhs = p1.getRhsHtml();
                String p2Rhs = p2.getRhsHtml();
                if (p1Rhs == null) {
                    p1Rhs = "";
                }
                if (p2Rhs == null) {
                    p2Rhs = "";
                }
                if (!p1Rhs.equals(p2Rhs)) {
                    return p1Rhs.compareTo(p2Rhs);
                }

                // Yuck - tostring comparison of sets!!
                if (!p1.getModifiers().toString().equals(p2.getModifiers().toString())) {
                    return p1.getModifiers().toString().compareTo(p2.getModifiers().toString());
                }
                
                return 0;
            }
        });
        
        boolean isSmart = true;
        for (CompletionProposal proposal : proposals) {
            if (isSmart && !proposal.isSmart()) {
                sb.append("------------------------------------\n");
                isSmart = false;
            }
            
            String n = proposal.getKind().toString();
            int MAX_KIND = 10;
            if (n.length() > MAX_KIND) {
                sb.append(n.substring(0, MAX_KIND));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_KIND; i++) {
                    sb.append(" ");
                }
            }

            sb.append(" ");
            
            n = proposal.getLhsHtml();
            int MAX_LHS = 30;
            if (n.length() > MAX_LHS) {
                sb.append(n.substring(0, MAX_LHS));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_LHS; i++) {
                    sb.append(" ");
                }
            }

            sb.append("  ");

            if (proposal.getModifiers().isEmpty()) {
                n = "";
            } else {
                n = proposal.getModifiers().toString();
            }
            int MAX_MOD = 9;
            if (n.length() > MAX_MOD) {
                sb.append(n.substring(0, MAX_MOD));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_MOD; i++) {
                    sb.append(" ");
                }
            }

            sb.append("  ");
            
            sb.append(proposal.getRhsHtml());
            sb.append("\n");
            
            isSmart = proposal.isSmart();
        }
        
        return sb.toString();
    }
    
    public void checkCompletion(String file, String caretLine) throws Exception {
        // TODO call TestCompilationInfo.setCaretOffset!        
        QueryType type = QueryType.COMPLETION;
        boolean caseSensitive = true;
        NameKind kind = caseSensitive ? NameKind.PREFIX : NameKind.CASE_INSENSITIVE_PREFIX;

        String jsDir = System.getProperty("xtest.js.home");
        if (jsDir == null) {
            throw new RuntimeException("xtest.js.home property has to be set when running within binary distribution");
        }
        File clusterDir = new File(jsDir);
        if (clusterDir.exists()) {
            FileObject preindexed = FileUtil.toFileObject(clusterDir).getFileObject("preindexed");
            if (preindexed != null) {
                JsIndexer.setPreindexedDb(preindexed);
            }
        }
        initializeRegistry();
        // Force classpath initialization
        LanguageRegistry.getInstance().getLibraryUrls();
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(JsMimeResolver.JAVASCRIPT_MIME_TYPE);
        org.netbeans.modules.gsfret.source.usages.ClassIndexManager.get(language).getBootIndices();
        
        CompilationInfo ci = getInfo(file);
        String text = ci.getText();
        assertNotNull(text);
        assertNotNull(AstUtilities.getParseResult(ci));
        
        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
        }

        
        JsCodeCompletion cc = new JsCodeCompletion();
        
        HtmlFormatter formatter = new HtmlFormatter() {
            private StringBuilder sb = new StringBuilder();
            
            @Override
            public void reset() {
                sb.setLength(0);
            }

            @Override
            public void appendHtml(String html) {
                sb.append(html);
            }

            @Override
            public void appendText(String text, int fromInclusive, int toExclusive) {
                sb.append(text, fromInclusive, toExclusive);
            }
            
            @Override
            public void emphasis(boolean start) {
            }

            @Override
            public void active(boolean start) {
            }

            @Override
            public void name(ElementKind kind, boolean start) {
            }

            @Override
            public void parameters(boolean start) {
            }

            @Override
            public void type(boolean start) {
            }

            @Override
            public void deprecated(boolean start) {
            }

            @Override
            public String getText() {
                return sb.toString();
            }
        };
        boolean upToOffset = type == QueryType.COMPLETION;
        String prefix = cc.getPrefix(ci, caretOffset, upToOffset);
        if (prefix == null) {
            if (prefix == null) {
                int[] blk =
                    org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument)ci.getDocument(),
                        caretOffset);

                if (blk != null) {
                    int start = blk[0];

                    if (start < caretOffset ) {
                        if (upToOffset) {
                            prefix = ci.getDocument().getText(start, caretOffset - start);
                        } else {
                            prefix = ci.getDocument().getText(start, blk[1]-start);
                        }
                    }
                }
            }
        }

        Source js = Source.forFileObject(ci.getFileObject());
        assertNotNull(js);
        //ci.getIndex();
        //index.setDirty(js);
        js.testUpdateIndex();
        JsIndex.setClusterUrl("file:/bogus"); // No translation
        List<CompletionProposal> proposals = cc.complete(ci, caretOffset, prefix, kind, type, caseSensitive, formatter);
        
        String described = describe(caretLine, kind, type, proposals);
        assertDescriptionMatches(file, described, true, ".completion");
    }
    
    public void checkComputeMethodCall(String file, String caretLine, String fqn, String param, boolean expectSuccess) throws Exception {
        // TODO call TestCompilationInfo.setCaretOffset!        
        QueryType type = QueryType.COMPLETION;
        boolean caseSensitive = true;

        String jsDir = System.getProperty("xtest.js.home");
        if (jsDir == null) {
            throw new RuntimeException("xtest.js.home property has to be set when running within binary distribution");
        }
        File clusterDir = new File(jsDir);
        if (clusterDir.exists()) {
            FileObject preindexed = FileUtil.toFileObject(clusterDir).getFileObject("preindexed");
            if (preindexed != null) {
                JsIndexer.setPreindexedDb(preindexed);
            }
        }
        
        initializeRegistry();
        // Force classpath initialization
        LanguageRegistry.getInstance().getLibraryUrls();
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(JsMimeResolver.JAVASCRIPT_MIME_TYPE);
        org.netbeans.modules.gsfret.source.usages.ClassIndexManager.get(language).getBootIndices();
        
        CompilationInfo ci = getInfo(file);
        String text = ci.getText();
        assertNotNull(text);
        assertNotNull(AstUtilities.getParseResult(ci));
        
        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
        }
        
        JsCodeCompletion cc = new JsCodeCompletion();
        boolean upToOffset = type == QueryType.COMPLETION;
        String prefix = cc.getPrefix(ci, caretOffset, upToOffset);
        if (prefix == null) {
            if (prefix == null) {
                int[] blk =
                    org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument)ci.getDocument(),
                        caretOffset);

                if (blk != null) {
                    int start = blk[0];

                    if (start < caretOffset ) {
                        if (upToOffset) {
                            prefix = ci.getDocument().getText(start, caretOffset - start);
                        } else {
                            prefix = ci.getDocument().getText(start, blk[1]-start);
                        }
                    }
                }
            }
        }

        Source js = Source.forFileObject(ci.getFileObject());
        assertNotNull(js);
        //ci.getIndex();
        //index.setDirty(js);
        js.testUpdateIndex();
        JsIndex.setClusterUrl("file:/bogus"); // No translation
        
        IndexedFunction[] methodHolder = new IndexedFunction[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        int lexOffset = caretOffset;
        int astOffset = caretOffset;
        boolean ok = JsCodeCompletion.computeMethodCall(ci, lexOffset, astOffset, methodHolder, paramIndexHolder, anchorOffsetHolder, null);

        if (expectSuccess) {
            assertTrue(ok);
        } else {
            return;
        }
        IndexedFunction method = methodHolder[0];
        assertNotNull(method);
        int index = paramIndexHolder[0];
        assertTrue(index >= 0);
        
        // The index doesn't work right at test time - not sure why
        // it doesn't have all of the gems...
        //assertEquals(fqn, method.getFqn());
        assertEquals(param, method.getParameters().get(index));
    }
    
    public void checkPrefix(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        BaseDocument doc = (BaseDocument)info.getDocument();
        StringBuilder sb = new StringBuilder();

        JsCodeCompletion completer = new JsCodeCompletion();

        int index = 0;
        while (index < doc.getLength()) {
            int lineStart = index;
            int lineEnd = Utilities.getRowEnd(doc, index);
            if (lineEnd == -1) {
                break;
            }
            if (Utilities.getRowFirstNonWhite(doc, index) != -1) {
                String line = doc.getText(lineStart, lineEnd-lineStart);
                for (int i = lineStart; i <= lineEnd; i++) {
                    String prefix = completer.getPrefix(info, i, true); // line.charAt(i)
                    if (prefix == null) {
                        continue;
                    }
                    String wholePrefix = completer.getPrefix(info, i, false);
                    assertNotNull(wholePrefix);

                    sb.append(line +"\n");
                    //sb.append("Offset ");
                    //sb.append(Integer.toString(i));
                    //sb.append(" : \"");
                    for (int j = lineStart; j < i; j++) {
                        sb.append(' ');
                    }
                    sb.append('^');
                    sb.append(prefix.length() > 0 ? prefix : "\"\"");
                    sb.append(",");
                    sb.append(wholePrefix.length() > 0 ? wholePrefix : "\"\"");
                    sb.append("\n");
                }
            }
            
            index = lineEnd+1;
        }

        String annotatedSource = sb.toString();

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".prefixes");
    }
    
    public void testPrefix1() throws Exception {
        checkPrefix("testfiles/cc-prefix1.js");
    }
    
    public void testPrefix2() throws Exception {
        checkPrefix("testfiles/cc-prefix2.js");
    }

    public void testPrefix3() throws Exception {
        checkPrefix("testfiles/cc-prefix3.js");
    }

    public void testPrefix4() throws Exception {
        checkPrefix("testfiles/cc-prefix4.js");
    }

    public void testPrefix5() throws Exception {
        checkPrefix("testfiles/cc-prefix5.js");
    }

    public void testPrefix6() throws Exception {
        checkPrefix("testfiles/cc-prefix6.js");
    }

    public void testPrefix7() throws Exception {
        checkPrefix("testfiles/cc-prefix7.js");
    }

    public void testPrefix8() throws Exception {
        checkPrefix("testfiles/cc-prefix8.js");
    }
    
    
    private void assertAutoQuery(QueryType queryType, String source, String typedText) {
        JsCodeCompletion completer = new JsCodeCompletion();
        int caretPos = source.indexOf('^');
        source = source.substring(0, caretPos) + source.substring(caretPos+1);
        
        BaseDocument doc = getDocument(source);
        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(caretPos);
        
        QueryType qt = completer.getAutoQuery(ta, typedText);
        assertEquals(queryType, qt);
    }

    public void testAutoQuery1() throws Exception {
        assertAutoQuery(QueryType.NONE, "foo^", "o");
        assertAutoQuery(QueryType.NONE, "foo^", " ");
        assertAutoQuery(QueryType.NONE, "foo^", "c");
        assertAutoQuery(QueryType.NONE, "foo^", "d");
        assertAutoQuery(QueryType.NONE, "foo^", "f");
        assertAutoQuery(QueryType.NONE, "Foo:^", ":");
        assertAutoQuery(QueryType.NONE, "Foo::^", ":");
        assertAutoQuery(QueryType.NONE, "Foo^ ", ":");
        assertAutoQuery(QueryType.NONE, "Foo^bar", ":");
        assertAutoQuery(QueryType.NONE, "Foo:^bar", ":");
        assertAutoQuery(QueryType.NONE, "Foo::^bar", ":");
    }

    public void testAutoQuery2() throws Exception {
        assertAutoQuery(QueryType.STOP, "foo^", ";");
        assertAutoQuery(QueryType.STOP, "foo^", "[");
        assertAutoQuery(QueryType.STOP, "foo^", "(");
        assertAutoQuery(QueryType.STOP, "foo^", "{");
        assertAutoQuery(QueryType.STOP, "foo^", "\n");
    }

    public void testAutoQuery3() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo.^", ".");
        assertAutoQuery(QueryType.COMPLETION, "foo^ ", ".");
        assertAutoQuery(QueryType.COMPLETION, "foo^bar", ".");
    }

    public void testAutoQueryComments() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo^ # bar", ".");
        assertAutoQuery(QueryType.NONE, "//^foo", ".");
        assertAutoQuery(QueryType.NONE, "/* foo^*/", ".");
        assertAutoQuery(QueryType.NONE, "// foo^", ".");
    }

    public void testAutoQueryStrings() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo^ 'foo'", ".");
        assertAutoQuery(QueryType.NONE, "'^foo'", ".");
        assertAutoQuery(QueryType.NONE, "/f^oo/", ".");
        assertAutoQuery(QueryType.NONE, "\"^\"", ".");
        assertAutoQuery(QueryType.NONE, "\" foo^ \"", ".");
    }

//    public void testAutoQueryRanges() throws Exception {
//        assertAutoQuery(QueryType.NONE, "x..^", ".");
//        assertAutoQuery(QueryType.NONE, "x..^5", ".");
//    }

//    public void testCompletion1() throws Exception {
//        checkCompletion("testfiles/completion/lib/test1.js", "f.e^");
//    }
//    
//    public void testCompletion2() throws Exception {
//        // This test doesn't pass yet because we need to index the -current- file
//        // before resuming
//        checkCompletion("testfiles/completion/lib/test2.js", "Result is #{@^myfield} and #@another.");
//    }
//    
//    public void testCompletion3() throws Exception {
//        checkCompletion("testfiles/completion/lib/test2.js", "Result is #{@myfield} and #@a^nother.");
//    }
//    

    public void testLocalCompletion1() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.js", "^alert('foo1");
    }

    public void testLocalCompletion2() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.js", "^alert('foo2");
    }
    
    public void test129036() throws Exception {
        checkCompletion("testfiles/completion/lib/test129036.js", "my^ //Foo");
    }
    
    public void testCompletionStringCompletion1() throws Exception {
        checkCompletion("testfiles/completion/lib/test1.js", "Hell^o World");
    }

    public void testCompletionStringCompletion2() throws Exception {
        checkCompletion("testfiles/completion/lib/test1.js", "\"f\\^oo\"");
    }
    
    public void testCompletionRegexpCompletion1() throws Exception {
        checkCompletion("testfiles/completion/lib/test1.js", "/re^g/");
    }

    public void testCompletionRegexpCompletion2() throws Exception {
        checkCompletion("testfiles/completion/lib/test1.js", "/b\\^ar/");
    }

//
//    public void testCompletion6() throws Exception {
//        checkCompletion("testfiles/completion/lib/test2.js", "class My^Test");
//    }
//    
//    // TODO: Test open classes, class inheritance, relative symbols, finding classes, superclasses, def completion, ...

    
    
// The call tests don't work yet because I don't have a preindexed database for jsstubs
// (and the test infrastructure refuses to update the index for test files themselves)
//    public void testCall1() throws Exception {
//        //checkComputeMethodCall("testfiles/calls/call1.js", "foo2(^x);", "Foo#bar", "name", true);
//        checkComputeMethodCall("testfiles/calls/call1.js", "x.addEventListener(type, ^listener, useCapture)", "Foo#bar", "name", true);
//    }
//
//    public void testCall2() throws Exception {
//        checkComputeMethodCall("testfiles/calls/call1.js", "foo1(^);",
//                "Foo#bar", "name", true);
//    }
//
//    public void testCall3() throws Exception {
//        checkComputeMethodCall("testfiles/calls/call1.js", "foo3(x^,y)",
//                "Foo#bar", "name", false);
//    }
//    public void testCall4() throws Exception {
//        checkComputeMethodCall("testfiles/calls/call2.js", "foo3(x,^)",
//                "Foo#bar", "name", false);
//    }
}
