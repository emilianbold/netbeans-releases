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
import java.awt.peer.TextComponentPeer;

import javax.swing.plaf.basic.BasicGraphicsUtils;

/**
 *
 * @author Tran Duc Trung
 */

class FakeTextComponentPeer extends FakeComponentPeer
    implements TextComponentPeer
{
  private String _text;
  
  FakeTextComponentPeer(TextComponent target) {
    super(target);
  }

  Component createDelegate() {
    return new Delegate();
  }

  public boolean isFocusTraversable() {
    return true;
  }

  public void setEditable(boolean editable) {
    repaint();
  }

  public String getText() {
    return _text;
  }

  public void setText(String text) {
    _text = text;
    repaint();
  }

  public int getSelectionStart() {
    return -1;
  }

  public int getSelectionEnd() {
    return -1;
  }

  public void select(int selStart, int selEnd) {
    // noop
  }

  public void setCaretPosition(int pos) {
    //noop
  }

  public int getCaretPosition() {
    return 0;
  }

  public int getIndexAtPoint(int x, int y) {
    return 0;
  }
  
  public Rectangle getCharacterBounds(int i) {
    return null;
  }
    
  public long filterEvents(long mask) {
    return 0;
  }
  
  //
  //
  //

  protected class Delegate extends Component
  {
    public void paint(Graphics g) {
      Dimension sz = _target.getSize();

      Color c = getBackground();
      if (c == null)
        c = Color.white;//SystemColor.text;

      g.setColor(c);
      g.fillRect(0, 0, sz.width, sz.height);
      
      BasicGraphicsUtils.drawLoweredBezel(g, 0, 0, sz.width, sz.height, 
                                          SystemColor.controlShadow,
                                          SystemColor.controlDkShadow,
                                          SystemColor.controlHighlight,
                                          SystemColor.controlLtHighlight);
    }
  }
}
