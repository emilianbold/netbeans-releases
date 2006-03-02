/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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
