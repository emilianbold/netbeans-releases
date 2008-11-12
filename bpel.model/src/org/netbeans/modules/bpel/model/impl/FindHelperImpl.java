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
package org.netbeans.modules.bpel.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.spi.FindHelper;
import org.netbeans.modules.bpel.model.api.support.ContainerIterator;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Helper class for finding various elements in OM.
 * 
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.model.spi.FindHelper.class)
public final class FindHelperImpl implements FindHelper {

    public FindHelperImpl() {}

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.spi.FindHelper#scopeIterator(org.netbeans.modules.soa.model.bpel20.api.BpelEntity)
     */
    public Iterator<BaseScope> scopeIterator( BpelEntity entity ) {
        return new ContainerIterator<BaseScope>(entity, BaseScope.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.spi.FindHelper#getParentActivity(org.netbeans.modules.soa.model.bpel20.api.BpelEntity)
     */
    public Activity getParentActivity( BpelEntity entity ) {
        ((BpelEntityImpl) entity).readLock();
        try {
            ContainerIterator<Activity> iterator = new ContainerIterator<Activity>(
                    entity, Activity.class);
            return iterator.next();
        }
        finally {
            ((BpelEntityImpl) entity).readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.spi.FindHelper#getXPath()
     */
    public String getXPath( BpelEntity entity ) {
        return ((BpelEntityImpl)entity).getModel().getXPathExpression( entity );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.spi.FindHelper#findModelElements(java.lang.String)
     */
    public BpelEntity[] findModelElements( BpelModel model, String xpath ) {
        BpelModelImpl modelImpl = (BpelModelImpl)model;
        assert modelImpl.getDocument() instanceof Document;
        Document doc = (Document)((BpelModelImpl)model).getDocument();
        List<Node> list = ((BpelModelImpl)model).getAccess().findNodes( doc , 
                xpath );
        List<BpelEntity> ret = new ArrayList<BpelEntity>( list.size() );
        for (Node node : list) {
            if ( node instanceof Element ){
                List<Element> pathToRoot = ((BpelModelImpl)model).getAccess().
                        getPathFromRoot(((BpelModelImpl)model).getDocument(), 
                                (Element)node );
                Component comp = ((BpelModelImpl)model).findComponent(pathToRoot);
                assert comp instanceof BpelEntity;
                ret.add( (BpelEntity) comp );
            }
        }
        return ret.toArray( new BpelEntity[ ret.size()] );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.spi.FindHelper#varaibleDeclarationScopes(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    public Iterator<VariableDeclarationScope> varaibleDeclarationScopes( 
            BpelEntity entity ) 
    {
        return new ContainerIterator<VariableDeclarationScope>(entity, 
                VariableDeclarationScope.class);
    }

    /**
     * Collects a path from the BPEL process root to the specified entity. 
     * The first element in the result list is the closest to the root.
     */ 
    public static List<BpelContainer> getObjectPathTo(BpelEntity entity) {
        ArrayList<BpelContainer> result = new ArrayList<BpelContainer>();
        BpelContainer parent = entity.getParent();
        while (parent != null) {
            result.add(0, parent);
            //
            parent = parent.getParent();
        }
        //
        return result;
    }
}
