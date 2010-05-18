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

/**
 *
 */
package org.netbeans.modules.bpel.model.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class PickImpl extends ActivityImpl implements Pick {

    PickImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    PickImpl( BpelBuilderImpl builder) {
        super(builder, BpelElements.PICK.getName() );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#getOnMessages()
     */
    public OnMessage[] getOnMessages() {
        readLock();
        try {
            List<OnMessage> list = getChildren(OnMessage.class);
            return list.toArray(new OnMessage[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#setOnMessages(org.netbeans.modules.soa.model.bpel20.api.OnMessage[])
     */
    public void setOnMessages( OnMessage[] messages ) {
        setArrayBefore(messages, OnMessage.class, BpelTypesEnum.ON_ALARM_PICK);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#getOnMessage(int)
     */
    public OnMessage getOnMessage( int i ) {
        return getChild(OnMessage.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#setOnMessage(org.netbeans.modules.soa.model.bpel20.api.OnMessage, int)
     */
    public void setOnMessage( OnMessage message, int i ) {
        setChildAtIndex(message, OnMessage.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#insertOnMessage(org.netbeans.modules.soa.model.bpel20.api.OnMessage, int)
     */
    public void insertOnMessage( OnMessage message, int i ) {
        insertAtIndex(message, OnMessage.class, i, BpelTypesEnum.ON_ALARM_PICK );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#addOnMessage(org.netbeans.modules.soa.model.bpel20.api.OnMessage, int)
     */
    public void addOnMessage( OnMessage message ) {
        addChildBefore(message, OnMessage.class, BpelTypesEnum.ON_ALARM_PICK);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#removeOnMessage(int)
     */
    public void removeOnMessage( int i ) {
        removeChild(OnMessage.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#getOnAlarms()
     */
    public OnAlarmPick[] getOnAlarms() {
        readLock();
        try {
            List<OnAlarmPick> list = getChildren(OnAlarmPick.class);
            return list.toArray(new OnAlarmPick[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#setOnAlarms(org.netbeans.modules.soa.model.bpel20.api.OnAlarmPick[])
     */
    public void setOnAlarms( OnAlarmPick[] alarms ) {
        setArrayBefore(alarms, OnAlarmPick.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#getOnAlarm(int)
     */
    public OnAlarmPick getOnAlarm( int i ) {
        return getChild(OnAlarmPick.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#setOnAlarm(org.netbeans.modules.soa.model.bpel20.api.OnAlarmPick, int)
     */
    public void setOnAlarm( OnAlarmPick alarm, int i ) {
        setChildAtIndex(alarm, OnAlarmPick.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#insertOnAlarm(org.netbeans.modules.soa.model.bpel20.api.OnAlarmPick, int)
     */
    public void insertOnAlarm( OnAlarmPick alarm, int i ) {
        insertAtIndex(alarm, OnAlarmPick.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#addOnAlarm(org.netbeans.modules.soa.model.bpel20.api.OnAlarmPick)
     */
    public void addOnAlarm( OnAlarmPick alarm ) {
        addChildBefore(alarm, OnAlarmPick.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#removeOnAlarm(int)
     */
    public void removeOnAlarm( int i ) {
        removeChild(OnAlarmPick.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#sizeOfOnMessages()
     */
    public int sizeOfOnMessages() {
        readLock();
        try {
            return getChildren(OnMessage.class).size();
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Pick#sizeOfOnAlarms()
     */
    public int sizeOfOnAlarms() {
        readLock();
        try {
            return getChildren(OnAlarmPick.class).size();
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Pick.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CreateInstanceActivity#getCreateInstance()
     */
    public TBoolean getCreateInstance() {
        return getBooleanAttribute( BpelAttributes.CREATE_INSTANCE);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CreateInstanceActivity#setCreateInstance(org.netbeans.modules.soa.model.bpel20.api.support.TBoolean)
     */
    public void setCreateInstance( TBoolean value ) {
        setBpelAttribute( BpelAttributes.CREATE_INSTANCE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.CreateInstanceActivity#removeCreateInstance()
     */
    public void removeCreateInstance() {
        removeAttribute( BpelAttributes.CREATE_INSTANCE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ){
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ActivityImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ){
        if ( BpelElements.ON_MESSAGE.getName().equals( element.getLocalName() ) ){
            return new OnMessageImpl( getModel() , element );
        }
        else if ( BpelElements.ON_ALARM_PICK.getName().equals( 
                element.getLocalName() ) )
        {
            return new OnAlarmPickImpl( getModel() , element );
        }
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 1];
            System.arraycopy( attr , 0 , ret , 1 , attr.length );
            ret[ 0 ] = BpelAttributes.CREATE_INSTANCE;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
}
