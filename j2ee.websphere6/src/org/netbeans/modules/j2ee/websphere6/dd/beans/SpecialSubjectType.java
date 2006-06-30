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
package org.netbeans.modules.j2ee.websphere6.dd.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
/**
 *
 * @author dlm198383
 */
public class SpecialSubjectType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {

    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);



    public SpecialSubjectType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public SpecialSubjectType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(1);
        this.initialize(options);
    }
    public void initialize(int options) {
        
    }
    public void setDefaults() {
        setXmiId("Special_");
        setName(SPECIAL_SUBJECTS_TYPE_EVERYONE_STRING+"_");
        setType(SPECIAL_SUBJECTS_TYPE_EVERYONE);
    }
    
    
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }
    
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    
    public void setXmiId(String value) {
        this.setAttributeValue(SPECIAL_SUBJECTS_XMI_ID,value);
    }
    
    public String getXmiId() {
        return (String)this.getAttributeValue(SPECIAL_SUBJECTS_XMI_ID);
    }
    public void setName(String value) {
        this.setAttributeValue(SPECIAL_SUBJECTS_NAME,value);
    }
    public String getName() {
        return (String)this.getAttributeValue(SPECIAL_SUBJECTS_NAME);
    }
    public void setType(String value) {
        if(value.equals(SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS)) {
            this.setAttributeValue(SPECIAL_SUBJECTS_TYPE,
                    SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS_STRING);
        } else if(value.equals(SPECIAL_SUBJECTS_TYPE_EVERYONE)) {
            this.setAttributeValue(SPECIAL_SUBJECTS_TYPE,
                    SPECIAL_SUBJECTS_TYPE_EVERYONE_STRING);
        } else {
            this.setAttributeValue(SPECIAL_SUBJECTS_TYPE,value);
        }
    }
    public String getType() {
        String str=(String)this.getAttributeValue(SPECIAL_SUBJECTS_TYPE);
        if(str==null) {
            return null;
        } else if(str.equals(SPECIAL_SUBJECTS_TYPE_EVERYONE_STRING)) {
            return SPECIAL_SUBJECTS_TYPE_EVERYONE;
        } else if(str.equals(SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS_STRING)) {
            return SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS;
        } else {
            return str;
        }
    }
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "SpecialSubjectType", this);	// NOI18N
        }
        if(getName()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "SpecialSubjectType", this);	// NOI18N
        }
        if(getType()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getType() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "SpecialSubjectType", this);	// NOI18N
        }
    }
    
    
    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append(SPECIAL_SUBJECTS);
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
