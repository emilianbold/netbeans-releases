/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class AuthorizationsType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {
    
    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    
    
    public AuthorizationsType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    
    public AuthorizationsType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(4);
        this.createProperty(ROLE_ID,
                ROLE,
                Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(ROLE, HREF_ID, ROLE_HREF,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        this.createProperty(GROUPS_ID,
                GROUPS,
                Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        
        this.createAttribute(GROUPS, XMI_ID_ID, GROUPS_XMI_ID,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(GROUPS, NAME_ID,  GROUPS_NAME ,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        this.createProperty(USERS_ID,
                USERS,
                Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        
        this.createAttribute(USERS, XMI_ID_ID, USERS_XMI_ID,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(USERS, NAME_ID,  USERS_NAME ,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        this.createProperty(SPECIAL_SUBJECTS_ID,
                SPECIAL_SUBJECTS,
                Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY,
                SpecialSubjectType.class);
        
        this.createAttribute(SPECIAL_SUBJECTS, XMI_ID_ID, SPECIAL_SUBJECTS_XMI_ID,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(SPECIAL_SUBJECTS, XMI_TYPE_ID,  SPECIAL_SUBJECTS_TYPE,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(SPECIAL_SUBJECTS, NAME_ID, SPECIAL_SUBJECTS_NAME,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        
        
        
        this.initialize(options);
    }
    
    // Setting the default values of the properties
    void initialize(int options) {
        
    }
    public void setDefaults(){
        String time_id="_"+java.lang.System.currentTimeMillis();
        setRoleHref("SOME_ROLE");
        setXmiId(AUTHORIZATION+time_id);
        SpecialSubjectType sst=new SpecialSubjectType();        
        sst.setType(SPECIAL_SUBJECTS_TYPE_EVERYONE);
        sst.setXmiId(SPECIAL_SUBJECTS_TYPE_EVERYONE + time_id);
        sst.setName(SPECIAL_SUBJECTS_TYPE_EVERYONE);
        setSpecialSubjects(sst);
        ;
    }
    
    public void setXmiId(String value) {
        this.setAttributeValue(AUTH_ID,value);
    }
    public String getXmiId() {
        return (String)getAttributeValue(AUTH_ID);
    }
    
    // This attribute is mandatory
    public void setRole(java.lang.String value) {
        this.setValue(ROLE, value);
    }
    
    //
    public java.lang.String getRole() {
        return (java.lang.String)this.getValue(ROLE);
    }
    
    // This attribute is optional
    public void setGroups(java.lang.String value) {
        this.setValue(GROUPS, value);
    }
    
    //
    public java.lang.String getGroups() {
        return (java.lang.String)this.getValue(GROUPS);
    }
    
    // This attribute is optional
    public void setUsers(java.lang.String value) {
        this.setValue(USERS, value);
    }
    
    //
    public java.lang.String getUsers() {
        return (java.lang.String)this.getValue(USERS);
    }
    
    //
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }
    
    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    
    // This attribute is optional
    public void setRoleHref(java.lang.String value) {
        if(getRole()==null) {
            setRole("");
        }
        setAttributeValue(ROLE, ROLE_HREF,DDXmi.addAppHrefToId(value));
    }
    
    public String getRoleHref() {
        if(getRole()==null) {
            return null;
        }
        return DDXmi.getIdFromHref((String)getAttributeValue(ROLE, ROLE_HREF));
    }
    
    public void setGroupsXmiId(java.lang.String value) {
        if(getGroups()==null) {
            setGroups("");
        }
        setAttributeValue(GROUPS, GROUPS_XMI_ID,value);
    }
    
    public String getGroupsXmiId() {
        if(getGroups()==null) {
            return null;
        }
        return (String)getAttributeValue(GROUPS, GROUPS_XMI_ID);
    }
    
    public void setGroupsName(java.lang.String value) {
        if(getGroups()==null) {
            setGroups("");
        }
        setAttributeValue(GROUPS, GROUPS_NAME,value);
    }
    
    public String getGroupsName() {
        if(getGroups()==null) {
            return null;
        }
        return (String)getAttributeValue(GROUPS, GROUPS_NAME);
    }
    
    
    public void setUsersXmiId(java.lang.String value) {
        if(getUsers()==null) {
            setUsers("");
        }
        setAttributeValue(USERS, USERS_XMI_ID,value);
    }
    
    public String getUsersXmiId() {
        if(getUsers()==null) {
            return null;
        }
        return (String)getAttributeValue(USERS, USERS_XMI_ID);
    }
    
    public void setUsersName(java.lang.String value) {
        if(getUsers()==null) {
            setUsers("");
        }
        setAttributeValue(USERS, USERS_NAME,value);
    }
    
    public String getUsersName() {
        if(getUsers()==null) {
            return null;
        }
        return (String)getAttributeValue(USERS, USERS_NAME);
    }
    
    
    // functions for manupulation SpecialSubjects
    public void setSpecialSubjects(SpecialSubjectType value) {
        this.setValue(SPECIAL_SUBJECTS,value);
    }
    
    public SpecialSubjectType getSpecialSubjects() {
        return (SpecialSubjectType) this.getValue(SPECIAL_SUBJECTS);
    }
    
    public int sizeSpecialSubjects() {
        return this.size(SPECIAL_SUBJECTS);
    }
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        boolean restrictionFailure = false;
        boolean restrictionPassed = false;
        
         if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, AUTHORIZATIONS, this);	// NOI18N
        }
        if(getRoleHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getRoleHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROLE, this);	// NOI18N
        }
        if(getGroups()!=null){
            if(getGroupsName()==null) {
                throw new org.netbeans.modules.schema2beans.ValidateException("getGroupsName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, GROUPS, this);	// NOI18N
            }
            if(getGroupsXmiId()==null) {
                throw new org.netbeans.modules.schema2beans.ValidateException("getGroupsXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, GROUPS, this);	// NOI18N
            }
        }
        if(getUsers()!=null){
            if(getUsersName()==null) {
                throw new org.netbeans.modules.schema2beans.ValidateException("getUsersName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, USERS, this);	// NOI18N
            }
            if(getUsersXmiId()==null) {
                throw new org.netbeans.modules.schema2beans.ValidateException("getUsersXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, USERS, this);	// NOI18N
            }
        }
         if(getSpecialSubjects()!=null){
            getSpecialSubjects().validate();
        }
        
        
    }
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("Role");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getRole();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(ROLE, 0, str, indent);
        
        str.append(indent);
        str.append("Groups");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getGroups();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(GROUPS, 0, str, indent);
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }}

// END_NOI18N

