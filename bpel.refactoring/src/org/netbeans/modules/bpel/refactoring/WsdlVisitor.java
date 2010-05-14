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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.bpel.refactoring;

import java.util.List;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor;

import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.06.27
 */
final class WsdlVisitor extends ChildVisitor {

    WsdlVisitor(List<Component> usage, Referenceable target) {
        myUsage = usage;
        myTarget = target;
    }

    @Override
    public void visit(ExtensibilityElement element) {
        checkUsages(element);

        if (element instanceof CorrelationProperty) {
            visitProperty((CorrelationProperty) element);
        } else if (element instanceof PropertyAlias) {
            visitAlias((PropertyAlias) element);
        } else if (element instanceof PartnerLinkType) {
            visitPartnerLinkType((PartnerLinkType) element);
        }
    }

    private void visitProperty(CorrelationProperty property) {
//out();
//out("PROPERTY: " + property);
        Util.visit(property.getType(), property.getElement(), myTarget, property, myUsage);
    }

    private void visitAlias(PropertyAlias alias) {
//out();
//out("ALIAS: " + alias);
        Util.visit(alias.getType(), alias.getElement(), myTarget, alias, myUsage);
        Util.visit(alias.getMessageType(), myTarget, alias, myUsage);
        Util.visit(alias.getPropertyName(), myTarget, alias, myUsage);

        if (myTarget instanceof Part) {
            visit(alias, (Part) myTarget);
        }
        if (!(myTarget instanceof Named)) {
            return;
        }
        Query query = alias.getQuery();

        if (Util.checkQuery(query, ((Named) myTarget).getName()) != -1) {
            myUsage.add(query);
        }
    }

    private void visitPartnerLinkType(PartnerLinkType partnerLinkType) {
        visitRole(partnerLinkType.getRole1());
        visitRole(partnerLinkType.getRole2());
    }

    private void visitRole(Role role) {
//out();
//out("ROLE: " + role);
        if (role == null) {
            return;
        }
        Util.visit(role.getPortType(), myTarget, role, myUsage);
        checkUsages(role);
    }

    private void visit(PropertyAlias alias, Part part) {
        if (!part.getName().equals(alias.getPart())) {
            return;
        }
        Reference reference = alias.getMessageType();

        if (reference == null || reference.get() == null) {
            return;
        }
        if (reference.get().equals(part.getParent())) {
            myUsage.add(alias);
        }
    }

    private void checkUsages(Component component) {
        if (myTarget.equals(component)) {
            myUsage.add(component);
        }
    }

    private List<Component> myUsage;
    private Referenceable myTarget;
}
