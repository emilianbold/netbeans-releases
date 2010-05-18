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

package org.netbeans.modules.visualweb.designer.html;
import org.w3c.dom.Element;

/**
 * HTML attributes. Most of these are probably unused now so we should
 * get rid of them.
 *
 * @author Tor Norbye
 */
public final class HtmlAttribute {
    public static final String FOR = "for"; // NOI18N
    public static final String FRAME = "frame"; // NOI18N
    public static final String RULES = "rules"; // NOI18N
    public static final String SIZE = "size"; // NOI18N
    public static final String COLOR = "color"; // NOI18N
    public static final String CLEAR = "clear"; // NOI18N
    public static final String BACKGROUND = "background"; // NOI18N
    public static final String BGCOLOR = "bgcolor"; // NOI18N
    public static final String TEXT = "text"; // NOI18N
    public static final String LINK = "link"; // NOI18N
    //public static final String VLINK = "vlink"; // NOI18N
    //public static final String ALINK = "alink"; // NOI18N
    public static final String WIDTH = "width"; // NOI18N
    public static final String HEIGHT = "height"; // NOI18N
    public static final String ALIGN = "align"; // NOI18N
    public static final String NAME = "name"; // NOI18N
    public static final String HREF = "href"; // NOI18N
    public static final String REL = "rel"; // NOI18N
    public static final String REV = "rev"; // NOI18N
    public static final String TITLE = "title"; // NOI18N
    public static final String TARGET = "target"; // NOI18N
    public static final String SHAPE = "shape"; // NOI18N
    public static final String COORDS = "coords"; // NOI18N
    public static final String ISMAP = "ismap"; // NOI18N
    public static final String NOHREF = "nohref"; // NOI18N
    public static final String ALT = "alt"; // NOI18N
    public static final String ID = "id"; // NOI18N
    public static final String SRC = "src"; // NOI18N
    public static final String HSPACE = "hspace"; // NOI18N
    public static final String VSPACE = "vspace"; // NOI18N
    public static final String USEMAP = "usemap"; // NOI18N
    public static final String LOWSRC = "lowsrc"; // NOI18N
    public static final String CODEBASE = "codebase"; // NOI18N
    public static final String CODE = "code"; // NOI18N
    public static final String ARCHIVE = "archive"; // NOI18N
    public static final String VALUE = "value"; // NOI18N
    public static final String VALUETYPE = "valuetype"; // NOI18N
    public static final String TYPE = "type"; // NOI18N
    public static final String CLASS = "class"; // NOI18N
    public static final String STYLE = "style"; // NOI18N
    public static final String LANG = "lang"; // NOI18N
    public static final String FACE = "face"; // NOI18N
    public static final String DIR = "dir"; // NOI18N
    public static final String DECLARE = "declare"; // NOI18N
    public static final String CLASSID = "classid"; // NOI18N
    public static final String DATA = "data"; // NOI18N
    public static final String CODETYPE = "codetype"; // NOI18N
    public static final String ONLOAD = "onload"; // NOI18N
    public static final String STANDBY = "standby"; // NOI18N
    public static final String BORDER = "border"; // NOI18N
    public static final String SHAPES = "shapes"; // NOI18N
    public static final String NOSHADE = "noshade"; // NOI18N
    public static final String COMPACT = "compact"; // NOI18N
    public static final String START = "start"; // NOI18N
    public static final String ACTION = "action"; // NOI18N
    public static final String METHOD = "method"; // NOI18N
    public static final String ENCTYPE = "enctype"; // NOI18N
    public static final String CHECKED = "checked"; // NOI18N
    public static final String MAXLENGTH = "maxlength"; // NOI18N
    public static final String MULTIPLE = "multiple"; // NOI18N
    public static final String SELECTED = "selected"; // NOI18N
    public static final String ROWS = "rows"; // NOI18N
    public static final String COLS = "cols"; // NOI18N
    //public static final String DUMMY = "dummy"; // NOI18N
    public static final String CELLSPACING = "cellspacing"; // NOI18N
    public static final String CELLPADDING = "cellpadding"; // NOI18N
    public static final String VALIGN = "valign"; // NOI18N
    public static final String HALIGN = "halign"; // NOI18N
    public static final String NOWRAP = "nowrap"; // NOI18N
    public static final String ROWSPAN = "rowspan"; // NOI18N
    public static final String COLSPAN = "colspan"; // NOI18N
    public static final String SPAN = "span"; // NOI18N
    public static final String PROMPT = "prompt"; // NOI18N
    public static final String HTTPEQUIV = "http-equiv"; // NOI18N
    public static final String CONTENT = "content"; // NOI18N
    public static final String LANGUAGE = "language"; // NOI18N
    public static final String VERSION = "version"; // NOI18N
    //public static final String N = "n"; // NOI18N
    public static final String FRAMEBORDER = "frameborder"; // NOI18N
    public static final String MARGINWIDTH = "marginwidth"; // NOI18N
    public static final String MARGINHEIGHT = "marginheight"; // NOI18N
    public static final String SCROLLING = "scrolling"; // NOI18N
    public static final String NORESIZE = "noresize"; // NOI18N
    public static final String ENDTAG = "endtag"; // NOI18N
    public static final String COMMENT = "comment"; // NOI18N
    public static final String MEDIA = "media"; // NOI18N

