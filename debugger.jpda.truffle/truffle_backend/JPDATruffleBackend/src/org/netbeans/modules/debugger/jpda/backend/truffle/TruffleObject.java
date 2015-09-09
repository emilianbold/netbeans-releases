/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.ExecutionContext;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.instrument.Visualizer;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Martin
 */
public class TruffleObject {
    
    static final int DISPLAY_TRIM = 1000;
    
    final Visualizer visualizer;
    final String name;
    final String type;
    final Object object;
    final String displayValue;
    final boolean leaf;
    
    public TruffleObject(Visualizer visualizer, String name, Object object) {
        this.visualizer = visualizer;
        this.name = name;
        this.object = object;
        this.displayValue = (visualizer != null) ? visualizer.displayValue(object, DISPLAY_TRIM) : object.toString();
        if (object instanceof String) {
            this.type = String.class.getSimpleName();
        } else if (object instanceof Number) {
            this.type = object.getClass().getSimpleName();
        } else {
            this.type = FrameSlotKind.Object.name();
        }
        this.leaf = isLeaf(object);
    }
    
    public Object[] getChildren() {
        // TODO: Handle arrays in a special way
        return getChildrenGeneric();
    }
    
    private static boolean isLeaf(Object object) {
        return isLeafGeneric(object);
    }
    /*
    private static boolean isLeafJS(Object object) {
        if (object instanceof DynamicObject) {
            DynamicObject dobj = (DynamicObject) object;
            Iterable<Property> enumerableProperties = JSObject.getEnumerableProperties(dobj);
            return !enumerableProperties.iterator().hasNext();
        } else {
            return true;
        }
    }
    */
    private static boolean isLeafGeneric(Object object) {
        if (object instanceof DynamicObject) {
            if (((DynamicObject) object).getShape().getPropertyCount() > 0 ) {//||
                //((DynamicObject) object).getShape().getEnumerablePropertyCount() > 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    /*
    private Object[] getChildrenJS() {
        //if (object instanceof JSObject) {
        //    JSObject jso = (JSObject) object;
        if (object instanceof DynamicObject) {
            DynamicObject dobj = (DynamicObject) object;
            Iterable<Property> enumerableProperties = JSObject.getEnumerableProperties(dobj);
            List<Object> ch = new ArrayList<>();
            for (Property p : enumerableProperties) {
                String name = p.getKey().toString();
                Object obj = JSObject.getProperty(dobj, name);
                //Object obj = p.get(dobj, );//jso.getProperty((JSContext) context, name);
                ch.add(new TruffleObject(visualizer, name, obj));
            }
            return ch.toArray();
        } else {
            return null;
        }
    }
    */
    private Object[] getChildrenGeneric() {
        if (object instanceof DynamicObject) {
            DynamicObject dobj = (DynamicObject) object;
            //System.err.println("getChildrenGeneric("+object+"): property count = "+dobj.getShape().getPropertyCount()+", property map = "+dobj.getShape().getPropertyMap()+", property list = "+dobj.getShape().getPropertyList());
            List<Property> props = dobj.getShape().getPropertyListInternal(true);
            int n = props.size();
            Object[] ch = new Object[n];
            for (int i = 0; i < n; i++) {
                String name = props.get(i).getKey().toString();
                Object obj = props.get(i).get(dobj, true);
                ch[i] = new TruffleObject(visualizer, name, obj);
            }
            return ch;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return name + " = " + displayValue;
    }
    
}
