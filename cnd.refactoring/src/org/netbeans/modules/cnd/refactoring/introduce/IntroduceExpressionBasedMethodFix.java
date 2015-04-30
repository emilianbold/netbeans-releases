/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.refactoring.introduce;

import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmExpressionStatement;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
final class IntroduceExpressionBasedMethodFix implements Fix {
    private final CsmContext context;
    private final CsmExpressionStatement expression;
    private final List<CsmType> parameterTypes;
    private final List<String> parameterNames;
    //        private Set<CsmType> thrownTypes;

    public IntroduceExpressionBasedMethodFix(CsmContext context, CsmExpressionStatement expression, List<CsmType> parameterTypes, List<String> parameterNames, Set<CsmType> thrownTypes) {
        this.context = context;
        this.expression = expression;
        this.parameterTypes = parameterTypes;
        this.parameterNames = parameterNames;
        //            this.thrownTypes = thrownTypes;
    } //            this.thrownTypes = thrownTypes;

    @Override
    public String getText() {
        return NbBundle.getMessage(IntroduceExpressionBasedMethodFix.class, "FIX_IntroduceMethod");
    }

    @Override
    public String toString() {
        return "[IntroduceExpressionBasedMethodFix]"; // NOI18N
    }

    @Override
    public ChangeInfo implement() throws Exception {
        JButton btnOk = new JButton(NbBundle.getMessage(IntroduceExpressionBasedMethodFix.class, "LBL_Ok"));
        JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceExpressionBasedMethodFix.class, "LBL_Cancel"));
        IntroduceMethodPanel panel = new IntroduceMethodPanel(null, btnOk, null); //NOI18N
        String caption = NbBundle.getMessage(IntroduceExpressionBasedMethodFix.class, "CAP_IntroduceMethod");
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        //final String name = panel.getMethodName();
        //final CsmVisibility access = panel.getAccess();
        return null;
    }

}
