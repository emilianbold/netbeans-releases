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
package org.netbeans.modules.soa.ui.wsdl;

import javax.swing.Icon;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.NbBundle;

/**
 * The implementation of the TreeItemInfoProvider for the wsdl related objects.
 * 
 * @author Vitaly Bychkov
 */
public class WSDLTreeInfoProvider implements TreeItemInfoProvider {

    public static enum ToolTipTitles {
        PORT_TYPE, 
        OPERATION, 
        ;
    
        public String getName() {
            String title = NbBundle.getMessage(
                    WSDLTreeInfoProvider.class, this.toString());
            return title;
        }
    
    }
    
    private static WSDLTreeInfoProvider singleton = new WSDLTreeInfoProvider();
    
    public static WSDLTreeInfoProvider getInstance() {
        return singleton;
    }

    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        //
        if (dataObj instanceof WSDLModel) {
            WSDLModel wsdlModel = (WSDLModel)dataObj;
            Definitions defs = wsdlModel.getDefinitions();
                return defs.getTargetNamespace();
        } 
        //
        return getDisplayByDataObj(dataObj);
    }
    
    public String getDisplayByDataObj(Object dataObj) {
        //
        if (dataObj instanceof Named) {
            return ((Named)dataObj).getName();
        }
        //
        return null;
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        return getIconByDataObj(dataObj);
    }
    
    public Icon getIconByDataObj(Object dataObj) {
        Icon result = null;
        if (dataObj instanceof PortType) {
            result = WSDLIcons.PORT_TYPE.getIcon();
        } else if (dataObj instanceof Operation) {
            result = WSDLIcons.OPERATION.getIcon();
        } else if (dataObj instanceof WSDLModel) {
            result = WSDLIcons.WSDL_FILE.getIcon();
        }
        //
        return result;
    }

    public String getToolTipText(TreeItem treeItem) {
        return getDisplayName(treeItem);
    }
}
