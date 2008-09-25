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
 * CssProperties.java
 * Created on December 17, 2004, 11:35 AM
 */

package org.netbeans.modules.css.visual.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

//XXX May be we should convert this in to a singleton class
// Unnecessary static initialization

/**
 * CSS properties information
 * @author Winston   Prakash
 * @version 1.0
 */
public final class CssProperties {
    
    public final static String FONT_FAMILY = "font-family"; //NOI18N
    public final static String FONT_SIZE = "font-size"; //NOI18N
    public final static String FONT_STYLE = "font-style"; //NOI18N
    public final static String FONT_WEIGHT = "font-weight"; //NOI18N
    public final static String FONT_VARIANT = "font-variant"; //NOI18N
    
    public final static String TEXT_DECORATION = "text-decoration"; //NOI18N
    public final static String TEXT_ALIGN = "text-align"; //NOI18N
    public final static String TEXT_INDENT = "text-indent"; //NOI18N
    
    public final static String COLOR = "color"; //NOI18N
    
    public final static String BACKGROUND = "background"; //NOI18N
    public final static String BACKGROUND_COLOR = "background-color"; //NOI18N
    public final static String BACKGROUND_IMAGE = "background-image"; //NOI18N
    public final static String BACKGROUND_REPEAT = "background-repeat"; //NOI18N
    public final static String BACKGROUND_ATTACHMENT = "background-attachment"; //NOI18N
    public final static String BACKGROUND_POSITION = "background-position"; //NOI18N
    
    public final static String DIRECTION = "direction"; //NOI18N
    public final static String LINE_HEIGHT = "line-height"; //NOI18N
    public final static String VERTICAL_ALIGN = "vertical-align"; //NOI18N
    
    public final static String WORD_SPACING = "word-spacing"; //NOI18N
    public final static String LETTER_SPACING = "letter-spacing"; //NOI18N
    
    public final static String BORDER = "border"; //NOI18N
    public final static String BORDER_TOP = "border-top"; //NOI18N
    public final static String BORDER_BOTTOM = "border-bottom"; //NOI18N
    public final static String BORDER_LEFT = "border-left"; //NOI18N
    public final static String BORDER_RIGHT = "border-right"; //NOI18N
    
    public final static String BORDER_COLOR = "border-color"; //NOI18N
    public final static String BORDER_STYLE = "border-style"; //NOI18N
    public final static String BORDER_WIDTH = "border-width"; //NOI18N
    
    public final static String BORDER_TOP_COLOR = "border-top-color"; //NOI18N
    public final static String BORDER_TOP_STYLE = "border-top-style"; //NOI18N
    public final static String BORDER_TOP_WIDTH = "border-top-width"; //NOI18N
    
    public final static String BORDER_BOTTOM_COLOR = "border-bottom-color"; //NOI18N
    public final static String BORDER_BOTTOM_STYLE = "border-bottom-style"; //NOI18N
    public final static String BORDER_BOTTOM_WIDTH = "border-bottom-width"; //NOI18N
    
    public final static String BORDER_LEFT_COLOR = "border-left-color"; //NOI18N
    public final static String BORDER_LEFT_STYLE = "border-left-style"; //NOI18N
    public final static String BORDER_LEFT_WIDTH = "border-left-width"; //NOI18N
    
    public final static String BORDER_RIGHT_COLOR = "border-right-color"; //NOI18N
    public final static String BORDER_RIGHT_STYLE = "border-right-style"; //NOI18N
    public final static String BORDER_RIGHT_WIDTH = "border-right-width"; //NOI18N
    
    public final static String MARGIN = "margin"; //NOI18N
    public final static String MARGIN_TOP = "margin-top"; //NOI18N
    public final static String MARGIN_BOTTOM = "margin-bottom"; //NOI18N
    public final static String MARGIN_LEFT = "margin-left"; //NOI18N
    public final static String MARGIN_RIGHT = "margin-right"; //NOI18N
    
    public final static String PADDING = "padding"; //NOI18N
    public final static String PADDING_TOP = "padding-top"; //NOI18N
    public final static String PADDING_BOTTOM = "padding-bottom"; //NOI18N
    public final static String PADDING_LEFT = "padding-left"; //NOI18N
    public final static String PADDING_RIGHT = "padding-right"; //NOI18N
    
