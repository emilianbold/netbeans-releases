/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/* $Id$ */

package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.TextComponentPeer;


/**
 *
 * @author Tran Duc Trung
 */

class FakeTextComponentPeer extends FakeComponentPeer
    implements TextComponentPeer
{
    private String _text;
    private int _caretPosition = 0;

    FakeTextComponentPeer(TextComponent target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    void initDelegate() {
        _text = ((TextComponent)_target).getText();
        super.initDelegate();
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
        if (pos == 0 || (_text != null && _text.length() > pos))
            _caretPosition = pos;
    }

    public int getCaretPosition() {
        return _caretPosition;
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
        Delegate() {
            this.setBackground(SystemColor.window);
            this.setForeground(SystemColor.windowText);
        }
        
        public void paint(Graphics g) {
            Dimension sz = _target.getSize();
            int w = sz.width;
            int h = sz.height;

            g.setColor(_target.getBackground());
            FakePeerUtils.drawLoweredBox(g,0,0,w,h);

            g.setClip(1,1,w-3,h-3);

            if (_target.isEnabled()) {
                g.setColor(_target.getForeground());
            }
            else {
                g.setColor(SystemColor.controlShadow);
            }
        }
    }
}
