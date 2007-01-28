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
package org.netbeans.modules.visualweb.insync.faces;


import java.beans.MethodDescriptor;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import org.netbeans.modules.visualweb.insync.beans.Event;

/**
 * Representation of a wiring for a single faces event delegate method.
 */
public class MethodBindEvent extends Event {

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct a bound event
     *
     * @param set
     * @param name
     */
    protected MethodBindEvent(MethodBindEventSet set, MethodDescriptor md) {
        super(set, md);
        // now find our handler as a method of host bean with the right param types
        String hname = getHandlerName();
        if(hname != null)
            handler = set.getUnit().getThisClass().getMethod(
                    hname, md.getMethod().getParameterTypes());
        assert Trace.trace("insync.faces", "FE new bound MethodBindEvent: " + this);
    }

    /**
     * Construct a created event
     *
     * @param set
     * @param name
     */
    protected MethodBindEvent(MethodBindEventSet set, MethodDescriptor md, String handlerName) {
        super(set, md);
        setHandler(handlerName);
        assert Trace.trace("insync.faces", "FE new created MethodBindEvent: " + this);
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * @return The MarkupProperty that contains the binding for this event.
     */
    public MarkupProperty getBindingProperty() {
        return ((MethodBindEventSet)set).bindingProperty;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Event#getHandlerName()
     */
    public String getHandlerName() {
        MethodBindEventSet mbset = (MethodBindEventSet)set;
        String bind = mbset.bindingProperty.getValueSource();
        if (bind.startsWith("#{") && bind.endsWith("}")) {
            int dot = bind.indexOf('.', 2);
            if (dot >= 0)
                return bind.substring(dot+1, bind.length()-1);
        }
        return null;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Event#setHandler(java.lang.String)
     */
    public void setHandler(String name) {
        super.setHandler(name);
        MethodBindEventSet mbset = (MethodBindEventSet)set;
        String bind = "#{" + set.getUnit().getBeanName() + "." + name + "}";
        mbset.bindingProperty.setValue(null, bind);
    }

    //--------------------------------------------------------------------------------------- Object

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansNode#toString(java.lang.StringBuffer)
     */
    public void toString(StringBuffer sb) {
        super.toString(sb);
    }
}