    public final static String POSITION = "position"; //NOI18N
    public final static String TOP = "top"; //NOI18N
    public final static String BOTTOM = "bottom"; //NOI18N
    public final static String LEFT = "left"; //NOI18N
    public final static String RIGHT = "right"; //NOI18N
    
    public final static String WIDTH = "width"; //NOI18N
    public final static String HEIGHT = "height"; //NOI18N
    public final static String MIN_WIDTH = "min-width"; //NOI18N
    public final static String MAX_WIDTH = "max-width"; //NOI18N
    public final static String MIN_HEIGHT = "min-height"; //NOI18N
    public final static String MAX_HEIGHT = "max-height"; //NOI18N
    
    public final static String Z_INDEX = "z-index"; //NOI18N
    public final static String VISIBILITY = "visibility"; //NOI18N
    
    public final static String CLIP = "clip"; //NOI18N
    
    public final static String STYLE = "style"; //NOI18N
    
    private static Set<String> cssPropertyNames = new  TreeSet<String>();
    private static void setCssPropertyNames(){
        cssPropertyNames.add(BACKGROUND);
        cssPropertyNames.add(BACKGROUND_ATTACHMENT);
        cssPropertyNames.add(BACKGROUND_COLOR);
        cssPropertyNames.add(BACKGROUND_IMAGE);
        cssPropertyNames.add(BACKGROUND_POSITION);
        cssPropertyNames.add(BACKGROUND_REPEAT);
        
        cssPropertyNames.add(BORDER);
        cssPropertyNames.add(BORDER_COLOR);
        cssPropertyNames.add(BORDER_STYLE);
        cssPropertyNames.add(BORDER_WIDTH);
        
        cssPropertyNames.add(BORDER_TOP);
        cssPropertyNames.add(BORDER_TOP_COLOR);
        cssPropertyNames.add(BORDER_TOP_STYLE);
        cssPropertyNames.add(BORDER_TOP_WIDTH);
        
        cssPropertyNames.add(BORDER_BOTTOM);
        cssPropertyNames.add(BORDER_BOTTOM_COLOR);
        cssPropertyNames.add(BORDER_BOTTOM_STYLE);
        cssPropertyNames.add(BORDER_BOTTOM_WIDTH);
        
        cssPropertyNames.add(BORDER_LEFT);
        cssPropertyNames.add(BORDER_LEFT_COLOR);
        cssPropertyNames.add(BORDER_LEFT_STYLE);
        cssPropertyNames.add(BORDER_LEFT_WIDTH);
        
        cssPropertyNames.add(BORDER_TOP);
        cssPropertyNames.add(BORDER_TOP_COLOR);
        cssPropertyNames.add(BORDER_TOP_STYLE);
        cssPropertyNames.add(BORDER_TOP_WIDTH);
        
        cssPropertyNames.add(MARGIN);
        cssPropertyNames.add(MARGIN_TOP);
        cssPropertyNames.add(MARGIN_BOTTOM);
        cssPropertyNames.add(MARGIN_LEFT);
        cssPropertyNames.add(MARGIN_RIGHT);
        
        cssPropertyNames.add(PADDING);
        cssPropertyNames.add(PADDING_TOP);
        cssPropertyNames.add(PADDING_BOTTOM);
        cssPropertyNames.add(PADDING_LEFT);
        cssPropertyNames.add(PADDING_RIGHT);
        
        cssPropertyNames.add(POSITION);
        cssPropertyNames.add(TOP);
        cssPropertyNames.add(BOTTOM);
        cssPropertyNames.add(LEFT);
        cssPropertyNames.add(RIGHT);
        
        cssPropertyNames.add(WIDTH);
        cssPropertyNames.add(HEIGHT);
        cssPropertyNames.add(MAX_WIDTH);
        cssPropertyNames.add(MIN_WIDTH);
        cssPropertyNames.add(MAX_HEIGHT);
        cssPropertyNames.add(MIN_HEIGHT);
        
        cssPropertyNames.add(STYLE);
        cssPropertyNames.add(CLIP);
        cssPropertyNames.add(Z_INDEX);
        cssPropertyNames.add(COLOR);
        cssPropertyNames.add(DIRECTION);
        cssPropertyNames.add(LINE_HEIGHT);
        cssPropertyNames.add(VERTICAL_ALIGN);
        
        cssPropertyNames.add(FONT_FAMILY);
        cssPropertyNames.add(FONT_SIZE);
        cssPropertyNames.add(FONT_STYLE);
        cssPropertyNames.add(FONT_WEIGHT);
        cssPropertyNames.add(FONT_VARIANT);
        
        cssPropertyNames.add(TEXT_DECORATION);
        cssPropertyNames.add(TEXT_ALIGN);
        cssPropertyNames.add(TEXT_INDENT);
        
        cssPropertyNames.add(COLOR);
    }
    
