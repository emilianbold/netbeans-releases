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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.impl;

import org.netbeans.modules.wlm.model.api.AssignmentHolder;
import org.netbeans.modules.wlm.model.api.DeadlineOrDuration;
import org.netbeans.modules.wlm.model.api.EmailAddress;
import org.netbeans.modules.wlm.model.api.Group;
import org.netbeans.modules.wlm.model.api.Keyword;
import org.netbeans.modules.wlm.model.api.LocalNotificationsHolder;
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
import org.netbeans.modules.wlm.model.api.VariableInit;
import org.netbeans.modules.wlm.model.api.User;
import org.netbeans.modules.wlm.model.api.UsersAndGroupsHolder;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMVisitor;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 * Visitor to add or remove a child of a WSDL component.
 * 
 * @author Nam Nguyen
 */
public class ChildComponentUpdateVisitor<T extends WLMComponent> implements
        WLMVisitor, ComponentUpdater<T> {

    private Operation operation;
    private WLMComponent parent;
    private int index;
    private boolean canAdd = false;

    /**
     * Creates a new instance of ChildComponentUpdateVisitor
     */
    public ChildComponentUpdateVisitor() {
    }

    public boolean canAdd(WLMComponent target, Component child) {
        if (!(child instanceof WLMComponent)) {
            return false;
        }
        update(target, (WLMComponent) child, null);
        return canAdd;
    }

    public void update(WLMComponent target, WLMComponent child,
            Operation operation) {
        update(target, child, -1, operation);
    }

    public void update(WLMComponent target, WLMComponent child, int index,
            Operation operation) {
        assert target != null;
        assert child != null;

        this.parent = target;
        this.operation = operation;
        this.index = index;

        child.accept(this);
    }

    @SuppressWarnings("unchecked")
    private void addChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).insertAtIndex(eventName, child, index);
    }

    @SuppressWarnings("unchecked")
    private void removeChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).removeChild(eventName, child);
    }

    private void checkOperationOnUnmatchedParent() {
        if (operation != null) {
            // note this unmatch should be caught by validation,
            // we don't want the UI view to go blank on invalid but still
            // well-formed document
            // throw new IllegalArgumentException("Unmatched parent-child
            // components"); //NO18N
        } else {
            canAdd = false;
        }
    }

    public void visitAction(TAction action) {
        if (parent instanceof TTask) {
            TTask target = (TTask) parent;
            if (operation == Operation.ADD) {
                addChild(TTask.ACTION_PROPERTY, action);
            } else if (operation == Operation.REMOVE) {
                target.removeAction(action);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitAssignment(TAssignment assignment) {
        if (parent instanceof AssignmentHolder) {
            AssignmentHolder target = (AssignmentHolder) parent;
            if (operation == Operation.ADD) {
                target.setAssignment(assignment);
            } else if (operation == Operation.REMOVE) {
                target.removeAssignment(assignment);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitExcluded(TExcluded excluded) {
        if (parent instanceof TAssignment) {
            TAssignment target = (TAssignment) parent;
            if (operation == Operation.ADD) {
                target.setExcluded(excluded);
            } else if (operation == Operation.REMOVE) {
                target.removeExcluded(excluded);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitEscalation(TEscalation escalation) {
        if (parent instanceof TTask) {
            TTask target = (TTask) parent;
            if (operation == Operation.ADD) {
                addChild(TTask.ESCALATION_PROPERTY, escalation);
            } else if (operation == Operation.REMOVE) {
                target.removeEscalation(escalation);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitGroup(Group group) {
        if (parent instanceof UsersAndGroupsHolder) {
            UsersAndGroupsHolder target = (UsersAndGroupsHolder) parent;
            if (operation == Operation.ADD) {
                addChild(UsersAndGroupsHolder.GROUP_PROPERTY, group);
            } else if (operation == Operation.REMOVE) {
                target.removeGroup(group);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitImport(TImport importEl) {
        if (parent instanceof TTask) {
            TTask target = (TTask) parent;
            if (operation == Operation.ADD) {
                addChild(TTask.IMPORT_TYPE_PROPERTY, importEl);
            } else if (operation == Operation.REMOVE) {
                target.removeImport(importEl);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitNotification(TNotification notification) {
        if (parent instanceof TTask) {
            TTask target = (TTask) parent;
            if (operation == Operation.ADD) {
                addChild(TTask.NOTIFICATION_PROPERTY, notification);
            } else if (operation == Operation.REMOVE) {
                target.removeNotification(notification);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitTask(TTask task) {
        checkOperationOnUnmatchedParent();
    }

    public void visitTimeout(TTimeout timeout) {
        if (parent instanceof TTask) {
            TTask target = (TTask) parent;
            if (operation == Operation.ADD) {
                addChild(TTask.TIMEOUT_PROPERTY, timeout);
            } else if (operation == Operation.REMOVE) {
                target.removeTimeOut(timeout);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitUser(User user) {
        if (parent instanceof UsersAndGroupsHolder) {
            UsersAndGroupsHolder target = (UsersAndGroupsHolder) parent;
            if (operation == Operation.ADD) {
                addChild(UsersAndGroupsHolder.USER_PROPERTY, user);
            } else if (operation == Operation.REMOVE) {
                target.removeUser(user);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitDeadLine(TDeadlineExpr deadline) {
        if (parent instanceof DeadlineOrDuration) {
            DeadlineOrDuration target = (DeadlineOrDuration) parent;
            if (operation == Operation.ADD) {
                target.setDeadline(deadline);
            } else if (operation == Operation.REMOVE) {
                target.removeDeadline(deadline);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitDuration(TDurationExpr duration) {
        if (parent instanceof DeadlineOrDuration) {
            DeadlineOrDuration target = (DeadlineOrDuration) parent;
            if (operation == Operation.ADD) {
                target.setDuration(duration);
            } else if (operation == Operation.REMOVE) {
                target.removeDuration(duration);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitInit(TInit init) {
        if (parent instanceof TTask) {
            TTask target = (TTask) parent;
            if (operation == Operation.ADD) {
                target.setInit(init);
            } else if (operation == Operation.REMOVE) {
                target.removeInit(init);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitPriority(TPriority priority) {
        if (parent instanceof TTask) {
            TTask target = (TTask) parent;
            if (operation == Operation.ADD) {
                target.setPriority(priority);
            } else if (operation == Operation.REMOVE) {
                target.removePriority(priority);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitTitle(TTitle title) {
        if (parent instanceof TTask) {
            TTask target = (TTask) parent;
            if (operation == Operation.ADD) {
                target.setTitle(title);
            } else if (operation == Operation.REMOVE) {
                target.removeTitle(title);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitVariableInit(VariableInit variableInit) {
        if (parent instanceof TInit) {
            TInit target = (TInit) parent;
            if (operation == Operation.ADD) {
                target.setVariableInit(variableInit);
            } else if (operation == Operation.REMOVE) {
                target.removeVariableInit(variableInit);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitCopy(TCopy copy) {
        if (parent instanceof TChangeVariables) {
            TChangeVariables target = (TChangeVariables) parent;
            if (operation == Operation.ADD) {
                addChild(TChangeVariables.COPY_ELEMENT_NAME, copy);
            } else if (operation == Operation.REMOVE) {
                target.removeCopy(copy);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitFrom(TFrom from) {
        if (parent instanceof TCopy) {
            TCopy target = (TCopy) parent;
            if (operation == Operation.ADD) {
                target.setFrom(from);
            } else if (operation == Operation.REMOVE) {
                target.removeFrom(from);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitTo(TTo to) {
        if (parent instanceof TCopy) {
            TCopy target = (TCopy) parent;
            if (operation == Operation.ADD) {
                target.setTo(to);
            } else if (operation == Operation.REMOVE) {
                target.removeTo(to);
            } else if (operation == null) {
                canAdd = true;
            } else {
                checkOperationOnUnmatchedParent();
            }
        }    
    }

    public void visitMessage(TMessage message) {
        if (parent instanceof TNotification) {
            TNotification target = (TNotification) parent;
            if (operation == Operation.ADD) {
                target.setMessage(message);
            } else if (operation == Operation.REMOVE) {
                target.removeMessage(message);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitMessageBody(MessageBody messageBody) {
        if (parent instanceof TMessage) {
            TMessage target = (TMessage) parent;
            if (operation == Operation.ADD) {
                target.setBody(messageBody);
            } else if (operation == Operation.REMOVE) {
                target.removeBody(messageBody);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitMessageSubject(MessageSubject messageSubject) {
        if (parent instanceof TMessage) {
            TMessage target = (TMessage) parent;
            if (operation == Operation.ADD) {
                target.setSubject(messageSubject);
            } else if (operation == Operation.REMOVE) {
                target.removeSubject(messageSubject);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitEmail(TEmail email) {
        if (parent instanceof TNotification) {
            TNotification target = (TNotification) parent;
            if (operation == Operation.ADD) {
                target.setEmail(email);
            } else if (operation == Operation.REMOVE) {
                target.removeEmail(email);
            } else if (operation == null) {
                canAdd = (target.getEmail() == null); // EXP
            }
        } else {
            checkOperationOnUnmatchedParent();
        }    
    }

    public void visitEmailAddress(EmailAddress emailAddress) {
        if (parent instanceof TEmail) {
            TEmail email = (TEmail) parent;
            if (operation == Operation.ADD) {
                addChild(TEmail.ADDRESS_ELEMENT_NAME, emailAddress);
            } else if (operation == Operation.REMOVE) {
                email.removeAddress(emailAddress);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }    
    }

    public void visitLocalNotification(TLocalNotification localNotification) {
        if (parent instanceof LocalNotificationsHolder) {
            LocalNotificationsHolder holder
                    = (LocalNotificationsHolder) parent;
            
            if (operation == Operation.ADD) {
                addChild(LocalNotificationsHolder
                        .LOCAL_NOTIFICATION_ELEMENT_NAME, localNotification);
            } else if (operation == Operation.REMOVE) {
                holder.removeLocalNotification(localNotification);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitChangeVariables(TChangeVariables changeVariables) {
        if (parent instanceof TAction) {
            TAction target = (TAction) parent;
            if (operation == Operation.ADD) {
                target.setChangeVariables(changeVariables);
            } else if (operation == Operation.REMOVE) {
                target.removeChangeVariables(changeVariables);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitKeyword(Keyword keyword) {
        if (parent instanceof TKeywords) {
            TKeywords target = (TKeywords) parent;
            if (operation == Operation.ADD) {
                addChild(TKeywords.KEYWORD_PROPERTY, keyword);
            } else if (operation == Operation.REMOVE) {
                target.removeKeyword(keyword);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visitKeywords(TKeywords keywords) {
        if (parent instanceof TTask) {
            TTask target = (TTask) parent;
            if (operation == Operation.ADD) {
                target.setKeywords(keywords);
            } else if (operation == Operation.REMOVE) {
                target.removeKeywords(keywords);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
}
