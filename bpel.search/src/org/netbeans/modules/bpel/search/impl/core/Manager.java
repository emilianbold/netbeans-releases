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
package org.netbeans.modules.bpel.search.impl.core;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JComponent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import org.openide.actions.FindAction;

import org.netbeans.modules.bpel.search.api.SearchManager;
import org.netbeans.modules.bpel.search.api.SearchMatch;
import org.netbeans.modules.bpel.search.api.SearchPattern;
import org.netbeans.modules.bpel.search.spi.SearchEngine;
import org.netbeans.modules.bpel.search.impl.ui.Find;
import org.netbeans.modules.bpel.search.impl.util.Util;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.15
 */
public final class Manager implements SearchManager {

  /**{@inheritDoc}*/
  public Manager() {
    myEngines = Util.getInstances(SearchEngine.class);
  }

  /**{@inheritDoc}*/
  public JComponent getUI(Object source, JComponent parent) {
    SearchEngine engine = getEngine(source);
//Log.out("Set engine: " + engine);
    assert engine != null : "Can't find engine for " + source; // NOI18N
    Find find = new Find(engine, source);
    find.setVisible(false);
    bindAction(parent, find);
    return find;
  }

  /**{@inheritDoc}*/
  public SearchPattern getPattern(
    String text,
    SearchMatch searchMatch,
    boolean isCaseSensitive)
  {
    return new Pattern(text, searchMatch, isCaseSensitive);
  }

  private void bindAction(JComponent parent, final JComponent component) {
    FindAction findAction = (FindAction) FindAction.get(FindAction.class);
    Object mapKey = findAction.getActionMapKey();
    parent.getActionMap().put(mapKey, new AbstractAction () {
      public void actionPerformed(ActionEvent event) {
        component.setVisible(true);
      }
    });
    InputMap keys =
      parent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    KeyStroke key = (KeyStroke) findAction.getValue(Action.ACCELERATOR_KEY);

    if (key == null) {
      key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
    }
    keys.put(key, mapKey);
  }

  private SearchEngine getEngine(Object source) {
    for (SearchEngine engine : myEngines) {
      if (engine.accepts(source)) {
        return engine;
      }
    }
    return null;
  }

  private List<SearchEngine> myEngines;
}