    static List<String> cssLengthUnits = new ArrayList<String>();
    private static void setCssLengthUnitNames(){
        cssLengthUnits.add("px"); //NOI18N
        cssLengthUnits.add("pt"); //NOI18N
        cssLengthUnits.add("%"); //NOI18N
        cssLengthUnits.add("in"); //NOI18N
        cssLengthUnits.add("cm"); //NOI18N
        cssLengthUnits.add("mm"); //NOI18N
        cssLengthUnits.add("em"); //NOI18N
        cssLengthUnits.add("ex"); //NOI18N
        cssLengthUnits.add("pc"); //NOI18N
    }
    
    private static List<String> backgroundRepeatValues = new ArrayList<String>();
    private static void setBackgroundRepeatValues(){
        backgroundRepeatValues.add("repeat"); //NOI18N
        backgroundRepeatValues.add("repeat-x"); //NOI18N
        backgroundRepeatValues.add("repeat-y"); //NOI18N
        backgroundRepeatValues.add("inherit"); //NOI18N
    }
    
    private static List<String> backgroundPositionValues = new ArrayList<String>();
    private static void setBackgroundPositionValues(){
        backgroundPositionValues.add("center"); //NOI18N
        backgroundPositionValues.add("left"); //NOI18N
        backgroundPositionValues.add("right"); //NOI18N
        backgroundPositionValues.add("top"); //NOI18N
        backgroundPositionValues.add("bottom"); //NOI18N
        backgroundPositionValues.add("inherit"); //NOI18N
    }
    
    private static List<String> backgroundAttachmentValues = new ArrayList<String>();
    private static void setBackgroundAttachmentValues(){
        backgroundAttachmentValues.add("none"); //NOI18N
        backgroundAttachmentValues.add("scroll"); //NOI18N
        backgroundAttachmentValues.add("fixed"); //NOI18N
        backgroundAttachmentValues.add("inherit"); //NOI18N
    }
    
    private static List<String> fontStyleValues = new ArrayList<String>();
    private static void setFontStyleValues(){
        fontStyleValues.add("normal"); //NOI18N
        fontStyleValues.add("italic"); //NOI18N
        fontStyleValues.add("oblique"); //NOI18N
        fontStyleValues.add("inherit"); //NOI18N
    }
    
    private static List<String> fontVariantValues = new ArrayList<String>();
    private static void setFontVariantValues(){
        fontVariantValues.add("normal"); //NOI18N
        fontVariantValues.add("small-caps"); //NOI18N
        fontVariantValues.add("inherit"); //NOI18N
    }
    
    private static List<String> fontWeightValues = new ArrayList<String>();
    private static void setFontWeightValues(){
        fontWeightValues.add("normal"); //NOI18N
        fontWeightValues.add("bold"); //NOI18N
        fontWeightValues.add("bolder"); //NOI18N
        fontWeightValues.add("lighter"); //NOI18N
        fontWeightValues.add("100"); //NOI18N
        fontWeightValues.add("200"); //NOI18N
        fontWeightValues.add("300"); //NOI18N
        fontWeightValues.add("400"); //NOI18N
        fontWeightValues.add("500"); //NOI18N
        fontWeightValues.add("600"); //NOI18N
        fontWeightValues.add("700"); //NOI18N
        fontWeightValues.add("800"); //NOI18N
        fontWeightValues.add("900"); //NOI18N
        fontWeightValues.add("inherit"); //NOI18N
    }
    
