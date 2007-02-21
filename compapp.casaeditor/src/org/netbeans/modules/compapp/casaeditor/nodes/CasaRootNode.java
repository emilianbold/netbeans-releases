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
 * CasaRootNode.java
 *
 * Created on November 2, 2006, 8:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.lang.ref.WeakReference;
import org.netbeans.modules.compapp.casaeditor.graph.CasaCustomizer;
import org.netbeans.modules.compapp.casaeditor.graph.CasaFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Josh Sandusky
 */
public class CasaRootNode extends CasaNode {
    
    private static final Image ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/CasaRootNode.png");
    
    private static final String CHILD_ID_WSDL_ENDPOINTS  = "WSDLEndpoints";
    private static final String CHILD_ID_SERVICE_ENGINES = "ServiceEngines";
    private static final String CHILD_ID_CONNECTIONS     = "Connections";
    
    private static final String[] CHILD_TYPES = {
        CHILD_ID_WSDL_ENDPOINTS,
        CHILD_ID_SERVICE_ENGINES,
        CHILD_ID_CONNECTIONS
    };
    
    private static final String AUTO_SAVE = "AutoSave";
    private static final String LOAD_DEFAULTS = "RestoreDefaultso";
    
    
    public CasaRootNode(Object data, Children children, Lookup lookup) {
        super(data, children, lookup);
    }
    
    public CasaRootNode(Object data, Lookup lookup) {
        super(data, new MyChildren(data, lookup), lookup);
    }
    
    
    public String getName() {
        return NbBundle.getMessage(getClass(), "LBL_CasaModel");
    }

    protected void setupPropertySheet(Sheet sheet) {
        final CasaWrapperModel model = (CasaWrapperModel) getData();
        if (model == null) {
            return;
        }
        
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
                        "") {
                    
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
                        "") {
                    
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
        
        for(String key : CasaFactory.getCasaCustomizer().getGeneralsMapReference().keySet()) {
            genericPropertySet.put (
                  new PropertySupport.ReadWrite(
                        key, // NO18N
                        Boolean.class, 
                            NbBundle.getMessage(getClass(), key), 
                        "") {
                    
                    public Object getValue() {
                        return CasaFactory.getCasaCustomizer().getValue(getName());
                    }
                    public void setValue(Object value) {
                        CasaFactory.getCasaCustomizer().setValue(getName(), (Boolean) value);
                    }
                    public void restoreDefaultValue() {
                        CasaCustomizer customizer = CasaFactory.getCasaCustomizer();
                        String strValue = customizer.getDefaultGenerals().get(getName());
                        customizer.setValue(getName(), customizer.getBoolean(strValue));
                    }
                    public boolean supportsDefaultValue() {
                        return true;
                    }

                });          
        }

        
    }

    private static class MyChildren extends CasaNodeChildren {
        private WeakReference mReference;
        public MyChildren(Object data, Lookup lookup) {
            super(data, lookup);
            mReference = new WeakReference(data);
        }
        protected Node[] createNodes(Object key) {
            String keyName = (String) key;
            if (mReference.get() != null) {
                try {
                CasaWrapperModel model = (CasaWrapperModel) mReference.get();
                if (keyName.equals(CHILD_ID_WSDL_ENDPOINTS)) {
                    return new Node[] { 
                        new WSDLEndpointsNode(
                                model.getCasaPorts(),
                                mLookup) };
                } else if (keyName.equals(CHILD_ID_SERVICE_ENGINES)) {
                    return new Node[] { 
                        new ServiceEnginesNode(
                            model.getServiceEngineServiceUnits(), 
                            mLookup) };
                } else if (keyName.equals(CHILD_ID_CONNECTIONS)) {
                    return new Node[] { 
                        new ConnectionsNode(
                            model.getCasaConnectionList(false), 
                            mLookup) };
                }
                }catch (Exception e) {} // TMP
            }
            return null;
        }
        public Object getChildKeys(Object data)  {
            return CHILD_TYPES;
        }
    }
    
    public Image getIcon(int type) {
        return ICON;
    }
    
    public Image getOpenedIcon(int type) {
        return ICON;
    }
}
