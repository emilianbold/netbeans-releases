/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.refactoring.introduce;

import java.io.IOException;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
final class IntroduceFieldFix implements Fix {
    private final String guessedName;
    private final CsmObject handle;
    private final CsmFile csmFile;
    private final int numDuplicates;
    private final int[] initilizeIn;
    private final boolean statik;
    private final boolean allowFinalInCurrentMethod;

    public IntroduceFieldFix(CsmObject handle, CsmFile csmFile, String guessedName, int numDuplicates, int[] initilizeIn, boolean statik, boolean allowFinalInCurrentMethod) {
        this.handle = handle;
        this.csmFile = csmFile;
        this.guessedName = guessedName;
        this.numDuplicates = numDuplicates;
        this.initilizeIn = initilizeIn;
        this.statik = statik;
        this.allowFinalInCurrentMethod = allowFinalInCurrentMethod;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(IntroduceFieldFix.class, "FIX_IntroduceField");
    }

    @Override
    public String toString() {
        return "[IntroduceField:" + guessedName + ":" + numDuplicates + ":" + statik + ":" + allowFinalInCurrentMethod + ":" + Arrays.toString(initilizeIn) + "]"; // NOI18N
    }

    @Override
    public ChangeInfo implement() throws IOException, BadLocationException {
        JButton btnOk = new JButton(NbBundle.getMessage(IntroduceFieldFix.class, "LBL_Ok"));
        btnOk.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IntroduceFieldFix.class, "AD_IntrHint_OK"));
        JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceFieldFix.class, "LBL_Cancel"));
        btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IntroduceFieldFix.class, "AD_IntrHint_Cancel"));
        IntroduceFieldPanel panel = new IntroduceFieldPanel(guessedName, initilizeIn, numDuplicates, allowFinalInCurrentMethod, btnOk);
        String caption = NbBundle.getMessage(IntroduceFieldFix.class, "CAP_IntroduceField");
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        return null;
    }

}
