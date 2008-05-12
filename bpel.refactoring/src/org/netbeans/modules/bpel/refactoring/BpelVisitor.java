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

import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Source;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.Target;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.Validate;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitorAdaptor;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.06.14
 */
final class BpelVisitor extends SimpleBpelModelVisitorAdaptor {

  BpelVisitor(List<Component> usage, Referenceable target) {
    if (target instanceof Named) {
      Named named = (Named) target;
      myXPath = new XPath(usage, named, named.getName());
    }
    myUsage = usage;
    myTarget = target;
  }
                      
  @Override
  public void visit(Import _import) {
//out();
//out("[IMPORT] location: " + _import.getLocation());
    if (ImportHelper.getWsdlModel(_import) == myTarget || ImportHelper.getSchemaModel(_import) == myTarget) {
      myUsage.add(_import);
    }
  }

  @Override
  public void visit(PartnerLink partnerLink) {
//out();
//out("[PARTNER LINK]: " + Util.getName(partnerLink));
    Util.visit(
      partnerLink.getPartnerLinkType(),
      myTarget,
      partnerLink,
      myUsage
    );
    Util.visit(
      partnerLink.getMyRole(),
      myTarget,
      partnerLink,
      myUsage
    );
    Util.visit(
      partnerLink.getPartnerRole(),
      myTarget,
      partnerLink,
      myUsage
    );
  }

  @Override
  public void visit(Validate validate) {
    List<BpelReference<VariableDeclaration>> variables = validate.getVaraibles();

    if (variables == null) {
      return;
    }
    for (BpelReference<VariableDeclaration> variable : variables) {
      Util.visit(
        variable,
        myTarget,
        validate,
        myUsage
      );
    }
  }

  @Override
  public void visit(StartCounterValue value) {
//out();
//out("[START COUNTER]: " + Util.getName(value));
    visitContentElement(value);
  }

  @Override
  public void visit(FinalCounterValue value) {
//out();
//out("[FINAL COUNTER]: " + Util.getName(value));
    visitContentElement(value);
  }

  @Override
  public void visit(Branches branches) {
//out();
//out("[BRANCHES]: " + Util.getName(branches));
    visitContentElement(branches);
  }
    
  @Override
  public void visit(BooleanExpr booleanExpression) {
//out();
//out("[BOOLEAN EXPRESSION]: " + Util.getName(booleanExpression));
    visitContentElement(booleanExpression);
  }

  @Override
  public void visit(RepeatEvery repeatEvery) {
//out();
//out("[REPEAT EVERY]: " + Util.getName(repeatEvery));
    visitContentElement(repeatEvery);
  }

  @Override
  public void visit(DeadlineExpression deadline) {
//out();
//out("[DEADLNE]: " + Util.getName(deadline));
    visitContentElement(deadline);
  }

  @Override
  public void visit(For _for) {
//out();
//out("[FOR]: " + Util.getName(_for));
    visitContentElement(_for);
  }

  @Override
  public void visit(CorrelationSet correlationSet) {
    List<WSDLReference<CorrelationProperty>> references = correlationSet.getProperties();

    if (references == null) {
      return;
    }
    for (WSDLReference<CorrelationProperty> reference : references) {
      Util.visit(
        reference,
        myTarget,
        correlationSet,
        myUsage
      );
    }
  }

  @Override
  public void visit(Correlation correlation) {
    Util.visit(
      correlation.getSet(),
      myTarget,
      correlation,
      myUsage
    );
  }

  @Override
  public void visit(Source source) {
    Util.visit(
      source.getLink(),
      myTarget,
      source,
      myUsage
    );
  }

  @Override
  public void visit(Target target) {
    Util.visit(
      target.getLink(),
      myTarget,
      target,
      myUsage
    );
  }

  @Override
  public void visit(From from) {
//out();
//out();
//out("[FROM]: " + Util.getName(from));
    Util.visit(
      from.getPart(),
      myTarget,
      from,
      myUsage
    );
    Util.visit(
      from.getProperty(),
      myTarget,
      from,
      myUsage
    );
    Util.visit(
      from.getVariable(),
      myTarget,
      from,
      myUsage
    );
    Util.visit(
      from.getPartnerLink(),
      myTarget,
      from,
      myUsage
    );
    visitContentElement(from);
  }

  @Override
  public void visit(FromPart fromPart) {
    Util.visit(
      fromPart.getToVariable(),
      myTarget,
      fromPart,
      myUsage
    );
  }

  @Override
  public void visit(To to) {
//out();
//out();
//out("[TO]: " + Util.getName(to));
    Util.visit(
      to.getPart(),
      myTarget,
      to,
      myUsage
    );
    Util.visit(
      to.getProperty(),
      myTarget,
      to,
      myUsage
    );
    Util.visit(
      to.getVariable(),
      myTarget,
      to,
      myUsage
    );
    Util.visit(
      to.getPartnerLink(),
      myTarget,
      to,
      myUsage
    );
    visitContentElement(to);
  }

  @Override
  public void visit(ToPart toPart) {
    Util.visit(
      toPart.getFromVariable(),
      myTarget,
      toPart,
      myUsage
    );
  }

