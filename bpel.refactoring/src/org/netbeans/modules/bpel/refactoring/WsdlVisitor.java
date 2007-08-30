/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

import static org.netbeans.modules.print.ui.PrintUI.*;

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
  public void visit(ExtensibilityElement element) 
  {
    checkUsages(element);

    if (element instanceof CorrelationProperty) {
      visitProperty((CorrelationProperty) element);
    }
    else if (element instanceof PropertyAlias) {
      visitAlias((PropertyAlias) element);
    }
    else if (element instanceof PartnerLinkType) {
      visitPartnerLinkType((PartnerLinkType) element);
    }
  }

  private void visitProperty(CorrelationProperty property) {
//out();
//out("PROPERTY: " + property);
    Util.visit(
      property.getType(),
      property.getElement(),
      myTarget,
      property,
      myUsage
    );
  }

  private void visitAlias(PropertyAlias alias) {
//out();
//out("ALIAS: " + alias);
    Util.visit(
      alias.getType(),
      alias.getElement(),
      myTarget,
      alias,
      myUsage
    );
    Util.visit(
      alias.getMessageType(),
      myTarget,
      alias,
      myUsage
    );
    Util.visit(
      alias.getPropertyName(),
      myTarget,
      alias,
      myUsage
    );
    if (myTarget instanceof Part) {
      visit(alias, (Part) myTarget);
    }
    if ( !(myTarget instanceof Named)) {
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
    Util.visit(
      role.getPortType(),
      myTarget,
      role,
      myUsage
    );
    checkUsages(role);
  }

  private void visit(PropertyAlias alias, Part part) {
    if ( !part.getName().equals(alias.getPart())) {
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

  private Referenceable myTarget;
  private List<Component> myUsage;
}
