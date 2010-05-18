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
            handler = set.getUnit().getThisClass().getEventMethod(
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
