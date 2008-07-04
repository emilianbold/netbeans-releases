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
package org.netbeans.modules.bpel.model.ext.editor.xam;

import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public enum EditorAttributes implements Attribute {
    TYPE("type", GlobalType.class), //NOI18N
    PATH("path", String.class, AttrType.PATH), //NOI18N
    SOURCE("source", Source.class), //NOI18N
    PARENT_PATH("parentPath", String.class, AttrType.PATH), //NOI18N
    QNAME("qName", QName.class, AttrType.QNAME), //NOI18N
    IS_ATTRIBUTE("isAttribute", boolean.class), //NOI18N
    ;
    
    public static enum AttrType {
        STRING,
        QNAME, 
        NCNAME,
        URI,
        PATH
    }

    EditorAttributes( String name, Class type ) {
        this(name, type, (Class)null);
    }
    
    EditorAttributes( String name, Class type , AttrType attrType ) {
        this(name, type, (Class)null);
        myType = attrType;
    }

    EditorAttributes( String name, Class type, Class subtype ) {
        myAttributeName = name;
        myAttributeType = type;
        myAttributeTypeInContainer = subtype;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    /** {@inheritDoc} */
    @Override
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
        for (EditorAttributes attr : values()) {
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
