/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.worklist.editor.navigator;

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
import org.netbeans.modules.wlm.model.api.WLMVisitor;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;

/**
 *
 * @author anjeleevich
 */
public class WLMNodeTypeResolver implements WLMVisitor {

    private WLMNodeType result;

    public WLMNodeType getResult() {
        return result;
    }

    public void reset() {
        result = null;
    }

    public void visitExcluded(TExcluded excluded) {
        result = null;
    }

    public void visitTask(TTask task) {
        result = WLMNodeType.TASK;
    }

    public void visitImport(TImport importElement) {
        result = WLMNodeType.IMPORT;
    }

    public void visitAssignment(TAssignment assignment) {
        result = WLMNodeType.ASSIGNMENT;
    }

    public void visitTimeout(TTimeout timeout) {
        result = WLMNodeType.TIMEOUT;
    }

    public void visitEscalation(TEscalation escalation) {
        result = WLMNodeType.ESCALATION;
    }

    public void visitNotification(TNotification notification) {
        result = WLMNodeType.NOTIFICATION;
    }

    public void visitMessage(TMessage message) {
        result = null;
    }

    public void visitMessageBody(MessageBody messageBody) {
        result = WLMNodeType.NOTIFICATUIN_BODY;
    }

    public void visitMessageSubject(MessageSubject messageSubject) {
        result = WLMNodeType.NOTIFICATION_SUBJECT;
    }

    public void visitEmail(TEmail email) {
        result = WLMNodeType.NOTIFICATION_EMAILS;
    }

    public void visitEmailAddress(EmailAddress emailAddress) {
        result = WLMNodeType.EMAIL_ADDRESS;
    }

    public void visitLocalNotification(TLocalNotification localNotification) {
        result = WLMNodeType.LOCAL_NOTIFICATION;
    }

    public void visitUser(User user) {
        result = WLMNodeType.USER;
    }

    public void visitGroup(Group group) {
        result = WLMNodeType.GROUP;
    }

    public void visitAction(TAction action) {
        result = WLMNodeType.ACTION;
    }

    public void visitDeadLine(TDeadlineExpr deadline) {
        result = null;
    }

    public void visitDuration(TDurationExpr duration) {
        result = null;
    }

    public void visitInit(TInit init) {
        result = null;
    }

    public void visitPriority(TPriority priority) {
        result = WLMNodeType.PRIORITY;
    }

    public void visitTitle(TTitle title) {
        result = WLMNodeType.TITLE;
    }

    public void visitVariableInit(VariableInit variableInit) {
        result = WLMNodeType.INIT_VARIABLES;
    }

    public void visitChangeVariables(TChangeVariables changeVariables) {
        result = WLMNodeType.CHANGE_VARIABLES;
    }

    public void visitCopy(TCopy copy) {
        result = WLMNodeType.COPY;
    }

    public void visitKeyword(Keyword keyword) {
        result = WLMNodeType.KEYWORD;
    }

    public void visitKeywords(TKeywords keywords) {
        result = WLMNodeType.KEYWORDS;
    }

    public void visitFrom(TFrom from) {
        result = null;
    }

    public void visitTo(TTo to) {
        result = null;
    }

    public static WLMNodeType getNodeType(WLMComponent component) {
        if (component == null) {
            return null;
        }

        synchronized (INSTANCE) {
            component.accept(INSTANCE);
            WLMNodeType type = INSTANCE.getResult();
            INSTANCE.reset();
            return type;
        }
    }

    private static final WLMNodeTypeResolver INSTANCE
            = new WLMNodeTypeResolver();
}
