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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.hints.spi.RuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.NbBundle;

/**
 * Check constant names to see if they conform to standard Ruby conventions
 * 
 * @author Tor Norbye
 */
public class ConstantNames implements AstRule {
    public ConstantNames() {
    }

    public boolean appliesTo(CompilationInfo info) {
        return true;
    }

    public Set<Integer> getKinds() {
        return Collections.singleton(NodeTypes.CONSTDECLNODE);
    }
    
    public void run(RuleContext context, List<Description> result) {
        Node node = context.node;
        CompilationInfo info = context.compilationInfo;

        assert (node.nodeId == NodeTypes.CONSTDECLNODE);
        String name = ((ConstDeclNode)node).getName();

        for (int i = 0; i < name.length(); i++) {
            if (Character.isLowerCase(name.charAt(i))) {
                String displayName = NbBundle.getMessage(ConstantNames.class, "InvalidConstantName");
                OffsetRange range = AstUtilities.getNameRange(node);
                range = LexUtilities.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    List<Fix> fixList = Collections.emptyList();
                    Description desc = new Description(this, displayName, info.getFileObject(), range, fixList, 1600);
                    result.add(desc);
                }
                return;
            }
        }
    }
    
    public String getId() {
        return "Constant_Names"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ConstantNames.class, "ConstantNameWarning");
    }

    public String getDescription() {
        return NbBundle.getMessage(ConstantNames.class, "ConstantNameWarningDesc");
    }

    public boolean getDefaultEnabled() {
        return false;
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

