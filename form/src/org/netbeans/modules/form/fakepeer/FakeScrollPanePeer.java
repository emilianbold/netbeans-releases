/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.ScrollPanePeer;

/**
 *
 * @author Tran Duc Trung
 */

class FakeScrollPanePeer extends FakeContainerPeer implements ScrollPanePeer
{
  FakeScrollPanePeer(ScrollPane target) {
    super(target);
  }

  Component createDelegate() {
    return new Delegate();
  }

  public int getHScrollbarHeight() {
    return 10;
  }
  
  public int getVScrollbarWidth() {
    return 10;
  }
  
  public void setScrollPosition(int x, int y) {}
  public void childResized(int w, int h) {}
  public void setUnitIncrement(Adjustable adj, int u) {}
  public void setValue(Adjustable adj, int v) {}

}
