/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */
package org.netbeans.lib.terminalemulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;

/**
 * @author Ilia Gromov
 */
public class Highlight {

    private final Term term;
    private final State state;

    private Extent[] highlightExtents = new Extent[0];

    Highlight(Term term, State state) {
        this.term = term;
        this.state = state;
    }

    public Extent[] getHighlightExtents() {
        Extent[] copy = new Extent[highlightExtents.length];
        for (int i = 0; i < highlightExtents.length; i++) {
            Extent x = highlightExtents[i];
            copy[i] = new Extent(x.begin, x.end);
        }
        return copy;
    }

    public void setHighlightExtents(Extent[] highlightExtents) {
        this.highlightExtents = Arrays.copyOf(highlightExtents, highlightExtents.length);
        for (Extent extent : highlightExtents) {
            extent.order();
        }
    }

    // properties:
    private Color color = new Color(230, 230, 255);  // swing color

    void setColor(Color color) {
        this.color = color;
    }

    Color getColor() {
        return color;
    }

    private Color xor_color = Color.white;

    void setXORColor(Color color) {
        xor_color = color;
    }

    Color getXORColor() {
        return xor_color;
    }

    /**
     * Select inside one line Rows and columns are in absolute coords.
     */
    private void paint(Graphics g, int row, int bcol, int ecol) {

        // Instead of doing this SHOULD clip the Extent to what's in view
        // Row is outside the view
        if (row < state.firstx) {
            return;
        }
        if (row > state.firstx + state.rows) {
            return;
        }

        // Construct the rectangle we're going to paint
        BCoord begin = new BCoord(row, bcol);
        BCoord end = new BCoord(row, ecol);

        begin = term.toViewCoord(begin);
        end = term.toViewCoord(end);

        //Hotfix for issue 40189
        if (begin == null || end == null) {
            return;
        }

        int lw;		// width of last character in selection
        Line l = term.buf().lineAt(row);
        lw = l.width(term.metrics(), ecol);

        Point pbegin = term.toPixel(begin);
        Point pend = term.toPixel(end);
        pend.y += term.metrics().height;
        pend.x += term.metrics().width * lw;	// xterm actually doesn't do this

        Dimension dim = new Dimension(pend.x - pbegin.x,
                pend.y - pbegin.y);
        Rectangle rect = new Rectangle(pbegin, dim);

        if (term.isSelectionXOR()) {
            g.setXORMode(xor_color);
        } else {
            g.setColor(color);
        }

        g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    void paint(final Graphics g) {
        /*
	 * Paint the selection.
         */

        for (Extent x : highlightExtents) {
            if (x == null) {
                continue;
            }
            x.order();

            // DEBUG System.out.println("Sel.paint extent: " + x);	// NOI18N
            term.visitLines(x.begin, x.end, true, new LineVisitor() {
                public boolean visit(Line l, int row, int bcol, int ecol) {
                    paint(g, row, bcol, ecol);
                    return true;
                }
            });
        }
    }

    public void clear() {
        highlightExtents = new Extent[0];
    }
}
