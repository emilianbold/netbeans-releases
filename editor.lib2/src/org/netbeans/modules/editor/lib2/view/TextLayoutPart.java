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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Color;
import java.awt.font.TextLayout;
import java.util.logging.Logger;


/**
 * Part of a text layout useful for highlights view.
 * <br/>
 * Multiple child views can use a single text layout if underlying text uses single font.
 * 
 * @author Miloslav Metelka
 */

final class TextLayoutPart {
    
    // -J-Dorg.netbeans.modules.editor.lib2.view.TextLayoutPart.level=FINE
    private static final Logger LOG = Logger.getLogger(TextLayoutPart.class.getName());

    private final TextLayoutWrapper wrapper; // 8-super + 4 = 12 bytes

    /**
     * Relative child view's index related to TextLayout's start.
     */
    private final int index; // 12 + 4 = 16 bytes
    
    /**
     * Relative character offset shift related to TextLayout's start.
     */
    private final int offsetShift; // 16 + 4 = 20 bytes
    
    /**
     * Relative x shift of the stored bounds of the child view against
     * a start of the text layout (first child view's bounds).
     * <br/>
     * This helps child view to compute bounds of the whole text layout
     * (e.g. for proper getVisualHighlightShape() computation)
     * without knowing its index in a parent box view.
     */
    private final float xShift; // 20 + 4 = 24 bytes
    
    /**
     * Extra foreground to be rendered (or null if same as whole text layout).
     */
    private final Color foreground; // 24 + 4 = 28 bytes

    /**
     * Extra background to be rendered (or null if same as whole text layout).
     */
    private final Color background; // 28 + 4 = 32 bytes

    TextLayoutPart(TextLayoutWrapper wrapper,
            int index, int offsetShift, float xShift,
            Color foreground, Color background)
    {
        this.wrapper = wrapper;
        this.index = index;
        this.offsetShift = offsetShift;
        this.xShift = xShift;
        this.foreground = foreground;
        this.background = background;
    }

    TextLayout textLayout() {
        return wrapper.textLayout();
    }
    
    /**
     * @return total view count of the text layout wrapper.
     */
    int viewCount() {
        return wrapper.viewCount();
    }

    Color textLayoutForeground() {
        return wrapper.foreground();
    }
    
    Color textLayoutBackground() {
        return wrapper.background();
    }
    
    float textLayoutWidth() {
        return TextLayoutUtils.getWidth(textLayout());
    }

    Color foreground() {
        return foreground;
    }
    
    Color background() {
        return background;
    }
    
    Color realBackground() {
        return (background != null) ? background : wrapper.background();
    }
    
    int index() {
        return index;
    }
    
    int offsetShift() {
        return offsetShift;
    }
    
    float xShift() {
        return xShift;
    }

    boolean isLast() {
        return (index() == viewCount() - 1);
    }
    
    public String toStringShort() {
        return "[" + index + "#" + viewCount() + "]<" + offsetShift + // NOI18N
                ">;x=" + ViewUtils.toStringPrec1(xShift) + // NOI18N
                ";fC=" + ViewUtils.toString(foreground) + // NOI18N
                ";bC=" + ViewUtils.toString(background); // NOI18N
    }

    @Override
    public String toString() {
        return toStringShort() + " in " + wrapper; // NOI18N
    }

}
