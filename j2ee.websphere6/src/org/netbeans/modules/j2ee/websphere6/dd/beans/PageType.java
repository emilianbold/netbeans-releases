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
public class PageType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {

    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);



    public PageType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public PageType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(1);
        this.initialize(options);
    }
    public void initialize(int options) {
       
    }
    public void setDefaults() {
         setXmiId("Page_");
         setName("");
         setUri("/");
    }
    
   
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }
         
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    
    public void setXmiId(String value) {
        this.setAttributeValue(PAGES_XMI_ID,value);
    }
    
    public String getXmiId() {
        return (String)this.getAttributeValue(PAGES_XMI_ID);
    }
    public void setName(String value) {
        this.setAttributeValue(PAGES_NAME,value);
    }
    public String getName() {
        return (String)this.getAttributeValue(PAGES_NAME);
    }
    public void setUri(String value) {
        this.setAttributeValue(PAGES_URI,value);
    }
    public String getUri() {
        return (String)this.getAttributeValue(PAGES_URI);
    } 
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "PageType", this);	// NOI18N
        }
        if(getName()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "PageType", this);	// NOI18N
        }
        if(getUri()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getUri() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "PageType", this);	// NOI18N
        }
        
                
        
    }
    
    
    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("PageType");	// NOI18N
        //this.dumpAttributes(PAGE, 0, str, indent);
        
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
