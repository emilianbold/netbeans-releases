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
import java.awt.peer.ListPeer;

/**
 *
 * @author Tran Duc Trung
 */

class FakeListPeer extends FakeComponentPeer implements ListPeer
{
    FakeListPeer(List target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    public int[] getSelectedIndexes() {
        return new int[0];
    }

    public void add(String item, int index) {
    }

    public void delItems(int start, int end) {
    }

    public void removeAll() {
    }

    public void select(int index) {
    }

    public void deselect(int index) {
    }

    public void makeVisible(int index) {
    }

    public void setMultipleMode(boolean b) {
    }

    public Dimension getPreferredSize(int rows) {
        return new Dimension(40, 80);
    }

    public Dimension getMinimumSize(int rows) {
        return new Dimension(40, 80);
    }

    public void addItem(String item, int index) {
        add(item, index);
    }

    public void clear() {
        removeAll();
    }

    public void setMultipleSelections(boolean v) {
        setMultipleMode(v);
    }

    public Dimension preferredSize(int rows) {
        return getPreferredSize(rows);
    }

    public Dimension minimumSize(int rows) {
        return getMinimumSize(rows);
    }

    //
    //
    //

    private class Delegate extends Component 
    {
        public void paint(Graphics g) {
            List target =(List) _target;

            Dimension sz = target.getSize();
            int w = sz.width,
                h = sz.height;

            // background & border
            Color c = getBackground();
            if (c == null)
                c = SystemColor.window; // white
            g.setColor(c);
            FakePeerUtils.drawLoweredBox(g,0,0,w,h);

            int n = target.getItemCount();
            if (n > 0) { // draw the items of the list...
                c = getForeground();
                if (c == null)
                    c = SystemColor.controlText;
                g.setColor(c);
                g.setFont(target.getFont());
                g.setClip(1,1,w-5,h-4);

                FontMetrics fm = g.getFontMetrics();
                int th = fm.getHeight(),
                    ty = th+2;

                for (int i=0; i < n; i++) {
                    g.drawString(target.getItem(i), 4, ty);
                    if (ty > h) break;
                    ty += th;
                }
            }
        }

        public Dimension getMinimumSize() {
            return FakeListPeer.this.getMinimumSize(1);
        }
    }
}
