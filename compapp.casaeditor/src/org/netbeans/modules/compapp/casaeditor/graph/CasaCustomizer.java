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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.awt.GradientRectangleColorScheme;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author rdara
 */
public class CasaCustomizer {
    
    private static final String S_COLOR_REGION_BINDING = "COLOR_REGION_BINDING";            // NOI18N
    private static final String S_COLOR_REGION_ENGINE = "COLOR_REGION_ENGINE";              // NOI18N
    private static final String S_COLOR_REGION_EXTERNAL = "COLOR_REGION_EXTERNAL";          // NOI18N

    private static final String S_COLOR_SU_INTERNAL_BORDER = "COLOR_SU_INTERNAL_BORDER";    // NOI18N
    private static final String S_COLOR_SU_INTERNAL_TITLE = "COLOR_SU_INTERNAL_TITLE";      // NOI18N
    private static final String S_COLOR_SU_INTERNAL_BACKGROUND = "COLOR_SU_INTERNAL_BACKGROUND";    // NOI18N
    private static final String S_COLOR_SU_INTERNAL_PIN = "COLOR_SU_INTERNAL_PIN";          // NOI18N

    private static final String S_COLOR_SU_EXTERNAL_BORDER = "COLOR_SU_EXTERNAL_BORDER";    // NOI18N
    private static final String S_COLOR_SU_EXTERNAL_TITLE = "COLOR_SU_EXTERNAL_TITLE";      // NOI18N
    private static final String S_COLOR_SU_EXTERNAL_BACKGROUND = "COLOR_SU_EXTERNAL_BACKGROUND";    // NOI18N
    private static final String S_COLOR_SU_EXTERNAL_PIN = "COLOR_SU_EXTERNAL_PIN";          // NOI18N

    private static final String S_COLOR_BC_TITLE = "COLOR_BC_TITLE";                        // NOI18N
    private static final String S_COLOR_BC_BACKGROUND = "COLOR_BC_BACKGROUND";              // NOI18N
    private static final String S_COLOR_BC_BORDER = "COLOR_BC_BORDER";                      // NOI18N
    private static final String S_COLOR_BC_TITLE_BACKGROUND = "COLOR_BC_TITLE_BACKGROUND";  // NOI18N
    private static final String S_COLOR_BC_LABEL = "COLOR_BC_LABEL";                        // NOI18N
    
    private static final String S_COLOR_SELECTION = "COLOR_SELECTION";                      // NOI18N
    
    private static final String S_COLOR_CONNECTION_NORMAL = "COLOR_CONNECTION_NORMAL";      // NOI18N
    private static final String S_COLOR_CONNECTION_HOVERED = "COLOR_CONNECTION_HOVERED";    // NOI18N
    
    private static final String S_COLOR_BC_REGION_TITLE = "COLOR_BC_REGION_TITLE";          // NOI18N
    private static final String S_COLOR_SU_REGION_TITLE = "COLOR_SU_REGION_TITLE";          // NOI18N
    private static final String S_COLOR_EXT_SU_REGION_TITLE = "COLOR_EXT_SU_REGION_TITLE";  // NOI18N

    private static final String S_FONT_BC_REGION_TITLE = "FONT_BC_REGION_TITLE";            // NOI18N
    private static final String S_FONT_SU_REGION_TITLE = "FONT_SU_REGION_TITLE";            // NOI18N
    private static final String S_FONT_EXT_SU_REGION_TITLE = "FONT_EXT_SU_REGION_TITLE";    // NOI18N

    private static final String S_FONT_BC_TITLE = "FONT_BC_TITLE";                          // NOI18N
    private static final String S_FONT_BC_LABEL = "FONT_BC_LABEL";                          // NOI18N
    private static final String S_FONT_SU_TITLE = "FONT_SU_TITLE";                          // NOI18N
    private static final String S_FONT_SU_PIN = "FONT_SU_PIN";                              // NOI18N
    
    private static final String S_FONT_EXT_SU_TITLE = "FONT_EXT_SU_TITLE";                  // NOI18N
    private static final String S_FONT_EXT_SU_PIN = "FONT_EXT_SU_PIN";                      // NOI18N
    
