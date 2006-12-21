/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.modelutil;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;

import org.netbeans.modules.cnd.api.model.CsmNamespace;

/**
 *
 * @author  Vladimir Voskresensky
 * after JCPaintComponent
 */

public abstract class CsmPaintComponent extends JPanel {
            
    protected DrawState drawState = new DrawState();
    
    protected Font drawFont;
           
    private int iconTextGap = 5;
            
    private int fontHeight;
    
    private int ascent;
    
    private Map widths;
    
    private FontMetrics fontMetrics;
    
    protected boolean isSelected;
    
   // private String text;
    
    private ArrayList postfixes;
    
    private static final String THROWS = " throws "; // NOI18N
    
    
    private static final String[] frequentWords = new String[] {
        "", " ", "[]", "(", ")", ", ", "String", THROWS // NOI18N
    };
    
    private static final Color KEYWORD_COLOR = Color.darkGray;
    private static final Color TYPE_COLOR = Color.black;
    private static final Color POSTFIX_COLOR = Color.gray;
    
    private Icon icon;
    
    protected int modifiers = 0;
    
    
    public CsmPaintComponent(){
        super();        
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        postfixes = new ArrayList();        
    }
    
    public void setSelected(boolean isSelected){
        this.isSelected = isSelected;
    }
    
    protected boolean isSelected(){
        return isSelected;
    }
    
    public void paintComponent(Graphics g) {
        // clear background
        g.setColor(getBackground());
        java.awt.Rectangle r = g.getClipBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        draw(g);
        
        if(!postfixes.isEmpty()) {
            drawString(g, " (", POSTFIX_COLOR);
            Iterator iter = postfixes.iterator();
            while(iter.hasNext()) {
                ((PostfixString) iter.next()).Draw(g);
                if(iter.hasNext()) {
                    drawString(g, ",  ", POSTFIX_COLOR);
                }
            } 
            drawString(g, ")", POSTFIX_COLOR);                        
        }        
    }
    
    public void appendPostfix(String text, Color c, int font) {
        postfixes.add(new PostfixString(text, c, font));
    }
    
    public void removePostfixes() {
        postfixes.clear();
    }
    
    public boolean hasPostfixes() {
        return !postfixes.isEmpty();
    }
    
    /** IMPORTANT:
     * when implemented => have to update toString!
     */
    abstract protected void draw(Graphics g);
    
    /**
     * returns string representation of paint item
     * IMPORTANT: have to be in sync with draw() method
     */   
    abstract public String toString();
    
    protected void setIcon(Icon icon){
        this.icon = icon;
    }
    
    protected Icon getIcon(){
        return icon;
    }
    
    
    /** Draw the icon if it is valid for the given type.
     * Here the initial drawing assignments are also done.
     */
    protected void drawIcon(Graphics g, Icon icon) {
        Insets i = getInsets();
        if (i != null) {
            drawState.drawX = i.left;
            drawState.drawY = i.top;
        } else {
            drawState.drawX = 0;
            drawState.drawY = 0;
        }
        
        if (icon != null) {
            if (g != null) {
                icon.paintIcon(this, g, drawState.drawX, drawState.drawY);
            }
            drawState.drawX += icon.getIconWidth() + iconTextGap;
            drawState.drawHeight = Math.max(fontHeight, icon.getIconHeight());
        } else {
            drawState.drawHeight = fontHeight;
        }
        if (i != null) {
            drawState.drawHeight += i.bottom;
        }
        drawState.drawHeight += drawState.drawY;
        drawState.drawY += ascent;
    }
    
    protected void drawType(Graphics g, CsmType typ) {
        drawType(g, typ, false);
    }
    
