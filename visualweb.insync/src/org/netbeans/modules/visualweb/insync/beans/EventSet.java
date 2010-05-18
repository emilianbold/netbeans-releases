/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.insync.beans;

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.EventDescriptor;
import org.netbeans.modules.visualweb.insync.java.DelegatorMethod;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.netbeans.modules.visualweb.insync.java.Method;
import org.netbeans.modules.visualweb.insync.java.Statement;

/**
 * Representation of a wiring for a single event listener, which maps to a single add*Listener
 * statement in the init block. Will contain one or more Events that route the specific events to
 * the handler methods
 */
public class EventSet extends BeansNode {

    public static final EventSet[] EMPTY_ARRAY = {};

    // General event set fields
    final protected Bean bean;   // owning bean
    final protected EventSetDescriptor descriptor;

    final protected List<Event> events = new ArrayList<Event>();

    // Java source-based event set fields
    private Statement stmt;
    private JavaClass adapterClass;
    private boolean inserted;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Partially construct an EventSet to be fully populated later
     *
     * @param beansUnit
     * @param bean
     * @param name
     */
    protected EventSet(Bean bean, EventSetDescriptor descriptor) {
        super(bean.getUnit());
        this.bean = bean;
        this.descriptor = descriptor;
    }

    /**
     * Construct an EventSet bound to existing statement & its bean. Called only from factory below.
     *
     * @param beansUnit
     */
    protected EventSet(Bean bean, EventSetDescriptor descriptor, Statement stmt, JavaClass adapter) {
        this(bean, descriptor);
        this.stmt = stmt;
        this.adapterClass = adapter;
        if (adapter != null)
            bindEvents();

        assert Trace.trace("insync.beans", "ES new bound EventSet: " + this);
    }


    /**
     * Create a EventSet setting bound to a specific statement of the form:
     *
     *     bean.addXyzListener(new XyzAdapter() {
     *             ...events...
     *         });
     *
     * @param unit
     * @param s
     * @return the new bound event set if bindable, else null
     */
    static EventSet newBoundInstance(BeansUnit unit, Statement stmt) {
        Bean bean = unit.getBean(stmt.getBeanName());
        if(bean == null) {
            return null;
        }
        EventSetDescriptor esd = bean.getEventSetDescriptorForAdder(stmt.getPropertySetterName());
        if (esd == null) {
            return null;
        }
        
        JavaClass adapter = stmt.getAdapterClass();
        if(adapter != null) {
            EventSet eventSet = new EventSet(bean, esd, stmt, adapter);
            eventSet.setInserted(true);
            return eventSet;
        }
        return null;
    }

    /**
     * Construct a new EventSet, creating the underlying statement methods
     * 
     * @param bean
     * @param name
     */
    EventSet(Bean bean, EventSetDescriptor descriptor, boolean unused) {
        this(bean, descriptor);
        assert Trace.trace("insync.beans", "ES new created EventSet: " + this);
    }

    /**
     * @param md
     * @param m
     * @return
     */
    protected Event newBoundEvent(MethodDescriptor md, DelegatorMethod m) {
        return Event.newBoundInstance(this, md, m);
    }

    /**
     * Scan our descriptor's methods and create individual events that match
     */
    protected void bindEvents() {
        MethodDescriptor[] lmds = descriptor.getListenerMethodDescriptors();
        for (int i = 0; i < lmds.length; i++) {
            DelegatorMethod m = adapterClass.getDelegatorMethod(lmds[i].getName(), 
                    lmds[i].getMethod().getParameterTypes());
            if(m != null ) {
                Event e = newBoundEvent(lmds[i], m);
                if (e != null)
                    events.add(e);
            }
        }
    }

    protected DelegatorMethod stubDelegatorMethod(MethodDescriptor mdescr) {
        Class retType = mdescr.getMethod().getReturnType();
        
        // now add parameter(s)
        Class[] pts = mdescr.getMethod().getParameterTypes();
        String[] pns = Naming.paramNames(pts, mdescr.getParameterDescriptors());
        
        String body = null;
        if (retType != Void.TYPE) {
            body = "return null;";
        }
        
        org.netbeans.modules.visualweb.insync.java.MethodInfo info = 
                new org.netbeans.modules.visualweb.insync.java.MethodInfo(mdescr.getName(), retType, Modifier.PUBLIC, 
                pns, pts, body, null);        
        return adapterClass.addDelegatorMethod(info);
    }

    private void stubBody(Method method) {
        Class retType = getMethodDescriptor(method.getName()).getMethod().getReturnType();
        String body = null;
        if (retType != Void.TYPE) {
            body = "return null;";
        }
        method.replaceBody(body);
    }

    protected DelegatorMethod getDelegatorMethod(MethodDescriptor mdescr) {
        DelegatorMethod delegate = adapterClass.getDelegatorMethod(mdescr.getMethod().getName(), 
                mdescr.getMethod().getParameterTypes());
        if (delegate == null)
            delegate = stubDelegatorMethod(mdescr);
        return delegate;
    }

    protected void removeDelegatorMethod(Method delegator) {
        Class type = getAdapterType();
        if (type != null) {
            delegator.remove();
        } else {
            stubBody(delegator);
        }
    }

