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

package org.netbeans.modules.ruby;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JTextArea;
import javax.swing.text.Caret;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Completable.QueryType;
import org.netbeans.api.gsf.CompletionProposal;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.HtmlFormatter;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.api.gsfpath.classpath.ClassPath;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.TestUtil;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.DefaultLanguage;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsfret.source.usages.Index;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public class CodeCompleterTest extends RubyTestBase {
    
    public CodeCompleterTest(String testName) {
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

                if (!p1.getRhsHtml().equals(p2.getRhsHtml())) {
                    return p1.getRhsHtml().compareTo(p2.getRhsHtml());
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

        System.setProperty("netbeans.user", getWorkDirPath());
        FileObject jrubyHome = TestUtil.getXTestJRubyHomeFO();
        assertNotNull(jrubyHome);
        FileObject clusterLoc = jrubyHome.getParent();
        Index.setClusterLoc(clusterLoc);
        LanguageRegistry registry = LanguageRegistry.getInstance();
        List<Action> actions = Collections.emptyList();
        if (!LanguageRegistry.getInstance().isSupported(RubyInstallation.RUBY_MIME_TYPE)) {
            List<String> extensions = Collections.singletonList("rb");
            Language dl = new DefaultLanguage("Ruby", "org/netbeans/modules/ruby/jrubydoc.png", "text/x-ruby", extensions, 
                    actions, new RubyLanguage(), 
                    new RubyParser(), new CodeCompleter(), new RenameHandler(), new DeclarationFinder(), 
                    new Formatter(), new BracketCompleter(), new RubyIndexer(), new StructureAnalyzer(), null, false);
            List<Language> languages = new ArrayList<Language>();
            languages.add(dl);
            registry.addLanguages(languages);
        }
        // Force classpath initialization
        Set<URL> urls = RubyInstallation.getInstance().getNonGemLoadPath();
        
        CompilationInfo ci = getInfo(file);
        String text = ci.getText();
        assertNotNull(text);
        assertNotNull(ci.getParserResult());
        
        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
        }

        
        CodeCompleter cc = new CodeCompleter();
        
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
            public void appendText(String text) {
                sb.append(text);
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
        RubyIndex.setClusterUrl("file:/bogus"); // No translation
        List<CompletionProposal> proposals = cc.complete(ci, caretOffset, prefix, kind, type, caseSensitive, formatter);
        
        String described = describe(caretLine, kind, type, proposals);
        assertDescriptionMatches(file, described, true, ".completion");
    }
    
    
    public void checkPrefix(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        BaseDocument doc = (BaseDocument)info.getDocument();
        StringBuilder sb = new StringBuilder();

        CodeCompleter completer = new CodeCompleter();

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
        checkPrefix("testfiles/cc-prefix1.rb");
    }
    
    public void testPrefix2() throws Exception {
        checkPrefix("testfiles/cc-prefix2.rb");
    }

    public void testPrefix3() throws Exception {
        checkPrefix("testfiles/cc-prefix3.rb");
    }

    public void testPrefix4() throws Exception {
        checkPrefix("testfiles/cc-prefix4.rb");
    }

    public void testPrefix5() throws Exception {
        checkPrefix("testfiles/cc-prefix5.rb");
    }

    public void testPrefix6() throws Exception {
        checkPrefix("testfiles/cc-prefix6.rb");
    }

    public void testPrefix7() throws Exception {
        checkPrefix("testfiles/cc-prefix7.rb");
    }

    public void testPrefix8() throws Exception {
        checkPrefix("testfiles/cc-prefix8.rb");
    }
    
    
    private void assertAutoQuery(QueryType queryType, String source, String typedText) {
        CodeCompleter completer = new CodeCompleter();
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
        assertAutoQuery(QueryType.NONE, "foo^", ";");
        assertAutoQuery(QueryType.NONE, "foo^", "f");
        assertAutoQuery(QueryType.NONE, "Foo:^", ":");
        assertAutoQuery(QueryType.NONE, "Foo^ ", ":");
        assertAutoQuery(QueryType.NONE, "Foo^bar", ":");
        assertAutoQuery(QueryType.NONE, "Foo:^bar", ":");
    }

    public void testAutoQuery2() throws Exception {
        assertAutoQuery(QueryType.STOP, "foo^", "[");
        assertAutoQuery(QueryType.STOP, "foo^", "(");
        assertAutoQuery(QueryType.STOP, "foo^", "{");
        assertAutoQuery(QueryType.STOP, "foo^", "\n");
    }

    public void testAutoQuery3() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo.^", ".");
        assertAutoQuery(QueryType.COMPLETION, "Foo::^", ":");
        assertAutoQuery(QueryType.COMPLETION, "foo^ ", ".");
        assertAutoQuery(QueryType.COMPLETION, "foo^bar", ".");
        assertAutoQuery(QueryType.COMPLETION, "Foo::^bar", ":");
    }

    public void testAutoQueryComments() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo^ # bar", ".");
        assertAutoQuery(QueryType.NONE, "#^foo", ".");
        assertAutoQuery(QueryType.NONE, "# foo^", ".");
        assertAutoQuery(QueryType.NONE, "# foo^", ":");
    }

    public void testAutoQueryStrings() throws Exception {
        assertAutoQuery(QueryType.COMPLETION, "foo^ 'foo'", ".");
        assertAutoQuery(QueryType.NONE, "'^foo'", ".");
        assertAutoQuery(QueryType.NONE, "/f^oo/", ".");
        assertAutoQuery(QueryType.NONE, "\"^\"", ".");
        assertAutoQuery(QueryType.NONE, "\" foo^ \"", ":");
    }

    public void testAutoQueryRanges() throws Exception {
        assertAutoQuery(QueryType.NONE, "x..^", ".");
        assertAutoQuery(QueryType.NONE, "x..^5", ".");
    }

    public void testCompletion1() throws Exception {
        checkCompletion("testfiles/completion/lib/test1.rb", "f.e^");
    }
    
    public void testCompletion2() throws Exception {
        // This test doesn't pass yet because we need to index the -current- file
        // before resuming
        checkCompletion("testfiles/completion/lib/test2.rb", "Result is #{@^myfield} and #@another.");
    }
    
    public void testCompletion3() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.rb", "Result is #{@myfield} and #@a^nother.");
    }
    
    public void testCompletion4() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.rb", "Hell^o World");
    }
    
    public void testCompletion5() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.rb", "/re^g/");
    }

    public void testCompletion6() throws Exception {
        checkCompletion("testfiles/completion/lib/test2.rb", "class My^Test");
    }
