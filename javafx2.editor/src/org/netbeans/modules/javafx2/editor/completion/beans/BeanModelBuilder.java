/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor7;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.model.FxClassUtils;

/**
 *
 * @author sdedic
 */
public final class BeanModelBuilder {
    /**
     * Environment, which should be used for introspection
     */
    private final CompilationInfo compilationInfo;
    
    /**
     * Fully qualified class name
     */
    private final String  className;
    
    private String defaultProperty;
    
    private Set<String> dependencies = Collections.emptySet();
    
    /**
     * Properties found on the Bean
     */
    private Map<String, PropertyInfo>   allProperties = Collections.emptyMap();
    
    private Map<String, PropertyInfo>   staticProperties = Collections.emptyMap();
    
    /**
     * List of simple properties in the class
     */
    private Map<String, PropertyInfo> simpleProperties = Collections.emptyMap();
    
    /**
     * Names of factory methods usable to create the bean instance
     */
    private Set<String> factoryMethods = Collections.emptySet();
    
    private FxBeanInfo  resultInfo;
    
    /**
     * Type element for the class.
     */
    @NullAllowed
    private TypeElement classElement;

    BeanModelBuilder(CompilationInfo compilationInfo, String className) {
        this.compilationInfo = compilationInfo;
        this.className = className;
    }
    
    private void addDependency(TypeMirror tm) {
        if (tm.getKind() == TypeKind.ARRAY) {
            addDependency(((ArrayType)tm).getComponentType());
        } else if (tm.getKind() == TypeKind.WILDCARD) {
            WildcardType wt = (WildcardType)tm;
            TypeMirror bound = wt.getSuperBound();
            if (bound == null) {
                bound = wt.getExtendsBound();
            }
            addDependency(bound);
        } else if (tm.getKind() == TypeKind.DECLARED) {
            addDependency(
                ((TypeElement)compilationInfo.getTypes().asElement(tm)).getQualifiedName().toString()
            );
        }
    }
    
    private void addDependency(String name) {
        if (dependencies.isEmpty()) {
            dependencies = new HashSet<String>();
        }
        dependencies.add(name);
    }
    
    public FxBeanInfo process() {
        classElement = compilationInfo.getElements().getTypeElement(className);
        FxBeanInfo declared = resultInfo = new FxBeanInfo(className);
        resultInfo.setJavaType(ElementHandle.create(classElement));
        inspectMembers();
        // try to find default property
        resultInfo.setProperties(allProperties);
        resultInfo.setSimpleProperties(simpleProperties);
        resultInfo.setAttachedProperties(staticProperties);
        resultInfo.setEvents(events);
        resultInfo.setFactoryNames(factoryMethods);
        resultInfo.setValueOf(FxClassUtils.findValueOf(classElement, compilationInfo) != null);
        String defaultProperty = FxClassUtils.getDefaultProperty(classElement);
        resultInfo.setDefaultPropertyName(defaultProperty);

        FxBeanInfo merge = new FxBeanInfo(className);
        merge.setDeclaredInfo(resultInfo);
        
        resultInfo = merge;
        collectSuperClass(classElement.getSuperclass());
        resultInfo.setParentBeanInfo(superBi);
        resultInfo.merge(declared);

        
        // add to the bean cache:
        if (beanCache != null) {
            beanCache.addBeanInfo(compilationInfo.getClasspathInfo(), resultInfo, dependencies);
        }
        return resultInfo;
    }
    
    public FxBeanInfo getBeanInfo() {
        return resultInfo;
    }
    
    TypeElement getClassElement() {
        return classElement;
    }
    
    private static final String SET_NAME_PREFIX = "set";
    private static final int SET_NAME_PREFIX_LEN = 3;
    private static final String GET_NAME_PREFIX = "get";
    private static final int GET_NAME_PREFIX_LEN = 3;
    
    private String getPropertyName(String setterName) {
        return Character.toLowerCase(setterName.charAt(SET_NAME_PREFIX_LEN)) + setterName.substring(SET_NAME_PREFIX_LEN + 1);
    }
    
    private boolean consumed;
    
    private Collection<ExecutableElement> getters = new ArrayList<ExecutableElement>();
    
    private void addCandidateROProperty(ExecutableElement m) {
        if (consumed) {
            return;
        }
        String name = m.getSimpleName().toString();
        if (name.length() > GET_NAME_PREFIX_LEN && name.startsWith(GET_NAME_PREFIX)) {
            String n = getPropertyName(name);
            if (m.getParameters().isEmpty()) {
                getters.add(m);
            }
        }
    }
    
