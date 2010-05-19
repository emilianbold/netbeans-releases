/*
 * AlertImpl.java
 * 
 * Created on Sep 22, 2007, 10:21:17 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.model.ext.logging.impl;

import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.support.EntityUpdater;
import org.netbeans.modules.bpel.model.ext.logging.api.Alert;
import org.netbeans.modules.bpel.model.ext.logging.api.AlertLevel;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;
import org.netbeans.modules.bpel.model.ext.logging.api.Location;
import org.netbeans.modules.bpel.model.ext.logging.xam.LoggingAttributes;
import org.netbeans.modules.bpel.model.ext.logging.xam.LoggingElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.bpel.model.impl.FromImpl;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author zgursky
 */
public class AlertImpl extends LoggingEntityImpl implements Alert {
    AlertImpl(LoggingEntityFactory factory, BpelModelImpl model, Element e ) {
        super(factory, model, e);
    }

    AlertImpl(LoggingEntityFactory factory, BpelBuilderImpl builder ) {
        super(factory, builder, LoggingElements.ALERT);
    }

    protected BpelEntity create(Element element) {
        if ( BpelElements.FROM.getName().equals(element.getLocalName())) {
            return new FromImpl(getModel(), element);
        } else {
            return null;
        }
    }

    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] ret = new Attribute[] {
                LoggingAttributes.LOCATION,
                LoggingAttributes.ALERT_LEVEL
            };
            myAttributes.compareAndSet( null ,  ret);
        }
        return myAttributes.get();
    }

    public Class<? extends BpelEntity> getElementType() {
        return Alert.class;
    }

    public EntityUpdater getEntityUpdater() {
        return AlertEntityUpdater.getInstance();
    }

    public From getFrom() {
        return getChild(From.class);
    }

    public void setFrom(From from) {
        setChild(from, From.class);
    }

    public AlertLevel getLevel() {
        readLock();
        try {
            String str = getAttribute(LoggingAttributes.ALERT_LEVEL);
            return AlertLevel.forString(str);
        }
        finally {
            readUnlock();
        }
    }

    public void setLevel(AlertLevel level) {
        setBpelAttribute(LoggingAttributes.ALERT_LEVEL, level);
    }
    
    public void removeLevel() {
        removeAttribute(LoggingAttributes.ALERT_LEVEL);
    }

    public Location getLocation() {
        readLock();
        try {
            String str = getAttribute(LoggingAttributes.LOCATION);
            return Location.forString(str);
        }
        finally {
            readUnlock();
        }
    }

    public void setLocation(Location location) {
        setBpelAttribute(LoggingAttributes.LOCATION, location);
    }

    public void removeLocation() {
        removeAttribute(LoggingAttributes.LOCATION);
    }

    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

    
    private static class AlertEntityUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE =
                new AlertEntityUpdater();
        
        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private AlertEntityUpdater() {
            
        }
        
        public void update(BpelEntity target, ExtensionEntity child, Operation operation) {
            if (target instanceof Trace) {
                Trace trace = (Trace)target;
                Alert alert = (Alert)child;
                switch (operation) {
                case ADD:
                    trace.addAlert(alert);
                    break;
                case REMOVE:
                    trace.remove(alert);
                    break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index, Operation operation) {
            if (target instanceof Trace) {
                Trace trace = (Trace)target;
                Alert alert = (Alert)child;
                switch (operation) {
                case ADD:
                    trace.insertAlert(alert, index);
                    break;
                case REMOVE:
                    trace.remove(alert);
                    break;
                }
            }
        }
    }
    
}
