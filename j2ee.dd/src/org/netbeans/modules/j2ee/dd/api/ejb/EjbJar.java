/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.dd.api.ejb;

/**
 * Generated interface for EjbJar element.<br>
 * The EjbJar object is the root of bean graph generated<br>
 * for deployment descriptor(ejb-jar.xml) file.<br>
 * For getting the root (EjbJar object) use the {@link DDProvider#getDDRoot} method.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
// 
// This interface has all of the bean info accessor methods.
// 
import org.netbeans.api.web.dd.common.VersionNotSupportedException;

public interface EjbJar extends org.netbeans.api.web.dd.common.RootInterface {
    public static final String PROPERTY_VERSION="dd_version"; //NOI18N
    public static final String VERSION_1_1="1.1"; //NOI18N
    public static final String VERSION_2_0="2.0"; //NOI18N
    public static final String VERSION_2_1="2.1"; //NOI18N
    public static final int STATE_VALID=0;
    public static final int STATE_INVALID_PARSABLE=1;
    public static final int STATE_INVALID_UNPARSABLE=2;
    public static final String PROPERTY_STATUS="dd_status"; //NOI18N
    
    public static final String ENTERPRISE_BEANS = "EnterpriseBeans";	// NOI18N
    public static final String RELATIONSHIPS = "Relationships";	// NOI18N
    public static final String ASSEMBLY_DESCRIPTOR = "AssemblyDescriptor";	// NOI18N
    public static final String EJB_CLIENT_JAR = "EjbClientJar";	// NOI18N
        
    //public void setVersion(java.lang.String value);
    /** Getter for version property.
     * @return property value
     */
    public java.math.BigDecimal getVersion();
    /** Getter for SAX Parse Error property.
     * Used when deployment descriptor is in invalid state.
     * @return property value or null if in valid state
     */
    public org.xml.sax.SAXParseException getError();
    /** Getter for status property.
     * @return property value
     */
    public int getStatus();
    
    public void setEnterpriseBeans(EnterpriseBeans value);

    public EnterpriseBeans getEnterpriseBeans();
    
    public EnterpriseBeans newEnterpriseBeans();
    
    public void setRelationships(Relationships value);
    
    public Relationships getSingleRelationships();
    
    public Relationships newRelationships();
    
    public void setAssemblyDescriptor(AssemblyDescriptor value);
    
    public AssemblyDescriptor getSingleAssemblyDescriptor();
    
    public AssemblyDescriptor newAssemblyDescriptor();
    
    public void setEjbClientJar(String value);
    
    public String getSingleEjbClientJar();  
           
    
}

