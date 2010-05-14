/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.edm.editor.graph.jgo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class MouseFocusHandler extends MouseAdapter implements FocusListener {
    public static final Color DEFAULT_FG_COLOR = new Color(30, 70, 230); // navy;
    public static final Color DEFAULT_FG_EDIT_COLOR = Color.BLACK;

    public static final Color DEFAULT_BG_COLOR = new Color(240, 240, 240); // light gray
    public static final Color DEFAULT_BG_EDIT_COLOR = Color.WHITE;
    public static final Color DEFAULT_BG_HOVER_COLOR = new Color(254, 254, 244); // light
                                                                                    // beige

    private Color fgColor = null;
    private Color fgEditColor = null;
    private Color bgColor = null;
    private Color bgEditColor = null;
    private Color hoverColor = null;

    private MouseFocusHandler() {
    }

    public abstract void mouseEntered(MouseEvent e);

    public abstract void mouseExited(MouseEvent e);

    public abstract void focusGained(FocusEvent e);

    public abstract void focusLost(FocusEvent e);

    public void showHoverColors(Component source) {
        source.setForeground(getForegroundColor());
        source.setBackground(getBackgroundColor());
    }

    public void showRenderingColors(Component source) {
        source.setForeground(getForegroundColor());
        source.setBackground(getBackgroundColor());
    }

    public void showEditingColors(Component source) {
        source.setForeground(getForegroundEditColor());
        source.setBackground(getBackgroundEditColor());
    }

    public Color getBackgroundEditColor() {
        return (bgEditColor != null) ? bgEditColor : DEFAULT_BG_EDIT_COLOR;
    }

    public void setBackgroundEditColor(Color newColor) {
        bgEditColor = newColor;
    }

    public Color getForegroundEditColor() {
        return (fgEditColor != null) ? fgEditColor : DEFAULT_FG_EDIT_COLOR;
    }

    public void setForegroundEditColor(Color newColor) {
        fgEditColor = newColor;
    }

    public Color getForegroundColor() {
        return (fgColor != null) ? fgColor : DEFAULT_FG_COLOR;
    }

    public void setForegroundColor(Color newColor) {
        fgColor = newColor;
    }

    public Color getBackgroundColor() {
        return (bgColor != null) ? bgColor : DEFAULT_BG_COLOR;
    }

    public void setBackgroundColor(Color newColor) {
        bgColor = newColor;
    }

    public Color getHoverColor() {
        return (hoverColor != null) ? hoverColor : DEFAULT_BG_HOVER_COLOR;
    }

    public void setHoverColor(Color newColor) {
        hoverColor = newColor;
    }

    static class Basic extends MouseFocusHandler {
        public void mouseEntered(MouseEvent e) {
            showHoverColors(e.getComponent());
        }

        public void mouseExited(MouseEvent e) {
            showRenderingColors(e.getComponent());
        }

        public void focusGained(FocusEvent e) {
            showHoverColors(e.getComponent());
        }

        public void focusLost(FocusEvent e) {
            showRenderingColors(e.getComponent());
        }
    }

    static class Editable extends MouseFocusHandler {
        public void mouseEntered(MouseEvent e) {
            Component source = e.getComponent();
            if (source != null && source.isFocusOwner()) {
                showEditingColors(source);
            } else {
                showHoverColors(source);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component source = e.getComponent();
            if (source != null && source.isFocusOwner()) {
                showEditingColors(source);
            } else {
                showRenderingColors(source);
            }
        }

        public void focusGained(FocusEvent e) {
            showEditingColors(e.getComponent());
        }

        public void focusLost(FocusEvent e) {
            showRenderingColors(e.getComponent());
        }
    }
}
