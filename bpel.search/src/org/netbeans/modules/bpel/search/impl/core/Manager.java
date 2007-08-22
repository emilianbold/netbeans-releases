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
package org.netbeans.modules.bpel.search.impl.core;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;

import org.netbeans.modules.xml.search.api.SearchManager;
import org.netbeans.modules.xml.search.api.SearchMatch;
import org.netbeans.modules.xml.search.api.SearchPattern;
import org.netbeans.modules.xml.search.api.SearchTarget;
import org.netbeans.modules.xml.search.spi.SearchEngine;

import org.netbeans.modules.bpel.search.impl.action.SearchAction;
import org.netbeans.modules.bpel.search.impl.ui.Find;
import org.netbeans.modules.bpel.search.impl.ui.Search;
import org.netbeans.modules.bpel.search.impl.util.Util;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.15
 */
public final class Manager implements SearchManager {

  /**{@inheritDoc}*/
  public Manager() {
    myEngines = Util.getInstances(SearchEngine.class);
    mySearch = new Search();
  }

  /**{@inheritDoc}*/
  public Component getUI(
    Object source,
    SearchTarget [] targets,
    JComponent parent,
    boolean advanced)
  {
    List<SearchEngine> engines = getEngines(source);
//out("Set engines: " + engines);
    
    if (engines.isEmpty()) {
      return null;
    }
    if (advanced) {
      return mySearch.getUIComponent(engines, source, targets);
    }
    else {
      return new Find(engines, source, parent);
    }
  }

  /**{@inheritDoc}*/
  public SearchPattern getPattern(
    String text,
    SearchMatch match,
    boolean caseSensitive)
  {
    return new Pattern(text, match, caseSensitive);
  }

  /**{@inheritDoc}*/  
  public Action getSearchAction() {
    return new SearchAction.Manager();
  }

  private List<SearchEngine> getEngines(Object source) {
    List<SearchEngine> engines = new ArrayList<SearchEngine>();

    for (SearchEngine engine : myEngines) {
      if (engine.accepts(source)) {
        engines.add(engine);
      }
    }
    return engines;
  }

  private Search mySearch;
  private List<SearchEngine> myEngines;
}
