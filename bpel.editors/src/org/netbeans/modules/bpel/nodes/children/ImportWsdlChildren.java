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

package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.netbeans.modules.bpel.nodes.synchronizer.ModelSynchronizer;
import org.netbeans.modules.bpel.nodes.synchronizer.SynchronisationListener;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 12 April 2006
 */
public class ImportWsdlChildren extends Children.Keys implements SynchronisationListener {
    
    private Lookup myLookup;
    private WSDLModel myWsdlModel;
    private ModelSynchronizer synchronizer;
    
    public ImportWsdlChildren(Import importObj, Lookup lookup) {
        myLookup = lookup;
        //
        myWsdlModel = ImportHelper.getWsdlModel(importObj, true);
        synchronizer = new ModelSynchronizer(this);
        synchronizer.subscribe(myWsdlModel);
    }
    
    protected Node[] createNodes(Object key) {
        NavigatorNodeFactory factory = NavigatorNodeFactory.getInstance();
        Node childNode = null;
        
        if (key instanceof PartnerLinkType) {
            childNode = factory.createNode(
                    NodeType.PARTNER_LINK_TYPE, key, myLookup);
        } else if (key instanceof CorrelationProperty) {
            childNode = factory.createNode(
                    NodeType.CORRELATION_PROPERTY, key, myLookup);
        } else if (key instanceof PropertyAlias) {
            childNode = factory.createNode(
                    NodeType.CORRELATION_PROPERTY_ALIAS, key, myLookup);
        }

        return childNode == null ? new Node[0] : new Node[] {childNode};
    }
    
    Collection getNodeKeys() {
        if (myWsdlModel == null) {
            return Collections.EMPTY_LIST;
        }
        
        List<Object> childs = new ArrayList<Object>();
        
        // set partnerLinkType nodes
        List<PartnerLinkType> plTypeList = myWsdlModel.getDefinitions().
                getExtensibilityElements(PartnerLinkType.class);
        if (plTypeList != null && plTypeList.size() > 0) {
            childs.addAll(plTypeList);
        }
        
        // set property nodes
        List<CorrelationProperty> cpList = myWsdlModel.getDefinitions().
                getExtensibilityElements(CorrelationProperty.class);
        if (cpList != null && cpList.size() > 0) {
            childs.addAll(cpList);
        }
        
        // set propertyAlias mapped to other wsdl
        List<PropertyAlias> propAliasList = myWsdlModel.getDefinitions().
                getExtensibilityElements(PropertyAlias.class);
        if (propAliasList != null && propAliasList.size() > 0) {
            for (PropertyAlias tmpPropAlias : propAliasList) {
                NamedComponentReference<CorrelationProperty> tmpCorrProp 
                        = tmpPropAlias.getPropertyName();
                if (tmpCorrProp != null 
                        && tmpCorrProp.get() != null
                        && !(myWsdlModel.equals(tmpCorrProp.get().getModel()))) 
                {
                    childs.add(tmpPropAlias);
                }
            }
        }        

        return childs;
    }
    
    protected void addNotify() {
        reload();
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }
    
    public void reload() {
        setKeys(getNodeKeys());
    }

    public void componentUpdated(Component component) {
        if (myWsdlModel == null) {
            return;
        }

        if (component instanceof PropertyAlias ) {
            reload();
//            NamedComponentReference<CorrelationProperty> tmpCorrRef 
//                    = ((PropertyAlias)component).getPropertyName();
//            if (tmpCorrRef == null ) {
//                return;
//            }
//            CorrelationProperty tmpCorrProp = tmpCorrRef.get();
//            if (tmpCorrProp == null) {
//                return;
//            }
//            WSDLModel corrPropModel = tmpCorrProp.getModel();
//            if (!myWsdlModel.equals(corrPropModel)) {
//                System.out.println("propAlias is Update : componentUpdated ");
//                reload();
//            }
        }
    }

    public void childrenUpdated(Component component) {
        if (myWsdlModel == null) {
            return;
        }
        if (component == myWsdlModel.getDefinitions()) {
            reload();
        }
        
//        if (component instanceof PropertyAlias ) {
//            NamedComponentReference<CorrelationProperty> tmpCorrRef 
//                    = ((PropertyAlias)component).getPropertyName();
//            if (tmpCorrRef == null ) {
//                return;
//            }
//            CorrelationProperty tmpCorrProp = tmpCorrRef.get();
//            if (tmpCorrProp == null) {
//                return;
//            }
//            WSDLModel corrPropModel = tmpCorrProp.getModel();
//            if (!myWsdlModel.equals(corrPropModel)) {
//                System.out.println("propAlias is Update : childrenUpdated ");
//                reload();
//            }
//        }
    }
    
    
}

