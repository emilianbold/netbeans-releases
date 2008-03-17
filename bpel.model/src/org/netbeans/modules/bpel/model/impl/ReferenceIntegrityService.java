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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.BpelReferenceable;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.impl.services.InnerEventDispatcherAdapter;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.bpel.model.api.support.Utils;

/**
 * This is service that reponsible for reference integrity in OM. There are
 * possible events that can affect other entities in model: 
 * 1) changing attribute of reference => all corresponded ReferenceCollection elements
 * should update their reference attribute ( actully this is their
 * implementation, we just call method update for ReferenceCollection ). 
 * 2) Removing attribute. ????
 * 
 * @author ads
 */
public class ReferenceIntegrityService extends InnerEventDispatcherAdapter {


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        BpelEntity entity = event.getParent();
        if ( entity instanceof BpelEntityImpl
                && event instanceof PropertyUpdateEvent
                //|| event instanceof PropertyRemoveEvent
                 )
        {
            if ( !(( BpelEntityImpl )entity).isInTree()) {
                // do not perform refactoring for element that is not in tree.
                return false;
            }
            return (!entity.getBpelModel().inSync())
                    && entity instanceof BpelReferenceable;
        }
        return false;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#preDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void preDispatch( ChangeEvent event ) {
        if (event instanceof PropertyUpdateEvent ) {
            BpelEntity entity = event.getParent();
            //we will collect all BPEL elements that refers to updated element.
            BpelEntity root;
            if ( ((BpelEntityImpl)entity).isInTree() ) {
                root = entity.getBpelModel().getProcess();
            }
            else {
                root = Utils.getUnattachedRoot( entity );
            }
            collectReferenced( root , entity , event );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        if (event instanceof PropertyUpdateEvent ) {
            BpelEntity subject = event.getParent();
            Map<BpelEntity, Collection<Reference>> collection = getMap(event);
            for (Entry<BpelEntity, Collection<Reference>> entry : 
                collection.entrySet()) 
            {
                BpelEntity entity = entry.getKey();
                Collection<Reference> referenceCollection = entry.getValue();
                for (Reference reference : referenceCollection) {
                    ((BpelEntityImpl) entity).updateReference(reference,
                            (BpelReferenceable) subject);
                }
            }
            reset(event);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#reset(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public void reset( ChangeEvent event ) {
        Map<ChangeEvent,Map<BpelEntity,Collection<Reference>>> map = myMap.get();
        if ( map == null ){
            return;
        }
        Map<BpelEntity,Collection<Reference>> refMap = map.remove(event);
        // Fix for #84029
        if ( refMap != null ) {
            refMap.clear();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void collectReferenced(  BpelEntity next , BpelEntity ref,
            ChangeEvent event ) 
    {
        assert ref!=null;
        if ( next instanceof ReferenceCollection ) {
            Reference[] references = ((ReferenceCollection)next).getReferences();
            assert references!=null;
            for (Reference reference : references) {
                if ( !(reference instanceof BpelReference )){
                    continue;
                }
                if ( reference.references( (BpelReferenceable)ref )) {
                    addReference( next, reference, event);
                }
            }
        }
        List<BpelEntityImpl> impls = next.getChildren( BpelEntityImpl.class );
        for (BpelEntityImpl impl : impls) {
            if ( impl.getParent().isRegular(impl) ) { // collect only regular elements
                collectReferenced( impl , ref , event );
            }
        }
    }
    
    private void addReference( BpelEntity entity , Reference reference , 
            ChangeEvent event) 
    {
        Map<BpelEntity,Collection<Reference>> map = getMap(event);
        Collection<Reference> collection = map.get( entity );
        if ( collection == null ) {
            collection = new LinkedList<Reference>();
            map.put( entity , collection );
        }
        collection.add( reference );
    }

    private Map<BpelEntity,Collection<Reference>> getMap( ChangeEvent event ) {
        Map<ChangeEvent,Map<BpelEntity,Collection<Reference>>> map = myMap.get();
        if (map == null) {
            map = new WeakHashMap<ChangeEvent, Map<BpelEntity,
                Collection<Reference>>>();
            myMap.set(map);
        }
        Map<BpelEntity,Collection<Reference>> refMap = map.get(event);
        if ( refMap == null ) {
            refMap = new HashMap<BpelEntity, Collection<Reference>>();
            map.put(event, refMap);
        }
        return refMap;
    }
    
    private ThreadLocal<Map<ChangeEvent,
                            Map<BpelEntity,Collection<Reference>>>> myMap = 
                                new ThreadLocal<Map<ChangeEvent,
                                    Map<BpelEntity,Collection<Reference>>>>();
    // changed from ",Reference" to ",Collection<Reference>" due IZ79690 
}
