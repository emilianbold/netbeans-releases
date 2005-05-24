/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
