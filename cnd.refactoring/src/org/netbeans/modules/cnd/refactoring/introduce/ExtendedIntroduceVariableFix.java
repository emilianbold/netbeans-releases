/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.refactoring.introduce;

import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.refactoring.actions.RefactoringKind;
import org.netbeans.modules.cnd.refactoring.hints.IntroduceVariableFix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

final class ExtendedIntroduceVariableFix extends IntroduceVariableFix {
    private final int numDuplicates;
    private final RefactoringKind kind;
    private boolean replaceOccurrences = false;
    private final List<Pair<Integer, Integer>> occurrences;
    private String type;

    public ExtendedIntroduceVariableFix(CsmStatement st, CsmOffsetable expression, List<Pair<Integer, Integer>> occurrences, Document doc, JTextComponent comp, FileObject fo) {
        super(st, expression, doc, comp, fo);
        kind = RefactoringKind.CREATE_VARIABLE;
        this.occurrences = occurrences;
        numDuplicates = occurrences.size();
    }

    public String getKeyExt() {
        switch (kind) {
            case CREATE_CONSTANT:
                return "IntroduceConstant"; //NOI18N
            case CREATE_VARIABLE:
                return "IntroduceVariable"; //NOI18N
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(ExtendedIntroduceVariableFix.class, "FIX_" + getKeyExt()); //NOI18N
    }

    @Override
    protected boolean isInstanceRename() {
        return false;
    }

    @Override
    protected List<Pair<Integer, Integer>> replaceOccurrences() {
        if (!replaceOccurrences) {
            return Collections.emptyList();
        } else {
            return occurrences;
        }
    }

    @Override
    protected String getType() {
        return type;
    }

    @Override
    protected String suggestName() {
        String guessedName = super.suggestName();
        type = super.getType();
        JButton btnOk = new JButton(NbBundle.getMessage(ExtendedIntroduceVariableFix.class, "LBL_Ok"));
        JButton btnCancel = new JButton(NbBundle.getMessage(ExtendedIntroduceVariableFix.class, "LBL_Cancel"));
        IntroduceVariablePanel panel = new IntroduceVariablePanel(numDuplicates, type, guessedName, kind == RefactoringKind.CREATE_CONSTANT, btnOk);
        String caption = NbBundle.getMessage(ExtendedIntroduceVariableFix.class, "CAP_" + getKeyExt()); //NOI18N
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        guessedName = panel.getVariableName();
        replaceOccurrences = panel.isReplaceAll();
        type = panel.getType();
        return guessedName;
    }
}

