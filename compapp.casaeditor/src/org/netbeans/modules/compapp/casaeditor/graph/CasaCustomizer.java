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
 * CasaCustomizer.java
 *
 * Created on January 26, 2007, 2:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.prefs.Preferences;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;

/**
 *
 * @author rdara
 */
public class CasaCustomizer {
    private static final String S_COLOR_REGION_BINDING = "COLOR_REGION_BINDING";
    private static final String S_COLOR_REGION_ENGINE = "COLOR_REGION_ENGINE";
    private static final String S_COLOR_REGION_EXTERNAL = "COLOR_REGION_EXTERNAL";

    private static final String S_COLOR_SU_INTERNAL_BORDER = "COLOR_SU_INTERNAL_BORDER";
    private static final String S_COLOR_SU_INTERNAL_TITLE = "COLOR_SU_INTERNAL_TITLE";
    private static final String S_COLOR_SU_INTERNAL_BACKGROUND = "COLOR_SU_INTERNAL_BACKGROUND";

    private static final String S_COLOR_SU_EXTERNAL_BORDER = "COLOR_SU_EXTERNAL_BORDER";
    private static final String S_COLOR_SU_EXTERNAL_TITLE = "COLOR_SU_EXTERNAL_TITLE";
    private static final String S_COLOR_SU_EXTERNAL_BACKGROUND = "COLOR_SU_EXTERNAL_BACKGROUND";

    private static final String S_COLOR_BC_TITLE = "COLOR_BC_TITLE";
    private static final String S_COLOR_BC_BACKGROUND = "COLOR_BC_BACKGROUND";
    private static final String S_COLOR_BC_BORDER = "COLOR_BC_BORDER";
    private static final String S_COLOR_BC_TITLE_BACKGROUND = "COLOR_BC_TITLE_BACKGROUND";
    
    private static final String S_COLOR_CONNECTION_NORMAL = "COLOR_CONNECTION_NORMAL";
    private static final String S_COLOR_SELECTED_BORDER = "COLOR_SELECTED_BORDER";

    private static final String S_COLOR_CONNECTION_SELECTED = "COLOR_CONNECTION_SELECTED";
    private static final String S_COLOR_CONNECTION_HOVERED = "COLOR_CONNECTION_HOVERED";

    
    private static final String S_COLOR_BC_REGION_TITLE = "COLOR_BC_REGION_TITLE";
    private static final String S_COLOR_SU_REGION_TITLE = "COLOR_SU_REGION_TITLE";
    private static final String S_COLOR_EXT_SU_REGION_TITLE = "COLOR_EXT_SU_REGION_TITLE";

    private static final String S_FONT_BC_REGION_TITLE = "FONT_BC_REGION_TITLE";
    private static final String S_FONT_SU_REGION_TITLE = "FONT_SU_REGION_TITLE";
    private static final String S_FONT_EXT_SU_REGION_TITLE = "FONT_EXT_SU_REGION_TITLE";

    private static final String S_FONT_BC_TITLE = "FONT_BC_TITLE";
    private static final String S_FONT_SU_TITLE = "FONT_SU_TITLE";
    private static final String S_FONT_EXT_SU_TITLE = "FONT_EXT_SU_TITLE";

    private static final String S_GEN_AUTO_SAVE = "GEN_AUTO_SAVE";
    private static final String S_GEN_RESTORE_DEFAULTS = "GEN_RESTORE_DEFAULTS";

    
    public static final String PROPERTY_CHANGE = "Customizer_Property_Changed";

    //private Font FONT_BC_HEADER = new Font("Helvetica", Font.BOLD, 14);    
    
    private LinkedHashMap<String, Color> mColorsMap = new LinkedHashMap<String, Color>();
    private LinkedHashMap<String, Font> mFontsMap   = new LinkedHashMap<String, Font>();
    private LinkedHashMap<String, GradientRectangleColorScheme> mGradientsMap = new LinkedHashMap<String, GradientRectangleColorScheme>();
    private LinkedHashMap<String, Boolean> mGeneralsMap   = new LinkedHashMap<String, Boolean>();
    
