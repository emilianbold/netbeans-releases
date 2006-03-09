/*
 * ModuleExtensionsType.java
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
public class ModuleExtensionsType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {
    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    
    
    static public final String XMI_TYPE="XmiType";
    static public final String XMI_ID="XmiId";
    static public final String ALT_ROOT="AltRoot";
    static public final String MODULE_XMI_TYPE="ModuleXmiType";
    static public final String MODULE_HREF="ModuleHref";
    static public final String MODULE="Module";
    /**
     * Creates a new instance of ModuleExtensionsType
     */
    public ModuleExtensionsType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public ModuleExtensionsType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(1);
        this.createProperty("module", 	// NOI18N
                MODULE,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(MODULE, XMI_TYPE_ID, MODULE_XMI_TYPE,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(MODULE, HREF_ID, MODULE_HREF,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        this.initialize(options);
    }
    public void initialize(int options) {
        
    }
    
    public void setModule(String value) {
        this.setValue(MODULE, value);
    }
    
    public String getModule() {
        return ( String)this.getValue(MODULE);
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
        setAttributeValue(MODULE, MODULE_HREF,DDXmi.addAppHrefToId(value));
    }
    
    public String getHref() {
        return DDXmi.getIdFromHref((String)getAttributeValue(MODULE, MODULE_HREF));
    }
    
    public void setModuleType(String value) {
        setAttributeValue(MODULE, MODULE_XMI_TYPE,value);
    }
    
    public String getModuleType() {
        return (String)getAttributeValue(MODULE, MODULE_XMI_TYPE);
    }
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if(getModule()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getModule() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "ModuleExtensions", this);	// NOI18N
        }
        if(getHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "Module", this);	// NOI18N
        }
        if(getModuleType()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getModuleType() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "Module", this);	// NOI18N
        }
        if((getModuleType()!=MODULE_XMI_TYPE_EJB_ID) && (getModuleType()!=MODULE_XMI_TYPE_WEB_ID)){
            throw new org.netbeans.modules.schema2beans.ValidateException(
                    "getModuleType() != {"+MODULE_XMI_TYPE_WEB_ID+","+MODULE_XMI_TYPE_EJB_ID+"}",
                            org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "Modules", this);	// NOI18N
        }
    }
     // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("Module");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getModule();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(MODULE, 0, str, indent);
        
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
