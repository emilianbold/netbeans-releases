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
package org.netbeans.modules.visualweb.css2;

import java.awt.FontMetrics;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.designer.WebForm;


/**
 * StringBox represents a box of text whose text is specified by
 * an attribute rather than the text-node children of the box' element
 * (which is the default).

 * @author Tor Norbye
 */
public class StringBox extends ContainerBox {
    private String string;

    /**
     * Create a StringBox for the given string
     *
     * @param webform The <code>WebForm</code>
     * @param element The element this string box is associated with
     * @param boxType Type of box.
     * @param string The string to manage
     * @param width The width to use for the box
     * @param height The height to use for the box
     */
    public StringBox(WebForm webform, Element element, BoxType boxType, String string,
        CssBorder border, int width, int height) {
        super(webform, element, boxType, true, true);
        this.string = string;
        this.width = width;
        this.height = height;
        this.border = border;
    }

    protected void initializeBorder() {
        if (border == null) {
            super.initializeBorder();
        }

//        if (border == null) {
        // XXX #115938 Don't show design border when there is zero width for the component.
        if (border == null && (string != null && string.length() > 0)) {
            border = CssBorder.getDesignerBorder();
        }

        if (border == null) {
            leftBorderWidth   = 0;
            topBorderWidth    = 0;
            bottomBorderWidth = 0;
            rightBorderWidth  = 0;
        } else {
            leftBorderWidth = border.getLeftBorderWidth();
            topBorderWidth = border.getTopBorderWidth();
            bottomBorderWidth = border.getBottomBorderWidth();
            rightBorderWidth = border.getRightBorderWidth();
        }
    }

    public int getIntrinsicWidth() {
        if (width == AUTO) {
            if (getBoxCount() > 0) {
                assert getBox(0) instanceof LineBoxGroup;

                return getBox(0).getPrefWidth();
            } else {
//                width = 30;
                // XXX #115938 No text (i.e. no child boxes), then no width.
                width = 0;
            }
        }

        return width;
    }

    public int getIntrinsicHeight() {
        if (height == AUTO) {
            if (getBoxCount() > 0) {
                assert getBox(0) instanceof LineBoxGroup;

                return ((LineBoxGroup)getBox(0)).getMetrics().getHeight();
            } else {
//                height = 14;
                // XXX #115938 Height should be based on the font.
                FontMetrics fontMetrics = CssUtilities.getDesignerFontMetricsForElement(getElement(), string, webform.getDefaultFontSize());
                height = fontMetrics.getHeight();
            }
        }

        return height;
    }

//    public String toString() {
//        return "StringBox[" + paramString() + "]";
//    }

    protected String paramString() {
        return super.paramString() + ", " + string;
    }

    /** When building the box hierarchy, instead of adding content
     * for children, add the string attribute content
     */
    protected void createChildren(CreateContext context) {
        LineBoxGroup old = context.lineBox;
        context.lineBox = null;
        addText(context, null, getElement(), string);
        finishLineBox(context);
        context.lineBox = old;
    }

    public int getPrefWidth() {
        return getIntrinsicWidth();
    }

    public int getPrefMinWidth() {
        return getIntrinsicWidth();
    }
}
