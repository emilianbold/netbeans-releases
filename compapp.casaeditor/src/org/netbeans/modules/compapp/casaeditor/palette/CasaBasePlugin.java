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
package org.netbeans.modules.compapp.casaeditor.palette;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteCategoryID;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteItemID;
import org.netbeans.modules.compapp.casaeditor.api.CasaPalettePlugin;
import org.netbeans.modules.compapp.casaeditor.api.PluginDropHandler;
import org.netbeans.modules.compapp.projects.jbi.api.JbiBindingInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;

/**
 *
 * @author Josh Sandusky
 */
public class CasaBasePlugin implements CasaPalettePlugin {
    
    private static final CasaBasePlugin mInstance = new CasaBasePlugin();
    
    private CasaBasePlugin() {
    }
    
    public static CasaBasePlugin getInstance() {
        return mInstance;
    }
    
    public CasaPaletteItemID[] getItemIDs() {
        List<CasaPaletteItemID> items = new ArrayList<CasaPaletteItemID>();
        // wsdl bindings
        items.addAll(getExternalWsdlPointItems());
        // service units
        items.add(CasaPalette.ITEM_ID_EXTERNAL_SU);
        // end points
        items.add(CasaPalette.ITEM_ID_CONSUME);
        items.add(CasaPalette.ITEM_ID_PROVIDE);
        return items.toArray(new CasaPaletteItemID[items.size()]);
    }
    
    
    private List<CasaPaletteItemID> getExternalWsdlPointItems() {
        List<CasaPaletteItemID> bcItems = new ArrayList<CasaPaletteItemID>();
        HashMap bcTemplates = getWsdlTemplates();
        JbiDefaultComponentInfo bcinfo = JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
        if (bcinfo != null) {
            List<JbiBindingInfo> bclist = bcinfo.getBindingInfoList();
            for (JbiBindingInfo bi : bclist) {
                String biName = bi.getBindingName().toUpperCase();
                if (bcTemplates.get(biName) != null) {
                    CasaPaletteItemID item = new CasaPaletteItemID(
                            this,
                            CasaPalette.CATEGORY_ID_WSDL_BINDINGS, 
                            bi.getBindingName(),
                            bi.getIcon().getFile());
                    item.setDataObject(bi.getBcName()); // set the component name
                    bcItems.add(item);
                }
            }
        }
        return bcItems;
    }
    
    private HashMap getWsdlTemplates() {
        ExtensibilityElementTemplateFactory factory = new ExtensibilityElementTemplateFactory();
        Collection<TemplateGroup> groups = factory.getExtensibilityElementTemplateGroups();
        Vector<LocalizedTemplateGroup> protocols = new Vector<LocalizedTemplateGroup>();
        LocalizedTemplateGroup ltg = null;
        HashMap temps = new HashMap();
        for (TemplateGroup group : groups) {
            ltg = factory.getLocalizedTemplateGroup(group);
            protocols.add(ltg);
            temps.put(ltg.getName(), ltg);
        }
        return temps;
    }

    public void handleDrop(PluginDropHandler handler, CasaPaletteItemID itemID) {
        DefaultPluginDropHandler defaultHandler = (DefaultPluginDropHandler) handler;
        String categoryID = itemID.getCategory();
        if        (categoryID.equals(CasaPalette.CATEGORY_ID_WSDL_BINDINGS)) {
            defaultHandler.addCasaPort(
                    itemID.getDisplayName(), 
                    (String) itemID.getDataObject()); // this is the component name
        } else if (categoryID.equals(CasaPalette.CATEGORY_ID_SERVICE_UNITS)) {
            if        (itemID.equals(CasaPalette.ITEM_ID_INTERNAL_SU)) {
                // add an internal SU to the model
                defaultHandler.addServiceEngineServiceUnit(true);
            } else if (itemID.equals(CasaPalette.ITEM_ID_EXTERNAL_SU)) {
                // add an external SU to the model
                defaultHandler.addServiceEngineServiceUnit(false);
            }
        }
    }

    public REGION getDropRegion(CasaPaletteItemID itemID) {
        if (itemID != null) {
            String categoryID = itemID.getCategory();
            if (categoryID.equals(CasaPalette.CATEGORY_ID_WSDL_BINDINGS)) {
                return REGION.WSDL_ENDPOINTS;
            } else if (categoryID.equals(CasaPalette.CATEGORY_ID_SERVICE_UNITS)) {
                if        (itemID.equals(CasaPalette.ITEM_ID_INTERNAL_SU)) {
                    return REGION.JBI_MODULES;
                } else if (itemID.equals(CasaPalette.ITEM_ID_EXTERNAL_SU)) {
                    return REGION.EXTERNAL;
                }
            }
        }
        return null;
    }
}