    private static final String S_BOOLEAN_CLASSIC_ENDPOINT_PIN_STYLE = "BOOLEAN_CLASSIC_ENDPOINT_PIN_STYLE";   // NOI18N
    private static final String S_BOOLEAN_CLASSIC_SESU_LAYOUT_STYLE = "BOOLEAN_CLASSIC_SESU_LAYOUT_STYLE";     // NOI18N
    private static final String S_BOOLEAN_CLASSIC_QOS_STYLE = "BOOLEAN_CLASSIC_QOS_STYLE";                     // NOI18N

    private static final String S_BOOLEAN_DISABLE_VALIDATION = "BOOLEAN_DISABLE_VALIDATION";                   // NOI18N
//    private static final String S_BOOLEAN_DISABLE_HOVERING_HIGHLIGHT = "BOOLEAN_DISABLE_HOVERING_HIGHLIGHT";   // NOI18N

    public static final String PROPERTY_CHANGE = "Customizer_Property_Changed";             // NOI18N

    private Map<String, Color> mColorsMap = new LinkedHashMap<String, Color>();
    private Map<String, Font> mFontsMap   = new LinkedHashMap<String, Font>();
    private Map<String, Boolean> mStylesMap   = new LinkedHashMap<String, Boolean>();
    private Map<String, GradientRectangleColorScheme> mGradientsMap =
            new LinkedHashMap<String, GradientRectangleColorScheme>();
    
    private static String S_SEPARATOR = ",";                                                    // NOI18N
    /**
     * Default Lable Font: java.awt.Font[family=Dialog,name=Dialog,style=plain,size=12]
     * Default Lable color: java.awt.Color[r=0,g=0,b=0]
     */
    
    public Map<String, Color> getColorsMapReference() {
        return mColorsMap;
    }
    
    public Map<String, Font> getFontsMapReference() {
        return mFontsMap;
    }
    
    public Map<String, GradientRectangleColorScheme> getGradientsMapReference() {
        return mGradientsMap;
    }
    
    public Map<String, Boolean> getStylesMapReference() {
        return mStylesMap;
    }
           
    /** Creates a new instance of CasaCustomizer */
    public CasaCustomizer() {
        loadFromPreferences(getPreferences(), false);
    }
    