    public LinkedHashMap<String, Color> getColorsMapReference() {
        return mColorsMap;
    }
    public LinkedHashMap<String, Font> getFontsMapReference() {
        return mFontsMap;
    }
    
    public LinkedHashMap<String, GradientRectangleColorScheme> getGradientsMapReference() {
        return mGradientsMap;
    }
    
    public LinkedHashMap<String, Boolean> getGeneralsMapReference() {
        return mGeneralsMap;
    }
           
    /** Creates a new instance of CasaCustomizer */
    public CasaCustomizer() {
        loadFromPreferences(getPreferences(), false);
        if(isRestoreDefaults()) {
            restoreDefaults(false);
        }
    }
    
    private Preferences getPreferences() {
        String ourNodeName = "/org/netbeans/modules/compapp/casaeditor/graph/CasaCustomizer";
        Preferences prefs = Preferences.userRoot().node(ourNodeName);
        //Preferences prefs = Preferences.systemRoot().node(ourNodeName);
        return prefs;
    }

    public Color getCOLOR_REGION_BINDING() {
        return mColorsMap.get(S_COLOR_REGION_BINDING);
    }

    public void setCOLOR_REGION_BINDING(Color COLOR_REGION_BINDING) {
        mColorsMap.put(S_COLOR_REGION_BINDING, COLOR_REGION_BINDING);
    }

    public Color getCOLOR_REGION_ENGINE() {
        return mColorsMap.get(S_COLOR_REGION_ENGINE);
    }

    public void setCOLOR_REGION_ENGINE(Color COLOR_REGION_ENGINE) {
        mColorsMap.put(S_COLOR_REGION_ENGINE, COLOR_REGION_ENGINE);
    }

    public Color getCOLOR_REGION_EXTERNAL() {
        return mColorsMap.get(S_COLOR_REGION_EXTERNAL);
    }

    public void setCOLOR_REGION_EXTERNAL(Color COLOR_REGION_EXTERNAL) {
        mColorsMap.put(S_COLOR_REGION_EXTERNAL, COLOR_REGION_EXTERNAL);
    }

    public Color getCOLOR_SU_INTERNAL_BORDER() {
        return mColorsMap.get(S_COLOR_SU_INTERNAL_BORDER);
    }

    public void setCOLOR_SU_INTERNAL_BORDER(Color COLOR_SU_INTERNAL_BORDER) {
        mColorsMap.put(S_COLOR_SU_INTERNAL_BORDER, COLOR_SU_INTERNAL_BORDER);
    }

    public Color getCOLOR_SU_INTERNAL_TITLE() {
        return mColorsMap.get(S_COLOR_SU_INTERNAL_TITLE);
    }

    public void setCOLOR_SU_INTERNAL_TITLE(Color COLOR_SU_INTERNAL_TITLE) {
        mColorsMap.put(S_COLOR_SU_INTERNAL_TITLE, COLOR_SU_INTERNAL_TITLE);
    }

    public Color getCOLOR_SU_INTERNAL_BACKGROUND() {
        return mColorsMap.get(S_COLOR_SU_INTERNAL_BACKGROUND);
    }

    public void setCOLOR_SU_INTERNAL_BACKGROUND(Color COLOR_SU_INTERNAL_BACKGROUND) {
        mColorsMap.put(S_COLOR_SU_INTERNAL_BACKGROUND, COLOR_SU_INTERNAL_BACKGROUND);
    }

    public Color getCOLOR_SU_EXTERNAL_BORDER() {
        return mColorsMap.get(S_COLOR_SU_EXTERNAL_BORDER);
    }

    public void setCOLOR_SU_EXTERNAL_BORDER(Color COLOR_SU_EXTERNAL_BORDER) {
        mColorsMap.put(S_COLOR_SU_EXTERNAL_BORDER, COLOR_SU_EXTERNAL_BORDER);
    }

    public Color getCOLOR_SU_EXTERNAL_TITLE() {
        return mColorsMap.get(S_COLOR_SU_EXTERNAL_TITLE);
    }

