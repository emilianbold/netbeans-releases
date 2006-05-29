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
/**
 *
 * @author dlm198383
 */
public class ExtendedServletsType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {
    
    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    
    static public final String EXTENDED_SERVLET_HREF="ExtendedServletHref";
    
    /**
     * Creates a new instance of ExtendedServletExtensionsType
     */
    public ExtendedServletsType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public ExtendedServletsType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(3);
        this.createProperty(EXTENDED_SERVLET_ID,
                EXTENDED_SERVLET,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        
        this.createAttribute(EXTENDED_SERVLET, HREF_ID, EXTENDED_SERVLET_HREF,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        
        
        this.createProperty(MARKUP_LANGUAGES_ID, 
                MARKUP_LANGUAGES,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                MarkupLanguagesType.class);
        
        this.createAttribute(MARKUP_LANGUAGES,XMI_ID_ID,MARKUP_LANGUAGES_XMI_ID,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        this.createAttribute(MARKUP_LANGUAGES,NAME_ID,MARKUP_LANGUAGES_NAME,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(MARKUP_LANGUAGES,MIME_TYPE_ID,MARKUP_LANGUAGES_MIME_TYPE,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(MARKUP_LANGUAGES,ERROR_PAGE_ID,MARKUP_LANGUAGES_ERROR_PAGE,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(MARKUP_LANGUAGES,DEFAULT_PAGE_ID,MARKUP_LANGUAGES_DEFAULT_PAGE ,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        
        this.createProperty(LOCAL_TRANSACTION_ID,
                LOCAL_TRANSACTION,
                Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(LOCAL_TRANSACTION, XMI_ID_ID, LOCAL_TRANSACTION_XMI_ID,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(LOCAL_TRANSACTION,UNRESOLVED_ACTION_ID,LOCAL_TRANSACTION_UNRESOLVED_ACTION,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(LOCAL_TRANSACTION,BOUNDARY_ID,LOCAL_TRANSACTION_BOUNDARY,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(LOCAL_TRANSACTION,RESOLVER_ID,LOCAL_TRANSACTION_RESOLVER,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        this.initialize(options);
    }
    public void initialize(int options) {
        
    }
    public void setDefaults() {
        setHref("");        
    }
    public void setExtendedServlet(String value) {
        this.setValue(EXTENDED_SERVLET, value);
    }
    
    public String getExtendedServlet() {
        return ( String)this.getValue(EXTENDED_SERVLET);
    }
    
    public void setXmiId(String value)  {
        setAttributeValue(EXTENDED_SERVLETS_XMI_ID,value);
    }
    
    public String getXmiId(){
        return (String)getAttributeValue(EXTENDED_SERVLETS_XMI_ID);
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
        setAttributeValue(EXTENDED_SERVLET, EXTENDED_SERVLET_HREF,DDXmi.addWebHrefToId(value));
    }
    
    public String getHref() {
        return DDXmi.getIdFromHref((String)getAttributeValue(EXTENDED_SERVLET, EXTENDED_SERVLET_HREF));
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
    
    public MarkupLanguagesType [] getMarkupLanguages() {
        return (MarkupLanguagesType [])this.getValues(MARKUP_LANGUAGES);
    }
    public MarkupLanguagesType getMarkupLanguages(int index) {
        return (MarkupLanguagesType)this.getValue(MARKUP_LANGUAGES,index);
    }
    public void setMarkupLanguages(int index,MarkupLanguagesType value) {
        this.setValue(MARKUP_LANGUAGES,index,value);
    }
    
    
    
    public int sizeMarkupLanguages() {
        return this.size(MARKUP_LANGUAGES);
    }
    public int addMarkupLanguages(MarkupLanguagesType value) {
        int positionOfNewItem = this.addValue(MARKUP_LANGUAGES, value);
        return positionOfNewItem;
    }
    
    public int removeMarkupLanguages(MarkupLanguagesType value) {
        return this.removeValue(MARKUP_LANGUAGES, value);
    }
    
    
    
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if(getExtendedServlet()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getExtendedServlet() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, EXTENDED_SERVLETS, this);	// NOI18N
        }
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getExtendedServletsId == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, EXTENDED_SERVLETS, this);	// NOI18N
        }
        if(getHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE,EXTENDED_SERVLETS, this);	// NOI18N
        }
        if(getLocalTransaction()!=null) {
            if(getLocalTransactionXmiId()==null)
                throw new org.netbeans.modules.schema2beans.ValidateException("getLocalTransactionXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, EXTENDED_SERVLETS, this);	// NOI18N
            if(getLocalTransactionUnresolvedAction()==null)
                throw new org.netbeans.modules.schema2beans.ValidateException("getLocalTransactionUnresolvedAction() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, EXTENDED_SERVLETS, this);	// NOI18N
        }
        int size=sizeMarkupLanguages();
        for(int i=0;i<size;i++) {
            getMarkupLanguages(i).validate();
        }
    }
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("ExtendedServlet");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getExtendedServlet();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(EXTENDED_SERVLET, 0, str, indent);
        
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
