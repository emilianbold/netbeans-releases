/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.content;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.border.Border;
import org.openide.util.Utilities;

/**
 *
 * @author S. Aubrecht
 */
public interface Constants {
    
    static final Color DEFAULT_BACKGROUND_COLOR = new Color( 255,255,255 );
    static final Color SCREEN_BACKGROUND_COLOR = new Color( 232,221,194 );
    static final Color SECTION_BACKGROUND_COLOR = new Color( 235,235,235 );

    static final Color HEADER_FILL_COLOR = new Color( 251,252,253 );
    static final Color FOOTER_FILL_COLOR = new Color( 255,255,255 );

    static final Color TITLE_TEXT_COLOR = new Color( 217,103,2 );
    static final Color BUTTON_TEXT_COLOR = new Color( 22,75,123 );
    static final Color HEADER_TEXT_COLOR = new Color( 14,27,85 );
    static final Color SECTION_TEXT_COLOR = new Color( 96,96,96 );
    static final Color DEFAULT_TEXT_COLOR = new Color( 0,0,0 );
    static final Color RSS_DATETIME_COLOR = new Color( 128,128,128 );

    static final String FONT_NAME = Utils.getFontName();
    static final int FONT_SIZE = Utils.getDefaultFontSize();
    static final Font BUTTON_FONT = new Font( FONT_NAME, Font.BOLD, FONT_SIZE );
    static final Font HEADER_FONT = BUTTON_FONT;
    static final Font REGULAR_FONT = BUTTON_FONT;
    static final Font RSS_DESCRIPTION_FONT = new Font( FONT_NAME, Font.PLAIN, FONT_SIZE-1 );

    static final Font WELCOME_HEADER_FONT = new Font( FONT_NAME, Font.BOLD, FONT_SIZE+3 );
    static final Font WELCOME_DESCRIPTION_FONT = new Font( FONT_NAME, Font.PLAIN, FONT_SIZE );

    static final int BOTTOM_MARGIN = 5;
    static final int UNDER_HEADER_MARGIN = 5;
    static final int ROW_MARGIN = 0;
    static final int SECTION_MARGIN = 5;
    static final int UNDER_SECTION_MARGIN = 0;

    static final int GRAPHICS_TOTAL_VERTICAL_HEIGHT = 230;

    static final int UPPER_LEFT = 1;
    static final int UPPER_RIGHT = 2;
    static final int BOTTOM_LEFT = 4;
    static final int BOTTOM_RIGHT = 8;

    static final String BULLET_IMAGE = "org/netbeans/modules/welcome/resources/bullet.png"; // NOI18N
    static final String BACKGROUND_IMAGE = "org/netbeans/modules/welcome/resources/welcome_background.png"; // NOI18N
    static final String BACKGROUND_TOP_IMAGE = "org/netbeans/modules/welcome/resources/top_grad.png"; // NOI18N
    static final String BACKGROUND_BOTTOM_IMAGE = "org/netbeans/modules/welcome/resources/bottom_grad.png"; // NOI18N
    static final String NB_LOGO_IMAGE = "org/netbeans/modules/welcome/resources/nb_logo.png"; // NOI18N
    static final String SUN_LOGO_IMAGE = "org/netbeans/modules/welcome/resources/sun_logo.png"; // NOI18N
    static final String JAVA_LOGO_IMAGE = "org/netbeans/modules/welcome/resources/java_logo.png"; // NOI18N

    static final String SEL_HEADER_TOP_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/t_topleft.png"; // NOI18N
    static final String SEL_HEADER_TOP_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/t_topright.png"; // NOI18N
    static final String SEL_HEADER_BOTTOM_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/t_bottomleft.png"; // NOI18N
    static final String SEL_HEADER_BOTTOM_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/t_bottomright.png"; // NOI18N
    static final String SEL_HEADER_TOP_IMAGE = "org/netbeans/modules/welcome/resources/t_top.png"; // NOI18N
    static final String SEL_HEADER_BOTTOM_IMAGE = "org/netbeans/modules/welcome/resources/t_bottom.png"; // NOI18N
    static final String SEL_HEADER_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/t_left.png"; // NOI18N
    static final String SEL_HEADER_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/t_right.png"; // NOI18N

