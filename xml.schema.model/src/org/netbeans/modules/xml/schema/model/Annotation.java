/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
