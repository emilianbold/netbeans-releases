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


package org.netbeans.modules.form.fakepeer;

import java.awt.*;

/**
 *
 * @author Tran Duc Trung
 */
class FakeTextComponentPeer extends FakeComponentPeer
{
    private String _text;
    private int _caretPosition = 0;
    private int selStart = -1;
    private int selEnd = -1;

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
        return selStart;
    }

    public int getSelectionEnd() {
        return selEnd;
    }

    public void select(int selStart, int selEnd) {
        this.selStart = selStart;
        this.selEnd = selEnd;
    }

    public void setCaretPosition(int pos) {
        if (pos == 0 || (_text != null && _text.length() > pos))
            _caretPosition = pos;
    }

    public int getCaretPosition() {
        return _caretPosition;
    }

    // JDK 1.3
    public int getIndexAtPoint(int x, int y) {
        return 0;
    }

    // JDK 1.3
    public Rectangle getCharacterBounds(int i) {
        return null;
    }

    // JDK 1.3
    public long filterEvents(long mask) {
        return 0;
    }

    // JDK 1.5
    public java.awt.im.InputMethodRequests getInputMethodRequests() {
        return null;
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

            Rectangle r = g.getClipBounds();
            if (r.x < 1) r.x = 1;
            if (r.y < 1) r.y = 1;
            if (r.width > w-3) r.width = w - 3;
            if (r.height > h-3) r.height = h - 3;
            g.setClip(r);
//            g.setClip(1,1,w-3,h-3);

            if (_target.isEnabled()) {
                g.setColor(_target.getForeground());
            }
            else {
                g.setColor(SystemColor.controlShadow);
            }
        }
    }
}
