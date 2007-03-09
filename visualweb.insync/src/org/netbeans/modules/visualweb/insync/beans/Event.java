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
package org.netbeans.modules.visualweb.insync.beans;

import org.netbeans.modules.visualweb.insync.java.DelegatorMethod;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import java.beans.MethodDescriptor;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import java.util.List;
import org.netbeans.modules.visualweb.insync.java.Method;

/**
 * Representation of a wiring for a single event handler within an EventSet's listener adapter in
 * the init block.
 *
 * @author cquinn
 */
public class Event extends BeansNode {

    public static final Event[] EMPTY_ARRAY = {};

    // General event fields
    protected final EventSet set;  // our parent event set
    final MethodDescriptor descriptor;
    protected String defaultBody;
    protected String[] parameterNames;
    protected String[] requiredImports;

    // Java source-based event fields
    protected DelegatorMethod delegator;
    protected Method handler;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Partially construct an event to be fully populated later.
     *
     * @param set  The EventSet that this event is a member of.
     * @param descriptor  The MethodDescriptor that defines the hendler method signature+
     */
    protected Event(EventSet set, MethodDescriptor descriptor) {
        super(set.getUnit());
        this.set = set;
        this.descriptor = descriptor;
    }

    /**
     * Construct an event bound to existing method & its bean. Called only from factory below.
     *
     * @param set  The EventSet that this event is a member of.
     * @param descriptor  The MethodDescriptor that defines the handler method signature+.
     * @param delegator  The adaptor method that delegates to our handler.
     * @param call  The Apply expression in the delegator that calls our handler.
     * @param handler  Our handler method that is populated by the user.
     */
    private Event(EventSet set, MethodDescriptor descriptor,
            DelegatorMethod delegator, Object/*MethodInvocationTree*/ call, Method handler) {
        this(set, descriptor);
        this.delegator = delegator;
        this.handler = handler;
        assert Trace.trace("insync.beans", "E new bound Event: " + this);
    }

    /**
     * Create an event bound to a specific statement adapter method.
     *
     *     new adapter() {
     *         void delegateMethod(ActionEvent ae) {
     *             handlerCall(ae);
     *         }
     *         ...
     *     }
     *
     * @param set  The EventSet that this event is a member of.
     * @param md  The MethodDescriptor that defines the handler method signature+.
     * @param am  The adapter
     * @return the new bound property if bindable, else null
     */
    static Event newBoundInstance(EventSet set, MethodDescriptor md, DelegatorMethod method) {
        String mname = method.getDelegateName();
        if(mname != null) {
            Method handler = set.getUnit().getThisClass().getMethod(mname, 
                    md.getMethod().getParameterTypes());
            return new Event(set, md, method, null, handler);
        }
        
        return null;
    }

    /**
     * Construct a new Event, creating the underlying delegating method & body, and handler method
     * 
     * @param set  The EventSet that this event is a member of.
     * @param descriptor  The MethodDescriptor that defines the handler method signature+.
     * @param handler  Our handler method that is populated by the user.
     */
    Event(EventSet set, MethodDescriptor descriptor, String handler) {
        this(set, descriptor);
        insertEntry(handler);
        setHandler(handler);
        assert Trace.trace("insync.beans", "E new created Event: " + this);
    }
    
    /** 
     * Set the default body to be used for this event when it is created.
     * If not set, a generic comment will be used.
     * @param defaultBody The Java source for the method body
     */
    public void setDefaultBody(String defaultBody) {
        this.defaultBody = defaultBody;
    }
    
    /**
     * Set the parameter names to be used when initially creating this
     * event. It has no effect if the event already exists.
     * @param parameterNames An array of names to be assigned to the 
     *  parameters. The array must have at least as many names as there
     *  are arguments in the method.
     */
    public void setParameterNames(String[] parameterNames) {
        this.parameterNames = parameterNames;
    }
    
    /**
     * Set the list of required imports requested by this event.
     * @param An array of fully qualified class names that should be
     *  imported when an event handler for this event is created
     */
    public void setRequiredImports(String[] requiredImports) {
        this.requiredImports = requiredImports;
    }

    /**
     * Wire up the delegation for this event to the handler method, inserting the methods and
     * statements as needed.
     * 
     * @param delegate name of delegate, pass null to use a tmp name & set later
     */
    protected void insertEntry(String handler) {
        Class[] pTypes = descriptor.getMethod().getParameterTypes();
        String[] pNames = Naming.paramNames(pTypes, descriptor.getParameterDescriptors());
        boolean noreturn = false;
        if (descriptor.getMethod().getReturnType().getName().equals("void")) {
            noreturn = true;
        }
        set.getDelegatorMethod(descriptor).addDelegateStatement(handler, pNames, noreturn);
    }

    /**
     * Remove this event's delegation method from the adapter. This event instance is dead & should
     * not be used.
     * 
     * @return true iff the source entry for this event was actually removed.
     */
    protected boolean removeEntry() {
        JavaClass adapter = set.getAdapter();
        if (delegator != null) {
            set.removeDelegatorMethod(delegator);
            delegator = null;
            return true;
        }
        return false;
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * Get the (method) descriptor for this event
     * 
     * @return
     */
    public MethodDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Get the owning event set for this event.
     * 
     * @return the owning event set.
     */
    public EventSet getEventSet() {
        return set;
    }

    /**
     * Get the name of this event
     * 
     * @return The name of this event.
     */
    public String getName() {
        return descriptor.getName();
    }

    /**
     * Set the name of the handler method for this event
     * 
     * @param name  The new handler method name.
     */
    public void setHandler(String name) {
        if (handler != null) {
            if (!getUnit().hasEventMethod(descriptor, name)) {
                handler.rename(name);  // rename existing method in place
            }
        } else {
            handler = getUnit().ensureEventMethod(descriptor, name,
                    defaultBody, parameterNames, requiredImports);
        }
        if (delegator != null) {
            delegator.setDelegateName(name);
        }
    }

    /**
     * Get the name of the handler method for this event.
     * @return The handler method name.
     */
    public String getHandlerName() {
        if(handler != null) {
            return handler.getName();
        } else if(delegator != null) {
            return delegator.getDelegateName();
        }
        
        return null;
    }

    /**
     * Get the handler method for this event.
     * 
     * @return The handler method itself.
     */
    public Method getHandlerMethod() {
        return handler;
    }

    //--------------------------------------------------------------------------------------- Object

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansNode#toString(java.lang.StringBuffer)
     */
    public void toString(StringBuffer sb) {
        sb.append(" name:");
        sb.append(getName());
        sb.append("=>");
        sb.append(getHandlerName());
    }
}
