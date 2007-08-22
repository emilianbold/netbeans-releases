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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

import org.netbeans.modules.bpel.editors.api.Diagram;
import org.netbeans.modules.bpel.editors.api.DiagramElement;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.01.11
 */
public final class DiagramImpl implements Diagram {

  public DiagramImpl(DesignView view) {
    myView = view;
  }

  public JComponent getComponent() {
    return myView;
  }

  public List<DiagramElement> getElements(boolean useSelection) {
    DiagramModel model = myView.getModel();
    List<DiagramElement> elements = new ArrayList<DiagramElement>();

    travel(getRoot(model, useSelection), elements, ""); // NOI18N

    return elements;
  }

  private void travel(Pattern pattern, List<DiagramElement> elements, String indent) {
    if (pattern == null) {
      return;
    }
    travel(pattern.getElements(), elements, indent);

    if (pattern instanceof CompositePattern) {
      CompositePattern composite = (CompositePattern) pattern;
      travel(composite.getBorder(), elements, indent);
      Collection<Pattern> patterns = composite.getNestedPatterns();

      for (Pattern patern : patterns) {
        travel(patern, elements, indent + "  "); // NOI18N
      }
    }
  }

  private void travel(Collection<VisualElement> visual, List<DiagramElement> elements, String indent) {
    for (VisualElement element : visual) {
      travel(element, elements, indent);
    }
  }

  private void travel(VisualElement element, List<DiagramElement> elements, String indent) {
    if (element != null) {
//out(indent + " see: " + element);
      elements.add(new DiagramElementImpl(element));
    }
  }

  private Pattern getRoot(DiagramModel model, boolean useSelection) {
    if (useSelection) {
      return model.getView().getSelectionModel().getSelectedPattern();
    }
    return model.getRootPattern();
  }

  public void clearHighlighting() {
    DiagramDecorator.getDecorator(myView).clearHighlighting();
  }

  private void out() {
    System.out.println();
  }

  private void out(Object object) {
    System.out.println(object);
  }

  private DesignView myView;
}
