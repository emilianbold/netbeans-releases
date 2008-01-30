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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;

import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationsHolder;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.impl.services.ExpressionUpdater;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.bpel.validation.util.ResultItem;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
public final class Validator extends org.netbeans.modules.bpel.validation.util.Validator {

    @Override
    public void visit(ForEach forEach) {
//out();
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
          addWarning("FIX_Receive_in_OnAlarm", receive); // NOI18N
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
        sameOperation(receive1, receive2))
      {
        addWarning("FIX_Receives_in_OnEventOnAlarm", receive1); // NOI18N
        addWarning("FIX_Receives_in_OnEventOnAlarm", receive2); // NOI18N
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
          addWarning("FIX_Receive_in_OnEvent", receive); // NOI18N
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

    // vlv # 93078
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

    // vlv # 81404
    @Override
    public void visit(Process process) {
      List<Reply> replies = new ArrayList<Reply>();
      List<CorrelationsHolder> holders = new ArrayList<CorrelationsHolder>();
      visitEntities(process.getChildren(), replies, holders);
      checkReplies(replies);
      // # 109412
      checkHolders(holders);
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

    private void checkReplies(Reply reply1, Reply reply2) {
//out();
//out("reply1: " + reply1.getName());
//out("reply2: " + reply2.getName());
      if ( !isInGate(reply1) && !isInGate(reply2)) {
        if (haveTheSamePartnerLink(reply1, reply2)) {
          addError("FIX_Replies_PartnerLink", reply1); // NOI18N
          addError("FIX_Replies_PartnerLink", reply2); // NOI18N
          return;
        }
      }
      if (getParent(reply1) == getParent(reply2)) {
        if (haveTheSamePartnerLink(reply1, reply2)) {
          addError("FIX_Replies_PartnerLink", reply1); // NOI18N
          addError("FIX_Replies_PartnerLink", reply2); // NOI18N
          return;
        }
      }
    }

    private void checkHolders(CorrelationsHolder holder1, CorrelationsHolder holder2) {
//out();
//out("holder1: " + holder1);
//out("holder2: " + holder2);
      if ( !isInGate(holder1) && !isInGate(holder2)) {
        if (haveTheSameCorrelationWithInitiateYes(holder1, holder2)) {
          addError("FIX_Holder_Correlation", holder1); // NOI18N
          addError("FIX_Holder_Correlation", holder2); // NOI18N
          return;
        }
      }
      if (getParent(holder1) == getParent(holder2)) {
        if (haveTheSameCorrelationWithInitiateYes(holder1, holder2)) {
          addError("FIX_Holder_Correlation", holder1); // NOI18N
          addError("FIX_Holder_Correlation", holder2); // NOI18N
          return;
        }
      }
    }

    private boolean haveTheSameCorrelationWithInitiateYes(CorrelationsHolder holder1, CorrelationsHolder holder2) {
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

    private boolean haveTheSamePartnerLink(Reply reply1, Reply reply2) {
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
      return partnerLink1 == partnerLink2;
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
//out("  isInIfElse...");
      BpelEntity parent = entity.getParent();

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
    
    @Override
    public void visit(Reply reply)
    {
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

    private void addError(String bundleKey, Collection<Component> collection, Object... values) {
        String str = i18n(getClass(), bundleKey);

        if (values != null && values.length > 0) {
            str = MessageFormat.format(str, values);
        }
        for(Component component: collection) {
            ResultItem resultItem = new ResultItem(this, ResultType.ERROR, component, str);
            getResultItems().add(resultItem);
        }
    }

    @Override
    public void visit(Import imp) {
        Model model = getModel(imp);

        if (model == null) {
            addInvalidImportModelError(imp);
            return;
        }
        validate(model);
    }

    private Model getModel(Import imp) {
        Model model = ImportHelper.getWsdlModel(imp, false);

        if (model != null) {
            return model;
        }
        return ImportHelper.getSchemaModel(imp, false);
    }

    private void addInvalidImportModelError(BpelEntity bpelEntity) {
      getResultItems().add(new ResultItem(this, ResultType.WARNING, bpelEntity, i18n(getClass(), "FIX_NotWellFormedImport"))); // NOI18N
    }
}
