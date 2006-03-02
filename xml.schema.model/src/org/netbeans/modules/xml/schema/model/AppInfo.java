/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.schema.model;

import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public interface AppInfo extends SchemaComponent {
    public static final String SOURCE_PROPERTY = "source";
    public static final String CONTENT_PROPERTY = "content";
    
    String getURI();
    void setURI(String uri);
    
    Element getAppInfoElement();
    void setAppInfoElement(Element content);
    
}
