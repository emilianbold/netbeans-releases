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
package org.netbeans.modules.editor;

import java.awt.Color;
import java.awt.Font;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.PrintContainer;
import org.netbeans.editor.SettingsUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class HtmlPrintContainer implements PrintContainer {

    private static final String T_HTML_S = "<HTML>";    //NOI18N
    private static final String T_HTML_E = "</HTML>";   //NOI18N
    private static final String T_HEAD_S = "<HEAD>";    //NOI18N
    private static final String T_HEAD_E = "</HEAD>";   //NOI18N
    private static final String T_BODY_S = "<BODY>";    //NOI18N
    private static final String T_BODY_E = "</BODY>";   //NOI18N
    private static final String T_TITLE = "<TITLE>{0}</TITLE>";    //NOI18N
    private static final String T_PRE_S = "<PRE>";   //NOI18N
    private static final String T_PRE_E = "</PRE>";  //NOI18N
    private static final String T_BLOCK_S = "<FONT CLASS=\"{0}\">";  //NOI18N
    private static final String T_BLOCK_E = "</FONT>";   //NOI18N
    private static final String T_NAME_TABLE = "<TABLE WIDTH=\"100%\"><TR><TD ALIGN=\"center\">{0}</TD></TR></TABLE>";    //NOI18N
    private static final String T_CHARSET = "<META HTTP-EQUIV=\"content-type\" CONTENT=\"text/html; charset={0}\">";    //NOI18N
    private static final String T_STYLE_S = "<STYLE TYPE=\"text/css\">";    //NOI18N
    private static final String T_STYLE_E = "</STYLE>"; //NOI18N
    private static final String T_COMMENT_S = "<!--";   //NOI18N
    private static final String T_COMMENT_E = "-->";    //NOI18N
    private static final String ST_BODY = "BODY";       //NOI18N
    private static final String ST_TABLE = "TABLE";     //NOI18N
    private static final String ST_BEGIN = "{";        //NOI18N
    private static final String ST_COLOR = "color: "; //NOI18N
    private static final String ST_BGCOLOR = "background-color: ";    //NOI18N
    private static final String ST_BOLD = "font-weight: bold";    //NOI18N
    private static final String ST_ITALIC = "font-style: italic"; //NOI18N
    private static final String ST_SIZE = "font-size: "; //NOI18N
    private static final String ST_FONT_FAMILY = "font-family: ";    //NOI18N
    private static final String ST_SEPARATOR = "; ";    //NOI18N
    private static final String ST_END = "}";           //NOI18N
    private static final String EOL = "\n";             //NOI18N
    private static final String WS = " ";               //NOI18N
    private static final String ESC_LT = "&lt;";        //NOI18N
    private static final String ESC_GT = "&gt;";        //NOI18N
    private static final String ESC_AMP = "&amp;";      //NOI18N
    private static final String ESC_QUOT = "&quot;";    //NOI18N
    private static final char   ZERO    = '0';          //NOI18N
    private static final char   DOT = '.';              //NOI18N
    private static final String STYLE_PREFIX = "ST";    //NOI18N

    private Color defaultBackgroundColor;
    private Color defaultForegroundColor;
    private Color headerBackgroundColor;
    private Color headerForegroundColor;
    private Font defaultFont;
    private StringBuffer buffer;
    private String fileName;
    private Styles styles;
    private boolean[] boolHolder;
    private Map syntaxColoring;
    private String charset;

    public HtmlPrintContainer () {
    }

    public final void begin (FileObject fo, Font font, Color fgColor, Color bgColor, Color hfgColor, Color hbgColor, Class kitClass, String charset) {
        styles = new Styles ();
        buffer = new StringBuffer();
        fileName = FileUtil.getFileDisplayName(fo);
        boolHolder = new boolean [1];
        this.defaultForegroundColor = fgColor;
        this.defaultBackgroundColor = bgColor;
        this.defaultFont = font;
        this.headerForegroundColor = hfgColor;
        this.headerBackgroundColor = hbgColor;
        this.syntaxColoring = SettingsUtil.getColoringMap(kitClass, false, true);
        this.charset = charset;
    }

    public final void add(char[] chars, Font font, Color foreColor, Color backColor) {
        String text = escape(chars, boolHolder);
        String styleId = this.styles.getStyleId (font, foreColor, backColor);
        boolHolder[0]&= (styleId!=null);
        if (boolHolder[0]) {
            buffer.append(MessageFormat.format(T_BLOCK_S,new Object[]{styleId}));
        }
        buffer.append (text);
        if (boolHolder[0]) {
            buffer.append(T_BLOCK_E);
        }
    }

    public final void eol() {
        buffer.append (EOL);
    }

    public final String end () {
        StringBuffer result = new StringBuffer ();
        result.append (T_HTML_S);
        result.append (EOL);
        result.append (T_HEAD_S);
        result.append (EOL);
        result.append (MessageFormat.format (T_TITLE, new Object[] {this.fileName}));
        result.append (EOL);
        result.append (MessageFormat.format (T_CHARSET, new Object[] {this.charset}));
        result.append (EOL);
        result.append (T_STYLE_S);
        result.append (EOL);
        result.append (T_COMMENT_S);
        result.append (EOL);
        result.append (createStyle(ST_BODY,null,getDefaultFont(),getDefaultColor(),getDefaultBackgroundColor(),false));
        result.append (EOL);
        result.append (createStyle(ST_TABLE,null,getDefaultFont(),headerForegroundColor,headerBackgroundColor,false));
        result.append (EOL);
        result.append (styles.toExternalForm());
        result.append (T_COMMENT_E);
        result.append (EOL);
        result.append (T_STYLE_E);
        result.append (EOL);
        result.append (T_HEAD_E);
        result.append (EOL);
        result.append (T_BODY_S); //NOI18N
        result.append (EOL);
        result.append (MessageFormat.format (T_NAME_TABLE, new Object[] {this.fileName}));
        result.append (EOL);
        result.append (T_PRE_S);
        result.append (EOL);
        result.append (this.buffer);
        result.append (T_PRE_E);
        result.append (T_BODY_E);
        result.append (EOL);
        result.append (T_HTML_E);
        result.append (EOL);
        this.styles = null;
        this.buffer = null;
        this.fileName = null;
        this.defaultBackgroundColor = null;
        this.defaultForegroundColor = null;
        this.defaultFont = null;
        return result.toString();
    }

    public final boolean initEmptyLines() {
        return false;
    }

    private String escape (char[] buffer, boolean[] boolHolder) {
        StringBuffer result = new StringBuffer();
        boolHolder[0] = false;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == '<') {         //NOI18N
                result.append(ESC_LT);
                boolHolder[0]|=true;
            }
            else if (buffer[i] == '>') {    //NOI18N
                result.append(ESC_GT);
                boolHolder[0]|=true;
            }
            else if (buffer[i] =='&') {     //NOI18N
                result.append(ESC_AMP);
                boolHolder[0]|=true;
            }
            else if (buffer[i] =='\'') {    //NOI18N
                result.append(ESC_QUOT);
                boolHolder[0]|=true;
            }
            else if (Character.isWhitespace(buffer[i])) {
                result.append (buffer[i]);
            }
            else {
                result.append (buffer[i]);
                boolHolder[0]|=true;
            }
        }
        return result.toString();
    }

    private Color getDefaultColor () {
        return this.defaultForegroundColor;
    }

    private Color getDefaultBackgroundColor () {
        return this.defaultBackgroundColor;
    }

    private Font getDefaultFont () {
        return this.defaultFont;
    }

    private String createStyle (String element, String selector, Font font, Color fg, Color bg, boolean useDefaults) {
        StringBuffer sb = new StringBuffer();
        if (element != null) {
            sb.append (element);
            sb.append (WS);
        }

        if (selector != null) {
            sb.append (DOT);
            sb.append (selector);
            sb.append (WS);
        }

        sb.append (ST_BEGIN);
        boolean first = true;
        if ((!useDefaults || !fg.equals(getDefaultColor())) && fg != null) {
            sb.append (ST_COLOR);
            sb.append (getHtmlColor(fg));
            first = false;
        }

        if ((!useDefaults || !bg.equals (getDefaultBackgroundColor())) && bg != null) {
            if (!first) {
                sb.append (ST_SEPARATOR);
            }
            sb.append (ST_BGCOLOR);
            sb.append (getHtmlColor(bg));
            first = false;
        }

        if ((!useDefaults || !font.equals (getDefaultFont())) && font != null) {
            if (!first) {
                sb.append (ST_SEPARATOR);
            }
            sb.append (ST_FONT_FAMILY);
            sb.append (font.getFamily());   //TODO: Locale should go here
            if (font.isBold()) {
                sb.append (ST_SEPARATOR);
                sb.append (ST_BOLD);
            }
            if (font.isItalic()) {
                sb.append (ST_SEPARATOR);
                sb.append (ST_ITALIC);
            }
            Font defaultFont = getDefaultFont();
            if (defaultFont!=null && defaultFont.getSize() != font.getSize()) {
                sb.append (ST_SEPARATOR);
                sb.append (ST_SIZE);
                sb.append (String.valueOf(font.getSize()));
            }
            
        }
        sb.append (ST_END);
        return sb.toString();
    }

    private static String getHtmlColor (Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        StringBuffer result = new StringBuffer();
        result.append ("#");        //NOI18N
        String rs = Integer.toHexString (r);
        String gs = Integer.toHexString (g);
        String bs = Integer.toHexString (b);
        if (r < 0x10)
            result.append(ZERO);
        result.append(rs);
        if (g < 0x10)
            result.append (ZERO);
        result.append(gs);
        if (b < 0x10)
            result.append (ZERO);
        result.append(bs);
        return result.toString();
    }

    private class Styles {
        private Map descs;
        private int sequence;

        public Styles () {
            this.descs = new HashMap();
        }

        private boolean coloringEquals(Coloring coloring, Font f, Color fc, Color bc){
            if (coloring == null) return false;
            Font coloringFont = coloring.getFont();
            if (coloringFont == null) coloringFont = getDefaultFont();
            Color coloringForeColor = coloring.getForeColor();
            if (coloringForeColor == null) coloringForeColor = getDefaultColor();
            Color coloringBackColor = coloring.getBackColor();
            if (coloringBackColor == null) coloringBackColor = getDefaultBackgroundColor();
            
            return f.equals(coloringFont) && fc.equals(coloringForeColor) && bc.equals(coloringBackColor);
        }
        
        public final String getStyleId (Font f, Color fc, Color bc) {
            if (!fc.equals(getDefaultColor()) || !bc.equals(getDefaultBackgroundColor()) || !f.equals(getDefaultFont())) {
                StyleDescriptor sd = new StyleDescriptor (f, fc, bc);
                String id = (String) this.descs.get (sd);
                if (id == null) {
                    java.util.Set keySet = syntaxColoring.keySet();
                    Iterator iter = keySet.iterator();
                    while(iter.hasNext()){
                        Object key = iter.next();
                        if (coloringEquals((Coloring)syntaxColoring.get(key), f, fc, bc)){
                            id = (String) key;
                            break;
                        }
                    }
                    
                    if (id == null){
                        id = STYLE_PREFIX + this.sequence++;
                    }
                    sd.name = id;
                    this.descs.put (sd, id);
                }
                return id;
            }
            else {
                return null;   //No style needed
            }
        }

        public final String toExternalForm () {
            StringBuffer result = new StringBuffer();
            for (Iterator it = descs.keySet().iterator(); it.hasNext();) {
                result.append (((StyleDescriptor)it.next()).toExternalForm());
                result.append (EOL);
            }
            return result.toString();
        }

        public final String toString () {
            return this.toExternalForm();
        }

        private class StyleDescriptor {

            String name;
            private Font font;
            private Color fgColor;
            private Color bgColor;

            public StyleDescriptor (Font font, Color fgColor, Color bgColor) {
                this.font = font;
                this.fgColor = fgColor;
                this.bgColor = bgColor;
            }

            public final String getName () {
                return this.name;
            }

            public final String toExternalForm () {
                return createStyle (null,name,font,fgColor,bgColor,true);
            }

            public final String toString () {
                return this.toExternalForm();
            }

            public final boolean equals (Object object) {
                if (!(object instanceof StyleDescriptor))
                    return false;
                StyleDescriptor od = (StyleDescriptor) object;
                return coloringEquals(new Coloring(font, fgColor, bgColor), od.font, od.fgColor, od.bgColor);
            }

            public final int hashCode () {
                return this.font.hashCode() ^ this.fgColor.hashCode() ^ this.bgColor.hashCode();
            }
        }
    }
}
