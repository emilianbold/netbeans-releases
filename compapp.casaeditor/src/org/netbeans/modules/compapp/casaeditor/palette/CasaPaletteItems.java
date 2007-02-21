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
 * CasaPaletteItems.java
 *
 * Created on December 8, 2006, 4:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.palette;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.compapp.projects.jbi.api.JbiBindingInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author rdara
 */
public class CasaPaletteItems extends Index.ArrayChildren {
   
    private CasaPaletteCategory category;
   
    public CasaPaletteItems(CasaPaletteCategory Category) {
        this.category = Category;
    }

    protected java.util.List<Node> initCollection() {
        ArrayList childrenNodes = new ArrayList();
        
        if(category.getCasaCategoryType().equals(CasaPalette.CASA_CATEGORY_TYPE.WSDL_BINDINGS)) {
            addExternalWsdlPoints(childrenNodes);
        }
        if(category.getCasaCategoryType().equals(CasaPalette.CASA_CATEGORY_TYPE.SERVICE_UNITS)) {
            addServiceUnits(childrenNodes);
        }
        if(category.getCasaCategoryType().equals(CasaPalette.CASA_CATEGORY_TYPE.END_POINTS)) {
            addInternalEndPoints(childrenNodes);
        }
        return childrenNodes;
    }
    
    private void addExternalWsdlPoints(ArrayList childrenNodes) {
        JbiDefaultComponentInfo bcinfo = JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
        if (bcinfo != null) {
            List<JbiBindingInfo> bclist = bcinfo.getBindingInfoList();
            for (JbiBindingInfo bi : bclist) {
                CasaPaletteItem item = new CasaPaletteItem();
                item.setCategoryType(CasaPalette.CASA_CATEGORY_TYPE.WSDL_BINDINGS);
                item.setTitle(bi.getBindingName());
                item.setComponentName(bi.getBcName());
                childrenNodes.add( new CasaPaletteItemNode( item, bi.getIcon().getFile()) );
            }
        }
    }
    
    private void addInternalEndPoints(ArrayList childrenNodes) {
        CasaPaletteItem consumeItem = new CasaPaletteItem();
        consumeItem.setCategoryType(CasaPalette.CASA_CATEGORY_TYPE.END_POINTS);
        consumeItem.setPaletteItemType(CasaPalette.CASA_PALETTE_ITEM_TYPE.CONSUME);
        consumeItem.setTitle(getMessage("Palette_Consume_Title"));
        childrenNodes.add( new CasaPaletteItemNode( consumeItem, "org/netbeans/modules/compapp/casaeditor/palette/resources/consume16.png" ) );
        
        CasaPaletteItem provideItem = new CasaPaletteItem();
        provideItem.setCategoryType(CasaPalette.CASA_CATEGORY_TYPE.END_POINTS);
        provideItem.setPaletteItemType(CasaPalette.CASA_PALETTE_ITEM_TYPE.PROVIDE);
        provideItem.setTitle(getMessage("Palette_Provide_Title"));
        childrenNodes.add( new CasaPaletteItemNode( provideItem, "org/netbeans/modules/compapp/casaeditor/palette/resources/provide16.png" ) );
      
    }

    private void addServiceUnits(ArrayList childrenNodes) {
        CasaPaletteItem intsuItem = new CasaPaletteItem();
        intsuItem.setCategoryType(CasaPalette.CASA_CATEGORY_TYPE.SERVICE_UNITS);
        intsuItem.setPaletteItemType(CasaPalette.CASA_PALETTE_ITEM_TYPE.INT_SU);
        intsuItem.setTitle(getMessage("Palette_IntSU_Title"));
        childrenNodes.add( new CasaPaletteItemNode( intsuItem, "org/netbeans/modules/compapp/casaeditor/palette/resources/intsu.png" ) );

        CasaPaletteItem extsuItem = new CasaPaletteItem();
        extsuItem.setCategoryType(CasaPalette.CASA_CATEGORY_TYPE.SERVICE_UNITS);
        extsuItem.setPaletteItemType(CasaPalette.CASA_PALETTE_ITEM_TYPE.EXT_SU);
        extsuItem.setTitle(getMessage("Palette_ExtSU_Title"));
        childrenNodes.add( new CasaPaletteItemNode( extsuItem, "org/netbeans/modules/compapp/casaeditor/palette/resources/extsu.png" ) );

    }

    private String getMessage(String key) {
        return NbBundle.getBundle(CasaPaletteItems.class).getString(key); 
    }

}
