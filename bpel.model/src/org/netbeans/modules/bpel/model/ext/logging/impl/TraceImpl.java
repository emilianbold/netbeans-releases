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

package org.netbeans.modules.bpel.model.ext.logging.impl;

import org.netbeans.modules.bpel.model.ext.logging.xam.LoggingTypesEnum;
import java.util.List;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.support.EntityUpdater;
import org.netbeans.modules.bpel.model.ext.logging.api.Alert;
import org.netbeans.modules.bpel.model.ext.logging.api.Log;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;
import org.netbeans.modules.bpel.model.ext.logging.xam.LoggingElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class TraceImpl extends LoggingEntityImpl implements Trace {

    TraceImpl(LoggingEntityFactory factory, BpelModelImpl model, Element e ) {
        super(factory, model, e);
    }

    TraceImpl(LoggingEntityFactory factory, BpelBuilderImpl builder ) {
        super(factory, builder, LoggingElements.TRACE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel2020.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Trace.class;
    }

    public EntityUpdater getEntityUpdater() {
        return TraceEntityUpdater.getInstance();
    }

    protected Attribute[] getDomainAttributes() {
        return new Attribute[0];
    }
    
    private static class TraceEntityUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE =
                new TraceEntityUpdater();
        
        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private TraceEntityUpdater() {
            
        }
        
        public void update(BpelEntity target, ExtensionEntity child, Operation operation) {
            if (target instanceof ExtensibleElements) {
                ExtensibleElements ee = (ExtensibleElements)target;
                switch (operation) {
                case ADD:
                    ee.addExtensionEntity(Trace.class, (Trace)child);
                    break;
                case REMOVE:
                    ee.remove(child);
                    break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index, Operation operation) {
            if (target instanceof ExtensibleElements) {
                ExtensibleElements ee = (ExtensibleElements)target;
                switch (operation) {
                case ADD:
                    ee.addExtensionEntity(Trace.class, (Trace)child);
                    break;
                case REMOVE:
                    ee.remove(child);
                    break;
                }
            }
        }
        
    }

    public Log[] getLogs() {
        readLock();
        try {
            List<Log> list = getChildren( Log.class );
            return list.toArray( new Log[ list.size()] );
        }
        finally {
            readUnlock();
        }
    }

    public Log getLog(int i) {
        return getChild( Log.class , i );
    }

    public void removeLog(int i) {
        removeChild( Log.class , i );
    }

    public void setLogs(Log[] logs) {
        setArrayBefore( logs , Log.class , LoggingTypesEnum.ALERT);
    }

    public void setLog(Log log, int i) {
        setChildAtIndex( log, Log.class , i ); 
    }

    public void addLog(Log log) {
        addChildBefore( log , Log.class , LoggingTypesEnum.ALERT );
    }

    public void insertLog(Log log, int i) {
        insertAtIndex( log , Log.class, i , LoggingTypesEnum.ALERT );
    }

    public Alert[] getAlerts() {
        readLock();
        try {
            List<Alert> list = getChildren( Alert.class );
            return list.toArray( new Alert[ list.size() ] );
        }
        finally {
            readUnlock();
        }
    }

    public Alert getAlert(int i) {
        return getChild(Alert.class, i);
    }

    public void removeAlert(int i) {
        removeChild(Alert.class, i);
    }

    public void setAlerts(Alert[] alerts) {
        setArrayBefore(alerts, Alert.class);
    }
    
    public void setAlert(Alert alert, int i) {
        setChildAtIndex(alert, Alert.class, i);
    }

    public void addAlert(Alert alert) {
        addChildBefore(alert, Alert.class);
    }

    public void insertAlert(Alert alert, int i) {
        insertAtIndex(alert, Alert.class, i);
    }

    public int sizeOfLogs() {
        readLock();
        try {
            return getChildren( Log.class ).size();
        }
        finally {
            readUnlock();
        }
    }

    public int sizeOfAlerts() {
        readLock();
        try {
            return getChildren( Alert.class ).size();
        }
        finally {
            readUnlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if ( LoggingElements.LOG.getName().equals(element.getLocalName())) {
            return new LogImpl(getFactory(), getModel(), element);
        }
        else if (LoggingElements.ALERT.getName().
                equals(element.getLocalName())) 
        {
            return new AlertImpl(getFactory(), getModel(), element);
        }
        return null;
    }
    
}
