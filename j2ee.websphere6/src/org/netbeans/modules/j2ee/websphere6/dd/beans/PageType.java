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
