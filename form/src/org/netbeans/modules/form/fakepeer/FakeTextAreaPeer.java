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


package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.TextAreaPeer;

/**
 *
 * @author Tran Duc Trung
 */

class FakeTextAreaPeer extends FakeTextComponentPeer implements TextAreaPeer
{
    FakeTextAreaPeer(TextArea target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    public void insert(String text, int pos) {
    }

    public void replaceRange(String text, int start, int end) {
    }

    public Dimension getPreferredSize(int rows, int columns) {
        return new Dimension(100, 80);
    }

    public Dimension getMinimumSize(int rows, int columns) {
        return new Dimension(100, 80);
    }

    public void insertText(String txt, int pos) {
        insert(txt, pos);
    }

    public void replaceText(String txt, int start, int end) {
        replaceRange(txt, start, end);
    }

    public Dimension preferredSize(int rows, int cols) {
        return getPreferredSize(rows, cols);
    }

    public Dimension minimumSize(int rows, int cols) {
        return getMinimumSize(rows, cols);
    }

    //
    //
    //

    private class Delegate extends FakeTextComponentPeer.Delegate
    {
        public void paint(Graphics g) {
            super.paint(g);

            TextArea target = (TextArea) _target;
            Dimension sz = target.getSize();
            int w = sz.width;
            int h = sz.height;
            String text = target.getText();

            if (text != null) {
                g.setFont(target.getFont());
                g.setColor(target.getForeground());
                
                FontMetrics fm = g.getFontMetrics();
                int th = fm.getHeight();
                int ty = th;
                int i = target.getCaretPosition();
                int len = text.length();
                
                StringBuffer buf = new StringBuffer(len);

                for ( ; i < len; i++) {
                    char ch = text.charAt(i);
                    if (ch != '\n' && ch != '\r') buf.append(ch);
                    else if (ch == '\n') {
                        g.drawString(buf.toString(),4,ty);
                        if (ty > h)
                            break;
                        ty += th;
                        buf.delete(0,buf.length());
                    }
                }
                g.drawString(buf.toString(), 4, ty);
            }

            if (sz.width > FakePeerUtils.SCROLL_W*2 && 
                sz.height > FakePeerUtils.SCROLL_H*2) {
                g.setColor(SystemColor.controlHighlight);
                FakePeerUtils.drawScrollbar(g,2,h-FakePeerUtils.SCROLL_H-2,
                                            w-4-FakePeerUtils.SCROLL_W,FakePeerUtils.SCROLL_H,
                                            Scrollbar.HORIZONTAL,false,true,0,0,0);

                g.setColor(SystemColor.controlHighlight);
                FakePeerUtils.drawScrollbar(g,w-FakePeerUtils.SCROLL_W-2,2,
                                            FakePeerUtils.SCROLL_W,h-4-FakePeerUtils.SCROLL_H,
                                            Scrollbar.VERTICAL,false,true,0,0,0);

                g.setColor(SystemColor.controlHighlight);
                g.fillRect(w-FakePeerUtils.SCROLL_W-2,h-FakePeerUtils.SCROLL_H-2,
                           FakePeerUtils.SCROLL_W,FakePeerUtils.SCROLL_H);
            }
        }

        public Dimension getMinimumSize() {
            TextArea target = (TextArea)_target;
            return FakeTextAreaPeer.this.getMinimumSize(target.getColumns(),target.getRows());
        }
    }
}
