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
package org.netbeans.modules.bpel.validation.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.netbeans.modules.bpel.model.api.BaseCorrelation;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationsHolder;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CreateInstanceActivity;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.Exit;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.api.support.ExpressionUpdater;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.bpel.validation.core.BpelValidator;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitorAdaptor;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
public final class Validator extends BpelValidator {

  @Override
  protected void init() {
    myErrored = new ArrayList<Component>();
  }

  @Override
  protected SimpleBpelModelVisitor getVisitor() { return new SimpleBpelModelVisitorAdaptor() {

  @Override
  public void visit(ForEach forEach) {
//out();
    // # 124918
    checkCounters(forEach);

    // # 125001
    checkNegativeCounter(forEach);

//out("forEach: " + forEach);
//out("forEach.getCounterName: " + forEach.getCounterName());
    String counter = forEach.getCounterName();

    if (counter == null) {
      return;
    }
    CompletionCondition completionCondition = forEach.getCompletionCondition();

    if (completionCondition == null) {
      return;
    }
    Branches branches = completionCondition.getBranches();

    if (branches == null) {
      return;
    }
    String expression = branches.getContent();

    if (expression == null) {
      return;
    }
    Collection<String> variables = ExpressionUpdater.getInstance().getUsedVariables(expression);

    if (variables == null) {
      return;
    }
//out("variables: " + variables);
//out();
    for (String variable : variables) {
      if (variable.equals(counter)) {
        addError("FIX_Branches_Cant_Use_Counter", branches, counter); // NOI18N
      }
    }
  }

  private void checkCounters(ForEach forEach) {
    StartCounterValue startCounterValue = forEach.getStartCounterValue();

    if (startCounterValue == null) {
      return;
    }
    int startCounter;

    try {
      startCounter = Integer.parseInt(startCounterValue.getContent());
    }
    catch (NumberFormatException e) {
      return;
    }
    FinalCounterValue finalCounterValue = forEach.getFinalCounterValue();

    if (finalCounterValue == null) {
      return;
    }
    int finalCounter;

    try {
      finalCounter = Integer.parseInt(finalCounterValue.getContent());
    }
    catch (NumberFormatException e) {
      return;
    }
    if (finalCounter < startCounter) {
      addError("FIX_Final_Start_Counters", forEach, "" + startCounter, "" + finalCounter); // NOI18N
    }
  }

  private void checkNegativeCounter(ForEach forEach) {
    // start
    StartCounterValue startCounterValue = forEach.getStartCounterValue();

    if (startCounterValue == null) {
      return;
    }
    int startCounter;

    try {
      startCounter = Integer.parseInt(startCounterValue.getContent());
    }
    catch (NumberFormatException e) {
      return;
    }
    if (startCounter < 0) {
      addError("FIX_Negative_Start_Counter", startCounterValue, "" + startCounter); // NOI18N
    }
    // final
    FinalCounterValue finalCounterValue = forEach.getFinalCounterValue();

    if (finalCounterValue == null) {
      return;
    }
    int finalCounter;

    try {
      finalCounter = Integer.parseInt(finalCounterValue.getContent());
    }
    catch (NumberFormatException e) {
      return;
    }
    if (finalCounter < 0) {
      addError("FIX_Negative_Final_Counter", finalCounterValue, "" + finalCounter); // NOI18N
    }
    // completion
    CompletionCondition completionCondition = forEach.getCompletionCondition();

    if (completionCondition == null) {
      return;
    }
    Branches branches = completionCondition.getBranches();

    if (branches == null) {
      return;
    }
    int completionCounter;

    try {
      completionCounter = Integer.parseInt(branches.getContent());
    }
    catch (NumberFormatException e) {
      return;
    }
    if (completionCounter < 0) {
      addError("FIX_Negative_Completion_Counter", branches, "" + completionCounter); // NOI18N
    }
  }

  @Override
  public void visit(EventHandlers handlers) {
//out();
//out("HANDLERS: " + handlers);
//out();
    // # 112489
    checkCase1(handlers);
    checkCase3(handlers);
    checkCase245(handlers);
  }

  private void checkCase3(EventHandlers handlers) {
    OnAlarmEvent [] onAlarms = handlers.getOnAlarms();

    if (onAlarms == null) {
      return;
    }
    for (OnAlarmEvent onAlarm : onAlarms) {
      if (onAlarm.getRepeatEvery() == null) {
        continue;
      }
      List<Receive> receives = getReceives(onAlarm);

      for (Receive receive : receives) {
        addWarning("FIX_Receive_in_OnAlarm", receive, receive.getName()); // NOI18N
      }
    }
  }

  private void checkCase245(EventHandlers handlers) {
    List<Receive> receives = new ArrayList<Receive>();
    
    receives.addAll(getReceives(handlers.getOnEvents()));
    receives.addAll(getReceives(handlers.getOnAlarms()));

    checkDuplicate(receives);
  }

  private List<Receive> getReceives(OnEvent [] onEvents) {
    List<Receive> receives = new ArrayList<Receive>();

    if (onEvents == null) {
      return receives;
    }
    for (OnEvent onEvent : onEvents) {
      receives.addAll(getReceives(onEvent));
    }
    return receives;
  }

  private void checkDuplicate(List<Receive> receives) {
    for (int i=0; i < receives.size(); i++) {
      for (int j=i+1; j < receives.size(); j++) {
        checkDuplicate(receives.get(i), receives.get(j));
      }
    }
  }
  
  private void checkDuplicate(Receive receive1, Receive receive2) {
    if (
      samePartnerLink(receive1, receive2) &&
      samePortType(receive1, receive2) &&
      sameOperation(receive1, receive2)
    ) {
      addWarning("FIX_Receives_in_OnEventOnAlarm", receive1, receive1.getName(), receive2.getName()); // NOI18N
      addWarning("FIX_Receives_in_OnEventOnAlarm", receive2, receive2.getName(), receive1.getName()); // NOI18N
    }
  }

  private boolean sameOperation(Receive receive1, Receive receive2) {
    WSDLReference<Operation> reference1 = receive1.getOperation();

    if (reference1 == null) {
      return false;
    }
    WSDLReference<Operation> reference2 = receive2.getOperation();

    if (reference2 == null) {
      return false;
    }
    return same(reference1.get(), reference2.get());
  }

  private boolean samePortType(Receive receive1, Receive receive2) {
    WSDLReference<PortType> reference1 = receive1.getPortType();

    if (reference1 == null) {
      return false;
    }
    WSDLReference<PortType> reference2 = receive2.getPortType();

    if (reference2 == null) {
      return false;
    }
    return same(reference1.get(), reference2.get());
  }

  private boolean samePartnerLink(Receive receive1, Receive receive2) {
    BpelReference<PartnerLink> reference1 = receive1.getPartnerLink();

    if (reference1 == null) {
      return false;
    }
    BpelReference<PartnerLink> reference2 = receive2.getPartnerLink();

    if (reference2 == null) {
      return false;
    }
    return same(reference1.get(), reference2.get());
  }

  private boolean same(Object object1, Object object2) {
    if (object1 == null) {
      return false;
    }
    return object1.equals(object2);
  }

  private List<Receive> getReceives(OnAlarmEvent [] onAlarms) {
    List<Receive> receives = new ArrayList<Receive>();

    if (onAlarms == null) {
      return receives;
    }
    for (OnAlarmEvent onAlarm : onAlarms) {
      if (onAlarm.getRepeatEvery() == null) {
        receives.addAll(getReceives(onAlarm));
      }
    }
    return receives;
  }

  private void checkCase1(EventHandlers handlers) {
    OnEvent [] onEvents = handlers.getOnEvents();

    if (onEvents == null) {
      return;
    }
    for (OnEvent onEvent : onEvents) {
      List<Receive> receives = getReceives(onEvent);

      for (Receive receive : receives) {
        addWarning("FIX_Receive_in_OnEvent", receive, receive.getName()); // NOI18N
      }
    }
  }

  private List<Receive> getReceives(BpelEntity entity) {
    List<Receive> receives = new ArrayList<Receive>();
    collectReceives(entity, receives);
    return receives;
  }

  private void collectReceives(BpelEntity entity, List<Receive> receives) {
    if (entity instanceof Receive) {
      receives.add((Receive) entity);
    }
    List<BpelEntity> children = entity.getChildren();

    for (BpelEntity child : children) {
      collectReceives(child, receives);
    }
  }

  // # 93078
  @Override
  public void visit(Branches branches) {
    String content = branches.getContent();

    if (content == null) {
      return;
    }
    content = content.toLowerCase();

    if (
      content.contains("true") || content.contains("false")) { // NOI18N
      addError("FIX_Branches_Must_Be_Integer", branches); // NOI18N
    }
  }

  @Override
  public void visit(Process process) {
    List<Reply> replies = new ArrayList<Reply>();
    List<CorrelationsHolder> holders = new ArrayList<CorrelationsHolder>();
    visitEntities(process.getChildren(), replies, holders);
    // # 81404
    checkReplies(replies);
    // # 109412
    checkHolders(holders);
    // # 129266
    checkExit(process);
  }

  private void checkExit(BpelEntity entity) {
    List<BpelEntity> children = entity.getChildren();
    boolean hasExit = false;

    if ( !(entity instanceof Flow)) {
      for (BpelEntity child : children) {
        if (hasExit) {
          addError("FIX_Activity_after_Exit", child); // NOI18N
        }
        if (child instanceof Exit) {
          hasExit = true;
        }
      }
    }
    for (BpelEntity child : children) {
      checkExit(child);
    }
  }

  private void visitEntities(List<BpelEntity> entities, List<Reply> replies, List<CorrelationsHolder> holders) {
    for (BpelEntity entity : entities) {
      if (entity instanceof Reply) {
        replies.add((Reply) entity);
      }
      else if (entity instanceof CorrelationsHolder) {
        holders.add((CorrelationsHolder) entity);
      }
      visitEntities(entity.getChildren(), replies, holders);
    }
  }

  private void checkReplies(List<Reply> replies) {
//out();
//out();
    for (int i=0; i < replies.size(); i++) {
      Reply reply1 = replies.get(i);

      for (int j=i+1; j < replies.size(); j++) {
        checkReplies(reply1, replies.get(j));
      }
    }
//out();
//out();
  }

  private void checkHolders(List<CorrelationsHolder> holders) {
    for (CorrelationsHolder holder : holders) {
      checkInitiateAndUse(holder);
    }
//out();
//out();
    for (int i=0; i < holders.size(); i++) {
      CorrelationsHolder holder1 = holders.get(i);

      for (int j=i+1; j < holders.size(); j++) {
        checkHolders(holder1, holders.get(j));
      }
    }
//out();
//out();
  }

  // # 120390
  private void checkInitiateAndUse(CorrelationsHolder holder) {
    CorrelationContainer container = holder.getCorrelationContainer();

    if (container == null) {
      return;
    }
    Correlation [] correlations = container.getCorrelations();

    if (correlations == null) {
      return;
    }
    Process process = holder.getBpelModel().getProcess();
//out();
//out("SEE: " + getName(holder));
    for (Correlation correlation : correlations) {
      Initiate initiate = correlation.getInitiate();

      if (initiate != Initiate.NO) {
        continue;
      }
      BpelReference<CorrelationSet> ref = correlation.getSet();

      if (ref == null) {
        continue;
      }
      CorrelationSet set = ref.get();

      if (set == null) {
        continue;
      }
//out("check: " + getName(correlation));
      if ( !checkCorrelationSet(set, holder, process)) {
        addError("FIX_Not_Instantiated_Correlation_Set", correlation, set.getName()); // NOI18N
      }
    }
  }

  private boolean checkCorrelationSet(CorrelationSet set, CorrelationsHolder holder, BpelEntity entity) {
    List<BpelEntity> children = entity.getChildren();

    for (BpelEntity child : children) {
      if (checkCorrelationSet(set, holder, child)) {
        return true;
      }
    }
    if (holder == entity) {
      return false;
    }
    if (checkCorrelationSetInInvoke(set, entity)) {
      return true;
    }
    if ( !(entity instanceof CorrelationsHolder)) {
      return false;
    }
    CorrelationsHolder current = (CorrelationsHolder) entity;
    CorrelationContainer container = current.getCorrelationContainer();

    if (container == null) {
      return false;
    }
    Correlation [] correlations = container.getCorrelations();

    if (correlations == null) {
      return false;
    }
    for (Correlation correlation : correlations) {
      if (theSame(set, correlation)) {
//out("    view: " + getName(corr));
        return true;
//out("    FOUND");
      }
    }
    return false;
  }

  private boolean checkCorrelationSetInInvoke(CorrelationSet set, BpelEntity entity) {
    if ( !(entity instanceof Invoke)) {
      return false;
    }
    Invoke invoke = (Invoke) entity;
    PatternedCorrelationContainer container = invoke.getPatternedCorrelationContainer();

    if (container == null) {
      return false;
    }
    PatternedCorrelation [] correlations = container.getPatternedCorrelations();

    if (correlations == null) {
      return false;
    }
    for (PatternedCorrelation correlation : correlations) {
      if (theSame(set, correlation)) {
//out("    view: " + getName(corr));
        return true;
//out("    FOUND");
      }
    }
    return false;
  }

  private boolean theSame(CorrelationSet set, BaseCorrelation correlation) {
    BpelReference<CorrelationSet> ref = correlation.getSet();

    if (ref == null) {
      return false;
    }
    CorrelationSet corr = ref.get();

    if (corr == null) {
      return false;
    }
//out("    view: " + getName(corr));
    return corr == set && correlation.getInitiate() != Initiate.NO;
  }

  private void checkReplies(Reply reply1, Reply reply2) {
//out();
//out("reply1: " + reply1.getName());
//out("reply2: " + reply2.getName());

    if ( !isInGate(reply1) && !isInGate(reply2)) {
      if (haveTheSamePartnerLinkAndOperation(reply1, reply2)) {
        if ( !hasNextExit(reply1) && !hasNextExit(reply2)) {
          addErrorCheck("FIX_Replies_PartnerLink_Gate", reply1, reply1.getName(), reply2.getName()); // NOI18N
          addErrorCheck("FIX_Replies_PartnerLink_Gate", reply2, reply2.getName(), reply1.getName()); // NOI18N
          return;
        }
      }
    }
    if (getParent(reply1) == getParent(reply2)) {
      if (haveTheSamePartnerLinkAndOperation(reply1, reply2)) {
        addErrorCheck("FIX_Replies_PartnerLink_Scope", reply1, reply1.getName(), reply2.getName()); // NOI18N
        addErrorCheck("FIX_Replies_PartnerLink_Scope", reply2, reply2.getName(), reply1.getName()); // NOI18N
        return;
      }
    }
  }

  private boolean hasNextExit(BpelEntity entity) {
    if (entity == null) {
      return false;
    }
    BpelEntity parent = entity.getParent();

    if (parent == null) {
      return false;
    }
    List<BpelEntity> children = parent.getChildren();
    boolean findExit = false;

    for (BpelEntity child : children) {
      if (findExit) {
        if (child instanceof Exit) {
          return true;
        }
      }
      if (child == entity) {
        findExit = true;
      }
    }
    return false;
  }

  private void checkHolders(CorrelationsHolder holder1, CorrelationsHolder holder2) {
//out();
//out("holder1: " + holder1);
//out("holder2: " + holder2);
    BpelEntity parent1 = getParent(holder1);
    BpelEntity parent2 = getParent(holder2);

    if ( !isInGate(holder1) && !isInGate(holder2)) {
      if (haveTheSameCorrelationWithInitiateYes(holder1, holder2, parent1, parent2)) {
        addErrorCheck("FIX_Holder_Correlation_Gate", holder1, getName(holder1), getName(holder2)); // NOI18N
        addErrorCheck("FIX_Holder_Correlation_Gate", holder2, getName(holder2), getName(holder1)); // NOI18N
        return;
      }
    }
    if (parent1 == parent2) {
      if (haveTheSameCorrelationWithInitiateYes(holder1, holder2, parent1, parent2)) {
        addErrorCheck("FIX_Holder_Correlation_Scope", holder1, getName(holder1), getName(holder2)); // NOI18N
        addErrorCheck("FIX_Holder_Correlation_Scope", holder2, getName(holder2), getName(holder1)); // NOI18N
        return;
      }
    }
  }

  private boolean haveTheSameCorrelationWithInitiateYes(
    CorrelationsHolder holder1,
    CorrelationsHolder holder2,
    BpelEntity parent1,
    BpelEntity parent2
  ) {
    // # 128357
    if (holder1 instanceof OnMessage && parent1 instanceof Pick) {
      return false;
    }
    if (holder2 instanceof OnMessage && parent2 instanceof Pick) {
      return false;
    }
    CorrelationContainer container1 = holder1.getCorrelationContainer();
//out("  1");

    if (container1 == null) {
      return false;
    }
//out("  2");
    Correlation[] correlations1 = container1.getCorrelations();

    if (correlations1 == null) {
      return false;
    }
//out("  3");
    CorrelationContainer container2 = holder2.getCorrelationContainer();

    if (container2 == null) {
      return false;
    }
//out("  4");
    Correlation[] correlations2 = container2.getCorrelations();

    if (correlations2 == null) {
      return false;
    }
//out("  5");
    return checkCorrelations(correlations1, correlations2);
  }

  private boolean checkCorrelations(Correlation [] correlations1, Correlation [] correlations2) {
//out(" ");
    for (Correlation correlation : correlations1) {
//out("SEE: " + correlation + " " + correlation.getInitiate());
      if (correlation.getInitiate() != Initiate.YES) {
        continue;
      }
      if (checkCorrelation(correlations2, correlation)) {
        return true;
      }
    }
    return false;
  }

  private boolean checkCorrelation(Correlation [] correlations, Correlation correlation) {
//out("for: " + correlation);
    for (Correlation next : correlations) {
//out("  see: " + next + " " + next.getInitiate());
      if (theSame(next, correlation)) {
        return next.getInitiate() == Initiate.YES;
      }
    }
    return false;
  }

  private boolean theSame(Correlation correlation1, Correlation correlation2) {
    BpelReference<CorrelationSet> ref1 = correlation1.getSet();

    if (ref1 == null) {
      return false;
    }
    BpelReference<CorrelationSet> ref2 = correlation2.getSet();

    if (ref2 == null) {
      return false;
    }
    return ref1.get() == ref2.get();
  }

  private boolean haveTheSamePartnerLinkAndOperation(Reply reply1, Reply reply2) {
    if (reply1.getPartnerLink() == null) {
//out("  reply1 has PL ref null");
      return false;
    }
    PartnerLink partnerLink1 = reply1.getPartnerLink().get();

    if (partnerLink1 == null) {
//out("  reply1 has PL null");
      return false;
    }
    if (reply2.getPartnerLink() == null) {
//out("  reply2 has PL ref null");
      return false;
    }
    PartnerLink partnerLink2 = reply2.getPartnerLink().get();

    if (partnerLink2 == null) {
//out("  reply2 has PL null");
      return false;
    }
    if (partnerLink1 != partnerLink2) {
      return false;
    }
    // operation
    if (reply1.getOperation() == null) {
      return false;
    }
    Operation operation1 = reply1.getOperation().get();

    if (operation1 == null) {
      return false;
    }
    if (reply2.getOperation() == null) {
      return false;
    }
    Operation operation2 = reply2.getOperation().get();

    if (operation2 == null) {
      return false;
    }
    return operation1 == operation2;
  }

  private BpelEntity getParent(BpelEntity entity) {
    BpelEntity parent = entity.getParent();
    
    while (true) {
      if (parent instanceof Sequence) {
        parent = parent.getParent();
        continue;
      }
      break;
    }
    return parent;
  }

  private boolean isInGate(BpelEntity entity) {
//out();
//out("is in gate ...");
    BpelEntity parent = entity.getParent();
//out("  entity: " + entity);
//out("  parent: " + parent);

    while (true) {
//out("  parent: " + parent);
      if (parent == null) {
        break;
      }
      if (parent instanceof If) {
        return true;
      }
      if (parent instanceof Else) {
        return true;
      }
      if (parent instanceof ElseIf) {
        return true;
      }
      if (parent instanceof FaultHandlers) {
        return true;
      }
      if (parent instanceof Flow) {
        return true;
      }
      if (parent instanceof OnMessage) {
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }
  
  // # 90125
  @Override
  public void visit(CorrelationContainer container) {
//out();
//out("see container: " + container + " " + container.getParent());
//out();
    Component parent = container.getParent();

    if ( !(parent instanceof CreateInstanceActivity)) {
      return;
    }
    CreateInstanceActivity activity = (CreateInstanceActivity) parent;

    if ( !isCreateInstanceYes(activity)) {
      return;
    }
    Correlation [] correlations = container.getCorrelations();

    if (correlations == null) {
      return;
    }
    for (Correlation correlation : correlations) {
      Initiate initiate = correlation.getInitiate();

      if (initiate != Initiate.NO) {
        return;
      }
    }
    addError("FIX_Activity_with_Correlation", parent); // NOI18N
  }
  
  // # 129986
  @Override
  public void visit(Receive receive) {
//out();
//out("RECEIVE: " + receive);
    WSDLReference<PortType> ref = receive.getPortType();

    if (ref == null) {
      return;
    }
    PortType portType = ref.get();

    if (portType == null) {
      return;
    }
    Collection<Operation> operations = portType.getOperations();

    if (operations.size() != 1) {
      return;
    }
    Operation operation = operations.iterator().next();

    if (operation == null) {
      return;
    }
    if (operation.getInput() == null || operation.getOutput() == null) {
      return;
    }
    if ( !findReply(receive.getBpelModel().getProcess(), portType)) {
      addError("FIX_In_Out_Receive_Reply", receive, receive.getName()); // NOI18N
    }
  }

  private boolean findReply(BpelEntity entity, PortType portType) {
    if (entity instanceof Reply) {
      WSDLReference<PortType> ref = ((Reply) entity).getPortType();

      if (ref != null && portType == ref.get()) {
        return true;
      }
    }
    List<BpelEntity> children = entity.getChildren();

    for (BpelEntity child : children) {
      if (findReply(child, portType)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void visit(Reply reply) {
      super.visit(reply);
      WSDLReference<Operation> opRef = reply.getOperation();
      
      if (opRef == null) {
          return;
      }
      Operation operation = opRef.get();

      if (operation == null) {
          return;
      }
      if ( !(operation instanceof RequestResponseOperation)) {
          addError("FIX_ReplyOperation", reply, opRef.getQName().toString()); // NOI18N
      }
  }

  // # 111409
  @Override
  public void visit(Throw _throw) {
    if (_throw.getParent() instanceof TerminationHandler) {
      addError("FIX_Throw_in_TerminationHandler", _throw, _throw.getName()); // NOI18N
    }
  }

  private void addErrorCheck(String key, Component component, String name1, String name2) {
    if (myErrored.contains(component)) {
      return;
    }
    myErrored.add(component);
    addError(key, component, name1, name2);
  }
  
  };}

  private List<Component> myErrored;
}
