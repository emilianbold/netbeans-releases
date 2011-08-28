/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Bachorik
 */
abstract public class JavaComponentInfo implements ComponentInfo {

    private static final JavaComponentInfo[] NO_SUBCOMPONENTS = new JavaComponentInfo[]{};
    private static final int MAX_TEXT_LENGTH = 80;
    //private AWTComponentInfo parent;
    private Rectangle bounds;
    private Rectangle windowBounds;
    private String name;
    private String type;
    private JavaComponentInfo[] subComponents;
    private List<PropertySet> propertySets = new ArrayList<PropertySet>();
    private PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    private JPDAThreadImpl thread;
    private ObjectReference component;
    private FieldInfo fieldInfo;
    private String componentText;
    private RemoteServices.ServiceType sType;

    public JavaComponentInfo(JPDAThreadImpl t, ObjectReference component, RemoteServices.ServiceType sType) throws RetrievalException {
        this.thread = t;
        this.component = component;
        this.type = component.referenceType().name();
        this.sType = sType;
    }
    
    final protected void init() throws RetrievalException {
        retrieve();
        addProperties();
        findComponentFields();
    }
    
    abstract protected void retrieve() throws RetrievalException;
    
    final public JPDAThreadImpl getThread() {
        return thread;
    }

    final public ObjectReference getComponent() {
        return component;
    }

    final public String getName() {
        return name;
    }

    final public String getTypeName() {
        int d = type.lastIndexOf('.');
        String typeName;
        if (d > 0) {
            typeName = type.substring(d + 1);
        } else {
            typeName = type;
        }
        return typeName;
    }

    final public void setComponentText(String componentText) {
        if (componentText.length() > MAX_TEXT_LENGTH) {
            this.componentText = componentText.substring(0, MAX_TEXT_LENGTH) + "...";
        } else {
            this.componentText = componentText;
        }
    }

    @Override
    public String getDisplayName() {
        String typeName = getTypeName();
        String fieldName = (fieldInfo != null) ? fieldInfo.getName() + " " : "";
        String text = (componentText != null) ? " \"" + componentText + "\"" : "";
        return fieldName + "[" + typeName + "]" + text;
    }

    @Override
    public String getHtmlDisplayName() {
        if (isCustomType() || componentText != null) {
            String typeName = getTypeName();
            if (isCustomType()) {
                typeName = "<b>" + typeName + "</b>";
            }
            String fieldName = (fieldInfo != null) ? fieldInfo.getName() + " " : "";
            String text;
            if (componentText != null) {
                text = escapeHTML(componentText);
                text = " <font color=\"#A0A0A0\">\"" + text + "\"</font>";
            } else {
                text = "";
            }
            return fieldName + "[" + typeName + "]" + text;
        } else {
            return null;
        }
    }

    final public String getType() {
        return type;
    }

    final public FieldInfo getField() {
        return fieldInfo;
    }

    final public boolean isCustomType() {
        return isCustomType(type);
    }
    
    public static boolean isCustomType(String type) {
        return !(type.startsWith("java.awt.") || type.startsWith("javax.swing."));  // NOI18N
    }

    @Override
    final public Rectangle getBounds() {
        return bounds;
    }

    @Override
    final public Rectangle getWindowBounds() {
        if (windowBounds == null) {
            return bounds;
        } else {
            return windowBounds;
        }
    }

    final public void addPropertySet(PropertySet ps) {
        propertySets.add(ps);
    }

    @Override
    final public PropertySet[] getPropertySets() {
        return propertySets.toArray(new PropertySet[]{});
    }

    final protected void setSubComponents(JavaComponentInfo[] subComponents) {
        this.subComponents = subComponents;
    }

    @Override
    final public JavaComponentInfo[] getSubComponents() {
        if (subComponents == null) {
            return NO_SUBCOMPONENTS;
        } else {
            return subComponents;
        }
    }