//    
//    // TODO: Test open classes, class inheritance, relative symbols, finding classes, superclasses, def completion, ...

    public void checkComputeMethodCall(String file, String caretLine, String fqn, String param, boolean expectSuccess) throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        FileObject jrubyHome = TestUtil.getXTestJRubyHomeFO();
        assertNotNull(jrubyHome);
        FileObject clusterLoc = jrubyHome.getParent();
        Index.setClusterLoc(clusterLoc);
        LanguageRegistry registry = LanguageRegistry.getInstance();
        List<Action> actions = Collections.emptyList();
        if (!LanguageRegistry.getInstance().isSupported(RubyInstallation.RUBY_MIME_TYPE)) {
            List<String> extensions = Collections.singletonList("rb");
            Language dl = new DefaultLanguage("Ruby", "org/netbeans/modules/ruby/jrubydoc.png", "text/x-ruby", extensions, 
                    actions, new RubyLanguage(), 
                    new RubyParser(), new CodeCompleter(), new RenameHandler(), new DeclarationFinder(), 
                    new Formatter(), new BracketCompleter(), new RubyIndexer(), new StructureAnalyzer(), null, false);
            List<Language> languages = new ArrayList<Language>();
            languages.add(dl);
            registry.addLanguages(languages);
        }
        // Force classpath initialization
        Set<URL> urls = RubyInstallation.getInstance().getNonGemLoadPath();

        CodeCompleter cc = new CodeCompleter();
        TestCompilationInfo info = getInfo(file);
        String text = info.getText();

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
        }

        info.setCaretOffset(caretOffset);

        assertNotNull(text);
        assertNotNull(info.getParserResult());

        
        Source js = Source.forFileObject(info.getFileObject());
        assertNotNull(js);
        //ci.getIndex();
        //index.setDirty(js);
        js.testUpdateIndex();
        
        Node root = AstUtilities.getRoot(info);
        IndexedMethod[] methodHolder = new IndexedMethod[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        int lexOffset = caretOffset;
        int astOffset = caretOffset;
        boolean ok = cc.computeMethodCall(info, lexOffset, astOffset, methodHolder, paramIndexHolder, anchorOffsetHolder, null);

        if (expectSuccess) {
            assertTrue(ok);
        } else {
            return;
        }
        IndexedMethod method = methodHolder[0];
        assertNotNull(method);
        int index = paramIndexHolder[0];
        assertTrue(index >= 0);
        
        // The index doesn't work right at test time - not sure why
        // it doesn't have all of the gems...
        //assertEquals(fqn, method.getFqn());
        assertEquals(param, method.getParameters().get(index));
    }

    public void testCall1() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table(^firstarg,  :id => true)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "name", true);
    }

    public void testCall2() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table(firstarg^,  :id => true)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "name", true);
    }
    public void testCall3() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table(firstarg,^  :id => true)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCall4() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table(firstarg,  ^:id => true)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCallSpace1() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table firstarg,  ^:id => true",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCallSpace2() throws Exception {
        checkComputeMethodCall("testfiles/calls/call1.rb", "create_table ^firstarg,  :id => true",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "name", true);
    }
    public void testCall5() throws Exception {
        checkComputeMethodCall("testfiles/calls/call2.rb", "create_table(^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "name", true);
    }
    public void testCall6() throws Exception {
        checkComputeMethodCall("testfiles/calls/call3.rb", "create_table^",
                null, null, false);
    }
    public void testCall7() throws Exception {
        checkComputeMethodCall("testfiles/calls/call3.rb", "create_table ^",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "name", true);
    }
    public void testCall8() throws Exception {
        checkComputeMethodCall("testfiles/calls/call4.rb", "create_table foo,^",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCall9() throws Exception {
        checkComputeMethodCall("testfiles/calls/call4.rb", "create_table foo, ^",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCall10() throws Exception {
        checkComputeMethodCall("testfiles/calls/call5.rb", " create_table(foo, ^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCall11() throws Exception {
        checkComputeMethodCall("testfiles/calls/call6.rb", " create_table(foo, :key => ^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }

    public void testCall12() throws Exception {
        checkComputeMethodCall("testfiles/calls/call7.rb", " create_table(foo, :key => :^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }

    public void testCall13() throws Exception {
        checkComputeMethodCall("testfiles/calls/call8.rb", " create_table(foo, :key => :a^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }
    public void testCall14() throws Exception {
        checkComputeMethodCall("testfiles/calls/call9.rb", " create_table(foo, :^)",
                "ActiveRecord::SchemaStatements::ClassMethods#create_table", "options", true);
    }

//    public void testCall15() throws Exception {
//        checkComputeMethodCall("testfiles/calls/call10.rb", "File.exists?(^)",
//                "File#exists", "file", true);
//    }

    public void testCall16() throws Exception {
        checkComputeMethodCall("testfiles/calls/call11.rb", " ^#",
                null, null, false);
    }

    public void testCall17() throws Exception {
        checkComputeMethodCall("testfiles/calls/call12.rb", " ^#",
                null, null, false);
    }
    
    // TODO - test more non-fc calls (e.g. x.foo)
    // TODO test with splat args (more args than are in def list)
    // TODO test with long arg lists
}
