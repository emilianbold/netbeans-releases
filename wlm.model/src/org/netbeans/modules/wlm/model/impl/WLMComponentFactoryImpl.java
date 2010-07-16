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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.impl;

import javax.xml.namespace.QName;

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
import org.netbeans.modules.wlm.model.api.TTimeout;
import org.netbeans.modules.wlm.model.api.TTitle;
import org.netbeans.modules.wlm.model.api.TTo;
import org.netbeans.modules.wlm.model.api.VariableInit;
import org.netbeans.modules.wlm.model.api.User;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMComponentFactory;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.w3c.dom.Element;

/**
 * 
 * @author rico
 */
public class WLMComponentFactoryImpl implements WLMComponentFactory {

    private WLMModel model;

    /** Creates a new instance of WSDLComponentFactoryImpl */
    public WLMComponentFactoryImpl(WLMModel model) {
        this.model = model;
    }

    public WLMComponent create(Element element, WLMComponent context) {

        return context.createChild(element);
    }

    public WLMComponent create(WLMComponent parent, QName qName) {
        String q = qName.getPrefix();
        if (q == null || q.length() == 0) {
            q = qName.getLocalPart();
        } else {
            q = q + ":" + qName.getLocalPart();
        }
        Element element = model.getDocument().createElementNS(
                qName.getNamespaceURI(), q);
        return parent.createChild(element);
    }

    public TImport createImport(WLMModel model) {
        return new ImportImpl(model);
    }

    public TInit createInit(WLMModel model) {
        return new InitImpl(model);
    }

    public TTitle createTitle(WLMModel model) {
        return new TitleImpl(model);
    }

    public TPriority createPriority(WLMModel model) {
        return new PriorityImpl(model);
    }

    public VariableInit createVariableInit(WLMModel model) {
        return new VariableInitImpl(model);
    }

    public TCopy createCopy(WLMModel model) {
        return new CopyImpl(model);
    }

    public TFrom createFrom(WLMModel model) {
        return new FromImpl(model);
    }

    public TTo createTo(WLMModel model) {
        return new ToImpl(model);
    }

    public TAssignment createAssignment(WLMModel model) {
        return new AssignmentImpl(model);
    }

    public TExcluded createExcluded(WLMModel model) {
        return new ExcludedImpl(model);
    }

    public User createUser(WLMModel model) {
        return new UserImpl(model);
    }

    public Group createGroup(WLMModel model) {
        return new GroupImpl(model);
    }

    public TTimeout createTimeout(WLMModel model) {
        return new TimeoutImpl(model);
    }

    public TDeadlineExpr createDeadline(WLMModel model) {
        return new DeadlineImpl(model);
    }

    public TDurationExpr createDuration(WLMModel model) {
        return new DurationImpl(model);
    }

    public TEscalation createEscalation(WLMModel model) {
        return new EscalationImpl(model);
    }

    public TNotification createNotification(WLMModel model) {
        return new NotificationImpl(model);
    }

    public TAction createAction(WLMModel model) {
        return new ActionImpl(model);
    }

    public TEmail createEmail(WLMModel model) {
        return new EmailImpl(model);
    }

    public EmailAddress createEmailAddress(WLMModel model) {
        return new EmailAddressImpl(model);
    }

    public TMessage createMessage(WLMModel model) {
        return new MessageImpl(model);
    }

    public MessageSubject createMessageSubject(WLMModel model) {
        return new MessageSubjectImpl(model);
    }

    public MessageBody createMessageBody(WLMModel model) {
        return new MessageBodyImpl(model);
    }

    public TChangeVariables createChangeVariables(WLMModel model) {
        return new ChangeVariablesImpl(model);
    }

    public TLocalNotification createLocalNotification(WLMModel model) {
        return new LocalNotificationImpl(model);
    }

    public Keyword createKeyword(WLMModel model) {
        return new KeywordImpl(model);
    }

    public TKeywords createKeywords(WLMModel model) {
        return new KeywordsImpl(model);
    }
}
