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
package org.netbeans.modules.bpel.search.impl.ui;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.openide.actions.FindAction;
import org.netbeans.modules.xml.xam.ui.search.SearchControlPanel;
import org.netbeans.modules.xml.xam.ui.search.SearchProvider;
import org.netbeans.modules.xml.xam.ui.search.Query;

import org.netbeans.modules.xml.search.api.SearchElement;
import org.netbeans.modules.xml.search.api.SearchEvent;
import org.netbeans.modules.xml.search.api.SearchException;
import org.netbeans.modules.xml.search.api.SearchMatch;
import org.netbeans.modules.xml.search.api.SearchOption;
import org.netbeans.modules.xml.search.spi.SearchEngine;
import org.netbeans.modules.xml.search.spi.SearchListener;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.20
 */
public final class Find extends SearchControlPanel {

  /**{@inheritDoc}*/
  public Find(List<SearchEngine> engines, Object source, JComponent parent) {
    super();
    bindAction(parent);
    List<Object> providers = new ArrayList<Object>();

    for (SearchEngine engine : engines) {
      providers.add(new Provider(engine, source));
    }
    setProviders(providers);
  }

  @Override
  protected void hideResults()
  {
//out("Hide selection");
    if (myElements == null) {
      return;
    }
    for (Object element : myElements) {
      ((SearchElement) element).highlight(false);
    }
    myElements = null;
  }

  @Override
  protected void showSearchResult(Object object)
  {
//out("show");
    if ( !(object instanceof SearchElement)) {
      return;
    }
    ((SearchElement) object).select();
  }

  private void bindAction(JComponent parent) {
    FindAction findAction = (FindAction) FindAction.get(FindAction.class);
    Object key = findAction.getActionMapKey();
    parent.getActionMap().put(key, new AbstractAction () {
      public void actionPerformed(ActionEvent event) {
        setVisible(true);
      }
    });
    InputMap keys =
      parent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    KeyStroke stroke = (KeyStroke) findAction.getValue(Action.ACCELERATOR_KEY);

    if (stroke == null) {
      stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
    }
    keys.put(stroke, key);
  }

  // ---------------------------------------------------------------------
  private final class Provider implements SearchProvider, SearchListener {
    
    private Provider(SearchEngine engine, Object source) {
      mySearchEngine = engine;
      mySearchEngine.addSearchListener(this);
      mySource = source;
    }

    public String getDisplayName() {
      return mySearchEngine.getDisplayName();
    }

    public String getShortDescription() {
      return mySearchEngine.getShortDescription();
    }

    public String getInputDescription() {
      return i18n(Provider.class, "CTL_Input_Description"); // NOI18N
    }

    public List<Object> search(Query query)
      throws org.netbeans.modules.xml.xam.ui.search.SearchException
    {
      SearchMatch match = getMatch(query);
      String text = getText(query, match);

      SearchOption option = new SearchOption.Adapter(
        text,
        mySource,
        null, // target
        match,
        false, // case sensitive
        query.useSelected());

      try {
        mySearchEngine.search(option);
      }
      catch (SearchException e) {
        throw new org.netbeans.modules.xml.xam.ui.search.SearchException(
          e.getMessage(), e);
      }
//out("returned: " + myElements);
      return myElements;
    }

    private SearchMatch getMatch(Query query) {
      if (query.isRegularExpression()) {
        return SearchMatch.REGULAR_EXPRESSION;
      }
      return SearchMatch.PATTERN;
    }

    private String getText(Query query, SearchMatch match) {
      String text = query.getQuery().trim();

      if (match == SearchMatch.PATTERN) {
        return "*" + text + "*"; // NOI18N
      }
      return text;
    }

    public void searchStarted(SearchEvent event) {
      myElements = new ArrayList<Object>();
    }

    public void searchFound(SearchEvent event) {
      myElements.add(event.getSearchElement());
    }

    public void searchFinished(SearchEvent event) {}

    private Object mySource;
    private SearchEngine mySearchEngine;
  }

  private List<Object> myElements;
}
