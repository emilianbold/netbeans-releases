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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * FontModel.java
 *
 * Created on October 26, 2004, 1:49 PM
 */

package org.netbeans.modules.css.visual.model;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

/**
 * Model for the Font Style Editor data
 * @author  Winston Prakash
 * @version 1.0
 */
public class FontModel{
    
    Map fontFamilyNames = new HashMap();
    List fontFaceNames = new ArrayList();

    public FontModel(){
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = ge.getAllFonts();

        // Process each font
        for (int i=0; i<fonts.length; i++) {
            // Get font's family and face
            String familyName = fonts[i].getFamily();
            String faceName = fonts[i].getName();

            // Add font to table
            List list = (List)fontFamilyNames.get(familyName);
            if (list == null) {
                list = new ArrayList();
                fontFamilyNames.put(familyName, list);

            }
            list.add(faceName);
            fontFaceNames.add(faceName);
        }
    }


    public DefaultListModel getFontFamilySetList(){
        return new FontFamilySetList();
    }

    public DefaultListModel getFontList(){
        return new FontList();
    }

    public DefaultListModel getWebFontList(){
        return new WebFontList();
    }

    public DefaultListModel getFontFamilyList(){
        return new FontFamilyList();
    }

    public DefaultListModel getFontSizeList(){
        return new FontSizeList();
    }

    public DefaultComboBoxModel getFontSizeUnitList(){
        return new FontSizeUnitList();
    }

    public DefaultComboBoxModel getFontStyleList(){
        return new FontStyleList();
    }

    public DefaultComboBoxModel getFontSelectionList(){
        return new FontSelectionList();
    }

    public DefaultComboBoxModel getFontWeightList(){
        return new FontWeightList();
    }

    public DefaultComboBoxModel getFontVariantList(){
        return new FontVariantList();
    }

    public FontSize getFontSize(String fontSizeStr){
        return  new FontSize(fontSizeStr);
    }


    public class FontSize{
        FontSizeUnitList unitList = new FontSizeUnitList();
        String fontSizeUnit = null;
        String fontSize = null;
        public FontSize(String fontSizeStr){
            fontSize = fontSizeStr.trim();
            for(int i=0; i< unitList.getSize(); i++){
                String unit = (String)unitList.getElementAt(i);
                if(fontSize.endsWith(unit)){
                    fontSizeUnit = unit;
                    fontSize = fontSize.replaceAll(unit,"");
                }
            }
        }

        public String getUnit(){
            return fontSizeUnit;
        }

        public String getValue(){
            if(Utils.isInteger(fontSize)){
                return fontSize;
            }else{
                return null;
            }
        }
    }


    public Font resolveFont(CssStyleData styleData, Font baseFont) {
        String fontFamily = styleData.getProperty(CssProperties.FONT_FAMILY);
        String fontSize = styleData.getProperty(CssProperties.FONT_SIZE);
        String fontStyle = styleData.getProperty(CssProperties.FONT_STYLE);
        String fontVariant = styleData.getProperty(CssProperties.FONT_VARIANT);
        String fontWeight = styleData.getProperty(CssProperties.FONT_WEIGHT);
        String name =   resolveFontName(fontFamily, baseFont.getName());
        int style = resolveFontStyle(fontStyle,fontWeight, baseFont.getStyle());
        int size = resolveFontSize(fontSize, baseFont.getSize());
        return new Font(name, style, size);
    }
    
    private String resolveFontName(String fontFamily, String defName){
        String fontName = defName;
        if(fontFamily != null){
            StringTokenizer st = new StringTokenizer(fontFamily.trim(),",");
            while(st.hasMoreTokens()){
                String cssFamilyName = st.nextToken();
                cssFamilyName = cssFamilyName.replaceAll("'","");
                //Check first in the family names
                if (fontFamilyNames.containsKey(cssFamilyName)){
                    List fontFaceList = (List) fontFamilyNames.get(cssFamilyName);
                    fontName = (String) fontFaceList.get(0);
                }else if (fontFaceNames.contains(cssFamilyName)){
                    fontName = cssFamilyName;
                }
            }
        }
        return fontName;
    }
    
