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
class FakeListPeer extends FakeComponentPeer
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
        Delegate() {
            this.setBackground(SystemColor.window);
            this.setForeground(SystemColor.windowText);
        }
        
        public void paint(Graphics g) {
            List target =(List) _target;
            Dimension sz = target.getSize();
            int w = sz.width;
            int h = sz.height;

            g.setColor(target.getBackground());
            FakePeerUtils.drawLoweredBox(g,0,0,w,h);

            int n = target.getItemCount();
            if (n <= 0)
                return;

            if (target.isEnabled()) {
                g.setColor(target.getForeground());
            }
            else {
                g.setColor(SystemColor.controlShadow);
            }
            
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

        public Dimension getMinimumSize() {
            return FakeListPeer.this.getMinimumSize(1);
        }
    }
}
