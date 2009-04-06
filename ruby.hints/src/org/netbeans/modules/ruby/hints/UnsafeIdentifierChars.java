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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.INameNode;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.NbBundle;

/**
 * Check identifiers to see if they are "safe" (a-z,A-Z,digits,_). Other multibyte values
 * can lead to trouble down the road.
 * 
 * @author Tor Norbye
 */
public class UnsafeIdentifierChars extends RubyAstRule {
    public UnsafeIdentifierChars() {
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public Set<NodeType> getKinds() {
        Set<NodeType> types = new HashSet<NodeType>();
        types.add(NodeType.LOCALASGNNODE);
        types.add(NodeType.DEFNNODE);
        types.add(NodeType.DEFSNODE);
        types.add(NodeType.CONSTDECLNODE);
        return types;
    }
    
    public void run(RubyRuleContext context, List<Hint> result) {
        Node node = context.node;
        ParserResult info = context.parserResult;

        String name = ((INameNode)node).getName();

        if (!RubyUtils.isSafeIdentifierName(name, 0)) {
            String displayName = NbBundle.getMessage(UnsafeIdentifierChars.class, "InvalidMultibyte");
            OffsetRange range = AstUtilities.getNameRange(node);
            List<HintFix> fixList = Collections.emptyList();
            range = LexUtilities.getLexerOffsets(info, range);
            if (range != OffsetRange.NONE) {
                Hint desc = new Hint(this, displayName, RubyUtils.getFileObject(info), range, fixList, 600);
                result.add(desc);
            }
        }
    }
    
    public String getId() {
        return "Unsafe_Identifier_Chars"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UnsafeIdentifierChars.class, "UnsafeIdentifierChars");
    }

    public String getDescription() {
        return NbBundle.getMessage(UnsafeIdentifierChars.class, "UnsafeIdentifierCharsDesc");
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
