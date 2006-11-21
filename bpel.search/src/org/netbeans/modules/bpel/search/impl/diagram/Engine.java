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

import java.util.Collection;

import org.openide.util.NbBundle;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

import org.netbeans.modules.bpel.search.api.SearchLog;
import org.netbeans.modules.bpel.search.api.SearchManagerAccess;
import org.netbeans.modules.bpel.search.api.SearchOption;
import org.netbeans.modules.bpel.search.api.SearchPattern;
import org.netbeans.modules.bpel.search.spi.SearchEngine;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.13
 */
public final class Engine extends SearchEngine.Adapter {

  /**{@inheritDoc}*/
  public String getDisplayName() {
    return NbBundle.getMessage(Engine.class, "CTL_Display_Name"); // NOI18N
  }

  /**{@inheritDoc}*/
  public String getShortDescription() {
    return NbBundle.getMessage(Engine.class, "CTL_Short_Description"); // NOI18N
  }

  /**{@inheritDoc}*/
  public boolean accepts(Object source) {
    return source instanceof DesignView;
  }

  /**{@inheritDoc}*/
  public void search(SearchOption searchOption) {
    DesignView designView = (DesignView) searchOption.getSource();
    DiagramModel model = designView.getModel();
    mySearchPattern = SearchManagerAccess.getManager().getPattern(
      searchOption.getText(),
      searchOption.getSearchMatch(),
      searchOption.isCaseSensitive());
//out();
    fireSearchStarted(searchOption);
    search(model.getRootPattern(), ""); // NOI18N
    fireSearchFinished();
  }

  private void search(Pattern pattern, String indent) {
    if (pattern == null) {
      return;
    }
    search(pattern.getElements(), indent);

    if (pattern instanceof CompositePattern) {
      CompositePattern composite = (CompositePattern) pattern;
      search(composite.getBorder(), indent);
      Collection<Pattern> patterns = composite.getNestedPatterns();

      for (Pattern patern : patterns) {
        search(patern, indent + "  "); // NOI18N
      }
    }
  }

  private void search(Collection<VisualElement> elements, String indent) {
    for (VisualElement element : elements) {
      search(element, indent);
    }
  }

  private void search(VisualElement element, String indent) {
    if (element == null) {
      return;
    }
    String text = element.getText();

    if (text == null) {
      return;
    }
    text = text.trim();
//out(indent + " see: " + text);

    if (mySearchPattern.accepts(text)) {
//out(indent + "      add.");
      fireSearchFound(new Element(element));
    }
  }

  private void out() {
    SearchLog.out();
  }

  private void out(Object object) {
    SearchLog.out(object);
  }

  private SearchPattern mySearchPattern;
}
