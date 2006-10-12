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

package org.netbeans.modules.j2ee.dd.api.common;

/**
 * Generated interface for ResourceEnvRef element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface ResourceEnvRef extends CommonDDBean, DescriptionInterface {


    public static final String RESOURCE_ENV_REF_NAME = "ResourceEnvRefName"; // NOI18N
    public static final String RESOURCE_ENV_REF_TYPE = "ResourceEnvRefType"; // NOI18N

    /** Setter for resource-env-ref-name property 
     * @param value property value
     */
    public void setResourceEnvRefName(String value);
    /** Getter for resource-env-ref-name property.
     * @return property value 
     */
    public String getResourceEnvRefName();
    /** Setter for resource-env-ref-type property.
     * @param value property value
     */
    public void setResourceEnvRefType(String value);
    /** Getter for resource-env-ref-type property.
     * @return property value 
     */
    public String getResourceEnvRefType();

    // Java EE 5
        
    void setMappedName(String value) throws VersionNotSupportedException;
    String getMappedName() throws VersionNotSupportedException;
    void setInjectionTarget(int index, InjectionTarget valueInterface) throws VersionNotSupportedException;
    InjectionTarget getInjectionTarget(int index) throws VersionNotSupportedException;
    int sizeInjectionTarget() throws VersionNotSupportedException;
    void setInjectionTarget(InjectionTarget[] value) throws VersionNotSupportedException;
    InjectionTarget[] getInjectionTarget() throws VersionNotSupportedException;
    int addInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException;
    int removeInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException;
    InjectionTarget newInjectionTarget() throws VersionNotSupportedException;

}