    public void setCOLOR_SU_EXTERNAL_TITLE(Color COLOR_SU_EXTERNAL_TITLE) {
        mColorsMap.put(S_COLOR_SU_EXTERNAL_TITLE, COLOR_SU_EXTERNAL_TITLE);
    }

    public Color getCOLOR_SU_EXTERNAL_BACKGROUND() {
        return mColorsMap.get(S_COLOR_SU_EXTERNAL_BACKGROUND);
    }

    public void setCOLOR_SU_EXTERNAL_BACKGROUND(Color COLOR_SU_EXTERNAL_BACKGROUND) {
        mColorsMap.put(S_COLOR_SU_EXTERNAL_BACKGROUND, COLOR_SU_EXTERNAL_BACKGROUND);
    }

    public Color getCOLOR_BC_TITLE() {
        return mColorsMap.get(S_COLOR_BC_TITLE);
    }

    public void setCOLOR_BC_TITLE(Color COLOR_BC_TITLE) {
        mColorsMap.put(S_COLOR_BC_BACKGROUND, COLOR_BC_TITLE);
    }
    
    
    public Color getCOLOR_BC_BACKGROUND() {
        return mColorsMap.get(S_COLOR_BC_BACKGROUND);
    }

    public void setCOLOR_BC_BACKGROUND(Color COLOR_BC_BACKGROUND) {
        mColorsMap.put(S_COLOR_BC_BACKGROUND, COLOR_BC_BACKGROUND);
    }

    public Color getCOLOR_BC_TITLE_BACKGROUND() {
        return mColorsMap.get(S_COLOR_BC_TITLE_BACKGROUND);
    }

    public void setCOLOR_BC_TITLE_BACKGROUND(Color COLOR_BC_TITLE_BACKGROUND) {
        mColorsMap.put(S_COLOR_BC_TITLE_BACKGROUND, COLOR_BC_TITLE_BACKGROUND);
    }
    
    
    public Color getCOLOR_BC_BORDER() {
        return mColorsMap.get(S_COLOR_BC_BORDER);
    }

    public void setCOLOR_BC_BORDER(Color COLOR_BC_BORDER) {
        mColorsMap.put(S_COLOR_BC_BORDER, COLOR_BC_BORDER);
    }

    public Color getCOLOR_SELECTED_CONNECTION() {
        return mColorsMap.get(S_COLOR_CONNECTION_SELECTED);
    }

    public void setCOLOR_SELECTED_CONNECTION(Color COLOR_SELECTED_EDGE) {
        mColorsMap.put(S_COLOR_CONNECTION_SELECTED, COLOR_SELECTED_EDGE);
    }

    public Color getCOLOR_HOVERED_EDGE() {
        return mColorsMap.get(S_COLOR_CONNECTION_HOVERED);
    }

    public void setCOLOR_HOVERED_EDGE(Color COLOR_HOVERED_EDGE) {
        mColorsMap.put(S_COLOR_CONNECTION_HOVERED, COLOR_HOVERED_EDGE);
    }

    public Color getCOLOR_SELECTED_BORDER() {
        return mColorsMap.get(S_COLOR_SELECTED_BORDER);
    }

    public void setCOLOR_SELECTED_BORDER(Color COLOR_SELECTED_BORDER) {
        mColorsMap.put(S_COLOR_SELECTED_BORDER, COLOR_SELECTED_BORDER);
    }


    public Color getCOLOR_CONNECTION_NORMAL() {
        return mColorsMap.get(S_COLOR_CONNECTION_NORMAL);
    }

    public void setCOLOR_CONNECTION_NORMAL(Color CASA_CONNECTION_COLOR_NORMAL) {
        mColorsMap.put(S_COLOR_CONNECTION_NORMAL, CASA_CONNECTION_COLOR_NORMAL);
    }

