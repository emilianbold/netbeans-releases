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

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;

/**
 * Represents the schema annotation component, <xs:annotation>.
 * An example:
 *    <xs:annotation>
 *       <xs:documentation>A type for experts only</xs:documentation>
 *       <xs:appinfo>
 *         <fn:specialHandling>checkForPrimes</fn:specialHandling>
 *       </xs:appinfo>
 *    </xs:annotation>
 * See http://www.w3.org/TR/2004/REC-xmlschema-1-20041028/structures.html#cAnnotations.
 *
**/
public interface Annotation extends SchemaComponent {
	
	public static final String DOCUMENTATION_PROPERTY = "documentation";
	public static final String APPINFO_PROPERTY = "appinfo";
        
    /**
     * Adds the given Documentation to this Annotation
     * @param documentation the documentation to add to this Annotation
    **/
    public void addDocumentation(Documentation documentation);

    /**
     * Removes the given Documentation from this Annotation
     * @param documentation the Documentation to remove
    **/
    public void removeDocumentation(Documentation documentation);
    
    /**
     * Returns an enumeration of all documentation elements for this Annotation
     * @return an enumeration of all documentation elements for this Annotation
    **/
    public Collection<Documentation> getDocumentations();
    
    Collection<AppInfo> getAppInfos();
    void addAppInfo(AppInfo appInfo);
    void removeAppInfo(AppInfo appInfo);
    
} //-- Annotation
