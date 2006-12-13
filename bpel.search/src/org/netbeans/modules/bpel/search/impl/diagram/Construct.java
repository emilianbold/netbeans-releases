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
package org.netbeans.modules.bpel.search.impl.diagram;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.openide.util.NbBundle;

import org.netbeans.modules.bpel.model.api.BpelEntity;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

import org.netbeans.modules.bpel.search.api.SearchOption;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.05
 */
public final class Construct extends Engine {

  /**{@inheritDoc}*/
  public void search(SearchOption option) {
    DesignView view = (DesignView) option.getSource();
    myModel = view.getModel();
    Util.getDecorator(view).clearHighlighting();
//out();
    fireSearchStarted(option);
    search(getRoot(myModel, option.useSelection()).getOMReference(), ""); // NOI18N
    fireSearchFinished(option);
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
    Pattern pattern = myModel.getPattern(element);

    if (pattern == null) {
      return;
    }
    Iterator<VisualElement> iterator = pattern.getElements().iterator();

    if ( !iterator.hasNext()) {
      return;
    }
    if (acceptsAttribute(element) || acceptsComponent(element)) {
//out(indent + "      add.");
      fireSearchFound(new Element(iterator.next()));
    }
  }

  private boolean acceptsAttribute(BpelEntity element) {
    NamedNodeMap attributes = element.getPeer().getAttributes();

    for (int i=0; i < attributes.getLength(); i++) {
      Node attribute = attributes.item(i);
     
      if (accepts(attribute.getNodeName())) {
        return true;
      }
      if (accepts(attribute.getNodeValue())) {
        return true;
      }
    }
    return false;
  }

  private boolean acceptsComponent(BpelEntity element) {
    return accepts(element.getPeer().getTagName());
  }

  /**{@inheritDoc}*/
  public String getDisplayName() {
    return NbBundle.getMessage(Engine.class, "CTL_Construct_Display_Name"); // NOI18N
  }

  /**{@inheritDoc}*/
  public String getShortDescription() {
    return NbBundle.getMessage(
      Engine.class, "CTL_Construct_Short_Description"); // NOI18N
  }

  private DiagramModel myModel;
}
