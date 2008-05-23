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
package org.netbeans.modules.bpel.validation.variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;

import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.bpel.validation.core.BpelValidator;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitorAdaptor;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.03.03
 */
public final class Validator extends BpelValidator {

  @Override
  protected final SimpleBpelModelVisitor getVisitor() { return new SimpleBpelModelVisitorAdaptor() {

  // # 135160
  @Override
  public void visit(Assign assign) {
    List<Copy> copies = list(assign.getChildren(Copy.class));

    for (int i=0; i < copies.size(); i++) {
      for (int j=i+1; j < copies.size(); j++) {
        checkCopies(copies.get(i), copies.get(j));
      }
    }
  }

  private void checkCopies(Copy copy1, Copy copy2) {
//out();
//out("see: " + copy1 + " "  + copy2);
    if (checkTo(copy1.getTo(), copy2.getTo()) && checkFrom(copy1.getFrom(), copy2.getFrom())) {
      addError("FIX_duplicate_copies", copy1); // NOI18N
      addError("FIX_duplicate_copies", copy2); // NOI18N
    }
  }

  private boolean checkTo(To to1, To to2) {
    if (checkContent(to1, to2)) {
      return true;
    }
    if (checkVariable(to1, to1, to2, to2)) {
      return true;
    }
    if (checkPartnerLink(to1, to2)) {
      return true;
    }
    return false;
  }

  private boolean checkPartnerLink(PartnerLinkReference partnerLinkReference1, PartnerLinkReference partnerLinkReference2) {
    // 1
    BpelReference<PartnerLink> partRef1 = partnerLinkReference1.getPartnerLink();

    if (partRef1 == null) {
      return false;
    }
    PartnerLink partnerLink1 = partRef1.get();

    if (partnerLink1 == null) {
      return false;
    }
    // 2
    BpelReference<PartnerLink> partRef2 = partnerLinkReference2.getPartnerLink();

    if (partRef2 == null) {
      return false;
    }
    PartnerLink partnerLink2 = partRef2.get();

    if (partnerLink2 == null) {
      return false;
    }
    return partnerLink1.equals(partnerLink2);
  }

  private boolean checkVariable(VariableReference variableReference1, PartReference partReference1, VariableReference variableReference2, PartReference partReference2) {
    // 1
    BpelReference<VariableDeclaration> varRef1 = variableReference1.getVariable();

    if (varRef1 == null) {
      return false;
    }
    VariableDeclaration variable1 = varRef1.get();

    if (variable1 == null) {
      return false;
    }
    WSDLReference<Part> partRef1 = partReference1.getPart();

    if (partRef1 == null) {
      return false;
    }
    Part part1 = partRef1.get();

    if (part1 == null) {
      return false;
    }
    // 2
    BpelReference<VariableDeclaration> varRef2 = variableReference2.getVariable();

    if (varRef2 == null) {
      return false;
    }
    VariableDeclaration variable2 = varRef2.get();

    if (variable2 == null) {
      return false;
    }
    WSDLReference<Part> partRef2 = partReference2.getPart();

    if (partRef2 == null) {
      return false;
    }
    Part part2 = partRef2.get();

    if (part2 == null) {
      return false;
    }
    return variable1.equals(variable2) && part1.equals(part2);
  }

  private boolean checkContent(ContentElement content1, ContentElement content2) {
    String value1 = content1.getContent();

    if (value1 == null || value1.length() == 0) {
      return false;
    }
    String value2 = content2.getContent();

    if (value2 == null || value2.length() == 0) {
      return false;
    }
//out();
//out("value1: " + value1);
//out("value2: " + value2);
    return value1.equals(value2);
  }

  private boolean checkFrom(From from1, From from2) {
    if (checkContent(from1, from2)) {
      return true;
    }
    if (checkVariable(from1, from1, from2, from2)) {
      return true;
    }
    if (checkPartnerLink(from1, from2)) {
      return true;
    }
    return false;
  }

  private List<Copy> list(Collection<Copy> collection) {
    List<Copy> list = new ArrayList<Copy>();

    if (collection == null) {
      return list;
    }
    Iterator<Copy> iterator = collection.iterator();

    while (iterator.hasNext()) {
      list.add(iterator.next());
    }
    return list;
  }

  @Override
  public void visit(OnMessage onMessage) {
    checkVariable(onMessage, onMessage, true);
  }

  @Override
  public void visit(Receive receive) {
    checkVariable(receive, receive, true);
  }

  @Override
  public void visit(Reply reply) {
    checkVariable(reply, reply, false);
  }

  // # 116242
  private void checkVariable(VariableReference variableReference, OperationReference operationReference, boolean isInput) {
    BpelReference<VariableDeclaration> ref2 = variableReference.getVariable();
    
    if (ref2 != null && ref2.get() != null) {
      return;
    }
//out("NO VARIABLE");
    WSDLReference<Operation> ref = operationReference.getOperation();

    if (ref == null) {
      return;
    }
    Operation operation = ref.get();

    if (operation == null) {
      return;
    }
    OperationParameter parameter;

    if (isInput) {
      parameter = operation.getInput();
    }
    else {
      parameter = operation.getOutput();
    }
    if (parameter == null) {
      return;
    }
    NamedComponentReference<Message> ref1 = parameter.getMessage();

    if (ref1 == null) {
      return;
    }
    Message message = ref1.get();

    if (message == null) {
      return;
    }
    Collection<Part> parts = message.getParts();

    if (parts == null) {
      return;
    }
//out();
//out("SIZE: " + parts.size());
//out();
    if (parts.size() != 0) {
      addError("FIX_WSDL_message_variable", (Component) variableReference);
    }
  }

};}}
