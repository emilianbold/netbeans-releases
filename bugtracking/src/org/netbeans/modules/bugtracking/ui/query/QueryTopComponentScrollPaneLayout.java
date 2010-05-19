/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.query;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.Border;

/**
 * Based
 */
class QueryTopComponentScrollPaneLayout extends ScrollPaneLayout {


    /**
     * The preferred size of a <code>ScrollPane</code> is the size of the insets,
     * plus the preferred size of the viewport,  plus the preferred size
     * of the scrollbars that will appear given the current view
     * and the current scrollbar displayPolicies.
     *
     * @param parent the <code>Container</code> that will be laid out
     * @return a <code>Dimension</code> object specifying the preferred size of the
     *         viewport and any scrollbars
     * @see ViewportLayout
     * @see LayoutManager
     */
    @Override
    public Dimension preferredLayoutSize(Container parent)
    {
	/* Sync the (now obsolete) policy fields with the
	 * JScrollPane.
	 */
	JScrollPane scrollPane = (JScrollPane) parent;
	vsbPolicy = scrollPane.getVerticalScrollBarPolicy();
	hsbPolicy = scrollPane.getHorizontalScrollBarPolicy();

	Insets insets = parent.getInsets();
	int prefWidth = insets.left + insets.right;
	int prefHeight = insets.top + insets.bottom;

	/*
         * Note that viewport.getViewSize() is equivalent to
	 * viewport.getView().getPreferredSize() modulo a null
	 * view or a view whose size was explicitly set.
	 */

	Dimension extentSize = null;
	Dimension viewSize = null;

	if (viewport != null) {
	    extentSize = viewport.getPreferredSize();
	    viewSize = viewport.getViewSize();
	}

	/* If there's a viewport add its preferredSize. */

	if (extentSize != null) {
	    prefWidth += extentSize.width;
	    prefHeight += extentSize.height;
	}

	/* If there's a JScrollPane.viewportBorder, add its insets. */

	Border viewportBorder = scrollPane.getViewportBorder();
	if (viewportBorder != null) {
	    Insets vpbInsets = viewportBorder.getBorderInsets(parent);
	    prefWidth += vpbInsets.left + vpbInsets.right;
	    prefHeight += vpbInsets.top + vpbInsets.bottom;
	}

	/*
         * If a scrollbar is going to appear, factor its preferred size in.
	 * If the scrollbars policy is AS_NEEDED, this can be a little
	 * tricky:
	 *
	 * - Assuming that a scrollbar hasn't been disabled by the
	 * previous constraint, we need to decide if the scrollbar is going
	 * to appear to correctly compute the JScrollPanes preferred size.
	 * To do this we compare the preferredSize of the viewport (the
	 * extentSize) to the preferredSize of the view.  Although we're
	 * not responsible for laying out the view we'll assume that the
	 * JViewport will always give it its preferredSize.
	 */

	if ((vsb != null)
                && (viewSize != null) && (extentSize != null)
                && (viewSize.height > extentSize.height)) {
            prefWidth += vsb.getPreferredSize().width;
	}

	if ((hsb != null) 
	        && (viewSize != null) && (extentSize != null)
		&& (viewSize.width > extentSize.width)) {
            prefHeight += hsb.getPreferredSize().height;
	}

	return new Dimension(prefWidth, prefHeight);
    }


