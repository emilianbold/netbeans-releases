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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.model.ext.logging.xam;

import org.netbeans.modules.bpel.model.ext.logging.api.Alert;
import org.netbeans.modules.bpel.model.ext.logging.api.AlertLevel;
import org.netbeans.modules.bpel.model.ext.logging.api.Log;
import org.netbeans.modules.bpel.model.ext.logging.api.LogLevel;
import org.netbeans.modules.bpel.model.ext.logging.api.Location;


import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author ads
 */
public enum LoggingAttributes implements Attribute {
    LOCATION(Log.LOCATION, Location.class),
    LOG_LEVEL(Log.LEVEL, LogLevel.class),
    ALERT_LEVEL(Alert.LEVEL, AlertLevel.class),
    ;
    
    public static enum AttrType {
        STRING,
        NCNAME,
        URI,
        VARIABLE
    }

    LoggingAttributes( String name, Class type ) {
        this(name, type, (Class)null);
    }
    
    LoggingAttributes( String name, Class type , AttrType attrType ) {
        this(name, type, (Class)null);
        myType = attrType;
    }

    LoggingAttributes( String name, Class type, Class subtype ) {
        myAttributeName = name;
        myAttributeType = type;
        myAttributeTypeInContainer = subtype;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    /** {@inheritDoc} */
    public String toString() {
        return myAttributeName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.Attribute#getType()
     */
    /** {@inheritDoc} */
    public Class getType() {
        return myAttributeType;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.Attribute#getName()
     */
    /** {@inheritDoc} */
    public String getName() {
        return myAttributeName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.Attribute#getMemberType()
     */
    /** {@inheritDoc} */
    public Class getMemberType() {
        return myAttributeTypeInContainer;
    }
    
    /**
     * @return type of attribute value
     */
    public AttrType getAttributeType(){
        return myType;
    }
    
    /**
     * @param name String representation of enum.
     * @return Enum that have <code>name</code> representaion.
     */
    public static Attribute forName( String name ){
        for (LoggingAttributes attr : values()) {
            if ( attr.getName().equals(name) &&
                    attr.getType().equals( String.class) )
            {
                return attr;
            }
        }
        return null;
    }

    private String myAttributeName;

    private Class myAttributeType;

    private Class myAttributeTypeInContainer;
    
    private AttrType myType;
}