    /**
     * Insert the stub source entry for this EventSet
     */
    public void insertEntry() {
        Class atype = getAdapterType();
        String adapterClassName;
        if (atype != null) {
            adapterClassName = atype.getName();
        }else {
            adapterClassName = getListenerType().getName();
        }
        Method method = unit.getPropertiesInitMethod();
        if (descriptor.getAddListenerMethod() != null) {
            stmt = method.addEventSetStatement(bean.getName(), 
                    descriptor.getAddListenerMethod().getName(), adapterClassName);
            adapterClass = stmt.getAdapterClass();
            if (atype == null) {
                // stub all methods in adapter body if we are extending just the interface
                MethodDescriptor[] mdescrs = descriptor.getListenerMethodDescriptors();
                for (int i = 0; i < mdescrs.length; i++) {
                    stubDelegatorMethod(mdescrs[i]);
                }
            }
            
            for (Event event : events) {
                event.insertEntry();
                event.setHandler();
            }
            inserted = true;
        }
    }

    /**
     * Release this EventSet's hold on the underlying source. Use when another object will take over.
     * Clear child event list, etc. This eventSet instance is dead & should not be used.
     */
    protected void releaseEntry() {
        events.clear();
        stmt = null;
    }

    /**
     * Clear child event list, and remove this eventSet's statement from the init method. Removing
     * this statement will take out all the event's entries, so they don't have to be explicitly
     * removed. This eventSet instance is dead & should not be used.
     * 
     * @return true iff the source entry for this event was actually removed.
     */
    public boolean removeEntry() {
        assert Trace.trace("insync.beans", "ES.removeEntry: " + this);
        boolean removed = false;
        events.clear();
        if(inserted & stmt != null) {
            removed = stmt.remove();
            stmt = null;
        }
        return removed;
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * Get the descriptor for this EventSet
     */
    public EventSetDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Get the name of this EventSet
     */
    public String getName() {
        return descriptor.getName();
    }

    /**
     * Get this EventSet's working adapter class
     */
    public JavaClass getAdapter() {
        return adapterClass;
    }

    /**
     * Get this EventSet's event listener interface type
     */
    public Class getListenerType() {
        return descriptor.getListenerType();
    }

    /**
     * Get this EventSet's event adapter type, if one exists
     */
    protected Class getAdapterType() {
        String lname = getListenerType().getName();
        int lpos = lname.lastIndexOf("Listener");
        if (lpos >= 0) {
            String aname = lname.substring(0, lpos) + "Adapter";
            try {
                return unit.getBeanClass(aname);
            }catch (ClassNotFoundException e) {
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------------------- Events

    /**
     * 
     */
    public MethodDescriptor getMethodDescriptor(String name) {
        MethodDescriptor[] mds = descriptor.getListenerMethodDescriptors();
        for (int i = 0; i < mds.length; i++) {
            if (mds[i].getName().equals(name))
                return mds[i];
        }
        return null;
    }
    
    /**
     * 
     */
    public Event[] getEvents() {
        return (Event[])events.toArray(Event.EMPTY_ARRAY);
    }

    /**
     * 
     */
    public Event getEvent(String name) {
        for (Iterator i = events.iterator(); i.hasNext(); ) {
            Event e = (Event)i.next();
            if (e.getName().equals(name))
                return e;
        }
        return null;
    }

    /**
     * 
     */
    protected Event newCreatedEvent(MethodDescriptor md, String value) {
        return new Event(this, md, value);
    }

    /**
     *
     */
    public Event setEvent(EventDescriptor ed, MethodDescriptor md, String value) {
        Event e = getEvent(md.getName());
        if (e != null) {
            Object defaultBody = ed.getEventSetDescriptor().getValue(Constants.EventDescriptor.DEFAULT_EVENT_BODY);
            if (defaultBody instanceof String) {
                e.setDefaultBody((String)defaultBody);
            }
            Object parameterNames = ed.getEventSetDescriptor().getValue(Constants.EventDescriptor.PARAMETER_NAMES);
            if (parameterNames instanceof String[]) {
                e.setParameterNames((String[])parameterNames);
            }
            Object requiredImports = ed.getEventSetDescriptor().getValue(Constants.EventDescriptor.REQUIRED_IMPORTS);
            if (requiredImports instanceof String[]) {
                e.setRequiredImports((String[])requiredImports);
            }
            e.setHandler(value);
        }
        else {
            e = newCreatedEvent(md, value);
            if (e != null)
                events.add(e);
        }
        return e;
    }

    /**
     * 
     */
    public void unsetEvent(Event e) {
        if (events.remove(e)) {
            e.removeEntry();
            if (events.isEmpty())
                bean.unsetEventSet(this);  // will call back to our removeEntry()
        }
    }
    
    public String getAddListenerMethodName() {
        if(descriptor.getAddListenerMethod() != null) {
            return descriptor.getAddListenerMethod().getName();
        }
        return null;
    }

    public boolean isInserted() {
        return inserted; 
    }
    
    public void setInserted(boolean inserted) {
        this.inserted = inserted;
    }
    
    //--------------------------------------------------------------------------------------- Object

    /**
     * 
     */
    public void toString(StringBuffer sb) {
        sb.append(" n:");
        sb.append(getName());
        sb.append(" adapter:");
        sb.append(adapterClass);
        sb.append(" events:");
        Event[] events = getEvents();
        for (int i = 0; i < events.length; i++)
            sb.append(events[i].toString());
    }
}
