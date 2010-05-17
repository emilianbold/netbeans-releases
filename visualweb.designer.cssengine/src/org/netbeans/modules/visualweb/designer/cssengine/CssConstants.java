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

package org.netbeans.modules.visualweb.designer.cssengine;

import org.apache.batik.util.CSSConstants;

/** Yuck, constant-interface */
public interface CssConstants extends CSSConstants {
    //
    // The CSS property names.
    //
    String CSS_BACKGROUND_PROPERTY = "background"; // NOI18N
    String CSS_BACKGROUND_ATTACHMENT_PROPERTY = "background-attachment"; // NOI18N
    String CSS_BACKGROUND_COLOR_PROPERTY = "background-color"; // NOI18N
    String CSS_BACKGROUND_IMAGE_PROPERTY = "background-image"; // NOI18N
    String CSS_BACKGROUND_REPEAT_PROPERTY = "background-repeat"; // NOI18N
    String CSS_BACKGROUND_POSITION_PROPERTY = "background-position"; // NOI18N
    String CSS_BORDER_PROPERTY = "border"; // NOI18N
    String CSS_BORDER_COLLAPSE_PROPERTY = "border-collapse"; // NOI18N
    String CSS_BORDER_COLOR_PROPERTY = "border-color"; // NOI18N
    String CSS_BORDER_LEFT_PROPERTY = "border-left"; // NOI18N
    String CSS_BORDER_RIGHT_PROPERTY = "border-right"; // NOI18N
    String CSS_BORDER_TOP_PROPERTY = "border-top"; // NOI18N
    String CSS_BORDER_BOTTOM_PROPERTY = "border-bottom"; // NOI18N
    String CSS_BORDER_LEFT_COLOR_PROPERTY = "border-left-color"; // NOI18N
    String CSS_BORDER_RIGHT_COLOR_PROPERTY = "border-right-color"; // NOI18N
    String CSS_BORDER_TOP_COLOR_PROPERTY = "border-top-color"; // NOI18N
    String CSS_BORDER_BOTTOM_COLOR_PROPERTY = "border-bottom-color"; // NOI18N
    String CSS_BORDER_WIDTH_PROPERTY = "border-width"; // NOI18N
    String CSS_BORDER_LEFT_WIDTH_PROPERTY = "border-left-width"; // NOI18N
    String CSS_BORDER_RIGHT_WIDTH_PROPERTY = "border-right-width"; // NOI18N
    String CSS_BORDER_TOP_WIDTH_PROPERTY = "border-top-width"; // NOI18N
    String CSS_BORDER_BOTTOM_WIDTH_PROPERTY = "border-bottom-width"; // NOI18N
    String CSS_BORDER_STYLE_PROPERTY = "border-style"; // NOI18N
    String CSS_BORDER_LEFT_STYLE_PROPERTY = "border-left-style"; // NOI18N
    String CSS_BORDER_RIGHT_STYLE_PROPERTY = "border-right-style"; // NOI18N
    String CSS_BORDER_TOP_STYLE_PROPERTY = "border-top-style"; // NOI18N
    String CSS_BORDER_BOTTOM_STYLE_PROPERTY = "border-bottom-style"; // NOI18N
    String CSS_BOTTOM_PROPERTY = "bottom"; // NOI18N
    String CSS_CAPTION_SIDE_PROPERTY = "caption-side"; // NOI18N
    String CSS_CLEAR_PROPERTY = "clear"; // NOI18N
    String CSS_FLOAT_PROPERTY = "float"; // NOI18N
    String CSS_HEIGHT_PROPERTY = "height"; // NOI18N
    String CSS_LEFT_PROPERTY = "left"; // NOI18N
    // Defined in CSSConstants
    //String CSS_LINE_HEIGHT_PROPERTY = "line-height"; // NOI18N
    String CSS_LIST_STYLE_PROPERTY = "list-style"; // NOI18N
    String CSS_LIST_STYLE_IMAGE_PROPERTY = "list-style-image"; // NOI18N
    String CSS_LIST_STYLE_TYPE_PROPERTY = "list-style-type"; // NOI18N
    String CSS_MARGIN_PROPERTY = "margin"; // NOI18N
    String CSS_MARGIN_LEFT_PROPERTY = "margin-left"; // NOI18N
    String CSS_MARGIN_RIGHT_PROPERTY = "margin-right"; // NOI18N
    String CSS_MARGIN_TOP_PROPERTY = "margin-top"; // NOI18N
    String CSS_MARGIN_BOTTOM_PROPERTY = "margin-bottom"; // NOI18N
    String CSS_POSITION_PROPERTY = "position"; // NOI18N
    String CSS_PADDING_PROPERTY = "padding"; // NOI18N
    String CSS_PADDING_LEFT_PROPERTY = "padding-left"; // NOI18N
    String CSS_PADDING_RIGHT_PROPERTY = "padding-right"; // NOI18N
    String CSS_PADDING_TOP_PROPERTY = "padding-top"; // NOI18N
    String CSS_PADDING_BOTTOM_PROPERTY = "padding-bottom"; // NOI18N
    String CSS_RIGHT_PROPERTY = "right"; // NOI18N
    String CSS_TABLE_LAYOUT_PROPERTY = "table-layout"; // NOI18N
    String CSS_TEXT_ALIGN_PROPERTY = "text-align"; // NOI18N
    String CSS_TEXT_INDENT_PROPERTY = "text-indent"; // NOI18N
    String CSS_TEXT_TRANSFORM_PROPERTY = "text-transform"; // NOI18N
    String CSS_TOP_PROPERTY = "top"; // NOI18N
    String CSS_VERTICAL_ALIGN_PROPERTY = "vertical-align"; // NOI18N
    String CSS_WHITE_SPACE_PROPERTY = "white-space"; // NOI18N
    String CSS_WIDTH_PROPERTY = "width"; // NOI18N
    String CSS_Z_INDEX_PROPERTY = "z-index"; // NOI18N

