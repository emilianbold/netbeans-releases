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
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface EjbJar extends org.netbeans.modules.j2ee.dd.api.common.RootInterface {
    public static final String PROPERTY_VERSION="dd_version"; //NOI18N
    public static final String VERSION_1_1="1.1"; //NOI18N
    public static final String VERSION_2_0="2.0"; //NOI18N
    public static final String VERSION_2_1="2.1"; //NOI18N
    public static final String VERSION_3_0="3.0"; //NOI18N
    public static final int STATE_VALID=0;
    public static final int STATE_INVALID_PARSABLE=1;
    public static final int STATE_INVALID_UNPARSABLE=2;
    public static final String PROPERTY_STATUS="dd_status"; //NOI18N
    
    public static final String ENTERPRISE_BEANS = "EnterpriseBeans";	// NOI18N
    public static final String RELATIONSHIPS = "Relationships";	// NOI18N
    public static final String ASSEMBLY_DESCRIPTOR = "AssemblyDescriptor";	// NOI18N
    public static final String EJB_CLIENT_JAR = "EjbClientJar";	// NOI18N
    
    /** Setter for version property.
     * Warning : Only the upgrade from lower to higher version is supported.
     * @param version ejb-jar version value
     */
    public void setVersion(java.math.BigDecimal version);
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

    // EJB 3.0
    
    void setInterceptors(Interceptors valueInterface) throws VersionNotSupportedException;
    Interceptors getInterceptors() throws VersionNotSupportedException;
    Interceptors newInterceptors() throws VersionNotSupportedException;
    
}

