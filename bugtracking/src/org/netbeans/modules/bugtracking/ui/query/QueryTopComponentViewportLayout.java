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
import java.awt.LayoutManager;
import java.awt.Point;
import javax.swing.JViewport;
import javax.swing.ViewportLayout;

/**
 *
 * @author Marian Petras
 */
public class QueryTopComponentViewportLayout extends ViewportLayout {

    static LayoutManager SHARED_INSTANCE = new QueryTopComponentViewportLayout();

    @Override
    public void layoutContainer(Container parent) {
	JViewport vp = (JViewport) parent;
	Component view = vp.getView();

	if (view == null) {
	    return;
	}

	/*
         * All of the dimensions below are in view coordinates, except
	 * vpSize which we're converting.
	 */

	Dimension vpSize = vp.getSize();
        if (view instanceof ViewportWidthAwarePanel) {
            ((ViewportWidthAwarePanel) view).setAvailableWidth(vpSize.width);
        }
	Dimension viewPrefSize = view.getPreferredSize();
	Dimension extentSize = vp.toViewCoordinates(vpSize);
	Dimension viewSize = new Dimension(viewPrefSize);

	Point viewPosition = vp.getViewPosition();

	/*
         * If the new viewport size would leave empty space to the
	 * right of the view, right justify the view or left justify
	 * the view when the width of the view is smaller than the
	 * container.
	 */
        if ((viewPosition.x + extentSize.width) > viewSize.width) {
            viewPosition.x = Math.max(0, viewSize.width - extentSize.width);
        }

	/*
         * If the new viewport size would leave empty space below the
	 * view, bottom justify the view or top justify the view when
	 * the height of the view is smaller than the container.
	 */
	if ((viewPosition.y + extentSize.height) > viewSize.height) {
	    viewPosition.y = Math.max(0, viewSize.height - extentSize.height);
	}

        /*
	 * If the origin of the view is showing and the viewport is
	 * bigger than the views preferred size, then make the view
	 * the same size as the viewport.
	 */
        if ((viewPosition.x == 0) && (vpSize.width > viewPrefSize.width)) {
            viewSize.width = vpSize.width;
        }
        if ((viewPosition.y == 0) && (vpSize.height > viewPrefSize.height)) {
            viewSize.height = vpSize.height;
        }

	vp.setViewPosition(viewPosition);
	vp.setViewSize(viewSize);
    }


}