    /**
     * Lays out the scrollpane. The positioning of components depends on
     * the following constraints:
     * <ul>
     * <li> If a vertical scrollbar is needed, i.e. if the viewport's extent
     * height is smaller than its view height or if the <code>displayPolicy</code>
     * is ALWAYS, it's treated like the row header with respect to its
     * dimensions and is made visible.
     *
     * <li> If a horizontal scrollbar is needed, it is treated like the
     * column header.
     *
     * <li> If the scrollpane has a non-<code>null</code>
     * <code>viewportBorder</code>, then space is allocated for that.
     *
     * <li> The viewport gets the space available after accounting for
     * the previous constraints.
     * </ul>
     *
     * @param parent the <code>Container</code> to lay out
     */
    @Override
    public void layoutContainer(Container parent) {

	/* Sync the (now obsolete) policy fields with the JScrollPane. */
	JScrollPane scrollPane = (JScrollPane) parent;
	vsbPolicy = scrollPane.getVerticalScrollBarPolicy();
	hsbPolicy = scrollPane.getHorizontalScrollBarPolicy();

	Rectangle availR = scrollPane.getBounds();
	availR.x = availR.y = 0;

	Insets insets = parent.getInsets();
	availR.x = insets.left;
	availR.y = insets.top;
	availR.width -= insets.left + insets.right;
	availR.height -= insets.top + insets.bottom;

	/*
         * If there's a JScrollPane.viewportBorder, remove the
	 * space it occupies for availR.
	 */

	Border viewportBorder = scrollPane.getViewportBorder();
	Insets vpbInsets;
	if (viewportBorder != null) {
	    vpbInsets = viewportBorder.getBorderInsets(parent);
	    availR.x += vpbInsets.left;
	    availR.y += vpbInsets.top;
	    availR.width -= vpbInsets.left + vpbInsets.right;
	    availR.height -= vpbInsets.top + vpbInsets.bottom;
	} else {
	    vpbInsets = new Insets(0,0,0,0);
	}


	/*
         * At this point availR is the space available for the viewport
	 * and scrollbars.
	 *
         * We'll decide about putting up scrollbars by comparing the
         * viewport views preferred size with the viewports extent
	 * size (generally just its size).  Using the preferredSize is
	 * reasonable because layout proceeds top down - so we expect
	 * the viewport to be laid out next.  And we assume that the
	 * viewports layout manager will give the view it's preferred
	 * size.  One exception to this is when the view implements
	 * Scrollable and Scrollable.getViewTracksViewport{Width,Height}
	 * methods return true.  If the view is tracking the viewports
	 * width we don't bother with a horizontal scrollbar, similarly
	 * if view.getViewTracksViewport(Height) is true we don't bother
	 * with a vertical scrollbar.
	 */

	Component view = (viewport != null) ? viewport.getView() : null;
        ViewportWidthAwarePanel viewportWidthAwareView
                                = (view instanceof ViewportWidthAwarePanel)
                                  ? (ViewportWidthAwarePanel) view
                                  : null;
	Dimension extentSize = (viewport != null)
                               ? viewport.toViewCoordinates(availR.getSize())
	                       : new Dimension(0,0);

	Dimension viewPrefSize;
        if (view != null) {
            if (viewportWidthAwareView != null) {
                viewportWidthAwareView.setAvailableWidth(extentSize.width);
            }
            viewPrefSize = view.getPreferredSize();
        } else {
            viewPrefSize = new Dimension(0,0);
        }

        boolean isEmpty = (availR.width < 0 || availR.height < 0);

	/*
         * If there's a vertical scrollbar and we need one, allocate
	 * space for it (we'll make it visible later). A vertical
	 * scrollbar is considered to be fixed width, arbitrary height.
	 */

	Rectangle vsbR = new Rectangle(0, availR.y - vpbInsets.top, 0, 0);

	boolean vsbNeeded;
        if (isEmpty) {
            vsbNeeded = false;
	} else {  // vsbPolicy == VERTICAL_SCROLLBAR_AS_NEEDED
	    vsbNeeded = viewPrefSize.height > extentSize.height;
	}


	if ((vsb != null) && vsbNeeded) {
	    adjustForVSB(availR, vsbR, vpbInsets);
	    extentSize = viewport.toViewCoordinates(availR.getSize());

            if (viewportWidthAwareView != null) {
                viewportWidthAwareView.setAvailableWidth(extentSize.width);
                viewPrefSize = view.getPreferredSize();
            }
	}

	/*
         * If there's a horizontal scrollbar and we need one, allocate
	 * space for it (we'll make it visible later). A horizontal
	 * scrollbar is considered to be fixed height, arbitrary width.
	 */

	Rectangle hsbR = new Rectangle(availR.x - vpbInsets.left, 0, 0, 0);
	boolean hsbNeeded;
        if (isEmpty) {
            hsbNeeded = false;
	} else {  // hsbPolicy == HORIZONTAL_SCROLLBAR_AS_NEEDED
	    hsbNeeded = viewPrefSize.width > extentSize.width;
	}

	if ((hsb != null) && hsbNeeded) {
	    adjustForHSB(availR, hsbR, vpbInsets);

	    /*
             * If we added the horizontal scrollbar then we've implicitly
	     * reduced  the vertical space available to the viewport.
	     * As a consequence we may have to add the vertical scrollbar,
	     * if that hasn't been done so already.  Of course we
	     * don't bother with any of this if the vsbPolicy is NEVER.
	     */
	    if ((vsb != null) && !vsbNeeded) {

		extentSize = viewport.toViewCoordinates(availR.getSize());
		vsbNeeded = viewPrefSize.height > extentSize.height;

		if (vsbNeeded) {
		    adjustForVSB(availR, vsbR, vpbInsets);

                    if (viewportWidthAwareView != null) {
                        extentSize = viewport.toViewCoordinates(availR.getSize());
                        viewportWidthAwareView.setAvailableWidth(extentSize.width);
                    }
		}
	    }
	}

	/*
         * Set the size of the viewport first, and then recheck the Scrollable
	 * methods. Some components base their return values for the Scrollable
	 * methods on the size of the Viewport, so that if we don't
	 * ask after resetting the bounds we may have gotten the wrong
	 * answer.
	 */

	if (viewport != null) {
	    viewport.setBounds(availR);
	}

	/*
         * We now have the final size of the viewport: availR.
	 * Now fixup the scrollbar widths/heights.
	 */
	vsbR.height = availR.height + vpbInsets.top + vpbInsets.bottom;
	hsbR.width = availR.width + vpbInsets.left + vpbInsets.right;

	/*
         * Set the bounds of the remaining components.  The scrollbars
	 * are made invisible if they're not needed.
	 */

	if (vsb != null) {
	    if (vsbNeeded) {
		vsb.setVisible(true);
		vsb.setBounds(vsbR);
	    }
	    else {
		vsb.setVisible(false);
	    }
	}

	if (hsb != null) {
	    if (hsbNeeded) {
		hsb.setVisible(true);
		hsb.setBounds(hsbR);
	    }
	    else {
		hsb.setVisible(false);
	    }
	}
    }

