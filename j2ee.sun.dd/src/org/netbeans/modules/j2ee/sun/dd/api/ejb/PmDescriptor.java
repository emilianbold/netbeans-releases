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
 * PmDescriptor.java
 *
 * Created on November 18, 2004, 10:36 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface PmDescriptor extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String PM_IDENTIFIER = "PmIdentifier";	// NOI18N
    public static final String PM_VERSION = "PmVersion";	// NOI18N
    public static final String PM_CONFIG = "PmConfig";	// NOI18N
    public static final String PM_CLASS_GENERATOR = "PmClassGenerator";	// NOI18N
    public static final String PM_MAPPING_FACTORY = "PmMappingFactory";	// NOI18N
        
    /** Setter for pm-identifier property
     * @param value property value
     */
    public void setPmIdentifier(java.lang.String value);
    /** Getter for pm-identifier property.
     * @return property value
     */
    public java.lang.String getPmIdentifier();
    /** Setter for pm-version property
     * @param value property value
     */
    public void setPmVersion(java.lang.String value);
    /** Getter for pm-version property.
     * @return property value
     */
    public java.lang.String getPmVersion();
    /** Setter for pm-config property
     * @param value property value
     */
    public void setPmConfig(java.lang.String value);
    /** Getter for pm-config property.
     * @return property value
     */
    public java.lang.String getPmConfig();
    /** Setter for pm-class-generator property
     * @param value property value
     */
    public void setPmClassGenerator(java.lang.String value);
    /** Getter for pm-class-generator property.
     * @return property value
     */
    public java.lang.String getPmClassGenerator();
    /** Setter for pm-mapping-factory property
     * @param value property value
     */
    public void setPmMappingFactory(java.lang.String value);
    /** Getter for pm-mapping-factory property.
     * @return property value
     */
    public java.lang.String getPmMappingFactory();
    
}
