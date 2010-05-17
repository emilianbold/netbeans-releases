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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.collab.channel.filesharing.ui;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;


/**
 * Border for FilesharingCollablet Filesystem Explorer
 *
 * @author  Todd Fast <todd.fast@sun.com>
 * @version 1.0
 */
public class ThinBevelBorder extends AbstractBorder {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static int WIDTH = 1;
    public static final int RAISED = 0;
    public static final int LOWERED = 1;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private int bevelType;
    private Color highlightColor;
    private Color shadowColor;

    /**
     *
     * @param type
     */
    public ThinBevelBorder(int type) {
        super();
        this.bevelType = type;
    }

    /**
     *
     * @param type
     * @param highlight
     * @param shadow
     */
    public ThinBevelBorder(int type, Color highlight, Color shadow) {
        this(type);
        highlightColor = highlight;
        shadowColor = shadow;
    }

    /**
     * getBevelType
     *
     * @return bevel type
     */
    public int getBevelType() {
        return bevelType;
    }

    /**
     *
     * @param value
     */
    public void setBevelType(int value) {
        bevelType = value;
    }

    /**
     *
     * @param component
     * @return border insets
     */
    public Insets getBorderInsets(Component component) {
        return new Insets(WIDTH, WIDTH, WIDTH, WIDTH);
    }

    /**
     *
     * @param component
     * @param insets
     * @return border insets
     */
    public Insets getBorderInsets(Component component, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = WIDTH;

        return insets;
    }

    /**
     * Returns the outer highlight color of the bevel border.
     * Will return null if no highlight color was specified
     * at instantiation.
     * @return hightlight color
     */
    public Color getHighlightColor() {
        return highlightColor;
    }

    /**
     * Returns the inner shadow color of the bevel border.
     * Will return null if no shadow color was specified
     * at instantiation.
     * @return shadow color
     */
    public Color getShadowColor() {
        return shadowColor;
    }

    /**
     * Returns the outer highlight color of the bevel border
     * when rendered on the specified component.  If no highlight
     * color was specified at instantiation, the highlight color
     * is derived from the specified component's background color.
     *
     * @param component The component for which the highlight may be derived
     * @return highlight color
     */
    public Color getHighlightColor(Component component) {
        Color highlight = getHighlightColor();

        return (highlight != null) ? highlight : component.getBackground().brighter().brighter();
    }

    /**
     * Returns the outer shadow color of the bevel border
     * when rendered on the specified component.  If no shadow
     * color was specified at instantiation, the shadow color
     * is derived from the specified component's background color.
     *
     * @param component The component for which the shadow may be derived
     * @return shadow color
     */
    public Color getShadowColor(Component component) {
        Color shadow = getShadowColor();

        return (shadow != null) ? shadow : component.getBackground().darker().darker();
    }

    /**
     *
     * @param c
     * @param g
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (getBevelType() == LOWERED) {
            paintLoweredBevel(c, g, x, y, width, height);
        } else {
            paintRaisedBevel(c, g, x, y, width, height);
        }
    }

    /**
     *
     * @param component
     * @param g
     * @param x
     * @param y
     * @param width
     * @param height
     */
    protected void paintRaisedBevel(Component component, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        g.translate(x, y);

        g.setColor(getHighlightColor(component));

        // Left
        g.drawLine(0, 0, 0, height - WIDTH);

        // Top
        g.drawLine(WIDTH, 0, width - WIDTH, 0);

        g.setColor(getShadowColor(component));

        // Bottom
        g.drawLine(0, height - WIDTH, width - WIDTH, height - WIDTH);

        // Right
        g.drawLine(width - WIDTH, 0, width - WIDTH, height - WIDTH);

        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    /**
     *
     * @param component
     * @param g
     * @param x
     * @param y
     * @param width
     * @param height
     */
    protected void paintLoweredBevel(Component component, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        g.translate(x, y);

        g.setColor(getShadowColor(component));

        // Left
        g.drawLine(0, 0, 0, height - WIDTH);

        // Top
        g.drawLine(WIDTH, 0, width - WIDTH, 0);

        g.setColor(getHighlightColor(component));

        // Bottom
        g.drawLine(0, height - WIDTH, width - WIDTH, height - WIDTH);

        // Right
        g.drawLine(width - WIDTH, 0, width - WIDTH, height - WIDTH);

        g.translate(-x, -y);
        g.setColor(oldColor);
    }
}
