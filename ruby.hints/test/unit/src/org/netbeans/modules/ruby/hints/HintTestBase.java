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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

import java.util.Collections;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.RubyTestBase;
import java.util.Map;
import org.netbeans.api.gsf.CompilationInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.options.HintsSettings;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyHintsProvider;
import org.netbeans.modules.ruby.hints.infrastructure.RulesManager;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtil;

/**
 * Common utility methods for testing a hint
 *
 * @author Tor Norbye
 */
public abstract class HintTestBase extends RubyTestBase {

    public HintTestBase(String testName) {
        super(testName);
    }
    
    private static final String[] JRUBY_BIG_FILES = {
        // Biggest files in the standard library
        "lib/ruby/1.8/drb/drb.rb",
        "lib/ruby/1.8/rdoc/parsers/parse_rb.rb",
        "lib/ruby/1.8/rdoc/parsers/parse_f95.rb",
        "lib/ruby/1.8/net/http.rb",
        "lib/ruby/1.8/cgi.rb",
        "lib/ruby/1.8/net/imap.rb",
         // Biggest files in Rails
        "lib/ruby/gems/1.8/gems//activerecord-1.15.3/test/associations_test.rb",
        "lib/ruby/gems/1.8/gems//actionmailer-1.3.3/lib/action_mailer/vendor/text/format.rb",
        "lib/ruby/gems/1.8/gems//actionpack-1.13.3/test/controller/routing_test.rb",
        "lib/ruby/gems/1.8/gems//activerecord-1.15.3/lib/active_record/associations.rb",
        "lib/ruby/gems/1.8/gems//activerecord-1.15.3/lib/active_record/base.rb",
        "lib/ruby/gems/1.8/gems//actionpack-1.13.3/test/template/date_helper_test.rb",
    };
    
    protected List<FileObject> getBigSourceFiles() {
        FileObject jruby = findJRuby();
        
        List<FileObject> files = new ArrayList<FileObject>();
        for (String relative : JRUBY_BIG_FILES) {
            FileObject f = jruby.getFileObject(relative);
            assertNotNull(f);
            files.add(f);
        }
        
        return files;
    }

    private String annotate(BaseDocument doc, List<ErrorDescription> result, int caretOffset) throws Exception {
        Map<OffsetRange, ErrorDescription> posToDesc = new HashMap<OffsetRange, ErrorDescription>();
        Set<OffsetRange> ranges = new HashSet<OffsetRange>();
        for (ErrorDescription desc : result) {
            int start = desc.getRange().getBegin().getOffset();
            int end = desc.getRange().getEnd().getOffset();
            OffsetRange range = new OffsetRange(start, end);
            posToDesc.put(range, desc);
            ranges.add(range);
        }
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        int index = 0;
        int length = text.length();
        while (index < length) {
            int lineStart = Utilities.getRowStart(doc, index);
            int lineEnd = Utilities.getRowEnd(doc, index);
            OffsetRange lineRange = new OffsetRange(lineStart, lineEnd);
            boolean skipLine = true;
            for (OffsetRange range : ranges) {
                if (lineRange.containsInclusive(range.getStart()) || lineRange.containsInclusive(range.getEnd())) {
                    skipLine = false;
                }
            }
            if (!skipLine) {
                List<ErrorDescription> descsOnLine = null;
                int underlineStart = -1;
                int underlineEnd = -1;
                for (int i = lineStart; i <= lineEnd; i++) {
                    if (i == caretOffset) {
                        sb.append("^");
                    }
                    if (starts.containsKey(i)) {
                        if (descsOnLine == null) {
                            descsOnLine = new ArrayList<ErrorDescription>();
                        }
                        underlineStart = i-lineStart;
                        OffsetRange range = starts.get(i);
                        ErrorDescription desc = posToDesc.get(range);
                        if (desc != null) {
                            descsOnLine.add(desc);
                        }
                    }
                    if (ends.containsKey(i)) {
                        underlineEnd = i-lineStart;
                    }
                    sb.append(text.charAt(i));
                }
                if (underlineStart != -1) {
                    for (int i = 0; i < underlineStart; i++) {
                        sb.append(" ");
                    }
                    for (int i = underlineStart; i < underlineEnd; i++) {
                        sb.append("-");
                    }
                    sb.append("\n");
                }
                if (descsOnLine != null) {
                    for (ErrorDescription desc : descsOnLine) {
                        sb.append("HINT:");
                        sb.append(desc.getDescription());
                        sb.append("\n");
                        LazyFixList list = desc.getFixes();
                        if (list != null) {
                            List<Fix> fixes = list.getFixes();
                            if (fixes != null) {
                                for (Fix fix : fixes) {
                                    sb.append("FIX:");
                                    sb.append(fix.getText());
                                    sb.append("\n");
                                }
                            }
                        }
                    }
                }
            }
            index = lineEnd + 1;
        }

        return sb.toString();
    }
    
    protected boolean parseErrorsOk;
    
