/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime;

import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;

/**
 * The EventDescriptor defines a single event (event set plus method).  This is used to define and
 * locate event hook objects.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see java.beans.EventSetDescriptor
 * @see java.beans.MethodDescriptor
 */
public class EventDescriptor {

    protected EventSetDescriptor eventSet;
    protected MethodDescriptor listenerMethod;

    public EventDescriptor() {}

    public EventDescriptor(EventSetDescriptor eventSet, MethodDescriptor listenerMethod) {
        this.eventSet = eventSet;
        this.listenerMethod = listenerMethod;
    }

    public void setEventSetDescriptor(EventSetDescriptor eventSet) {
        this.eventSet = eventSet;
    }

    public EventSetDescriptor getEventSetDescriptor() {
        return eventSet;
    }

    public void setListenerMethodDescriptor(MethodDescriptor listenerMethod) {
        this.listenerMethod = listenerMethod;
    }

    public MethodDescriptor getListenerMethodDescriptor() {
        return listenerMethod;
    }

    public String getName() {
        return listenerMethod != null ? listenerMethod.getName() : "";
    }

    public String getDisplayName() {
        return listenerMethod != null ? listenerMethod.getDisplayName() : "";
    }

    public String getShortDescription() {
        return listenerMethod != null ? listenerMethod.getShortDescription() : "";
    }

    public boolean isExpert() {
        return listenerMethod != null ? listenerMethod.isExpert() : false;
    }

    public boolean isHidden() {
        return listenerMethod != null ? listenerMethod.isHidden() : false;
    }

    public boolean isPreferred() {
        return listenerMethod != null ? listenerMethod.isPreferred() : false;
    }

    public boolean equals(Object o) {
        if (o instanceof EventDescriptor) {
            EventDescriptor ed = (EventDescriptor)o;
            return ed == this ||
                ed.eventSet.equals(this.eventSet) &&
                ed.listenerMethod.equals(this.listenerMethod);
        }
        return false;
    }
}
