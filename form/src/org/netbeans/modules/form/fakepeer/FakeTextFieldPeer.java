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
class FakeTextFieldPeer extends FakeTextComponentPeer
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
            String text = ((TextField)_target).getText();

            FontMetrics fm = this.getFontMetrics(this.getFont());
            int w = fm.stringWidth(text);
            int h = fm.getHeight();

            return new Dimension(w > 92 ? 100 : w+8, h + 4);
        }
    }
}
