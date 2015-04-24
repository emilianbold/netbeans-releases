/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.refactoring.introduce;

import javax.swing.JButton;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.refactoring.hints.IntroduceVariableFix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

final class ExtendedIntroduceVariableFix extends IntroduceVariableFix {
    private int numDuplicates;
    private final IntroduceKind kind;
    private boolean declareConst = false;

    public ExtendedIntroduceVariableFix(CsmStatement st, CsmOffsetable expression, Document doc, JTextComponent comp, FileObject fo) {
        super(st, expression, doc, comp, fo);
        kind = IntroduceKind.CREATE_VARIABLE;
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
    protected boolean declareConst() {
        return declareConst;
    }

    @Override
    protected String suggestName() {
        String guessedName = super.suggestName();
        JButton btnOk = new JButton(NbBundle.getMessage(ExtendedIntroduceVariableFix.class, "LBL_Ok"));
        JButton btnCancel = new JButton(NbBundle.getMessage(ExtendedIntroduceVariableFix.class, "LBL_Cancel"));
        IntroduceVariablePanel panel = new IntroduceVariablePanel(numDuplicates, guessedName, kind == IntroduceKind.CREATE_CONSTANT, btnOk);
        String caption = NbBundle.getMessage(ExtendedIntroduceVariableFix.class, "CAP_" + getKeyExt()); //NOI18N
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        guessedName = panel.getVariableName();
        declareConst = panel.isDeclareFinal();
        return guessedName;
    }
}

