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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.print.impl.action;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.JComponent;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.PrintCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

import org.netbeans.modules.print.api.PrintManager;
import org.netbeans.modules.print.spi.PrintProvider;
import org.netbeans.modules.print.impl.provider.ComponentProvider;
import org.netbeans.modules.print.impl.provider.TextProvider;
import org.netbeans.modules.print.impl.ui.Preview;
import org.netbeans.modules.print.impl.util.Option;
import static org.netbeans.modules.print.impl.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.24
 */
public class PrintMenu extends IconAction {

  public PrintMenu() {
    this("LBL_Print_Menu", "TLT_Print_Menu", null); // NOI18N
  }

  protected PrintMenu(String name, String toolTip, String icon) {
    super(
      i18n(PrintAction.class, name),
      i18n(PrintAction.class, toolTip),
      icon(Option.class, icon)
    );
    setEnabled(false);
  }

  public void actionPerformed(ActionEvent event) {
    List<PrintProvider> providers = getPrintProviders();

    if (providers != null) {
      Preview.getDefault().print(providers, true);
    }
    else {
      PrintCookie cookie = getPrintCookie();

      if (cookie != null) {
        cookie.print();
      }
    }
  }

  private List<PrintProvider> getPrintProviders() {
//out();
//out("get print providers");
    List<PrintProvider> providers = getComponentProviders();

    if (providers != null) {
//out("COMPONENT PROVIDERS: " + providers);
      return providers;
    }
    providers = getNodeProviders();

    if (providers != null) {
//out("NODE PROVIDERS: " + providers);
      return providers;
    }
    return null;
  }

  private List<PrintProvider> getComponentProviders() {
    PrintProvider provider = getComponentProvider();

    if (provider == null) {
      return null;
    }
    return Collections.singletonList(provider);
  }

  private PrintProvider getComponentProvider() {
    TopComponent top = getActiveTopComponent();

    if (top == null) {
      return null;
    }
//out("TOP: " + top.getDisplayName() + " " + top.getName() + " " + top.getClass().getName());
    PrintProvider provider = (PrintProvider) top.getLookup().lookup(PrintProvider.class);

    if (provider != null) {
//out("TOP PROVIDER: " + provider);
      return provider;
    }
    DataObject data = (DataObject) top.getLookup().lookup(DataObject.class);
//out("DATA: " + data);

    if (data != null) {
      provider = (PrintProvider) data.getLookup().lookup(PrintProvider.class);

      if (provider != null) {
//out("DATA PROVIDER: " + provider);
        return provider;
      }
    }
    return getComponentProvider(top, data);
  }

  private PrintProvider getComponentProvider(TopComponent top, DataObject data) {
    List<JComponent> components = getComponents(top);

    if (components.size() == 0) {
      return null;
    }
    return new ComponentProvider(components, getName(components, top, data), getDate(data));
  }

  private String getName(List<JComponent> components, TopComponent top, DataObject data) {
    for (JComponent component : components) {
      Object object = component.getClientProperty(PrintManager.PRINT_NAME);

      if (object instanceof String) {
        return (String) object;
      }
    }
    if (data == null) {
      return top.getDisplayName();
    }
    return data.getName();
  }

  private Date getDate(DataObject data) {
    if (data != null) {
      return data.getPrimaryFile().lastModified();
    }
    return new Date(System.currentTimeMillis());
  }

  private List<JComponent> getComponents(Container container) {
//out();
    List<JComponent> printable = new ArrayList<JComponent>();
    getPrintable(container, printable);
//out();
    return printable;
  }

  private void getPrintable(Container container, List<JComponent> printable) {
    if (container.isShowing() && isPrintable(container)) {
//out("see: " + container.getClass().getName());
      printable.add((JComponent) container);
    }
    Component[] components = container.getComponents();

    for (Component component : components) {
      if (component instanceof Container) {
        getPrintable((Container) component, printable);
      }
    }
  }

  private boolean isPrintable(Container container) {
    return
      container instanceof JComponent &&
      ((JComponent) container).getClientProperty(PrintManager.PRINT_PRINTABLE) == Boolean.TRUE;
  }

  private List<PrintProvider> getNodeProviders() {
    Node [] nodes = getSelectedNodes();
//out();
//out("get node provider");
    if (nodes == null) {
//out("NODES NULL");
      return null;
    }
    List<PrintProvider> providers = new ArrayList<PrintProvider>();
    PrintProvider provider;

    for (Node node : nodes) {
//out("  see: " + node);
      provider = (PrintProvider) node.getLookup().lookup(PrintProvider.class);
//out("     : " + provider);

      if (provider != null) {
        providers.add(provider);
        continue;
      }
      provider = getEditorProvider(node);

      if (provider != null) {
        providers.add(provider);
      }
    }
    if (providers.size() == 0) {
//out("result null");
      return null;
    }
//out("result: " + providers);
    return providers;
  }

  private PrintProvider getEditorProvider(Node node) {
//out("get editor provider");
    DataObject data = getDataObject(node);

    if (data == null) {
//out("get editor provider.1");
      return null;
    }
    EditorCookie editor = (EditorCookie) data.getCookie(EditorCookie.class);

    if (editor == null) {
//out("get editor provider.2");
      return null;
    }
    if (editor.getDocument() == null) {
//out("get editor provider.3");
      return null;
    }
//out("get editor provider.4");
    return new TextProvider(editor, getDate(data));
  }

  private PrintCookie getPrintCookie() {
    Node node = getSelectedNode();

    if (node == null) {
      return null;
    }
    return (PrintCookie) node.getCookie(PrintCookie.class);
  }

  @Override
  public boolean isEnabled() {
    if (super.isEnabled()) {
      return true;
    }
//out("IS ENABLED: " + (getPrintProviders() != null || getPrintCookie() != null));
//out("          : " + getPrintProviders());
    return getPrintProviders() != null || getPrintCookie() != null;
  }
}
