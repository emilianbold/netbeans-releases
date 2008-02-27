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
package org.netbeans.modules.bpel.search.impl.model;

import javax.swing.Icon;
import org.netbeans.modules.xml.xam.Component;

import org.netbeans.modules.bpel.editors.api.utils.Util;
import org.netbeans.modules.bpel.editors.api.utils.RefactorUtil;
import org.netbeans.modules.bpel.search.api.SearchElement;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.17
 */
final class Element extends SearchElement.Adapter {

  Element(Component component, Object cookie, Object view) {
    super(
      getName(component),
      getToolTip(component),
      getIcon(component),
      getParent(component, cookie, view));

    myComponent = component;
    myCookie = cookie;
    myView = view;
  }

  @Override
  public void gotoSource()
  {
    Util.goToSource(myComponent);
  }

  @Override
  public void select()
  {
    Util.goToDesign(myComponent, myCookie, myView);
  }

  @Override
  public boolean equals(Object object)
  {
    if ( !(object instanceof Element)) {
      return false;
    }
    return ((Element) object).myComponent.equals(myComponent);
  }

  @Override
  public int hashCode()
  {
    return myComponent.hashCode();
  }

  private static String getName(Component component) {
    return RefactorUtil.getName(component);
  }

  private static String getToolTip(Component component) {
    return RefactorUtil.getToolTip(component);
  }

  private static SearchElement getParent(
    Component component,
    Object cookie,
    Object view)
  {
    Component parent = component.getParent();

    if (parent == null) {
      return null;
    }
    return new Element(parent, cookie, view);
  }

  private static Icon getIcon(Component component) {
    return RefactorUtil.getIcon(component);
  }

  private Component myComponent;
  private Object myCookie;
  private Object myView;
}