    private static final String LIST_CLASS = "java.util.List";
    
    private static final String MAP_CLASS = "java.util.Map";
    
    
    private void processGetters() {
        // Check presence !
        TypeMirror listType = compilationInfo.getElements().getTypeElement(LIST_CLASS).asType();
        TypeMirror mapType = compilationInfo.getElements().getTypeElement(MAP_CLASS).asType();
        for (ExecutableElement m : getters) {
            String n = getPropertyName(m.getSimpleName().toString());
            if (allProperties.containsKey(n)) {
                continue;
            }
            TypeMirror retType = m.getReturnType();
            TypeMirror erasure = compilationInfo.getTypes().erasure(retType);
            if (compilationInfo.getTypes().isAssignable(erasure, listType)) {
                addListProperty(m, n);
            } else if (compilationInfo.getTypes().isAssignable(erasure, mapType)) {
                addMapProperty(m, n);
            }
        }
    }
    
    private void addAttachedProperty(ExecutableElement m) {
        if (consumed) {
            return;
        }
        String name = m.getSimpleName().toString();
        if (!name.startsWith(SET_NAME_PREFIX) || name.length() == SET_NAME_PREFIX_LEN ||
             !Character.isUpperCase(name.charAt(SET_NAME_PREFIX_LEN))) {
            return;
        }
        if (!isStatic(m)) {
            return;
        }
        if (m.getParameters().size() != 2) {
            return;
        }

        // setWhateverProperty(attachedObject, value)
        TypeMirror objectType = m.getParameters().get(0).asType();
        TypeMirror paramType = m.getParameters().get(1).asType();
        
        TypeElement objectTypeEl = (TypeElement)compilationInfo.getTypes().asElement(objectType);
        boolean simple = FxClassUtils.isSimpleType(paramType, compilationInfo);
        // analysis depends ont he paramType contents:
        addDependency(paramType);
        PropertyInfo pi = new PropertyInfo(getPropertyName(name), PropertyInfo.Kind.ATTACHED);
        pi.setSimple(simple);
        pi.setType(TypeMirrorHandle.create(paramType));
        pi.setAccessor(ElementHandle.create(m));
        
        // setup the discovered object type
        pi.setObjectType(ElementHandle.create(objectTypeEl));
        
        if (staticProperties.isEmpty()) {
            staticProperties = new HashMap<String, PropertyInfo>();
        }
        staticProperties.put(pi.getName(), pi);
        
        consumed = true;
    }

    private ExecutableElement mapGetMethod = null;
    private ExecutableElement listGetMethod = null;
    
    private ExecutableType findMapGetMethod(DeclaredType inType) {
        TypeElement mapClass = compilationInfo.getElements().getTypeElement(MAP_CLASS);

        if (mapGetMethod == null) {
            for (ExecutableElement mm : ElementFilter.methodsIn(mapClass.getEnclosedElements())) {
                if (mm.getSimpleName().toString().equals("get")) { // NOI18N
                    mapGetMethod = mm;
                }
            }
        }
        return (ExecutableType)compilationInfo.getTypes().asMemberOf(inType, mapGetMethod);
    }

    private ExecutableType findListGetMethod(DeclaredType inType) {
        TypeElement mapClass = compilationInfo.getElements().getTypeElement(LIST_CLASS);

        if (listGetMethod == null) {
            for (ExecutableElement mm : ElementFilter.methodsIn(mapClass.getEnclosedElements())) {
                if (mm.getSimpleName().toString().equals("get")) { // NOI18N
                    listGetMethod = mm;
                }
            }
        }
        return (ExecutableType)compilationInfo.getTypes().asMemberOf(inType, listGetMethod);
    }
    
    private void addMapProperty(ExecutableElement m, String propName) {
        PropertyInfo pi = new PropertyInfo(propName, PropertyInfo.Kind.MAP);
        pi.setSimple(false);
        pi.setAccessor(ElementHandle.create(m));
        
        // must extract type arguments; assume there's a DeclaredType
        DeclaredType t = ((DeclaredType)m.getReturnType());
        ExecutableType getterType = findMapGetMethod(t);
        
        pi.setType(TypeMirrorHandle.create(getterType.getReturnType()));
        
        registerProperty(pi);
    }

