/*
 * ResRefBindingsType.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
            this.setAttributeValue(SPECIAL_SUBJECTS_TYPE,SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS_STRING);
        } else if(value.equals(SPECIAL_SUBJECTS_TYPE_EVERYONE)) {
            this.setAttributeValue(SPECIAL_SUBJECTS_TYPE,SPECIAL_SUBJECTS_TYPE_EVERYONE_STRING);
        } else {
            this.setAttributeValue(SPECIAL_SUBJECTS_TYPE,value);
        }
    }
    public String getType() {
        String str=(String)this.getAttributeValue(SPECIAL_SUBJECTS_TYPE);
        if(str.equals(SPECIAL_SUBJECTS_TYPE_EVERYONE_STRING)) {
            return SPECIAL_SUBJECTS_TYPE_EVERYONE;
        } else if(str.equals(SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS_STRING)) {
            return SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS;
        } else {
            return str;
        }
    }
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "PageType", this);	// NOI18N
        }
        if(getName()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "PageType", this);	// NOI18N
        }
        if(getType()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getType() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "PageType", this);	// NOI18N
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
