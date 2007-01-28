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

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.EventDescriptor;
import org.netbeans.modules.visualweb.insync.java.JMIUtils;
import org.netbeans.modules.visualweb.insync.java.JMIExpressionUtils;
import org.netbeans.modules.visualweb.insync.java.JavaClassAdapter;
import org.netbeans.modules.visualweb.insync.java.JMIMethodUtils;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.netbeans.jmi.javamodel.CallableFeature;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.jmi.javamodel.Expression;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.MethodInvocation;
import org.netbeans.jmi.javamodel.MultipartId;
import org.netbeans.jmi.javamodel.NewClassExpression;
import org.netbeans.jmi.javamodel.PrimaryExpression;
import org.netbeans.jmi.javamodel.Statement;
import org.netbeans.jmi.javamodel.StatementBlock;
import org.netbeans.jmi.javamodel.VariableAccess;

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

    final protected ArrayList events = new ArrayList();

    // Java source-based event set fields
    Statement stmt;
    JavaClassAdapter adapterClass;

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
    protected EventSet(Bean bean, EventSetDescriptor descriptor, Statement stmnt,
                        ClassDefinition adapter) {
        this(bean, descriptor);
        this.stmt = stmnt;
        this.adapterClass = new JavaClassAdapter(unit.getJavaUnit(), adapter);

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
    static EventSet newBoundInstance(BeansUnit unit, Statement s) {
        // statement must be an exec (execute expression)
        JMIUtils.beginTrans(false);
        try {
            MethodInvocation mExpr = Property.getExpression(unit, s);
            if(mExpr == null)
                return null;
            PrimaryExpression pExpr = mExpr.getParentClass();
            String cname = null;
            if (pExpr instanceof VariableAccess) {
                cname = ((VariableAccess)pExpr).getName();
            } else if (pExpr instanceof MultipartId) {
                cname = ((MultipartId)pExpr).getName();
            }
            Bean bean = unit.getBean(cname);
            if (bean == null)
                return null;
            String mname = mExpr.getName();
            EventSetDescriptor esd = bean.getEventSetDescriptorForAdder(mname);
            if (esd == null)
                return null;

            Expression argExpr = (Expression) mExpr.getParameters().get(0);
            if(!(argExpr instanceof NewClassExpression))
                return null;
            ClassDefinition clsDef = ((NewClassExpression)argExpr).getClassDefinition();
            return new EventSet(bean, esd, s, clsDef);
        }finally {
            JMIUtils.endTrans();
        }
    }

    /**
     * Construct a new EventSet, creating the underlying statement methods
     * 
     * @param bean
     * @param name
     */
    EventSet(Bean bean, EventSetDescriptor descriptor, boolean unused) {
        this(bean, descriptor);
        insertEntry();
        assert Trace.trace("insync.beans", "ES new created EventSet: " + this);
    }

    /**
     * @param md
     * @param m
     * @return
     */
    protected Event newBoundEvent(MethodDescriptor md, Method m) {
        return Event.newBoundInstance(this, md, m);
    }

    /**
     * Scan our descriptor's methods and create individual events that match
     */
    protected void bindEvents() {
        MethodDescriptor[] lmds = descriptor.getListenerMethodDescriptors();
        for (int i = 0; i < lmds.length; i++) {
            Method m = adapterClass.getMethod(lmds[i].getName(), 
                    lmds[i].getMethod().getParameterTypes());
            if(m != null) {
                Event e = newBoundEvent(lmds[i], m);
                if (e != null)
                    events.add(e);
            }
        }
    }

    protected Method stubDelegatorMethod(MethodDescriptor mdescr) {
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
        return (Method)adapterClass.addMethod(info);
    }

    private void stubBody(Method method) {
        String body = null;
        if (!method.getType().equals("void")) {
            body = "return null;";
        }
        
        JMIMethodUtils.replaceMethodBody(method, body);
    }

    protected Method getDelegatorMethod(MethodDescriptor mdescr) {
        Method delegate = adapterClass.getMethod(mdescr.getMethod().getName(), 
                mdescr.getMethod().getParameterTypes());
        if (delegate == null)
            delegate = stubDelegatorMethod(mdescr);
        return delegate;
    }

    protected void removeDelegatorMethod(Method delegator) {
        Class atype = getAdapterType();
        if (atype != null) {
            adapterClass.removeMethod(delegator);
        }
        else {
            stubBody(delegator);
        }
    }

    /**
     * Insert the stub source entry for this EventSet
     */
    protected void insertEntry() {
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            Class atype = getAdapterType();
            String adapterClassName;
            if (atype != null) {
                adapterClassName = atype.getName();
            }else {
                adapterClassName = getListenerType().getName();
            }
            CallableFeature method = unit.getPropertiesInitMethod();
            NewClassExpression nClsExpr = JMIExpressionUtils.getNewClassExpression(unit.getJavaUnit().getJavaClass(), adapterClassName);
            ArrayList arrList = new ArrayList();
            arrList.add(nClsExpr);
            stmt = JMIMethodUtils.addMethodInvocationStatement(method,
                    bean.getName(), descriptor.getAddListenerMethod().getName(), arrList);
            adapterClass = new JavaClassAdapter(nClsExpr.getClassDefinition());
            
            if (atype == null) {
                // stub all methods in adapter body if we are extending just the interface
                MethodDescriptor[] mdescrs = descriptor.getListenerMethodDescriptors();
                for (int i = 0; i < mdescrs.length; i++) {
                    System.out.println("**********Adding method: " + mdescrs[i].getMethod().getName());
                    stubDelegatorMethod(mdescrs[i]);
                }
            } 
            rollback = false;
        }finally {
            JMIUtils.endTrans(rollback);
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
    protected boolean removeEntry() {
        assert Trace.trace("insync.beans", "ES.removeEntry: " + this);
        events.clear();
        boolean retVal = false;
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            StatementBlock[] blocks = unit.getInitBlocks();
            for(int i = 0; i < blocks.length; i++) {
                Statement stmt = JMIMethodUtils.findStatement(
                        blocks[i], descriptor.getAddListenerMethod().getName(), 
                        bean.getName());
                if (stmt != null) {
                    retVal = JMIMethodUtils.removeStatement(blocks[i], stmt);
                    if(retVal)
                        //!CQ maybe remove the delegate method(s) also? would need to let Event do that...
                        stmt = null;
					break;
                }
            }
            rollback = false;
        }finally {
            JMIUtils.endTrans(rollback);
        }
        
        return retVal;        
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
    public JavaClassAdapter getAdapter() {
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
            }
            catch (ClassNotFoundException e) {
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