    //Region title colors
    public Color getCOLOR_BC_REGION_TITLE() {
        return mColorsMap.get(S_COLOR_BC_REGION_TITLE);
    }
    public void setCOLOR_BC_REGION_TITLE(Color color) {
        mColorsMap.put(S_COLOR_BC_REGION_TITLE, color);
    }
    public Color getCOLOR_SU_REGION_TITLE() {
        return mColorsMap.get(S_COLOR_SU_REGION_TITLE);
    }
    public void setCOLOR_SU_REGION_TITLE(Color color) {
        mColorsMap.put(S_COLOR_SU_REGION_TITLE, color);
    }
     public Color getCOLOR_EXT_SU_REGION_TITLE() {
        return mColorsMap.get(S_COLOR_EXT_SU_REGION_TITLE);
    }
    public void setCOLOR_EXT_SU_REGION_TITLE(Color color) {
        mColorsMap.put(S_COLOR_EXT_SU_REGION_TITLE, color);
    }


    public Font getFONT_BC_REGION_TITLE() {
        return mFontsMap.get(S_FONT_BC_REGION_TITLE);
    }
    public void setFONT_BC_REGION_TITLE(Font font) {
        mFontsMap.put(S_FONT_BC_REGION_TITLE, font);
    }
    public Font getFONT_SU_REGION_TITLE() {
        return mFontsMap.get(S_FONT_SU_REGION_TITLE);
    }
    public void setFONT_SU_REGION_TITLE(Font font) {
        mFontsMap.put(S_FONT_SU_REGION_TITLE, font);
    }
    public Font getFONT_EXT_SU_REGION_TITLE() {
        return mFontsMap.get(S_FONT_EXT_SU_REGION_TITLE);
    }
    public void setFONT_EXT_SU_REGION_TITLE(Font font) {
        mFontsMap.put(S_FONT_EXT_SU_REGION_TITLE, font);
    }
    

    public Font getFONT_BC_HEADER() {
        return mFontsMap.get(S_FONT_BC_TITLE);
    }
    public void setFONT_BC_HEADER(Font font) {
        mFontsMap.put(S_FONT_BC_TITLE, font);
    }
    public Font getFONT_SU_HEADER() {
        return mFontsMap.get(S_FONT_SU_TITLE);
    }
    public void setFONT_SU_HEADER(Font font) {
        mFontsMap.put(S_FONT_SU_TITLE, font);
    }
    public Font getFONT_EXT_SU_HEADER() {
        return mFontsMap.get(S_FONT_EXT_SU_TITLE);
    }
    public void setFONT_EXT_SU_HEADER(Font font) {
        mFontsMap.put(S_FONT_EXT_SU_TITLE, font);
    }
    
    public GradientRectangleColorScheme getGradientINT_SU_BACKGROUND() {
        return mGradientsMap.get(S_COLOR_SU_INTERNAL_BACKGROUND);
    }
    public GradientRectangleColorScheme getGradientEXT_SU_BACKGROUND() {
        return mGradientsMap.get(S_COLOR_SU_EXTERNAL_BACKGROUND);
    }
    public GradientRectangleColorScheme getGradientBC_TITLE_BACKGROUND() {
        return mGradientsMap.get(S_COLOR_BC_TITLE_BACKGROUND);
    }
    
    public void renderCasaDesignView(CasaModelGraphScene scene) {
        changeBindingRegion(scene);
        changeEngineRegion(scene);
        changeExternalRegion(scene);
        changeConnectionLayer(scene);
        scene.validate();
        if(isAutoSave()) {
            savePreferences();
        }
    }

    private void changeBindingRegion(CasaModelGraphScene scene) {
        CasaRegionWidget regionWidget = (CasaRegionWidget) scene.getBindingRegion();
        //LayerWidget regionWidget = getScene().getBindingRegion();
        regionWidget.setBackground(getCOLOR_REGION_BINDING());
        regionWidget.setCOLOR_REGION_TITLE(getCOLOR_BC_REGION_TITLE());
        regionWidget.setFONT_REGION_TITLE(getFONT_BC_REGION_TITLE());
        
        for(Widget widget : regionWidget.getChildren()) {
            if(widget instanceof CasaNodeWidgetBinding) {
                change((CasaNodeWidgetBinding) widget);
            }
        }
    }
    
