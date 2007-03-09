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

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.graph.CasaCustomizer;
import org.netbeans.modules.compapp.casaeditor.graph.CasaFactory;
import org.netbeans.modules.compapp.casaeditor.properties.LookAndFeelProperty;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author jsandusky
 */
public class LookAndFeelNode extends CasaNode {
    
    private static final Image ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/LookAndFeelNode.png"); // NOI18N
    
    
    public LookAndFeelNode() {
        super();
    }
        
    public String getName() {
        return NbBundle.getMessage(LookAndFeelProperty.class, "LBL_LookAndFeel");        // NOI18N
    }


    protected void setupPropertySheet(Sheet sheet) {
        Sheet.Set fontPropertySet  = getPropertySet(sheet, PropertyUtils.PropertiesGroups.FONT_SET);
        Sheet.Set colorPropertySet = getPropertySet(sheet, PropertyUtils.PropertiesGroups.COLOR_SET);
        Sheet.Set genericPropertySet = getPropertySet(sheet, PropertyUtils.PropertiesGroups.GENERIC_SET);
        
        sheet.put(genericPropertySet);
        sheet.put(colorPropertySet);
        sheet.put(fontPropertySet);

        for(String key : CasaFactory.getCasaCustomizer().getColorsMapReference().keySet()) {
            colorPropertySet.put (
                  new PropertySupport.ReadWrite(
                        key, // NO18N
                        Color.class, 
                        NbBundle.getMessage(getClass(), key), 
                        Constants.EMPTY_STRING) {
                    
                    public Object getValue() {
                        return CasaFactory.getCasaCustomizer().getValue(getName());
                    }
                    public void setValue(Object value) {
                        CasaFactory.getCasaCustomizer().setValue(getName(), (Color) value);
                    }
                    public void restoreDefaultValue() {
                        CasaCustomizer customizer = CasaFactory.getCasaCustomizer();
                        String strValue = customizer.getDefaultColors().get(getName());
                        customizer.setValue(getName(), new Color(Integer.parseInt(strValue)));
                        if(customizer.getDefaultGradients().containsKey(getName())) {
                            strValue = customizer.getDefaultGradients().get(getName());
                            customizer.setValue(getName(), customizer.getGradient(strValue));
                        } 
                    }
                    public boolean supportsDefaultValue() {
                        return true;
                    }
                });          
        }
        
        for(String key : CasaFactory.getCasaCustomizer().getFontsMapReference().keySet()) {
            fontPropertySet.put (
                  new PropertySupport.ReadWrite(
                        key, // NO18N
                        Font.class, 
                            NbBundle.getMessage(getClass(), key), 
                        Constants.EMPTY_STRING) {
                    
                    public Object getValue() {
                        return CasaFactory.getCasaCustomizer().getValue(getName());
                    }
                    public void setValue(Object value) {
                        CasaFactory.getCasaCustomizer().setValue(getName(), (Font) value);
                    }
                    public void restoreDefaultValue() {
                        CasaCustomizer customizer = CasaFactory.getCasaCustomizer();
                        String strValue = customizer.getDefaultFonts().get(getName());
                        customizer.setValue(getName(), customizer.getFont(strValue));
                    }
                    public boolean supportsDefaultValue() {
                        return true;
                    }
                    
                });          
        }
    }
    
    public Image getIcon(int type) {
        return ICON;
    }
    
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
}