    private int resolveFontStyle(String fontStyle, String fontWeight, int defStyle){
        int style = defStyle;
        if((fontStyle != null) && (fontStyle.equals("italic") || fontStyle.equals("oblique"))){
            //System.out.println(fontStyle);
            style =  Font.ITALIC;
        }
        if((fontWeight != null) && fontWeight.equals(fontWeight)){
            style = style | Font.BOLD;
        }
        return style;
    }
    
    private int resolveFontSize(String fontSizeStr, int defSize){
        // XXX convert units to pixels
        int size = defSize;
        if(fontSizeStr != null){
            FontSize fontSize = new FontSize(fontSizeStr);
            String fontSizeValue = fontSize.getValue();
            if(Utils.isInteger(fontSizeValue)){
                size = Utils.getInteger(fontSizeValue);
            }else{
                if (fontSizeValue.equals("XX-small")) size = 4; //NOI18N
                if (fontSizeValue.equals("X-small")) size = 6; //NOI18N
                if (fontSizeValue.equals("small")) size = 8; //NOI18N
                if (fontSizeValue.equals("medium")) size = 12; //NOI18N
                if (fontSizeValue.equals("large")) size = 14; //NOI18N
                if (fontSizeValue.equals("X-large")) size = 16; //NOI18N
                if (fontSizeValue.equals("XX-large")) size = 20; //NOI18N
            }
            
            if (size < 4)   {
                size = 4;
            }else if( size > 72){
                size = 72;
            }
        }
        return size;
    }
    
    
    public static class FontSelectionList extends DefaultComboBoxModel{
        public FontSelectionList(){
            addElement(org.openide.util.NbBundle.getMessage(FontModel.class, "FONTS")); //NOI18N
            addElement(org.openide.util.NbBundle.getMessage(FontModel.class, "FONT_FAMILIES")); //NOI18N
            addElement(org.openide.util.NbBundle.getMessage(FontModel.class, "WEB_FONTS")); //NOI18N
        }
    }
    
    public static class FontList extends DefaultListModel{
        public FontList(){
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String fontNames[] = ge.getAvailableFontFamilyNames();
            
            // Iterate the font names and add to the model
            for (int i=0; i<fontNames.length; i++) {
                addElement(fontNames[i]);
            }
        }
    }
    
    public static class WebFontList extends DefaultListModel{
        public WebFontList(){
            addElement(CssStyleData.NOT_SET);
            String[] webFontValues = CssProperties.getWebFontValues();
            for (int i=0; i<webFontValues.length; i++) {
                addElement(webFontValues[i]);
            }
        }
    }
    
    public static class FontFamilyList extends DefaultListModel{
        public FontFamilyList(){
            addElement(CssStyleData.NOT_SET);
            String[] FontfamilyValues = CssProperties.getFontFamilyValues();
            for (int i=0; i<FontfamilyValues.length; i++) {
                addElement(FontfamilyValues[i]);
            }
        }
    }
    
    public static class FontFamilySetList extends DefaultListModel{
        public FontFamilySetList(){
            addElement(CssStyleData.NOT_SET);
            String[] FontfamilySetValues = CssProperties.getFontFamilySetValues();
            for (int i=0; i<FontfamilySetValues.length; i++) {
                addElement(FontfamilySetValues[i]);
            }
        }
    }
    
    public static class FontSizeList extends DefaultListModel{
        public FontSizeList(){
            addElement(CssStyleData.NOT_SET);
            String[] FontSizeValues = CssProperties.getFontSizeValues();
            for (int i=0; i<FontSizeValues.length; i++) {
                addElement(FontSizeValues[i]);
            }
        }
    }
    
    public static class FontSizeUnitList extends DefaultComboBoxModel{
        public FontSizeUnitList(){
            String[] unitValues = CssProperties.getCssLengthUnits();
            for(int i=0; i< unitValues.length; i++){
                addElement(unitValues[i]);
            }
        }
    }
    
    public static class FontStyleList extends DefaultComboBoxModel{
        public FontStyleList(){
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.FONT_STYLE);
            addElement(CssStyleData.NOT_SET);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }
    
    public static class FontWeightList extends DefaultComboBoxModel{
        public FontWeightList(){
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.FONT_WEIGHT);
            addElement(CssStyleData.NOT_SET);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
            setSelectedItem("px");
        }
    }
    
    public static class FontVariantList extends DefaultComboBoxModel{
        public FontVariantList(){
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.FONT_VARIANT);
            addElement(CssStyleData.NOT_SET);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }
}
