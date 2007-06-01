/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.impl.common.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.TypeAnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;

/**
 * Utility methods mainly for getting annotations.
 * This is a helper class; all methods are static.
 * @author Tomas Mysik, Martin Adamek
 */
public class CommonAnnotationHelper {
    
    // see JSR250
    private static final Set<String> RESOURCE_REF_TYPES = new HashSet<String>(Arrays.<String>asList(
            "javax.sql.DataSource",
            "javax.jms.ConnectionFactory",
            "javax.jms.QueueConnectionFactory",
            "javax.jms.TopicConnectionFactory",
            "javax.mail.Session",
            "java.net.URL",
            "javax.resource.cci.ConnectionFactory",
            "org.omg.CORBA_2_3.ORB"
            // any other connection factory defined by a resource adapter
            ));
    private static final Set<String> ENV_ENTRY_TYPES = new HashSet<String>(Arrays.<String>asList(
            "java.lang.String",
            "java.lang.Character",
            "java.lang.Integer",
            "java.lang.Boolean",
            "java.lang.Double",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Long",
            "java.lang.Float"));
    private static final Set<String> SERVICE_REF_TYPES = new HashSet<String>(Arrays.<String>asList(
            "javax.xml.rpc.Service",
            "javax.xml.ws.Service",
            "javax.jws.WebService"));
    private static final Set<String> MESSAGE_DESTINATION_TYPES = new HashSet<String>(Arrays.<String>asList(
            "javax.jms.Queue",
            "javax.jms.Topic"));
    
    private CommonAnnotationHelper() {
    }
    
