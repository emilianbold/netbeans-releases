
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

package org.netbeans.modules.wlm.validation.rules;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.wlm.model.api.TEmail;
import org.netbeans.modules.wlm.model.api.TNotification;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WSDLReference;
import org.netbeans.modules.wlm.validation.WLMValidationResultBuilder;
import org.netbeans.modules.wlm.validation.WLMValidationRule;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;


/**
 *
 * @author anjeleevich
 */
public class NotificationWSDLValidationRule extends WLMValidationRule {

    public NotificationWSDLValidationRule(
            WLMValidationResultBuilder builder)
    {
        super(builder);
    }

    @Override
    public void checkRule() {
        List<TNotification> notifications = getNotifications();
        if (notifications == null) {
            return;
        }

        for (TNotification notification : notifications) {
            TEmail email = notification.getEmail();
            if (email == null) {
                continue;
            }

            WSDLReference<Operation> operationRef = email.getOperation();

            if (operationRef == null) {
                addError(email, "OPERATION_IS_NOT_SPECIFIED"); // NOI18N
            } else {
                Operation operation = operationRef.get();
                if (operation == null) {
                    addError(email, "UNABLE_TO_RESOLVE_OPERATION"); // NOI18N
                } else {
                    Input input = operation.getInput();
                    NamedComponentReference<Message> messageRef
                            = (input == null) ? null : input.getMessage();
                    Message message = (messageRef == null) ? null
                            : messageRef.get();

                    Collection<Part> parts = (message == null) ? null 
                            : message.getParts();

                    int partsSize = (parts == null) ? 0 : parts.size();
                    if (partsSize == 3 || partsSize == 4) {
                        boolean hasAddressesPart = false;
                        boolean hasSubjectPart = false;
                        boolean hasMessagePart = false;
                        boolean hasFromPart = false;

                        for (Part part : parts) {
                            String partName = part.getName();
                            hasAddressesPart |= "addresses".equals(partName); // NOI18N
                            hasSubjectPart |= "subject".equals(partName); // NOI18N
                            hasMessagePart |= "message".equals(partName); // NOI18N
                            hasFromPart |= "from".equals(partName); // NOI18N
                        }

                        if ((partsSize == 3) 
                                && hasAddressesPart
                                && hasSubjectPart
                                && hasMessagePart)
                        {
                            return;
                        }

                        if ((partsSize == 4)
                                && hasAddressesPart
                                && hasSubjectPart
                                && hasMessagePart
                                && hasFromPart)
                        {
                            return;
                        }
                    }

                    addError(email, "NOTIFICATION_WRONG_PARTS"); // NOI18N
                }
            }
        }
    }


    private List<TNotification> getNotifications() {
        WLMModel model = getModel();
        TTask task = (model == null) ? null : model.getTask();
        return (task == null) ? null : task.getNotifications();
    }
}
