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

import java.beans.PropertyDescriptor;
import javax.el.MethodExpression;

import javax.faces.el.MethodBinding;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.EventDescriptor;
import com.sun.faces.util.ConstantMethodBinding;

/**
 * A specialized event for MethodBinding based events that coordinates with a MethodBindDesignProperty
 *
 * @author Carl Quinn
 */
public class MethodBindDesignEvent extends BeansDesignEvent {

    MethodBindDesignProperty reference;  // cross reference to the shadow MB live property

    /**
     * @param ed
     * @param lbean
     * @param reference
     */
    public MethodBindDesignEvent(EventDescriptor ed, BeansDesignBean lbean,
                               MethodBindDesignProperty reference) {
        super(ed, lbean);
        this.reference = reference;
        reference.setEventReference(this);
        assert Trace.trace("insync.live", "FLE event:" + reference);
    }

    /*
     * Set the event handler method as an unqualified name string
     * @see com.sun.rave.designtime.DesignEvent#getHandlerName()
     */
    public boolean setHandlerName(String methodName) {
        if (super.setHandlerName(methodName)) {
            reference.initLive();
            return true;
        }
        return false;
    }

    protected void initEvent(String handlerName) {
        super.initEvent(handlerName);
        if (reference != null) {
	        // This looks HACKed, but I am just not sure
	        // I cloned this code from BeansDesignBean.newDesignEvent since when I was created
	        // the property may not have existed, IE not set, on the actual bean
	        Object possiblePropertyDescriptor = descriptor.getEventSetDescriptor().getValue(Constants.EventSetDescriptor.BINDING_PROPERTY);
	        if (possiblePropertyDescriptor instanceof PropertyDescriptor) {
	            PropertyDescriptor propertyDescriptor = (PropertyDescriptor) possiblePropertyDescriptor;
	            if (MethodBindDesignProperty.isMethodBindingProperty(propertyDescriptor)) {
	                reference.property = ((BeansDesignBean) liveBean).bean.getProperty(propertyDescriptor.getName());
	            }
	        }
        }
    }

    /*
     * @see com.sun.rave.designtime.DesignEvent#removeHandler()
     */
    public boolean removeHandler() {
        if (super.removeHandler()) {
            reference.unset();
            reference.initLive();
            return true;
        }
        return false;
    }

    /**
     * @param value
     */
    void propertyChanged(Object value) {
        if (event != null &&
                ((value instanceof ConstantMethodBinding) ||
                (!(value instanceof MethodBinding)  && !(value instanceof MethodExpression)))){
            ((BeansDesignBean)liveBean).bean.releaseEventSet(event.getEventSet());
            event = null;
        }
    }

}
