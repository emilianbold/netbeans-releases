/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.html;

import java.lang.reflect.Modifier;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.text.AttributedString;
import java.util.ArrayList;
import javax.swing.*;
import org.openide.util.Utilities;

/**
 *
 * @author  Dusan Balek
 */
public class HTMLCompletionResultItemPaintComponent extends JPanel {
    
    static final String PACKAGE = "org/netbeans/modules/editor/resources/completion/defaultFolder.gif"; // NOI18N
    
    protected int drawX;

    protected int drawY;

    protected int drawHeight;

    private Font drawFont;

    private int iconTextGap = 5;

    private int fontHeight;

    private int ascent;

    private Map widths;

    private FontMetrics fontMetrics;

    private boolean isSelected;

    private boolean isDeprecated;

    private static final String THROWS = " throws "; // NOI18N

    private static String str; //completion item text

    private static final String[] frequentWords = new String[] {
        "", " ", "[]", "(", ")", ", ", "String", THROWS // NOI18N
    };

    public static final Color KEYWORD_COLOR = Color.darkGray;
    public static final Color TYPE_COLOR = Color.black;

    public HTMLCompletionResultItemPaintComponent(){
        super();
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    }
    
    public void setString(String str){
        this.str = str;
    }
    
    public void setSelected(boolean isSelected){
        this.isSelected = isSelected;
    }
    
    protected void setDeprecated(boolean isDeprecated){
        this.isDeprecated = isDeprecated;
    }

    public  boolean isSelected(){
        return isSelected;
    }

    protected boolean isDeprecated(){
        return isDeprecated;
    }

    protected Icon getIcon() {
        return null;
    }
    
    public void paintComponent(Graphics g) {
        // clear background
        g.setColor(getBackground());
        java.awt.Rectangle r = g.getClipBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        draw(g);
    }

    protected void draw(Graphics g){
    }

    /** Draw the icon if it is valid for the given type.
     * Here the initial drawing assignments are also done.
     */
    protected void drawIcon(Graphics g, Icon icon) {
        Insets i = getInsets();
        if (i != null) {
            drawX = i.left;
            drawY = i.top;
        } else {
            drawX = 0;
            drawY = 0;
        }

        if (icon != null) {
            if (g != null) {
                icon.paintIcon(this, g, drawX, drawY);
            }
            drawX += icon.getIconWidth() + iconTextGap;
            drawHeight = Math.max(fontHeight, icon.getIconHeight());
        } else {
            drawHeight = fontHeight;
        }
        if (i != null) {
            drawHeight += i.bottom;
        }
        drawHeight += drawY;
        drawY += ascent;
    }

    protected void drawString(Graphics g, String s){
        drawString(g, s, false);
    }

    /** Draw string using the foreground color */
    protected void drawString(Graphics g, String s, boolean strike) {
        if (g != null) {
            g.setColor(getForeground());
        }
        drawStringToGraphics(g, s, null, strike);
    }


    /** Draw string with given color which is first possibly modified
     * by calling getColor() method to care about selection etc.
     */
    protected void drawString(Graphics g, String s, Color c) {
        if (g != null) {
            g.setColor(getColor(s, c));
        }
        drawStringToGraphics(g, s);
    }

    protected void drawString(Graphics g, String s, Color c, Font font, boolean strike) {
        if (g != null) {
            g.setColor(getColor(s, c));
            g.setFont(font);
        }
        drawStringToGraphics(g, s, font,  strike);
        if (g != null) {
            g.setFont(drawFont);
        }

    }
    
    protected void drawTypeName(Graphics g, String s, Color c) {
        if (g == null) {
            drawString(g, "   "); // NOI18N
            drawString(g, s, c);
        } else {
            int w = getWidth() - getWidth(s) - drawX;
            int spaceWidth = getWidth(" "); // NOI18N
            if (w > spaceWidth * 2) {
                drawX = getWidth() - 2 * spaceWidth - getWidth(s);
            } else {
                drawX = getWidth() - 2 * spaceWidth - getWidth(s) - getWidth("...   "); // NOI18N
                g.setColor(getBackground());
                g.fillRect(drawX, 0, getWidth() - drawX, getHeight());
                drawString(g, "...   ", c); // NOI18N
            }
            drawString(g, s, c);
        }
    }

    protected void drawStringToGraphics(Graphics g, String s) {
        drawStringToGraphics(g, s, null, false);
    }

    protected void drawStringToGraphics(Graphics g, String s, Font font, boolean strike) {
        if (g != null) {
            if (!strike){
                g.drawString(s, drawX, drawY);
            }else{
                Graphics2D g2 = ((Graphics2D)g);
                AttributedString strikeText = new AttributedString(s);
                strikeText.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                strikeText.addAttribute(TextAttribute.FONT, g.getFont());
                g2.drawString(strikeText.getIterator(), drawX, drawY);
            }
        }
        drawX += getWidth(s, font);
    }

    protected int getWidth(String s) {
        Integer i = (Integer)widths.get(s);
        if (i != null) {
            return i.intValue();
        } else {
            if (s == null) {
                s = "";
            }
            return fontMetrics.stringWidth(s);
        }
    }

    protected int getWidth(String s, Font font) {
        if (font == null) return getWidth(s);
        return getFontMetrics(font).stringWidth(s);
    }

    protected Color getColor(String s, Color defaultColor) {
        return isSelected ? getForeground()
        : defaultColor;
    }

    private void storeWidth(String s) {
        fontMetrics.stringWidth(s);
    }

    public void setFont(Font font) {
        super.setFont(font);

        fontMetrics = this.getFontMetrics(font);
        fontHeight = fontMetrics.getHeight();
        ascent = fontMetrics.getAscent();
        if (widths != null) {
            widths.clear();
        } else {
            widths = new HashMap();
        }
        for (int i = 0; i < frequentWords.length; i++) {
            storeWidth(frequentWords[i]);
        }
        drawFont = font;
    }

    protected Font getDrawFont(){
        return drawFont;
    }

    public Dimension getPreferredSize() {
        draw(null);
        Insets i = getInsets();
        if (i != null) {
            drawX += i.right;
        }
        if (drawX > getMaximumSize().width)
            drawX = getMaximumSize().width;
        return new Dimension(drawX, drawHeight);
    }

    public static class StringPaintComponent extends HTMLCompletionResultItemPaintComponent {
        private Color c;
        
        public StringPaintComponent(Color c) {
            this.c = c;
        }
        
        protected void draw(Graphics g){
            drawIcon(g, getIcon());
            drawString(g, str, c);
        }
    }

}