    private void changeEngineRegion(CasaModelGraphScene scene) {
        CasaRegionWidget regionWidget = (CasaRegionWidget) scene.getEngineRegion();
        //LayerWidget regionWidget = getScene().getEngineRegion();
        regionWidget.setBackground(getCOLOR_REGION_ENGINE());
        regionWidget.setCOLOR_REGION_TITLE(getCOLOR_SU_REGION_TITLE());
        regionWidget.setFONT_REGION_TITLE(getFONT_SU_REGION_TITLE());

        for(Widget widget : regionWidget.getChildren()) {
            if(widget instanceof CasaNodeWidgetEngineInternal) {
                change((CasaNodeWidgetEngineInternal) widget);
            }
        }
    }
    
    private void changeExternalRegion(CasaModelGraphScene scene) {
        CasaRegionWidget regionWidget = (CasaRegionWidget) scene.getExternalRegion();
        //LayerWidget regionWidget = getScene().getExternalRegion();
        regionWidget.setBackground(getCOLOR_REGION_EXTERNAL());
        regionWidget.setCOLOR_REGION_TITLE(getCOLOR_EXT_SU_REGION_TITLE());
        regionWidget.setFONT_REGION_TITLE(getFONT_EXT_SU_REGION_TITLE());
        
        for(Widget widget : regionWidget.getChildren()) {
            if(widget instanceof CasaNodeWidgetEngineExternal) {
                change((CasaNodeWidgetEngineExternal) widget);
            }
        }
    }
    
    private void changeConnectionLayer(CasaModelGraphScene scene) {
        LayerWidget regionWidget = scene.getConnectionLayer();
        for(Widget widget : regionWidget.getChildren()) {
            if(widget instanceof CasaConnectionWidget) {
                change((CasaConnectionWidget) widget);
            }
        }
    }

    private void change(CasaNodeWidgetEngineInternal internalSUWidget) {
        internalSUWidget.setTitleColor(getCOLOR_SU_INTERNAL_TITLE());
        internalSUWidget.setTitleFont(getFONT_SU_HEADER());
        internalSUWidget.paintWidget();
    }
    private void change(CasaNodeWidgetEngineExternal externalSUWidget){
        externalSUWidget.setTitleColor(getCOLOR_SU_EXTERNAL_TITLE());
        externalSUWidget.setTitleFont(this.getFONT_EXT_SU_HEADER());
        externalSUWidget.paintWidget();
    }
    private void change(CasaNodeWidgetBinding bindingWidget) {
        bindingWidget.setBackgroundColor(getCOLOR_BC_BACKGROUND());
        //bindingWidget.setTitleBackgroundColor(getCOLOR_BC_TITLE_BACKGROUND());
        bindingWidget.regenerateHeaderBorder();
        bindingWidget.regenerateVerticalTextBarImage(); //For FONT_BC_HEADER
        bindingWidget.setTitleBackgroundColor(getCOLOR_BC_TITLE_BACKGROUND());
    }
    private void change(CasaConnectionWidget connectionWidget) {
        connectionWidget.setForegroundColor(getCOLOR_CONNECTION_NORMAL());
    }

    public Font getFont(String str) {
        //Font name, style, size
        if (str == null || str.trim().equals("")) {
            Scene scene = new Scene();
            return scene.getDefaultFont().deriveFont(Font.BOLD);
        }
        String[] strNumbers = str.split(",");
        int appearance = Integer.parseInt(strNumbers[1].trim());
        int size = Integer.parseInt(strNumbers[2].trim());
        return new Font(strNumbers[0].trim(), appearance, size);
    }
    
    public GradientRectangleColorScheme getGradient(String str) {
        String[] strNumbers = str.split(",");
        return new GradientRectangleColorScheme(null, 
                    new Color(Integer.parseInt(strNumbers[0])),
                    new Color(Integer.parseInt(strNumbers[1])),
                    new Color(Integer.parseInt(strNumbers[2])),
                    new Color(Integer.parseInt(strNumbers[3])),
                    new Color(Integer.parseInt(strNumbers[4])));
    }

