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
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypes;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author ads
 */
public abstract class BpelContainerImpl extends BpelEntityImpl implements BpelContainer {

    protected BpelContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    protected BpelContainerImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
    }
    
    protected BpelContainerImpl( BpelBuilderImpl builder, BpelElements elem ) {
        super( builder , elem );
    }

    @Override
    public boolean canPaste(Component child) {
        if ( !Utils.checkPasteCompensate( this , child ) ){
            return false;
        }
        return true;
    }
    
    /**
     * This method should be implemented by each container . Container should
     * recognize in <code>element</code> its child and create Bpel element
     * respectively. Warning! Here exist some possibility for error. Each
     * container should recognize only those children that could be inside it as
     * specification said. One cannot create one static mehtod for recognition
     * all BPEL elements because f.e. container VariableContainer could have
     * ONLY Variable inside it. It cannot have "flow" tag inside it ( it can
     * have "flow" but this is just extention element that will not be treated
     * as BPEL element ).
     * 
     * @param element
     * @return
     */
    protected abstract BpelEntity create( Element element );
       
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelContainer#indexOf(java.lang.Class, T)
     */
    public <T extends BpelEntity> int indexOf( Class<T> type, T entity ) {
        readLock();
        try {
            List<T> list = getChildren( type );
            return list.indexOf( entity );
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelContainer#remove(org.netbeans.modules.soa.model.bpel20.api.BpelEntity)
     */
    public <T extends BpelEntity>  void remove( T entity ) {
        writeLock();
        try {
            assert entity != null;
            checkDeleted();

            Class<? extends BpelEntity> clazz = getChildType( entity );

            List<? extends BpelEntity> list = getChildren(clazz);
            int i = list.indexOf(entity);

            if (i>-1) {
                BpelEntity nonRegular = null;
                if ( getMultiplicity( entity ) == Multiplicity.SINGLE && i==0) {
                    i=-1;
                    if ( list.size() >1 ) { // there non-regular elements exist
                       nonRegular = list.get( 1 );
                    }
                }
                ChangeEvent event = null;
                if ( nonRegular == null ) {
                    event = preEntityRemove(entity, i);
                }
                else {
                    event = preEntityUpdate( entity , nonRegular , i );
                }
                    
                boolean isRegular = isRegular( entity);
                removeChild( entity);
                postGlobalEvent( event ,  isRegular );
            }
            else {
                throw new IllegalArgumentException("Specified entity is not"// NOI18N
                        + " found in this container"); // NOI18N
            }
        }
        finally {
            writeUnlock();
        }
    }
    
    @Override
    public <T extends BpelEntity> List<T> getChildren(Class<T> type )
    {
        readLock();
        try {
            checkDeleted();
            return super.getChildren(type);
        }
        finally {
            readUnlock();
        }

    }

    /**
     * This method should be overriden in concrete implementation
     * if <code>entity</code> can present in this container
     * with not unbounded multiplicity. 
     */
    protected Multiplicity getMultiplicity( BpelEntity entity ) {
        return Multiplicity.UNBOUNDED;
    }

    /**
     * 
     * This method possibly needs to be overriden by some containers...
     * F.e. it SHOULD to be overriden ActivityHolder and CompositeActivity
     * because in these cases those containers contains elements as a whole
     * they don't distinguish them by its own types ( Empty.class , etc. ).
     * They need to count index for such children in common array ,not
     * personal array. 
     */
    protected <T extends BpelEntity> Class<? extends BpelEntity> getChildType( 
            T entity ) 
    {
        return entity.getElementType();
    }
    

    @Override
    protected <T extends BpelEntity> T getChild( Class<T> type ) {
        readLock();
        try {
            checkDeleted();
            return super.getChild(type);
        }
        finally {
            readUnlock();
        }
    }

    protected <T extends BpelEntity> T getChild( Class<T> type, int i ) {
        readLock();
        try {
            checkDeleted();
            return getChildren(type).get(i);
        }
        finally {
            readUnlock();
        }
    }

    /**
     * This method removes all old elements in array and set new list of
     * elements with specified <code>clazz</code> type. This method add
     * <code>entities</code> array right AFTER elements with type in
     * <code>types</code> array.
     */
    @SuppressWarnings("unchecked")
    protected <T extends BpelEntity> void setArrayAfter( T[] entities,
            Class<T> clazz, BpelTypes... types )
    {
        writeLock();
        try {
            assert entities != null;
            List<? extends BpelEntity> list = getChildren(clazz);
            ArrayUpdateEvent<BpelEntity> event = preArrayUpdate(clazz, list,
                    entities);
            if (entities.length == 0) {
                for (BpelEntity component : list) {
                    BpelEntityImpl impl = (BpelEntityImpl) component;
                    removeChild(impl.getEntityName(), component);
                }
            }
            else {
                // we remove all previous elements from children list and
                // set first child
                setChildAfter(clazz, ((BpelEntityImpl) entities[0])
                        .getEntityName(), entities[0], types);
                // then we consequently add all other elements from array.
                if (entities.length > 1) {
                    for (int i = 1; i < entities.length; i++) {
                        BpelEntityImpl entity = (BpelEntityImpl) entities[i];
                        addAfter(entity.getEntityName(), entity, Utils.of(types));
                    }
                }
            }
            postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }

    /**
     * This method removes all old elements in array and set new list of
     * elements with specified <code>clazz</code> type. This method add
     * <code>entities</code> array right BEFORE elements with type in
     * <code>types</code> array.
     */
    @SuppressWarnings("unchecked")
    protected <T extends BpelEntity> void setArrayBefore( T[] entities,
            Class<T> clazz, BpelTypes... types )
    {
        writeLock();
        try {
            assert entities != null;
            List<T> list = getChildren(clazz);
            ArrayUpdateEvent<BpelEntity> event = preArrayUpdate(clazz, list,
                    entities);
            if (entities.length == 0) {
                for (BpelEntity component : getChildren(clazz)) {
                    BpelEntityImpl impl = (BpelEntityImpl) component;
                    removeChild(impl.getEntityName(), component);
                }
            }
            else {
                // we remove all previous elements from children list and
                // set first child
                setChildBefore(clazz, ((BpelEntityImpl) entities[0])
                        .getEntityName(), entities[0], types);
                // then we consequently add all other elements from array.
                if (entities.length > 1) {
                    for (int i = 1; i < entities.length; i++) {
                        BpelEntityImpl entity = (BpelEntityImpl) entities[i];
                        addBefore(entity.getEntityName(), entity, Utils.of(types));
                    }
                }
            }
            postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }


    protected <T extends BpelEntity> void addChildBefore( T entity,
            Class<T> clazz, BpelTypes... types )
    {
        writeLock();
        try {
            checkDeleted();
            List<T> list = getChildren(clazz);
            int i = list.size();
            
            EntityInsertEvent<? extends BpelEntity> event = 
                preEntityInsert(entity, i);
            addBefore(((BpelEntityImpl) entity).getEntityName(),
                    entity, Utils.of(types));
            postGlobalEvent(event , isRegular( entity) );
        }
        finally {
            writeUnlock();
        }
    }
    
    protected <T extends BpelEntity> void addChildAfter( T entity,
            Class<T> clazz, BpelTypes... types )
    {
        writeLock();
        try {
            List<T> list = getChildren(clazz);
            int i = list.size();
            addEntityAfter( entity , i , types);
        }
        finally {
            writeUnlock();
        }
    }

    /**
     * This method adds <code>entity</code> element to the end of children
     * list in this container. This is useful method when we have container with
     * only one element types. Then we just need to append element.
     * 
     * @param entity
     * @param clazz
     */
    protected <T extends BpelEntity> void addChild( T entity, Class<T> clazz ) {
        writeLock();
        try {
            checkDeleted();
            List<T> list = getChildren(clazz);
            int i = list.size();
            EntityInsertEvent<T> event = preEntityInsert(entity, i);
            appendChild(((BpelEntityImpl) entity).getEntityName(),entity);
            postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }

    protected <T extends BpelEntity> void insertAtIndex( T entity,
            Class<T> clazz, int index , BpelTypes... types )
    {
        writeLock();
        try {
            checkDeleted();
            if ((index < 0) || (getChildren(clazz).size() < index)) {
                throw new ArrayIndexOutOfBoundsException(
                        "Index is out of bound, " + "index is :" + index// NOI18N
                                + ", size of children with needed type : "// NOI18N
                                + getChildren(clazz).size());// NOI18N
            }
            if ( index == getChildren(clazz).size() ){
                addChildBefore( entity , clazz , types );
                return;
            }
            BpelEntity previous = null;
            if ( getMultiplicity( entity) == Multiplicity.SINGLE ){
                previous = getChild( clazz );
            }
            
            ChangeEvent event;
            if ( previous == null ) {
                event = preEntityInsert(entity, index);
            }
            else {
                event = preEntityUpdate( previous , entity ,  -1 );
            }
            insertAtIndex(((BpelEntityImpl) entity).getEntityName(),
                    entity, index, clazz);
            postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }
    
    /**
     * This method is used by SyncUpdateVisitor for inserting
     * element with given index.
     * Index is absolute index in common children list for this container.
     * We need to recalculate this index respectively 
     * <code>clazz</code> type children list.  
     * @return false if inserting was not performed. So 
     * There should be "add" performed instead of insert.
     * We don't know how to perform "add" because of ordering issue
     * ( we possibly need to know class type for object that follow 
     * clazz type ). 
     */
    protected <T extends BpelEntity> boolean insertAtAbsoluteIndex( T entity,
            Class<T> clazz, int index)
    {
        writeLock();
        try {
            checkDeleted();
            if ( index <0 ){
                // in this case we need to add .
                return false;
            }
            List<T> list = getChildren( clazz );
            if ( list.size() == 0 ){
                // there was not found any children with "clazz" type. So we need to add this entity.
                return false;
            }
            int indexForInsert = 0; 
            for (BpelEntity child : list) {
                BpelEntityImpl impl = (BpelEntityImpl) child; 
                int ind = getModel().getAccess().getElementIndexOf( getPeer() , 
                        impl.getPeer() );
                if ( ind >= index ){
                    break;
                }
                indexForInsert++;
            }
            if ( indexForInsert >= list.size() ){
                // index is more then quantity of elements in the children list of specified type
                return false;
            }
            insertAtIndex( entity , clazz , indexForInsert );
            return true;
        }
        finally {
            writeUnlock();
        }
    }
    
    protected <T extends BpelEntity> void insertAtIndexAfter( T entity,
            Class<T> clazz, int index , BpelTypes... types )
    {
        writeLock();
        try {
            checkDeleted();
            if ((index < 0) || (getChildren(clazz).size() < index)) {
                throw new ArrayIndexOutOfBoundsException(
                        "Index is out of bound, " + "index is :" + index// NOI18N
                                + ", size of children with needed type : "// NOI18N
                                + getChildren(clazz).size());// NOI18N
            }
            if ( index == getChildren(clazz).size() ){
                addChildAfter( entity , clazz , types );
                return;
            }
            EntityInsertEvent<T> event = preEntityInsert(entity, index);
            insertAtIndex(((BpelEntityImpl) entity).getEntityName(),
                    entity, index, clazz);
            postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }


    protected <T extends BpelEntity> void setChildAtIndex( T entity,
            Class<T> clazz, int index )
    {
        writeLock();
        try {
            List<T> list = getChildren(clazz);
            BpelEntity component = list.get(index);
            String name = ((BpelEntityImpl) component).getEntityName();

            EntityUpdateEvent<T> event = preEntityUpdate(clazz.cast(component),
                    entity, index);
            removeChild(name, component);
            insertAtIndex(entity, clazz, index);

            postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }

    /**
     * This method will be used consistently when one need to insert element as
     * child in some container. We need to keep some order for children in many
     * containers and will always use <code>types</code> as "next" elements
     * even if container doesn't have any children in <code>types</code>.
     * Element will be added BEFORE elements in types array.
     */
    protected <T extends BpelEntity> void setChild( T newEl,
            Class<T> classType, BpelTypes... types )
    {
        writeLock();
        try {
            T old = getChild(classType);
            //
            if (newEl == old) {
                // See issue #129274                
                // If the old and the new values are the same objects: 
                //  It prevents some problems. When the old child BPEL entity 
                //  is replaced with a new one, it is also deleted from the BPEL model.
                //  It the old is just the same as the new, then the new turned out 
                //  deleted as well. 
                return;
            }
            //
            EntityUpdateEvent<T> event = preEntityUpdate(old, newEl, -1);

            setChildBefore(classType, ((BpelEntityImpl) newEl).getEntityName(),
                    newEl, types);

            postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }

    }

    protected void removeChild( Class<? extends BpelEntity> classType ) {
        writeLock();
        try {
            checkDeleted();
            BpelEntity component = getChild(classType);
            BpelEntity nonRegular = null;
            
            if ( component == null ){
                return;
            }
            
            if ( getMultiplicity( component ) == Multiplicity.SINGLE ) {
                // actully this should be always for this method
                List<? extends BpelEntity> list = getChildren( classType );
                if ( list.size() >1 ) { // there non-regular elements exist
                   nonRegular = list.get( 1 );
                }
            }

            ChangeEvent event;
            if ( nonRegular == null ) {
                event = preEntityRemove(component, -1);
            }
            else {
                event = preEntityUpdate( component , nonRegular , -1 );
            }

            removeChild(component);

            postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }

    protected void removeChild( Class<? extends BpelEntity> classType, int i ) {
        writeLock();
        try {
            checkDeleted();
            BpelEntity component = getChildren(classType).get(i);
            EntityRemoveEvent<? extends BpelEntity> event = preEntityRemove(
                    component, -1);
            removeChild(component);

            postGlobalEvent(event);
        }
        finally {
            writeUnlock();
        }
    }

    @Override
    protected void populateChildren( List<BpelEntity> children )
    {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    BpelEntity component = getModel().createComponent( this, 
                            (Element) n);
                    if (component != null) {
                        children.add(component);
                    }
                }
            }
        }

    }
    
    /**
     * This method is used for determening regularity of element.
     * Regular element means "correct" element from API point of view.
     * There is possibility for element to be OM entity but is not 
     * valid element from API OM point of view. 
     * F.e. many activities in Process are not allowed. So only 
     * first activity is correct. All others are valid OM entity but
     * not correctly located. User can always work with
     * such non-regular entities from source editor.
     * But design view based on OM will not allow to user 
     * work with such elements. This method helps determine
     * such elements and it will be used for distinguishing
     * elements that could be subject of events in OM and 
     * non-regular elements that will be not subjects of events.
     *        
     * @return Is <code>entity</code> regular.
     */
    final boolean isRegular( BpelEntity entity ) {
        assert entity!= null;
        Multiplicity mult = getMultiplicity( entity );
        if ( mult == Multiplicity.UNBOUNDED ){
            return true;
        }
        if ( mult == Multiplicity.SINGLE ){
            BpelEntity child = getChild( getChildType( entity) );
            return entity.equals( child );
        }
        assert false;
        return true;
    }

    class CopyKey {
    };
    
    protected enum Multiplicity {
        SINGLE,
        UNBOUNDED
    }


    @SuppressWarnings("unchecked")
    private <T extends BpelEntity> void setChildAfter( Class<T> classType,
            String propertyName, T newEl, BpelTypes... types )
    {
        writeLock();
        try {
            Collection<Class<? extends BpelEntity>> classes = Collections.EMPTY_LIST;
            if (types.length != 0) {
                classes = Utils.of(types);
            }
            super.setChildAfter(classType, propertyName, newEl, classes);
        }
        finally {
            writeUnlock();
        }
    }

    private <T extends BpelEntity> void setChildBefore( Class<T> classType,
            String propertyName, T newEl, BpelTypes... types )
    {
        assert newEl != null;
        writeLock();
        try {
            Collection<Class<? extends BpelEntity>> classes = Utils
                    .of( types );
            super.setChildBefore(classType, propertyName, newEl, classes);
        }
        finally {
            writeUnlock();
        }
    }


    private void removeChild( BpelEntity component ) {
        if (component == null) {
            return;
        }
        String name = ((BpelEntityImpl) component).getEntityName();
        removeChild(name, component);
    }
    
    @SuppressWarnings("unchecked")
    private <T extends BpelEntity> void addEntityAfter( T entity, int i , 
            BpelTypes... types) 
    {
        checkDeleted();
        EntityInsertEvent<T> event = preEntityInsert(entity, i);
        super.addAfter(((BpelEntityImpl) entity).getEntityName(),
                entity, Utils.of(types) );
        postGlobalEvent(event , isRegular( entity ) );
    }

    private <T extends BpelEntity> EntityRemoveEvent<T> preEntityRemove(
            T entity, int indx )
    {
        EntityRemoveEvent<T> event = new EntityRemoveEvent<T>(getModel()
                .getSource(), this, ((BpelEntityImpl) entity).getEntityName(),
                entity, indx);
        try {
            getModel().preInnerEventNotify(event);
        }
        catch (VetoException e) {
            assert false;
        }
        return event;
    }

    private <T extends BpelEntity> EntityInsertEvent<T> preEntityInsert(
            T entity, int i )
    {
        EntityInsertEvent<T> event = new EntityInsertEvent<T>(getModel()
                .getSource(), this, ((BpelEntityImpl) entity).getEntityName(),
                entity, i);
        try {
            getModel().preInnerEventNotify(event);
        }
        catch (VetoException e) {
            assert false;
        }
        if ( isInTree() ){
            ((BpelEntityImpl)entity).setInTreeRecursively();
        }
        return event;
    }

    private <T extends BpelEntity> EntityUpdateEvent<T> preEntityUpdate( T old,
            T entity, int i )
    {
        EntityUpdateEvent<T> event = new EntityUpdateEvent<T>(getModel()
                .getSource(), this, ((BpelEntityImpl) entity).getEntityName(),
                old, entity, i);
        try {
            getModel().preInnerEventNotify(event);
        }
        catch (VetoException e) {
            assert false;
        }
        if ( isInTree() ){
            ((BpelEntityImpl)entity).setInTreeRecursively();
        }
        return event;
    }

    private <T extends BpelEntity> ArrayUpdateEvent<BpelEntity> preArrayUpdate(
            Class<T> clazz, List<? extends BpelEntity> oldChildren,
            T[] newChildren )
    {
        checkDeleted();
        /*
         * We allow to set as child in new newChildren array any children that
         * already was in this container ( and ONLY this ). So we need to
         * perform copy for any such child ( because we cannot insert the same
         * entity one more time event after deletion ). And set instead of
         * original entity in newChildren array it copy. The enity is identified
         * by its UID. So we will assign to copied entity the same uid. Inner
         * visitor will need to set corresponding id's for children of that
         * entity if any. This method can be called for reordering elements in
         * array f.e.
         */
        BpelEntity[] old = new BpelEntity[oldChildren.size()];
        int j = 0;
        for (BpelEntity component : oldChildren) {
            old[j] = component;
            for (int i = 0; i < newChildren.length; i++) {
                if (component == newChildren[i]) {
                    ((BpelEntityImpl) newChildren[i]).checkDeleted();
                    BpelEntity child = ((BpelEntityImpl) newChildren[i])
                            .copy(this);
                    component.setCookie(CopyKey.class, child);
                    newChildren[i] = clazz.cast(child);
                    break;
                }
            }
            j++;
        }

        // now newChildren array don't have children that already in tree
        // and we just set up new array.

        ArrayUpdateEvent<BpelEntity> event = new ArrayUpdateEvent<BpelEntity>(
                getModel().getSource(), this, null, old, newChildren);

        try {
            getModel().preInnerEventNotify(event);
        }
        catch (VetoException e) {
            assert false;
        }
        if (isInTree()) {
            for (BpelEntity child : newChildren) {
                ((BpelEntityImpl)child).setInTreeRecursively();
            }
        }
        return event;
    }
}