    protected void drawType(Graphics g, CsmType typ, boolean strike) {
        Color c = getTypeColor(typ.getClassifier().getName());
        drawString(g, typ.getText(), c, null, strike);
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
    
    protected void drawStringToGraphics(Graphics g, String s) {
        drawStringToGraphics(g, s, null, false);
    }
    
    protected void drawStringToGraphics(Graphics g, String s, Font font, boolean strike) {
        if (g != null) {
            if (!strike){
                g.drawString(s, drawState.drawX, drawState.drawY);
            }else{
                Graphics2D g2 = ((Graphics2D)g);
                AttributedString strikeText = new AttributedString(s);
                strikeText.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                strikeText.addAttribute(TextAttribute.FONT, g.getFont());
                g2.drawString(strikeText.getIterator(), drawState.drawX, drawState.drawY);
            }
        }
        drawState.drawX += getWidth(s, font);
    }
    
    protected int getWidth(String s) {
        Integer i = (Integer)widths.get(s);
        if (i != null) {
            return i.intValue();
        } else {
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
//        Iterator i = CsmUtilities.getPrimitiveClassIterator();
//        while (i.hasNext()) {
//            storeWidth(((CsmClass)i.next()).getName());
//        }
         drawFont = font;
    }
    
    protected Font getDrawFont(){
        return drawFont;
    }
    
    protected Color getTypeColor(String s) {
        return (CsmUtilities.isPrimitiveClassName(s))
        ? KEYWORD_COLOR : TYPE_COLOR;
    }
    
    public Dimension getPreferredSize() {
        draw(null);
        Insets i = getInsets();
        if (i != null) {
            drawState.drawX += i.right;
        }
        return new Dimension(drawState.drawX, drawState.drawHeight);
    }
    
    public void setModifiers(int modifiers){
        this.modifiers = modifiers;
    }

    public int getModifiers(){
        return modifiers;
    }
      
    DrawState getDrawState() {
        return drawState;
    }
    
    void setDrawState(DrawState drawState) {
        this.drawState = drawState;
    }
    
    //.................. INNER CLASSES .......................
    
    private class PostfixString {
        private String text;
        private Color c;
        private int fontStyle;
        
        public PostfixString(String text, Color c, int fontStyle) {
            this.text = text;
            this.c = c;
            this.fontStyle = fontStyle;
        }
        
        public PostfixString(String text, int fontStyle) {
            this(text, CsmPaintComponent.this.POSTFIX_COLOR, fontStyle);            
        }
        
        void Draw(Graphics g) {            
            CsmPaintComponent.this.drawString(g, text, c, new Font(getDrawFont().getName(), 
                                                                   getDrawFont().getStyle() | fontStyle, 
                                                                   getDrawFont().getSize()),
                                               false);
                                        
        }        
    }
    
    private class DrawState {
        int drawX, drawY;
        int drawHeight;    
               
        public DrawState() {
            drawX = drawY = drawHeight = 0;
        }        
    }
    
    public static class NamespacePaintComponent extends CsmPaintComponent{
        
        private CsmNamespace pkg;
        private String pkgName;
        private boolean displayFullNamespacePath;
        private Color NAMESPACE_COLOR = Color.green.darker().darker().darker();
        
        public NamespacePaintComponent(){
            super();
        }
        
        public void setNamespaceName(String pkgName){
            this.pkgName = pkgName;
        }
        
        public void setDisplayFullNamespacePath(boolean displayFullNamespacePath){
            this.displayFullNamespacePath = displayFullNamespacePath;
        }
        
        protected void draw(Graphics g) {
            // IMPORTANT:
            // when updated => have to update toString!
            drawIcon(g, getIcon());
            String name = pkgName;
            if (!displayFullNamespacePath) {
                name = name.substring(name.lastIndexOf('.') + 1);
            }
            drawString(g, name,	    NAMESPACE_COLOR);
        }
        
        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        public String toString() {
            return pkgName;
        }
    }
    
    public static class EnumPaintComponent extends CsmPaintComponent {
        
        String formatEnumName;
        private Color ENUM_COLOR = Color.red.darker().darker().darker().darker();
        private boolean displayFQN;
        
        public void EnumPaintComponent(String formatEnumName){
            this.formatEnumName = formatEnumName;
        }
        
        protected Color getColor(){
            return ENUM_COLOR;
        }
        
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, formatEnumName, getColor(), null, strike);
        }
        
        public void setFormatEnumName(String formatEnumName){
            this.formatEnumName = formatEnumName;
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        public String toString() {
            return formatEnumName;
        }
    }
    
    public static class EnumeratorPaintComponent extends CsmPaintComponent {
        
        String formatEnumeratorName;
        private Color ENUMERATOR_COLOR = Color.blue.darker().darker().darker().darker();
        private boolean displayFQN;
        
        public void EnumeratorPaintComponent(String formatEnumeratorName){
            this.formatEnumeratorName = formatEnumeratorName;
        }
        
        protected Color getColor(){
            return ENUMERATOR_COLOR;
        }
        
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, formatEnumeratorName, getColor(), null, strike);
        }
        