    // Integer IDS
    public static final int ALIGN_ID = 0;
    public static final int VALIGN_ID = ALIGN_ID + 1;
    public static final int BGCOLOR_ID = VALIGN_ID + 1;
    public static final int BACKGROUND_ID = BGCOLOR_ID + 1;
    public static final int TEXT_ID = BACKGROUND_ID + 1;
    public static final int WIDTH_ID = TEXT_ID + 1;
    public static final int HEIGHT_ID = WIDTH_ID + 1;
    public static final int NOWRAP_ID = HEIGHT_ID + 1;
    public static final int BORDER_ID = NOWRAP_ID + 1;
    public static final int COLOR_ID = BORDER_ID + 1;
    public static final int SIZE_ID = COLOR_ID + 1;
    public static final int FACE_ID = SIZE_ID + 1;
    public static final int TYPE_ID = FACE_ID + 1;
    public static final int LINK_ID = TYPE_ID + 1;
    public static final int CLEAR_ID = LINK_ID + 1;

    /**
      * Fetches an integer attribute value.  HtmlAttribute values
      * are stored as a string, and this is a convenience method
      * to convert to an actual integer.
      *
      * @param attr the set of attributes to use to try to fetch a value
      * @param key the key to use to fetch the value
      * @param def the default value to use if the attribute isn't
      *  defined or there is an error converting to an integer
      * @todo Make this do the right thing for "20px" - right now it
      *  returns 0 instead of 20 ! Ditto for percentages...
      */
    public static int getIntegerAttributeValue(
        Element el,
        String key,
        int def) {
        int value = def;
        String istr = el.getAttribute(key);
        if (istr != null && istr.length() > 0) {
            try {
                //value = Integer.parseInt(istr);
                value = parseInt(istr);
            } catch (NumberFormatException e) {
                value = def;
            }
        }
        return value;
    }

    public static final byte VALUE_ABSOLUTE = 1;
    public static final byte VALUE_PERCENTAGE = 2;
    public static final byte VALUE_RELATIVE = 4;

    /** Return the number type of the given String, if it represents
     * a number, percentage or relative length */
    public static byte getNumberType(String s) {
        // I start from the beginning rather than the end before I
        // want to use the first relevant character after the number;
        // the String could point to more stuff afterwards
        for (int i = 0, n = s.length(); i < n; i++) {
            if (s.charAt(i) == '%') {
                return VALUE_PERCENTAGE;
            } else if (s.charAt(i) == '*') {
                return VALUE_RELATIVE;
            }
        }
        return VALUE_ABSOLUTE;
    }

    /**
     * Very similar to Integer.parseInt (based on it), but more liberal
     * about what it allows.  Specifically, it allows the string to have
     * a suffix that's not a number; this is ignored. For example
     * "100px" will return "100". Integer.parseInt would throw an
     * exception. This specific case is useful, since for example a
     * buggy <table>tag might set border="1px" - confusing attributes
     * with CSS style attributes - and this would with Integer.parseInt
     * evaluate to 0, whereas both Mozilla and Safari will use a border
     * of 1. Unlike Integer.parseInt, we only support base 10.
     * @todo Move into Util
     */
    public static int parseInt(String s) {
        int radix = 10;
        if (s == null) {
            return 0;
        }

        int result = 0;
        boolean negative = false;
        int i = 0, max = s.length();
        int limit;
        int multmin;
        int digit;

        if (max > 0) {
            // Skip initial spaces
            while (Character.isWhitespace(s.charAt(i))) {
                i++;
            }
            if (i == max) {
                return 0;
            }

            if (s.charAt(i) == '-') {
                negative = true;
                limit = Integer.MIN_VALUE;
                i++;
            } else {
                limit = -Integer.MAX_VALUE;
            }
            multmin = limit / radix;
            if (i < max) {
                digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0) {
                    //throw NumberFormatException.forInputString(s);
                    return negative ? result : -result;
                } else {
                    result = -digit;
                }
            }
            while (i < max) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0) {
                    //throw NumberFormatException.forInputString(s);
                    return negative ? result : -result;
                }
                if (result < multmin) {
                    //throw NumberFormatException.forInputString(s);
                    return negative ? result : -result;
                }
                result *= radix;
                if (result < limit + digit) {
                    //throw NumberFormatException.forInputString(s);
                    return negative ? result : -result;
                }
                result -= digit;
            }
        } else {
            //throw NumberFormatException.forInputString(s);
            //return negative ? result : -result;
            return 0;
        }
        done: if (negative) {
            if (i > 1) {
                return result;
            } else { /* Only got "-" */
                //throw NumberFormatException.forInputString(s);
                return 0;
            }
        } else {
            return -result;
        }
    }
}
