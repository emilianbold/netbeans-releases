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
package org.netbeans.modules.bpel.search.impl.diagram;

import java.util.List;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

import org.netbeans.modules.bpel.editors.api.Diagram;
import org.netbeans.modules.bpel.editors.api.DiagramElement;

import org.netbeans.modules.xml.search.api.SearchException;
import org.netbeans.modules.xml.search.api.SearchOption;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.05
 */
public final class Construct extends Engine {

  /**{@inheritDoc}*/
  public void search(SearchOption option) throws SearchException {
    Diagram diagram = (Diagram) option.getSource();
    diagram.clearHighlighting();
//out();
    fireSearchStarted(option);
    search(diagram, option.useSelection());
    fireSearchFinished(option);
  }
  
  private void search(Diagram diagram, boolean useSelection) {
    List<DiagramElement> elements = diagram.getElements(useSelection);

    for (DiagramElement element : elements) {
      Component component = element.getComponent();

      if (acceptsAttribute(component) || acceptsComponent(component)) {
//out(indent + "      add.");
        fireSearchFound(new Element(element));
      }
    }
  }
  
  private boolean acceptsAttribute(Component component) {
    if ( !(component instanceof DocumentComponent)) {
      return false;
    }
    NamedNodeMap attributes = ((DocumentComponent) component).
      getPeer().getAttributes();

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

  private boolean acceptsComponent(Component component) {
    if ( !(component instanceof DocumentComponent)) {
      return false;
    }
    return accepts(((DocumentComponent) component).getPeer().getTagName());
  }

  /**{@inheritDoc}*/
  public String getDisplayName() {
    return i18n(Engine.class, "CTL_Construct_Display_Name"); // NOI18N
  }

  /**{@inheritDoc}*/
  public String getShortDescription() {
    return i18n(Engine.class, "CTL_Construct_Short_Description"); // NOI18N
  }
}
