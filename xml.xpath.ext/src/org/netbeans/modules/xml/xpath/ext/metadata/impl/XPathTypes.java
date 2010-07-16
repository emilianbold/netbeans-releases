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

package org.netbeans.modules.xml.xpath.ext.metadata.impl;

import org.netbeans.modules.xml.xpath.ext.metadata.XPathMetadataUtils;
import java.util.ArrayList;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType.XPathSchemaType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 *
 * @author nk160297
 */
public abstract class XPathTypes {

    public static final class XPathStringType extends XPathSchemaType {
        public XPathStringType() {
            super(XPathMetadataUtils.findPrimitiveType("string")); // NOI18N
        }
        
        @Override
        public MetaType getMetaType() {
            return MetaType.STRING;
        }
        
        
        @Override
        public String getName() {
            return "String"; // NOI18N
        }

        @Override
        public boolean isAssignableFrom(XPathType otherType) {
            if (otherType instanceof XPathStringType) { 
                return true;
            } else if (otherType instanceof XPathDateTimeType) { 
                return true;
            } else if (otherType instanceof XPathSchemaType) {
                SchemaComponent otherSType = 
                        ((XPathSchemaType)otherType).getSchemaType();
                if (otherSType == mSchemaType) {
                    return true;
                }
                //
                if (XPathMetadataUtils.isTypeDerived(mSchemaType, otherSType)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        
    }
    
    public static final class XPathBooleanType extends XPathSchemaType {
        public XPathBooleanType() {
            super(XPathMetadataUtils.findPrimitiveType("boolean")); // NOI18N
        }
        
        @Override
        public MetaType getMetaType() {
            return MetaType.BOOLEAN;
        }
        
        
        @Override
        public String getName() {
            return "Boolean"; // NOI18N
        }
        
        @Override
        public boolean isAssignableFrom(XPathType otherType) {
            if (otherType instanceof XPathBooleanType) { 
                return true;
            } else if (otherType instanceof XPathSchemaType) {
                SchemaComponent otherSType = 
                        ((XPathSchemaType)otherType).getSchemaType();
                if (otherSType == mSchemaType) {
                    return true;
                }
                //
                if (XPathMetadataUtils.isTypeDerived(mSchemaType, otherSType)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }
    
    public static final class XPathNumberType implements XPathType {
        
        private ArrayList<GlobalType> mNumberTypes;
        
        public XPathNumberType() {
            mNumberTypes = new ArrayList<GlobalType>();
            //
            mNumberTypes.add(XPathMetadataUtils.findPrimitiveType("decimal")); // NOI18N
            mNumberTypes.add(XPathMetadataUtils.findPrimitiveType("double")); // NOI18N
            mNumberTypes.add(XPathMetadataUtils.findPrimitiveType("float")); // NOI18N
            mNumberTypes.add(XPathMetadataUtils.findPrimitiveType("hexBinary")); // NOI18N
        }
        
        public MetaType getMetaType() {
            return MetaType.NUMBER;
        }

        public String getName() {
            return "Number"; // NOI18N
        }

        public boolean isAssignableFrom(XPathType otherType) {
            if (otherType instanceof XPathNumberType) {
                return true;
            } else if (otherType instanceof XPathSchemaType) {
                SchemaComponent otherSType = 
                        ((XPathSchemaType)otherType).getSchemaType();
                for (GlobalType myAllowedType : mNumberTypes) {
                    if (otherSType == myAllowedType) {
                        return true;
                    }
                    //
                    if (XPathMetadataUtils.isTypeDerived(myAllowedType, otherSType)) {
                        return true;
                    }
                }
                //
                return false;
            } else {
                return false;
            }
        }
    }
    
    public static final class XPathNodeSetType implements XPathType {
        
        public MetaType getMetaType() {
            return MetaType.NODE_SET;
        }

        public String getName() {
            return "Node Set"; // NOI18N
        }

        public boolean isAssignableFrom(XPathType otherType) {
            if (otherType instanceof XPathNodeSetType || 
                    otherType instanceof XPathNodeType) { 
                // One node can be considered as a set with only one element
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static final class XPathNodeType implements XPathType {
        
        public MetaType getMetaType() {
            return MetaType.NODE;
        }

        public String getName() {
            return "Node"; // NOI18N
        }

        public boolean isAssignableFrom(XPathType otherType) {
            if (otherType instanceof XPathNodeType) {
                // A Node Set can't be considered as a Node because 
                // in many cases it can be treated as a mistake. 
                return true;
            } else {
                return false;
            }
        }
    }
    
    // See Issue #162495
    public static final class XPathSubjectType implements XPathType {
        
        public MetaType getMetaType() {
            return MetaType.SUBJECT;
        }

        public String getName() {
            return "Subject"; // NOI18N
        }

        public boolean isAssignableFrom(XPathType otherType) {
            if (otherType instanceof XPathSubjectType) {
                // A Node Set can't be considered as a Node because 
                // in many cases it can be treated as a mistake. 
                return true;
            } else {
                return false;
            }
        }
    }

    public static final class XPathAnyType implements XPathType {
        
        public MetaType getMetaType() {
            return MetaType.ANY_TYPE;
        }

        public String getName() {
            return "Any Type"; // NOI18N
        }

        public boolean isAssignableFrom(XPathType otherType) {
            return true;
        }
    }
    
    public static final class XPathDateTimeType extends XPathSchemaType {
        
        public XPathDateTimeType() {
            super(XPathMetadataUtils.findPrimitiveType("string")); // NOI18N
        }
        
        @Override
        public MetaType getMetaType() {
            return MetaType.STRING;
        }
        
        
        @Override
        public String getName() {
            return "Date/Time"; // NOI18N
        }

        @Override
        public boolean isAssignableFrom(XPathType otherType) {
            if (otherType instanceof XPathDateTimeType) { 
                return true;
            } else if (otherType instanceof XPathStringType) { 
                return true;
            } else if (otherType instanceof XPathSchemaType) {
                SchemaComponent otherSType = 
                        ((XPathSchemaType)otherType).getSchemaType();
                if (otherSType == mSchemaType) {
                    return true;
                }
                //
                if (XPathMetadataUtils.isTypeDerived(mSchemaType, otherSType)) {
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
