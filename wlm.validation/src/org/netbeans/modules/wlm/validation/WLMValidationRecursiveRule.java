/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.validation;

import java.util.List;
import org.netbeans.modules.wlm.model.api.EmailAddress;
import org.netbeans.modules.wlm.model.api.Group;
import org.netbeans.modules.wlm.model.api.Keyword;
import org.netbeans.modules.wlm.model.api.MessageBody;
import org.netbeans.modules.wlm.model.api.MessageSubject;
import org.netbeans.modules.wlm.model.api.TAction;
import org.netbeans.modules.wlm.model.api.TAssignment;
import org.netbeans.modules.wlm.model.api.TChangeVariables;
import org.netbeans.modules.wlm.model.api.TCopy;
import org.netbeans.modules.wlm.model.api.TDeadlineExpr;
import org.netbeans.modules.wlm.model.api.TDurationExpr;
import org.netbeans.modules.wlm.model.api.TEmail;
import org.netbeans.modules.wlm.model.api.TEscalation;
import org.netbeans.modules.wlm.model.api.TExcluded;
import org.netbeans.modules.wlm.model.api.TFrom;
import org.netbeans.modules.wlm.model.api.TImport;
import org.netbeans.modules.wlm.model.api.TInit;
import org.netbeans.modules.wlm.model.api.TKeywords;
import org.netbeans.modules.wlm.model.api.TLocalNotification;
import org.netbeans.modules.wlm.model.api.TMessage;
import org.netbeans.modules.wlm.model.api.TNotification;
import org.netbeans.modules.wlm.model.api.TPriority;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.TTimeout;
import org.netbeans.modules.wlm.model.api.TTitle;
import org.netbeans.modules.wlm.model.api.TTo;
import org.netbeans.modules.wlm.model.api.User;
import org.netbeans.modules.wlm.model.api.VariableInit;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMVisitor;

/**
 *
 * @author anjeleevich
 */
public class WLMValidationRecursiveRule
        extends WLMValidationRule
        implements WLMVisitor
{
    public WLMValidationRecursiveRule(WLMValidationResultBuilder builder) {
        super(builder);
    }

    @Override
    public final void checkRule() {
        WLMModel model = getModel();
        TTask task = model.getTask();
        checkRecursively(task);
    }

    private void checkRecursively(WLMComponent component) {
        component.accept(this);

        List<WLMComponent> children = component.getChildren();

        if (children != null) {
            for (WLMComponent child : children) {
                checkRecursively(child);
            }
        }
    }

    public void visitTask(TTask task) {
    }

    public void visitImport(TImport importEl) {
    }

    public void visitAssignment(TAssignment assignment) {
    }

    public void visitTimeout(TTimeout timeout) {
    }

    public void visitEscalation(TEscalation escalation) {
    }

    public void visitNotification(TNotification notification) {
    }

    public void visitMessage(TMessage message) {
    }

    public void visitMessageBody(MessageBody messageBody) {
    }

    public void visitMessageSubject(MessageSubject messageSubject) {
    }

    public void visitEmail(TEmail email) {
    }

    public void visitEmailAddress(EmailAddress emailAddress) {
    }

    public void visitLocalNotification(TLocalNotification localNotification) {
    }

    public void visitUser(User user) {
    }

    public void visitGroup(Group group) {
    }

    public void visitAction(TAction action) {
    }

    public void visitDeadLine(TDeadlineExpr deadline) {
    }

    public void visitDuration(TDurationExpr duration) {
    }

    public void visitInit(TInit init) {
    }

    public void visitPriority(TPriority priority) {
    }

    public void visitTitle(TTitle title) {
    }

    public void visitVariableInit(VariableInit variableInit) {
    }

    public void visitChangeVariables(TChangeVariables changeVariables) {
    }

    public void visitCopy(TCopy copy) {
    }

    public void visitFrom(TFrom from) {
    }

    public void visitTo(TTo to) {
    }

    public void visitExcluded(TExcluded excluded) {
    }

    public void visitKeyword(Keyword keyword) {
    }

    public void visitKeywords(TKeywords keywords) {
    }
}