  private void visitContentElement(BpelEntity entity) {
//out("[CONTENT]: " + ((ContentElement) entity).getContent());
    if (myXPath != null) {
      myXPath.visit(((ContentElement) entity).getContent(), entity);
    }
  }

  @Override
  public void visit(OnEvent event) {
//out();
//out("[EVENT]: " + Util.getName(event));
    Util.visit(
      event.getMessageType(),
      myTarget,
      event,
      myUsage
    );
    Util.visit(
      event.getPortType(),
      myTarget,
      event,
      myUsage
    );
    Util.visit(
      event.getOperation(),
      myTarget,
      event,
      myUsage
    );
    Util.visit(
      event.getMessageExchange(),
      myTarget,
      event,
      myUsage
    );
    Util.visit(
      event.getPartnerLink(),
      myTarget,
      event,
      myUsage
    );
  }

  @Override
  public void visit(OnMessage message) {
//out();
//out("[MESSAGE]: " + Util.getName(message));
    Util.visit(
      message.getPortType(),
      myTarget,
      message,
      myUsage
    );
    Util.visit(
      message.getOperation(),
      myTarget,
      message,
      myUsage
    );
    Util.visit(
      message.getMessageExchange(),
      myTarget,
      message,
      myUsage
    );
    Util.visit(
      message.getVariable(),
      myTarget,
      message,
      myUsage
    );
    Util.visit(
      message.getPartnerLink(),
      myTarget,
      message,
      myUsage
    );
  }

  @Override
  public void visit(Catch _catch) {
//out();
//out("[CATCH]: " + Util.getName(_catch));
    Util.visit(
      _catch.getFaultMessageType(),
      myTarget,
      _catch,
      myUsage
    );
    Util.visit(
      _catch.getFaultName(),
      myTarget,
      _catch,
      myUsage
    );
    Util.visit(
      _catch.getFaultElement(),
      myTarget,
      _catch,
      myUsage
    );
  }

  @Override
  public void visit(Reply reply) {
//out();
//out("[REPLY]: " + Util.getName(reply));
    Util.visit(
      reply.getFaultName(),
      myTarget,
      reply,
      myUsage
    );
    Util.visit(
      reply.getPortType(),
      myTarget,
      reply,
      myUsage
    );
    Util.visit(
      reply.getOperation(),
      myTarget,
      reply,
      myUsage
    );
    Util.visit(
      reply.getMessageExchange(),
      myTarget,
      reply,
      myUsage
    );
    Util.visit(
      reply.getVariable(),
      myTarget,
      reply,
      myUsage
    );
    Util.visit(
      reply.getPartnerLink(),
      myTarget,
      reply,
      myUsage
    );
  }

  @Override
  public void visit(Receive receive) {
//out();
//out("[RECEIVE]: " + Util.getName(receive));
    Util.visit(
      receive.getPortType(),
      myTarget,
      receive,
      myUsage
    );
    Util.visit(
      receive.getOperation(),
      myTarget,
      receive,
      myUsage
    );
    Util.visit(
      receive.getMessageExchange(),
      myTarget,
      receive,
      myUsage
    );
    Util.visit(
      receive.getVariable(),
      myTarget,
      receive,
      myUsage
    );
    Util.visit(
      receive.getPartnerLink(),
      myTarget,
      receive,
      myUsage
    );
  }

  @Override
  public void visit(Invoke invoke) {
//out();
//out("[INVOKE]: " + Util.getName(invoke));
    Util.visit(
      invoke.getPortType(),
      myTarget,
      invoke,
      myUsage
    );
    Util.visit(
      invoke.getOperation(),
      myTarget,
      invoke,
      myUsage
    );
    Util.visit(
      invoke.getInputVariable(),
      myTarget,
      invoke,
      myUsage
    );
    Util.visit(
      invoke.getOutputVariable(),
      myTarget,
      invoke,
      myUsage
    );
    Util.visit(
      invoke.getPartnerLink(),
      myTarget,
      invoke,
      myUsage
    );
    if (invoke.getCompensationHandler() == myTarget) {
      myUsage.add(invoke);
    }
  }

  @Override
  public void visit(Scope scope) {
    if (scope.getCompensationHandler() == myTarget) {
      myUsage.add(scope);
    }
  }

  @Override
  public void visit(CompensateScope scope) {
    Util.visit(
      scope.getTarget(),
      myTarget,
      scope,
      myUsage
    );
  }

  @Override
  public void visit(Throw _throw) {
//out();
//out("[THROW]: " + Util.getName(_throw));
    Util.visit(
      _throw.getFaultName(),
      myTarget,
      _throw,
      myUsage
    );
    Util.visit(
      _throw.getFaultVariable(),
      myTarget,
      _throw,
      myUsage
    );
  }

  @Override
  public void visit(Variable variable) {
//out();
//out("[VARIABLE]: " + Util.getName(variable));
    Util.visit(
      variable.getMessageType(),
      myTarget,
      variable,
      myUsage
    );
    Util.visit(
      variable.getType(),
      variable.getElement(),
      myTarget,
      variable,
      myUsage
    );
  }

  private XPath myXPath;
  private Referenceable myTarget;
  private List<Component> myUsage;
}
