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
package org.netbeans.modules.ruby.extrahints;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.types.INameNode;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.EditList;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.hints.spi.PreviewableFix;
import org.netbeans.modules.ruby.hints.spi.RuleContext;
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
 * 
 * @author Tor Norbye
 */
public class Deprecations implements AstRule {
    
    private static class Deprecation {
        private String oldMethodName;
        private String newMethodName;
        /** {0} is the old name, {1} is the new name */
        private String descriptionKey;
        private String helpUrl;

        public Deprecation(String oldMethodName, String newMethodName, String descriptionKey,
                String helpUrl) {
            this.oldMethodName = oldMethodName;
            this.newMethodName = newMethodName;
            this.descriptionKey = descriptionKey;
            this.helpUrl = helpUrl;
        }
    }
    
    static Set<Integer> kinds = new HashSet<Integer>();
    static Map<String,String> deprecatedMethods = new HashMap<String,String>();
    static Map<String,Deprecation> deprecations = new HashMap<String,Deprecation>();
    static {
        kinds.add(NodeTypes.FCALLNODE);
        kinds.add(NodeTypes.VCALLNODE);
        kinds.add(NodeTypes.CALLNODE);

        // Note - these replacements will be offered as fixes so if more complicated code block
        // replacements are needed this will need to be revised.
        deprecatedMethods.put("require_gem", "gem"); // NOI18N
        
        Deprecation require_gem = new Deprecation("require_gem", "gem", "HELP_require_gem", "http://www.ruby-forum.com/topic/136010"); // NOI18N
        deprecations.put(require_gem.oldMethodName, require_gem);
    }

    public Deprecations() {
    }

    public boolean appliesTo(CompilationInfo info) {
        return true;
    }

    public Set<Integer> getKinds() {
        return kinds;
    }

    public void run(RuleContext context, List<Description> result) {
        Node node = context.node;
        CompilationInfo info = context.compilationInfo;

        // Look for use of deprecated fields
        String name = ((INameNode)node).getName();

        if (deprecatedMethods.containsKey(name)) {
            // Add a warning - you're using a deprecated field. Use the
            // method/attribute instead!
            OffsetRange range = AstUtilities.getNameRange(node);

            range = LexUtilities.getLexerOffsets(info, range);
            if (range != OffsetRange.NONE) {
                Deprecation deprecation = deprecations.get(name);

                String message= NbBundle.getMessage(Deprecations.class, deprecation.descriptionKey != null ?
                    deprecation.descriptionKey : "DeprecatedMethodUse", 
                    deprecation.oldMethodName, deprecation.newMethodName);

                List<Fix> fixes = new ArrayList<Fix>();
                fixes.add(new DeprecationCallFix(info, node, deprecation, false));
                if (deprecation.helpUrl != null) {
                    fixes.add(new DeprecationCallFix(info, node, deprecation, true));
                }
                
                Description desc = new Description(this, message, info.getFileObject(), range, fixes, 100);
                result.add(desc);
            }
        }
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

        private CompilationInfo info;
        private Node node;
        private Deprecation deprecation;
        private boolean help;

        public DeprecationCallFix(CompilationInfo info, Node node, Deprecation deprecation, boolean help) {
            this.info = info;
            this.node = node;
            this.deprecation = deprecation;
            this.help = help;
        }

        public String getDescription() {
            if (help) {
                return NbBundle.getMessage(Deprecations.class, "ShowDeprecationHelp");
            } else {
                return NbBundle.getMessage(Deprecations.class, "DeprecationFix", 
                    deprecation.oldMethodName, deprecation.newMethodName);
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
            BaseDocument doc = (BaseDocument) info.getDocument();
            OffsetRange range = AstUtilities.getCallRange(node);
            
            EditList list = new EditList(doc);
            if (range != OffsetRange.NONE) {
                list.replace(range.getStart(), range.getLength(), deprecation.newMethodName, false, 0);
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
