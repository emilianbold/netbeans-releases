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
package org.netbeans.modules.bpel.search.impl.ui;

import java.util.Collections;

import org.netbeans.modules.xml.xam.ui.search.SearchControlPanel;
import org.netbeans.modules.bpel.search.api.SearchElement;
import org.netbeans.modules.bpel.search.spi.SearchEngine;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.20
 */
public final class Find extends SearchControlPanel {

  /**{@inheritDoc}*/
  public Find(SearchEngine engine, Object source) {
    super();
    setProviders(Collections.singleton(new Provider(engine, source)));
  }

  protected void showSearchResult(Object object) {
    if ( !(object instanceof SearchElement)) {
      return;
    }
    SearchElement element = (SearchElement) object;
    element.selectOnDiagram();
  }
}