    public Boolean getBoolean(String str) {
        Boolean bValue;
        if(str.equalsIgnoreCase("true")) {
            bValue = new Boolean(true);
        } else {
            bValue = new Boolean(false);
        }
        return bValue;
    }
    
    public Object getValue(String key) {
        Object retValue = null;
        if(getColorsMapReference().containsKey(key)) {
           retValue =  (Object) getColorsMapReference().get(key);
        } else if(getFontsMapReference().containsKey(key)) {
           retValue =  (Object) getFontsMapReference().get(key);
        } else if(getGeneralsMapReference().containsKey(key)) {
           retValue = (Object)  getGeneralsMapReference().get(key);
        }
        return retValue;
    }


    public void setValue(String key, Color color) {
        getColorsMapReference().put(key, color);
        if(mGradientsMap.containsKey(key)) {
            GradientRectangleColorScheme gradientColor = new GradientRectangleColorScheme(null, color);
            mGradientsMap.put(key, gradientColor);
        }
        CasaFactory.getCasaCustomizerRegistor().propagateChange();
    }
    
    public void setValue(String key, Font font) {
        getFontsMapReference().put(key, font);
        CasaFactory.getCasaCustomizerRegistor().propagateChange();
    }
    
    public void setValue(String key, GradientRectangleColorScheme gradient) {
        getGradientsMapReference().put(key, gradient);
        CasaFactory.getCasaCustomizerRegistor().propagateChange();
    }

    public void setValue(String key, Boolean bValue) {
        getGeneralsMapReference().put(key, bValue);
        if(key.equals(S_GEN_AUTO_SAVE)) {
            if(bValue.booleanValue()) {
                savePreferences();
            }
        }  else if(key.equals(this.S_GEN_RESTORE_DEFAULTS)) {
            if(bValue.booleanValue()) {
                restoreDefaults(true);
            }
        }
    }

    public void restoreDefaults(boolean bPropagateChange) {
        LinkedHashMap<String, String> defaultColors = getDefaultColors();
        int i;
        for(String key : defaultColors.keySet()){
            i = Integer.parseInt(defaultColors.get(key));
            mColorsMap.put(key, new Color(i));
        }
        LinkedHashMap<String, String> defaultFonts = getDefaultFonts();
        Font font;
        for(String key : defaultFonts.keySet()){
            font = getFont(defaultFonts.get(key));
            mFontsMap.put(key, font);
        }
        
        LinkedHashMap<String, String> dafaultGradients = getDefaultGradients();
        GradientRectangleColorScheme gradientColor;
        for(String key : dafaultGradients.keySet()){
            gradientColor = getGradient(dafaultGradients.get(key));
            mGradientsMap.put(key, gradientColor);
        }

        LinkedHashMap<String, String> dafaultGenerals = getDefaultGenerals();
        Boolean booleanValue;
        for(String key : dafaultGenerals.keySet()){
           booleanValue = getBoolean(dafaultGenerals.get(key));
            mGeneralsMap.put(key, booleanValue);
        }

         if(bPropagateChange) {
            CasaFactory.getCasaCustomizerRegistor().propagateChange();
        }
    }
    
    public void loadFromPreferences(Preferences prefs, boolean bRenderDesignView) {
        LinkedHashMap<String, String> defaultColors = getDefaultColors();
        String value;
        int i;
        for(String key : defaultColors.keySet()){
            value = prefs.get(key, defaultColors.get(key));
            try {
                i = Integer.parseInt(value);
            } catch(Exception e) {
                i = Integer.parseInt(defaultColors.get(key));
            }
            mColorsMap.put(key, new Color(i));
        }
        
        LinkedHashMap<String, String> defaultFonts = getDefaultFonts();
        Font font;
        for(String key : defaultFonts.keySet()){
            value = prefs.get(key, defaultFonts.get(key));
            try {
                font = getFont(value);
            } catch(Exception e) {
                font = getFont(defaultFonts.get(key));
            }
            mFontsMap.put(key, font);
        }
        
        LinkedHashMap<String, String> dafaultGradients = getDefaultGradients();
        GradientRectangleColorScheme gradientColor;
        for(String key : dafaultGradients.keySet()){
            value = prefs.get(key, dafaultGradients.get(key));
            try {
                gradientColor = getGradient(value);
            } catch(Exception e) {
                gradientColor = getGradient(dafaultGradients.get(key));
            }
            mGradientsMap.put(key, gradientColor);
        }
        
        LinkedHashMap<String, String> dafaultGenerals = getDefaultGenerals();
        Boolean booleanValue;
        for(String key : dafaultGenerals.keySet()){
            value = prefs.get(key, dafaultGenerals.get(key));
            try {
                booleanValue = getBoolean(value);
            } catch(Exception e) {
                booleanValue = getBoolean(dafaultGenerals.get(key));
            }
            mGeneralsMap.put(key, booleanValue);
        }
        
        if(bRenderDesignView) {
            CasaFactory.getCasaCustomizerRegistor().propagateChange();
        }
    }
    
