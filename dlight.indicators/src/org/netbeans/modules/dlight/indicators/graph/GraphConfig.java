/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.dlight.indicators.graph;

import java.awt.Color;

/**
 * Provides common colors for graph painting.
 *
 * @author Alexey Vladykin
 */
public final class GraphConfig {

    public static final float FONT_SIZE = 10.0f;
    public static final float LINE_WIDTH = 2.0f;
    public static final int GRID_SIZE = 10;
    public static final int BALL_SIZE = 5;
    public static final int STEP_SIZE = 5;

    public static final int GRAPH_WIDTH = 80;
    public static final int GRAPH_HEIGHT = 80;

    public static final int LEGEND_WIDTH = 80;
    public static final int LEGEND_HEIGHT = 80;

    public static final int VERTICAL_AXIS_WIDTH = 30;
    public static final int HORIZONTAL_AXIS_HEIGHT = 20;

//    public static final Color COLOR_1 = new Color(0xE7, 0x6F, 0x00);
//    public static final Color COLOR_2 = new Color(0x53, 0x82, 0xA1);
//    public static final Color COLOR_3 = new Color(0xFF, 0xC7, 0x26);
//    public static final Color COLOR_4 = new Color(0xB2, 0xBC, 0x00);

    public static final Color BORDER_COLOR = new Color(0x72, 0x8A, 0x84);
    public static final Color GRADIENT_BOTTOM_COLOR = new Color(0xD6, 0xE3, 0xF3);
    public static final Color GRADIENT_TOP_COLOR = Color.WHITE;
    public static final Color GRID_COLOR = new Color(0xD7, 0xE0, 0xE3);
    public static final Color LEGEND_COLOR = Color.WHITE;
    public static final Color TEXT_COLOR = new Color(0x31, 0x4E, 0x72);
    public static final Color DIM_COLOR = new Color(0xEE, 0xEE, 0xEE, 0xA0);

    private GraphConfig() {}
}
