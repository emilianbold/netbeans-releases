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

import java.util.List;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Named;

import org.netbeans.modules.bpel.editors.api.utils.Util;

import org.netbeans.modules.xml.search.api.SearchException;
import org.netbeans.modules.xml.search.api.SearchMatch;
import org.netbeans.modules.xml.search.api.SearchOption;
import org.netbeans.modules.xml.search.api.SearchTarget;
import org.netbeans.modules.xml.search.spi.SearchEngine;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.13
 */
public final class Engine extends SearchEngine.Adapter {

  /**{@inheritDoc}*/
  public void search(SearchOption option) throws SearchException {
    myClazz = null;
    myOption = option;
    myList = (List) option.getSource();
    SearchTarget target = option.getTarget();

    if (target != null) {
      myClazz = target.getClazz();
    }
//out();
    fireSearchStarted(option);
    search(Util.getRoot((Model) myList.get(0)), ""); // NOI18N
    fireSearchFinished(option);
  }

  private void search(Object object, String indent) {
    if ( !(object instanceof Component)) {
      return;
    }
    Component component = (Component) object;
    process(component, indent);
    List children = component.getChildren();
  
    for (Object child : children) {
      search(child, indent + "    "); // NOI18N
    }
  }

  private void process(Component component, String indent) {
//out(indent + " see: " + component);
    if (checkClazz(component) && checkName(component)) {
//out(indent + "      add.");
      fireSearchFound(new Element(component, myList.get(1), myList.get(2)));
    }
  }

  private boolean checkClazz(Object object) {
    if (myClazz == null) {
      return true;
    }
    return myClazz.isAssignableFrom(object.getClass());
  }

  private boolean checkName(Component component) {
    if (anyName()) {
      return true;
    }
    if ( !(component instanceof Named)) {
      return false;
    }
    return accepts(((Named) component).getName());
  }

  private boolean anyName() {
    String text = myOption.getText();
    SearchMatch match = myOption.getSearchMatch();

    if (match == SearchMatch.PATTERN && text.equals("*")) { // NOI18N
      return true;
    }
    if (match == SearchMatch.REGULAR_EXPRESSION && text.equals("\\.*")) { // NOI18N
      return true;
    }
    if (match == null && text.equals("")) { // NOI18N
      return true;
    }
    return false;
  }

  /**{@inheritDoc}*/
  public boolean accepts(Object source) {
    return source instanceof List;
  }

  /**{@inheritDoc}*/
  public String getDisplayName() {
    return i18n(Engine.class, "CTL_Engine_Display_Name"); // NOI18N
  }

  /**{@inheritDoc}*/
  public String getShortDescription() {
    return i18n(Engine.class, "CTL_Engine_Short_Description"); // NOI18N
  }

  private List myList;
  private SearchOption myOption;
  private Class<? extends Component> myClazz;
}
