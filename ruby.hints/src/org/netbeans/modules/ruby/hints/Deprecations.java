/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.INameNode;
import org.jruby.util.ByteList;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;

/**
 * <p>A hint which looks at all files and scans for usages of deprecated
 * constructs; it adds warnings (and in some cases fixes) for these.
 * </p>
 *
 * @todo Offer customized messages per suggested fix, e.g. explaining why a particular
 *   fix is necessary, which versions it applies to, and perhaps a link to more information.
 * @todo See http://blade.nagaokaut.ac.jp/cgi-bin/scat.rb/ruby/ruby-core/2397
 *  Looks like deprecations are marked like this:
 *  +warn "Warning: getopts.rb is deprecated after Ruby 1.8.1"
 * 
 * @author Tor Norbye
 */
public class Deprecations extends RubyAstRule {
    
    static class Deprecation {
        final String oldName;
        final String newName;
        /** Key: {0} is the old name, {1} is the new name */
        final String descriptionKey;
        final String helpUrl;

        public Deprecation(String oldName, String newName, String descriptionKey,
                String helpUrl) {
            this.oldName = oldName;
            this.newName = newName;
            this.descriptionKey = descriptionKey;
            this.helpUrl = helpUrl;
        }
    }
    
    static Set<NodeType> kinds = new HashSet<NodeType>();
    private static Map<String,Deprecation> deprecatedMethods = new HashMap<String,Deprecation>();
    private static Map<String,Deprecation> deprecatedRequires = new HashMap<String,Deprecation>();
    static {
        kinds.add(NodeType.FCALLNODE);
        kinds.add(NodeType.VCALLNODE);
        kinds.add(NodeType.CALLNODE);

        Deprecation require_gem = new Deprecation("require_gem", "gem", "HELP_require_gem", "http://www.ruby-forum.com/topic/136010"); // NOI18N
        deprecatedMethods.put(require_gem.oldName, require_gem);
        
        Deprecation assert_raises = new Deprecation("assert_raises", "assert_raise", "HELP_assert_raises", "http://blade.nagaokaut.ac.jp/cgi-bin/scat.rb/ruby/ruby-talk/155815"); // NOI18N
        deprecatedMethods.put(assert_raises.oldName, assert_raises);
        
        // Deprecated requires
        Deprecation d = new Deprecation("getopts", "optparse", null, null); // NOI18N
        deprecatedRequires.put(d.oldName, d);
        
        d = new Deprecation("cgi-lib", "cgi", null, null); // NOI18N
        deprecatedRequires.put(d.oldName, d);
        
        d = new Deprecation("importenv", "(no replacement)", null, null); // NOI18N
        deprecatedRequires.put(d.oldName, d);
        
        d = new Deprecation("parsearg", "optparse", null, null); // NOI18N
        deprecatedRequires.put(d.oldName, d);

        d = new Deprecation("ftools", "fileutils", "HELP_ftools", null); // NOI18N
        deprecatedRequires.put(d.oldName, d);
    }

    public Deprecations() {
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public Set<NodeType> getKinds() {
        return kinds;
    }

    public void run(RubyRuleContext context, List<Hint> result) {
        Node node = context.node;
        ParserResult info = context.parserResult;

        // Look for use of deprecated fields
        String name = ((INameNode)node).getName();

        Deprecation deprecation = null;
        final boolean isRequire;
        if ("require".equals(name)) { // NOI18N
            isRequire = true;
            // It's a require-completion.
            String require = getStringArg(node);
            if (require != null) {
                deprecation = deprecatedRequires.get(require);
            }
        } else if (deprecatedMethods.containsKey(name)) {
            isRequire = false;
            deprecation = deprecatedMethods.get(name);
        } else {
            return;
        }

        if (deprecation != null) {
            // Add a warning - you're using a deprecated field. Use the
            // method/attribute instead!
            OffsetRange range = AstUtilities.getNameRange(node);

            range = LexUtilities.getLexerOffsets(info, range);
            if (range != OffsetRange.NONE) {
                String defaultKey = isRequire ? "DeprecatedRequire" : "DeprecatedMethodUse"; // NOI18N
                String message = NbBundle.getMessage(Deprecations.class, deprecation.descriptionKey != null ?
                    deprecation.descriptionKey : defaultKey, 
                    deprecation.oldName, deprecation.newName);

                List<HintFix> fixes = new ArrayList<HintFix>();
                if (!isRequire) {
                    fixes.add(new DeprecationCallFix(context, node, deprecation, false));
                }
                if (deprecation.helpUrl != null) {
                    fixes.add(new DeprecationCallFix(context, node, deprecation, true));
                }
                
                Hint desc = new Hint(this, message, RubyUtils.getFileObject(info), range, fixes, 100);
                result.add(desc);
            }
        }
    }
    
    private static String getStringArg(Node node) {
        if (node.getNodeType() == NodeType.FCALLNODE) {
            Node argsNode = ((FCallNode)node).getArgsNode();

            if (argsNode instanceof ListNode) {
                ListNode args = (ListNode)argsNode;

                if (args.size() > 0) {
                    Node n = args.get(0);

                    // For dynamically computed strings, we have n instanceof DStrNode
                    // but I can't handle these anyway
                    if (n instanceof StrNode) {
                        String require = ((StrNode)n).getValue();
                        
                        if ((require != null) && (require.length() > 0)) {
                            return require.toString();
                        }
                    }
                }
            }
        }
        
        return null;
    }

    public void cancel() {
        // Does nothing
    }

    public String getId() {
        return "Deprecations"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(Deprecations.class, "Deprecation");
    }

    public String getDescription() {
        return NbBundle.getMessage(Deprecations.class, "DeprecationDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public boolean showInTasklist() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }
    
    private static class DeprecationCallFix implements PreviewableFix {

        private final RubyRuleContext context;
        private final Node node;
        private final Deprecation deprecation;
        private final boolean help;

        public DeprecationCallFix(RubyRuleContext context, Node node, Deprecation deprecation, boolean help) {
            this.context = context;
            this.node = node;
            this.deprecation = deprecation;
            this.help = help;
        }

        public String getDescription() {
            if (help) {
                return NbBundle.getMessage(Deprecations.class, "ShowDeprecationHelp");
            } else {
                return NbBundle.getMessage(Deprecations.class, "DeprecationFix", 
                    deprecation.oldName, deprecation.newName);
            }
        }

        public void implement() throws Exception {
            if (help) {
                URL url = new URL(deprecation.helpUrl);
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            } else {
                EditList edits = getEditList();
                if (edits != null) {
                    edits.apply();
                }
            }
        }

        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            OffsetRange range = AstUtilities.getCallRange(node);
            
            EditList list = new EditList(doc);
            if (range != OffsetRange.NONE) {
                if ("require_gem".equals(deprecation.oldName)) { // NOI18N
                    // Special case; see Dr. Nic's advice in my blog entry's comments
                    // http://blogs.sun.com/tor/entry/require_gem
                    String gemName = getStringArg(node);
                    int rowEnd = Utilities.getRowEnd(doc, range.getStart());
                    list.replace(range.getStart(), range.getLength(), deprecation.newName, false, 0);
                    if (gemName != null) {
                        list.replace(rowEnd, 0, "\nrequire \"" + gemName + "\"", false, 1); // NOI18N
                    }
                    list.setFormatAll(true);
                } else {
                    list.replace(range.getStart(), range.getLength(), deprecation.newName, false, 0);
                }
            }
            
            return list;
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }

        public boolean canPreview() {
            return !help;
        }
    }
}