    /**
     * Adjusts the <code>Rectangle</code> <code>available</code> as
     * the vertical scrollbar is needed (<code>wantsVSB</code>).
     * The location of the vsb is updated in <code>vsbR</code>, and
     * the viewport border insets (<code>vpbInsets</code>) are used to offset
     * the vsb.
     */
    private void adjustForVSB(Rectangle available,
			      Rectangle vsbR, Insets vpbInsets) {
        int vsbWidth = Math.max(0, Math.min(vsb.getPreferredSize().width,
                                            available.width));

        available.width -= vsbWidth;
        vsbR.width = vsbWidth;
        vsbR.x = available.x + available.width + vpbInsets.right;
    }

    /**
     * Adjusts the <code>Rectangle</code> <code>available</code> as
     * the horizontal scrollbar is needed.
     * The location of the hsb is updated in <code>hsbR</code>, and
     * the viewport border insets (<code>vpbInsets</code>) are used to offset
     * the hsb.
     */
    private void adjustForHSB(Rectangle available,
			      Rectangle hsbR, Insets vpbInsets) {
        int hsbHeight = Math.max(0, Math.min(available.height,
                                          hsb.getPreferredSize().height));

        available.height -= hsbHeight;
        hsbR.y = available.y + available.height + vpbInsets.bottom;
        hsbR.height = hsbHeight;
    }

}