    protected ComputedHints getHints(NbTestCase test, AstRule hint, String relFilePath, FileObject fileObject, String caretLine) throws Exception {
        assert relFilePath == null || fileObject == null;

        // Make sure the hint is enabled
        if (!HintsSettings.isEnabled(hint)) {
            Preferences p = RulesManager.getInstance().getPreferences(hint, HintsSettings.getCurrentProfileId());
            HintsSettings.setEnabled(p, true);
        }
        
        CompilationInfo info = fileObject != null ? getInfo(fileObject) : getInfo(relFilePath);
        Node root = AstUtilities.getRoot(info);
        if (root == null) {
            if (parseErrorsOk) {
                List<ErrorDescription> result = new ArrayList<ErrorDescription>();
                int caretOffset = 0;
                return new ComputedHints(info, result, caretOffset);
            }
            assertNotNull("Unexpected parse error in test case " + 
                    FileUtil.getFileDisplayName(info.getFileObject()) + "\nErrors = " + 
                    info.getDiagnostics(), root);
        }

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

        RubyHintsProvider provider = new RubyHintsProvider();

        // Create a hint registry which contains ONLY our hint (so other registered
        // hints don't interfere with the test)
        Map<Integer, List<AstRule>> testHints = new HashMap<Integer, List<AstRule>>();
        if (hint.appliesTo(info)) {
            for (int nodeId : hint.getKinds()) {
                testHints.put(nodeId, Collections.singletonList(hint));
            }
        }
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        if (RulesManager.getInstance().getSeverity(hint) == HintSeverity.CURRENT_LINE_WARNING) {
            provider.setTestingHints(null, testHints);
            provider.computeSuggestions(info, result, caretOffset);
        } else {
            provider.setTestingHints(testHints, null);
            provider.computeHints(info, result);
        }

        return new ComputedHints(info, result, caretOffset);
    }

    protected void assertNoJRubyMatches(AstRule hint, Set<String> exceptions) throws Exception {
        List<FileObject> files = findJRubyRubyFiles();
        assertTrue(files.size() > 0);
        
        Set<String> fails = new HashSet<String>();
        for (FileObject fileObject : files) {
            ComputedHints r = getHints(this, hint, null, fileObject, null);
            CompilationInfo info = r.info;
            List<ErrorDescription> result = r.hints;
            int caretOffset = r.caretOffset;

            String annotatedSource = annotate((BaseDocument)info.getDocument(), result, caretOffset);
            
            if (annotatedSource.length() > 0) {
                // Check if there's an exception
                String name = fileObject.getNameExt();
                if (exceptions.contains(name)) {
                    continue;
                }
                
                fails.add(fileObject.getNameExt());
            }
        }
        
        assertTrue(fails.toString(), fails.size() == 0);
    }
    
    // TODO - rename to "checkHints"
    protected void findHints(NbTestCase test, AstRule hint, String relFilePath, String caretLine) throws Exception {
        findHints(test, hint, relFilePath, null, caretLine);
    }

    // TODO - rename to "checkHints"
    protected void findHints(NbTestCase test, AstRule hint, FileObject fileObject, String caretLine) throws Exception {
        findHints(test, hint, null, fileObject, caretLine);
    }
    
    // TODO - rename to "checkHints"
    protected void findHints(NbTestCase test, AstRule hint, String relFilePath, FileObject fileObject, String caretLine) throws Exception {
        ComputedHints r = getHints(test, hint, relFilePath, fileObject, caretLine);
        CompilationInfo info = r.info;
        List<ErrorDescription> result = r.hints;
        int caretOffset = r.caretOffset;
        
        String annotatedSource = annotate((BaseDocument)info.getDocument(), result, caretOffset);

        if (fileObject != null) {
            assertDescriptionMatches(fileObject, annotatedSource, true, ".hints");
        } else {
            assertDescriptionMatches(relFilePath, annotatedSource, true, ".hints");
        }
    }

    protected void applyHint(NbTestCase test, AstRule hint, String relFilePath,
            String caretLine, String fixDesc) throws Exception {
        ComputedHints r = getHints(test, hint, relFilePath, null, caretLine);
        CompilationInfo info = r.info;
        
        Fix fix = findApplicableFix(r, fixDesc);
        assertNotNull(fix);
        
        fix.implement();
        
        Document doc = info.getDocument();
        String fixed = doc.getText(0, doc.getLength());

        assertDescriptionMatches(relFilePath, fixed, true, ".fixed");
    }
    
    public void ensureRegistered(AstRule hint) throws Exception {
        Map<Integer, List<AstRule>> hints = RulesManager.getInstance().getHints();
        Set<Integer> kinds = hint.getKinds();
        for (int nodeType : kinds) {
            List<AstRule> rules = hints.get(nodeType);
            assertNotNull(rules);
            boolean found = false;
            for (AstRule rule : rules) {
                if (rule instanceof BlockVarReuse) {
                    found  = true;
                    break;
                }
            }
            
            assertTrue(found);
        }
    }

    private Fix findApplicableFix(ComputedHints r, String text) {
        boolean substringMatch = true;
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length()-1);
            substringMatch = false;
        }
        int caretOffset = r.caretOffset;
        for (ErrorDescription desc : r.hints) {
            int start = desc.getRange().getBegin().getOffset();
            int end = desc.getRange().getEnd().getOffset();
            OffsetRange range = new OffsetRange(start, end);
            if (range.containsInclusive(caretOffset)) {
                // Optionally make sure the text is the one we're after such that
                // tests can disambiguate among multiple fixes
                LazyFixList list = desc.getFixes();
                assertNotNull(list);
                for (Fix fix : list.getFixes()) {
                    if (text == null ||
                            (substringMatch && fix.getText().indexOf(text) != -1) ||
                            (!substringMatch && fix.getText().equals(text))) {
                        return fix;
                    }
                }
            }
        }
        
        return null;
    }
    
    private static class ComputedHints {
        ComputedHints(CompilationInfo info, List<ErrorDescription> hints, int caretOffset) {
            this.info = info;
            this.hints = hints;
            this.caretOffset = caretOffset;
        }

        CompilationInfo info;
        List<ErrorDescription> hints;
        int caretOffset;
    }
}
