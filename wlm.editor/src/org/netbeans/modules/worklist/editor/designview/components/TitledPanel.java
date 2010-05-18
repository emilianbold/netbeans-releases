/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author anjeleevich
 */
public class TitledPanel extends JPanel {
    private LinkButton actionButton;
    private Action action;
    private JLabel titleLabel;
    private JComponent content;
    private int maxHeight;

    public TitledPanel(String titleText, JComponent content, int maxHeight) {
        this(titleText, null, content, maxHeight);
    }
    
    public TitledPanel(String titleText, Action action, JComponent content, 
            int maxHeight) 
    {
        setBackground(Color.WHITE);
        setBorder(null);
        setOpaque(true);
        
        this.action = action;
        if (action != null) {
            actionButton = new LinkButton(action);
            actionButton.setBorder(new EmptyBorder(1, 4, 1, 4));
            actionButton.setBackground(Color.WHITE);
            actionButton.setOpaque(true);
            add(actionButton);
        }
        
        titleLabel = new StyledLabel(StyledLabel.BOLD_STYLE);
        titleLabel.setText(titleText);
        titleLabel.setBorder(new EmptyBorder(0, 4, 0, 4));
        titleLabel.setBackground(Color.WHITE);
        titleLabel.setOpaque(true);
        
        this.content = content;
        
        add(titleLabel);
        add(content);
        
        this.maxHeight = maxHeight;
    }
    
    public void setTitleText(String titleText) {
        titleLabel.setText(titleText);
    }
    
    public void setTitleIcon(Icon titleIcon) {
        titleLabel.setIcon(titleIcon);
    }
    
    public JComponent getContent() {
        return content;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = ExUtils.prepareG2(g, true);
        
        Rectangle rect  = titleLabel.getBounds();
        
        int titleMinY = rect.y;
        int titleMaxY = rect.y + rect.height;

        if (actionButton != null) {
            rect = actionButton.getBounds();
            titleMinY = Math.min(titleMinY, rect.y);
            titleMaxY = Math.max(titleMaxY, rect.y + rect.height);
        }
        
        int w = getWidth();
        int h = getHeight();
        
        int y1 = (titleMinY + titleMaxY) / 2;
        int y2 = h - 2;
        
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRoundRect(1, y1, w - 3, y2 - y1, 12, 12);
        
        ExUtils.disposeG2(g2, true);        
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = ExUtils.prepareG2(g, true);
        
        Rectangle rect  = titleLabel.getBounds();
        
        int titleMinY = rect.y;
        int titleMaxY = rect.y + rect.height;
        
        if (actionButton != null) {
            rect = actionButton.getBounds();
            titleMinY = Math.min(titleMinY, rect.y);
            titleMaxY = Math.max(titleMaxY, rect.y + rect.height);
        }
        
        int w = getWidth();
        int h = getHeight();
        
        int y1 = (titleMinY + titleMaxY) / 2;
        int y2 = h - 2;
        
        g2.setColor(ExTabbedPane.TAB_BORDER_COLOR);
        g2.drawRoundRect(1, y1, w - 3, y2 - y1, 12, 12);
        
        ExUtils.disposeG2(g2, true);
    }

    @Override
    public void doLayout() {
        synchronized (getTreeLock()) {
            Dimension titleSize = titleLabel.getPreferredSize();

            int width = getWidth();

            int titleX1 = LEFT_TITLE_INSET;
            int titleX2 = width - RIGHT_TITLE_INSET;

            int titleH = titleSize.height;

            Dimension actionButtonSize = null;
            if (actionButton != null) {
                actionButtonSize = actionButton.getPreferredSize();
                titleH = Math.max(titleH, actionButtonSize.height);
            }

            int contentX = LEFT_INSET;
            int contentY = titleH + TOP_INSET;
            int contentW = width - contentX - RIGHT_INSET;
            int contentH = getHeight() - contentY - BOTTOM_INSET;

            titleLabel.setBounds(titleX1, 0, titleSize.width, titleH);
            if (actionButtonSize != null) {
                actionButton.setBounds(titleX2 - actionButtonSize.width, 
                        0, actionButtonSize.width, titleH);
            }
            
            content.setBounds(contentX, contentY, contentW, contentH);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        synchronized (getTreeLock()) {
            Dimension size = content.getPreferredSize();
            Dimension titleSize = titleLabel.getPreferredSize();

            if (actionButton != null) {
                Dimension buttonSize = actionButton.getPreferredSize();
                titleSize.height = Math.max(titleSize.height, 
                        buttonSize.height);
                titleSize.width += HORIZONTAL_TITLE_GAP + buttonSize.width;
            }
            
            size.width = LEFT_INSET 
                    + Math.max(size.width, MIN_CONTENT_WIDTH) 
                    + RIGHT_INSET;
            size.height += titleSize.height + TOP_INSET + BOTTOM_INSET;

            return size;
        }
    }

    @Override
    public Dimension getMaximumSize() {
        synchronized (getTreeLock()) {
            Dimension size = getPreferredSize();
            size.width = Integer.MAX_VALUE;
            size.height = Math.max(size.height, maxHeight);
            return size;
        }
    }
    
    public static final Color BACKGROUND_COLOR = new Color(0xFCFAF5);
    
    private static final int LEFT_TITLE_INSET = 20;
    private static final int HORIZONTAL_TITLE_GAP = 20;
    private static final int RIGHT_TITLE_INSET = 20;
    private static final int LEFT_INSET = 8;
    private static final int RIGHT_INSET = 8;
    private static final int TOP_INSET = 3;
    private static final int BOTTOM_INSET = 8;
    private static final int MIN_CONTENT_WIDTH = 100;
}