    private void addListProperty(ExecutableElement m, String propName) {
        PropertyInfo pi = new PropertyInfo(propName, PropertyInfo.Kind.LIST);
        pi.setSimple(false);
        pi.setAccessor(ElementHandle.create(m));
        
        // must extract type arguments; assume there's a DeclaredType
        DeclaredType t = ((DeclaredType)m.getReturnType());
        ExecutableType getterType = findListGetMethod(t);
        
        pi.setType(TypeMirrorHandle.create(getterType.getReturnType()));
        
        registerProperty(pi);
    }

    /**
     * Checks if the method represents a simple setter property. If so, it creates
     * and registers the appropriate PropertyInfo
     * @param m 
     */
    private void addProperty(ExecutableElement m) {
        if (consumed) {
            return;
        }
        String name = m.getSimpleName().toString();
        if (!name.startsWith(SET_NAME_PREFIX) || name.length() == SET_NAME_PREFIX_LEN ||
             !Character.isUpperCase(name.charAt(SET_NAME_PREFIX_LEN))) {
            return;
        }

        // check number of parameters:
        if (m.getParameters().size() != 1) {
            return;
        }

        TypeMirror paramType = m.getParameters().get(0).asType();
        boolean simple = FxClassUtils.isSimpleType(paramType, compilationInfo);
        addDependency(paramType);
        PropertyInfo pi = new PropertyInfo(getPropertyName(name), PropertyInfo.Kind.SETTER);
        pi.setSimple(simple);
        pi.setType(TypeMirrorHandle.create(paramType));
        pi.setAccessor(ElementHandle.create(m));
        
        registerProperty(pi);
        if (simple) {
            if (simpleProperties.isEmpty()) {
                simpleProperties = new HashMap<String, PropertyInfo>();
            }
            simpleProperties.put(pi.getName(), pi);
        }
        consumed = true;
    }
    
    private void registerProperty(PropertyInfo pi) {
        if (allProperties.isEmpty()) {
            allProperties = new HashMap<String, PropertyInfo>();
        }
        allProperties.put(pi.getName(), pi);
    }
    
    /**
     * Checks whether the method is a factory method for the class,
     * and if so, adds its name to the list
     */
    private void addFactoryMethod(ExecutableElement m) {
        if (consumed) {
            return;
        }
        if (!isStatic(m)) {
            return;
        }
        if (!m.getParameters().isEmpty()) {
            return;
        }
        // the method must return the type itself:
        if (!compilationInfo.getTypes().isSameType(
                m.getReturnType(), classElement.asType())) {
            return;
        }
        
        if (factoryMethods.isEmpty()) {
            factoryMethods = new HashSet<String>();
        }
        factoryMethods.add(m.getSimpleName().toString());
        consumed = true;
    }
    
    private static final String EVENT_PREFIX = "setOn";
    private static final int EVENT_PREFIX_LEN = 5;
    private static final String ANNOTATION_TYPE_FXML = "javax.fxml.beans.FXML";
    
    
    private boolean isStatic(ExecutableElement m) {
         return m.getModifiers().contains(Modifier.STATIC);
    }
    