    private static List<String> directionValues = new ArrayList<String>();
    private static void setDirectionValues(){
        directionValues.add("ltr"); //NOI18N
        directionValues.add("rtl"); //NOI18N
        directionValues.add("inherit"); //NOI18N
    }
    
    private static List<String> textAlignValues = new ArrayList<String>();
    private static void setTextAlignValues(){
        textAlignValues.add("center"); //NOI18N
        textAlignValues.add("left"); //NOI18N
        textAlignValues.add("right"); //NOI18N
        textAlignValues.add("justify"); //NOI18N
        textAlignValues.add("inherit"); //NOI18N
    }
    
    private static List<String> textDecorationValues = new ArrayList<String>();
    private static void setTextDecorationValues(){
        textDecorationValues.add("none"); //NOI18N
        textDecorationValues.add("underline"); //NOI18N
        textDecorationValues.add("overline"); //NOI18N
        textDecorationValues.add("line-through"); //NOI18N
        textDecorationValues.add("inherit"); //NOI18N
    }
    
    private static List<String> verticalAlignValues = new ArrayList<String>();
    private static void setVerticalAlignValues(){
        verticalAlignValues.add("baseline"); //NOI18N
        verticalAlignValues.add("sub"); //NOI18N
        verticalAlignValues.add("super"); //NOI18N
        verticalAlignValues.add("top"); //NOI18N
        verticalAlignValues.add("text-top"); //NOI18N
        verticalAlignValues.add("middle"); //NOI18N
        verticalAlignValues.add("bottom"); //NOI18N
        verticalAlignValues.add("text-bottom"); //NOI18N
        verticalAlignValues.add("inherit"); //NOI18N
    }
    
    private static List<String> positionValues = new ArrayList<String>();
    private static void setPositionValues(){
        positionValues.add("absolute"); //NOI18N
        positionValues.add("static"); //NOI18N
        positionValues.add("relative"); //NOI18N
        positionValues.add("fixed"); //NOI18N
        positionValues.add("inherit"); //NOI18N
    }
    
    private static List<String> visiblityValues = new ArrayList<String>();
    private static void setVisiblityValues(){
        visiblityValues.add("visible"); //NOI18N
        visiblityValues.add("hidden"); //NOI18N
        visiblityValues.add("collapse"); //NOI18N
        visiblityValues.add("inherit"); //NOI18N
    }
    
    private static List<String> borderStyleValues = new ArrayList<String>();
    private static void setBorderStyleValues(){
        borderStyleValues.add("none"); //NOI18N
        borderStyleValues.add("hidden"); //NOI18N
        borderStyleValues.add("dotted"); //NOI18N
        borderStyleValues.add("dashed"); //NOI18N
        borderStyleValues.add("solid"); //NOI18N
        borderStyleValues.add("double"); //NOI18N
        borderStyleValues.add("groove"); //NOI18N
        borderStyleValues.add("ridge"); //NOI18N
        borderStyleValues.add("inset"); //NOI18N
        borderStyleValues.add("outset"); //NOI18N
        borderStyleValues.add("inherit"); //NOI18N
    }
    
    private static List<String> colorValues = new ArrayList<String>();
    private static void setColorValues(){
        colorValues.add("aqua"); //NOI18N
        colorValues.add("black"); //NOI18N
        colorValues.add("blue"); //NOI18N
        colorValues.add("fuchsia"); //NOI18N
        colorValues.add("gray"); //NOI18N
        colorValues.add("green"); //NOI18N
        colorValues.add("lime"); //NOI18N
        colorValues.add("maroon"); //NOI18N
        colorValues.add("navy"); //NOI18N
        colorValues.add("olive"); //NOI18N
        colorValues.add("orange"); //NOI18N
        colorValues.add("purple"); //NOI18N
        colorValues.add("red"); //NOI18N
        colorValues.add("silver"); //NOI18N
        colorValues.add("teal"); //NOI18N
        colorValues.add("white"); //NOI18N
        colorValues.add("yellow"); //NOI18N
    }
    
