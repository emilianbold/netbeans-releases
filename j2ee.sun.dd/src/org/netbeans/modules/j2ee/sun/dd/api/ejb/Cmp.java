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
/*
 * Cmp.java
 *
 * Created on November 17, 2004, 5:11 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface Cmp extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String MAPPING_PROPERTIES = "MappingProperties";	// NOI18N
    public static final String IS_ONE_ONE_CMP = "IsOneOneCmp";	// NOI18N
    public static final String ONE_ONE_FINDERS = "OneOneFinders";	// NOI18N
    public static final String PREFETCH_DISABLED = "PrefetchDisabled";	// NOI18N
    
    /** Setter for mapping-properties property
     * @param value property value
     */
    public void setMappingProperties(java.lang.String value);
    /** Getter for mapping-properties property.
     * @return property value
     */
    public java.lang.String getMappingProperties();
    /** Setter for is-one-one-cmp property
     * @param value property value
     */
    public void setIsOneOneCmp(java.lang.String value);
    /** Getter for is-one-one-cmp property.
     * @return property value
     */
    public java.lang.String getIsOneOneCmp();
    /** Setter for one-one-finders property
     * @param value property value
     */
    public void setOneOneFinders(OneOneFinders value);
    /** Getter for one-one-finders property.
     * @return property value
     */
    public OneOneFinders getOneOneFinders(); 
     
    public OneOneFinders newOneOneFinders();
    
    //AppServer 8.1
    /** Setter for prefetch-disabled property
     * @param value property value
     */ 
    public void setPrefetchDisabled (PrefetchDisabled  value) throws VersionNotSupportedException; 
    /** Getter for prefetch-disabled property.
     * @return property value
     */
    public PrefetchDisabled getPrefetchDisabled () throws VersionNotSupportedException;  
    
    public PrefetchDisabled newPrefetchDisabled () throws VersionNotSupportedException;  
}
