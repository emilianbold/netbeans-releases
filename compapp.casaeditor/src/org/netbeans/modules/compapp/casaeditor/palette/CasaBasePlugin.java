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
package org.netbeans.modules.compapp.casaeditor.palette;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
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
        Map<String, LocalizedTemplateGroup> bcTemplates = getWsdlTemplates();
        JbiDefaultComponentInfo bcinfo = JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
        if (bcinfo != null) {
            List<JbiBindingInfo> bclist = bcinfo.getBindingInfoList();
            for (JbiBindingInfo bi : bclist) {
                String bindingType = bi.getBindingType().toUpperCase();
                if (bcTemplates.get(bindingType) != null) {
                    CasaPaletteItemID item = new CasaPaletteItemID(
                            this,
                            CasaPalette.CATEGORY_ID_WSDL_BINDINGS, 
                            bi.getBindingType(),
                            bi.getIcon().getFile());
                    item.setDataObject(bi.getBindingComponentName()); // set the component name
                    bcItems.add(item);
                } else {
                    System.err.println("WARNING: JBI binding component template for " + bindingType + " is undefined.");
                }
            }
        }
        return bcItems;
    }
    
    private Map<String, LocalizedTemplateGroup> getWsdlTemplates() {
        ExtensibilityElementTemplateFactory factory = ExtensibilityElementTemplateFactory.getDefault();
        Collection<TemplateGroup> groups = factory.getExtensibilityElementTemplateGroups();
        Vector<LocalizedTemplateGroup> protocols = new Vector<LocalizedTemplateGroup>();
        LocalizedTemplateGroup ltg = null;
        Map<String, LocalizedTemplateGroup> temps = new HashMap<String, LocalizedTemplateGroup>();
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