    private static Map<String, String> colorNameHexMap = new HashMap<String, String>();
    private static void setColorNameHexMap(){
        colorNameHexMap.put("black","#000000"); //NOI18N
        colorNameHexMap.put("gray","#808080"); //NOI18N
        colorNameHexMap.put("white","#FFFFFF"); //NOI18N
        colorNameHexMap.put("maroon","#800000"); //NOI18N
        colorNameHexMap.put("red","#FF0000"); //NOI18N
        colorNameHexMap.put("purple","#800080"); //NOI18N
        colorNameHexMap.put("fuchsia","#FF00FF"); //NOI18N
        colorNameHexMap.put("green","#008000"); //NOI18N
        colorNameHexMap.put("lime","#00FF00"); //NOI18N
        colorNameHexMap.put("olive","#808000"); //NOI18N
        colorNameHexMap.put("orange","#FFA500"); //NOI18N
        colorNameHexMap.put("yellow","#FFFF00"); //NOI18N
        colorNameHexMap.put("navy","#000080"); //NOI18N
        colorNameHexMap.put("blue","#0000FF"); //NOI18N
        colorNameHexMap.put("silver","#C0C0C0"); //NOI18N
        colorNameHexMap.put("teal","#008080"); //NOI18N
        colorNameHexMap.put("aqua","#00FFFF"); //NOI18N
    }
    
    private static List<String> fontFamilyValues = new ArrayList<String>();
    private static void setFontFamilyValues(){
        fontFamilyValues.add("serif"); //NOI18N
        fontFamilyValues.add("sans-serif"); //NOI18N
        fontFamilyValues.add("monospace"); //NOI18N
        fontFamilyValues.add("fantasy"); //NOI18N
    }
    
    private static List<String> webFontValues = new ArrayList<String>();
    private static void setWebFontValues(){
        webFontValues.add("Arial Black"); //NOI18N
        webFontValues.add("Cosmic Sans"); //NOI18N
        webFontValues.add("Impact"); //NOI18N
        webFontValues.add("Veranda"); //NOI18N
        webFontValues.add("Webdings"); //NOI18N
        webFontValues.add("Trebuchet"); //NOI18N
        webFontValues.add("Georgia"); //NOI18N
        webFontValues.add("Minion Web"); //NOI18N
    }
    
    private static List<String> fontFamiliySetValues = new ArrayList<String>();
    private static void setFontFamiliySetValues(){
        // Do not keep spaces between commas. Batik parser automatically
        // removes the spaces.
        fontFamiliySetValues.add("Arial,Helvetica,sans-serif"); //NOI18N
        fontFamiliySetValues.add("\'Times New Roman\',Times,serif"); //NOI18N
        fontFamiliySetValues.add("\'Courier New\',Courier,monospace"); //NOI18N
        fontFamiliySetValues.add("Georgia,\'Times New Roman\',times,serif"); //NOI18N
        fontFamiliySetValues.add("Verdana,Arial,Helvetica,sans-serif"); //NOI18N
        fontFamiliySetValues.add("Geneva,Arial,Helvetica,sans-serif"); //NOI18N
        fontFamiliySetValues.add("serif"); //NOI18N
        fontFamiliySetValues.add("sans-serif"); //NOI18N
        fontFamiliySetValues.add("monospace"); //NOI18N
        fontFamiliySetValues.add("cursive"); //NOI18N
        fontFamiliySetValues.add("fantasy"); //NOI18N
    }
    
    private static List<String> fontSizeValues = new ArrayList<String>();
    private static void setFontSizeValues(){
        fontSizeValues.add("8"); //NOI18N
        fontSizeValues.add("10"); //NOI18N
        fontSizeValues.add("12"); //NOI18N
        fontSizeValues.add("14"); //NOI18N
        fontSizeValues.add("18"); //NOI18N
        fontSizeValues.add("24"); //NOI18N
        fontSizeValues.add("36"); //NOI18N
        fontSizeValues.add("XX-small"); //NOI18N
        fontSizeValues.add("X-small"); //NOI18N
        fontSizeValues.add("small"); //NOI18N
        fontSizeValues.add("medium"); //NOI18N
        fontSizeValues.add("large"); //NOI18N
        fontSizeValues.add("X-large"); //NOI18N
        fontSizeValues.add("XX-large"); //NOI18N
        fontSizeValues.add("smaller"); //NOI18N
        fontSizeValues.add("larger"); //NOI18N
    }
    