    private boolean isAccessible(ExecutableElement m) {
        if (!m.getModifiers().contains(Modifier.PUBLIC)) {
            for (AnnotationMirror am : m.getAnnotationMirrors()) {
                String atype = ((TypeElement)am.getAnnotationType().asElement()).getQualifiedName().toString();
                if (ANNOTATION_TYPE_FXML.equals(atype)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }
    /** 
     * Accessible symbols are either public, or annotated with FXML.
     */
    private boolean isAccessible(ExecutableElement m, boolean classMethod) {
        return isAccessible(m) && m.getModifiers().contains(Modifier.STATIC) == classMethod;
    }
    
    private static final String JAVAFX_EVENT_BASE = "javafx.event.EventHandler"; // NOI18N
    
    private TypeMirror  eventHandlerBase;
    
    private TypeMirror  getHandlerBaseType() {
        if (eventHandlerBase == null) {
            TypeElement el = compilationInfo.getElements().getTypeElement(JAVAFX_EVENT_BASE);
            if (el == null) {
                // FIXME - better exception, must be catched & reported outside
                throw new IllegalStateException();
            }
            eventHandlerBase = el.asType();
        }
        return eventHandlerBase;
    }
    
    private void addEventSource(ExecutableElement m) {
        if (consumed) {
            return;
            
        }
        String sn = m.getSimpleName().toString();
        
        if (!isAccessible(m, false)) {
            return;
        }
        
        if (!sn.startsWith(EVENT_PREFIX) || sn.length() == EVENT_PREFIX_LEN) {
            return;
        }
        
        if (m.getParameters().size() != 1) {
            return;
        }
        VariableElement param = m.getParameters().get(0);
        TypeMirror varType = param.asType();
        
        // the type must be assignable to the event handler
        if (compilationInfo.getTypes().isAssignable(varType, getHandlerBaseType())) {
            return;
        }
        ElementHandle<TypeElement> eventHandle = null;
        String eventClassName = null;
        
        if (varType.getKind() == TypeKind.DECLARED) {
            // extract event type as the type of event / argument for the event handler method
            DeclaredType dt = (DeclaredType)varType;
            List<? extends TypeMirror> tParams = dt.getTypeArguments();
            if (tParams.size() != 1) {
                // something very wrong, the event handler has just 1 type parameter
                throw new IllegalStateException();
            }
            TypeMirror eventType = tParams.get(0);
            if (eventType.getKind() == TypeKind.WILDCARD) {
                TypeMirror t = ((WildcardType)eventType).getSuperBound();
                if (t == null) {
                    t = ((WildcardType)eventType).getExtendsBound();
                }
                eventType = t;
            }
            if (eventType.getKind() != TypeKind.DECLARED) {
                throw new IllegalStateException();
            }
            TypeElement te = (TypeElement)compilationInfo.getTypes().asElement(eventType);
            eventClassName = te.getQualifiedName().toString();
            eventHandle = ElementHandle.create(te);
            addDependency(eventType);
        }

        String eventName = Character.toLowerCase(sn.charAt(EVENT_PREFIX_LEN)) + sn.substring(EVENT_PREFIX_LEN + 1);
        EventSourceInfo ei = new EventSourceInfo(eventName);
        ei.setEventClassName(eventClassName);
        ei.setEventType(eventHandle);
        
        if (events.isEmpty()) {
            events = new HashMap<String, EventSourceInfo>();
        }
        events.put(ei.getName(), ei);
        
        consumed = true;
    }
    
    private Map<String, EventSourceInfo>    events = Collections.emptyMap();
    
    private FxBeanCache beanCache;
    
    private void inspectMembers() {
        List<ExecutableElement> methods = ElementFilter.methodsIn(classElement.getEnclosedElements());
        
        for (ExecutableElement m :methods) {
            if (!isAccessible(m)) {
                continue;
            }
            consumed = false;

            // event sources (except for on* property types)
            addEventSource(m);

            // instance properties
            addProperty(m);
            // factory methods
            addFactoryMethod(m);
            // attached properties
            addAttachedProperty(m);
            
            addCandidateROProperty(m);
        }
        // add list and map properties, which have no corresponding setter and are r/o
        processGetters();
    }

   void setBeanCache(FxBeanCache beanCache) {
        this.beanCache = beanCache;
    }
    
    private static final String JAVA_LANG_OBJECT = "java.lang.Object"; // NOI18N
    
    private FxBeanInfo superBi;
    
    /**
     * Collects information from superclasses. Creates an additional instance of
     * BeanModelBuilder to get all the information.
     */
    private void collectSuperClass(TypeMirror superT) {
        if (superT == null) {
            return;
        }
        TypeElement elem = (TypeElement)compilationInfo.getTypes().asElement(superT);
        String fqn = elem.getQualifiedName().toString();
        if (JAVA_LANG_OBJECT.equals(fqn)) {
            return;
        }
        addDependency(fqn);
        superBi = null;
        if (beanCache != null) {
            superBi = beanCache.getBeanInfo(compilationInfo.getClasspathInfo(), fqn);
        }
        if (superBi == null) {
            BeanModelBuilder builder = new BeanModelBuilder(compilationInfo, fqn);
            builder.setBeanCache(beanCache);
            superBi = builder.process();
        }
        resultInfo.merge(superBi);
    }
    
    public static FxBeanInfo getBeanInfo(CompilationInfo ci, String className) {
        if (className == null) {
            return null;
        }
        FxBeanInfo bi = FxBeanCache.instance().getBeanInfo(ci.getClasspathInfo(), className);
        if (bi != null) {
            return bi;
        }
        BeanModelBuilder bld = new BeanModelBuilder(ci, className);
        bld.setBeanCache(FxBeanCache.instance());
        return bld.process();
    }
}
