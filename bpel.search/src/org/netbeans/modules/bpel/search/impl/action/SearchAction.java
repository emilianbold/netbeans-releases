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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.ModelCookie;

import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.basic.SchemaColumnsView;
import org.netbeans.modules.xml.schema.ui.basic.SchemaTreeView;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.bpel.core.helper.api.CoreUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.search.api.SearchManager;
import org.netbeans.modules.bpel.search.api.SearchTarget;
import org.netbeans.modules.bpel.search.impl.ui.Search;
import org.netbeans.modules.bpel.search.impl.util.Util;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.13
 */
public final class SearchAction extends IconAction {

  public SearchAction() {
    this(null, "TLT_Search_Action", "search"); // NOI18N
  }

  private SearchAction(String name, String toolTip, String icon) {
    super(
      i18n(SearchAction.class, name),
      i18n(SearchAction.class, toolTip),
      icon(Util.class, icon)
    );
    setEnabled(false);
  }

  public void actionPerformed(ActionEvent event) {
    Node node = getLastNode();

    Model model = getModel(node);
    ShowCookie cookie = getShowCookie(node);
    Object view = getView();
//out();
//out("ShowCookie: " + cookie);
//out("SchemaTreeView: " + view);
    List<Object> list = new ArrayList<Object>();

    list.add(model);
    list.add(cookie);
    list.add(view);

    SearchManager.getDefault().createSearch(list, getTargets(model));
  }

  private ShowCookie getShowCookie(Node node) {
    DataObject data = getDataObject(node);

    if (data == null) {
      return null;
    }
    return data.getCookie(ShowCookie.class);
  }

  private Object getView() {
    Container container = getActivateTopComponent();
    Object view = getTreeView(container, "  "); // NOI18N

    if (view != null) {
      return view;
    }
    return getColumnView(container, "  "); // NOI18N
  }

  private SchemaTreeView getTreeView(Container container, String indent) {
//out(indent + container.getClass().getName());
    if (container instanceof SchemaTreeView) {
      return (SchemaTreeView) container;
    }
    Component[] components = container.getComponents();
    SchemaTreeView view;

    for (Component component : components) {
      if (component instanceof Container) {
        view = getTreeView((Container) component, "    " + indent); // NOI18N

        if (view != null) {
          return view;
        }
      }
    }
    return null;
  }

  private SchemaColumnsView getColumnView(
    Container container,
    String indent)
  {
//out(indent + container.getClass().getName());
    if (container instanceof SchemaColumnsView) {
      return (SchemaColumnsView) container;
    }
    Component[] components = container.getComponents();
    SchemaColumnsView view;

    for (Component component : components) {
      if (component instanceof Container) {
        view = getColumnView((Container) component, "    " + indent); // NOI18N

        if (view != null) {
          return view;
        }
      }
    }
    return null;
  }
  
  private SearchTarget [] getTargets(Model model) {
    if (model instanceof BpelModel) {
      return Target.BPEL;
    }
    if (model instanceof WSDLModel) {
      return Target.WSDL;
    }
    if (model instanceof SchemaModel) {
      return Target.SCHEMA;
    }
    return new SearchTarget [] {};
  }

  private Model getModel(Node node) {
//out();
//out("get model");
//out("node: " + node);
    DataObject data = getDataObject(node);
//out("data: " + data);

    if (data == null) {
      return null;
    }
    Model model = CoreUtil.getBpelModel(data);
//out("model: " + model);

    if (model != null) {
      return model;
    }
    ModelCookie cookie = data.getCookie(ModelCookie.class);

    if (cookie == null) {
      return null;
    }
    try {
      return cookie.getModel();
    } 
    catch (IOException e) {
      return null;
    }
  }

  @Override
  public boolean isEnabled()
  {
    return true;
  }

  private Search mySearch;
  public static final Action DEFAULT = new SearchAction();
}
