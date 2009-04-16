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
package org.netbeans.modules.cnd.refactoring.introduce;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.refactoring.support.CsmContext;
import org.netbeans.modules.cnd.refactoring.hints.infrastructure.HintAction;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * based on  org.netbeans.modules.java.hints.introduce.IntroduceAction
 * @author Vladimir Voskresensky
 */
public final class IntroduceAction extends HintAction {

    private IntroduceKind type;
    private static String INTRODUCE_CONSTANT = "introduce-constant";//NOI18N
    private static String INTRODUCE_VARIABLE = "introduce-variable";//NOI18N
    private static String INTRODUCE_METHOD = "introduce-method";//NOI18N
    private static String INTRODUCE_FIELD = "introduce-field";//NOI18N

    private IntroduceAction(IntroduceKind type) {
        this.type = type;
        putValue(NAME, getActionName(type));
        String displayText = getMenuItemText(type);
        putValue(SHORT_DESCRIPTION,displayText);
        putValue("PopupMenuText",displayText); // NOI18N
        putValue("menuText",displayText); // NOI18N
    }
//    @Override
//    public Object getValue(String key) {
//        if (Action.ACCELERATOR_KEY.equals(key)) {
//            JTextComponent target = EditorRegistry.focusedComponent();
//            if (target != null) {
//                Keymap km = target.getKeymap();
//                if (km != null) {
//                    KeyStroke[] keys = km.getKeyStrokesForAction(this);
//                    System.err.println("strokes " + keys);
//                }
//            }
//        }
//        System.err.println("getValue " + key + ":" + super.getValue(key));
//        return super.getValue(key);
//    }

    protected void perform(CsmContext context) {
        String error = doPerformAction(context);

        if (error != null) {
            String errorText = NbBundle.getMessage(IntroduceAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);

            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }

    private String doPerformAction(CsmContext context) {
        final Map<IntroduceKind, Fix> fixes = new EnumMap<IntroduceKind, Fix>(IntroduceKind.class);
        final Map<IntroduceKind, String> errorMessages = new EnumMap<IntroduceKind, String>(IntroduceKind.class);

        try {
            IntroduceHint.computeError(context, fixes, errorMessages, new AtomicBoolean());
            Fix fix = fixes.get(type);

            if (fix != null) {
                fix.implement();

                return null;
            }

            String errorMessage = errorMessages.get(type);

            if (errorMessage != null) {
                return errorMessage;
            }

            return "ERR_Invalid_Selection"; //XXX  //NOI18N
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    public static IntroduceAction createVariable() {
        return new IntroduceAction(IntroduceKind.CREATE_VARIABLE);
    }

    public static IntroduceAction createConstant() {
        return new IntroduceAction(IntroduceKind.CREATE_CONSTANT);
    }

    public static IntroduceAction createField() {
        return new IntroduceAction(IntroduceKind.CREATE_FIELD);
    }

    public static IntroduceAction createMethod() {
        return new IntroduceAction(IntroduceKind.CREATE_METHOD);
    }

    private static String getActionName(IntroduceKind type) {
        switch (type) {
            case CREATE_CONSTANT:
                return INTRODUCE_CONSTANT;
            case CREATE_VARIABLE:
                return INTRODUCE_VARIABLE;
            case CREATE_FIELD:
                return INTRODUCE_FIELD;
            case CREATE_METHOD:
                return INTRODUCE_METHOD;
            default:
                return null;
        }
    }
    
    private static String getMenuItemText(IntroduceKind type) {
        switch (type) {
            case CREATE_CONSTANT:
                return NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceConstantAction");
            case CREATE_VARIABLE:
                return NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceVariableAction");
            case CREATE_FIELD:
                return NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceFieldAction");
            case CREATE_METHOD:
                return NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceMethodAction");
            default:
                return null;
        }
    }
}
 
