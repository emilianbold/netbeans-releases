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

import java.util.List;
import org.netbeans.modules.wlm.model.api.TAssignment;
import org.netbeans.modules.wlm.model.api.TDeadlineExpr;
import org.netbeans.modules.wlm.model.api.TDurationExpr;
import org.netbeans.modules.wlm.model.api.TEscalation;
import org.netbeans.modules.wlm.model.api.TLocalNotification;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMVisitor;
import org.w3c.dom.Element;

public class EscalationImpl extends DeadLineOrDurationImpl implements
        TEscalation {

    public EscalationImpl(WLMModel model, Element e) {
        super(model, e);
    }

    public EscalationImpl(WLMModel model) {
        this(model, createNewElement(WLMQNames.ESCALATION.getQName(), model));
    }

    public void accept(WLMVisitor visitor) {
        visitor.visitEscalation(this);
    }

    @Override
    public WLMComponent createChild(Element childElement) {
        WLMComponent child = null;
        if (childElement != null) {
            String localName = childElement.getLocalName();
            if (localName == null || localName.length() == 0) {
                localName = childElement.getTagName();
            }
            if (ASSIGNMENT_ELEMENT_NAME.equals(localName)) {
                child = new AssignmentImpl(getModel(), childElement);
            } else if (LOCAL_NOTIFICATION_ELEMENT_NAME.equals(localName)) {
                child = new LocalNotificationImpl(getModel(), childElement);
            } else {
                child = super.createChild(childElement);
            }
        }
        return child;
    }
    
    public TAssignment getAssignment() {
        return getChild(TAssignment.class);
    }

    public void setAssignment(TAssignment value) {
        setChild(TAssignment.class, ASSIGNMENT_ELEMENT_NAME, value,
                ASSIGNMENT_POSITON);
    }

    public void removeAssignment(TAssignment value) {
        removeChild(ASSIGNMENT_ELEMENT_NAME, value);
    }

    public List<TLocalNotification> getLocalNotifications() {
        return getChildren(TLocalNotification.class);
    }

    public void addLocalNotification(TLocalNotification localNotification) {
        addAfter(LOCAL_NOTIFICATION_ELEMENT_NAME, localNotification, 
                LOCAL_NOTIFICATION_POSITION);
    }

    public void removeLocalNotification(TLocalNotification localNotification) {
        removeChild(LOCAL_NOTIFICATION_ELEMENT_NAME, localNotification);
    }

    public boolean hasLocalNotifications() {
        return (getChild(TLocalNotification.class) != null);
    }
    
    private static final ElementPosition TIMEOUT_POSITION 
            = new ElementPosition(TDeadlineExpr.class, TDurationExpr.class);
    
    private static final ElementPosition ASSIGNMENT_POSITON
            = new ElementPosition(TIMEOUT_POSITION, TAssignment.class);
    
    private static final ElementPosition LOCAL_NOTIFICATION_POSITION
            = new ElementPosition(ASSIGNMENT_POSITON, TLocalNotification.class);
}
