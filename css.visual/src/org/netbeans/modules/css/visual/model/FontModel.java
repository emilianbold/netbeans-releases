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

/*
 * FontModel.java
 *
 * Created on October 26, 2004, 1:49 PM
 */

package org.netbeans.modules.css.visual.model;

import org.netbeans.modules.css.editor.model.CssRuleContent;
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
    
    Map<String, List<String>> fontFamilyNames = new HashMap<String, List<String>>();
    List<String> fontFaceNames = new ArrayList<String>();

    public FontModel(){
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = ge.getAllFonts();

        // Process each font
        for (int i=0; i<fonts.length; i++) {
            // Get font's family and face
            String familyName = fonts[i].getFamily();
            String faceName = fonts[i].getName();

            // Add font to table
            List<String> list = fontFamilyNames.get(familyName);
            if (list == null) {
                list = new ArrayList<String>();
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
            if(Utils.isFloat(fontSize)){
                return fontSize;
            }else{
                return null;
            }
        }
    }


    public Font resolveFont(CssRuleContent styleData, Font baseFont) {
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
            addElement(CssRuleContent.NOT_SET);
            String[] webFontValues = CssProperties.getWebFontValues();
            for (int i=0; i<webFontValues.length; i++) {
                addElement(webFontValues[i]);
            }
        }
    }
    
    public static class FontFamilyList extends DefaultListModel{
        public FontFamilyList(){
            addElement(CssRuleContent.NOT_SET);
            String[] FontfamilyValues = CssProperties.getFontFamilyValues();
            for (int i=0; i<FontfamilyValues.length; i++) {
                addElement(FontfamilyValues[i]);
            }
        }
    }
    
    public static class FontFamilySetList extends DefaultListModel{
        public FontFamilySetList(){
            addElement(CssRuleContent.NOT_SET);
            String[] FontfamilySetValues = CssProperties.getFontFamilySetValues();
            for (int i=0; i<FontfamilySetValues.length; i++) {
                addElement(FontfamilySetValues[i]);
            }
        }
    }
    
    public static class FontSizeList extends DefaultListModel{
        public FontSizeList(){
            addElement(CssRuleContent.NOT_SET);
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
            addElement(CssRuleContent.NOT_SET);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }
    
    public static class FontWeightList extends DefaultComboBoxModel{
        public FontWeightList(){
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.FONT_WEIGHT);
            addElement(CssRuleContent.NOT_SET);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
            setSelectedItem("px");
        }
    }
    
    public static class FontVariantList extends DefaultComboBoxModel{
        public FontVariantList(){
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.FONT_VARIANT);
            addElement(CssRuleContent.NOT_SET);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }
}