    static{
        setCssPropertyNames();
        setCssLengthUnitNames();
        setBackgroundRepeatValues();
        setBackgroundPositionValues();
        setBackgroundAttachmentValues();
        setFontStyleValues();
        setFontVariantValues();
        setFontWeightValues();
        setDirectionValues();
        setTextAlignValues();
        setTextDecorationValues();
        setVerticalAlignValues();
        setPositionValues();
        setVisiblityValues();
        setBorderStyleValues();
        setColorValues();
        setColorNameHexMap();
        setWebFontValues();
        setFontFamilyValues();
        setFontFamiliySetValues();
        setFontSizeValues();
    }
    
    /**
     * Get the names of the supported properties
     * @return Set of property names.
     */
    public static String[] getCssPropertyNames(){
        return cssPropertyNames.toArray(new String[cssPropertyNames.size()]);
    }
    
    public static String[] getCssPropertyValues(String cssProperty) {
        if(BACKGROUND_REPEAT.equals(cssProperty)){
            return backgroundRepeatValues.toArray(new String[backgroundRepeatValues.size()]);
        }else if(BACKGROUND_POSITION.equals(cssProperty)){
            return backgroundPositionValues.toArray(new String[backgroundPositionValues.size()]);
        }else if(BACKGROUND_ATTACHMENT.equals(cssProperty)){
            return backgroundAttachmentValues.toArray(new String[backgroundAttachmentValues.size()]);
        }else if(FONT_STYLE.equals(cssProperty)){
            return fontStyleValues.toArray(new String[fontStyleValues.size()]);
        }else if(FONT_VARIANT.equals(cssProperty)){
            return fontVariantValues.toArray(new String[fontVariantValues.size()]);
        }else if(FONT_WEIGHT.equals(cssProperty)){
            return fontWeightValues.toArray(new String[fontWeightValues.size()]);
        }else if(DIRECTION.equals(cssProperty)){
            return directionValues.toArray(new String[directionValues.size()]);
        }else if(TEXT_ALIGN.equals(cssProperty)){
            return textAlignValues.toArray(new String[textAlignValues.size()]);
        }else if(TEXT_DECORATION.equals(cssProperty)){
            return textDecorationValues.toArray(new String[textDecorationValues.size()]);
        }else if(VERTICAL_ALIGN.equals(cssProperty)){
            return verticalAlignValues.toArray(new String[verticalAlignValues.size()]);
        }else if(POSITION.equals(cssProperty)){
            return positionValues.toArray(new String[positionValues.size()]);
        }else if(VISIBILITY.equals(cssProperty)){
            return visiblityValues.toArray(new String[visiblityValues.size()]);
        }else if(BORDER_STYLE.equals(cssProperty)){
            return borderStyleValues.toArray(new String[borderStyleValues.size()]);
        }
        return new String[0];
    }
    
    public static String[] getCssLengthUnits() {
        return cssLengthUnits.toArray(new String[cssLengthUnits.size()]);
    }
    
    public static String[] getColorValues() {
        return colorValues.toArray(new String[colorValues.size()]);
    }
    
    public static Map getColorNameHexMap() {
        return Collections.unmodifiableMap(colorNameHexMap);
    }
    
    public static String[] getFontFamilyValues() {
        return fontFamilyValues.toArray(new String[fontFamilyValues.size()]);
    }
    
    public static String[] getWebFontValues() {
        return  webFontValues.toArray(new String[webFontValues.size()]);
    }
    
    public static String[] getFontFamilySetValues() {
        return fontFamiliySetValues.toArray(new String[fontFamiliySetValues.size()]);
    }
    
    public static String[] getFontSizeValues() {
        return fontSizeValues.toArray(new String[fontSizeValues.size()]);
    }
}
