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
package org.netbeans.modules.visualweb.propertyeditors.css.model;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

/**
 * Data Structure to hold the font family data
 * @author  Winston Prakash
 */
public class FontModel{
    
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
            fontSizeStr = fontSizeStr.trim();
            for(int i=0; i< unitList.getSize(); i++){
                String unit = (String)unitList.getElementAt(i);
                if(fontSizeStr.endsWith(unit)){
                    fontSizeUnit = unit;
                    fontSize = fontSizeStr.replaceAll(unit,"");
                }
            }
        }
        
        public String getUnit(){
            return fontSizeUnit;
        }
        
        public String getValue(){
            return fontSize;
        }
    }
    
    public class FontSelectionList extends DefaultComboBoxModel{
        public FontSelectionList(){
            addElement(org.openide.util.NbBundle.getMessage(FontModel.class, "FONTS"));
            addElement(org.openide.util.NbBundle.getMessage(FontModel.class, "FONT_FAMILIES"));
            addElement(org.openide.util.NbBundle.getMessage(FontModel.class, "WEB_FONTS"));
        }
    }
    
    public class FontList extends DefaultListModel{
        public FontList(){
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String fontNames[] = ge.getAvailableFontFamilyNames();
            
            // Iterate the font names and add to the model
            for (int i=0; i<fontNames.length; i++) {
                addElement(fontNames[i]);
            }
        }
    }
    
    public class WebFontList extends DefaultListModel{
        public WebFontList(){
            addElement("Arial Black"); //NOI18N
            addElement("Cosmic Sans"); //NOI18N
            addElement("Impact"); //NOI18N
            addElement("Veranda"); //NOI18N
            addElement("Webdings"); //NOI18N
            addElement("Trebuchet"); //NOI18N
            addElement("Georgia"); //NOI18N
            addElement("Minion Web"); //NOI18N
        }
    }
    
    public class FontFamilyList extends DefaultListModel{
        public FontFamilyList(){
            addElement("serif"); //NOI18N
            addElement("sans-serif"); //NOI18N
            addElement("monospace"); //NOI18N
            addElement("cursive"); //NOI18N
            addElement("fantasy"); //NOI18N
        }
    }
    
    public class FontFamilySetList extends DefaultListModel{
        public FontFamilySetList(){
            addElement(CssStyleData.NOT_SET);
            // Do not keep spaces between commas. Batik parser automatically 
            // removes the spaces.
            addElement("Arial,Helvetica,sans-serif"); //NOI18N
            addElement("\'Times New Roman\',Times,serif"); //NOI18N
            addElement("\'Courier New\',Courier,monospace"); //NOI18N
            addElement("Georgia,\'Times New Roman\',times,serif"); //NOI18N
            addElement("Verdana,Arial,Helvetica,sans-serif"); //NOI18N
            addElement("Geneva,Arial,Helvetica,sans-serif"); //NOI18N
            addElement("serif"); //NOI18N
            addElement("sans-serif"); //NOI18N
            addElement("monospace"); //NOI18N
            addElement("cursive"); //NOI18N
            addElement("fantasy"); //NOI18N
        }
    }
    
    public class FontSizeList extends DefaultListModel{
        public FontSizeList(){
            addElement(CssStyleData.NOT_SET);
            addElement("8"); //NOI18N
            addElement("10"); //NOI18N
            addElement("12"); //NOI18N
            addElement("14"); //NOI18N
            addElement("18"); //NOI18N
            addElement("24"); //NOI18N
            addElement("36"); //NOI18N
            addElement("XX-small"); //NOI18N
            addElement("X-small"); //NOI18N
            addElement("small"); //NOI18N
            addElement("medium"); //NOI18N
            addElement("large"); //NOI18N
            addElement("X-large"); //NOI18N
            addElement("XX-large"); //NOI18N
            addElement("smaller"); //NOI18N
            addElement("larger"); //NOI18N
        }
    }
    
    public class FontSizeUnitList extends DefaultComboBoxModel{
        public FontSizeUnitList(){
            addElement("px"); //NOI18N
            addElement("pt"); //NOI18N
            addElement("%"); //NOI18N
            addElement("in"); //NOI18N
            addElement("cm"); //NOI18N
            addElement("mm"); //NOI18N
            addElement("em"); //NOI18N
            addElement("ex"); //NOI18N
            addElement("pc"); //NOI18N
        }
    }
    
    public class FontStyleList extends DefaultComboBoxModel{
        public FontStyleList(){
            addElement(CssStyleData.NOT_SET);
            addElement("normal"); //NOI18N
            addElement("italic"); //NOI18N
            addElement("oblique"); //NOI18N
        }
    }
    
    public class FontWeightList extends DefaultComboBoxModel{
        public FontWeightList(){
            addElement(CssStyleData.NOT_SET);
            addElement("normal"); //NOI18N
            addElement("bold"); //NOI18N
            addElement("bolder"); //NOI18N
            addElement("lighter"); //NOI18N
        }
    }
    
    public class FontVariantList extends DefaultComboBoxModel{
        public FontVariantList(){
            addElement(CssStyleData.NOT_SET);
            addElement("small-caps"); //NOI18N
        }
    }
}
