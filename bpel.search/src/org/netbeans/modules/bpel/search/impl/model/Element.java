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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.search.impl.model;

import javax.swing.Icon;

import org.netbeans.modules.bpel.editors.api.utils.Util;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.NamedElement;

import org.netbeans.modules.bpel.search.api.SearchElement;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.17
 */
final class Element extends SearchElement.Adapter {

  Element(BpelEntity element) {
    super(
      getText(element),
      getToolTip(element),
      getIcon(element),
      getParent(element)); 

    myElement = element;
  }

  /**{@inheritDoc}*/
  @Override
  public void gotoSource()
  {
    Util.goToSource(myElement);
  }

  /**{@inheritDoc}*/
  @Override
  public void selectOnDiagram()
  {
    Util.goToDesign(myElement);
  }

  /**{@inheritDoc}*/
  @Override
  public boolean equals(Object object)
  {
    if ( !(object instanceof Element)) {
      return false;
    }
    return ((Element) object).myElement.equals(myElement);
  }

  /**{@inheritDoc}*/
  @Override
  public int hashCode()
  {
    return myElement.hashCode();
  }

  private static String getText(BpelEntity element) {
    String name = getName(element);

    if (name != null) {
      return name;
    }
    return getType(element);
  }

  private static String getToolTip(BpelEntity element) {
    String name = getName(element);
    String type = getType(element);

    if (name == null) {
      return type;
    }
    return type + " '" + name + "'"; // NOI18N
  }

  private static SearchElement getParent(BpelEntity element) {
    BpelEntity parent = element.getParent();

    if (parent == null) {
      return null;
    }
    return new Element(parent);
  }

  private static String getName(BpelEntity element) {
    if (element instanceof NamedElement) {
      return ((NamedElement) element).getName();
    }
    return null;
  }

  private static String getType(BpelEntity element) {
    String type = element.getElementType().getName();
    int k = type.lastIndexOf("."); // NOI18N

    if (k == -1) {
      return type;
    }
    return type.substring(k + 1);
  }

  private static Icon getIcon(BpelEntity element) {
    return Util.getBasicNodeType(element).getIcon();
  }

  private BpelEntity myElement;
}
