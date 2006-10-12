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

import org.netbeans.modules.j2ee.dd.api.common.InjectionTarget;

/**
 *
 * @author Martin Adamek
 */
public interface PersistenceContextRef {
    
    int addDescription(String value);
    int addInjectionTarget(InjectionTarget value);
    int addPersistenceProperty(Property value);
    String[] getDescription();
    String getDescription(int index);
    InjectionTarget[] getInjectionTarget();
    InjectionTarget getInjectionTarget(int index);
    String getMappedName();
    String getPersistenceContextRefName();
    String getPersistenceContextType();
    Property[] getPersistenceProperty();
    Property getPersistenceProperty(int index);
    String getPersistenceUnitName();
    InjectionTarget newInjectionTarget();
    Property newProperty();
    int removeDescription(String value);
    int removeInjectionTarget(InjectionTarget value);
    int removePersistenceProperty(Property value);
    void setDescription(int index, String value);
    void setDescription(String[] value);
    void setInjectionTarget(int index, InjectionTarget value);
    void setInjectionTarget(InjectionTarget[] value);
    void setMappedName(String value);
    void setPersistenceContextRefName(String value);
    void setPersistenceContextType(String value);
    void setPersistenceProperty(int index, Property value);
    void setPersistenceProperty(Property[] value);
    void setPersistenceUnitName(String value);
    int sizeDescription();
    int sizeInjectionTarget();
    int sizePersistenceProperty();
    
}
