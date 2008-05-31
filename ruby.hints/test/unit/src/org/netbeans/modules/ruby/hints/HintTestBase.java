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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

import org.netbeans.modules.ruby.RubyTestBase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jruby.ast.Node;
import org.jruby.ast.NodeType;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.api.ruby.platform.TestUtil;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintsProvider;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.Rule;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyHintsProvider;
import org.openide.filesystems.FileObject;

/**
 * Common utility methods for testing a hint
 *
 * @author Tor Norbye
 */
public abstract class HintTestBase extends RubyTestBase {

    public HintTestBase(String testName) {
        super(testName);
    }
    
    @Override
    protected HintsProvider getHintsProvider() {
        return new RubyHintsProvider();
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
        "lib/ruby/gems/1.8/gems/activerecord-2.0.2/test/associations_test.rb",
        "lib/ruby/gems/1.8/gems/actionmailer-2.0.2/lib/action_mailer/vendor/text-format-0.6.3/text/format.rb",
        "lib/ruby/gems/1.8/gems/actionpack-2.0.2/test/controller/routing_test.rb",
        "lib/ruby/gems/1.8/gems/activerecord-2.0.2/lib/active_record/associations.rb",
        "lib/ruby/gems/1.8/gems/activerecord-2.0.2/lib/active_record/base.rb",
        "lib/ruby/gems/1.8/gems/actionpack-2.0.2/test/template/date_helper_test.rb",
    };
    
    protected List<FileObject> getBigSourceFiles() {
        FileObject jruby = TestUtil.getXTestJRubyHomeFO();
        
        List<FileObject> files = new ArrayList<FileObject>();
        for (String relative : JRUBY_BIG_FILES) {
            FileObject f = jruby.getFileObject(relative);
            assertNotNull(relative, f);
            files.add(f);
        }
        
        return files;
    }


    protected void assertNoJRubyMatches(Rule hint, Set<String> exceptions) throws Exception {
        List<FileObject> files = findJRubyRubyFiles();
        assertTrue(files.size() > 0);
        
        Set<String> fails = new HashSet<String>();
        for (FileObject fileObject : files) {
            ComputedHints r = getHints(this, hint, null, fileObject, null);
            CompilationInfo info = r.info;
            List<Hint> result = r.hints;
            int caretOffset = r.caretOffset;
            if (hint.getDefaultSeverity() == HintSeverity.CURRENT_LINE_WARNING && hint instanceof RubyAstRule) {
                result = new ArrayList<Hint>(result);
                Set<NodeType> nodeTypes = ((RubyAstRule)hint).getKinds();
                Node root = AstUtilities.getRoot(info);
                List<Node> nodes = new ArrayList<Node>();
                NodeType[] nodeIds = new NodeType[nodeTypes.size()];
                int index = 0;
                for (NodeType id : nodeTypes) {
                    nodeIds[index++] = id;
                }
                AstUtilities.addNodesByType(root, nodeIds, nodes);
                BaseDocument doc = (BaseDocument) info.getDocument();
                assertNotNull(doc);
                for (Node n : nodes) {
                    int start = AstUtilities.getRange(n).getStart();
                    int lineStart = Utilities.getRowFirstNonWhite(doc, start);
                    int lineEnd = Utilities.getRowEnd(doc, start);
                    String first = doc.getText(lineStart, start-lineStart); 
                    String last = doc.getText(start, lineEnd-start);
                    if (first.indexOf("^") == -1 && last.indexOf("^") == -1) {
                        String caretLine = first + "^" + last;
                        ComputedHints r2 = getHints(this, hint, null, fileObject, caretLine);
                        result.addAll(r.hints);
                    }
                }
            }

            String annotatedSource = annotateHints((BaseDocument)info.getDocument(), result, caretOffset);
            
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
    
//    
//    
//    @SuppressWarnings("unchecked")
//    public void ensureRegistered(RubyAstRule hint) throws Exception {
//        Map<NodeType, List<RubyAstRule>> hints = (Map)RubyRulesManager.getInstance().getHints();
//        Set<NodeType> kinds = hint.getKinds();
//        for (NodeType nodeType : kinds) {
//            List<RubyAstRule> rules = hints.get(nodeType);
//            assertNotNull(rules);
//            boolean found = false;
//            for (RubyAstRule rule : rules) {
//                if (rule instanceof BlockVarReuse) {
//                    found  = true;
//                    break;
//                }
//            }
//            
//            assertTrue(found);
//        }
//    }
}
