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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;

/**
 *
 * @author sdedic
 */
public final class FxBeanInfo {
    private String  className;
    
    /**
     * Contains the 'value-of' method
     */
    private boolean hasValueOf;
    
    /**
     * The Typeelement for the bean
     */
    private ElementHandle<TypeElement>  javaType;
    
    /**
     * Properties available on this class
     */
    private Map<String, PropertyInfo>    simpleProperties = Collections.emptyMap();
    
    private Map<String, PropertyInfo>    properties =  Collections.emptyMap();
    
    /**
     * Attached properties
     */
    private Map<String, PropertyInfo>   attachedProperties =  Collections.emptyMap();
    
    /**
     * Custom events fired from the object
     */
    private Map<String, EventSourceInfo>    events =  Collections.emptyMap();

    /**
     * Names of factory methods
     */
    private Set<String> factoryNames = Collections.emptySet();

    private FxBeanInfo parentInfo;

    /**
     * BeanInfo, which only contains declarations present on the class itself,
     * does not include parents.
     */
    private FxBeanInfo declaredInfo;
    
    private String defaultPropertyName;

    FxBeanInfo(String className) {
        this.className = className;
    }
    
    void setValueOf(boolean has) {
        this.hasValueOf = has;
    }
    
    public boolean hasValueOf() {
        return hasValueOf;
    }
    
    void setDefaultPropertyName(String propName) {
        this.defaultPropertyName = propName;
    }

    public PropertyInfo getDefaultProperty() {
        return defaultPropertyName == null ? null : properties.get(defaultPropertyName);
    }
    
    public Set<String> getFactoryNames() {
        return Collections.unmodifiableSet(factoryNames);
    }

    void setFactoryNames(Set<String> factoryNames) {
        this.factoryNames = factoryNames;
    }
    
    public String getClassName() {
        return className;
    }

    public ElementHandle<TypeElement> getJavaType() {
        return javaType;
    }

    public Map<String, PropertyInfo> getProperties() {
        return properties;
    }
    
    public Set<String> getPropertyNames() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    public Map<String, PropertyInfo> getSimpleProperties() {
        return properties;
    }

    public Collection<String> getSimplePropertyNames() {
        return Collections.unmodifiableSet(simpleProperties.keySet());
    }
    
    public Collection<String> getAttachedPropertyNames() {
        return Collections.unmodifiableSet(attachedProperties.keySet());
    }
    
    public PropertyInfo getProperty(String n) {
        return properties.get(n);
    }
    
    public PropertyInfo getSimpleProperty(String n) {
        return simpleProperties.get(n);
    }
    
    public PropertyInfo getAttachedProperty(String n) {
        return attachedProperties.get(n);
    }

    public Map<String, PropertyInfo> getAttachedProperties() {
        return attachedProperties;
    }

    public Map<String, EventSourceInfo> getEvents() {
        return events;
    }

    void setJavaType(ElementHandle<TypeElement> javaType) {
        this.javaType = javaType;
    }

    void setProperties(Map<String, PropertyInfo> properties) {
        this.properties = properties;
    }

    void setSimpleProperties(Map<String, PropertyInfo> properties) {
        this.simpleProperties = properties;
    }

    void setAttachedProperties(Map<String, PropertyInfo> attachedProperties) {
        this.attachedProperties = attachedProperties;
    }

    void setEvents(Map<String, EventSourceInfo> events) {
        this.events = events;
    }
    
    void setParentBeanInfo(FxBeanInfo parent) {
        this.parentInfo = parent;
    }
    
    public FxBeanInfo getParentBeanInfo() {
        return parentInfo;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BeanInfo[");
        sb.append("\n  className: ").append(getClassName()).
                append("; default: ").append(getDefaultProperty()).
                append("; value: ").append(hasValueOf).
                append("\n factories: ").append(getFactoryNames());
        sb.append("\n properties: ").append("\n");
        appendMap(sb, getProperties());
        sb.append("\n events: ").append("\n");
        appendMap(sb, getEvents());
        
        return sb.toString();
    }
    
    private void appendMap(StringBuilder sb, Map m) {
        ArrayList al = new ArrayList(m.keySet());
        Collections.sort(al);
        
        for (Object o : al) {
            Object v = m.get(o);
            sb.append("    ").append(v).append("\n");
        }
    }
    
    /**
     * Service, which provides FxBeanInfo for the given FQN.
     */
    public interface Provider {
        public FxBeanInfo getBeanInfo(String fqn);
    }
    
    public FxBeanInfo getDeclareadInfo() {
        return declaredInfo;
    }
    
    void setDeclaredInfo(FxBeanInfo declaredInfo) {
        this.declaredInfo = declaredInfo;
    }
    
    void merge(FxBeanInfo superBi) {
        if (superBi == null) {
            return;
        }
        
        if (attachedProperties.isEmpty() && !superBi.getAttachedProperties().isEmpty()) {
            attachedProperties = new HashMap<String, PropertyInfo>(superBi.getAttachedProperties());
        } else {
            attachedProperties.putAll(superBi.getAttachedProperties());
        }
        if (properties.isEmpty() && !superBi.getProperties().isEmpty()) {
            properties = new HashMap<String, PropertyInfo>(superBi.getProperties());
        } else {
            properties.putAll(superBi.getProperties());
        }
        if (simpleProperties.isEmpty() && !superBi.getSimpleProperties().isEmpty()) {
            simpleProperties = new HashMap<String, PropertyInfo>(superBi.getSimpleProperties());
        } else {
            simpleProperties.putAll(superBi.getSimpleProperties());
        }
        if (events.isEmpty() && !superBi.getEvents().isEmpty()) {
            events = new HashMap<String, EventSourceInfo>(superBi.getEvents());
        } else {
            events.putAll(superBi.getEvents());
        }
        
        defaultPropertyName = superBi.getDefaultProperty() == null ? null : superBi.getDefaultProperty().getName();
    }
}
