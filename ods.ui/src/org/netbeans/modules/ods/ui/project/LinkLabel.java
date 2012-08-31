/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ods.ui.project;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author jpeska
 */
public abstract class LinkLabel extends JLabel implements MouseListener {

    private Map<TextAttribute, Object> underlineFontMap;
    private static final Color FOREGROUND_COLOR = Color.BLUE;
    private static final Color FOREGROUND_FOCUS_COLOR = new Color(0, 150, 255);
    private static final Icon ICON_LINK = ImageUtilities.loadImageIcon("org/netbeans/modules/ods/ui/resources/link.png", true); //NOI18N
    private static final Icon ICON_LINK_FOCUS = ImageUtilities.loadImageIcon("org/netbeans/modules/ods/ui/resources/link_focus.png", true); //NOI18N
    private final boolean showIcon;
    private Action[] popupActions = new Action[0];

    public LinkLabel(String text, boolean showIcon) {
        this.showIcon = showIcon;
        underlineFontMap = new HashMap<TextAttribute, Object>();
        underlineFontMap.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
        if (!text.isEmpty()) {
            setText(text);
        }
        init();
    }

    public LinkLabel(boolean showIcon) {
        this("", showIcon);
    }

    public LinkLabel(String text) {
        this(text, false);
        setText(text);
    }

    public LinkLabel() {
        this("", false);
    }

    private void init() {
        if (showIcon) {
            setIcon(ICON_LINK);
        }
        Font font = getFont();
        font = font.deriveFont(underlineFontMap);
        setFont(font);
        setForeground(FOREGROUND_COLOR);
        addMouseListener(this);
    }

    public void setPopupActions(Action... popupActions) {
        this.popupActions = popupActions;
    }

    @Override
    public void setFont(Font font) {
        font = font.deriveFont(underlineFontMap);
        super.setFont(font);
    }

    @Override
    public abstract void mouseClicked(MouseEvent e);

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e.getPoint());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e.getPoint());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(FOREGROUND_FOCUS_COLOR);
        if (showIcon) {
            setIcon(ICON_LINK_FOCUS);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        setForeground(FOREGROUND_COLOR);
        if (showIcon) {
            setIcon(ICON_LINK);
        }
    }

    private void showPopup(Point p) {
        if (popupActions.length > 0) {
            JPopupMenu menu = Utilities.actionsToPopup(popupActions, this);
            menu.show(this, p.x, p.y);
        }
    }
}
