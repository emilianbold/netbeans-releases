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
package org.netbeans.modules.j2ee.persistence.entitygenerator;

/**
 * This class represents an instance of an member in an entity bean class which
 * is backed by a ColumnElement representing a relational database.
 * @author Christopher Webster
 */

import org.netbeans.modules.dbschema.ColumnElement;
import org.netbeans.modules.dbschema.util.SQLTypeUtil;

class DbSchemaEntityMember extends EntityMember {

   /**
    * is this member part of primary key
    */
    private boolean isPrimaryKey;

    /**
     * Original mapping to sql type
     */
    private SQLType sqlType;

    /**
     * Column Element providing metadata
     */
    private ColumnElement columnElement;

    public DbSchemaEntityMember(ColumnElement element) {
        columnElement = element;
        sqlType = SQLType.getSQLType(element.getType());
        setMemberName(makeFieldName(element.getName().getName()));
	isPrimaryKey = false;
        setMemberType(sqlType.getMemberType(element));
    }

    public boolean isNullable() {
        return columnElement.isNullable();
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean isPk, boolean isPkField) {
        isPrimaryKey = isPk;

        // this is relevant for CMP 2.1 and earlier where a pk field is not 
        // allowed to be of a primitive type, so the first corresponding class  
        // if extracted from the sqlType
        if (isPkField) {
            setMemberType(getRespectiveNonPrimitiveType());
        }
    }

    /**
     * Tries to get the respective non-primitive type for the type of
     * this member. In other words, gets the respective wrapper class for 
     * the member type if it is a primitive <code>int, long, short, byte, 
     * double, float or char</code>, otherwise 
     * returns <code>sqlType#getFirstNonPrimitiveType</code>.
     */ 
    private String getRespectiveNonPrimitiveType(){
        String type = getMemberType();
        if ("int".equals(type)){//NO18N
            return Integer.class.getName();
        } else if ("long".equals(type)){//NO18N
            return Long.class.getName();
        } else if ("short".equals(type)){//NO18N
            return Short.class.getName();
        } else if ("byte".equals(type)){//NO18N
            return Byte.class.getName();
        } else if ("double".equals(type)){//NO18N
            return Double.class.getName();
        } else if ("float".equals(type)){//NO18N
            return Float.class.getName();
        } else if ("char".equals(type)){//NO18N
            return Character.class.getName();
        }
        return sqlType.getFirstNonPrimitiveType();
    }
    
    private ColumnElement getColumnElement() {
        return columnElement;
    }
    
    public boolean supportsFinder() {
        return sqlType.supportsFinder();
    }
    
    public String getColumnName() {
        return getColumnElement().getName().getName();
    }
    
    public String getTableName() {
        return getColumnElement().getDeclaringTable().getName().getName();
    }

    public boolean isLobType() {
        return SQLTypeUtil.isLob(getColumnElement().getType());
    }
    
}
