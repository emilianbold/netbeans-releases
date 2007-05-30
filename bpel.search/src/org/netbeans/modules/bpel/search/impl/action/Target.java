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
package org.netbeans.modules.bpel.search.impl.action;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Component;

import org.netbeans.modules.xml.xam.ui.search.api.SearchTarget;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.29
 */
final class Target implements SearchTarget {

  static final SearchTarget [] BPEL = new SearchTarget [] {
    new Target(Component.class),
    new Target(org.netbeans.modules.bpel.model.api.Assign.class),
    new Target(org.netbeans.modules.bpel.model.api.Branches.class),
    new Target(org.netbeans.modules.bpel.model.api.Catch.class),
    new Target(org.netbeans.modules.bpel.model.api.CatchAll.class),
    new Target(org.netbeans.modules.bpel.model.api.Compensate.class),
    new Target(org.netbeans.modules.bpel.model.api.CompensationHandler.class),
    new Target(org.netbeans.modules.bpel.model.api.Condition.class),
    new Target(org.netbeans.modules.bpel.model.api.Copy.class),
    new Target(org.netbeans.modules.bpel.model.api.Correlation.class),
    new Target(org.netbeans.modules.bpel.model.api.CorrelationContainer.class),
    new Target(org.netbeans.modules.bpel.model.api.CorrelationSet.class),
    new Target(org.netbeans.modules.bpel.model.api.CorrelationSetContainer.class),
    new Target(org.netbeans.modules.bpel.model.api.DeadlineExpression.class),
    new Target(org.netbeans.modules.bpel.model.api.Documentation.class),
    new Target(org.netbeans.modules.bpel.model.api.DurationExpression.class),
    new Target(org.netbeans.modules.bpel.model.api.Else.class),
    new Target(org.netbeans.modules.bpel.model.api.ElseIf.class),
    new Target(org.netbeans.modules.bpel.model.api.Empty.class),
    new Target(org.netbeans.modules.bpel.model.api.EventHandlers.class),
    new Target(org.netbeans.modules.bpel.model.api.Exit.class),
    new Target(org.netbeans.modules.bpel.model.api.FaultHandlers.class),
    new Target(org.netbeans.modules.bpel.model.api.Flow.class),
    new Target(org.netbeans.modules.bpel.model.api.For.class),
    new Target(org.netbeans.modules.bpel.model.api.ForEach.class),
    new Target(org.netbeans.modules.bpel.model.api.From.class),
    new Target(org.netbeans.modules.bpel.model.api.FromPart.class),
    new Target(org.netbeans.modules.bpel.model.api.If.class),
    new Target(org.netbeans.modules.bpel.model.api.Import.class),
    new Target(org.netbeans.modules.bpel.model.api.Invoke.class),
    new Target(org.netbeans.modules.bpel.model.api.Link.class),
    new Target(org.netbeans.modules.bpel.model.api.LinkContainer.class),
    new Target(org.netbeans.modules.bpel.model.api.Literal.class),
    new Target(org.netbeans.modules.bpel.model.api.MessageExchange.class),
    new Target(org.netbeans.modules.bpel.model.api.MessageExchangeContainer.class),
    new Target(org.netbeans.modules.bpel.model.api.OnAlarmEvent.class),
    new Target(org.netbeans.modules.bpel.model.api.OnAlarmPick.class),
    new Target(org.netbeans.modules.bpel.model.api.OnEvent.class),
    new Target(org.netbeans.modules.bpel.model.api.OnMessage.class),
    new Target(org.netbeans.modules.bpel.model.api.PartnerLink.class),
    new Target(org.netbeans.modules.bpel.model.api.PartnerLinkContainer.class),
    new Target(org.netbeans.modules.bpel.model.api.PatternedCorrelation.class),
    new Target(org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer.class),
    new Target(org.netbeans.modules.bpel.model.api.Pick.class),
    new Target(org.netbeans.modules.bpel.model.api.Process.class),
    new Target(org.netbeans.modules.bpel.model.api.Receive.class),
    new Target(org.netbeans.modules.bpel.model.api.RepeatUntil.class),
    new Target(org.netbeans.modules.bpel.model.api.Reply.class),
    new Target(org.netbeans.modules.bpel.model.api.ReThrow.class),
    new Target(org.netbeans.modules.bpel.model.api.Scope.class),
    new Target(org.netbeans.modules.bpel.model.api.Sequence.class),
    new Target(org.netbeans.modules.bpel.model.api.Source.class),
    new Target(org.netbeans.modules.bpel.model.api.Target.class),
    new Target(org.netbeans.modules.bpel.model.api.TerminationHandler.class),
    new Target(org.netbeans.modules.bpel.model.api.Throw.class),
    new Target(org.netbeans.modules.bpel.model.api.TimeEvent.class),
    new Target(org.netbeans.modules.bpel.model.api.To.class),
    new Target(org.netbeans.modules.bpel.model.api.ToPart.class),
    new Target(org.netbeans.modules.bpel.model.api.Validate.class),
    new Target(org.netbeans.modules.bpel.model.api.Variable.class),
    new Target(org.netbeans.modules.bpel.model.api.VariableContainer.class),
    new Target(org.netbeans.modules.bpel.model.api.VariableDeclaration.class),
    new Target(org.netbeans.modules.bpel.model.api.Wait.class),
    new Target(org.netbeans.modules.bpel.model.api.While.class),
  };
  
  static final SearchTarget [] WSDL = new SearchTarget [] {
    new Target(Component.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Binding.class),
    new Target(org.netbeans.modules.xml.wsdl.model.BindingFault.class),
    new Target(org.netbeans.modules.xml.wsdl.model.BindingInput.class),
    new Target(org.netbeans.modules.xml.wsdl.model.BindingOperation.class),
    new Target(org.netbeans.modules.xml.wsdl.model.BindingOutput.class),
    new Target(org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Definitions.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Documentation.class),
    new Target(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Fault.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Import.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Input.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Message.class),
    new Target(org.netbeans.modules.xml.wsdl.model.NotificationOperation.class),
    new Target(org.netbeans.modules.xml.wsdl.model.OneWayOperation.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Operation.class),
    new Target(org.netbeans.modules.xml.wsdl.model.OperationParameter.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Output.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Part.class),
    new Target(org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Port.class),
    new Target(org.netbeans.modules.xml.wsdl.model.PortType.class),
    new Target(org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias.class),
    new Target(org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query.class),
    new Target(org.netbeans.modules.xml.wsdl.model.RequestResponseOperation.class),
    new Target(org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Service.class),
    new Target(org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation.class),
    new Target(org.netbeans.modules.xml.wsdl.model.Types.class),
  };

  // todo a
  static final SearchTarget [] SCHEMA = new SearchTarget [] {
    new Target(Component.class),
  };

  private Target(Class<? extends Component> clazz) {
    myClazz = clazz;
  }

  public Class<? extends Component> getClazz() {
    return myClazz;
  }

  /**{@inheritDoc}*/
  public String toString() {
    return i18n(Target.class, name());
  }

  private String name() {
    String name = myClazz.getName();

    int k = name.lastIndexOf("."); // NOI18N

    if (k == -1) {
      return name;
    }
    return name.substring(k + 1);
  }

  private Class<? extends Component> myClazz;
}
