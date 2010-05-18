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