    static final String SEL_FOOTER_TOP_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/b_topleft.png"; // NOI18N
    static final String SEL_FOOTER_TOP_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/b_topright.png"; // NOI18N
    static final String SEL_FOOTER_BOTTOM_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/b_bottomleft.png"; // NOI18N
    static final String SEL_FOOTER_BOTTOM_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/b_bottomright.png"; // NOI18N
    static final String SEL_FOOTER_TOP_IMAGE = "org/netbeans/modules/welcome/resources/b_top.png"; // NOI18N
    static final String SEL_FOOTER_BOTTOM_IMAGE = "org/netbeans/modules/welcome/resources/b_bottom.png"; // NOI18N
    static final String SEL_FOOTER_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/b_left.png"; // NOI18N
    static final String SEL_FOOTER_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/b_right.png"; // NOI18N

    static final String SEL_LEFT_SIDE_IMAGE = "org/netbeans/modules/welcome/resources/leftside.png"; // NOI18N
    static final String SEL_RIGHT_SIDE_IMAGE = "org/netbeans/modules/welcome/resources/rightside.png"; // NOI18N


    static final String DESEL_HEADER_TOP_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/desel_t_topleft.png"; // NOI18N
    static final String DESEL_HEADER_TOP_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/desel_t_topright.png"; // NOI18N
    static final String DESEL_HEADER_BOTTOM_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/desel_t_bottomleft.png"; // NOI18N
    static final String DESEL_HEADER_BOTTOM_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/desel_t_bottomright.png"; // NOI18N
    static final String DESEL_HEADER_TOP_IMAGE = "org/netbeans/modules/welcome/resources/desel_t_top.png"; // NOI18N
    static final String DESEL_HEADER_BOTTOM_IMAGE = "org/netbeans/modules/welcome/resources/desel_t_bottom.png"; // NOI18N
    static final String DESEL_HEADER_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/desel_t_left.png"; // NOI18N
    static final String DESEL_HEADER_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/desel_t_right.png"; // NOI18N

    static final String DESEL_FOOTER_TOP_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/desel_b_topleft.png"; // NOI18N
    static final String DESEL_FOOTER_TOP_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/desel_b_topright.png"; // NOI18N
    static final String DESEL_FOOTER_BOTTOM_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/desel_b_bottomleft.png"; // NOI18N
    static final String DESEL_FOOTER_BOTTOM_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/desel_b_bottomright.png"; // NOI18N
    static final String DESEL_FOOTER_TOP_IMAGE = "org/netbeans/modules/welcome/resources/desel_b_top.png"; // NOI18N
    static final String DESEL_FOOTER_BOTTOM_IMAGE = "org/netbeans/modules/welcome/resources/desel_b_bottom.png"; // NOI18N
    static final String DESEL_FOOTER_LEFT_IMAGE = "org/netbeans/modules/welcome/resources/desel_b_left.png"; // NOI18N
    static final String DESEL_FOOTER_RIGHT_IMAGE = "org/netbeans/modules/welcome/resources/desel_b_right.png"; // NOI18N

    static final String DESEL_LEFT_SIDE_IMAGE = "org/netbeans/modules/welcome/resources/desel_leftside.png"; // NOI18N
    static final String DESEL_RIGHT_SIDE_IMAGE = "org/netbeans/modules/welcome/resources/desel_rightside.png"; // NOI18N

    static final Stroke LINK_IN_FOCUS_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_BEVEL, 0, new float[] {0, 2}, 0);
    static final Color LINK_IN_FOCUS_COLOR = new Color(0,0,0);
    static final String RSS_LINK_COLOR = "164B7B";

    static final int RSS_FEED_TIMER_RELOAD_MILLIS = 60*60*1000;


    static final int TEXT_INSETS_LEFT = 10;
    static final int TEXT_INSETS_RIGHT = 10;

    static final Border HEADER_TEXT_BORDER = BorderFactory.createEmptyBorder( 1, TEXT_INSETS_LEFT, 1, TEXT_INSETS_RIGHT );

    static final int FEED_PANEL_MAX_WIDTH = 650;
    static final int FEED_PANEL_MIN_WIDTH = 200;

    static final ImageIcon BULLET_ICON = new ImageIcon( Utilities.loadImage( BULLET_IMAGE ) );
}
