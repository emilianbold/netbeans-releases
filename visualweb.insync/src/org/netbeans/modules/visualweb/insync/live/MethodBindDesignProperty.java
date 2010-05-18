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

import javax.faces.application.Application;
import javax.faces.el.MethodBinding;

import com.sun.faces.util.ConstantMethodBinding;

/**
 * A BeansDesignProperty subclass that knows how to handle the additional processing for JSF Method
 * Binding. Property field is always in markup, thus always a MarkupProperty.
 *
 * @author Carl Quinn
 * @version 1.0
 */
public class MethodBindDesignProperty extends BeansDesignProperty {

    MethodBindDesignEvent event;

    /**
     * Determines whether or not a property type is a method binding type, and thus should be
     * handled by this subclass.
     *
     * @return whether or not the property type is a method binding type.
     */
    public static final boolean isMethodBindingProperty(PropertyDescriptor pd) {
         return (MethodBinding.class.isAssignableFrom(pd.getPropertyType())) ||
                 (MethodExpression.class.isAssignableFrom(pd.getPropertyType()));

    }

    private  boolean isMethodExpression() {
         return MethodExpression.class.isAssignableFrom(getPropertyDescriptor().getPropertyType());

    }

    /**
     *
     */
    MethodBindDesignProperty(PropertyDescriptor descriptor, BeansDesignBean lbean) {
        super(descriptor, lbean);
        // event will get set by MethodBindDesignEvent callback
    }

    void setEventReference(MethodBindDesignEvent event) {
        this.event = event;
    }

    public MethodBindDesignEvent getEventReference() {
        return event;
    }

    /*
     *
     */
    protected void initLive() {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(((LiveUnit)getDesignBean().getDesignContext()).getBeansUnit().getClassLoader());
            if (property != null) {
                // intercept if the source is a string that we know is a binding EL
                Object value = property.getValue(descriptor.getPropertyType());
                if (value instanceof String)
                    invokeSetter(fromSource((String)value));
                else
                    super.initLive();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }

    /*
     *
     */
    protected String toSource(Object value) {
        if (value instanceof MethodBinding) {
            MethodBinding mb = (MethodBinding)value;
            if (mb instanceof ConstantMethodBinding)
                return (String)mb.invoke(null, null);  // a way to get the outcome back out
            else
                return mb.getExpressionString();
        } else if (value instanceof MethodExpression) {
            MethodExpression me = (MethodExpression)value;
                return me.getExpressionString();
        }
        if (value != null)
            System.err.println("FMBLP.toSource: Unexpected property value: " + value);
        return super.toSource(value);
    }

    /*
     *
     */
    protected Object fromSourceIncludeUnknown(String sourceValue) {
        //!CQ only works with 0-arg. TODO: match method signature

        if (FacesDesignProperty.isBindingValue(sourceValue)) {
            Application app = liveBean.unit.getFacesContext().getApplication();
            if (this.isMethodExpression()){
                return app.getExpressionFactory().createMethodExpression(liveBean.unit.getFacesContext().getELContext(), sourceValue, String.class, new Class[] {});
            } else {
                return app.createMethodBinding(sourceValue, new Class[] {});
            }
        } else if (sourceValue.length() > 0) {
            return new ConstantMethodBinding(sourceValue);
        }
        return null;
    }

    //-------------------------------------------------------------------------------------- Setters

    /**
     *
     */
    public boolean setValue(Object value) {
        // intercept strings that look like MethodBindings and convert those first
        if (value instanceof String && FacesDesignProperty.isBindingValue((String)value))
            value = fromSource((String)value);

        // default: allow super to set something sensible
        return super.setValue(value);
    }

    /**
     * Pass a value (in object and/or source form) to our bean property, creating it as needed
     */
    protected void setBeanProperty(Object value, String valueSource) {
        super.setBeanProperty(value, valueSource);
        if (value == FROMSOURCE_UNKNOWNVALUE)
            value = null;
        if (event != null)
            event.propertyChanged(value);
    }

    /**
     *
     */
    public boolean unset() {
        if (super.unset()) {
            if (event != null)
                event.propertyChanged(null);
            return true;
        }
        return false;
    }

    /**
     *
     */
    public String toString() {
        return "[FMBLP name:" + descriptor.getName() +
            " type:" + descriptor.getPropertyType() +
            " value:" + getValue() + " valueSource:" + getValueSource() + "]";
    }
}
