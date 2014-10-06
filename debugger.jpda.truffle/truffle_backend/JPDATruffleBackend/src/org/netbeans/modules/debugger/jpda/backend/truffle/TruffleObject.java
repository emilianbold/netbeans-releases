/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.ExecutionContext;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.objects.JSObject;
import com.oracle.truffle.om.DynamicObject;
import com.oracle.truffle.om.Property;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Martin
 */
public class TruffleObject {
    
    final ExecutionContext context;
    final String name;
    final String type;
    final Object object;
    final String displayValue;
    final boolean leaf;
    
    public TruffleObject(ExecutionContext context, String name, Object object) {
        this.context = context;
        this.name = name;
        this.object = object;
        this.displayValue = context.getVisualizer().displayValue(context, object);
        if (object instanceof String) {
            this.type = String.class.getSimpleName();
        } else {
            this.type = FrameSlotKind.Object.name();
        }
        this.leaf = isLeaf(object);
    }
    
    public Object[] getChildren() {
        // TODO: Handle arrays in a special way
        return getChildrenJS();
    }
    
    private static boolean isLeaf(Object object) {
        return isLeafJS(object);
    }
    
    private static boolean isLeafJS(Object object) {
        if (object instanceof DynamicObject) {
            DynamicObject dobj = (DynamicObject) object;
            Iterable<Property> enumerableProperties = JSObject.getEnumerableProperties(dobj);
            return !enumerableProperties.iterator().hasNext();
        } else {
            return true;
        }
    }
    
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
    
    private Object[] getChildrenJS() {
        //if (object instanceof JSObject) {
        //    JSObject jso = (JSObject) object;
        if (object instanceof DynamicObject) {
            DynamicObject dobj = (DynamicObject) object;
            Iterable<Property> enumerableProperties = JSObject.getEnumerableProperties(dobj);
            List<Object> ch = new ArrayList<>();
            for (Property p : enumerableProperties) {
                String name = p.getName().toString();
                Object obj = JSObject.getProperty(dobj, name);
                //Object obj = p.get(dobj, );//jso.getProperty((JSContext) context, name);
                ch.add(new TruffleObject(context, name, obj));
            }
            return ch.toArray();
        } else {
            return null;
        }
    }
    
    private Object[] getChildrenGeneric() {
        if (object instanceof DynamicObject) {
            DynamicObject dobj = (DynamicObject) object;
            //System.err.println("getChildrenGeneric("+object+"): property count = "+dobj.getShape().getPropertyCount()+", property map = "+dobj.getShape().getPropertyMap()+", property list = "+dobj.getShape().getPropertyList());
            List<Property> props = dobj.getShape().getPropertyListInternal(true);
            int n = props.size();
            Object[] ch = new Object[n];
            for (int i = 0; i < n; i++) {
                String name = props.get(i).getName().toString();
                Object obj = props.get(i).get(dobj, true);
                ch[i] = new TruffleObject(context, name, obj);
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
