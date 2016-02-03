/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.mixeddev.ui;

import javax.swing.Action;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.netbeans.modules.cnd.mixeddev.java.jni.actions.CopyJNICallMethodCodeAction;
import org.netbeans.modules.cnd.mixeddev.java.jni.actions.CopyJNIGetFieldCodeAction;
import org.netbeans.modules.cnd.mixeddev.java.jni.actions.CopyJNISetFieldCodeAction;
import org.netbeans.modules.cnd.mixeddev.java.jni.actions.CopyJNISignatureAction;
import org.netbeans.modules.cnd.mixeddev.java.jni.actions.GenerateHeaderForJNIClassAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
@ActionID(id = "org.netbeans.modules.cnd.mixeddev.ui.MixedDevContextMetaAction", category = "MixedDevelopment")
@ActionRegistration(displayName = "unused-name", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Editors/text/x-java/Popup", position = 1950)
})
public class MixedDevContextMetaAction extends AbstactDynamicMenuAction implements ContextAwareAction {
    
    private static final RequestProcessor RP = new RequestProcessor(MixedDevContextMetaAction.class.getName(), 1);

    public MixedDevContextMetaAction() {
        super(RP, NbBundle.getMessage(MixedDevUtils.class, "Editors/text/x-java/Popup/MixedDevelopment")); // NOI18N
    }

    @Override
    protected Action[] createActions(Lookup actionContext) {
        return new Action[] {
            new GenerateHeaderForJNIClassAction(actionContext),
            new CopyJNISignatureAction(actionContext),
            new CopyJNICallMethodCodeAction(actionContext),
            new CopyJNIGetFieldCodeAction(actionContext),
            new CopyJNISetFieldCodeAction(actionContext)
//            GenerateHeaderForJNIClassAction.INSTANCE,
//            CopyJNISignatureAction.INSTANCE,
//            CopyJNICallMethodCodeAction.INSTANCE,
//            CopyJNIGetFieldCodeAction.INSTANCE,
//            CopyJNISetFieldCodeAction.INSTANCE
        };
    }
}
