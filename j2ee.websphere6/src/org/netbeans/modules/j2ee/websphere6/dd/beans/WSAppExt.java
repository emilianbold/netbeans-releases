/*
 * WSAppExt.java
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
import java.io.*;

/**
 *
 * @author dlm198383
 */
public class WSAppExt extends DDXmi{
    private static final String ROOT=TYPE_APP_EXT_APP_ID;
    
    private static final String ROOT_NAME="ApplicationExt";
    private static final String MODULE_EXTENSIONS="ModuleExtensions";
    private static final String MODULE_EXTENSIONS_XMI_ID="ModuleExtensionsXmiId";
    private static final String MODULE_EXTENSIONS_APPROOT="ModuleExtensionsAppRoot";
    private static final String MODULE_EXTENSIONS_XMI_TYPE="ModuleExtensionsXmiType";
    /** Creates a new instance of WSAppExt */
    public WSAppExt() {
        this(null, Common.USE_DEFAULT_VALUES);
    }
    public WSAppExt(org.w3c.dom.Node doc, int options) {
        this(Common.NO_DEFAULT_VALUES);
        try {
            initFromNode(doc, options);
        } catch (Schema2BeansException e) {
            throw new RuntimeException(e);
        }
    }
    public WSAppExt(int options) {
        super(options,ROOT);
        //initOptions(options);
    }
    
    public WSAppExt(File f,boolean validate) throws IOException{
        this(GraphManager.createXmlDocument(new FileInputStream(f), validate), Common.NO_DEFAULT_VALUES);
    }
    
    public WSAppExt(InputStream in, boolean validate) {
        this(GraphManager.createXmlDocument(in, validate), Common.NO_DEFAULT_VALUES);
    }
    
    protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException {
        if (doc == null) {
            doc = GraphManager.createRootElementNode(ROOT);	// NOI18N
            if (doc == null)
                throw new Schema2BeansException(Common.getMessage(
                        "CantCreateDOMRoot_msg", ROOT));
        }
        Node n = GraphManager.getElementNode(ROOT, doc);	// NOI18N
        if (n == null) {
            throw new Schema2BeansException(Common.getMessage("DocRootNotInDOMGraph_msg", ROOT, doc.getFirstChild().getNodeName()));
        }
        this.graphManager.setXmlDocument(doc);
        this.createBean(n, this.graphManager());
        this.initialize(options);
    };
    
    public void initialize(int options) {
        
    }
    
    public void setDefaults() {
        setXmiVersion();
        setNsXmi();
        setNsAppExt();
        setNsApp();
        setXmiId("Application_ID_Ext");
        setApplication("");
        setApplicationHref("Application_ID");
    }
    
