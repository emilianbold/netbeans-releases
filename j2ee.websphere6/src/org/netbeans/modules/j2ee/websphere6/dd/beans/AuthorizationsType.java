
package org.netbeans.modules.j2ee.websphere6.dd.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class AuthorizationsType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {
    
    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    
    static public final String ROLE   = "Role";	// NOI18N
    static public final String GROUPS = "Groups";	// NOI18N
    static public final String ROLE_HREF   = "RoleHref";
    static public final String GROUPS_XMI_ID = "GroupsXmiId";
    static public final String GROUPS_NAME   = "GroupsName";
    
    public AuthorizationsType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    
    public AuthorizationsType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(2);
        this.createProperty("role", 	// NOI18N
                ROLE,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(ROLE, HREF_ID, ROLE_HREF,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        this.createProperty("groups", 	// NOI18N
                GROUPS,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        
        this.createAttribute(GROUPS, XMI_ID_ID, GROUPS_XMI_ID,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(GROUPS, NAME_ID,  GROUPS_NAME ,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        
        this.initialize(options);
    }
    
    // Setting the default values of the properties
    void initialize(int options) {
        
    }
    void setDefaults(){
        setHref("SOME_ROLE");
        setXmiId("Groups_");
        setName("some");
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
    
    //
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }
    
    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    
    // This attribute is optional
    public void setHref(java.lang.String value) {        
        setAttributeValue(ROLE, ROLE_HREF,DDXmi.addAppHrefToId(value));
    }
    
    public String getHref() {
        return DDXmi.getIdFromHref((String)getAttributeValue(ROLE, ROLE_HREF));        
    }
    
    public void setXmiId(java.lang.String value) {
        setAttributeValue(GROUPS, GROUPS_XMI_ID,value);
    }
    
    public String getXmiId() {
        return (String)getAttributeValue(GROUPS, GROUPS_XMI_ID);
    }
    
    public void setName(java.lang.String value) {
        setAttributeValue(GROUPS, GROUPS_NAME,value);
    }
    
    public String getName() {
        return (String)getAttributeValue(GROUPS, GROUPS_NAME);
    }
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        boolean restrictionFailure = false;
        boolean restrictionPassed = false;
        // Validating propertys
        if(getHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "role", this);	// NOI18N
        }
        if(getName()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "groups", this);	// NOI18N
        }
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "groups", this);	// NOI18N
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

