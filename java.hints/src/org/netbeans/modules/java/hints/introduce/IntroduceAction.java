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
package org.netbeans.modules.java.hints.introduce;

import java.beans.PropertyChangeEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.java.hints.infrastructure.HintAction;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class IntroduceAction extends HintAction {
    
    private IntroduceKind type;
    private static String INTRODUCE_CONSTANT = "introduce-constant";//NOI18N
    private static String INTRODUCE_VARIABLE = "introduce-variable";//NOI18N
    private static String INTRODUCE_METHOD = "introduce-method";//NOI18N
    private static String INTRODUCE_FIELD = "introduce-field";//NOI18N

    /** Property identifier for menu text, neccessary for display in menu */
    private static final String MENU_TEXT = "menuText"; //NOI18N
    /** Property identifier for popup textm, neccessary for display popup */
    private static final String POPUP_TEXT = "popupText"; //NOI18N
    

    private IntroduceAction(IntroduceKind type) {
        this.type = type;
        switch (type) {
            case CREATE_CONSTANT:
                putValue(NAME, INTRODUCE_CONSTANT);
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceConstantAction"));
                putValue(MENU_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceConstantAction"));
                putValue(POPUP_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceConstantAction"));
                break;
            case CREATE_VARIABLE:
                putValue(NAME, INTRODUCE_VARIABLE);
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceVariableAction"));
                putValue(MENU_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceVariableAction"));
                putValue(POPUP_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceVariableAction"));
                break;
            case CREATE_FIELD:
                putValue(NAME, INTRODUCE_FIELD);
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceFieldAction"));
                putValue(MENU_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceFieldAction"));
                putValue(POPUP_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceFieldAction"));
                break;
            case CREATE_METHOD:
                putValue(NAME, INTRODUCE_METHOD);
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceMethodAction"));
                putValue(MENU_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceMethodAction"));
                putValue(POPUP_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceMethodAction"));
                break;
        }

        setEnabled(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }


    
    protected void perform(JavaSource js, int[] selection) {
        String error = doPerformAction(js, selection);
        
        if (error != null) {
            String errorText = NbBundle.getMessage(IntroduceAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }
    
    private String doPerformAction(JavaSource js,final int[] span) {
        final Map<IntroduceKind, Fix> fixes = new EnumMap<IntroduceKind, Fix>(IntroduceKind.class);
        final Map<IntroduceKind, String> errorMessages = new EnumMap<IntroduceKind, String>(IntroduceKind.class);
        
        try {
            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    IntroduceHint.computeError(parameter, span[0], span[1], fixes, errorMessages, new AtomicBoolean());
                }
            }, true);
            
            Fix fix = fixes.get(type);
            
            if (fix != null) {
                fix.implement();
                
                return null;
            }
            
            String errorMessage = errorMessages.get(type);
            
            if (errorMessage != null)
                return errorMessage;
            
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
    
}
