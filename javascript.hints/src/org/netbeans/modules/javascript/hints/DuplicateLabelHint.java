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
package org.netbeans.modules.javascript.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.modules.csl.api.CompilationInfo;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.hints.infrastructure.JsAstRule;
import org.netbeans.modules.javascript.hints.infrastructure.JsRuleContext;
import org.openide.util.NbBundle;

/**
 * Check for duplicate labels in object literals.
 *
 * @author Tor Norbye
 */
public class DuplicateLabelHint extends JsAstRule {

    public DuplicateLabelHint() {
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public Set<Integer> getKinds() {
        return Collections.singleton(Token.OBJECTLIT);
    }

    public void run(JsRuleContext context, List<Hint> result) {
        CompilationInfo info = context.compilationInfo;
        Node node = context.node;

        Object[] properties = (Object[]) node.getProp(Node.OBJECT_IDS_PROP);
        int count = properties.length;
        if (count <= 1) {
            return;
        }

        List<String> ids = new ArrayList<String>(count);
        // load array with property ids
        for (int i = 0; i != count; ++i) {
            Object id = properties[i];
            if (id instanceof String) {
                ids.add((String) id);
            } else {
                ids.add(Integer.toString(((Integer) id).intValue()));
            }
        }
        Collections.sort(ids);

        String prev = ids.get(0);
        for (int i = 1, n = ids.size(); i < n; i++) {
            String curr = ids.get(i);
            if (curr.equals(prev)) {
                // Found duplicate
                // Don't look for additional duplicates, only warn about the first duplicate

                boolean isFirst = true;
                List<Node> nodes = new ArrayList<Node>();
                for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                    if (child.getType() == Token.OBJLITNAME && child instanceof Node.StringNode) {
                        String s = child.getString();
                        if (s.equals(curr)) {
                            if (isFirst) {
                                isFirst = false;
                            } else {
                                nodes.add(child);
                            }
                        }
                    }
                }
                
                if (nodes.size() > 0) {
                    for (Node duplicate : nodes) {
                        OffsetRange range = AstUtilities.getNameRange(duplicate);
                        List<HintFix> fixList = Collections.emptyList();
                        String displayName = getDisplayName();
                        Hint desc = new Hint(this, displayName, info.getFileObject(), range, fixList, 1500);
                        result.add(desc);
                    }
                }

                return;
            }
            prev = curr;
        }
    }

    public String getId() {
        return "DuplicateLabel"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(DuplicateLabelHint.class, "DuplicateLabel");
    }

    public String getDescription() {
        return NbBundle.getMessage(DuplicateLabelHint.class, "DuplicateLabelDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }
}
