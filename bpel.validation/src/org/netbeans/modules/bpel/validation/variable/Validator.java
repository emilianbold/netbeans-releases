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
import java.util.LinkedList;

import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.ExpressionUpdater;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;

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
  protected SimpleBpelModelVisitor getVisitor() { return new SimpleBpelModelVisitorAdaptor() {

  // # 94195
  @Override
  public void visit(VariableContainer container) {
    Variable [] variables = container.getVariables();
//out();
//out("WE: " + container.getParent().getClass().getName());

    if (variables == null) {
      return;
    }
    List<VariableInfo> infos = new LinkedList<VariableInfo>();

    for (Variable variable : variables) {
      infos.add(new VariableInfo(variable));
    }
    findVariables(container.getParent(), infos);

//out();
    boolean isInitialized;
    boolean isUsed;

    for (VariableInfo info : infos) {
//out("  " + info);
      isInitialized = info.isInitialized();
      isUsed = info.isUsed();
      Variable variable = info.getVariable();
      String name = variable.getName();

      if ( !isInitialized && !isUsed) {
        addWarning("FIX_not_initialized_and_not_used", variable, name); // NOI18N
      }
      else if ( !isInitialized && isUsed) {
        addError("FIX_not_initialized_but_used", variable, name); // NOI18N
      }
//      else if (isInitialized && !isUsed) {
//todo r        addWarning("FIX_initialized_and_not_used", variable, name); // NOI18N
//      }
    }
  }

  private void findVariables(BpelEntity entity, List<VariableInfo> infos) {
//out("    see: " + getName(entity));
    checkInitialization(entity, infos);
    checkUsages(entity, infos);
    Collection<BpelEntity> children = entity.getChildren();

    for (BpelEntity child : children) {
      findVariables(child, infos);
    }
  }

  private void checkInitialization(BpelEntity entity, List<VariableInfo> infos) {
    if (entity instanceof To || entity instanceof Receive || entity instanceof OnMessage) {
      checkInitializationVariableReference((VariableReference) entity, infos);
    }
    if (entity instanceof ContentElement && entity instanceof To) {
      checkInitializationContent((ContentElement) entity, infos);
    }
    if (entity instanceof Invoke) {
      checkInfoVariableDeclaration(((Invoke) entity).getOutputVariable(), infos, false);
    }
    if (entity instanceof Catch) {
      checkInfoVariable(((Catch) entity).getFaultVariable(), infos, false);
    }
  }

  private void checkUsages(BpelEntity entity, List<VariableInfo> infos) {
    if (entity instanceof From || entity instanceof Reply) {
      checkUsagesVariableReference((VariableReference) entity, infos);
    }
    if (entity instanceof ContentElement && !(entity instanceof To)) {
      checkUsagesContent((ContentElement) entity, infos);
    }
    if (entity instanceof Invoke) {
      checkInfoVariableDeclaration(((Invoke) entity).getInputVariable(), infos, true);
    }
    if (entity instanceof Throw) {
      checkInfoVariableDeclaration(((Throw) entity).getFaultVariable(), infos, true);
    }
    //!
// todo r
//    if (entity instanceof OnMessage) {
//      checkUsagesVariableReference((VariableReference) entity, infos);
//    }
  }

  private void checkInitializationContent(ContentElement content, List<VariableInfo> infos) {
    checkInfoContent(content, infos, false);
  }

  private void checkUsagesContent(ContentElement content, List<VariableInfo> infos) {
    checkInfoContent(content, infos, true);
  }

  private void checkInfoContent(ContentElement content, List<VariableInfo> infos, boolean isUsed) {
//out();
    String expression = content.getContent();
//out("        check content: " + expression);
    Collection<String> variables = ExpressionUpdater.getInstance().getUsedVariables(expression);
//out("        variables: " + variables);

    if (variables == null) {
      return;
    }
    for (String variable : variables) {
      for (VariableInfo info : infos) {
//out("            : " + info.getVariable().getName());
//out("            : " + variable);
        if (info.getVariable().getName().equals(variable)) {
          if (isUsed) {
//out(" set used content: " + info.getVariable().getName());
            info.setUsed();
          }
          else {
//out(" set init content: " + info.getVariable().getName());
            info.setInitialized();
          }
        }
      }
    }
  }

  private void checkInitializationVariableReference(VariableReference reference, List<VariableInfo> infos) {
    checkInfoVariableReference(reference, infos, false);
  }

  private void checkUsagesVariableReference(VariableReference reference, List<VariableInfo> infos) {
    checkInfoVariableReference(reference, infos, true);
  }

  private void checkInfoVariableReference(VariableReference reference, List<VariableInfo> infos, boolean isUsed) {
    checkInfoVariableDeclaration(reference.getVariable(), infos, isUsed);
  }

  private void checkInfoVariableDeclaration(BpelReference<VariableDeclaration> reference, List<VariableInfo> infos, boolean isUsed) {
//if (isUsed) out("check V D: " + reference);
    if (reference == null) {
      return;
    }
    VariableDeclaration declaration = reference.get();
//if (isUsed) out("      V D: " + declaration);

    if (declaration == null) {
      return;
    }
    checkInfoVariable(declaration.getVariableName(), infos, isUsed);
  }

  private void checkInfoVariable(String variable, List<VariableInfo> infos, boolean isUsed) {
    if (variable == null) {
      return;
    }
    for (VariableInfo info : infos) {
      if (info.getVariable().getName().equals(variable)) {
        if (isUsed) {
//out(" set used: " + info.getVariable().getName());
          info.setUsed();
        }
        else {
//out(" set init: " + info.getVariable().getName());
          info.setInitialized();
        }
      }
    }
  }

  // # 83632
  @Override
  public void visit(Flow flow) {
//out();
//out("Flow: " + flow);
    List<List<VariablePart>> list = new ArrayList<List<VariablePart>>();
    Collection<BpelEntity> children = flow.getChildren();

    for (BpelEntity child : children) {
      List<VariablePart> variables = new LinkedList<VariablePart>();
      findVariableParts(child, variables);

      if ( !variables.isEmpty()) {
        list.add(variables);
      }
    }
    for (int i=0; i < list.size(); i++) {
      for (int j=i+1; j < list.size(); j++) {
        VariablePart variablePart = getCommonVariable(list.get(i), list.get(j));

        if (variablePart != null) {
          addWarning("FIX_Variable_in_Flow", flow, flow.getName(), variablePart.toString()); // NOI18N
          break;
        }
      }
    }
  }

  private VariablePart getCommonVariable(List<VariablePart> variables1, List<VariablePart> variables2) {
    for (VariablePart variable : variables1) {
      if (variables2.contains(variable)) {
        return variable;
      }
    }
    return null;
  }

  private void findVariableParts(BpelEntity entity, List<VariablePart> variables) {
    if (entity instanceof Scope) {
      return;
    }
    if (entity instanceof Pick) {
      return;
    }
    if (entity instanceof Assign) {
      findVariablePartsInAssign((Assign) entity, variables);
    }
    Collection<BpelEntity> children = entity.getChildren();

    for (BpelEntity child : children) {
      findVariableParts(child, variables);
    }
  }

  private void findVariablePartsInAssign(Assign assign, List<VariablePart> variables) {
    Collection<Copy> copies = assign.getChildren(Copy.class);

    for (Copy copy : copies) {
      To to = copy.getTo();
      BpelReference<VariableDeclaration> ref = to.getVariable();

      if (ref == null) {
        continue;
      }
      VariableDeclaration declaration = ref.get();

      if (declaration == null) {
        continue;
      }
      variables.add(new VariablePart(declaration, getPart(to)));
    }
  }

  private Part getPart(To to) {
    WSDLReference<Part> ref = to.getPart();

    if (ref == null) {
      return null;
    }
    return ref.get();
  }

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
      addError("FIX_SA00047", (Component) variableReference); // NOI18N
    }
  }};}

  // -------------------------
  private class VariablePart {
    VariablePart(VariableDeclaration variable, Part part) {
      myVariable = variable;
      myPart = part;
    }

    @Override
    public boolean equals(Object object) {
      if ( !(object instanceof VariablePart)) {
        return false;
      }
      VariablePart variablePart = (VariablePart) object;

      return variablePart.myVariable == myVariable && variablePart.myPart == myPart;
    }

    @Override
    public int hashCode() {
      if (myPart == null) {
        return myVariable.hashCode();
      }
      else {
        return myVariable.hashCode() * myPart.hashCode();
      }
    }

    @Override
    public String toString() {
      if (myPart == null) {
        return getName(myVariable);
      }
      else {
        return getName(myVariable) + "." + myPart.getName(); // NOI18N
      }
    }

    private Part myPart;
    private VariableDeclaration myVariable;
  }

  // -------------------------
  private class VariableInfo {
    VariableInfo(Variable variable) {
      myVariable = variable;
      myIsUsed = false;
      myIsInitialized = false;
    }

    public Variable getVariable() {
      return myVariable;
    }
    
    public void setUsed() {
      myIsUsed = true;
    }

    public boolean isUsed() {
      return myIsUsed;
    }

    public void setInitialized() {
      myIsInitialized = true;
    }

    public boolean isInitialized() {
      return myIsInitialized;
    }

    @Override
    public String toString() {
      return myVariable.getName() + "\t " + myIsInitialized + "\t " + myIsUsed; // NOI18N
    }

    private boolean myIsUsed;
    private boolean myIsInitialized;
    private Variable myVariable;
  }
}
