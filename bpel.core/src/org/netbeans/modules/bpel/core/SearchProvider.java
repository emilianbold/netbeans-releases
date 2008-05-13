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
package org.netbeans.modules.bpel.core;

import javax.swing.Icon;
import org.openide.loaders.DataObject;
import org.netbeans.modules.xml.xam.Component;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.xml.search.api.SearchTarget;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.04.16
 */
final class SearchProvider extends org.netbeans.modules.xml.search.spi.SearchProvider.Adapter {

  // vlv
  public SearchProvider(DataObject data) {
    super(null, data);
  }

  @Override
  protected final Component getRoot(DataObject data) {
    BpelModel model = EditorUtil.getBpelModel(data);

    if (model == null) {
      return null;
    }
    return model.getProcess();
  }

  @Override
  protected final String getType(Component component) {
    return ((BpelEntity) component).getElementType().getName();
  }

  @Override
  protected final Icon getIcon(Component component) {
    return EditorUtil.getIcon(component);
  }

  @Override
  protected void gotoSource(Component component) {
    EditorUtil.goToSource((BpelEntity) component);
  }

  @Override
  protected final void gotoVisual(Component component) {
    EditorUtil.goToDesign((BpelEntity) component);
  }

  private static SearchTarget create(Class<? extends Object> clazz) {
    return new SearchTarget.Adapter(SearchProvider.class, clazz);
  }

  @Override
  public SearchTarget [] getTargets() {
    return TARGETS;
  }

  private static final SearchTarget [] TARGETS = new SearchTarget [] {
    create(org.netbeans.modules.bpel.model.api.BpelEntity.class),
    create(org.netbeans.modules.bpel.model.api.Assign.class),
    create(org.netbeans.modules.bpel.model.api.Branches.class),
    create(org.netbeans.modules.bpel.model.api.Catch.class),
    create(org.netbeans.modules.bpel.model.api.CatchAll.class),
    create(org.netbeans.modules.bpel.model.api.Compensate.class),
    create(org.netbeans.modules.bpel.model.api.CompensationHandler.class),
    create(org.netbeans.modules.bpel.model.api.Condition.class),
    create(org.netbeans.modules.bpel.model.api.Copy.class),
    create(org.netbeans.modules.bpel.model.api.Correlation.class),
    create(org.netbeans.modules.bpel.model.api.CorrelationContainer.class),
    create(org.netbeans.modules.bpel.model.api.CorrelationSet.class),
    create(org.netbeans.modules.bpel.model.api.CorrelationSetContainer.class),
    create(org.netbeans.modules.bpel.model.api.DeadlineExpression.class),
    create(org.netbeans.modules.bpel.model.api.Documentation.class),
    create(org.netbeans.modules.bpel.model.api.DurationExpression.class),
    create(org.netbeans.modules.bpel.model.api.Else.class),
    create(org.netbeans.modules.bpel.model.api.ElseIf.class),
    create(org.netbeans.modules.bpel.model.api.Empty.class),
    create(org.netbeans.modules.bpel.model.api.EventHandlers.class),
    create(org.netbeans.modules.bpel.model.api.Exit.class),
    create(org.netbeans.modules.bpel.model.api.FaultHandlers.class),
    create(org.netbeans.modules.bpel.model.api.Flow.class),
    create(org.netbeans.modules.bpel.model.api.For.class),
    create(org.netbeans.modules.bpel.model.api.ForEach.class),
    create(org.netbeans.modules.bpel.model.api.From.class),
    create(org.netbeans.modules.bpel.model.api.FromPart.class),
    create(org.netbeans.modules.bpel.model.api.If.class),
    create(org.netbeans.modules.bpel.model.api.Import.class),
    create(org.netbeans.modules.bpel.model.api.Invoke.class),
    create(org.netbeans.modules.bpel.model.api.Link.class),
    create(org.netbeans.modules.bpel.model.api.LinkContainer.class),
    create(org.netbeans.modules.bpel.model.api.Literal.class),
    create(org.netbeans.modules.bpel.model.api.MessageExchange.class),
    create(org.netbeans.modules.bpel.model.api.MessageExchangeContainer.class),
    create(org.netbeans.modules.bpel.model.api.OnAlarmEvent.class),
    create(org.netbeans.modules.bpel.model.api.OnAlarmPick.class),
    create(org.netbeans.modules.bpel.model.api.OnEvent.class),
    create(org.netbeans.modules.bpel.model.api.OnMessage.class),
    create(org.netbeans.modules.bpel.model.api.PartnerLink.class),
    create(org.netbeans.modules.bpel.model.api.PartnerLinkContainer.class),
    create(org.netbeans.modules.bpel.model.api.PatternedCorrelation.class),
    create(org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer.class),
    create(org.netbeans.modules.bpel.model.api.Pick.class),
    create(org.netbeans.modules.bpel.model.api.Process.class),
    create(org.netbeans.modules.bpel.model.api.Receive.class),
    create(org.netbeans.modules.bpel.model.api.RepeatUntil.class),
    create(org.netbeans.modules.bpel.model.api.Reply.class),
    create(org.netbeans.modules.bpel.model.api.ReThrow.class),
    create(org.netbeans.modules.bpel.model.api.Scope.class),
    create(org.netbeans.modules.bpel.model.api.Sequence.class),
    create(org.netbeans.modules.bpel.model.api.Source.class),
    create(org.netbeans.modules.bpel.model.api.Target.class),
    create(org.netbeans.modules.bpel.model.api.TerminationHandler.class),
    create(org.netbeans.modules.bpel.model.api.Throw.class),
    create(org.netbeans.modules.bpel.model.api.TimeEvent.class),
    create(org.netbeans.modules.bpel.model.api.To.class),
    create(org.netbeans.modules.bpel.model.api.ToPart.class),
    create(org.netbeans.modules.bpel.model.api.Validate.class),
    create(org.netbeans.modules.bpel.model.api.Variable.class),
    create(org.netbeans.modules.bpel.model.api.VariableContainer.class),
    create(org.netbeans.modules.bpel.model.api.VariableDeclaration.class),
    create(org.netbeans.modules.bpel.model.api.Wait.class),
    create(org.netbeans.modules.bpel.model.api.While.class),
  };
}
