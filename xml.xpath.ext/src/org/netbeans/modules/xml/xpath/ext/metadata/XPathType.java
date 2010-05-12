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

package org.netbeans.modules.xml.xpath.ext.metadata;

import org.netbeans.modules.xml.xpath.ext.metadata.impl.XPathTypes;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Named;

/**
 * Represents XPath types.
 * The content of theis fils is draft!
 * 
 * @author nk160297
 */
public interface XPathType {

    String getName();
    
    MetaType getMetaType();
    
    boolean isAssignableFrom(XPathType otherType);
    
    //==========================================================================
    // Constants
    
    XPathType STRING_TYPE = new XPathTypes.XPathStringType();
    XPathType BOOLEAN_TYPE = new XPathTypes.XPathBooleanType();
    XPathType NUMBER_TYPE = new XPathTypes.XPathNumberType();
    XPathType NODE_SET_TYPE = new XPathTypes.XPathNodeSetType();
    XPathType NODE_TYPE = new XPathTypes.XPathNodeType();
    XPathType SUBJECT_TYPE = new XPathTypes.XPathSubjectType();
    XPathType ANY_TYPE = new XPathTypes.XPathAnyType();
    XPathType DATE_TIME_TYPE = new XPathTypes.XPathDateTimeType();
    
    //==========================================================================

    enum MetaType {
        NODE_SET, 
        NODE,
        SUBJECT,
        STRING, 
        NUMBER, 
        BOOLEAN, 
        ANY_SCHEMA_TYPE, 
        ANY_TYPE;
    }
    
    class XPathSchemaType implements XPathType {
        
        protected SchemaComponent mSchemaType;
        
        public XPathSchemaType(SchemaComponent sType) {
            assert sType instanceof GlobalType || sType instanceof LocalType : 
                "sType parameter can be either local or global type";
            mSchemaType = sType;
        }

        public String getName() {
            return ((Named)mSchemaType).getName();
        }

        public MetaType getMetaType() {
            return MetaType.ANY_SCHEMA_TYPE;
        }
        
        public SchemaComponent getSchemaType() {
            return mSchemaType;
        }

        public boolean isAssignableFrom(XPathType otherType) {
            if (otherType instanceof XPathSchemaType) {
                SchemaComponent otherSType = 
                        ((XPathSchemaType)otherType).getSchemaType();
                if (otherSType == mSchemaType) {
                    return true;
                }
                //
                if (XPathMetadataUtils.isTypeDerived(mSchemaType, otherSType) || 
                        XPathMetadataUtils.isTypeDerived(otherSType, mSchemaType)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        
    }
}
