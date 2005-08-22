/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.editor.completion.support;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.netbeans.modules.editor.completion.PatchedHtmlRenderer;

/**
 * Various code completion utilities including completion item
 * contents rendering.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CompletionUtilities {
    
    /**
     * The gap between left edge and icon.
     */
    private static final int BEFORE_ICON_GAP = 1;
    
    /**
     * The gap between icon and the left text.
     */
    private static final int AFTER_ICON_GAP = 4;
    
    /**
     * By default 16x16 icons should be used.
     */
    private static final int ICON_HEIGHT = 16;
    private static final int ICON_WIDTH = 16;
    
    /**
     * The gap between left and right text.
     */
    private static final int BEFORE_RIGHT_TEXT_GAP = 5;

    /**
     * The gap between right text and right edge.
     */
    private static final int AFTER_RIGHT_TEXT_GAP = 3;
    
    private CompletionUtilities() {
        // no instances
    }
    
    public static int getPreferredWidth(String leftHtmlText, String rightHtmlText,
    Graphics g, Font defaultFont) {
        int width = BEFORE_ICON_GAP + ICON_WIDTH + AFTER_ICON_GAP + AFTER_RIGHT_TEXT_GAP;
        if (leftHtmlText != null) {
            width += (int)PatchedHtmlRenderer.renderHTML(leftHtmlText, g, 0, 0, Integer.MAX_VALUE, 0,
                    defaultFont, Color.black, PatchedHtmlRenderer.STYLE_CLIP, false, true);
        }
        if (rightHtmlText != null) {
            if (leftHtmlText != null) {
                width += BEFORE_RIGHT_TEXT_GAP;
            }
            width += (int)PatchedHtmlRenderer.renderHTML(rightHtmlText, g, 0, 0, Integer.MAX_VALUE, 0,
                    defaultFont, Color.black, PatchedHtmlRenderer.STYLE_CLIP, false, true);
        }
        return width;
    }
    
    public static void renderHtml(ImageIcon icon, String leftHtmlText, String rightHtmlText,
    Graphics g, Font defaultFont, Color defaultColor,
    int width, int height, boolean selected) {
        if (icon != null) {
            // The image of the ImageIcon should already be loaded
            // so no ImageObserver should be necessary
            boolean done = g.drawImage(icon.getImage(), BEFORE_ICON_GAP, 0, null);
            assert (done);
        }
        int iconWidth = BEFORE_ICON_GAP + ICON_WIDTH + AFTER_ICON_GAP;
        int rightTextX = width - AFTER_RIGHT_TEXT_GAP;
        FontMetrics fm = g.getFontMetrics(defaultFont);
        int textY = (height - fm.getHeight())/2 + fm.getHeight() - fm.getDescent();
        if (rightHtmlText != null) {
            int rightTextWidth = (int)PatchedHtmlRenderer.renderHTML(rightHtmlText, g, 0, 0, Integer.MAX_VALUE, 0,
                    defaultFont, defaultColor, PatchedHtmlRenderer.STYLE_CLIP, false, true);
            rightTextX = Math.max(iconWidth, rightTextX - rightTextWidth);
            // Render right text
            PatchedHtmlRenderer.renderHTML(rightHtmlText, g, rightTextX, textY, rightTextWidth, textY,
                defaultFont, defaultColor, PatchedHtmlRenderer.STYLE_CLIP, true, selected);
            rightTextX = Math.max(iconWidth, rightTextX - BEFORE_RIGHT_TEXT_GAP);
        }

        // Render left text
        if (leftHtmlText != null && rightTextX > iconWidth) { // any space for left text?
            PatchedHtmlRenderer.renderHTML(leftHtmlText, g, iconWidth, textY, rightTextX - iconWidth, textY,
                defaultFont, defaultColor, PatchedHtmlRenderer.STYLE_TRUNCATE, true, selected);
        }
    }
    
    public static void renderHtml(ImageIcon icon, String leftHtmlText, String rightHtmlText,
    Graphics g, Font defaultFont, Color defaultColor,
    int width, int height, boolean selected, boolean preferLeftText) {
        if (!preferLeftText) {
            renderHtml(icon, leftHtmlText, rightHtmlText, g, defaultFont, defaultColor, width, height, selected);
        } else {
            // Prefer left text to be fully displayed
            if (icon != null) {
                // The image of the ImageIcon should already be loaded
                // so no ImageObserver should be necessary
                boolean done = g.drawImage(icon.getImage(), BEFORE_ICON_GAP, 0, null);
                assert (done);
            }
            int iconWidth = BEFORE_ICON_GAP + ICON_WIDTH + AFTER_ICON_GAP;
            int textWidth = width - iconWidth - AFTER_RIGHT_TEXT_GAP;
            FontMetrics fm = g.getFontMetrics(defaultFont);
            int textY = (height - fm.getHeight())/2 + fm.getHeight() - fm.getDescent();
            int textX = iconWidth;
            if (leftHtmlText != null) {
                int leftTextWidth = (int)PatchedHtmlRenderer.renderHTML(
                        leftHtmlText, g, iconWidth, textY, textX, textY,
                        defaultFont, defaultColor,
                        PatchedHtmlRenderer.STYLE_CLIP, true, selected
                );
                leftTextWidth += BEFORE_RIGHT_TEXT_GAP;
                textX += leftTextWidth;
                textWidth -= leftTextWidth;
            }
            
            // Render left text
            if (rightHtmlText != null && textWidth > 0) { // any space for right text?
                int rightTextWidth = (int)PatchedHtmlRenderer.renderHTML(
                        rightHtmlText, g, 0, 0, Integer.MAX_VALUE, 0,
                        defaultFont, defaultColor,
                        PatchedHtmlRenderer.STYLE_CLIP, false, true
                );
                // Shift right text more to the right if too narrow
                if (rightTextWidth < textWidth) {
                    textX += textWidth - rightTextWidth;
                }

                PatchedHtmlRenderer.renderHTML(rightHtmlText, g,
                        textX, textY, textWidth, textY,
                        defaultFont, defaultColor,
                        PatchedHtmlRenderer.STYLE_TRUNCATE, true, selected
                );
            }
            
        }
    }

}