    public void savePreferences() {
        String ourNodeName = "/org/netbeans/modules/compapp/casaeditor/graph/CasaCustomizer";
        Preferences prefs = Preferences.userRoot().node(ourNodeName);
        
        for(String key : mColorsMap.keySet()){
            prefs.put(key, Integer.toString(mColorsMap.get(key).getRGB()));
        }
        Font font;
        String value;
        for(String key : mFontsMap.keySet()){
            font = mFontsMap.get(key);
            value = font.getName() + "," + Integer.toString(font.getStyle()) + "," + Integer.toString(font.getSize());
            prefs.put(key, value);
        }
        
        GradientRectangleColorScheme gradientColor;
        for(String key : mGradientsMap.keySet()){
            gradientColor = mGradientsMap.get(key);
            value = Integer.toString(gradientColor.getColor1().getRGB()) + "," + 
                    Integer.toString(gradientColor.getColor2().getRGB()) + "," +
                    Integer.toString(gradientColor.getColor3().getRGB()) + "," +
                    Integer.toString(gradientColor.getColor4().getRGB()) + "," +
                    Integer.toString(gradientColor.getColor5().getRGB());
            prefs.put(key, value);
        }

        for(String key : mGeneralsMap.keySet()){
            prefs.put(key, mGeneralsMap.get(key).toString());
        }
    }
    
