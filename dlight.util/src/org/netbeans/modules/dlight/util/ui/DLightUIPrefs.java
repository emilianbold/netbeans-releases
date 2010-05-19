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
package org.netbeans.modules.dlight.util.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;
import org.openide.util.NbBundle;

/**
 * Common place for fonts, colors and sizes used in DLight.
 * Modeled after {@link javax.swing.UIManager}.
 *
 * @author Alexey Vladykin
 */
public final class DLightUIPrefs {

    public static final String INDICATOR_BORDER_COLOR = "indicator.border.color"; // NOI18N
    public static final String INDICATOR_PADDING = "indicator.padding"; // NOI18N
    public static final String INDICATOR_TITLE_FONT = "indicator.title.font"; // NOI18N
    public static final String INDICATOR_TITLE_FONT_COLOR = "indicator.title.font.color"; // NOI18N

    public static final String INDICATOR_GRAPH_WIDTH = "indicator.graph.width"; // NOI18N
    public static final String INDICATOR_GRAPH_HEIGHT = "indicator.graph.height"; // NOI18N
    public static final String INDICATOR_GRAPH_BGCOLOR_TOP = "indicator.graph.bgcolor_top"; // NOI18N
    public static final String INDICATOR_GRAPH_BGCOLOR_BOTTOM = "indicator.graph.bgcolor_bottom"; // NOI18N
    public static final String INDICATOR_GRAPH_FILTER_COLOR = "indicator.graph.filter.color"; // NOI18N
    public static final String INDICATOR_LINE_THICKNESS = "indicator.line.thickness"; // NOI18N
    public static final String INDICATOR_BALL_SIZE = "indicator.ball.size"; // NOI18N
    public static final String INDICATOR_GRID_COLOR = "indicator.grid.color"; // NOI18N

    public static final String INDICATOR_LEGEND_WIDTH = "indicator.legend.width"; // NOI18N
    public static final String INDICATOR_LEGEND_HEIGHT = "indicator.legend.height"; // NOI18N
    public static final String INDICATOR_LEGEND_BGCOLOR = "indicator.legend.bgcolor"; // NOI18N
    public static final String INDICATOR_LEGEND_FONT = "indicator.legend.font"; // NOI18N
    public static final String INDICATOR_LEGEND_FONT_COLOR = "indicator.legend.font.color"; // NOI18N

    public static final String INDICATOR_X_AXIS_WIDTH = "indicator.x_axis.width"; // NOI18N
    public static final String INDICATOR_X_AXIS_HEIGHT = "indicator.x_axis.height"; // NOI18N
    public static final String INDICATOR_X_AXIS_FONT = "indicator.x_axis.font"; // NOI18N
    public static final String INDICATOR_X_AXIS_FONT_COLOR = "indicator.x_axis.font_color"; // NOI18N

    public static final String INDICATOR_Y_AXIS_WIDTH = "indicator.y_axis.width"; // NOI18N
    public static final String INDICATOR_Y_AXIS_HEIGHT = "indicator.y_axis.height"; // NOI18N
    public static final String INDICATOR_Y_AXIS_FONT = "indicator.y_axis.font"; // NOI18N
    public static final String INDICATOR_Y_AXIS_FONT_COLOR = "indicator.y_axis.font_color"; // NOI18N

    /**
     * @param key
     * @return
     */
    public static float getFloat(String key) {
        return getInstance()._getFloat(key);
    }

    /**
     * @param key
     * @return
     */
    public static int getInt(String key) {
        return getInstance()._getInt(key);
    }

    /**
     * @param key
     * @return
     */
    public static Color getColor(String key) {
        return getInstance()._getColor(key);
    }

    /**
     * @param key
     * @return
     */
    public static Font getFont(String key) {
        return getInstance()._getFont(key);
    }

    private final Map<String, Object> prefs;

