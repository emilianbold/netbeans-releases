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
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaModel;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.06.27
 */
final class Util {

  private Util() {}
  
  public static void visit(NamedComponentReference<GlobalType> type, NamedComponentReference<GlobalElement> element, Referenceable target, Component component, List<Component> usage) {
    visit(type, target, component, usage);
    visit(element, target, component, usage);
  }        
          
  public static void visit(Reference reference, Referenceable target, Component component, List<Component> usage) {
    if (reference == null || reference.get() == null) {
      return;
    }
//out();
//out("   Visit: " + getName(reference.get()));
//out("  target: " + target.getName());
    if (target.equals(reference.get())) {
//out();
//out("AdD: " + getName(component));
      usage.add(component);
    }
  }

  public static void visit(QName qName, Referenceable target, Component component, List<Component> usage) {
//out();
//out("VISIT: " + qName);
    if (target instanceof Named && contains(qName, (Named) target)) {
//out();
//out("ADd: " + getName(component));
      usage.add(component);
    }
  }

  private static boolean contains(QName qName, Named target) {
    if (qName == null) {
//out("qName is null");
      return false;
    }
    String part = qName.getLocalPart();

    if ( !part.equals(target.getName())) {
//out("Target name != part");
//out("         part: " + part);
//out("  Target name: " + target.getName());
      return false;
    }
    return qName.getNamespaceURI().equals(getNamespace(target.getModel()));
  }

  private static String getNamespace(Model model) {
    if (model instanceof SchemaModel) {
      return ((SchemaModel) model).getSchema().getTargetNamespace();
    }
    if (model instanceof WSDLModel) {
      return ((WSDLModel) model).getDefinitions().getTargetNamespace();
    }
    return null;
  }

  public static String getName(Object component) {
    if (component == null) {
      return null;
    }
    if (component instanceof Named) {
      String name = ((Named) component).getName();

      if (name != null) {
        return name;
      }
    }
    return component.getClass().getName();
  }

  public static int checkQuery(Query query, String name) {
    if (name == null || name.length() == 0) {
      return -1;
    }
    if (query == null) {
      return -1;
    }
    String path = query.getContent();

    if (path == null) {
      return -1;
    }
    return checkQuery(path, name);
  }

  private static int checkQuery(String path, String name) {
//out();
//out("path: " + path);
//out("name: " + name);
    if (path.startsWith(name + "/")) {
      return 0;
    }
    if (path.startsWith("/" + name + "/")) {
      return 1;
    }
    int k = path.indexOf("/" + name + "/");

    if (k != -1) {
      return k + 1;
    }
    k = path.indexOf(":" + name + "/");

    if (k != -1) {
      return k + 1;
    }
    if (path.endsWith("/" + name)) {
      return path.length() - name.length();
    }
    if (path.endsWith(":" + name)) {
      return path.length() - name.length();
    }
    return -1;
  }
}
