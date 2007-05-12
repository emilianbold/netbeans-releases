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
