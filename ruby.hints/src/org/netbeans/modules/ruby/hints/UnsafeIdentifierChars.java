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
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.types.INameNode;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.hints.spi.RuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.NbBundle;

/**
 * Check identifiers to see if they are "safe" (a-z,A-Z,digits,_). Other multibyte values
 * can lead to trouble down the road.
 * 
 * @author Tor Norbye
 */
public class UnsafeIdentifierChars implements AstRule {
    public UnsafeIdentifierChars() {
    }

    public boolean appliesTo(CompilationInfo info) {
        return true;
    }

    public Set<Integer> getKinds() {
        Set<Integer> integers = new HashSet<Integer>();
        integers.add(NodeTypes.LOCALASGNNODE);
        integers.add(NodeTypes.DEFNNODE);
        integers.add(NodeTypes.DEFSNODE);
        integers.add(NodeTypes.CONSTDECLNODE);
        return integers;
    }
    
    public void run(RuleContext context, List<Description> result) {
        Node node = context.node;
        CompilationInfo info = context.compilationInfo;

        String name = ((INameNode)node).getName();

        if (!RubyUtils.isSafeIdentifierName(name, 0)) {
            String displayName = NbBundle.getMessage(UnsafeIdentifierChars.class, "InvalidMultibyte");
            OffsetRange range = AstUtilities.getNameRange(node);
            List<Fix> fixList = Collections.emptyList();
            range = LexUtilities.getLexerOffsets(info, range);
            if (range != OffsetRange.NONE) {
                Description desc = new Description(this, displayName, info.getFileObject(), range, fixList, 600);
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
