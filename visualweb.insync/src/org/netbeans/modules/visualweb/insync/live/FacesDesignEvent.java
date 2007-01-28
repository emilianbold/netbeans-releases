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
package org.netbeans.modules.visualweb.insync.live;

import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import javax.el.MethodExpression;

import javax.faces.application.Application;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.validator.Validator;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import com.sun.rave.designtime.EventDescriptor;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;

/**
 * A pseudo-event that encapsulates an EL MethodBinding and is implemented on top of a property
 * modeled with MethodBindDesignProperty. Does not use beans.Event, nor the faces.ReflectedEvent*
 * classes.
 *
 * NOTE: this is OBSOLETE now in favor of using the true beans.Event / faces.FacesEvent classes
 *
 * @author Carl Quinn
 */
public class FacesDesignEvent extends SourceDesignEvent {

    MethodBindDesignProperty reference;  // the persisted event state if set, i.e. not default, null if not set

    static FacesDesignEvent newInstance(BeansDesignBean bean, MethodBindDesignProperty p) {
        PropertyDescriptor pd = p.getPropertyDescriptor();
        String name = pd.getName();
        EventDescriptor ed = null;
        try {
            if (name.equals("action")) {
                Method m = Method.class.getDeclaredMethod("getName", new Class[]{});
                ed = newBoundEventDescriptor("action", m);
            }
            else if (name.equals("actionListener")) {
                Method m = ActionListener.class.getDeclaredMethod("processAction", new Class[]{ActionEvent.class});
                ed = newBoundEventDescriptor("actionListener", m);
            }
            else if (name.equals("validator")) {
                Method m = Validator.class.getDeclaredMethod("validate", new Class[]{FacesContext.class, UIInput.class});
                ed = newBoundEventDescriptor("validator", m);
            }
            else if (name.equals("valueChangeListener")) {
                Method m = ValueChangeListener.class.getDeclaredMethod("processValueChange", new Class[]{ValueChangeEvent.class});
                ed = newBoundEventDescriptor("valueChangeListener", m);
            }
        }
        catch (NoSuchMethodException e) {
        }
        return ed != null ? new FacesDesignEvent(ed, bean, p) : null;
    }

    static EventDescriptor newBoundEventDescriptor(String name, Method m) {
        MethodDescriptor md = new MethodDescriptor(m);
        md.setName(name);
        md.setDisplayName(name + "#{}");
        //md.setShortDescription();
        return new EventDescriptor(null, md);
    }

    static EventDescriptor newDescriptor(EventSetDescriptor esd) {
        MethodDescriptor md = new MethodDescriptor(esd.getListenerMethods()[0]);
        md.setName(esd.getName());
        md.setDisplayName(esd.getDisplayName());
        md.setShortDescription(esd.getShortDescription());
        return new EventDescriptor(esd, md);
    }

    /**
     * @param esd
     * @param bean
     * @param reference
     */
    public FacesDesignEvent(EventSetDescriptor esd, BeansDesignBean bean, MethodBindDesignProperty reference) {
        super(newDescriptor(esd), bean);
        this.reference = reference;
        assert Trace.trace("insync.live", "FLE event:" + reference);
    }

    /**
     * @param ed
     * @param bean
     * @param reference
     */
    public FacesDesignEvent(EventDescriptor ed, BeansDesignBean bean, MethodBindDesignProperty reference) {
        super(ed, bean);
        this.reference = reference;
        assert Trace.trace("insync.live", "FLE event:" + reference);
    }

    String scopePrefixed(String methodName) {
        return liveBean.unit.rootContainer.getInstanceName() + "." + methodName;
    }

    String scopeStripped(String refName) {
        return refName.substring(refName.indexOf('.') + 1);
    }

    /*
     * Get the unqualified event handler method name
     * @see com.sun.rave.designtime.DesignEvent#getHandlerName()
     */
    public String getHandlerName() {
        String binding = null;
        if(reference.getValue() instanceof MethodBinding) {
            MethodBinding mb = (MethodBinding)reference.getValue();
            if (mb != null) {
                binding = mb.getExpressionString();
            }
        } else if(reference.getValue() instanceof MethodExpression){
            MethodExpression me = (MethodExpression)reference.getValue();
            if(me != null) {
                binding = me.getExpressionString();
            }
        }
        if (binding == null)
            return null;
        if (binding.startsWith("#{"))
            binding = binding.substring(2, binding.length()-1);
        return scopeStripped(binding);
    }

    /*
     * Set the event handler method as an unqualified name string
     * @see com.sun.rave.designtime.DesignEvent#getHandlerName()
     */
    public boolean setHandlerName(String methodName) {
        if (methodName == null)
            methodName = getDefaultHandlerName();

        String binding = "#{" + scopePrefixed(methodName) +"}";
        Application app = ((FacesPageUnit)liveBean.unit.sourceUnit).getFacesApplication();
        MethodBinding mb = app.createMethodBinding(binding, new Class[] {});
        reference.setValue(mb);

        MethodDescriptor lmd = descriptor.getListenerMethodDescriptor();
        //!CQ move this into the Event impl
        //!TN Do we need the default body here? Doesn't seem like this could be called
        // before Event.setHandler anyway
        // XXX YES: Here's how you reproduce: Double click on a button to
        // generate the specialized event handler: then delete this in source,
        // then go back and double click again. You now get the generic
        // handler because it comes through this interface
        liveBean.unit.sourceUnit.ensureEventMethod(lmd, methodName, null, null, null);

        return true;
    }

    public boolean isHandled() {
        return reference.isModified();
    }

    public boolean removeHandler() {
        return reference.unset();
    }

    //!JOE - this is just a stub!  This needs to bang in the source code to the handler method
    String handlerMethodBody;
    public void setHandlerMethodSource(String methodBody) throws IllegalArgumentException {
        this.handlerMethodBody = methodBody;
    }
    public String getHandlerMethodSource() {
        return handlerMethodBody;
    }
}
