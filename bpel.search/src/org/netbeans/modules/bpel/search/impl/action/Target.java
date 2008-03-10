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
package org.netbeans.modules.bpel.search.impl.action;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.bpel.search.api.SearchTarget;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.29
 */
final class Target implements SearchTarget {

  static final SearchTarget [] BPEL = new SearchTarget [] {
    new Target(org.netbeans.modules.bpel.model.api.BpelEntity.class),
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
    new Target(org.netbeans.modules.xml.wsdl.model.WSDLComponent.class),
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

  static final SearchTarget [] SCHEMA = new SearchTarget [] {
    new Target(org.netbeans.modules.xml.schema.model.SchemaComponent.class),
    new Target(org.netbeans.modules.xml.schema.model.Annotation.class),
    new Target(org.netbeans.modules.xml.schema.model.AppInfo.class),
    new Target(org.netbeans.modules.xml.schema.model.Attribute.class),
    new Target(org.netbeans.modules.xml.schema.model.BoundaryFacet.class),
    new Target(org.netbeans.modules.xml.schema.model.Choice.class),
    new Target(org.netbeans.modules.xml.schema.model.ComplexContent.class),
    new Target(org.netbeans.modules.xml.schema.model.ComplexContentDefinition.class),
    new Target(org.netbeans.modules.xml.schema.model.ComplexContentRestriction.class),
    new Target(org.netbeans.modules.xml.schema.model.ComplexExtension.class),
    new Target(org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition.class),
    new Target(org.netbeans.modules.xml.schema.model.ComplexType.class),
    new Target(org.netbeans.modules.xml.schema.model.ComplexTypeDefinition.class),
    new Target(org.netbeans.modules.xml.schema.model.Constraint.class),
    new Target(org.netbeans.modules.xml.schema.model.Documentation.class),
    new Target(org.netbeans.modules.xml.schema.model.Element.class),
    new Target(org.netbeans.modules.xml.schema.model.Enumeration.class),
    new Target(org.netbeans.modules.xml.schema.model.Extension.class),
    new Target(org.netbeans.modules.xml.schema.model.Field.class),
    new Target(org.netbeans.modules.xml.schema.model.FractionDigits.class),
    new Target(org.netbeans.modules.xml.schema.model.GlobalAttribute.class),
    new Target(org.netbeans.modules.xml.schema.model.GlobalAttributeGroup.class),
    new Target(org.netbeans.modules.xml.schema.model.GlobalComplexType.class),
    new Target(org.netbeans.modules.xml.schema.model.GlobalElement.class),
    new Target(org.netbeans.modules.xml.schema.model.GlobalGroup.class),
    new Target(org.netbeans.modules.xml.schema.model.GlobalSimpleType.class),
    new Target(org.netbeans.modules.xml.schema.model.GlobalType.class),
    new Target(org.netbeans.modules.xml.schema.model.Import.class),
    new Target(org.netbeans.modules.xml.schema.model.Include.class),
    new Target(org.netbeans.modules.xml.schema.model.Key.class),
    new Target(org.netbeans.modules.xml.schema.model.Length.class),
    new Target(org.netbeans.modules.xml.schema.model.LengthFacet.class),
    new Target(org.netbeans.modules.xml.schema.model.List.class),
    new Target(org.netbeans.modules.xml.schema.model.LocalAttribute.class),
    new Target(org.netbeans.modules.xml.schema.model.LocalAttributeContainer.class),
    new Target(org.netbeans.modules.xml.schema.model.LocalComplexType.class),
    new Target(org.netbeans.modules.xml.schema.model.LocalElement.class),
    new Target(org.netbeans.modules.xml.schema.model.LocalGroupDefinition.class),
    new Target(org.netbeans.modules.xml.schema.model.LocalSimpleType.class),
    new Target(org.netbeans.modules.xml.schema.model.LocalType.class),
    new Target(org.netbeans.modules.xml.schema.model.MaxExclusive.class),
    new Target(org.netbeans.modules.xml.schema.model.MaxInclusive.class),
    new Target(org.netbeans.modules.xml.schema.model.MaxLength.class),
    new Target(org.netbeans.modules.xml.schema.model.MinExclusive.class),
    new Target(org.netbeans.modules.xml.schema.model.MinInclusive.class),
    new Target(org.netbeans.modules.xml.schema.model.MinLength.class),
    new Target(org.netbeans.modules.xml.schema.model.Notation.class),
    new Target(org.netbeans.modules.xml.schema.model.Pattern.class),
    new Target(org.netbeans.modules.xml.schema.model.Redefine.class),
    new Target(org.netbeans.modules.xml.schema.model.Schema.class),
    new Target(org.netbeans.modules.xml.schema.model.Selector.class),
    new Target(org.netbeans.modules.xml.schema.model.Sequence.class),
    new Target(org.netbeans.modules.xml.schema.model.SequenceDefinition.class),
    new Target(org.netbeans.modules.xml.schema.model.SimpleContent.class),
    new Target(org.netbeans.modules.xml.schema.model.SimpleContentDefinition.class),
    new Target(org.netbeans.modules.xml.schema.model.SimpleContentRestriction.class),
    new Target(org.netbeans.modules.xml.schema.model.SimpleExtension.class),
    new Target(org.netbeans.modules.xml.schema.model.SimpleRestriction.class),
    new Target(org.netbeans.modules.xml.schema.model.SimpleType.class),
    new Target(org.netbeans.modules.xml.schema.model.SimpleTypeDefinition.class),
    new Target(org.netbeans.modules.xml.schema.model.SimpleTypeRestriction.class),
    new Target(org.netbeans.modules.xml.schema.model.TotalDigits.class),
    new Target(org.netbeans.modules.xml.schema.model.Union.class),
    new Target(org.netbeans.modules.xml.schema.model.Unique.class),
  };

  private Target(Class<? extends Component> clazz) {
    myClazz = clazz;
  }

  public Class<? extends Component> getClazz() {
    return myClazz;
  }

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
