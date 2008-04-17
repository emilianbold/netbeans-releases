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
package org.netbeans.modules.bpel.diagram;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;

import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.search.api.SearchTarget;
import org.netbeans.modules.bpel.search.spi.SearchProvider;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.04.16
 */
public class DiagramProvider implements SearchProvider {

  public Model getModel(Node node) {
//out();
//out("node: " + node);
    if (node == null) {
      return null;
    }
    DataObject data = (DataObject) node.getLookup().lookup(DataObject.class);
//out("data: " + data);

    if (data == null) {
      return null;
    }
    return EditorUtil.getBpelModel(data);
  }

  public SearchTarget [] getTargets() {
    return TARGETS;
  }

  private static SearchTarget createTarget(Class<? extends Component> clazz) {
    return new SearchTarget.Adapter(DiagramProvider.class, clazz);
  }

  private static final SearchTarget [] TARGETS = new SearchTarget [] {
    createTarget(org.netbeans.modules.bpel.model.api.BpelEntity.class),
    createTarget(org.netbeans.modules.bpel.model.api.Assign.class),
    createTarget(org.netbeans.modules.bpel.model.api.Branches.class),
    createTarget(org.netbeans.modules.bpel.model.api.Catch.class),
    createTarget(org.netbeans.modules.bpel.model.api.CatchAll.class),
    createTarget(org.netbeans.modules.bpel.model.api.Compensate.class),
    createTarget(org.netbeans.modules.bpel.model.api.CompensationHandler.class),
    createTarget(org.netbeans.modules.bpel.model.api.Condition.class),
    createTarget(org.netbeans.modules.bpel.model.api.Copy.class),
    createTarget(org.netbeans.modules.bpel.model.api.Correlation.class),
    createTarget(org.netbeans.modules.bpel.model.api.CorrelationContainer.class),
    createTarget(org.netbeans.modules.bpel.model.api.CorrelationSet.class),
    createTarget(org.netbeans.modules.bpel.model.api.CorrelationSetContainer.class),
    createTarget(org.netbeans.modules.bpel.model.api.DeadlineExpression.class),
    createTarget(org.netbeans.modules.bpel.model.api.Documentation.class),
    createTarget(org.netbeans.modules.bpel.model.api.DurationExpression.class),
    createTarget(org.netbeans.modules.bpel.model.api.Else.class),
    createTarget(org.netbeans.modules.bpel.model.api.ElseIf.class),
    createTarget(org.netbeans.modules.bpel.model.api.Empty.class),
    createTarget(org.netbeans.modules.bpel.model.api.EventHandlers.class),
    createTarget(org.netbeans.modules.bpel.model.api.Exit.class),
    createTarget(org.netbeans.modules.bpel.model.api.FaultHandlers.class),
    createTarget(org.netbeans.modules.bpel.model.api.Flow.class),
    createTarget(org.netbeans.modules.bpel.model.api.For.class),
    createTarget(org.netbeans.modules.bpel.model.api.ForEach.class),
    createTarget(org.netbeans.modules.bpel.model.api.From.class),
    createTarget(org.netbeans.modules.bpel.model.api.FromPart.class),
    createTarget(org.netbeans.modules.bpel.model.api.If.class),
    createTarget(org.netbeans.modules.bpel.model.api.Import.class),
    createTarget(org.netbeans.modules.bpel.model.api.Invoke.class),
    createTarget(org.netbeans.modules.bpel.model.api.Link.class),
    createTarget(org.netbeans.modules.bpel.model.api.LinkContainer.class),
    createTarget(org.netbeans.modules.bpel.model.api.Literal.class),
    createTarget(org.netbeans.modules.bpel.model.api.MessageExchange.class),
    createTarget(org.netbeans.modules.bpel.model.api.MessageExchangeContainer.class),
    createTarget(org.netbeans.modules.bpel.model.api.OnAlarmEvent.class),
    createTarget(org.netbeans.modules.bpel.model.api.OnAlarmPick.class),
    createTarget(org.netbeans.modules.bpel.model.api.OnEvent.class),
    createTarget(org.netbeans.modules.bpel.model.api.OnMessage.class),
    createTarget(org.netbeans.modules.bpel.model.api.PartnerLink.class),
    createTarget(org.netbeans.modules.bpel.model.api.PartnerLinkContainer.class),
    createTarget(org.netbeans.modules.bpel.model.api.PatternedCorrelation.class),
    createTarget(org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer.class),
    createTarget(org.netbeans.modules.bpel.model.api.Pick.class),
    createTarget(org.netbeans.modules.bpel.model.api.Process.class),
    createTarget(org.netbeans.modules.bpel.model.api.Receive.class),
    createTarget(org.netbeans.modules.bpel.model.api.RepeatUntil.class),
    createTarget(org.netbeans.modules.bpel.model.api.Reply.class),
    createTarget(org.netbeans.modules.bpel.model.api.ReThrow.class),
    createTarget(org.netbeans.modules.bpel.model.api.Scope.class),
    createTarget(org.netbeans.modules.bpel.model.api.Sequence.class),
    createTarget(org.netbeans.modules.bpel.model.api.Source.class),
    createTarget(org.netbeans.modules.bpel.model.api.Target.class),
    createTarget(org.netbeans.modules.bpel.model.api.TerminationHandler.class),
    createTarget(org.netbeans.modules.bpel.model.api.Throw.class),
    createTarget(org.netbeans.modules.bpel.model.api.TimeEvent.class),
    createTarget(org.netbeans.modules.bpel.model.api.To.class),
    createTarget(org.netbeans.modules.bpel.model.api.ToPart.class),
    createTarget(org.netbeans.modules.bpel.model.api.Validate.class),
    createTarget(org.netbeans.modules.bpel.model.api.Variable.class),
    createTarget(org.netbeans.modules.bpel.model.api.VariableContainer.class),
    createTarget(org.netbeans.modules.bpel.model.api.VariableDeclaration.class),
    createTarget(org.netbeans.modules.bpel.model.api.Wait.class),
    createTarget(org.netbeans.modules.bpel.model.api.While.class),
  };
}