    private DLightUIPrefs() {
        this.prefs = new HashMap<String, Object>();

        Font font = UIManager.getFont("Panel.font").deriveFont(10f); // NOI18N
        FontMetrics fontMetrics = getFontMetrics(font);
        Color fontColor = new Color(0x31, 0x4E, 0x72);

        int graphWidth = 20 + font.getSize() + fontMetrics.stringWidth(getMessage("indicator.legend.longest_text")); // NOI18N
        int graphHeight = 5 * fontMetrics.getHeight();

        prefs.put(INDICATOR_BORDER_COLOR, new Color(0x72, 0x8A, 0x84));
        prefs.put(INDICATOR_PADDING, 12);
        prefs.put(INDICATOR_TITLE_FONT, UIManager.getFont("Label.font").deriveFont(Font.BOLD)); // NOI18N
        prefs.put(INDICATOR_TITLE_FONT_COLOR, fontColor);

        prefs.put(INDICATOR_GRAPH_WIDTH, graphWidth);
        prefs.put(INDICATOR_GRAPH_HEIGHT, graphHeight);
        prefs.put(INDICATOR_GRAPH_BGCOLOR_TOP, Color.WHITE);
        prefs.put(INDICATOR_GRAPH_BGCOLOR_BOTTOM, new Color(0xD6, 0xE3, 0xF3));
        prefs.put(INDICATOR_GRAPH_FILTER_COLOR, new Color(0xB4, 0xB4, 0xB4, 0x80));
        prefs.put(INDICATOR_LINE_THICKNESS, 2.0f);
        prefs.put(INDICATOR_BALL_SIZE, 5);
        prefs.put(INDICATOR_GRID_COLOR, new Color(0xD7, 0xE0, 0xE3, 0x80));

        prefs.put(INDICATOR_LEGEND_WIDTH, graphWidth);
        prefs.put(INDICATOR_LEGEND_HEIGHT, graphHeight);
        prefs.put(INDICATOR_LEGEND_BGCOLOR, Color.WHITE);
        prefs.put(INDICATOR_LEGEND_FONT, font);
        prefs.put(INDICATOR_LEGEND_FONT_COLOR, fontColor);

        prefs.put(INDICATOR_X_AXIS_WIDTH, graphWidth);
        prefs.put(INDICATOR_X_AXIS_HEIGHT, 2 * font.getSize());
        prefs.put(INDICATOR_X_AXIS_FONT, font);
        prefs.put(INDICATOR_X_AXIS_FONT_COLOR, fontColor);

        prefs.put(INDICATOR_Y_AXIS_WIDTH, font.getSize() / 2 + fontMetrics.stringWidth(getMessage("indicator.y_axis.longest_text"))); // NOI18N
        prefs.put(INDICATOR_Y_AXIS_HEIGHT, graphHeight);
        prefs.put(INDICATOR_Y_AXIS_FONT, font);
        prefs.put(INDICATOR_Y_AXIS_FONT_COLOR, fontColor);
    }

    private float _getFloat(String key) {
        Object value = prefs.get(key);
        if (value instanceof Float) {
            return ((Float) value).floatValue();
        } else {
            return 0f;
        }
    }

    private int _getInt(String key) {
        Object value = prefs.get(key);
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        } else {
            return 0;
        }
    }

    private Color _getColor(String key) {
        Object value = prefs.get(key);
        if (value instanceof Color) {
            return (Color) value;
        } else {
            return null;
        }
    }

    private Font _getFont(String key) {
        Object value = prefs.get(key);
        if (value instanceof Font) {
            return (Font) value;
        } else {
            return null;
        }
    }

    private FontMetrics getFontMetrics(Font font) {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_4BYTE_ABGR);
        return img.getGraphics().getFontMetrics(font);
    }

    private static DLightUIPrefs instance;

    private static synchronized DLightUIPrefs getInstance() {
        if (instance == null) {
            instance = new DLightUIPrefs();
        }
        return instance;
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(DLightUIPrefs.class, key);
    }
}
