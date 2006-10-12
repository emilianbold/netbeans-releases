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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.ejb;

import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;

/**
 *
 * @author Martin Adamek
 */
public interface Interceptor {
    
    int addAroundInvoke(AroundInvoke value);
    int addDescription(String value);
    int addEjbLocalRef(EjbLocalRef valueInterface);
    int addEjbRef(EjbRef valueInterface);
    int addEnvEntry(EnvEntry valueInterface);
    int addMessageDestinationRef(MessageDestinationRef valueInterface);
    int addPersistenceContextRef(PersistenceContextRef value);
    int addPersistenceUnitRef(PersistenceUnitRef value);
    int addPostActivate(LifecycleCallback value);
    int addPostConstruct(LifecycleCallback value);
    int addPreDestroy(LifecycleCallback value);
    int addPrePassivate(LifecycleCallback value);
    int addResourceEnvRef(ResourceEnvRef valueInterface);
    int addResourceRef(ResourceRef valueInterface);
    int addServiceRef(ServiceRef valueInterface);
    AroundInvoke[] getAroundInvoke();
    AroundInvoke getAroundInvoke(int index);
    String[] getDescription();
    String getDescription(int index);
    EjbLocalRef[] getEjbLocalRef();
    EjbLocalRef getEjbLocalRef(int index);
    EjbRef[] getEjbRef();
    EjbRef getEjbRef(int index);
    EnvEntry[] getEnvEntry();
    EnvEntry getEnvEntry(int index);
    String getInterceptorClass();
    MessageDestinationRef[] getMessageDestinationRef();
    MessageDestinationRef getMessageDestinationRef(int index);
    PersistenceContextRef[] getPersistenceContextRef();
    PersistenceContextRef getPersistenceContextRef(int index);
    PersistenceUnitRef[] getPersistenceUnitRef();
    PersistenceUnitRef getPersistenceUnitRef(int index);
    LifecycleCallback[] getPostActivate();
    LifecycleCallback getPostActivate(int index);
    LifecycleCallback[] getPostConstruct();
    LifecycleCallback getPostConstruct(int index);
    LifecycleCallback[] getPreDestroy();
    LifecycleCallback getPreDestroy(int index);
    LifecycleCallback[] getPrePassivate();
    LifecycleCallback getPrePassivate(int index);
    ResourceEnvRef[] getResourceEnvRef();
    ResourceEnvRef getResourceEnvRef(int index);
    ResourceRef[] getResourceRef();
    ResourceRef getResourceRef(int index);
    ServiceRef[] getServiceRef();
    ServiceRef getServiceRef(int index);
    AroundInvoke newAroundInvoke();
    EjbLocalRef newEjbLocalRef();
    EjbRef newEjbRef();
    EnvEntry newEnvEntry();
    LifecycleCallback newLifecycleCallback();
    MessageDestinationRef newMessageDestinationRef();
    PersistenceContextRef newPersistenceContextRef();
    PersistenceUnitRef newPersistenceUnitRef();
    ResourceEnvRef newResourceEnvRef();
    ResourceRef newResourceRef();
    ServiceRef newServiceRef();
    int removeAroundInvoke(AroundInvoke value);
    int removeDescription(String value);
    int removeEjbLocalRef(EjbLocalRef valueInterface);
    int removeEjbRef(EjbRef valueInterface);
    int removeEnvEntry(EnvEntry valueInterface);
    int removeMessageDestinationRef(MessageDestinationRef valueInterface);
    int removePersistenceContextRef(PersistenceContextRef value);
    int removePersistenceUnitRef(PersistenceUnitRef value);
    int removePostActivate(LifecycleCallback value);
    int removePostConstruct(LifecycleCallback value);
    int removePreDestroy(LifecycleCallback value);
    int removePrePassivate(LifecycleCallback value);
    int removeResourceEnvRef(ResourceEnvRef valueInterface);
    int removeResourceRef(ResourceRef valueInterface);
    int removeServiceRef(ServiceRef valueInterface);
    void setAroundInvoke(int index, AroundInvoke value);
    void setAroundInvoke(AroundInvoke[] value);
    void setDescription(int index, String value);
    void setDescription(String[] value);
    void setEjbLocalRef(int index, EjbLocalRef valueInterface);
    void setEjbLocalRef(EjbLocalRef[] value);
    void setEjbRef(int index, EjbRef valueInterface);
    void setEjbRef(EjbRef[] value);
    void setEnvEntry(int index, EnvEntry valueInterface);
    void setEnvEntry(EnvEntry[] value);
    void setInterceptorClass(String value);
    void setMessageDestinationRef(int index, MessageDestinationRef valueInterface);
    void setMessageDestinationRef(MessageDestinationRef[] value);
    void setPersistenceContextRef(int index, PersistenceContextRef value);
    void setPersistenceContextRef(PersistenceContextRef[] value);
    void setPersistenceUnitRef(int index, PersistenceUnitRef value);
    void setPersistenceUnitRef(PersistenceUnitRef[] value);
    void setPostActivate(int index, LifecycleCallback value);
    void setPostActivate(LifecycleCallback[] value);
    void setPostConstruct(int index, LifecycleCallback value);
    void setPostConstruct(LifecycleCallback[] value);
    void setPreDestroy(int index, LifecycleCallback value);
    void setPreDestroy(LifecycleCallback[] value);
    void setPrePassivate(int index, LifecycleCallback value);
    void setPrePassivate(LifecycleCallback[] value);
    void setResourceEnvRef(int index, ResourceEnvRef valueInterface);
    void setResourceEnvRef(ResourceEnvRef[] value);
    void setResourceRef(int index, ResourceRef valueInterface);
    void setResourceRef(ResourceRef[] value);
    void setServiceRef(int index, ServiceRef valueInterface);
    void setServiceRef(ServiceRef[] value);
    int sizeAroundInvoke();
    int sizeDescription();
    int sizeEjbLocalRef();
    int sizeEjbRef();
    int sizeEnvEntry();
    int sizeMessageDestinationRef();
    int sizePersistenceContextRef();
    int sizePersistenceUnitRef();
    int sizePostActivate();
    int sizePostConstruct();
    int sizePreDestroy();
    int sizePrePassivate();
    int sizeResourceEnvRef();
    int sizeResourceRef();
    int sizeServiceRef();
    
}
