/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.swing.layouts;

import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * A layout manager which gets data from a shared ancestor component
 * about grid column positions, so multiple LDPLayout panels can be
 * placed on a parent panel and their columns will align, even if the
 * panels are nested one inside another.
 * <p>
 * To use, implement SharedLayoutData on a JPanel (or use the convenience
 * class SharedLayoutParentPanel).  Add child panels which use LDPLayout
 * as you wish to that panel (easiest is to use the convenience class
 * SharedLayoutPanel).  Even if you add child LDPLayout panels within
 * panels within panels, all components in them will have the same
 * columnar alignment.
 * <p>
 * This is typically useful for components that, for example, display a row
 * of components and below that display a customizer component supplied
 * by some other object.  It is more visually pleasing if the components
 * align, even though the component with the controls knows nothing about
 * the customizer component.  In this way components that were not designed
 * together can visually appear as if they were.
 *
 * Borrowed from http://imagine.dev.java.net
 * @author Tim Boudreau
 */
public final class LDPLayout implements LayoutManager {

    private final int gap;
    private final boolean fillHorizontal;
    private final boolean fillVertical;
    private final boolean stretch;

    /**
     * Create a new layout.
     *
     * @param gap Horizontal gap between components.  If -1 is passed, an
     * OS-appropriate spacing will be used.  Value must be a 0, a positive
     * integer or -1.
     * @param fillHorizontal If true, each component will use the full available
     * height.  If false, each component will be set to its preferred height and
     * centered.
     * @param fillVertical If true, each component will completely fill its
     * column - it will be as wide as the preferred size of the widest component
     * in its column.  If false, each component will start at the beginning of
     * its column, but only be as wide as its preferred size
     * @param stretch If true, the layout will try to use the full available
     * width of the parent by redistributing extra horizontal space evenly
     * between components which are not labels or buttons.  If false, the
     * component will not use the full width of the parent if it does not need
     * to
     */
    public LDPLayout(int gap, boolean fillHorizontal, boolean fillVertical, boolean stretch) {
        this.gap = gap == 0 ? defaultGap() : gap;
        if (gap < -1) {
            throw new IllegalArgumentException("Negative gap " + gap); //NOI18N
        }
        this.fillHorizontal = fillHorizontal;
        this.fillVertical = fillVertical;
        this.stretch = stretch;
    }

    /**
     * Equivalent to LDPLayout (-1, true, false, true)
     */
    public LDPLayout() {
        this(defaultGap(), true, false, true);
    }

    private static int defaultGap() {
        return Utilities.getOperatingSystem() == Utilities.OS_MAC ? 12 : 5;
    }

    /**
     * Returns the column position at this index in the parent component -
     * i.e. where the component at index <code>index</code> would should
     * be placed if the preferred width of all preceding components is
     * honored.
     * @param parent A container
     * @param index the index of the component
     * @return An x offset in pixels, taking into account the parent's insets.
     * If the passed index is > the number of child components of the parent,
     * returns the position of the last component
     */
    public int getColumnPosition(Container parent, int index) {
        Insets ins = parent.getInsets();
        Component[] comps = parent.getComponents();
        int x = ins.left + ins.right;
        for (int i = 0; i < comps.length; i++) {
            if (i == index) {
                break;
            }
            x += comps[i].getPreferredSize().width + gap;
        }
        return x;
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension result = layoutSize(parent, false);
        if (stretch) {
            int width = parent.getWidth();
            Insets ins = parent.getInsets();
            width -= ins.left + ins.right;
            if (result.width < width) {
                result.width = width;
            }
        }
        return result;
    }

    public Dimension minimumLayoutSize(Container parent) {
        return layoutSize(parent, true);
    }

