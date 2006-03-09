

package org.netbeans.modules.j2ee.websphere6.dd.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class AuthorizationTableType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {
    
    static Vector comparators = new Vector();
    static private final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    
    static private final String AUTHORIZATION = "Authorization";	// NOI18N
    static private final String AUTH_ID="Id";
    
    public AuthorizationTableType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    
    public AuthorizationTableType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(1);
        this.createProperty("authorizations", 	// NOI18N
                AUTHORIZATION,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                AuthorizationsType.class);
        this.createAttribute(AUTHORIZATION, XMI_ID_ID, AUTH_ID,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.initialize(options);
    }
    
    // Setting the default values of the properties
    void initialize(int options) {
        
        setId("RoleAssignement_");
    }
    
    // This attribute is an array, possibly empty
    public void setAuthorization(int index, AuthorizationsType value) {
        this.setValue(AUTHORIZATION, index, value);
    }
    
    //
    public AuthorizationsType getAuthorization(int index) {
        return (AuthorizationsType)this.getValue(AUTHORIZATION, index);
    }
    
    // Return the number of properties
    public int sizeAuthorization() {
        return this.size(AUTHORIZATION);
    }
    
    // This attribute is an array, possibly empty
    public void setAuthorization(AuthorizationsType[] value) {
        this.setValue(AUTHORIZATION, value);
    }
    
    //
    public AuthorizationsType[] getAuthorization() {
        return (AuthorizationsType[])this.getValues(AUTHORIZATION);
    }
    
    // Add a new element returning its index in the list
    public int addAuthorization(AuthorizationsType value) {
        int positionOfNewItem = this.addValue(AUTHORIZATION, value);
        return positionOfNewItem;
    }
    
    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeAuthorization(AuthorizationsType value) {
        return this.removeValue(AUTHORIZATION, value);
    }
    
    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public AuthorizationsType newApplicationType() {
        return new AuthorizationsType();
    }
    
    public void setId(String value) {
        this.setAttributeValue(AUTHORIZATION,AUTH_ID,value);
    }
    public String getId() {
        return (String)getAttributeValue(AUTHORIZATION,AUTH_ID);
    }
    //
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }
    
    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        boolean restrictionFailure = false;
        boolean restrictionPassed = false;
        // Validating property parameter
        for (int _index = 0; _index < sizeAuthorization(); ++_index) {
            AuthorizationsType element = getAuthorization(_index);
            if (element != null) {
                element.validate();
            }
        }
        if(getId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "authorizations", this);	// NOI18N
        }
    }
    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("Authorizations["+this.sizeAuthorization()+"]");	// NOI18N
        for(int i=0; i<this.sizeAuthorization(); i++) {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (org.netbeans.modules.schema2beans.BaseBean) this.getAuthorization(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(AUTHORIZATION, i, str, indent);
        }
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }}

// END_NOI18N