    public static SecurityRole[] getSecurityRoles(AnnotationModelHelper helper) {
        final List<SecurityRole> result = new ArrayList<SecurityRole>();
        final AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectStringArray("value", new ArrayValueHandler() { // NOI18N
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    String value = (String) arrayMember.getValue();
                    result.add(new SecurityRoleImpl(value));
                }
                return null;
            }
        }, null);
        helper.getAnnotationScanner().findAnnotatedTypes("javax.annotation.security.DeclareRoles", new TypeAnnotationHandler() { // NOI18N
            public void typeAnnotation(TypeElement typeElement, AnnotationMirror annotation) {
                parser.parse(annotation);
            }
        });
        return result.toArray(new SecurityRole[result.size()]);
    }
    
    /**
     * Get all {@link ResourceRef}s for given class.
     * @param helper        annotation model helper.
     * @param typeElement   class that is searched.
     * @return              all found {@link ResourceRef}s.
     */
    public static ResourceRef[] getResourceRefs(final AnnotationModelHelper helper, final TypeElement typeElement) {
        assert helper != null;
        assert typeElement != null;
        
        List<ResourceImpl> resources = getResources(helper, typeElement);
        return getResourceRefs(resources);
    }
    
    /**
     * Get all {@link ResourceRef}s
     * for given classpath (via given annotation model helper).
     * @param helper    annotation model helper.
     * @return          all found {@link ResourceRef}s.
     */
    public static ResourceRef[] getResourceRefs(final AnnotationModelHelper helper) {
        assert helper != null;
        
        List<ResourceImpl> resources = getResources(helper);
        return getResourceRefs(resources);
    }
    
    /**
     * Get all {@link ResourceEnvRef}s for given class.
     * @param helper        annotation model helper.
     * @param typeElement   class that is searched.
     * @return              all found {@link ResourceEnvRef}s.
     */
    public static ResourceEnvRef[] getResourceEnvRefs(final AnnotationModelHelper helper, final TypeElement typeElement) {
        assert helper != null;
        assert typeElement != null;
        
        List<ResourceImpl> resources = getResources(helper, typeElement);
        return getResourceEnvRefs(resources);
    }
    
    /**
     * Get all {@link ResourceEnvRef}s
     * for given classpath (via given annotation model helper).
     * @param helper    annotation model helper.
     * @return          all found {@link ResourceEnvRef}s.
     */
    public static ResourceEnvRef[] getResourceEnvRefs(final AnnotationModelHelper helper) {
        assert helper != null;
        
        List<ResourceImpl> resources = getResources(helper);
        return getResourceEnvRefs(resources);
    }
    
    /**
     * Get all {@link EnvEntry EnvEntries} for given class.
     * @param helper        annotation model helper.
     * @param typeElement   class that is searched.
     * @return              all found {@link EnvEntry}s.
     */
    public static EnvEntry[] getEnvEntries(final AnnotationModelHelper helper, final TypeElement typeElement) {
        assert helper != null;
        assert typeElement != null;
        
        List<ResourceImpl> resources = getResources(helper, typeElement);
        return getEnvEntries(resources);
    }
    
    /**
     * Get all {@link EnvEntry EnvEntries}
     * for given classpath (via given annotation model helper).
     * @param helper    annotation model helper.
     * @return          all found {@link EnvEntry}s.
     */
    public static EnvEntry[] getEnvEntries(final AnnotationModelHelper helper) {
        assert helper != null;
        
        List<ResourceImpl> resources = getResources(helper);
        return getEnvEntries(resources);
    }
    
    /**
     * Get all {@link MessageDestinationRef}s for given class.
     * @param helper        annotation model helper.
     * @param typeElement   class that is searched.
     * @return              all found {@link MessageDestinationRef}s.
     */
    public static MessageDestinationRef[] getMessageDestinationRefs(final AnnotationModelHelper helper, final TypeElement typeElement) {
        assert helper != null;
        assert typeElement != null;
        
        List<ResourceImpl> resources = getResources(helper, typeElement);
        return getMessageDestinationRefs(resources);
    }
    
    /**
     * Get all {@link MessageDestinationRef}s
     * for given classpath (via given annotation model helper).
     * @param helper    annotation model helper.
     * @return          all found {@link MessageDestinationRef}s.
     */
    public static MessageDestinationRef[] getMessageDestinationRefs(final AnnotationModelHelper helper) {
        assert helper != null;
        
        List<ResourceImpl> resources = getResources(helper);
        return getMessageDestinationRefs(resources);
    }
    
    private static List<ResourceImpl> getResources(final AnnotationModelHelper helper, final TypeElement typeElement) {
        
        final List<ResourceImpl> result = new ArrayList<ResourceImpl>();
        
        // fields
        for (VariableElement field : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (helper.hasAnnotation(field.getAnnotationMirrors(), "javax.annotation.Resource")) { //NOI18N
                ResourceImpl resource = new ResourceImpl(field, typeElement, helper);
                result.add(resource);
            }
        }
        // methods
        for (ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (helper.hasAnnotation(method.getAnnotationMirrors(), "javax.annotation.Resource")) { //NOI18N
                ResourceImpl resource = new ResourceImpl(method, typeElement, helper);
                result.add(resource);
            }
        }
        return result;
    }
    
    private static List<ResourceImpl> getResources(final AnnotationModelHelper helper) {
        assert helper != null;
        
        final List<ResourceImpl> result = new ArrayList<ResourceImpl>();
        helper.getAnnotationScanner().findAnnotations(
                
                "javax.annotation.Resource", // NOI18N
                EnumSet.of(ElementKind.CLASS, ElementKind.METHOD, ElementKind.FIELD),new AnnotationHandler() {
                    public void handleAnnotation(TypeElement typeElement, Element element, AnnotationMirror annotation) {
                        ResourceImpl resource = new ResourceImpl(element, typeElement, helper);
                        result.add(resource);
                    }
                });
        return result;
    }
    
    private static ResourceRef[] getResourceRefs(final List<ResourceImpl> resources) {
        List<ResourceRef> elements = new ArrayList<ResourceRef>(resources.size());
        
        for (ResourceImpl resource : resources) {
            if (RESOURCE_REF_TYPES.contains(resource.getType())) {
                elements.add(new ResourceRefImpl(resource));
            }
        }
        return elements.toArray(new ResourceRef[elements.size()]);
    }
    
    private static EnvEntry[] getEnvEntries(final List<ResourceImpl> resources) {
        List<EnvEntry> elements = new ArrayList<EnvEntry>(resources.size());
        
        for (ResourceImpl resource : resources) {
            if (ENV_ENTRY_TYPES.contains(resource.getType())) {
                elements.add(new EnvEntryImpl(resource));
            }
        }
        return elements.toArray(new EnvEntry[elements.size()]);
    }
    
    private static MessageDestinationRef[] getMessageDestinationRefs(final List<ResourceImpl> resources) {
        List<MessageDestinationRef> elements = new ArrayList<MessageDestinationRef>(resources.size());
        
        for (ResourceImpl resource : resources) {
            if (MESSAGE_DESTINATION_TYPES.contains(resource.getType())) {
                elements.add(new MessageDestinationRefImpl(resource));
            }
        }
        return elements.toArray(new MessageDestinationRef[elements.size()]);
    }
    
    private static ResourceEnvRef[] getResourceEnvRefs(final List<ResourceImpl> resources) {
        List<ResourceEnvRef> elements = new ArrayList<ResourceEnvRef>(resources.size());
        
        for (ResourceImpl resource : resources) {
            if (!RESOURCE_REF_TYPES.contains(resource.getType())
                    && !ENV_ENTRY_TYPES.contains(resource.getType())
                    && !MESSAGE_DESTINATION_TYPES.contains(resource.getType())
                    && !SERVICE_REF_TYPES.contains(resource.getType())) {
                elements.add(new ResourceEnvRefImpl(resource));
            }
        }
        return elements.toArray(new ResourceEnvRef[elements.size()]);
    }
}
