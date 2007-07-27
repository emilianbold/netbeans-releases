/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax.completion;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.HashMap;
import java.text.AttributedString;
import javax.swing.*;

/**
 *
 * @author  Dusan Balek
 */
public class ResultItemPaintComponent extends JPanel {

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

    public ResultItemPaintComponent(){
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
        drawStringToGraphics(g, s, getDrawFont(), strike);
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

    public int getPreferredWidth(Graphics g, Font f) {
        setFont(f);
        draw(g);
        Insets i = getInsets();
        if (i != null) {
            drawX += i.right;
        }
        if (drawX > getMaximumSize().width)
            drawX = getMaximumSize().width;
        return drawX;
    }
    
    
    //.................. INNER CLASSES .......................

    public static class StringPaintComponent extends ResultItemPaintComponent {

        protected void draw(Graphics g){
            drawIcon(g, getIcon());
            drawString(g, str, TYPE_COLOR);
        }
    }

    
    public static class AbbrevPaintComponent extends ResultItemPaintComponent {
        private String abbrev;
        
        protected void draw(Graphics g){
            drawIcon(g, getIcon());
            drawString(g, abbrev, Color.BLUE, getDrawFont().deriveFont(Font.BOLD), false); 
            drawString(g, " " + str, TYPE_COLOR);
        }
        
        public void setAbbrev(String abbrev) {
            this.abbrev = abbrev;
        }
    }
    
    public static class JspTagPaintComponent extends ResultItemPaintComponent {
        private boolean isEmpty;
        
        public JspTagPaintComponent(boolean isEmpty) {
            this.isEmpty = isEmpty;
        }
        
        protected void draw(Graphics g){
            drawIcon(g, getIcon());
            drawString(g, "<");
            drawString(g, str, Color.BLUE, getDrawFont().deriveFont(Font.BOLD), false );
            if(isEmpty) drawString(g, "/>");
                else drawString(g, ">");
        }
    }

    public static class JspDirectivePaintComponent extends ResultItemPaintComponent {
        protected void draw(Graphics g){
            drawIcon(g, getIcon());
            drawString(g, "<%@");
            drawString(g, str, Color.BLUE, getDrawFont().deriveFont(Font.BOLD), false );
            drawString(g, " ... ", Color.GREEN.darker());
            drawString(g, "%>");
        }
    }

    public static class AttributePaintComponent extends ResultItemPaintComponent {
        private boolean mandatory;
        
        public AttributePaintComponent(boolean mandatory) {
            this.mandatory = mandatory;
        }
        
        protected void draw(Graphics g){
            drawIcon(g, getIcon());
            drawString(g, str, (mandatory ? Color.RED: Color.GREEN.darker()));
        }
    }

    // ------------------- EL CLASSES -----------------------------
    
    public static class ELPaintComponent extends ResultItemPaintComponent{
        private String typeName = null;
           
        public void draw(Graphics g){
            drawIcon(g, getIcon());
            drawString(g, str, getExpressionColor(), new Font(getDrawFont().getName(), getDrawFont().getStyle() | Font.BOLD, getDrawFont().getSize()), false);
            if (getTypeName() != null)
                drawTypeName(g, getTypeName(), getTypeColor());
        }

        public Color getExpressionColor() {
            return Color.blue;
        }

        public Color getTypeColor() {
            return Color.BLACK;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }
    
    public static class ELImplicitObjectPaintComponent extends ELPaintComponent {
        private int type = 0;
       
        private static final String OBJECT_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/class_16.png"; //NOI18N
        private static final String MAP_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/map_16.png";      //NOI18N
        
        protected Icon getIcon(){
            Icon icon = null;
            switch (type){
                case ELImplicitObjects.OBJECT_TYPE:
                    icon = new ImageIcon(org.openide.util.Utilities.loadImage(OBJECT_PATH)); break;
                case ELImplicitObjects.MAP_TYPE: 
                    icon = new ImageIcon(org.openide.util.Utilities.loadImage(MAP_PATH)); break;
            }
            return icon;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
            
        }
        
    }
    
    public static class ELBeanPaintComponent extends ELPaintComponent {
        
        public static final Color BEAN_NAME_COLOR = Color.blue.darker().darker();
        private static final String BEAN_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/bean_16.png";    //NOI18N
        
        protected Icon getIcon(){
            return new ImageIcon(org.openide.util.Utilities.loadImage(BEAN_PATH));
        }
        
        public Color getExpressionColor(){
            return BEAN_NAME_COLOR;
        }
        
    }
    
    public static class ELPropertyPaintComponent extends ELPaintComponent {
        
        public static final Color PROPERTY_NAME_COLOR = Color.blue.darker().darker();
        private static final String PROPERTY_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/property_16.png"; //NOI18N

        
        protected Icon getIcon(){
            return new ImageIcon(org.openide.util.Utilities.loadImage(PROPERTY_PATH));
        }
   
        public Color getExpressionColor(){
            return PROPERTY_NAME_COLOR;
        }
    }
    
    public static class ELFunctionPaintComponent extends ELPaintComponent {
        
        private String prefix = null;
        private String parameters = null;
        private static final Color PREFIX_COLOR = Color.blue.darker().darker();
        private static final Color FUNCTION_NAME_COLOR = Color.black;
        private static final Color PARAMETER_COLOR = Color.black;
        
        private static final String ICON_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/function_16.png";

        
        protected Icon getIcon(){
            return new ImageIcon(org.openide.util.Utilities.loadImage(ICON_PATH));
        }
   
        public Color getExpressionColor(){
            return FUNCTION_NAME_COLOR;
        }
        
        public void draw(Graphics g){
            drawIcon(g, getIcon());
            drawString(g, prefix, PREFIX_COLOR, new Font(getDrawFont().getName(), getDrawFont().getStyle() | Font.BOLD, getDrawFont().getSize()), false);
            drawString(g, ":"+str, FUNCTION_NAME_COLOR, new Font(getDrawFont().getName(), getDrawFont().getStyle() | Font.BOLD, getDrawFont().getSize()), false);
            drawParameters(g, parameters);
            if (getTypeName() != null)
                drawTypeName(g, getTypeName(), getTypeColor());
        }
        
        protected void drawParameters(Graphics g, String parList) {
            drawString(g, "(", PARAMETER_COLOR); // NOI18N
            if (parameters != null)
                drawString(g, parameters, PARAMETER_COLOR);
            drawString(g, ")", PARAMETER_COLOR);
        }
        
        public void setPrefix(String prefix){
            this.prefix = prefix;
        }
        
        public void setParameters(String parameters){
            this.parameters = parameters;
        }

    }
}
