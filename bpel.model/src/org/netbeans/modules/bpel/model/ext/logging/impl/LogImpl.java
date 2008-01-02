/*
 * LogImpl.java
 * 
 * Created on Sep 22, 2007, 10:18:00 PM
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
import org.netbeans.modules.bpel.model.ext.logging.api.Log;
import org.netbeans.modules.bpel.model.ext.logging.api.LogLevel;
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
public class LogImpl extends LoggingEntityImpl implements Log {
    LogImpl(LoggingEntityFactory factory, BpelModelImpl model, Element e ) {
        super(factory, model, e);
    }

    LogImpl(LoggingEntityFactory factory, BpelBuilderImpl builder ) {
        super(factory, builder, LoggingElements.LOG);
    }

    @Override
    protected BpelEntity create( Element element ) {
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
                LoggingAttributes.LOG_LEVEL
            };
            myAttributes.compareAndSet( null ,  ret);
        }
        return myAttributes.get();
    }

    public Class<? extends BpelEntity> getElementType() {
        return Log.class;
    }

    public EntityUpdater getEntityUpdater() {
        return LogEntityUpdater.getInstance();
    }

    public From getFrom() {
        return getChild(From.class);
    }

    public void setFrom(From from) {
        setChild(from, From.class);
    }

    public LogLevel getLevel() {
        readLock();
        try {
            String str = getAttribute(LoggingAttributes.LOG_LEVEL);
            return LogLevel.forString(str);
        }
        finally {
            readUnlock();
        }
    }

    public void setLevel(LogLevel level) {
        setBpelAttribute(LoggingAttributes.LOG_LEVEL, level);
    }

    public void removeLevel() {
        removeAttribute(LoggingAttributes.LOG_LEVEL);
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

    private static class LogEntityUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE =
                new LogEntityUpdater();
        
        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private LogEntityUpdater() {
            
        }
        
        public void update(BpelEntity target, ExtensionEntity child, Operation operation) {
            if (target instanceof Trace) {
                Trace trace = (Trace)target;
                Log log = (Log)child;
                switch (operation) {
                case ADD:
                    trace.addLog(log);
                    break;
                case REMOVE:
                    trace.remove(log);
                    break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index, Operation operation) {
            if (target instanceof Trace) {
                Trace trace = (Trace)target;
                Log log = (Log)child;
                switch (operation) {
                case ADD:
                    trace.insertLog(log, index);
                    break;
                case REMOVE:
                    trace.remove(log);
                    break;
                }
            }
        }
    }
}
