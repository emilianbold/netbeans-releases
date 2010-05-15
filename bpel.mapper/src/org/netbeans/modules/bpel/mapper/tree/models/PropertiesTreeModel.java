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

package org.netbeans.modules.bpel.mapper.tree.models;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperPseudoComp;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.predicates.BpelMapperPredicate;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperTypeCast;
import org.netbeans.modules.bpel.mapper.properties.PropertiesNode;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperty;
import org.netbeans.modules.soa.ui.tree.SoaTreeExtensionModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.soa.xpath.mapper.tree.models.MapperConnectabilityProvider;

/**
 * The implementation of the MapperTreeModel for the properties' tree.
 * TODO: Extract this model from VariablesTreeModel.
 *
 * @author Nikita Krjukov
 */
public class PropertiesTreeModel implements SoaTreeExtensionModel, 
        TreeStructureProvider, MapperConnectabilityProvider {

    private BpelDesignContext mDesignContext;

    private VariableTreeInfoProvider mTreeInfoProvider;
    private boolean leftTreeFlag = true;
    
    public PropertiesTreeModel(BpelDesignContext context, boolean leftTree) {
        leftTreeFlag = leftTree;
    }
    
    public PropertiesTreeModel(BpelDesignContext context) {
        this(context, VariableTreeInfoProvider.getInstance());
    }
    
    public PropertiesTreeModel(BpelDesignContext context,
            VariableTreeInfoProvider treeInfoProvider) {
        mDesignContext = context;
        mTreeInfoProvider = treeInfoProvider;
    }
    
    public List<Object> getChildren(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof AbstractVariableDeclaration) {
            List<Object> result = null;
            AbstractVariableDeclaration varDecl = (AbstractVariableDeclaration)dataObj;
            WSDLReference<Message> msgRef = varDecl.getMessageType();
            if (msgRef != null) {
                result = new ArrayList<Object>();
                result.add(new PropertiesNode(varDecl, leftTreeFlag));
            }
            return result;
        } else if (dataObj instanceof PropertiesNode) {
            return ((PropertiesNode) dataObj).getChildren(mDesignContext);
        } else if (dataObj instanceof FileObject) {
            TreeItem i = treeItem.getParent();
            while (i != null) {
                Object data = i.getDataObject();
                if (data instanceof PropertiesNode) {
                    return ((PropertiesNode) data).getChildren(mDesignContext, 
                            dataObj);
                }
                i = i.getParent();
            }
        }
        //
        return null;
    }

    public Boolean isLeaf(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof PropertiesNode) {
            return Boolean.FALSE;
        }
        
        if (dataObj instanceof FileObject) {
            return Boolean.valueOf(!((FileObject) dataObj).isFolder());
        }
        
        return null;
    }

    public Boolean isConnectable(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof Element || 
                dataObj instanceof Attribute ||
                dataObj instanceof Part || 
                dataObj instanceof BpelMapperPredicate ||
                dataObj instanceof XPathSpecialStep ||
                dataObj instanceof BpelMapperTypeCast ||
                dataObj instanceof BpelMapperPseudoComp ||
                dataObj instanceof CorrelationProperty) {
            return Boolean.TRUE;
        }
        
        if (dataObj instanceof FileObject &&
                !((FileObject) dataObj).isFolder())
        {
            return Boolean.TRUE;
        }
        
        if (dataObj instanceof NMProperty) {
            return Boolean.TRUE;
        }
        
        //
        if (dataObj instanceof AbstractVariableDeclaration && 
                !(dataObj instanceof VariableDeclarationScope)) {
            return Boolean.TRUE;
        }
        //
        return null;
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return mTreeInfoProvider;
    }

    public TreeStructureProvider getTreeStructureProvider() {
        return this;
    }
    
    public TreeItemActionsProvider getTreeItemActionsProvider() {
        return mTreeInfoProvider;
    }
}
