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

package org.netbeans.modules.welcome.content;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Stroke;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 *
 * @author S. Aubrecht
 */
public interface Constants {

    static final String COLOR_SCREEN_BACKGROUND = "ScreenBackgrouncColor"; //NOI18N
    static final String COLOR_TAB_UNSEL_BACKGROUND = "TabUnselectedBackgrouncColor"; //NOI18N
    
    static final String COLOR_TAB_SEL_FOREGROUND = "TabSelForegroundColor"; //NOI18N
    static final String COLOR_TAB_UNSEL_FOREGROUND = "TabUnselForegroundColor"; //NOI18N
    static final String COLOR_WELCOME_LABEL = "WelcomeLabelColor"; //NOI18N
    static final String COLOR_SECTION_HEADER = "SectionHeaderColor"; //NOI18N
    
    static final int FONT_SIZE = Utils.getDefaultFontSize();
    static final Font BUTTON_FONT = new Font( null, Font.BOLD, FONT_SIZE );
    static final Font BIGGER_FONT = new Font( null, Font.BOLD, FONT_SIZE+2 );
    static final Font RSS_DESCRIPTION_FONT = new Font( null, Font.PLAIN, FONT_SIZE-1 );
    static final Font TAB_FONT = new Font( Utils.getPreferredFontName(), Font.BOLD, FONT_SIZE+2 ); //NOI18N
    static final Font WELCOME_LABEL_FONT = new Font( Utils.getPreferredFontName(), Font.BOLD, FONT_SIZE+2 ) ; //NOI18N
    static final Font SECTION_HEADER_FONT = new Font( Utils.getPreferredFontName(), Font.BOLD, FONT_SIZE+18 ); //NOI18N

    static final String BULLET_IMAGE = "org/netbeans/modules/welcome/resources/bullet.png"; // NOI18N
    static final String SUN_LOGO_IMAGE = "org/netbeans/modules/welcome/resources/sun_logo.png"; // NOI18N
    static final String JAVA_LOGO_IMAGE = "org/netbeans/modules/welcome/resources/java_logo.png"; // NOI18N

    static final String IMAGE_TOPBAR_CENTER = "org/netbeans/modules/welcome/resources/topbar_center.png"; // NOI18N
    static final String IMAGE_TOPBAR_LEFT = "org/netbeans/modules/welcome/resources/topbar_left.png"; // NOI18N
    static final String IMAGE_TOPBAR_RIGHT = "org/netbeans/modules/welcome/resources/topbar_right.png"; // NOI18N
    
    static final String IMAGE_TAB_UNSEL = "org/netbeans/modules/welcome/resources/tab_unsel_bottom.png"; // NOI18N
    static final String IMAGE_TAB_SEL_LEFT = "org/netbeans/modules/welcome/resources/tab_sel_left.png"; // NOI18N
    static final String IMAGE_TAB_SEL_UPPER_LEFT = "org/netbeans/modules/welcome/resources/tab_sel_upper_left.png"; // NOI18N
    static final String IMAGE_TAB_SEL_LOWER_LEFT = "org/netbeans/modules/welcome/resources/tab_sel_lower_left.png"; // NOI18N
    static final String IMAGE_TAB_SEL_RIGHT = "org/netbeans/modules/welcome/resources/tab_sel_right.png"; // NOI18N
    static final String IMAGE_TAB_SEL_UPPER_RIGHT = "org/netbeans/modules/welcome/resources/tab_sel_upper_right.png"; // NOI18N
    static final String IMAGE_TAB_SEL_LOWER_RIGHT = "org/netbeans/modules/welcome/resources/tab_sel_lower_right.png"; // NOI18N
    
    static final String IMAGE_STRIP_BOTTOM_EAST = "org/netbeans/modules/welcome/resources/strip_bottom_east.png"; // NOI18N
    static final String IMAGE_STRIP_BOTTOM_WEST = "org/netbeans/modules/welcome/resources/strip_bottom_west.png"; // NOI18N
    static final String IMAGE_STRIP_BOTTOM_CENTER = "org/netbeans/modules/welcome/resources/strip_bottom_center.png"; // NOI18N
    
    static final String IMAGE_STRIP_TOP_WEST = "org/netbeans/modules/welcome/resources/strip_top_west.png"; // NOI18N
    static final String IMAGE_STRIP_TOP_CENTER = "org/netbeans/modules/welcome/resources/strip_top_center.png"; // NOI18N
    
    static final String IMAGE_STRIP_MIDDLE_EAST = "org/netbeans/modules/welcome/resources/strip_middle_east.png"; // NOI18N
    static final String IMAGE_STRIP_MIDDLE_CENTER = "org/netbeans/modules/welcome/resources/strip_middle_center.png"; // NOI18N
    
    static final String BROKEN_IMAGE = "org/netbeans/modules/welcome/resources/broken_image.png"; // NOI18N

    static final Stroke LINK_IN_FOCUS_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_BEVEL, 0, new float[] {0, 2}, 0);
    static final String LINK_IN_FOCUS_COLOR = "LinkInFocusColor"; //NOI18N
    static final String LINK_COLOR = "LinkColor"; //NOI18N
    static final String VISITED_LINK_COLOR = "VisitedLinkColor"; //NOI18N

    static final int RSS_FEED_TIMER_RELOAD_MILLIS = 60*60*1000;

    static final int TEXT_INSETS_LEFT = 10;
    static final int TEXT_INSETS_RIGHT = 10;

    static final Border HEADER_TEXT_BORDER = BorderFactory.createEmptyBorder( 1, TEXT_INSETS_LEFT, 1, TEXT_INSETS_RIGHT );
}
