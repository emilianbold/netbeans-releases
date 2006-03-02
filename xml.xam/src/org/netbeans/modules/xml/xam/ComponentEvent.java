/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.xam;

import java.util.EventObject;

/**
 *
 * @author Nam Nguyen
 * @author Rico Cruz
 * @author Chris Webster
 */
public class ComponentEvent extends EventObject {
    static final long serialVersionUID = 1L;
    
    private EventType event;
    
    /**
     * Creates a new instance of ComponentEvent
     */
    public ComponentEvent(Object source, EventType t) {
        super(source);
        event = t;
    }
    
    public enum EventType {
        ATTRIBUTE {
            public void fireEvent(ComponentEvent evt, 
                                           ComponentListener l) {
                l.valueChanged(evt);
                }}, 
        CHILD_ADDED {
                public void fireEvent(ComponentEvent evt, 
                                           ComponentListener l) {
                l.childrenAdded(evt);
                }}, 
        CHILD_REMOVED {
                public void fireEvent(ComponentEvent evt, 
                                           ComponentListener l) {
                l.childrenDeleted(evt);
                }};
        
        public abstract void fireEvent(ComponentEvent evt, 
                                       ComponentListener l);
    }
    
    public EventType getEventType() {
        return event;
    }
}