    public LinkedHashMap<String, String> getDefaultColors() {
         LinkedHashMap<String, String> colorsMap = new LinkedHashMap<String, String>();
        
        colorsMap.put(S_COLOR_REGION_BINDING, new Integer((new Color(241, 254, 239)).getRGB()).toString());
        colorsMap.put(S_COLOR_BC_REGION_TITLE, new Integer((new Color(64, 114,  57)).getRGB()).toString());
        
        colorsMap.put(S_COLOR_REGION_ENGINE,  new Integer((new Color(255, 255, 255)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_REGION_TITLE, new Integer((new Color(70,  70,  70)).getRGB()).toString());
        
        colorsMap.put(S_COLOR_REGION_EXTERNAL, new Integer((new Color(251, 249, 242)).getRGB()).toString());
        colorsMap.put(S_COLOR_EXT_SU_REGION_TITLE, new Integer((new Color(209,  97,   0)).getRGB()).toString());

        colorsMap.put(S_COLOR_BC_TITLE, new Integer((new Color(102, 102,  102)).getRGB()).toString());
        colorsMap.put(S_COLOR_BC_BACKGROUND, new Integer((new Color(255, 255, 255)).getRGB()).toString());
        colorsMap.put(S_COLOR_BC_TITLE_BACKGROUND, new Integer((new Color(191, 219, 219)).getRGB()).toString());
        colorsMap.put(S_COLOR_BC_BORDER, new Integer((new Color(186, 205, 240)).getRGB()).toString());
        
        // temporarily preserve these colors, as it is still undecided on the title color defaults
//        colorsMap.put(S_COLOR_BC_REGION_TITLE, new Integer((new Color(64, 114,  57)).getRGB()).toString());
//        colorsMap.put(S_COLOR_SU_REGION_TITLE, new Integer((new Color(70,  70,  70)).getRGB()).toString());
//        colorsMap.put(S_COLOR_EXT_SU_REGION_TITLE, new Integer((new Color(209,  97,   0)).getRGB()).toString());
        colorsMap.put(S_COLOR_BC_REGION_TITLE, new Integer((new Color(153, 153, 153)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_REGION_TITLE, new Integer((new Color(153, 153, 153)).getRGB()).toString());
        colorsMap.put(S_COLOR_EXT_SU_REGION_TITLE, new Integer((new Color(153, 153, 153)).getRGB()).toString());

        colorsMap.put(S_COLOR_SU_INTERNAL_BORDER, new Integer((new Color(186, 205, 240)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_INTERNAL_TITLE, new Integer((new Color(102,  102, 102)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_INTERNAL_BACKGROUND, new Integer((new Color(191, 219, 219)).getRGB()).toString());
        
        colorsMap.put(S_COLOR_SU_EXTERNAL_BORDER, new Integer((new Color(186, 205, 240)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_EXTERNAL_TITLE, new Integer((new Color(102, 102, 102)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_EXTERNAL_BACKGROUND, new Integer((new Color(255, 230, 159)).getRGB()).toString());
        
        
        colorsMap.put(S_COLOR_SELECTED_BORDER, new Integer((new Color(255, 130, 0)).getRGB()).toString());
        colorsMap.put(S_COLOR_CONNECTION_NORMAL, new Integer((new Color(204, 204, 204)).getRGB()).toString());
        colorsMap.put(S_COLOR_CONNECTION_HOVERED, new Integer((new Color(38,  85,  185)).getRGB()).toString());
        colorsMap.put(S_COLOR_CONNECTION_SELECTED, new Integer((new Color(255, 130, 0)).getRGB()).toString());


        return colorsMap;
    }
    
    public LinkedHashMap<String, String> getDefaultFonts() {
        LinkedHashMap<String, String> fontsMap = new LinkedHashMap<String, String>();   
        
        fontsMap.put(S_FONT_BC_REGION_TITLE, "Dialog, 1, 14");
        fontsMap.put(S_FONT_SU_REGION_TITLE, "Dialog, 1, 14");
        fontsMap.put(S_FONT_EXT_SU_REGION_TITLE, "Dialog, 1, 14");

        fontsMap.put(S_FONT_BC_TITLE, "SansSerif, 1, 14");
        fontsMap.put(S_FONT_SU_TITLE, "");
        fontsMap.put(S_FONT_EXT_SU_TITLE, "");
        
        return fontsMap;
    }
    
    public LinkedHashMap<String, String> getDefaultGradients() {
        LinkedHashMap<String, String> gradientsMap = new LinkedHashMap<String, String>();

        String gradientColor = new Integer((new Color(221, 235, 246)).getRGB()).toString() + "," +
                              new Integer((new Color(255, 255, 255)).getRGB()).toString() + "," +
                              new Integer((new Color(214, 235, 255)).getRGB()).toString() + "," +
                              new Integer((new Color(241, 249, 253)).getRGB()).toString() + "," +
                              new Integer((new Color(255, 255, 255)).getRGB()).toString();
        
        gradientsMap.put(S_COLOR_SU_INTERNAL_BACKGROUND, gradientColor);
        gradientsMap.put(S_COLOR_SU_EXTERNAL_BACKGROUND, gradientColor);
        gradientsMap.put(S_COLOR_BC_TITLE_BACKGROUND, gradientColor);
        
        return gradientsMap;
    }
    
    public LinkedHashMap<String, String> getDefaultGenerals() {
        LinkedHashMap<String, String> generalsMap = new LinkedHashMap<String, String>();
        generalsMap.put(S_GEN_AUTO_SAVE, "false");
        generalsMap.put(S_GEN_RESTORE_DEFAULTS, "false");
        return generalsMap;
    }
    
    private boolean isAutoSave() {
        return getGeneralsMapReference().get(S_GEN_AUTO_SAVE).booleanValue();
    }
    
    private boolean isRestoreDefaults() {
        return getGeneralsMapReference().get(S_GEN_RESTORE_DEFAULTS).booleanValue();
    }
}
