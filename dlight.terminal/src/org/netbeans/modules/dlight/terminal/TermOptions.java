/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.terminal;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

/**
 *
 * @author ak119685
 */
public final class TermOptions {

    private final static TermOptions defaults = new TermOptions();
    private final boolean lineWrap;
    private final Font controlFont;
    private final int fontSize;
    private final Font font;
    private final Color foreground;
    private final Color background;
    private final Color selectionBackground;
    private final int historySize;
    private final int tabSize;
    private final boolean clickToType;
    private final boolean scrollOnOutput;
    private final boolean scrollOnInput;

    private TermOptions() {
        controlFont = UIManager.getFont("controlFont"); // NOI18N
        fontSize = (controlFont == null) ? 12 : controlFont.getSize();
        font = new Font("monospaced", Font.PLAIN, fontSize); // NOI18N
        foreground = Color.black;
        background = Color.white;
        selectionBackground = UIManager.getColor("TextArea.selectionBackground"); // NOI18N
        historySize = 5000;
        tabSize = 8;
        clickToType = true;
        scrollOnInput = true;
        scrollOnOutput = true;
        lineWrap = true;
    }

    public Color getBackground() {
        return background;
    }

    public Font getControlFont() {
        return controlFont;
    }

    public Font getFont() {
        return font;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Color getForeground() {
        return foreground;
    }

    public int getHistorySize() {
        return historySize;
    }

    public Color getSelectionBackground() {
        return selectionBackground;
    }

    public int getTabSize() {
        return tabSize;
    }

    public static TermOptions getDefaults() {
        return defaults;
    }

    public boolean isClickToType() {
        return clickToType;
    }

    public boolean isLineWrap() {
        return lineWrap;
    }

    public boolean isScrollOnInput() {
        return scrollOnInput;
    }

    public boolean isScrollOnOutput() {
        return scrollOnOutput;
    }
}
