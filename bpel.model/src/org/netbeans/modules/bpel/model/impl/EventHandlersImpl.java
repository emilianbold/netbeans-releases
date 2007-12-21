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

import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterImport;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class EventHandlersImpl extends ExtensibleElementsImpl implements
        EventHandlers, AfterImport, AfterSources
{

    EventHandlersImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    EventHandlersImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.EVENT_HANDLERS.getName() );
    }

 
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#getOnAlarm(int)
     */
    public OnAlarmEvent getOnAlarm( int i ) {
        return getChild(OnAlarmEvent.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#removeOnAlarm(int)
     */
    public void removeOnAlarm( int i ) {
        removeChild(OnAlarmEvent.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#setOnAlarms(org.netbeans.modules.soa.model.bpel20.api.OnAlarm[])
     */
    public void setOnAlarms( OnAlarmEvent[] alarm ) {
        setArrayBefore(alarm, OnAlarmEvent.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#setOnAlarm(org.netbeans.modules.soa.model.bpel20.api.OnAlarm,
     *      int)
     */
    public void setOnAlarm( OnAlarmEvent alarm, int i ) {
        setChildAtIndex(alarm, OnAlarmEvent.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#addOnAlarm(org.netbeans.modules.soa.model.bpel20.api.OnAlarm)
     */
    public void addOnAlarm( OnAlarmEvent alarm ) {
        addChildBefore(alarm, OnAlarmEvent.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#insertOnAlarm(org.netbeans.modules.soa.model.bpel20.api.OnAlarm,
     *      int)
     */
    public void insertOnAlarm( OnAlarmEvent alarm, int i ) {
        insertAtIndex(alarm, OnAlarmEvent.class, i);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#getOnEvents()
     */
    public OnEvent[] getOnEvents() {
        readLock();
        try {
            List<OnEvent> list = getChildren( OnEvent.class );
            return list.toArray( new OnEvent[ list.size()] );
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#getOnEvent(int)
     */
    public OnEvent getOnEvent( int i ) {
        return getChild( OnEvent.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#removeOnEvent(int)
     */
    public void removeOnEvent( int i ) {
        removeChild( OnEvent.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#setOnEvent(org.netbeans.modules.soa.model.bpel20.api.OnEvent[])
     */
    public void setOnEvent( OnEvent[] events ) {
        setArrayBefore( events , OnEvent.class , BpelTypesEnum.ON_ALARM_EVENT );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#setOnEvent(org.netbeans.modules.soa.model.bpel20.api.OnEvent, int)
     */
    public void setOnEvent( OnEvent event, int i ) {
        setChildAtIndex( event, OnEvent.class , i ); 
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#addOnEvent(org.netbeans.modules.soa.model.bpel20.api.OnEvent)
     */
    public void addOnEvent( OnEvent event ) {
        addChildBefore( event , OnEvent.class , BpelTypesEnum.ON_ALARM_EVENT );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#insertOnEvent(org.netbeans.modules.soa.model.bpel20.api.OnEvent, int)
     */
    public void insertOnEvent( OnEvent event, int i ) {
        insertAtIndex( event , OnEvent.class, i , BpelTypesEnum.ON_ALARM_EVENT );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#getOnAlarms()
     */
    public OnAlarmEvent[] getOnAlarms() {
        readLock();
        try {
            List<OnAlarmEvent> list = getChildren( OnAlarmEvent.class );
            return list.toArray( new OnAlarmEvent[ list.size() ] );
        }
        finally {
            readUnlock();
        }
    }   


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#sizeOfOnEvents()
     */
    public int sizeOfOnEvents() {
        readLock();
        try {
            return getChildren( OnEvent.class ).size();
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.EventHandlers#sizeOfOnAlarms()
     */
    public int sizeOfOnAlarms() {
        readLock();
        try {
            return getChildren( OnAlarmEvent.class ).size();
        }
        finally {
            readUnlock();
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return EventHandlers.class;
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.ON_EVENT.getName().equals(element.getLocalName())) {
            return new OnEventImpl(getModel(), element);
        }
        else if (BpelElements.ON_ALARM_EVENT.getName().
                equals(element.getLocalName())) 
        {
            return new OnAlarmEventImpl(getModel(), element);
        }
        return super.create( element );
    }



}
