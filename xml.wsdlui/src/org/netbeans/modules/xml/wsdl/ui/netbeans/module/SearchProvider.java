/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;

import org.netbeans.modules.xml.search.api.SearchTarget;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie.View;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.04.16
 */
final class SearchProvider extends org.netbeans.modules.xml.search.spi.SearchProvider.Adapter {

  public SearchProvider(DataObject data) {
    super(null, data);
  }

  @Override
  protected final Component getRoot(DataObject data)
  {
    WSDLModel model = getWSDLModel(data);

    if (model == null) {
      return null;
    }
    return model.getDefinitions();
  }

  @Override
  protected final String getType(Component component)
  {
    return component.getClass().getName();
  }

  @Override
  protected final Node getNode(Component component)
  {
    return NodesFactory.getInstance().create(component);
  }
  
  @Override
  protected final void gotoVisual(Component component)
  {
    highlight(component);
    ViewComponentCookie cookie = getDataObject().getCookie(ViewComponentCookie.class);
    if (cookie != null && cookie.canView(View.STRUCTURE, component)) {
        cookie.view(View.STRUCTURE, component);
    }
  }

  @Override
  protected void gotoSource(Component component) {
    ViewComponentCookie cookie = getDataObject().getCookie(ViewComponentCookie.class);
    if (cookie != null && cookie.canView(View.SOURCE, component)) {
        cookie.view(View.SOURCE, component);
    }
  }

  private WSDLModel getWSDLModel(DataObject data) {
    Model model = getModel(data);

    if (model instanceof WSDLModel) {
      return (WSDLModel) model;
    }
    return null;
  }

  @Override
  public SearchTarget [] getTargets()
  {
    return TARGETS;
  }

  private static SearchTarget create(Class<? extends Object> clazz) {
    return new SearchTarget.Adapter(SearchProvider.class, clazz);
  }

  private static final SearchTarget [] TARGETS = new SearchTarget [] {
    create(org.netbeans.modules.xml.wsdl.model.WSDLComponent.class),
    create(org.netbeans.modules.xml.wsdl.model.Binding.class),
    create(org.netbeans.modules.xml.wsdl.model.BindingFault.class),
    create(org.netbeans.modules.xml.wsdl.model.BindingInput.class),
    create(org.netbeans.modules.xml.wsdl.model.BindingOperation.class),
    create(org.netbeans.modules.xml.wsdl.model.BindingOutput.class),
    create(org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty.class),
    create(org.netbeans.modules.xml.wsdl.model.Definitions.class),
    create(org.netbeans.modules.xml.wsdl.model.Documentation.class),
    create(org.netbeans.modules.xml.wsdl.model.ExtensibilityElement.class),
    create(org.netbeans.modules.xml.wsdl.model.Fault.class),
    create(org.netbeans.modules.xml.wsdl.model.Import.class),
    create(org.netbeans.modules.xml.wsdl.model.Input.class),
    create(org.netbeans.modules.xml.wsdl.model.Message.class),
    create(org.netbeans.modules.xml.wsdl.model.NotificationOperation.class),
    create(org.netbeans.modules.xml.wsdl.model.OneWayOperation.class),
    create(org.netbeans.modules.xml.wsdl.model.Operation.class),
    create(org.netbeans.modules.xml.wsdl.model.OperationParameter.class),
    create(org.netbeans.modules.xml.wsdl.model.Output.class),
    create(org.netbeans.modules.xml.wsdl.model.Part.class),
    create(org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType.class),
    create(org.netbeans.modules.xml.wsdl.model.Port.class),
    create(org.netbeans.modules.xml.wsdl.model.PortType.class),
    create(org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias.class),
    create(org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query.class),
    create(org.netbeans.modules.xml.wsdl.model.RequestResponseOperation.class),
    create(org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role.class),
    create(org.netbeans.modules.xml.wsdl.model.Service.class),
    create(org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation.class),
    create(org.netbeans.modules.xml.wsdl.model.Types.class),
  };
}
