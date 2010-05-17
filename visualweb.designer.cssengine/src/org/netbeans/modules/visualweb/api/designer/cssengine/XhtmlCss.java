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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.api.designer.cssengine;

/**
 * Common XHTML CSS definitions
 *
 * @author Tor Norbye
 */
public class XhtmlCss {

    //
    // The property indexes - KEEP IN SYNC WITH VALUE MANAGERS IN XhtmlCssEngine!
    //
    public final static int BACKGROUND_COLOR_INDEX = 0;
    public final static int BACKGROUND_IMAGE_INDEX = BACKGROUND_COLOR_INDEX+1;
    public final static int BACKGROUND_POSITION_INDEX = BACKGROUND_IMAGE_INDEX+1;
    public final static int BACKGROUND_REPEAT_INDEX = BACKGROUND_POSITION_INDEX+1;
    public final static int BORDER_COLLAPSE_INDEX = BACKGROUND_REPEAT_INDEX+1;
    public final static int BORDER_LEFT_COLOR_INDEX = BORDER_COLLAPSE_INDEX+1;
    public final static int BORDER_RIGHT_COLOR_INDEX = BORDER_LEFT_COLOR_INDEX + 1;
    public final static int BORDER_TOP_COLOR_INDEX = BORDER_RIGHT_COLOR_INDEX + 1;
    public final static int BORDER_BOTTOM_COLOR_INDEX = BORDER_TOP_COLOR_INDEX + 1;
    public final static int BORDER_LEFT_STYLE_INDEX = BORDER_BOTTOM_COLOR_INDEX+1;
    public final static int BORDER_RIGHT_STYLE_INDEX = BORDER_LEFT_STYLE_INDEX + 1;
    public final static int BORDER_TOP_STYLE_INDEX = BORDER_RIGHT_STYLE_INDEX + 1;
    public final static int BORDER_BOTTOM_STYLE_INDEX = BORDER_TOP_STYLE_INDEX + 1;
    public final static int BORDER_LEFT_WIDTH_INDEX = BORDER_BOTTOM_STYLE_INDEX+1;
    public final static int BORDER_RIGHT_WIDTH_INDEX = BORDER_LEFT_WIDTH_INDEX + 1;
    public final static int BORDER_TOP_WIDTH_INDEX = BORDER_RIGHT_WIDTH_INDEX + 1;
    public final static int BORDER_BOTTOM_WIDTH_INDEX = BORDER_TOP_WIDTH_INDEX + 1;
    public final static int CAPTION_SIDE_INDEX = BORDER_BOTTOM_WIDTH_INDEX + 1;
    public final static int CLEAR_INDEX = CAPTION_SIDE_INDEX + 1;
    public final static int CLIP_INDEX = CLEAR_INDEX + 1;
    public final static int COLOR_INDEX = CLIP_INDEX + 1;
    public final static int DIRECTION_INDEX = COLOR_INDEX + 1;
    public final static int DISPLAY_INDEX = DIRECTION_INDEX + 1;
    public final static int FLOAT_INDEX = DISPLAY_INDEX + 1;
    public final static int FONT_FAMILY_INDEX = FLOAT_INDEX + 1;
    public final static int FONT_SIZE_INDEX = FONT_FAMILY_INDEX + 1;
    public final static int FONT_SIZE_ADJUST_INDEX = FONT_SIZE_INDEX + 1;
    public final static int FONT_STRETCH_INDEX = FONT_SIZE_ADJUST_INDEX + 1;
    public final static int FONT_STYLE_INDEX = FONT_STRETCH_INDEX + 1;
    public final static int FONT_VARIANT_INDEX = FONT_STYLE_INDEX + 1;
    public final static int FONT_WEIGHT_INDEX = FONT_VARIANT_INDEX + 1;
    public final static int HEIGHT_INDEX = FONT_WEIGHT_INDEX + 1;
    public final static int LINE_HEIGHT_INDEX = HEIGHT_INDEX + 1;
    public final static int LIST_STYLE_IMAGE_INDEX = LINE_HEIGHT_INDEX + 1;
    public final static int LIST_STYLE_TYPE_INDEX = LIST_STYLE_IMAGE_INDEX + 1;
    public final static int MARGIN_LEFT_INDEX = LIST_STYLE_TYPE_INDEX + 1;
    public final static int MARGIN_RIGHT_INDEX = MARGIN_LEFT_INDEX + 1;
    public final static int MARGIN_TOP_INDEX = MARGIN_RIGHT_INDEX + 1;
    public final static int MARGIN_BOTTOM_INDEX = MARGIN_TOP_INDEX + 1;
    public final static int LEFT_INDEX = MARGIN_BOTTOM_INDEX + 1;
    public final static int RIGHT_INDEX = LEFT_INDEX + 1;
    public final static int TOP_INDEX = RIGHT_INDEX + 1;
    public final static int BOTTOM_INDEX = TOP_INDEX + 1;
    public final static int OVERFLOW_INDEX = BOTTOM_INDEX + 1;
    public final static int PADDING_LEFT_INDEX = OVERFLOW_INDEX + 1;
    public final static int PADDING_RIGHT_INDEX = PADDING_LEFT_INDEX + 1;
    public final static int PADDING_TOP_INDEX = PADDING_RIGHT_INDEX + 1;
    public final static int PADDING_BOTTOM_INDEX = PADDING_TOP_INDEX + 1;
    public final static int POSITION_INDEX = PADDING_BOTTOM_INDEX + 1;
    public final static int TABLE_LAYOUT_INDEX = POSITION_INDEX + 1;
    public final static int TEXT_ALIGN_INDEX = TABLE_LAYOUT_INDEX + 1;
    public final static int TEXT_DECORATION_INDEX = TEXT_ALIGN_INDEX + 1;
    public final static int TEXT_INDENT_INDEX = TEXT_DECORATION_INDEX + 1;
    public final static int TEXT_TRANSFORM_INDEX = TEXT_INDENT_INDEX + 1;
    public final static int UNICODE_BIDI_INDEX = TEXT_TRANSFORM_INDEX + 1;
    public final static int VERTICAL_ALIGN_INDEX = UNICODE_BIDI_INDEX + 1;
    public final static int VISIBILITY_INDEX = VERTICAL_ALIGN_INDEX + 1;
    public final static int WHITE_SPACE_INDEX = VISIBILITY_INDEX + 1;
    public final static int WIDTH_INDEX = WHITE_SPACE_INDEX + 1;
    public final static int Z_INDEX = WIDTH_INDEX + 1;
    public final static int RAVELAYOUT_INDEX = Z_INDEX + 1;
    public final static int RAVELINKCOLOR_INDEX = RAVELAYOUT_INDEX + 1;
    /** XXX See the only suspicious usage in XhtmlCssEngine. */
    public final static int FINAL_INDEX = RAVELINKCOLOR_INDEX;
}
