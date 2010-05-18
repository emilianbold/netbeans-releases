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
package org.netbeans.modules.visualweb.insync.faces;

import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import javax.el.MethodExpression;

import javax.faces.el.MethodBinding;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import org.netbeans.modules.visualweb.insync.beans.Event;
import org.netbeans.modules.visualweb.insync.beans.EventSet;
import org.netbeans.modules.visualweb.insync.beans.Property;

/**
 * An EventSet that
 */
public class MethodBindEventSet extends EventSet {

    MarkupProperty bindingProperty;  // the MarkupProperty (attr) that defines our event wiring
    MethodBindEvent event;  // The Event instance that represents the wiring

    /**
     * Determins if a given property can and does represent a method binding.
     *
     * @param p The property to check.
     * @return True iff the property can and does represent a method binding.
     */
    static boolean isMethodBindProperty(Property p) {
        PropertyDescriptor pd = p.getDescriptor();
        if (MethodBinding.class.isAssignableFrom(pd.getPropertyType()) ||
                 MethodExpression.class.isAssignableFrom(pd.getPropertyType())) {
            String bind = p.getValueSource();
            if (bind.startsWith("#{") && bind.endsWith("}"))
                return bind.indexOf('.', 2) >= 0;
        }
        return false;
    }

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct an MethodBindEventSet bound to an existing specific method binding MarkupProperty
     *
     * @param bean
     * @param descriptor
     * @param bindingProperty
     */
    protected MethodBindEventSet(FacesBean bean, EventSetDescriptor descriptor,
                                 MarkupProperty bindingProperty) {
        super(bean, descriptor);  // won't try to bind
        this.bindingProperty = bindingProperty;

        bindEvents();

        assert Trace.trace("insync.faces", "MBES new bound MethodBindEventSet: " + this);
    }

    /**
     * Construct a new MethodBindEventSet, creating the underlying statement methods
     *
     * @param bean
     * @param name
     */
    MethodBindEventSet(FacesBean bean, EventSetDescriptor descriptor, PropertyDescriptor pd) {
        super(bean, descriptor);
        bindingProperty = (MarkupProperty)bean.setProperty(pd.getName(), null, "");
        bindEvents();
        assert Trace.trace("insync.beans", "MBES new created MethodBindEventSet: " + this);
    }

    /**
     * Called when the property has changed, decoupling from this event & we just need to clean up.
     * @see org.netbeans.modules.visualweb.insync.beans.EventSet#releaseEntry()
     */
    public void releaseEntry() {
        if (bindingProperty != null) {
            event = null;
            bindingProperty = null;
        }
    }

    /**
     * @see org.netbeans.modules.visualweb.insync.beans.EventSet#insertEntry()
     */
    public void insertEntry() {
    }
    
    /*
     * @see org.netbeans.modules.visualweb.insync.beans.EventSet#removeEntry()
     */
    public boolean removeEntry() {
        assert Trace.trace("insync.beans", "MBES.removeEntry: " + this);
        if (bindingProperty != null) {
            event = null;
            bean.unsetProperty(bindingProperty);
            bindingProperty = null;
            return true;
        }
        return false;
    }

    //--------------------------------------------------------------------------------------- Events

    /**
     * This set always contains exactly one event which we already know about.
     * @see org.netbeans.modules.visualweb.insync.beans.EventSet#newCreatedEvent(java.beans.MethodDescriptor, java.lang.String)
     */
    protected Event newCreatedEvent(MethodDescriptor md, String value) {
        return null;
    }

    /**
     * Bind our always-present, exactly-one Event
     * @see org.netbeans.modules.visualweb.insync.beans.EventSet#bindEvents()
     */
    protected void bindEvents() {
        MethodDescriptor[] lmds = descriptor.getListenerMethodDescriptors();
        event = new MethodBindEvent(this, lmds[0]);
        events.add(event);
    }

    //--------------------------------------------------------------------------------------- Object

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansNode#toString(java.lang.StringBuffer)
     */
    public void toString(StringBuffer sb) {
        sb.append(" bindingProperty:");
        sb.append(bindingProperty);
        super.toString(sb);
    }
}