    // The CSS property values.
    //
    String CSS_TRANSPARENT_VALUE = "transparent"; // NOI18N
    // Border styles
    String CSS_NONE_VALUE = "none"; // NOI18N
    String CSS_HIDDEN_VALUE = "hidden"; // NOI18N
    String CSS_DOTTED_VALUE = "dotted"; // NOI18N
    String CSS_DASHED_VALUE = "dashed"; // NOI18N
    String CSS_SOLID_VALUE = "solid"; // NOI18N
    String CSS_DOUBLE_VALUE = "double"; // NOI18N
    String CSS_GROOVE_VALUE = "groove"; // NOI18N
    String CSS_RIDGE_VALUE = "ridge"; // NOI18N
    String CSS_INSET_VALUE = "inset"; // NOI18N
    String CSS_OUTSET_VALUE = "outset"; // NOI18N
    // Border widths
    String CSS_THIN_VALUE = "thin"; // NOI18N
    String CSS_MEDIUM_VALUE = "medium"; // NOI18N
    String CSS_THICK_VALUE = "thick"; // NOI18N
    // Border collapse
    String CSS_COLLAPSE_VALUE = "collapse"; // NOI18N
    String CSS_SEPARATE_VALUE = "separate"; // NOI18N

    // Float types
    String CSS_LEFT_VALUE = "left"; // NOI18N
    String CSS_RIGHT_VALUE = "right"; // NOI18N

    // Position types
    String CSS_ABSOLUTE_VALUE = "absolute"; // NOI18N
    String CSS_FIXED_VALUE = "fixed"; // NOI18N
    String CSS_RELATIVE_VALUE = "relative"; // NOI18N
    String CSS_STATIC_VALUE = "static"; // NOI18N

    // Background Repeat choices
    String CSS_REPEAT_VALUE = "repeat"; // NOI18N
    String CSS_REPEAT_X_VALUE = "repeat-x"; // NOI18N
    String CSS_REPEAT_Y_VALUE = "repeat-y"; // NOI18N
    String CSS_NO_REPEAT_VALUE = "no-repeat"; // NOI18N

    // Background Position choices
    /* Not yet implemented
    String CSS_CENTER_VALUE = "center"; // NOI18N
    String CSS_BOTTOM_VALUE = "bottom"; // NOI18N
    String CSS_TOP_VALUE = "top"; // NOI18N
    String CSS_LEFT_VALUE = "left"; // NOI18N
    String CSS_RIGHT_VALUE = "right"; // NOI18N
    */

    // Text Alignment choices (left, right, center, etc. inherited)
    String CSS_JUSTIFY_VALUE = "justify"; // NOI18N

    // List Style Type Choices
    String CSS_LOWER_ROMAN_VALUE = "lower-roman"; // NOI18N
    String CSS_DISC_VALUE = "disc"; // NOI18N
    String CSS_CIRCLE_VALUE = "circle"; // NOI18N
    String CSS_DECIMAL_VALUE = "decimal"; // NOI18N
    String CSS_DECIMAL_LEADING_ZERO_VALUE = "decimal-leading-zero"; // NOI18N
    String CSS_UPPER_ROMAN_VALUE = "upper-roman"; // NOI18N
    String CSS_LOWER_LATIN_VALUE = "lower-latin"; // NOI18N
    String CSS_UPPER_LATIN_VALUE = "upper-latin"; // NOI18N
    // "lower-alpha" and "upper-alpha" are not part of the CSS2.1
    // spec. But it seems to be used in older documents so we'll
    // support it.
    String CSS_LOWER_ALPHA_VALUE = "lower-alpha"; // NOI18N
    String CSS_UPPER_ALPHA_VALUE = "upper-alpha"; // NOI18N

    // Vendor specific properties: Rave specific
    String CSS_RAVE_LAYOUT_PROPERTY = "-rave-layout"; // NOI18N
    String CSS_GRID_VALUE = "grid"; // NOI18N
    String CSS_FLOW_VALUE = "flow"; // NOI18N
    String CSS_RAVE_LINK_COLOR_PROPERTY = "-rave-link-color";

    // White-space values
    String CSS_PRE_VALUE = "pre"; // NOI18N
    String CSS_NOWRAP_VALUE = "nowrap"; // NOI18N
    // Defined in CSSConstants
    //String CSS_NORMAL_VALUE = "normal"; // NOI18N
    String CSS_PRE_WRAP_VALUE = "pre-wrap"; // NOI18N
    String CSS_PRE_LINE_VALUE = "pre-line"; // NOI18N

    // Clear values
    String CSS_BOTH_VALUE = "both"; // NOI18N

    // Text values
    String CSS_CAPITALIZE_VALUE = "capitalize"; // NOI18N
    String CSS_UPPERCASE_VALUE = "uppercase"; // NOI18N
    String CSS_LOWERCASE_VALUE = "lowercase"; // NOI18N

    // Provide a way to specify a block center value
    String CSS_RAVECENTER_VALUE = "-rave-center";
}
