/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.refactoring.introduce;

import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
final class IntroduceMethodFix implements Fix {
    private final CsmFile csmFile;
    private final CsmScope parentBlock;
    private final List<CsmType> parameterTypes;
    private final List<String> parameterNames;
    private final List<CsmType> additionalLocalTypes;
    private final List<String> additionalLocalNames;
    private final CsmType returnType;
    private final String returnName;
    private final boolean declareVariableForReturnValue;
    private final Set<CsmType> thrownTypes;
    private final List<CsmObject> exists;
    private final boolean exitsFromAllBranches;
    private final int from;
    private final int to;

    public IntroduceMethodFix(CsmFile csmFile, CsmScope parentBlock, List<CsmType> parameterTypes, List<String> parameterNames, List<CsmType> additionalLocalTypes, List<String> additionalLocalNames, CsmType returnType, String returnName, boolean declareVariableForReturnValue, Set<CsmType> thrownTypes, List<CsmObject> exists, boolean exitsFromAllBranches, int from, int to) {
        this.csmFile = csmFile;
        this.parentBlock = parentBlock;
        this.parameterTypes = parameterTypes;
        this.parameterNames = parameterNames;
        this.additionalLocalTypes = additionalLocalTypes;
        this.additionalLocalNames = additionalLocalNames;
        this.returnType = returnType;
        this.returnName = returnName;
        this.declareVariableForReturnValue = declareVariableForReturnValue;
        this.thrownTypes = thrownTypes;
        this.exists = exists;
        this.exitsFromAllBranches = exitsFromAllBranches;
        this.from = from;
        this.to = to;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(IntroduceMethodFix.class, "FIX_IntroduceMethod");
    }

    public String toDebugString() {
        return "[IntroduceMethod:" + from + ":" + to + "]"; // NOI18N
    }

    @Override
    public ChangeInfo implement() throws Exception {
        JButton btnOk = new JButton(NbBundle.getMessage(IntroduceMethodFix.class, "LBL_Ok"));
        JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceMethodFix.class, "LBL_Cancel"));
        IntroduceMethodPanel panel = new IntroduceMethodPanel(""); //NOI18N
        panel.setOkButton(btnOk);
        String caption = NbBundle.getMessage(IntroduceMethodFix.class, "CAP_IntroduceMethod");
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        return null;
    }

}