        public void setFormatEnumeratorName(String formatEnumeratorName){
            assert(formatEnumeratorName != null);
            this.formatEnumeratorName = formatEnumeratorName;
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        public String toString() {
            return formatEnumeratorName;
        }
    }
    
    public static class ClassPaintComponent extends CsmPaintComponent{
        
        String formatClassName;
        private Color CLASS_COLOR = Color.red.darker().darker().darker();
        private boolean displayFQN;
        
        public void setFormatClassName(String formatClassName){
            this.formatClassName = formatClassName;
        }
        
        protected Color getColor(){
            return CLASS_COLOR;
        }
        
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, formatClassName, getColor(), null, strike);
        }
      
        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        public String toString() {
            return formatClassName;
        }        
    }

    
    public static class TypedefPaintComponent extends CsmPaintComponent{
        
        String formatTypedefName;
        private Color TYPEDEF_COLOR = Color.blue.darker().darker().darker();
        private boolean displayFQN;
        
        public void setFormatTypedefName(String formatTypedefName){
            this.formatTypedefName = formatTypedefName;
        }
        
        protected Color getColor(){
            return TYPEDEF_COLOR;
        }
        
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, formatTypedefName, getColor(), null, strike);
        }
      
        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        public String toString() {
            return formatTypedefName;
        }        
    }

    public static class StructPaintComponent extends ClassPaintComponent{
        
        private Color STRUCT_COLOR = Color.red.darker().darker();
        
        protected Color getColor(){
            return STRUCT_COLOR;
        }
        
        public StructPaintComponent(){
            super();
        }
    }
    
    
    public static class UnionPaintComponent extends ClassPaintComponent{
        
        private Color UNION_COLOR = Color.red.darker();
        
        protected Color getColor(){
            return UNION_COLOR;
        }
        
        public UnionPaintComponent(){
            super();
        }
    }
    
    public static class FieldPaintComponent extends CsmPaintComponent{
        private Color FIELD_COLOR = Color.blue.darker();
        protected String typeName;
        protected Color typeColor;
        protected String fldName;
        
        public FieldPaintComponent() {
            super();
        }
        
        public Color getNameColor() {
            return FIELD_COLOR;
        }
        
        public void setName(String fldName){
            this.fldName= fldName;
        }
        
        public void setTypeColor(Color typeColor){
            this.typeColor = typeColor;
        }
        
        public void setTypeName(String typeName){
            this.typeName = typeName;
        }
        
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            int level = CsmUtilities.getLevel(modifiers);
            drawIcon(g, getIcon());
            
            drawString(g, typeName, typeColor, null, strike);
            drawString(g, " ", strike); // NOI18N
            if ((modifiers & CsmUtilities.LOCAL_MEMBER_BIT) != 0){
                // it is local field, draw as bold
                drawString(g, fldName, getNameColor(), new Font(getDrawFont().getName(), getDrawFont().getStyle() | Font.BOLD, getDrawFont().getSize()), strike);
            }else{
                drawString(g, fldName, getNameColor() , null, strike);
            }
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append(typeName);
            buf.append(' ');
            buf.append(fldName);
            return buf.toString();
        }        
    }
    
    public static class LocalVariablePaintComponent extends FieldPaintComponent {
        private Color VARIABLE_COLOR = Color.red.darker().darker().darker();
        
        public LocalVariablePaintComponent(){
            super();
            this.modifiers |= CsmUtilities.LOCAL_MEMBER_BIT | this.modifiers;
        }
        
        public Color getNameColor() {
            return VARIABLE_COLOR;
        }

        public void setModifiers(int modifiers) {
            super.setModifiers(modifiers | CsmUtilities.LOCAL_MEMBER_BIT);
        }
    }
    
    public static class FileLocalVariablePaintComponent extends FieldPaintComponent {
        private Color VARIABLE_COLOR = Color.blue.darker().darker().darker();
        
        public FileLocalVariablePaintComponent(){
            super();
        }
        
        public Color getNameColor() {
            return VARIABLE_COLOR;
        }
    }
    
    public static class GlobalVariablePaintComponent extends FieldPaintComponent {
        private Color VARIABLE_COLOR = Color.blue.darker().darker().darker();
        
        public GlobalVariablePaintComponent(){
            super();
        }
        
        public Color getNameColor() {
            return VARIABLE_COLOR;
        }
    }
    
    public static class MacroPaintComponent extends CsmPaintComponent{
        private Color MACRO_NAME_COLOR = Color.green.darker().darker();
        private Color MACRO_PARAMETER_NAME_COLOR = Color.magenta.darker();
        private List params = new ArrayList();
        private String name;

        public MacroPaintComponent(){
            super();
        }

        public String getName(){
            return name;
        }
        
        public void setName(String name){
            this.name = name;
        }
        
        public void setParams(List params){
            this.params = params;
        }
        
        protected List getParamList(){
            return params;
        }

        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, getName(), MACRO_NAME_COLOR, null, strike);
            drawParameterList(g, getParamList(), strike);
        }

        protected void drawParameterList(Graphics g, List prmList, boolean strike) {
            if (prmList == null || prmList.size()==0){
                return;
            }
            drawString(g, "(", strike); // NOI18N
            for (Iterator it = prmList.iterator(); it.hasNext();) {
                drawString(g, (String)it.next(), MACRO_PARAMETER_NAME_COLOR, null, strike);
                if (it.hasNext()) {
                    drawString(g, ", ", strike); // NOI18N
                }
            }
            drawString(g, ")", strike); // NOI18N
        }

        protected String toStringParameterList(List prmList) {
            if (prmList == null || prmList.size()==0){
                return "";
            }
            StringBuffer buf = new StringBuffer();
            buf.append('('); // NOI18N
            for (Iterator it = prmList.iterator(); it.hasNext();) {
                buf.append((String)it.next());
                if (it.hasNext()) {
                    buf.append(", "); // NOI18N
                }
            }
            buf.append(')'); // NOI18N
            return buf.toString();
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            //macro name
            buf.append(getName());
            //macro params
            buf.append(toStringParameterList(getParamList()));
            return buf.toString();
        }    
    }
    
    
    public static class ConstructorPaintComponent extends CsmPaintComponent{
        
        private Color CONSTRUCTOR_COLOR = Color.orange.darker().darker();
        private Color PARAMETER_NAME_COLOR = Color.magenta.darker();
        private List params = new ArrayList();
        private List excs = new ArrayList();
        private String name;
        
        public ConstructorPaintComponent(){
            super();
        }
        
        public int getMethodModifiers(){
            return modifiers;
        }
        
        public String getName(){
            return name;
        }
        
        public void setName(String name){
            this.name = name;
        }
        
        public void setParams(List params){
            this.params = params;
        }
        
        public void setExceptions(List excs){
            this.excs = excs;
        }
        
        protected List getParamList(){
            return params;
        }
        
        protected List getExceptionList(){
            return excs;
        }
        
        protected void drawParameter(Graphics g, ParamStr prm) {
            drawParameter(g, prm, false);
        }
        
        protected void drawParameter(Graphics g, ParamStr prm, boolean strike) {
            
            //drawType
            drawString(g, prm.getSimpleTypeName(), prm.getTypeColor(), null, strike);
            
            String name = prm.getName();
            if (name != null && name.length() > 0) {
                drawString(g, " ", strike); // NOI18N
                drawString(g, prm.getName(), PARAMETER_NAME_COLOR, null, strike);
            }
        }
        
        protected void drawParameterList(Graphics g, List prmList) {
            drawParameterList(g, prmList, false);
        }
        
        protected void drawParameterList(Graphics g, List prmList, boolean strike) {
            drawString(g, "(", strike); // NOI18N
            for (Iterator it = prmList.iterator(); it.hasNext();) {
                drawParameter(g, (ParamStr)it.next(), strike);
                if (it.hasNext()) {
                    drawString(g, ", ", strike); // NOI18N
                }
            }
            drawString(g, ")", strike); // NOI18N
        }
        
        protected void drawExceptions(Graphics g, List exc, boolean strike) {
            if (exc.size() > 0) {
                drawString(g, THROWS, KEYWORD_COLOR, null, strike);
                for (Iterator it = exc.iterator(); it.hasNext();) {
                    ExceptionStr ex = (ExceptionStr) it.next();
                    drawString(g, ex.getName(), ex.getTypeColor(), null, strike);
                    if (it.hasNext()) {
                        drawString(g, ", ", strike); // NOI18N
                    }
                    
                }
            }
        }
        
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            int level = CsmUtilities.getLevel(getModifiers());
            drawIcon(g, getIcon());
            drawString(g, getName(), CONSTRUCTOR_COLOR, null, strike);
            drawParameterList(g, getParamList(), strike);
            drawExceptions(g, getExceptionList(), strike);
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            //constructor name
            buf.append(getName());
            //constructor params
            buf.append(toStringParameterList(getParamList()));
            //constructor exceptions
            buf.append(toStringExceptions(getExceptionList()));
            return buf.toString();
        }    
        
        protected String toStringParameter(ParamStr prm) {
            StringBuffer buf = new StringBuffer();
            //type
            buf.append(prm.getSimpleTypeName());
            //name
            String name = prm.getName();
            if (name != null && name.length() > 0) {
                buf.append(' '); // NOI18N
                buf.append(prm.getName());
            }
            return buf.toString();
        }
        
        protected String toStringParameterList(List prmList) {
            StringBuffer buf = new StringBuffer();
            buf.append('('); // NOI18N
            for (Iterator it = prmList.iterator(); it.hasNext();) {
                buf.append(toStringParameter((ParamStr)it.next()));
                if (it.hasNext()) {
                    buf.append(", "); // NOI18N
                }
            }
            buf.append(')'); // NOI18N
            return buf.toString();
        }
        
        protected String toStringExceptions(List exc) {
            StringBuffer buf = new StringBuffer();
            if (exc.size() > 0) {
                buf.append(THROWS);
                for (Iterator it = exc.iterator(); it.hasNext();) {
                    ExceptionStr ex = (ExceptionStr) it.next();
                    buf.append(ex.getName());
                    if (it.hasNext()) {
                        buf.append(", "); // NOI18N
                    }
                    
                }
            }
            return buf.toString();
        }
    }
    
    public static class MethodPaintComponent extends ConstructorPaintComponent {
        
        private Color PARAMETER_NAME_COLOR = Color.magenta.darker();
        private Color METHOD_COLOR = Color.red.darker().darker();
        private String typeName;
        private Color typeColor;
        
        public MethodPaintComponent(){
            super();
        }
        
        public Color getNameColor() {
            return METHOD_COLOR;
        }
        
        public String getTypeName(){
            return typeName;
        }
        
        public Color getTypeColor(){
            return typeColor;
        }
        
        public void setTypeName(String typeName){
            this.typeName = typeName;
        }
        
        public void setTypeColor(Color typeColor){
            this.typeColor = typeColor;
        }
        
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            int level = CsmUtilities.getLevel(getModifiers());
            drawIcon(g, getIcon());
            
            drawString(g, getTypeName(), getTypeColor(), null, strike);
            drawString(g, " ", strike); // NOI18N
            if ((getModifiers() & CsmUtilities.LOCAL_MEMBER_BIT) != 0){
                drawString(g, getName(), getNameColor() , new Font(getDrawFont().getName(), getDrawFont().getStyle() | Font.BOLD, getDrawFont().getSize()), strike);
            }else{
                drawString(g, getName(), getNameColor(), null, strike);
            }
            drawParameterList(g, getParamList(), strike);
            drawExceptions(g, getExceptionList(), strike);
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            //return type
            buf.append(getTypeName());
            buf.append(' '); // NOI18N
            //method name
            buf.append(getName());
            //method params
            buf.append(toStringParameterList(getParamList()));
            //method exceptions
            buf.append(toStringExceptions(getExceptionList()));
            return buf.toString();            
        }        
    }
    
    public static class GlobalFunctionPaintComponent extends MethodPaintComponent {
        private Color FUN_COLOR = Color.red.darker().darker();
        
        public GlobalFunctionPaintComponent(){
            super();
        }
        
        public Color getNameColor() {
            return FUN_COLOR;
        }
    }
    
    public static class CsmPaintComponentWrapper extends CsmPaintComponent {    
        private CsmPaintComponent comp;

        public CsmPaintComponentWrapper() {
            super();        
        }
        
        public void setCsmComponent(CsmPaintComponent comp) {           
            this.comp = comp;
        }

        protected void draw(Graphics g) {
            if (comp != null) {
                comp.draw(g);            
                setDrawState(comp.getDrawState());
            }
        }
        
        public void setFont(Font font) {
            super.setFont(font);
            if (comp != null)
                 comp.setFont(font);
        }
        
        public String toString() {
            if (comp != null)
                return comp.toString();
            return "";
        }    
    } 
    
}
