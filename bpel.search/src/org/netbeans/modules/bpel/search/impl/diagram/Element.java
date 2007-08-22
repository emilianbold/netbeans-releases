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

import org.netbeans.modules.bpel.editors.api.DiagramElement;
import org.netbeans.modules.xml.search.api.SearchElement;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.17
 */
final class Element extends SearchElement.Adapter {

  Element(DiagramElement element) {
    super(element.getText(), element.getText(), null, null);
    myElement = element;
    highlight(true);
  }

  /**{@inheritDoc}*/
  @Override
  public void gotoSource()
  {
    myElement.gotoSource();
  }

  /**{@inheritDoc}*/
  @Override
  public void select()
  {
    myElement.select();
  }

  /**{@inheritDoc}*/
  @Override
  public void highlight(boolean highlighted)
  {
    myElement.highlight(highlighted);
  }

  private DiagramElement myElement;
}
