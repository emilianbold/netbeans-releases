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
 
/* $Id$ */

package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.TextFieldPeer;

/**
 *
 * @author Tran Duc Trung
 */

class FakeTextFieldPeer extends FakeTextComponentPeer implements TextFieldPeer
{
    FakeTextFieldPeer(TextField target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    public void setEchoChar(char echoChar) {
    }

    public Dimension getPreferredSize(int columns) {
        return _delegate.getMinimumSize(); //new Dimension(100, 20);
    }

    public Dimension getMinimumSize(int columns) {
        return _delegate.getMinimumSize(); //new Dimension(100, 20);
    }

    public void setEchoCharacter(char c) {
        setEchoChar(c);
    }

    public Dimension preferredSize(int cols) {
        return getPreferredSize(cols);
    }

    public Dimension minimumSize(int cols) {
        return getMinimumSize(cols);
    }

    //
    //
    //

    private class Delegate extends FakeTextComponentPeer.Delegate
    {
        public void paint(Graphics g) {
            super.paint(g);

            TextField target =(TextField) _target;
            String text = target.getText();

            if (text != null) { // draw the text
                String textOut = text.substring(target.getCaretPosition());
//                Dimension sz = target.getSize();
                g.setFont(target.getFont());

                FontMetrics fm = g.getFontMetrics();
                int h = fm.getHeight() - fm.getDescent();
                g.drawString(textOut, 4, 1 + h); //(sz.height - h) / 2 + h -2);
            }
        }

        public Dimension getMinimumSize() {
            String text =((TextField)_target).getText();

            FontMetrics fm = this.getFontMetrics(_target.getFont());
            int w = fm.stringWidth(text);
            int h = fm.getHeight();

            return new Dimension(w > 92 ? 100 : w+8, h + 4);
       }
    }
}
