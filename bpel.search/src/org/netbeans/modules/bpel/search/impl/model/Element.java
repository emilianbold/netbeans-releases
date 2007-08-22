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
package org.netbeans.modules.bpel.search.impl.model;

import javax.swing.Icon;

import org.netbeans.modules.xml.xam.Component;

import org.netbeans.modules.bpel.editors.api.utils.Util;
import org.netbeans.modules.bpel.editors.api.utils.RefactorUtil;
import org.netbeans.modules.xml.search.api.SearchElement;

import static org.netbeans.modules.print.ui.PrintUI.*;

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

  /**{@inheritDoc}*/
  @Override
  public void gotoSource()
  {
    Util.goToSource(myComponent);
  }

  /**{@inheritDoc}*/
  @Override
  public void select()
  {
    Util.goToDesign(myComponent, myCookie, myView);
  }

  /**{@inheritDoc}*/
  @Override
  public boolean equals(Object object)
  {
    if ( !(object instanceof Element)) {
      return false;
    }
    return ((Element) object).myComponent.equals(myComponent);
  }

  /**{@inheritDoc}*/
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
