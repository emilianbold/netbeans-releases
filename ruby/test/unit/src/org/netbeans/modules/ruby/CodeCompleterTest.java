/*
 * CodeCompleterTest.java
 * JUnit based test
 *
 * Created on July 6, 2007, 12:09 PM
 */

package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JTextArea;
import javax.swing.text.Caret;
import junit.framework.TestCase;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Completable.QueryType;
import org.netbeans.api.gsf.CompletionProposal;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.HtmlFormatter;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.api.gsf.ParameterInfo;
import org.netbeans.api.gsfpath.classpath.ClassPath;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gsf.DefaultLanguage;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class CodeCompleterTest extends RubyTestBase {
    
    public CodeCompleterTest(String testName) {
        super(testName);
    }

//    public void testCompletion1() throws Exception {
//        System.setProperty("ruby.interpreter", FileUtil.toFile(findJRuby().getFileObject("bin/jruby")).getAbsolutePath());
//        LanguageRegistry registry = LanguageRegistry.getInstance();
//        List<Action> actions = Collections.emptyList();
//        List<String> extensions = Collections.singletonList("rb");
//        Language dl = new DefaultLanguage("Ruby", "org/netbeans/modules/ruby/jrubydoc.png", "text/x-ruby", extensions, 
//                actions, new RubyLanguage(), 
//                new RubyParser(), new CodeCompleter(), new RenameHandler(), new DeclarationFinder(), 
//                new Formatter(), new BracketCompleter(), new RubyIndexer(), new StructureAnalyzer(), null);
//        List<Language> languages = new ArrayList<Language>();
//        languages.add(dl);
//        registry.addLanguages(languages);
//        List<ClassPath.Entry> entries = RubyInstallation.getInstance().getClassPathEntries();
//        for (ClassPath.Entry entry : entries) {
//            System.out.println(entry.getURL());
//        }
//        
//        CompilationInfo ci = getInfo("testfiles/completion1.rb");
//        assertEquals("completion1.rb", ci.getFileObject().getNameExt());
//        assertNotNull(ci.getText());
//        assertNotNull(ci.getParserResult());
//        
//        int offset = 16;
//        
//        CodeCompleter cc = new CodeCompleter();
//        boolean caseSensitive = false;
//        String prefix = "e";
//        
//        HtmlFormatter formatter = null;
//        List<CompletionProposal> proposals = cc.complete(ci, offset, prefix, NameKind.CASE_INSENSITIVE_PREFIX, QueryType.COMPLETION, caseSensitive, formatter);
//        assertTrue(proposals.size() > 8);
//    }

    
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
}