    @Override
    final public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pchs.addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    final public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pchs.removePropertyChangeListener(propertyChangeListener);
    }

    final protected void firePropertyChange(String name, Object o, Object n) {
        pchs.firePropertyChange(name, o, n);
    }

    final public void setFieldInfo(FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }
    
    final public void setBounds(Rectangle r) {
        this.bounds = r;
    }
    
    
    final public void setWindowBounds(Rectangle rectangle) {
        this.windowBounds = rectangle;
    }
    
    
    final public void setName(String value) {
        this.name = value;
    }
    
    
    final public void setComponent(ObjectReference component) {
        this.component = component;
    }
    
    
    final public void setType(String name) {
        this.type = name;
    }
    
    private void addProperties() {
        // TODO: Try to find out the BeanInfo of the class
        List<Method> allMethods = component.referenceType().allMethods();
        //System.err.println("Have "+allMethods.size()+" methods.");
        Map<String, Method> methodsByName = new HashMap<String, Method>(allMethods.size());
        for (Method m : allMethods) {
            String mName = m.name();
            if ((mName.startsWith("get") || mName.startsWith("set")) && mName.length() > 3 ||
                 mName.startsWith("is") && mName.length() > 2) {
                if ((mName.startsWith("get") || mName.startsWith("is")) && m.argumentTypeNames().size() == 0 ||
                    mName.startsWith("set") && m.argumentTypeNames().size() == 1 && "void".equals(m.returnTypeName())) {

                    methodsByName.put(mName, m);
                }
            }
        }
        Map<String, Property> sortedProperties = new TreeMap<String, Property>();
        //final List<Property> properties = new ArrayList<Property>();
        for (String mName : methodsByName.keySet()) {
            //System.err.println("  Have method '"+name+"'...");
            if (mName.startsWith("set")) {
                continue;
            }
            String property;
            String setName;
            if (mName.startsWith("is")) {
                property = Character.toLowerCase(mName.charAt(2)) + mName.substring(3);
                setName = "set" + mName.substring(2);
            } else { // startsWith("get"):
                property = Character.toLowerCase(mName.charAt(3)) + mName.substring(4);
                setName = "set" + mName.substring(3);
            }
            Property p = new ComponentProperty(property, methodsByName.get(mName), methodsByName.get(setName),
                                               this, component, getThread(), getThread().getDebugger(), sType);
            sortedProperties.put(property, p);
            //System.err.println("    => property '"+property+"', p = "+p);
        }
        final Property[] properties = sortedProperties.values().toArray(new Property[] {});
        addPropertySet(
            new PropertySet("Properties", "Properties", "All component properties") {

                @Override
                public Property<?>[] getProperties() {
                    return properties;//.toArray(new Property[] {});
                }
            });
        Method getTextMethod = methodsByName.get("getText");    // NOI18N
        if (getTextMethod != null) {
            try {
                Value theText = component.invokeMethod(getThread().getThreadReference(), getTextMethod, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                if (theText instanceof StringReference) {
                    setComponentText(((StringReference) theText).value());
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    protected static boolean isInstanceOfClass(ClassType c1, ClassType c2) {
        if (c1.equals(c2)) {
            return true;
        }
        c1 = c1.superclass();
        if (c1 == null) {
            return false;
        }
        return isInstanceOfClass(c1, c2);
    }
    
    private void findComponentFields() {
        List<JavaComponentInfo> customParents = new ArrayList<JavaComponentInfo>();
        fillCustomParents(customParents, this);
        findFieldsInParents(customParents, this);
    }

    private static void fillCustomParents(List<JavaComponentInfo> customParents, JavaComponentInfo ci) {
        ComponentInfo[] subs = ci.getSubComponents();
        if (subs.length > 0 && ci.isCustomType()) {
            customParents.add(ci);
        }
        for (ComponentInfo sci : subs) {
            fillCustomParents(customParents, (JavaComponentInfo)sci);
        }
    }

    private static void findFieldsInParents(List<JavaComponentInfo> customParents, JavaComponentInfo ci) {
        ComponentInfo[] subComponents = ci.getSubComponents();
        ObjectReference component = ci.getComponent();
        for (JavaComponentInfo cp : customParents) {
            ObjectReference c = cp.getComponent();
            Map<Field, Value> fieldValues = c.getValues(((ClassType) c.referenceType()).fields());
            for (Map.Entry<Field, Value> e : fieldValues.entrySet()) {
                if (component.equals(e.getValue())) {
                    ci.setFieldInfo(new JavaComponentInfo.FieldInfo(e.getKey(), cp));
                }
            }
        }
        for (ComponentInfo sci : subComponents) {
            findFieldsInParents(customParents, (JavaComponentInfo)sci);
        }
    }
    
    private static class ComponentProperty extends Node.Property {
        
        private String propertyName;
        private Method getter;
        private Method setter;
        private JavaComponentInfo ci;
        private ObjectReference component;
        private JPDAThreadImpl t;
        private ThreadReference tawt;
        private JPDADebuggerImpl debugger;
        private String value;
        private final Object valueLock = new Object();
        private final String valueCalculating = "calculating";
        private final RemoteServices.ServiceType sType;
        private boolean valueIsEditable;
        private Type valueType;
        
        ComponentProperty(String propertyName, Method getter, Method setter,
                          JavaComponentInfo ci, ObjectReference component,
                          JPDAThreadImpl t, JPDADebuggerImpl debugger, RemoteServices.ServiceType sType) {
            super(String.class);
            this.propertyName = propertyName;
            this.getter = getter;
            this.setter = setter;
            this.ci = ci;
            this.component = component;
            this.t = t;
            this.tawt = t.getThreadReference();
            this.debugger = debugger;
            this.sType = sType;
        }

        @Override
        public String getName() {
            return propertyName;
        }
        
        @Override
        public String getDisplayName() {
            return propertyName;
        }
        
        @Override
        public boolean canRead() {
            return getter != null;
        }
        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            synchronized (valueLock) {
                if (value == null) {
                    value = valueCalculating;
                    debugger.getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                RemoteServices.runOnStoppedThread(t, new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean[] isEditablePtr = new boolean[] { false };
                                        Type[] typePtr = new Type[] { null };
                                        String v = getValueLazy(isEditablePtr, typePtr);
                                        synchronized (valueLock) {
                                            value = v;
                                            valueIsEditable = isEditablePtr[0];
                                            valueType = typePtr[0];
                                        }
                                        ci.firePropertyChange(propertyName, null, v);
                                    }
                                }, sType);
                            } catch (PropertyVetoException ex) {
                                value = ex.getLocalizedMessage();
                            }
                        }
                    });
                }
                return value;
            }
        }
        
        private String getValueLazy(boolean[] isEditablePtr, Type[] typePtr) {
            Lock l = t.accessLock.writeLock();
            l.lock();
            try {
                Value v = component.invokeMethod(tawt, getter, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                if (v != null) {
                    typePtr[0] = v.type();
                }
                if (v instanceof StringReference) {
                    isEditablePtr[0] = true;
                    return ((StringReference) v).value();
                }
                if (v instanceof ObjectReference) {
                    Type t = v.type();
                    if (t instanceof ClassType) {
                        Method toStringMethod = ((ClassType) t).concreteMethodByName("toString", "()Ljava/lang/String;");
                        v = ((ObjectReference) v).invokeMethod(tawt, toStringMethod, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                        if (v instanceof StringReference) {
                            return ((StringReference) v).value();
                        }
                    }
                } else if (v instanceof PrimitiveValue) {
                    isEditablePtr[0] = true;
                }
                return String.valueOf(v);
            } catch (InvalidTypeException ex) {
                Exceptions.printStackTrace(ex);
                return ex.getMessage();
            } catch (ClassNotLoadedException ex) {
                Exceptions.printStackTrace(ex);
                return ex.getMessage();
            } catch (IncompatibleThreadStateException ex) {
                Exceptions.printStackTrace(ex);
                return ex.getMessage();
            } catch (final InvocationException ex) {
                final InvocationExceptionTranslated iextr = new InvocationExceptionTranslated(ex, debugger);
                iextr.setPreferredThread(t);
                /*
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        iextr.getMessage();
                        iextr.getLocalizedMessage();
                        iextr.getCause();
                        iextr.getStackTrace();
                        Exceptions.printStackTrace(iextr);
                        Exceptions.printStackTrace(ex);
                    }
                }, 100);
                 */
                //Exceptions.printStackTrace(iextr);
                //Exceptions.printStackTrace(ex);
                return iextr.getMessage();
            } finally {
                l.unlock();
            }
        }
        
       private String setValueLazy(String val, String oldValue, Type type) {
            Value v;
            VirtualMachine vm = type.virtualMachine();
            if (type instanceof PrimitiveType) {
                String ts = type.name();
                try {
                    if (Boolean.TYPE.getName().equals(ts)) {
                        v = vm.mirrorOf(Boolean.parseBoolean(val));
                    } else if (Byte.TYPE.getName().equals(ts)) {
                        v = vm.mirrorOf(Byte.parseByte(val));
                    } else if (Character.TYPE.getName().equals(ts)) {
                        if (val.length() == 0) {
                            throw new NumberFormatException("Zero length input.");
                        }
                        v = vm.mirrorOf(val.charAt(0));
                    } else if (Short.TYPE.getName().equals(ts)) {
                        v = vm.mirrorOf(Short.parseShort(val));
                    } else if (Integer.TYPE.getName().equals(ts)) {
                        v = vm.mirrorOf(Integer.parseInt(val));
                    } else if (Long.TYPE.getName().equals(ts)) {
                        v = vm.mirrorOf(Long.parseLong(val));
                    } else if (Float.TYPE.getName().equals(ts)) {
                        v = vm.mirrorOf(Float.parseFloat(val));
                    } else if (Double.TYPE.getName().equals(ts)) {
                        v = vm.mirrorOf(Double.parseDouble(val));
                    } else {
                        throw new IllegalArgumentException("Unknown type '"+ts+"'");
                    }
                    val = v.toString();
                } catch (NumberFormatException nfex) {
                    NotifyDescriptor msg = new NotifyDescriptor.Message(nfex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                    return oldValue;
                }
            } else {
                if ("java.lang.String".equals(type.name())) {
                    v = vm.mirrorOf(val);
                } else {
                    throw new IllegalArgumentException("Unknown type '"+type.name()+"'");
                }
            }
            Lock l = t.accessLock.writeLock();
            l.lock();
            try {
                component.invokeMethod(tawt, setter, Collections.singletonList(v), ObjectReference.INVOKE_SINGLE_THREADED);
                return val;
            } catch (InvalidTypeException ex) {
                Exceptions.printStackTrace(ex);
                return oldValue;
            } catch (ClassNotLoadedException ex) {
                Exceptions.printStackTrace(ex);
                return oldValue;
            } catch (IncompatibleThreadStateException ex) {
                Exceptions.printStackTrace(ex);
                return oldValue;
            } catch (final InvocationException ex) {
                final InvocationExceptionTranslated iextr = new InvocationExceptionTranslated(ex, debugger);
                iextr.setPreferredThread(t);
                
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        iextr.getMessage();
                        iextr.getLocalizedMessage();
                        iextr.getCause();
                        iextr.getStackTrace();
                        Exceptions.printStackTrace(iextr);
                        //Exceptions.printStackTrace(ex);
                    }
                }, 100);
                
                //Exceptions.printStackTrace(iextr);
                //Exceptions.printStackTrace(ex);
                return oldValue;
            } finally {
                l.unlock();
            }
        }

        @Override
        public boolean canWrite() {
            synchronized (valueLock) {
                return setter != null && valueIsEditable;
            }
        }

        @Override
        public void setValue(final Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (!(val instanceof String)) {
                throw new IllegalArgumentException("val = "+val);
            }
            final String oldValue;
            final Type type;
            synchronized (valueLock) {
                oldValue = value;
                type = valueType;
                value = valueCalculating;
            }
            debugger.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        RemoteServices.runOnStoppedThread(t, new Runnable() {
                            @Override
                            public void run() {
                                String v;
                                Throwable t = null;
                                try {
                                    v = setValueLazy((String) val, oldValue, type);
                                } catch (Throwable th) {
                                    if (th instanceof ThreadDeath) {
                                        throw (ThreadDeath) th;
                                    }
                                    t = th;
                                    v = oldValue;
                                    
                                }
                                synchronized (valueLock) {
                                    value = v;
                                }
                                ci.firePropertyChange(propertyName, null, v);
                                if (t != null) {
                                    Exceptions.printStackTrace(t);
                                }
                            }
                        }, sType);
                    } catch (PropertyVetoException ex) {
                        NotifyDescriptor msg = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                    }
                }
            });
        }
    }
    
    public static class FieldInfo {

        private String name;
        private Field f;
        private JavaComponentInfo parent;

        FieldInfo(Field f, JavaComponentInfo parent) {
            this.name = f.name();
            this.f = f;
            this.parent = parent;
        }

        public String getName() {
            return name;
        }

        public Field getField() {
            return f;
        }

        public JavaComponentInfo getParent() {
            return parent;
        }
    }
    
    private static String escapeHTML(String message) {
        if (message == null) {
            return null;
        }
        int len = message.length();
        StringBuilder result = new StringBuilder(len + 20);
        char aChar;

        for (int i = 0; i < len; i++) {
            aChar = message.charAt(i);
            switch (aChar) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                default:
                    result.append(aChar);
            }
        }
        return result.toString();
    }
}
