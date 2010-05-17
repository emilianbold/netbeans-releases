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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintFix;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.python.editor.PythonAstUtils;
import org.netbeans.modules.python.editor.PythonParserResult;
import org.netbeans.modules.python.editor.scopes.SymbolTable;
import org.openide.util.NbBundle;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Module;

/**
 * check for redundancy cycling in parent child
 * @author Jean-Yves Mengant
 */
public class ClassCircularRedundancy extends PythonAstRule {
    private final static String CLASS_CIRCULAR_REDUNDANCY = "ClassCircularRedundancy";
    private final static String CLASS_CIRCULAR_REDUNDANCY_VAR = "ClassCircularRedundancyVariable";
    private final static String CLASS_CIRCULAR_REDUNDANCY_DESC = "ClassCircularRedundancyDesc";

    @Override
    public Set<Class> getKinds() {
        return Collections.<Class>singleton(Module.class);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        CompilationInfo info = context.compilationInfo;
        PythonParserResult pr = PythonAstUtils.getParseResult(info);
        SymbolTable symbolTable = pr.getSymbolTable();


        HashMap<ClassDef, String> cyclingRedundancies = symbolTable.getClassesCyclingRedundancies(info);
        if (cyclingRedundancies.size() > 0) {
            Set<Entry<ClassDef, String>> wk = cyclingRedundancies.entrySet();
            for (Entry<ClassDef, String> cur : wk) {
                ClassDef curClass = cur.getKey();
                String curCyclingMsg = curClass.getInternalName() + "/" + cur.getValue(); // NOI18N
                OffsetRange range = PythonAstUtils.getNameRange(info, curClass);
                // range = PythonLexerUtils.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    List<HintFix> fixList = Collections.emptyList();
                    String message = NbBundle.getMessage(NameRule.class, CLASS_CIRCULAR_REDUNDANCY_VAR, curCyclingMsg);
                    Hint desc = new Hint(this, message, info.getFileObject(), range, fixList, 2305);
                    result.add(desc);
                }
            }
        }
    }

    public String getId() {
        return CLASS_CIRCULAR_REDUNDANCY;
    }

    public String getDescription() {
        return NbBundle.getMessage(RelativeImports.class, CLASS_CIRCULAR_REDUNDANCY_DESC);
    }

    public boolean getDefaultEnabled() {
        return false;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AccessToProtected.class, CLASS_CIRCULAR_REDUNDANCY);
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }
}
