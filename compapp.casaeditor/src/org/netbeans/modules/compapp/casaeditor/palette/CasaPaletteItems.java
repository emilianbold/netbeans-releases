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

import java.util.*;
import org.netbeans.modules.compapp.casaeditor.plugin.CasaPalettePlugin;

import org.netbeans.modules.compapp.projects.jbi.api.JbiBindingInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author rdara
 */
public class CasaPaletteItems extends Index.ArrayChildren {
   
    private CasaPaletteCategoryID mCategoryID;
    private Lookup mLookup;
    
   
    public CasaPaletteItems(CasaPaletteCategoryID categoryID, Lookup lookup) {
        this.mCategoryID = categoryID;
        mLookup = lookup;
    }

    protected java.util.List<Node> initCollection() {
        ArrayList childrenNodes = new ArrayList();
        
        if       (mCategoryID.equals(CasaPalette.CATEGORY_ID_WSDL_BINDINGS)) {
            addExternalWsdlPoints(childrenNodes);
        } else if(mCategoryID.equals(CasaPalette.CATEGORY_ID_SERVICE_UNITS)) {
            addServiceUnits(childrenNodes); 
        } else if(mCategoryID.equals(CasaPalette.CATEGORY_ID_END_POINTS)) {
            addInternalEndPoints(childrenNodes);
        } else {
            // must be a plugin category
            addPluginPaletteNodes(childrenNodes);
        }
        
        return childrenNodes;
    }

    private void addPluginPaletteNodes(ArrayList childrenNodes) {
        CasaPaletteItemID[] pluginItems = mCategoryID.getPlugin().getItemIDs(mCategoryID);
        if (pluginItems != null) {
            for (CasaPaletteItemID itemID : pluginItems) {
                childrenNodes.add(new CasaPaletteItemNode(itemID, mLookup));
            }
        }
    }
    
    private void addExternalWsdlPoints(ArrayList childrenNodes) {
        HashMap bcTemplates = getWsdlTemplates();
        JbiDefaultComponentInfo bcinfo = JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
        if (bcinfo != null) {
            List<JbiBindingInfo> bclist = bcinfo.getBindingInfoList();
            for (JbiBindingInfo bi : bclist) {
                String biName = bi.getBindingName().toUpperCase();
                if (bcTemplates.get(biName) != null) {
                    CasaPaletteItemID item = new CasaPaletteItemID(
                            CasaPalette.CATEGORY_ID_WSDL_BINDINGS, 
                            bi.getBindingName(),
                            bi.getIcon().getFile());
                    item.setDataObject(bi.getBcName()); // set the component name
                    childrenNodes.add(new CasaPaletteItemNode(item, mLookup, true));
                }
            }
        }
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
    
    private void addInternalEndPoints(ArrayList childrenNodes) {
        childrenNodes.add(new CasaPaletteItemNode(CasaPalette.ITEM_ID_CONSUME, mLookup));
        childrenNodes.add(new CasaPaletteItemNode(CasaPalette.ITEM_ID_PROVIDE, mLookup));
    }

    private void addServiceUnits(ArrayList childrenNodes) {
        // Don't add Int. SU / Jbi Module. Add it when its supported.
//        childrenNodes.add(new CasaPaletteItemNode(CasaPalette.ITEM_ID_INTERNAL_SU, mLookup));
        
        childrenNodes.add(new CasaPaletteItemNode(CasaPalette.ITEM_ID_EXTERNAL_SU, mLookup));
    }
}
