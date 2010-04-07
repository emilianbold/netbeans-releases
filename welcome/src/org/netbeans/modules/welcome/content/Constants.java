/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

    static final String COLOR_TAB_SEL_FOREGROUND = "TabSelForegroundColor"; //NOI18N
    static final String COLOR_TAB_UNSEL_FOREGROUND = "TabUnselForegroundColor"; //NOI18N
    static final String COLOR_SECTION_HEADER = "SectionHeaderColor"; //NOI18N
    static final String COLOR_SECTION_SEPARATOR = "SectionSeparatorColor"; //NOI18N

    static final String COLOR_RSS_DATE = "RssDateTimeColor"; //NOI18N
    static final String COLOR_RSS_DETAILS = "RssDetailsColor"; //NOI18N
    static final String COLOR_HEADER = "HeaderForegroundColor"; //NOI18N
    
    static final int FONT_SIZE = Utils.getDefaultFontSize();
    static final Font BUTTON_FONT = new Font( null, Font.BOLD, FONT_SIZE );
    static final Font RSS_DESCRIPTION_FONT = new Font( null, Font.PLAIN, FONT_SIZE-1 );
    static final Font TAB_FONT = new Font( null, Font.BOLD, FONT_SIZE+3 ); //NOI18N
    static final Font WELCOME_LABEL_FONT = new Font( null, Font.BOLD, FONT_SIZE+2 ) ; //NOI18N
    static final Font SECTION_HEADER_FONT = new Font( null, Font.BOLD, FONT_SIZE+3 ); //NOI18N
    static final Font GET_STARTED_FONT = new Font( null, Font.BOLD, (int)(FONT_SIZE*1.4) ); //NOI18N

    static final String ORACLE_LOGO_IMAGE = "org/netbeans/modules/welcome/resources/oracle_logo.png"; // NOI18N
    static final String JAVA_LOGO_IMAGE = "org/netbeans/modules/welcome/resources/java_logo.png"; // NOI18N

    static final String IMAGE_TOPBAR_CENTER = "org/netbeans/modules/welcome/resources/topbar_center.png"; // NOI18N
    static final String IMAGE_TOPBAR_LEFT = "org/netbeans/modules/welcome/resources/topbar_left.png"; // NOI18N
    static final String IMAGE_TOPBAR_RIGHT = "org/netbeans/modules/welcome/resources/topbar_right.png"; // NOI18N
    static final String IMAGE_TOPBAR_LOGO = "org/netbeans/modules/welcome/resources/nb_logo.png"; // NOI18N

    static final String BROKEN_IMAGE = "org/netbeans/modules/welcome/resources/broken_image.png"; // NOI18N
    static final String IMAGE_PICTURE_FRAME = "org/netbeans/modules/welcome/resources/picture_frame.png"; // NOI18N

    static final Stroke LINK_IN_FOCUS_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_BEVEL, 0, new float[] {0, 2}, 0);
    static final String LINK_IN_FOCUS_COLOR = "LinkInFocusColor"; //NOI18N
    static final String LINK_COLOR = "LinkColor"; //NOI18N
    static final String MOUSE_OVER_LINK_COLOR = "MouseOverLinkColor"; //NOI18N
    static final String VISITED_LINK_COLOR = "VisitedLinkColor"; //NOI18N
    static final String MOUSE_OVER_TAB_COLOR = "MouseOverTabColor"; //NOI18N

    static final int RSS_FEED_TIMER_RELOAD_MILLIS = 60*60*1000;

    static final int TEXT_INSETS_LEFT = 10;
    static final int TEXT_INSETS_RIGHT = 10;

    static final Border HEADER_TEXT_BORDER = BorderFactory.createEmptyBorder( 1, TEXT_INSETS_LEFT, 1, TEXT_INSETS_RIGHT );
    
    static final int START_PAGE_MIN_WIDTH = 600;
}
