/*
 * SimpleInvoker.java
 *
 * Created on January 25, 2004, 5:53 PM
 */

package org.netbeans.actions.simple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author  tim
 */
class SimpleInvoker {
    private String targetClass;
    private String targetMethod;
    private String name;
    private boolean isDirect;
    /** Creates a new instance of SimpleInvoker */
    public SimpleInvoker(String name, String targetClass, String targetMethod, boolean isDirect) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.isDirect = isDirect;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    private Class clazz = null;
    private Class getTargetClass() throws ClassNotFoundException {
        if (clazz == null) {
            clazz = Class.forName(targetClass);
        }
        return clazz;
    }
    
    private Method method = null;
    private Method getTargetMethod() throws InvocationTargetException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException {
        if (method == null) {
            Class c = getTargetClass();
            method = c.getDeclaredMethod(targetMethod, null);
            method.setAccessible(true);
        }
        return method;
    }
    
    public void invoke (Map context) {
        try {
            if (isDirect) {
                getTargetMethod().invoke(null, null);
            } else {
                Class clazz = getTargetClass();
                Object o = context.get(clazz);
                if (o == null) {
                    throw new NullPointerException ("No instance of " + clazz + " in context");
                }
                getTargetMethod().invoke(o, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String toString() {
        return getClass() + "[" + targetClass + " method " + targetMethod + " name=" + getName() + " isDirect=" + isDirect + "]";
    }
    
    public int hashCode() {
        return getName().hashCode();
    }
    
    public boolean equals (Object o) {
        boolean result = false;
        if (o.getClass() == SimpleInvoker.class) {
            result = ((SimpleInvoker)o).getName().equals(toString());
        }
        return result;
    }    
    
}
