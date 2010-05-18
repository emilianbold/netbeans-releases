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

import org.netbeans.modules.wlm.model.api.TAction;
import org.netbeans.modules.wlm.model.api.TActionType;
import org.netbeans.modules.wlm.model.api.TChangeVariables;
import org.netbeans.modules.wlm.model.api.TLocalNotification;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMVisitor;
import org.w3c.dom.Element;


public class ActionImpl extends WLMComponentBase implements TAction {

    public ActionImpl(WLMModel model, Element e) {
        super(model, e);
    }

    public ActionImpl(WLMModel model) {
        this(model, createNewElement(WLMQNames.ACTION.getQName(), model));
    }

    @Override
    public Class<? extends WLMComponent> getElementType() {
        return TAction.class;
    }

    public TActionType getType() {
        String typeStr = getAttribute(WLMAttribute.TYPE);
        return TActionType.fromValue(typeStr);
    }

    public void setType(TActionType value) {
        setAttribute(TYPE_PROPERTY, WLMAttribute.TYPE, value.value());
    }

    public void accept(WLMVisitor visitor) {
        visitor.visitAction(this);
    }

    public WLMComponent createChild(Element childEl) {
        WLMComponent child = null;
        if (childEl != null) {
            String localName = childEl.getLocalName();
            if (localName == null || localName.length() == 0) {
                localName = childEl.getTagName();
            }

            if (localName.equals(CHANGE_VARIABLES_ELEMENT_NAME)) {
                child = new VariableInitImpl(getModel(), childEl);
            } else if (localName.equals(LOCAL_NOTIFICATION_ELEMENT_NAME)) {
                child = new LocalNotificationImpl(getModel(), childEl);
            }
        }
        return child;
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

    public void setName(String name) {
        setAttribute(NAME_PROPERTY, WLMAttribute.NAME, name);
    }

    public String getName() {
        return getAttribute(WLMAttribute.NAME);
    }

    public TChangeVariables getChangeVariables() {
        return getChild(TChangeVariables.class);
    }

    public void setChangeVariables(TChangeVariables changeVariables) {
        setChild(TChangeVariables.class, CHANGE_VARIABLES_ELEMENT_NAME, 
                changeVariables, CHANGE_VARIABLES_POSITON);
    }

    public void removeChangeVariables(TChangeVariables changeVariables) {
        removeChild(CHANGE_VARIABLES_ELEMENT_NAME, changeVariables);
    }

    private static final ElementPosition LOCAL_NOTIFICATION_POSITION
            = new ElementPosition(TLocalNotification.class);
    private static final ElementPosition CHANGE_VARIABLES_POSITON
            = new ElementPosition(LOCAL_NOTIFICATION_POSITION,
            TChangeVariables.class);
}
