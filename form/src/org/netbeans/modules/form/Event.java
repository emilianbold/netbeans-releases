/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * A class that represents one event of a RADComponent.
 * The event is identified by the listener class and the handling method.
 * The Event class also holds all event handlers attached to the event.
 */
public class Event
{
    /**
     * The event handler methods or null if not specified. Members are
     * EventHandler.
     */
    private Vector eventHandlers = new Vector();
    private RADComponent metacomponent;
    private Method listenerMethod;

    /**
     * Constructs a new Event for the specified event method.
     * @param m The Event method that is to be represented by this Event
     */ 
    Event(RADComponent metacomp, Method m) {
        metacomponent = metacomp;
        listenerMethod = m;
    }

    public String getName() {
        return listenerMethod.getName();
    }

    public Method getListenerMethod() {
        return listenerMethod;
    }

    public RADComponent getComponent() {
        return metacomponent;
    }

    public void addHandler(EventHandler handler) {
        eventHandlers.add(handler);
    }

    public void removeHandler(EventHandler handler) {
        eventHandlers.remove(handler);
    }

    public Vector getHandlers() {
        return eventHandlers;
    }

    public void createDefaultEventHandler() {
        metacomponent.getFormModel().getFormEventHandlers().addEventHandler(
            this,
            FormUtils.getDefaultEventName(metacomponent, listenerMethod));
            
        metacomponent.getFormModel().fireFormChanged();
            
        String newHandlerName = ((EventHandler) eventHandlers.get(
            eventHandlers.size() -1)).getName();
            
        metacomponent.getNodeReference().firePropertyChangeHelper(
            FormEditor.EVENT_PREFIX + getName(), null, newHandlerName);
    }

    public void gotoEventHandler() {
        if (eventHandlers.size() == 1)
            metacomponent.getFormModel().getCodeGenerator().gotoEventHandler(
                ((EventHandler) eventHandlers.get(0)).getName());
    }

    public void gotoEventHandler(String handlerName) {
        EventHandler handler = null;
        for (int i = 0, n=eventHandlers.size(); i<n; i++)
            if (((EventHandler) eventHandlers.get(i)).getName().equals(handlerName)) {
                handler = (EventHandler) eventHandlers.get(i);
                break;
            }
        if (handler != null) {
            metacomponent.getFormModel().getCodeGenerator().gotoEventHandler(handler.getName());
        }
    }


    /** Helper class for holding textual information about one event and its handlers.
     * Used for writing to/reading from XML. */
    static class EventInfo {
        String eventName; // name of the event (equals to the listeners method)
        String eventListener; // full name of the listener class (helps to identify the event)
        String paramTypes; // names of event method parameters (helps to identify the event)
        String eventHandlers; // names of event handlers

        // constructor (used for writing Event -> XML)
        EventInfo(Event event) {
            eventName = event.getName();
            eventListener = event.getListenerMethod().getDeclaringClass().getName();

            Class[] params = event.getListenerMethod().getParameterTypes();
            StringBuffer strBuf = new StringBuffer(100);
            for (int i=0; i < params.length; i++) {
                strBuf.append(params[i].getName());
                if (i+1 < params.length)
                    strBuf.append(",");
            }
            paramTypes = strBuf.toString();

            List handlers = event.getHandlers();
            strBuf.delete(0,strBuf.length());
            for (int i=0, n=handlers.size(); i < n; i++) {
                EventHandler eh = (EventHandler)handlers.get(i);
                strBuf.append(eh.getName());
                if (i+1 < n)
                    strBuf.append(",");
            }
            eventHandlers = strBuf.toString();
        }

        // constructor (used for reading XML -> Event)
        EventInfo(String name, String listener, String params, String handlers) {
            eventName = name;
            eventListener = listener;
            paramTypes = params;
            eventHandlers = handlers;
        }

        boolean matchEvent(Event event) {
            if (!event.getName().equals(eventName))
                return false;

            if (eventListener != null && 
                    !event.getListenerMethod().getDeclaringClass().getName().equals(eventListener))
                return false;

            return paramTypes == null || matchParameters(event.getListenerMethod().getParameterTypes());
        }

        Event findEvent(ComponentEventHandlers componentHandlers) {
            EventSet[] eventSets = componentHandlers.getEventSets();

            if (eventListener != null && !eventListener.equals("")) {
                for (int i=0; i < eventSets.length; i++) {
                    if (eventListener.equals(eventSets[i].getEventSetDescriptor().getListenerType().getName())) {
                        Event[] events = eventSets[i].getEvents();
                        for (int j=0; j < events.length; j++) {
                            if (events[j].getName().equals(eventName) && 
                                (paramTypes==null || matchParameters(events[j].getListenerMethod().getParameterTypes())))
                                return events[j]; // this is it
                        }
                    }
                }
            }
            else {
                for (int i=0; i < eventSets.length; i++) {
                    Event[] events = eventSets[i].getEvents();
                    for (int j=0; j < events.length; j++) {
                        if (events[j].getName().equals(eventName))
                            return events[j];
                    }
                }
            }

            return null;
        }

        private boolean matchParameters(Class[] params) {
            StringTokenizer tok = new StringTokenizer(paramTypes, ",");
            if (params==null || params.length==0)
                return !tok.hasMoreTokens();

            for (int i=0; i < params.length; i++) {
                if (!tok.hasMoreTokens() ||
                    !params[i].getName().equals(tok.nextToken())) return false;
            }
            return !tok.hasMoreTokens();
        }
    }
}
