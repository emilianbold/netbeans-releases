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

package org.netbeans.modules.ruby.extrahints;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.jruby.nb.ast.HashNode;
import org.jruby.nb.ast.ListNode;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.NodeType;
import org.jruby.nb.ast.StrNode;
import org.jruby.nb.ast.types.INameNode;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.NbBundle;

/**
 * Detect duplicate hash keys.
 *
 * @author Tor Norbye
 */
public class DuplicateHashKeys extends RubyAstRule {
    
    public DuplicateHashKeys() {
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public Set<NodeType> getKinds() {
        return Collections.singleton(NodeType.HASHNODE);
    }

    public void run(RubyRuleContext context, List<Hint> result) {
        Node node = context.node;
        AstPath path = context.path;
        ParserResult info = context.parserResult;

        // Note - we're placing both Strings and ByteList instances into this
        // Set<CharSequence>. A String and a ByteList will never be equal. That's
        // intentional. ByteLists correspond to actual string keys, and Strings correspond
        // to symbol keys - and these are NOT equal, e.g. you can have both
        //  "a" and :a as keys in a map and thee are not duplicates!
        Set<CharSequence> names = new HashSet<CharSequence>();
        Set<CharSequence> duplicateNames = null;
        HashNode hash = (HashNode) node;
        ListNode list = hash.getListNode();
        List<Node> children = list.childNodes();
        for (int i = 0, n = children.size(); i < n; i += 2) { // +=2: Skip value nodes
            Node keyNode = children.get(i);
            CharSequence name = getKeyString(keyNode);
            if (name != null) {
                if (names.contains(name)) {
                    // Error!
                    if (duplicateNames == null) {
                        duplicateNames = new HashSet<CharSequence>();
                    }
                    duplicateNames.add(name);
                } else {
                    names.add(name);
                }
            }
        }

        if (duplicateNames != null) {
            for (int i = 0, n = children.size(); i < n; i += 2) { // +=2: Skip value nodes
                Node keyNode = children.get(i);
                CharSequence name = getKeyString(keyNode);
                if (name != null && duplicateNames.contains(name)) {
                    OffsetRange astRange = AstUtilities.getNameRange(keyNode);
                    OffsetRange lexRange = LexUtilities.getLexerOffsets(info, astRange);
                    if (lexRange != null) {
                        List<HintFix> fixList = Collections.emptyList();
                        String displayName = NbBundle.getMessage(DuplicateHashKeys.class, "DuplicateHashName", name);
                        Hint desc = new Hint(this, displayName, RubyUtils.getFileObject(info), lexRange, fixList, 1000);
                        result.add(desc);

                    }
                }
            }
        }
    }

    private CharSequence getKeyString(Node keyNode) {
        if (keyNode.nodeId == NodeType.SYMBOLNODE) {
            //return ":"+((INameNode)keyNode).getName(); // NOI18N
            return ((INameNode)keyNode).getName();
        } else if (keyNode.nodeId == NodeType.STRNODE) {
            return ((StrNode)keyNode).getValue();
        }

        return null;
    }

    public String getId() {
        return "DuplicateHashKeys"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(DuplicateHashKeys.class, "DuplicateHashKeys");
    }

    public String getDescription() {
        return NbBundle.getMessage(DuplicateHashKeys.class, "DuplicateHashKeysDesc");
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
}
