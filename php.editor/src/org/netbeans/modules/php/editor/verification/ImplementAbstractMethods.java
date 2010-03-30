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
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public class ImplementAbstractMethods extends AbstractRule {
    @Override
    public String getId() {
        return "Implement.Abstract.Methods";//NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ImplementAbstractMethods.class, "ImplementAbstractMethodsDesc");//NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ImplementAbstractMethods.class, "ImplementAbstractMethodsDispName");//NOI18N
    }

    @Override
    void computeHintsImpl(PHPRuleContext context, List<Hint> hints, PHPHintsProvider.Kind kind) throws BadLocationException {
        final BaseDocument doc = context.doc;
        final int caretOffset = context.caretOffset;
        int lineBegin = -1;
        int lineEnd = -1;
        lineBegin = caretOffset > 0 ? Utilities.getRowStart(doc, caretOffset) : -1;
        lineEnd = (lineBegin != -1) ? Utilities.getRowEnd(doc, caretOffset) : -1;
        if (lineBegin != -1 && lineEnd != -1 && caretOffset > lineBegin) {
            Collection<? extends TypeScope> allTypes = ModelUtils.getDeclaredTypes(context.fileScope);
            for (FixInfo fixInfo : checkHints(allTypes, lineBegin, lineEnd, context)) {
                hints.add(new Hint(ImplementAbstractMethods.this, getDisplayName(),
                        context.parserResult.getSnapshot().getSource().getFileObject(), fixInfo.classNameRange,
                        Collections.<HintFix>singletonList(new Fix(context,
                        fixInfo)), 500));
            }
        }
    }


    private static boolean isInside(int carret, int left, int right) {
        return carret >= left && carret <= right;
    }

    private Collection<FixInfo> checkHints(Collection<? extends TypeScope> allTypes, int lineBegin, int lineEnd, PHPRuleContext context) throws BadLocationException{
        List<FixInfo> retval = new ArrayList<FixInfo>();
        for (TypeScope typeScope : allTypes) {
            if (!isInside(typeScope.getOffset(), lineBegin, lineEnd)) continue;
            Index index = context.getIndex();
            ElementFilter declaredMethods = ElementFilter.forExcludedNames(toNames(index.getDeclaredMethods(typeScope)), PhpElementKind.METHOD);
            Set<MethodElement> accessibleMethods = declaredMethods.filter(index.getAccessibleMethods(typeScope, typeScope));
            LinkedHashSet<String> methodSkeletons = new LinkedHashSet<String>();

            for (MethodElement methodElement : accessibleMethods) {
                final TypeElement type = methodElement.getType();
                if ((type.isInterface() || methodElement.isAbstract()) && !methodElement.isFinal()) {
                    String skeleton = methodElement.asString(PrintAs.DeclarationWithEmptyBody);
                    skeleton = skeleton.replace("abstract ", ""); //NOI18N
                    methodSkeletons.add(skeleton);
                }
            }
            if (!methodSkeletons.isEmpty()) {
                int offset = getOffset(typeScope, context);
                if (offset != -1) {
                    retval.add(new FixInfo(typeScope, methodSkeletons, offset));
                }
            }
        }
        return retval;
    }

    private static Set<String> toNames(Set<? extends PhpElement> elements) {
        Set<String> names = new HashSet<String>();
        for (PhpElement elem : elements) {
            names.add(elem.getName());
        }
        return names;
    }

    private static int getOffset(TypeScope typeScope, PHPRuleContext context) throws BadLocationException {
        int offset = -1;
        Collection<? extends MethodScope> declaredMethods = typeScope.getDeclaredMethods();
        for (MethodScope methodScope : declaredMethods) {
            OffsetRange blockRange = methodScope.getBlockRange();
            if (blockRange != null && blockRange.getEnd() > offset) {
                offset = blockRange.getEnd();
            }
        }
        if (offset == -1 && typeScope.getBlockRange() != null) {
            offset = Utilities.getRowStart(context.doc, typeScope.getBlockRange().getEnd()) - 1;
        }
        if (offset != -1) {
            offset = Utilities.getRowEnd(context.doc, offset);
        }
        return offset;
    }

    private class Fix implements HintFix {

        private RuleContext context;
        private final FixInfo fixInfo;

        Fix(RuleContext context, FixInfo fixInfo) {
            this.context = context;
            this.fixInfo = fixInfo;
        }

        @Override
        public String getDescription() {
            return ImplementAbstractMethods.this.getDescription();
        }

        @Override
        public void implement() throws Exception {
            getEditList().apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);
            for (String methodScope : fixInfo.methodSkeletons) {
                edits.replace(fixInfo.offset, 0, "\n"+methodScope, true, 0);
            }
            return edits;
        }
    }

    private static class FixInfo {
        private List<String> methodSkeletons;
        private int offset;
        private OffsetRange classNameRange;

        FixInfo(TypeScope typeScope, LinkedHashSet<String> methodSkeletons, int offset) {
            this.methodSkeletons = new ArrayList<String>(methodSkeletons);
            Collections.sort(this.methodSkeletons);
            this.classNameRange = typeScope.getNameRange();
            this.offset = offset;
        }
    }
}
