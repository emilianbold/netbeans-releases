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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.references.MappedReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.impl.references.BpelAttributesType;
import org.netbeans.modules.bpel.model.impl.services.InnerEventDispatcherAdapter;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * 
 * This service is developed for notifying clients of OM
 * about possible changes in references.
 * 
 * We need to find external model file  
 * for resolving all external references ( Schema, WSDL ). Implementation
 * of this search for corresponding "import" statements in BPEL file. 
 * One need to find all references that ( may be ) was resolved via
 * "import" when some
 * action is performed on "imoprt" ( it is added, changed, deleted ).  
 *     
 * This service find all references that depends from "import" (
 * if it is subject of event ) and notify listeners of model about changes.
 * 
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher.class)
public class ImportChangeHandlingService extends InnerEventDispatcherAdapter {
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        if ( event instanceof PropertyUpdateEvent ||
                event instanceof PropertyRemoveEvent )
        {
            if ( event.getParent()!=null && 
                    event.getParent().getElementType().equals(Import.class) )
            {
                BpelEntity entity = event.getParent();
                return (entity instanceof BpelEntityImpl)?
                        ((BpelEntityImpl)entity).isInTree():true;
            }
        }
        else if ( event instanceof EntityUpdateEvent ){
            return ((EntityUpdateEvent)event).getNewValue() instanceof Import ||
                ((EntityUpdateEvent)event).getOldValue() instanceof Import;
        }
        else if ( event instanceof EntityRemoveEvent ){
            return ((EntityRemoveEvent)event).getOldValue() instanceof Import;
        }
        else if ( event instanceof EntityInsertEvent ){
            return ((EntityInsertEvent)event).getValue() instanceof Import;
        }
        else if ( event instanceof ArrayUpdateEvent ){
            BpelEntity[] array =  ((ArrayUpdateEvent)event).getNewArray();
            
            if ( array!= null && array.length>0 ){
                return array[0].getElementType().equals( Import.class );
            }
            array = ((ArrayUpdateEvent)event).getOldArray();
            if ( array!=null && array.length>0 ){
                return array[0].getElementType().equals( Import.class );
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        if ( handlePropertyChange(event) ){
            return;
        }
        if ( handleInsertEvent( event )) {
            return;
        }
        if ( handleRemoveEvent( event )) {
            return;
        }
        if ( handleUpdateEvent( event )) {
            return;
        }
        handleArrayUpdateEvent( event );
    }

    private boolean handleInsertEvent( ChangeEvent event ) {
        boolean isInsert = event instanceof EntityInsertEvent;
        if ( ! isInsert ){
            return false;
        }
        BpelEntity entity = ((EntityInsertEvent)event).getValue();
        assert entity instanceof Import;
        String importType = ((Import)entity).getImportType();
        if ( Import.WSDL_IMPORT_TYPE.equals(importType)
                || Import.SCHEMA_IMPORT_TYPE.equals(importType))
        {
            String namespace = ((Import)entity).getNamespace();
            String location = ((Import)entity).getLocation();
            // if location is null - we will not be able to reresolve references. 
            if ( namespace != null &&  location!= null ){
                visitRefereces( (ProcessImpl)entity.getBpelModel().getProcess(), 
                        namespace, null );
            }
        }
        return isInsert;
    }
    
    private boolean handleRemoveEvent( ChangeEvent event ) {
        boolean isRemove = event instanceof EntityRemoveEvent;
        if ( ! isRemove ){
            return false;
        }
        BpelEntity entity = ((EntityRemoveEvent)event).getOldValue();
        assert entity instanceof Import;
        String importType = ((Import)entity).getImportType();
        if ( Import.WSDL_IMPORT_TYPE.equals(importType)
                || Import.SCHEMA_IMPORT_TYPE.equals(importType))
        {
            String namespace = ((Import)entity).getNamespace();
            String location = ((Import)entity).getLocation();
            // if location is null - we will not be able to reresolve references. 
            if ( namespace != null &&  location!= null ){
                visitRefereces( (ProcessImpl)entity.getBpelModel().getProcess(), 
                        namespace, null );
            }
        }
        return isRemove;
    }
    
    private boolean handleUpdateEvent( ChangeEvent event ) {
        boolean isUpdate = event instanceof EntityUpdateEvent;
        if ( ! isUpdate ){
            return false;
        }
        BpelEntity oldEntity = ((EntityUpdateEvent)event).getOldValue();
        BpelEntity newEntity = ((EntityUpdateEvent)event).getOldValue();
        assert oldEntity instanceof Import || newEntity instanceof Import;
        String oldImportType = ((Import)oldEntity).getImportType();
        String newImportType = ((Import)newEntity).getImportType();
        if ( Import.WSDL_IMPORT_TYPE.equals(oldImportType)
                || Import.SCHEMA_IMPORT_TYPE.equals(oldImportType)
                || Import.WSDL_IMPORT_TYPE.equals(newImportType)
                || Import.WSDL_IMPORT_TYPE.equals(newImportType) )
                
        {
            String oldNamespace = ((Import)oldEntity).getNamespace();
            String newNamespace = ((Import)newEntity).getNamespace();
            String oldLocation = ((Import)oldEntity).getLocation();
            String newLocation = ((Import)newEntity).getLocation();
            // if location is null - we will not be able to reresolve references. 
            if ( (oldNamespace != null || newNamespace!= null )&&  
                    (oldLocation!= null || newLocation!= null )  )
            {
                BpelEntity notNull = oldEntity==null? newEntity : oldEntity;
                visitRefereces( (ProcessImpl)notNull.getBpelModel().getProcess(), 
                        oldNamespace, newNamespace );
            }
        }
        return isUpdate;
    }
    
    private void handleArrayUpdateEvent( ChangeEvent event ) {
        boolean isUpdate = event instanceof ArrayUpdateEvent;
        if ( ! isUpdate ){
            return;
        }
        Set<String> affectedNS = new HashSet<String>();
        BpelEntity[] oldArray = ((ArrayUpdateEvent)event).getOldArray();
        handleChangesInArray(affectedNS, oldArray);
        BpelEntity[] newArray = ((ArrayUpdateEvent)event).getNewArray();
        handleChangesInArray(affectedNS, newArray);
    }

    private boolean handlePropertyChange( ChangeEvent event ) {
        boolean property = false;
        String name = null;
        String oldNamespace = null;
        String newNamespace = null;
        if ( event instanceof PropertyUpdateEvent ) {
            property = true;
            name = ((PropertyUpdateEvent)event).getName();
            oldNamespace = (String)((PropertyUpdateEvent)event).getOldValue();
            newNamespace = (String)((PropertyUpdateEvent)event).getNewValue();
        }
        else if ( event instanceof PropertyRemoveEvent){
            property = true;
            name = ((PropertyRemoveEvent)event).getName();
            oldNamespace = (String)((PropertyRemoveEvent)event).getOldValue();
        }
        if ( !property ) {
            return false;
        }
        if ( !name.equals( Import.NAMESPACE) ){
                oldNamespace = event.getParent().getAttribute( 
                        BpelAttributes.NAMESPACE );
                newNamespace = oldNamespace;
        }
        
        // Now we have 'namespace' value and we should walk through tree
        // and find all references with the same namespace. For such references
        // we need to fire change event.
        
        visitRefereces( (ProcessImpl)event.getParent().getBpelModel().getProcess(), 
                oldNamespace , newNamespace );
        
        return property;
    }
    
    private  void visitRefereces( BpelEntityImpl entity , String oldNS, 
            String newNS )
    {
        if ( entity instanceof ReferenceCollection ){
            Set<Attribute> fired = new HashSet<Attribute>(); 
            Reference[] refs = ((ReferenceCollection)entity).getReferences();
            for (Reference reference : refs) {
                if ( reference instanceof MappedReference) {
                    Attribute attr = ((MappedReference) reference).getAttribute();
                    if ( fired.contains( attr )){
                        continue;
                    }
                    else {
                        fired.add( attr );
                    }
                }
                handleReference( entity , reference, oldNS , newNS );
            }
        }
        
        List<BpelEntityImpl> list = entity.getChildren( BpelEntityImpl.class );
        for (BpelEntityImpl child : list) {
            visitRefereces( child , oldNS, newNS );
        }
    }

    private void handleReference ( BpelEntityImpl entity, Reference reference , 
            String oldNS, String newNS ) 
    {
        if ( reference instanceof BpelAttributesType 
                && reference instanceof NamedComponentReference )
        { 
            /* 
             * Here we care only about QName attribute types.
             * Responsibility for refereshing for other types references 
             * is on clients of OM.   
             */ 
            NamedComponentReference ref = 
                (NamedComponentReference)reference;
            BpelAttributesType.AttrType type = 
                ((BpelAttributesType)reference).getAttributeType();
            if ( type.equals( BpelAttributesType.AttrType.QNAME )){
                if ( oldNS != null && oldNS.equals( ref.getEffectiveNamespace())){
                    entity.getAttributeAccess().fireUpdateReference( ref );
                    return;
                }
                if ( newNS != null && newNS.equals( ref.getEffectiveNamespace())){
                    entity.getAttributeAccess().fireUpdateReference( ref );
                    return;
                }
            }
        }
    }
    
    private void handleChangesInArray( Set<String> affectedNS, 
            BpelEntity[] newArray ) 
    {
        for (BpelEntity entity : newArray) {
            assert entity instanceof Import;
            String importType = ((Import)entity).getImportType();
            if ( Import.WSDL_IMPORT_TYPE.equals(importType)
                    || Import.SCHEMA_IMPORT_TYPE.equals(importType))
            {
                String namespace = ((Import)entity).getNamespace();
                String location = ((Import)entity).getLocation();
                // if location is null - we will not be able to reresolve references. 
                if ( namespace != null &&  location!= null ){
                    if ( affectedNS.contains( namespace) ){
                        continue;
                    }
                    else {
                        affectedNS.add( namespace );
                    }
                    visitRefereces( (ProcessImpl)entity.getBpelModel().getProcess(), 
                            namespace, null );
                }
            }
        }
    }
}
