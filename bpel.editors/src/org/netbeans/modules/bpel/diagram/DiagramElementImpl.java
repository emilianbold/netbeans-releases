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
package org.netbeans.modules.bpel.diagram;

import org.netbeans.modules.xml.xam.Component;

import org.netbeans.modules.bpel.editors.api.DiagramElement;
import org.netbeans.modules.bpel.editors.api.utils.Util;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.06
 */
final class DiagramElementImpl implements DiagramElement {

  DiagramElementImpl(VisualElement element) {
    myElement = element;
  }

  public String getText() {
    return myElement.getText();
  }

  public void gotoSource() {
    Util.goToSource(getComponent());
  }

  public void select() {
    Pattern pattern = myElement.getPattern();
    DesignView view = pattern.getModel().getView();

    // select
    view.getSelectionModel().setSelectedPattern(pattern);

    // glow
    getDecorator().select(getComponent());

    // scroll
    myElement.scrollTo();
  }

  public void highlight(boolean highlighted) {
    getDecorator().highlight(getComponent(), highlighted);
  }

  private DiagramDecorator getDecorator() {
    return DiagramDecorator.getDecorator(myElement.getPattern().getModel().getView());
  }

  public Component getComponent() {
    return myElement.getPattern().getOMReference();
  }

  private VisualElement myElement;
}
