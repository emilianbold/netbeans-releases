/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.python.editor.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.python.editor.PythonAstUtils;
import org.netbeans.modules.python.editor.lexer.PythonLexerUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.EditList;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintFix;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.PreviewableFix;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.netbeans.modules.python.editor.options.CodeStyle;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.alias;

/**
 * Import statements should be one per line. This quickfix
 * offers to make it so.
 *
 * @author Tor Norbye
 */
public class SplitImports extends PythonAstRule {
    @Override
    public Set<Class> getKinds() {
        return Collections.singleton((Class)Import.class);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        Import imp = (Import)context.node;
        List<alias> names = imp.getInternalNames();
        if (names != null && names.size() > 1) {
            PythonTree node = context.node;
            CompilationInfo info = context.compilationInfo;
            OffsetRange astOffsets = PythonAstUtils.getNameRange(info, node);
            OffsetRange lexOffsets = PythonLexerUtils.getLexerOffsets(info, astOffsets);
            BaseDocument doc = context.doc;
            try {
                if (lexOffsets != OffsetRange.NONE && lexOffsets.getStart() < doc.getLength() &&
                        (context.caretOffset == -1 ||
                        Utilities.getRowStart(doc, context.caretOffset) == Utilities.getRowStart(doc, lexOffsets.getStart()))) {
                    List<HintFix> fixList = new ArrayList<HintFix>();
                    fixList.add(new SplitImportsFix(context, imp));
                    String displayName = getDisplayName();
                    Hint desc = new Hint(this, displayName, info.getFileObject(), lexOffsets, fixList, 1500);
                    result.add(desc);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public String getId() {
        return "SplitImports"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SplitImports.class, "SplitImports");
    }

    public String getDescription() {
        return NbBundle.getMessage(SplitImports.class, "SplitImportsDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    public boolean appliesTo(RuleContext context) {
        CodeStyle codeStyle = CodeStyle.getDefault(context.doc);
        return codeStyle == null || codeStyle.oneImportPerLine();
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    private static class SplitImportsFix implements PreviewableFix {
        private final PythonRuleContext context;
        private final Import imp;

        private SplitImportsFix(PythonRuleContext context, Import imp) {
            this.context = context;
            this.imp = imp;
        }

        public String getDescription() {
            return NbBundle.getMessage(SplitImports.class, "SplitImportsFix");
        }

        public boolean canPreview() {
            return true;
        }

        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);

            OffsetRange astRange = PythonAstUtils.getRange(imp);
            if (astRange != OffsetRange.NONE) {
                OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(context.compilationInfo, astRange);
                if (lexRange != OffsetRange.NONE) {
                    int indent = GsfUtilities.getLineIndent(doc, lexRange.getStart());
                    StringBuilder sb = new StringBuilder();
                    List<alias> names = imp.getInternalNames();
                    if (names != null) {
                        for (alias at : names) {
                            if (indent > 0 && sb.length() > 0) {
                                sb.append(IndentUtils.createIndentString(doc, indent));
                            }
                            sb.append("import "); // NOI18N
                            sb.append(at.getInternalName());
                            if (at.getInternalAsname() != null && at.getInternalAsname().length() > 0) {
                                sb.append(" as "); // NOI18N
                                sb.append(at.getInternalAsname());
                            }
                            sb.append("\n");
                        }
                    }
                    // Remove the final newline since Import doesn't include it
                    sb.setLength(sb.length() - 1);

                    edits.replace(lexRange.getStart(), lexRange.getLength(), sb.toString(), false, 0);
                }
            }

            return edits;
        }

        public void implement() throws Exception {
            EditList edits = getEditList();
            edits.apply();
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }
    }
}
