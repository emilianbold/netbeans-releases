/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.introduce;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.deep.CsmExpressionStatement;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.hints.ExpressionFinder;
import org.netbeans.modules.cnd.refactoring.hints.infrastructure.HintAction;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * based on  org.netbeans.modules.java.hints.introduce.IntroduceAction
 * @author Vladimir Voskresensky
 */
public final class IntroduceAction extends HintAction {

    private final IntroduceKind type;
    private static final String INTRODUCE_CONSTANT = "introduce-constant";//NOI18N
    private static final String INTRODUCE_VARIABLE = "introduce-variable";//NOI18N
    private static final String INTRODUCE_METHOD = "introduce-method";//NOI18N
    private static final String INTRODUCE_FIELD = "introduce-field";//NOI18N

    private IntroduceAction(IntroduceKind type) {
        this.type = type;
        putValue(NAME, getActionName(type));
        String displayText = getMenuItemText(type);
        putValue(SHORT_DESCRIPTION,displayText);
        putValue("PopupMenuText",displayText); // NOI18N
        putValue("menuText",displayText); // NOI18N
    }

    @Override
    protected void perform(CsmContext context) {
        String error = doPerformAction(context);

        if (error != null) {
            String errorText = NbBundle.getMessage(IntroduceAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);

            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }

    private String doPerformAction(CsmContext context) {
        final Map<IntroduceKind, Fix> fixes = new EnumMap<>(IntroduceKind.class);
        final Map<IntroduceKind, String> errorMessages = new EnumMap<>(IntroduceKind.class);

        try {
            computeError(context, fixes, errorMessages, new AtomicBoolean());
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

    private List<ErrorDescription> computeError(CsmContext info, Map<IntroduceKind, Fix> fixesMap, Map<IntroduceKind, String> errorMessage, AtomicBoolean cancel) {
        List<ErrorDescription> hints = new LinkedList<>();
        detectIntroduceVariable(fixesMap, info.getFile(), info.getCaretOffset(), info.getStartOffset(), info.getEndOffset(), info.getDocument(), cancel, info.getFileObject(), info.getComponent());
        return hints;
    }

    private void detectIntroduceVariable(Map<IntroduceKind, Fix> fixesMap, CsmFile file, int caretOffset, int selectionStart, int selectionEnd, final Document doc, final AtomicBoolean canceled, final FileObject fileObject, JTextComponent comp) {
        ExpressionFinder expressionFinder = new ExpressionFinder(doc, file, caretOffset, selectionStart, selectionEnd, canceled);
        ExpressionFinder.StatementResult res = expressionFinder.findExpressionStatement();
        if (res == null) {
            return;
        }
        if (canceled.get()) {
            return;
        }
        CsmExpressionStatement expression = res.getExpression();
        if (expression != null) {
            fixesMap.put(IntroduceKind.CREATE_VARIABLE, new ExtendedAssignmentVariableFix(expression.getExpression(), doc, fileObject));
        }
        if (res.getContainer() != null && res.getStatementInBody() != null && comp != null && selectionStart < selectionEnd) {
            if (CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, selectionStart)[0] ==
                    CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, selectionEnd)[0] &&
                    expressionFinder.isExpressionSelection()) {
                if (!(res.getContainer().getStartOffset() == selectionStart &&
                        res.getContainer().getEndOffset() == selectionEnd)) {
                    CsmOffsetable applicableTextExpression = expressionFinder.applicableTextExpression();
                    if (applicableTextExpression != null) {
                        List<Pair<Integer, Integer>> occurrences = res.getOccurrences(applicableTextExpression);
                        fixesMap.put(IntroduceKind.CREATE_VARIABLE, new ExtendedIntroduceVariableFix(res.getStatementInBody(), applicableTextExpression, occurrences, doc, comp, fileObject));
                    }
                }
            }
        }
    }
}
 
