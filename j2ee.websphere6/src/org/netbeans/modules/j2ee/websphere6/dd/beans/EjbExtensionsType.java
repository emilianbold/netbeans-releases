/*
 * EjbBindingsType.java
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
public class EjbExtensionsType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {
    
    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    
    
    
    
    public EjbExtensionsType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public EjbExtensionsType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(2);
        
        this.createProperty("enterpriseBean", 	// NOI18N
                ENTERPRISE_BEAN,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        
        this.createAttribute(ENTERPRISE_BEAN, HREF_ID, ENTERPRISE_BEAN_HREF,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(ENTERPRISE_BEAN, XMI_TYPE_ID, ENTERPRISE_BEAN_TYPE,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        this.createProperty("localTransaction", //NOI18N
                LOCAL_TRANSACTION,
                Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(LOCAL_TRANSACTION, XMI_ID_ID, LOCAL_TRANSACTION_XMI_ID,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(LOCAL_TRANSACTION,"unresolvedAction",LOCAL_TRANSACTION_UNRESOLVED_ACTION,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(LOCAL_TRANSACTION,"boundary",LOCAL_TRANSACTION_BOUNDARY,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(LOCAL_TRANSACTION,"resolver",LOCAL_TRANSACTION_RESOLVER,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        
        this.initialize(options);
    }
    public void initialize(int options) {
        
    }
    public void setDefaults(){
        setEnterpriseBean("");
        setHref("");        
    }
    
    public void setEnterpriseBean(String value) {
        this.setValue(ENTERPRISE_BEAN, value);
    }
    
    public String getEnterpriseBean() {
        return ( String)this.getValue(ENTERPRISE_BEAN);
    }
    
    //
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }
    
    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    
    public void setHref(java.lang.String value) {
        setAttributeValue(ENTERPRISE_BEAN, ENTERPRISE_BEAN_HREF,DDXmi.addEjbJarHrefToId(value));
        
    }
    
    public String getHref() {        
        return DDXmi.getIdFromHref((String)getAttributeValue(ENTERPRISE_BEAN, ENTERPRISE_BEAN_HREF));
    }
    
    public void setXmiType(java.lang.String value) {
        setAttributeValue(ENTERPRISE_BEAN, ENTERPRISE_BEAN_TYPE,value);
    }
    
    public String getXmiType() {
        return (String)getAttributeValue(ENTERPRISE_BEAN, ENTERPRISE_BEAN_TYPE);
    }
    
    public void setXmiId(String value)  {
        setAttributeValue(EJB_EXTENSIONS_XMI_ID,value);
    }
    
    public String getXmiId(){
        return (String)getAttributeValue(EJB_EXTENSIONS_XMI_ID);
    }
    
    
    public void setEjbExtensionsType(String value)  {
        setAttributeValue(EJB_EXTENSIONS_XMI_TYPE,value);
    }
    
    public String getEjbExtensionsType(){
        return (String)getAttributeValue(EJB_EXTENSIONS_XMI_TYPE);
    }
    public void setXmiName(String value)  {
        setAttributeValue(EJB_EXTENSIONS_XMI_NAME,value);
    }
    
    public String getXmiName(){
        return (String)getAttributeValue(EJB_EXTENSIONS_XMI_NAME);
    }
    
    public String getLocalTransaction() {
        return (String)this.getValue(LOCAL_TRANSACTION);
    }
    public void setLocalTransaction(String value) {
        this.setValue(LOCAL_TRANSACTION,value);
    }
    
    
    public void setLocalTransactionXmiId(String value){
        this.setAttributeValue(LOCAL_TRANSACTION, LOCAL_TRANSACTION_XMI_ID,value);
    }
    public String getLocalTransactionXmiId(){
        return (String)this.getAttributeValue(LOCAL_TRANSACTION, LOCAL_TRANSACTION_XMI_ID);
    }
    
    public void setLocalTransactionUnresolvedAction(String value){
        this.setAttributeValue(LOCAL_TRANSACTION, LOCAL_TRANSACTION_UNRESOLVED_ACTION,value);
    }
    public String getLocalTransactionUnresolvedAction(){
        return (String)this.getAttributeValue(LOCAL_TRANSACTION, LOCAL_TRANSACTION_UNRESOLVED_ACTION);
    }
    
    public void setLocalTransactionResolver(String value){
        this.setAttributeValue(LOCAL_TRANSACTION, LOCAL_TRANSACTION_RESOLVER,value);
    }
    public String getLocalTransactionResolver(){
        return (String)this.getAttributeValue(LOCAL_TRANSACTION, LOCAL_TRANSACTION_RESOLVER);
    }
    
     public void setLocalTransactionBoundary(String value){
        this.setAttributeValue(LOCAL_TRANSACTION, LOCAL_TRANSACTION_BOUNDARY,value);
    }
    public String getLocalTransactionBoundary(){
        return (String)this.getAttributeValue(LOCAL_TRANSACTION, LOCAL_TRANSACTION_BOUNDARY);
    }
    
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if(getEnterpriseBean()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getEnterpriseBean() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ENTERPRISE_BEAN, this);	// NOI18N
        }
        if(getHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ENTERPRISE_BEAN, this);	// NOI18N
        }
        if(getXmiType()==null)     {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiType() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ENTERPRISE_BEAN, this);	// NOI18N
        }
        
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, EJB_EXTENSIONS, this);	// NOI18N
        }
        if(getXmiName()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiName == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, EJB_EXTENSIONS, this);	// NOI18N
        }
        if(getEjbExtensionsType()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getEjbExtensionsType == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, EJB_EXTENSIONS, this);	// NOI18N
        }
        if(getLocalTransaction()!=null) {
            if(getLocalTransactionXmiId()==null)
                throw new org.netbeans.modules.schema2beans.ValidateException("getLocalTransactionXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, EXTENDED_SERVLETS, this);	// NOI18N
            if(getLocalTransactionUnresolvedAction()==null)
                throw new org.netbeans.modules.schema2beans.ValidateException("getLocalTransactionUnresolvedAction() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, EXTENDED_SERVLETS, this);	// NOI18N
        }
    }
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("EnterpriseBean");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getEnterpriseBean();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(ENTERPRISE_BEAN, 0, str, indent);
        
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