    private Dimension layoutSize(Container parent, boolean isMin) {
        Insets ins = parent.getInsets();
        Component[] comps = parent.getComponents();
        int x = ins.left + ins.right;
        int y = ins.top + ins.bottom;
        SharedLayoutData data = (SharedLayoutData) SwingUtilities.getAncestorOfClass(SharedLayoutData.class, parent);
        if (data == null) {
            for (Component c : comps) {
                Dimension d = isMin ? c.getMinimumSize() : c.getPreferredSize();
                x += d.width + gap;
                y = Math.max(y, d.height);
            }
        } else {
            for (int i = 0; i < comps.length; i++) {
                int colpos = data.xPosForColumn(i);
                Dimension d = comps[i].getPreferredSize();
                x = colpos + d.width + gap;
            }
        }
        y = Math.max(30, y);
        return new Dimension(x, y);
    }

    private Rectangle[] getBoundingBoxes(Container parent, Component[] comps) {
        Insets ins = parent.getInsets();
        Rectangle[] result = new Rectangle[comps.length];
        int y = ins.top;
        SharedLayoutData data = (SharedLayoutData) SwingUtilities.getAncestorOfClass(SharedLayoutData.class, parent);
        int h = 0;
        for (Component c : comps) {
            Dimension d = c.getPreferredSize();
            h = Math.max(h, d.height);
        }
        h = Math.max(30, h);
        int nextLoc = ins.left;
        for (int i = 0; i < comps.length; i++) {
            int colpos = data == null ? nextLoc : data.xPosForColumn(i);
            Dimension d = comps[i].getPreferredSize();
            Component c = comps[i];
            if (!fillHorizontal && !fillVertical) {
                int yy = d.height == h ? 0 : (h / 2) - (d.height / 2);
                result[i] = new Rectangle(colpos, y + yy, d.width, d.height);
            } else if (fillHorizontal && !fillVertical) {
                int yy = d.height == h ? 0 : (h / 2) - (d.height / 2);
                int nextPos = data.xPosForColumn(i + 1);
                int ww;
                if (nextPos == colpos) {
                    //last column
                    ww = (parent.getWidth() - ins.right) - colpos;
                } else {
                    ww = (nextPos - gap) - colpos;
                }
                result[i] = new Rectangle(colpos, y + yy, ww, d.height);
            } else if (!fillHorizontal && fillVertical) {
                result[i] = new Rectangle(colpos, y, d.width, h);
            } else if (fillVertical) {
                int ww;
                int nextPos = data.xPosForColumn(i + 1);
                if (nextPos == colpos) {
                    //last column
                    ww = (parent.getWidth() - ins.right) - colpos;
                } else {
                    ww = (nextPos - gap) - colpos;
                }
                result[i] = new Rectangle(colpos, y, ww, h);
            }
            nextLoc += result[i].width + gap;
        }
        return result;
    }

    public void layoutContainer(Container parent) {
        Component[] c = parent.getComponents();
        Rectangle[] r = getBoundingBoxes(parent, c);
        if (stretch) {
            //Find out how much spare width we have and redistribute it to
            //"stretchy" components
            Insets ins = parent.getInsets();
            int usedWidth = (r[r.length - 1].x + r[r.length - 1].width) - ins.left;
            int stretchyCount = 0;
            int usableWidth = parent.getWidth() - (ins.left + ins.right);
            if (usableWidth > usedWidth) {
                for (Component aC : c) {
                    if (isStretchy(aC)) {
                        stretchyCount++;
                    }
                }
                if (stretchyCount > 0) {
                    int redistributableSpace = usableWidth - usedWidth;
                    int amtPerComponent = redistributableSpace / stretchyCount;
                    int amtAdded = 0;
                    for (int i = 0; i < r.length; i++) {
                        r[i].x += amtAdded;
                        if (isStretchy(c[i])) {
                            r[i].width += amtPerComponent;
                            amtAdded += amtPerComponent;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < c.length; i++) {
            c[i].setBounds(r[i]);
        }
    }

    private boolean isStretchy(Component c) {
        boolean notResult = c instanceof JLabel || c instanceof AbstractButton ||
                c instanceof Label || c instanceof Button;
        return !notResult;
    }
}
