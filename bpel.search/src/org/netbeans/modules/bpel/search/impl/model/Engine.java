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

import java.util.List;
import org.openide.util.NbBundle;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.NamedElement;

import org.netbeans.modules.bpel.search.api.SearchException;
import org.netbeans.modules.bpel.search.api.SearchMatch;
import org.netbeans.modules.bpel.search.api.SearchOption;
import org.netbeans.modules.bpel.search.spi.SearchEngine;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.13
 */
public final class Engine extends SearchEngine.Adapter {

  /**{@inheritDoc}*/
  public void search(SearchOption option) throws SearchException {
    BpelModel model = (BpelModel) option.getSource();
    myTarget = ((Target) option.getTarget()).getClazz();
    myOption = option;
//out();
    fireSearchStarted(option);
    search(model.getProcess(), ""); // NOI18N
    fireSearchFinished(option);
  }

  /**{@inheritDoc}*/
  public Object[] getTargets() {
    return Target.values();
  }

  private void search(BpelEntity element, String indent) {
    if (element == null) {
      return;
    }
    process(element, indent);
    List<BpelEntity> children = element.getChildren();
  
    for (BpelEntity entity : children) {
      search(entity, indent + "    "); // NOI18N
    }
  }

  private void process(BpelEntity element, String indent) {
//out(indent + " see: " + element);
    if (checkTarget(element) && checkName(element)) {
//out(indent + "      add.");
      fireSearchFound(new Element(element));
    }
  }

  private boolean checkTarget(Object object) {
    if (myTarget == null) {
      return true;
    }
    return myTarget.isAssignableFrom(object.getClass());
  }

  private boolean checkName(BpelEntity element) {
    if (anyName()) {
      return true;
    }
    if ( !(element instanceof NamedElement)) {
      return false;
    }
    NamedElement named = (NamedElement) element;
    return accepts(named.getName());
  }

  private boolean anyName() {
    String text = myOption.getText();
    SearchMatch match = myOption.getSearchMatch();

    if (match == SearchMatch.PATTERN_MATCH && text.equals("*")) { // NOI18N
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
    return source instanceof BpelModel;
  }

  /**{@inheritDoc}*/
  public String getDisplayName() {
    return NbBundle.getMessage(Engine.class, "CTL_Engine_Display_Name"); // NOI18N
  }

  /**{@inheritDoc}*/
  public String getShortDescription() {
    return NbBundle.getMessage(
      Engine.class, "CTL_Engine_Short_Description"); // NOI18N
  }

  private SearchOption myOption;
  private Class<? extends BpelEntity> myTarget;
}