    private Preferences getPreferences() {
        String ourNodeName = "/org/netbeans/modules/compapp/casaeditor/graph/CasaCustomizer";   // NOI18N
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

    public Color getCOLOR_SU_INTERNAL_PIN() {
        return mColorsMap.get(S_COLOR_SU_INTERNAL_PIN);
    }

    public void setCOLOR_SU_INTERNAL_PIN(Color COLOR_SU_INTERNAL_PIN) {
        mColorsMap.put(S_COLOR_SU_INTERNAL_PIN, COLOR_SU_INTERNAL_PIN);
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
    
    public Color getCOLOR_SU_EXTERNAL_PIN() {
        return mColorsMap.get(S_COLOR_SU_EXTERNAL_PIN);
    }

    public void setCOLOR_SU_EXTERNAL_PIN(Color COLOR_SU_EXTERNAL_PIN) {
        mColorsMap.put(S_COLOR_SU_EXTERNAL_PIN, COLOR_SU_EXTERNAL_PIN);
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
    
    
    public Color getCOLOR_BC_LABEL() {
        return mColorsMap.get(S_COLOR_BC_LABEL);
    }

    public void setCOLOR_BC_LABEL(Color COLOR_BC_LABEL) {
        mColorsMap.put(S_COLOR_BC_LABEL, COLOR_BC_LABEL);
    }

    public Color getCOLOR_HOVERED_EDGE() {
        return mColorsMap.get(S_COLOR_CONNECTION_HOVERED);
    }

    public void setCOLOR_HOVERED_EDGE(Color COLOR_HOVERED_EDGE) {
        mColorsMap.put(S_COLOR_CONNECTION_HOVERED, COLOR_HOVERED_EDGE);
    }

    public Color getCOLOR_SELECTION() {
        return mColorsMap.get(S_COLOR_SELECTION);
    }

    public void setCOLOR_SELECTION(Color COLOR_SELECTION) {
        mColorsMap.put(S_COLOR_SELECTION, COLOR_SELECTION);
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

    public Font getFONT_BC_LABEL() {
        return mFontsMap.get(S_FONT_BC_LABEL);
    }
    public void setFONT_BC_LABEL(Font font) {
        mFontsMap.put(S_FONT_BC_LABEL, font);
    }

    public Font getFONT_SU_HEADER() {
        return mFontsMap.get(S_FONT_SU_TITLE);
    }
    public void setFONT_SU_HEADER(Font font) {
        mFontsMap.put(S_FONT_SU_TITLE, font);
    }

    public Font getFONT_SU_PIN() {
        return mFontsMap.get(S_FONT_SU_PIN);
    }
    public void setFONT_SU_PIN(Font font) {
        mFontsMap.put(S_FONT_SU_PIN, font);
    }
    
    public Font getFONT_EXT_SU_HEADER() {
        return mFontsMap.get(S_FONT_EXT_SU_TITLE);
    }
    public void setFONT_EXT_SU_HEADER(Font font) {
        mFontsMap.put(S_FONT_EXT_SU_TITLE, font);
    }
    
    public Font getFONT_EXT_SU_PIN() {
        return mFontsMap.get(S_FONT_EXT_SU_PIN);
    }
    public void setFONT_EXT_SU_PIN(Font font) {
        mFontsMap.put(S_FONT_EXT_SU_PIN, font);
    }
    
    public Boolean getBOOLEAN_CLASSIC_ENDPOINT_PIN_STYLE() {
        return mStylesMap.get(S_BOOLEAN_CLASSIC_ENDPOINT_PIN_STYLE);
    }    
//    public void setBOOLEAN_CLASSIC_ENDPOINT_PIN_STYLE(boolean value) {
//        mStylesMap.put(S_BOOLEAN_CLASSIC_ENDPOINT_PIN_STYLE, value);
//    }
    
    public Boolean getBOOLEAN_CLASSIC_SESU_LAYOUT_STYLE() {
        return mStylesMap.get(S_BOOLEAN_CLASSIC_SESU_LAYOUT_STYLE);
    }    
//    public void setBOOLEAN_CLASSIC_SESU_LAYOUT_STYLE(boolean value) {
//        mStylesMap.put(S_BOOLEAN_CLASSIC_SESU_LAYOUT_STYLE, value);
//    }
    
    public Boolean getBOOLEAN_CLASSIC_QOS_STYLE() {
        return mStylesMap.get(S_BOOLEAN_CLASSIC_QOS_STYLE);
    }    
//    public void setBOOLEAN_CLASSIC_QOS_STYLE(boolean value) {
//        mStylesMap.put(S_BOOLEAN_CLASSIC_QOS_STYLE, value);
//    }
    public Boolean getBOOLEAN_DISABLE_VALIDATION() {
        return mStylesMap.get(S_BOOLEAN_DISABLE_VALIDATION);
    }

//    public Boolean getBOOLEAN_DISABLE_HOVERING_HIGHLIGHT() {
//        return mStylesMap.get(S_BOOLEAN_DISABLE_HOVERING_HIGHLIGHT);
//    }

    
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
        scene.revalidate();
        scene.validate();
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
            if(widget instanceof CasaNodeWidgetEngine.Internal) {
                change((CasaNodeWidgetEngine.Internal) widget);
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
            if(widget instanceof CasaNodeWidgetEngine.External) {
                change((CasaNodeWidgetEngine.External) widget);
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

    private void change(CasaNodeWidgetEngine.Internal internalSUWidget) {
        internalSUWidget.setTitleColor(getCOLOR_SU_INTERNAL_TITLE());
        internalSUWidget.setTitleFont(getFONT_SU_HEADER());
        internalSUWidget.setPinFont(getFONT_SU_PIN());
        internalSUWidget.setPinColor(getCOLOR_SU_INTERNAL_PIN());
        internalSUWidget.updatePinImage();
        internalSUWidget.readjustBounds();
    }
    
    private void change(CasaNodeWidgetEngine.External externalSUWidget){
        externalSUWidget.setTitleColor(getCOLOR_SU_EXTERNAL_TITLE());
        externalSUWidget.setTitleFont(this.getFONT_EXT_SU_HEADER());
        externalSUWidget.setPinFont(getFONT_EXT_SU_PIN());
        externalSUWidget.setPinColor(getCOLOR_SU_EXTERNAL_PIN());
        externalSUWidget.updatePinImage();
        externalSUWidget.readjustBounds();
    }
    
    private void change(CasaNodeWidgetBinding bindingWidget) {
        bindingWidget.setBackgroundColor(getCOLOR_BC_BACKGROUND());
        bindingWidget.regenerateHeaderBorder();
        bindingWidget.regenerateVerticalTextBarImage(); //For FONT_BC_HEADER
        bindingWidget.setTitleBackgroundColor(getCOLOR_BC_TITLE_BACKGROUND());
        bindingWidget.setLabelColor(getCOLOR_BC_LABEL());
        bindingWidget.setLabelFont(getFONT_BC_LABEL());
        bindingWidget.updatePinImage();
    }

    private void change(CasaConnectionWidget connectionWidget) {
        connectionWidget.setForegroundColor(getCOLOR_CONNECTION_NORMAL());
        connectionWidget.updateQoSWidgets();
    }

    public Font getFont(String str) {
        //Font name, style, size
        if (str == null || str.trim().equals("")) {  // NOI18N
            Scene scene = new Scene();
            return scene.getDefaultFont().deriveFont(Font.BOLD);
        }
        String[] strNumbers = str.split(S_SEPARATOR);
        int appearance = Integer.parseInt(strNumbers[1].trim());
        int size = Integer.parseInt(strNumbers[2].trim());
        return new Font(strNumbers[0].trim(), appearance, size);
    }
    
    public GradientRectangleColorScheme getGradient(String str) {
        String[] strNumbers = str.split(S_SEPARATOR);  
        return new GradientRectangleColorScheme(
                new Color(Integer.parseInt(strNumbers[0])),
                new Color(Integer.parseInt(strNumbers[1])),
                new Color(Integer.parseInt(strNumbers[2])),
                new Color(Integer.parseInt(strNumbers[3])),
                new Color(Integer.parseInt(strNumbers[4])));
    }

    public Boolean getBoolean(String str) {
        Boolean bValue;
        if(str.equalsIgnoreCase("true")) {      // NOI18N
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
        } else if(getStylesMapReference().containsKey(key)) {
           retValue =  (Object) getStylesMapReference().get(key);
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
    
    public void setValue(String key, Boolean newValue) {
        Boolean oldValue = getStylesMapReference().get(key);
        
        getStylesMapReference().put(key, newValue);
        CasaFactory.getCasaCustomizerRegistor().propagateChange();
        
        if (key.equals(S_BOOLEAN_CLASSIC_SESU_LAYOUT_STYLE) &&
                oldValue != newValue) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(
                    CasaCustomizer.class,
                    "MSG_PROPERTY_CHANGE_WILL_TAKE_EFFECT_NEXT_TIME"),
                    NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd); 
        }

    }
    
    public void restoreDefaults(boolean bPropagateChange) {
        Map<String, String> defaultColors = getDefaultColors();
        int i;
        for(String key : defaultColors.keySet()){
            i = Integer.parseInt(defaultColors.get(key));
            mColorsMap.put(key, new Color(i));
        }
        
        Map<String, String> defaultFonts = getDefaultFonts();
        Font font;
        for(String key : defaultFonts.keySet()){
            font = getFont(defaultFonts.get(key));
            mFontsMap.put(key, font);
        }
        
        Map<String, String> dafaultGradients = getDefaultGradients();
        GradientRectangleColorScheme gradientColor;
        for(String key : dafaultGradients.keySet()){
            gradientColor = getGradient(dafaultGradients.get(key));
            mGradientsMap.put(key, gradientColor);
        }

        Map<String, String> defaultStyles = getDefaultStyles();
        for(String key : defaultStyles.keySet()) {
            boolean value = getBoolean(defaultStyles.get(key));
            mStylesMap.put(key, value);
        }
        
        if(bPropagateChange) {
            CasaFactory.getCasaCustomizerRegistor().propagateChange();
        }
    }
    
    public void loadFromPreferences(Preferences prefs, boolean bRenderDesignView) {
        Map<String, String> defaultColors = getDefaultColors();
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
        
        Map<String, String> defaultFonts = getDefaultFonts();
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
        
        Map<String, String> defaultStyles = getDefaultStyles();
        Boolean bool;
        for(String key : defaultStyles.keySet()){
            value = prefs.get(key, defaultStyles.get(key));
            try {
                bool = getBoolean(value);
            } catch(Exception e) {
                bool = false;
            }
            mStylesMap.put(key, bool);
        }
        
        Map<String, String> dafaultGradients = getDefaultGradients();
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

        if(bRenderDesignView) {
            CasaFactory.getCasaCustomizerRegistor().propagateChange();
        }
    }
    
    public void savePreferences() {
        String ourNodeName = "/org/netbeans/modules/compapp/casaeditor/graph/CasaCustomizer";   // NOI18N
        Preferences prefs = Preferences.userRoot().node(ourNodeName);
        
        for(String key : mColorsMap.keySet()){
            prefs.put(key, Integer.toString(mColorsMap.get(key).getRGB()));
        }
        
        for(String key : mFontsMap.keySet()){
            Font font = mFontsMap.get(key);
            String value = font.getName() + S_SEPARATOR + Integer.toString(font.getStyle()) + S_SEPARATOR + Integer.toString(font.getSize());  // NOI18N
            prefs.put(key, value);
        }
        
        for(String key : mStylesMap.keySet()){
            prefs.put(key, Boolean.toString(mStylesMap.get(key)));
        }
        
        for(String key : mGradientsMap.keySet()){
            GradientRectangleColorScheme gradientColor = mGradientsMap.get(key);
            String value = 
                    Integer.toString(gradientColor.getColor1().getRGB()) + S_SEPARATOR +                
                    Integer.toString(gradientColor.getColor2().getRGB()) + S_SEPARATOR +                
                    Integer.toString(gradientColor.getColor3().getRGB()) + S_SEPARATOR +                
                    Integer.toString(gradientColor.getColor4().getRGB()) + S_SEPARATOR +                
                    Integer.toString(gradientColor.getColor5().getRGB());
            prefs.put(key, value);
        }
    }
    
    public Map<String, String> getDefaultColors() {
        Map<String, String> colorsMap = new LinkedHashMap<String, String>();
        
        // REGIONS
        
        colorsMap.put(S_COLOR_REGION_BINDING, new Integer((new Color(241, 254, 239)).getRGB()).toString());
        colorsMap.put(S_COLOR_BC_REGION_TITLE, new Integer((new Color(153, 153, 153)).getRGB()).toString());
        colorsMap.put(S_COLOR_REGION_ENGINE,  new Integer((new Color(255, 255, 255)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_REGION_TITLE, new Integer((new Color(153, 153, 153)).getRGB()).toString());
        colorsMap.put(S_COLOR_REGION_EXTERNAL, new Integer((new Color(251, 249, 242)).getRGB()).toString());
        colorsMap.put(S_COLOR_EXT_SU_REGION_TITLE, new Integer((new Color(153, 153, 153)).getRGB()).toString());
        
        // WIDGETS
        
        colorsMap.put(S_COLOR_BC_TITLE, new Integer((new Color(102, 102,  102)).getRGB()).toString());
        colorsMap.put(S_COLOR_BC_LABEL, new Integer((new Color(102, 102,  102)).getRGB()).toString());
        colorsMap.put(S_COLOR_BC_BACKGROUND, new Integer((new Color(255, 255, 255)).getRGB()).toString());
        colorsMap.put(S_COLOR_BC_TITLE_BACKGROUND, new Integer((new Color(180, 200, 220)).getRGB()).toString());
        colorsMap.put(S_COLOR_BC_BORDER, new Integer((new Color(186, 205, 240)).getRGB()).toString());
        
        colorsMap.put(S_COLOR_SU_INTERNAL_BORDER, new Integer((new Color(186, 205, 240)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_INTERNAL_TITLE, new Integer((new Color(102,  102, 102)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_INTERNAL_PIN, new Integer((new Color(102,  102, 102)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_INTERNAL_BACKGROUND, new Integer((new Color(241, 249, 249)).getRGB()).toString());
        
        colorsMap.put(S_COLOR_SU_EXTERNAL_BORDER, new Integer((new Color(186, 205, 240)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_EXTERNAL_TITLE, new Integer((new Color(102, 102, 102)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_EXTERNAL_PIN, new Integer((new Color(102,  102, 102)).getRGB()).toString());
        colorsMap.put(S_COLOR_SU_EXTERNAL_BACKGROUND, new Integer((new Color(241, 249, 249)).getRGB()).toString());
        
        // SELECTION COLOR
        
        colorsMap.put(S_COLOR_SELECTION, new Integer((new Color(255, 100, 0)).getRGB()).toString());
        
        // CONNECTIONS
        
        colorsMap.put(S_COLOR_CONNECTION_NORMAL, new Integer((new Color(204, 204, 255)).getRGB()).toString());
        colorsMap.put(S_COLOR_CONNECTION_HOVERED, new Integer((new Color(38,  85,  185)).getRGB()).toString());
        
        return colorsMap;
    }
    
    public Map<String, String> getDefaultFonts() {
        Map<String, String> fontsMap = new LinkedHashMap<String, String>();   
        
        fontsMap.put(S_FONT_BC_REGION_TITLE, "Dialog, 1, 14");          // NOI18N
        fontsMap.put(S_FONT_SU_REGION_TITLE, "Dialog, 1, 14");          // NOI18N
        fontsMap.put(S_FONT_EXT_SU_REGION_TITLE, "Dialog, 1, 14");      // NOI18N

        fontsMap.put(S_FONT_BC_TITLE, "SansSerif, 1, 14");              // NOI18N
        fontsMap.put(S_FONT_BC_LABEL, "Dialog, 0, 12");                 // NOI18N
        fontsMap.put(S_FONT_SU_TITLE, "");                              // NOI18N
        fontsMap.put(S_FONT_SU_PIN, "Dialog, 0, 12");                   // NOI18N
        fontsMap.put(S_FONT_EXT_SU_TITLE, "");                          // NOI18N
        fontsMap.put(S_FONT_EXT_SU_PIN, "Dialog, 0, 12");               // NOI18N
        
        return fontsMap;
    }
    
    public Map<String, String> getDefaultStyles() {
        Map<String, String> stylesMap = new LinkedHashMap<String, String>();   
        
        stylesMap.put(S_BOOLEAN_CLASSIC_ENDPOINT_PIN_STYLE, "false");          // NOI18N
        stylesMap.put(S_BOOLEAN_CLASSIC_SESU_LAYOUT_STYLE, "true");          // NOI18N
        stylesMap.put(S_BOOLEAN_CLASSIC_QOS_STYLE, "true");      // NOI18N
        stylesMap.put(S_BOOLEAN_DISABLE_VALIDATION, "true");      // NOI18N
//        stylesMap.put(S_BOOLEAN_DISABLE_HOVERING_HIGHLIGHT, "false");      // NOI18N

        return stylesMap;
    }
    
    public Map<String, String> getDefaultGradients() {
        Map<String, String> gradientsMap = new LinkedHashMap<String, String>();

        String gradientColor = new Integer((new Color(221, 235, 246)).getRGB()).toString() + S_SEPARATOR +
                              new Integer((new Color(255, 255, 255)).getRGB()).toString() + S_SEPARATOR +
                              new Integer((new Color(214, 235, 255)).getRGB()).toString() + S_SEPARATOR +
                              new Integer((new Color(241, 249, 253)).getRGB()).toString() + S_SEPARATOR +
                              new Integer((new Color(255, 255, 255)).getRGB()).toString();
        
        gradientsMap.put(S_COLOR_SU_INTERNAL_BACKGROUND, gradientColor);
        gradientsMap.put(S_COLOR_SU_EXTERNAL_BACKGROUND, gradientColor);
        gradientsMap.put(S_COLOR_BC_TITLE_BACKGROUND, gradientColor);
        
        return gradientsMap;
    }
}