    protected void initOptions(int options) {
        this.graphManager = new GraphManager(this);
        this.createRoot(ROOT, ROOT_NAME,	// NOI18N
                Common.TYPE_1 | Common.TYPE_BEAN, WSAppExt.class);
        
        initPropertyTables(2);
        this.createAttribute(XMI_ID_ID,     XMI_ID,      AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_APP_ID,     NS_APP,      AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_APP_EXT_ID, NS_APP_EXT,  AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_XMI_ID,     NS_XMI,      AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(XMI_VERSION_ID,XMI_VERSION, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        
        
        this.createProperty("moduleExtensions", 	// NOI18N
                MODULE_EXTENSIONS,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                ModuleExtensionsType.class);
        this.createAttribute(MODULE_EXTENSIONS,XMI_TYPE_ID,MODULE_EXTENSIONS_XMI_TYPE,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(MODULE_EXTENSIONS,APPROOT_ID ,MODULE_EXTENSIONS_APPROOT ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(MODULE_EXTENSIONS,XMI_ID_ID  ,MODULE_EXTENSIONS_XMI_ID  ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.createProperty(APPLICATION_ID, 	// NOI18N
                APPLICATION,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(APPLICATION,HREF_ID,APPLICATION_HREF,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.initialize(options);
    }
    
    public void setModuleExtensions(int index,ModuleExtensionsType value) {
        this.setValue(MODULE_EXTENSIONS, index,value);
    }
    
    public void setModuleExtensions(ModuleExtensionsType[]value) {
        this.setValue(MODULE_EXTENSIONS, value);
    }
    //
    public ModuleExtensionsType[] getModuleExtensions() {
        return (ModuleExtensionsType[])this.getValues(MODULE_EXTENSIONS);
    }
    public ModuleExtensionsType getModuleExtensions(int index) {
        return (ModuleExtensionsType)this.getValue(MODULE_EXTENSIONS,index);
    }
    public int sizeModuleExtensions() {
        return this.size(MODULE_EXTENSIONS);
    }
    public int addModuleExtensions(ModuleExtensionsType value) {
        int positionOfNewItem = this.addValue(MODULE_EXTENSIONS, value);
        return positionOfNewItem;
    }
    
    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeModuleExtensions(ModuleExtensionsType value) {
        return this.removeValue(MODULE_EXTENSIONS, value);
    }
    
    
    public void setModuleExtensionsId(String value,int index)  {
        setAttributeValue(MODULE_EXTENSIONS,index,XMI_ID,value);
    }
    
    public String getModuleExtensionsId(int index){
        return (String)getAttributeValue(MODULE_EXTENSIONS,index,MODULE_EXTENSIONS_XMI_ID);
    }
    
    public String getModuleExtensionsType(int index){
        return (String)getAttributeValue(MODULE_EXTENSIONS,index,MODULE_EXTENSIONS_XMI_TYPE);
    }
    public void setModuleExtensionsType(String value,int index)  {
        setAttributeValue(MODULE_EXTENSIONS,index,MODULE_EXTENSIONS_XMI_TYPE,value);
    }
    
    public void setModuleExtensionsAppRoot(String value,int index)  {
        setAttributeValue(MODULE_EXTENSIONS,index,MODULE_EXTENSIONS_APPROOT,value);
    }
    
    public String getModuleExtensionsAppRoot(int index){
        return (String)getAttributeValue(MODULE_EXTENSIONS,index,MODULE_EXTENSIONS_APPROOT);
    }
    
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if (getModuleExtensions()!= null) {
            // Validating property jdbcConnectionPool
            for (int _index = 0; _index < sizeModuleExtensions(); ++_index) {
                ModuleExtensionsType element = getModuleExtensions(_index);
                if (element != null) {
                    element.validate();
                }
                if(getModuleExtensionsId(_index)==null) {
                    throw new org.netbeans.modules.schema2beans.ValidateException("getModuleExtensionsId["+_index+"] == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, MODULE_EXTENSIONS, this);	// NOI18N
                }
                if(getModuleExtensionsAppRoot(_index)==null){
                    throw new org.netbeans.modules.schema2beans.ValidateException("getModuleExtensionsAppRoot["+_index+"] == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, MODULE_EXTENSIONS, this);	// NOI18N
                }
                if(getModuleExtensionsType(_index)==null){
                    throw new org.netbeans.modules.schema2beans.ValidateException("getModuleExtensionsType["+_index+"] == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, MODULE_EXTENSIONS, this);	// NOI18N
                }
                
            }
        }
        if (getApplication()== null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getApplication() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, APPLICATION, this);	// NOI18N
        }
        if(getApplicationHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getApplicationHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, APPLICATION, this);	// NOI18N
        }
        if(getNsApp()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsApp() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getNsAppExt()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsAppExt() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getNsCommon()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsCommon() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getNsXmi()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsXmi() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getXmiVersion()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiVersion() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        
    }
    
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        BaseBean n;
        
        str.append(indent);
        str.append(MODULE_EXTENSIONS+"["+this.sizeModuleExtensions()+"]");	// NOI18N
        for(int i=0; i<this.sizeModuleExtensions(); i++) {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (BaseBean) this.getModuleExtensions(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(MODULE_EXTENSIONS, i, str, indent);
        }
        str.append(indent);
        str.append(APPLICATION);	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getApplication();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(APPLICATION, 0, str, indent);
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
