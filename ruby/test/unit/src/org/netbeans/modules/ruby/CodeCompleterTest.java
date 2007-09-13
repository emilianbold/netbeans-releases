/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.Action;
import javax.swing.JTextArea;
import javax.swing.text.Caret;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Completable.QueryType;
import org.netbeans.api.gsf.CompletionProposal;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.HtmlFormatter;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.api.gsfpath.classpath.ClassPath;
import org.netbeans.api.retouche.source.Source;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.TestUtil;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.DefaultLanguage;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.retouche.source.usages.Index;
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
                    new Formatter(), new BracketCompleter(), new RubyIndexer(), new StructureAnalyzer(), null);
            List<Language> languages = new ArrayList<Language>();
            languages.add(dl);
            registry.addLanguages(languages);
        }
        // Force classpath initialization
        List<ClassPath.Entry> entries = RubyInstallation.getInstance().getClassPathEntries();
        
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
            
            public void reset() {
                sb.setLength(0);
            }

            public void appendHtml(String html) {
                sb.append(html);
            }

            public void appendText(String text) {
                sb.append(text);
            }

            public void name(ElementKind kind, boolean start) {
            }

            public void parameters(boolean start) {
            }

            public void type(boolean start) {
            }

            public void deprecated(boolean start) {
            }

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

//    public void testCompletion1() throws Exception {
//        checkCompletion("testfiles/completion/lib/test1.rb", "f.e^");
//    }
//    
//    public void testCompletion2() throws Exception {
//        // This test doesn't pass yet because we need to index the -current- file
//        // before resuming
//        checkCompletion("testfiles/completion/lib/test2.rb", "Result is #{@^myfield} and #@another.");
//    }
//    
//    public void testCompletion3() throws Exception {
//        checkCompletion("testfiles/completion/lib/test2.rb", "Result is #{@myfield} and #@a^nother.");
//    }
//    
//    public void testCompletion4() throws Exception {
//        checkCompletion("testfiles/completion/lib/test2.rb", "Hell^o World");
//    }
//    
//    public void testCompletion5() throws Exception {
//        checkCompletion("testfiles/completion/lib/test2.rb", "/re^g/");
//    }
//
//    public void testCompletion6() throws Exception {
//        checkCompletion("testfiles/completion/lib/test2.rb", "class My^Test");
//    }
//    
//    // TODO: Test open classes, class inheritance, relative symbols, finding classes, superclasses, def completion, ...
    
}
