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

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupDesignContext;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.JTextArea;
import org.openide.util.NbBundle;

/**
 * The Style Properties Data
 * @author  Winston Prakash
 */
public class CssStyleData {
    
    public final static String PREVIEW_NOT_SUPPORTED = NbBundle.getMessage(CssStyleData.class, "PREVIEW_NOT_SUPPORTED_MSG");
    public final static String NOT_SET = NbBundle.getMessage(CssStyleData.class, "NOT_SET");
    public final static String VALUE = NbBundle.getMessage(CssStyleData.class, "VALUE");
    
    public final static String FONT_FAMILY = "font-family"; //NOI18N
    public final static String FONT_SIZE = "font-size"; //NOI18N
    public final static String FONT_STYLE = "font-style"; //NOI18N
    public final static String FONT_WEIGHT = "font-weight"; //NOI18N
    public final static String FONT_VARIANT = "font-variant"; //NOI18N
    
    public final static String TEXT_DECORATION = "text-decoration"; //NOI18N
    public final static String TEXT_ALIGN = "text-align"; //NOI18N
    public final static String TEXT_INDENT = "text-indent"; //NOI18N
    
    public final static String COLOR = "color"; //NOI18N
    
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
    
    Properties styleProperties = new Properties();
    
    Set propertyNames = new  HashSet();
    
    Map fontFaceFamilyMap = new HashMap();
    Set fontNames = new HashSet();
    
    Font defaultFont = new JTextArea().getFont();
    String  defaultFontName = defaultFont.getFontName();
    int  defaultFontSize = defaultFont.getSize();
    int  defaultFontStyle = defaultFont.getStyle();
    
    DesignProperty designProperty;
    
    /**
     * Holds value of property test.
     */
    private String test;
    
    /**
     * Utility field used by bound properties.
     */
    private PropertyChangeSupport propertyChangeSupport =  new PropertyChangeSupport(this);
    
    public CssStyleData(){
        propertyNames.add(FONT_FAMILY);
    }
    
    public void setDesignProperty(DesignProperty designProperty){
        this.designProperty = designProperty;
    }
    
    /**
     * Get the names of the supported properties
     * @return Set of property names.
     */
    public Set getPropertyNames(){
        return propertyNames;
    }
    
    /**
     * Get the value of specified property from the set.
     * @return Value of the specified property.
     */
    public String getProperty(String property) {
        return (String)styleProperties.get(property);
    }
    
    /**
     * Add the specified property to the property set.
     * @param property name & value of the property.
     */
    public void addProperty(String property, String value) {
        if(value != null){
            if (styleProperties.containsKey(property)) {
                styleProperties.remove(property);
            }
            styleProperties.put(property, value.trim());
        }
    }
    
    /**
     * Modify the specified property in the property set.
     * @param property name & value of the property.
     */
    public void modifyProperty(String property, String newValue) {
        Object oldValue = null;
        newValue = newValue.trim();
        if (styleProperties.containsKey(property)) {
            oldValue = styleProperties.remove(property);
        }
        if((newValue != null) && (!newValue.equals("")) && (!newValue.equals(NOT_SET)) && (!newValue.equals(VALUE))){
            styleProperties.put(property, newValue);
        }
        propertyChangeSupport.firePropertyChange("property", oldValue, newValue);
    }
    
    public String getStyleValue(){
        return toString();
    }
    
    /**
     * Reove the specified property from the property set.
     * @param property name & value of the property.
     */
    public void removeProperty(String property) {
        if (styleProperties.containsKey(property)) {
            String oldValue = getProperty(property);
            styleProperties.remove(property);
            propertyChangeSupport.firePropertyChange("property", oldValue, null); //NOI18N
        }
    }
    
    public String toString(){
        // If the design property is not null, then construct the Style
        // String from the style properties using  DesignContext.convertMapToCssStyle
   
        if(designProperty != null){
            MarkupDesignBean liveBean = (MarkupDesignBean)designProperty.getDesignBean();
            MarkupDesignContext liveContext = (MarkupDesignContext) liveBean.getDesignContext();
            String styleString = liveContext.convertMapToCssStyle(styleProperties);
            return styleString;
        }else{
            StringWriter strWriter = new StringWriter();
            for(Iterator iter = styleProperties.keySet().iterator(); iter.hasNext();){
                String property = (String)iter.next();
                strWriter.write(property);
                strWriter.write(":");
                strWriter.write(getProperty(property));
                if(iter.hasNext()) strWriter.write("; ");
            }
            return strWriter.toString().replaceAll("\"","&quot;"); //NOI18N
        }
    }
    
    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param listener The listener to add.
     */
    public void addCssPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param listener The listener to remove.
     */
    public void removeCssPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
