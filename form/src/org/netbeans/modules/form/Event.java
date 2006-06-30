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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.*;
import java.beans.EventSetDescriptor;
import java.lang.reflect.Method;

/**
 * Represents one event of a component. Holds handlers attached to the event.
 *
 * @author Tomas Pavek
 */

public class Event {

    private static String[] NO_HANDLERS = {};

    private RADComponent component;
    private EventSetDescriptor eventSetDescriptor;
    private Method listenerMethod;
    private boolean inCEDL; // CEDL - common event dispatching listener
    private List eventHandlers;

    Event(RADComponent component,
          EventSetDescriptor eventSetDescriptor,
          Method listenerMethod)
    {
        this.component = component;
        this.eventSetDescriptor = eventSetDescriptor;
        this.listenerMethod = listenerMethod;
    }

    // --------

    public String getName() {
        return listenerMethod.getName();
    }

    public String getId() {
        return FormEvents.getEventIdName(listenerMethod);
    }

    public final RADComponent getComponent() {
        return component;
    }

    public final EventSetDescriptor getEventSetDescriptor() {
        return eventSetDescriptor;
    }

    public final Method getListenerMethod() {
        return listenerMethod;
    }

    public boolean hasEventHandlers() {
        return eventHandlers != null && eventHandlers.size() > 0;
    }

    public boolean hasEventHandler(String handler) {
        return eventHandlers != null ? eventHandlers.contains(handler) : false;
    }

    public String[] getEventHandlers() {
        if (eventHandlers == null || eventHandlers.size() == 0)
            return NO_HANDLERS;

        String[] handlerNames = new String[eventHandlers.size()];
        eventHandlers.toArray(handlerNames);
        return handlerNames;
    }

    // CEDL - common event dispatching listener
    public final boolean isInCEDL() {
        return inCEDL;
    }

    // --------

    void setInCEDL(boolean isIn) {
        inCEDL = isIn;
    }

    boolean addEventHandler(String handlerName) {
        if (eventHandlers == null)
            eventHandlers = new ArrayList(1);
        else if (eventHandlers.contains(handlerName))
            return false;

        eventHandlers.add(handlerName);
        return true;
    }

    boolean removeEventHandler(String handlerName) {
        return eventHandlers != null && eventHandlers.remove(handlerName);
    }

    boolean renameEventHandler(String oldHandlerName, String newHandlerName) {
        if (eventHandlers == null)
            return false;
        int index = eventHandlers.indexOf(oldHandlerName);
        if (index < 0 || eventHandlers.contains(newHandlerName))
            return false;

        eventHandlers.set(index, newHandlerName);
        return true;
    }

    List getEventHandlerList() {
        return eventHandlers;
    }
}
