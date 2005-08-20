/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ResourceConfigData.java
 *
 * Created on October 5, 2002, 6:20 PM
 */
package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.util.Set;
import java.util.Vector;
import java.util.Hashtable;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;

/**
 *
 * @author  shirleyc
 */
public class ResourceConfigData implements WizardConstants {
 
    private DataFolder targetFolder;
    private String targetFolderPath;
    private String targetFile;
    private FileObject targetFileObject;
    private ResourceConfigHelperHolder holder;
    private Hashtable prop_value_map = new Hashtable();
    private String resName;
    
    public static Wizard cpWizard = null;
    public static Wizard dsWizard = null;
    public static Wizard jmsWizard = null;
    public static Wizard mailWizard = null;
    public static Wizard pmWizard = null;
    
    public ResourceConfigData() {
    }
    
    /*public ResourceConfigData(org.netbeans.modules.j2ee.sun.share.dd.resources.JdbcConnectionPool pool) throws NullPointerException{
        Wizard wiz = getCPWizard();
        String[] attrNames = FieldHelper.getFieldNames(wiz);  
            
        for (int i = 0; i < attrNames.length; i++) {
            try {
                String value = pool.getAttributeValue(attrNames[i]);
                if (value != null) {
                    setString(attrNames[i], value);
                }
            } catch (Exception ex) {
                System.out.println("error in Resource Config data ");
            }
        }
        ExtraProperty[] props = pool.getExtraProperty();
        Vector vec = new Vector();
        for (int i = 0; i < props.length; i++) {
            NameValuePair pair = new NameValuePair();
            pair.setParamName(props[i].getAttributeValue("name"));  //NOI18N
            pair.setParamValue(props[i].getAttributeValue("value"));  //NOI18N
            pair.setParamDescription(props[i].getDescription());  //NOI18N
            vec.add(pair);
        }
        set(__Properties, vec);
        
        //set name
        String name = pool.getAttributeValue(__Name);
        String dataSourceClassName = pool.getAttributeValue(__DatasourceClassname);
        setString(__Name, name);
    }*/
    
    public void removeAll() {
        prop_value_map = new Hashtable();
    }
    
    public String getResourceName() {
        return resName;
    }
    
    public void setResourceName(String name) {
        resName = name;
    }
    
    public String getString(String name) {
        Object value = prop_value_map.get(name);
        if (value == null)
            return new String();
        else
            return (String)value;
    }
    
    public void setString(String name, String value) {
        set(name, value);
    }
    
    public Object get(String name) {
        return prop_value_map.get(name);
    }
    
    public void set(String name, Object value) {
        prop_value_map.put(name, value);
    }
    
    public String[] getFieldNames() {
        Set keySet = prop_value_map.keySet();
        String[] fieldNames = new String[keySet.size()];
        return (String[])keySet.toArray(fieldNames);
    }
    
    public Vector getProperties() {
        Vector props = (Vector)prop_value_map.get(__Properties);  //NOI18N
        if (props == null) {
            props = new Vector();
            prop_value_map.put(__Properties, props);  //NOI18N
        }
        return props;
    }
    
    public Vector getPropertyNames() {
        Vector props = getProperties();
        Vector vec = new Vector();
        for (int i = 0; i < props.size(); i++) {
            vec.add(((NameValuePair)props.elementAt(i)).getParamName());
        }
        return vec;
    }
    
    public String getPropertyValue(String propName) {
        Vector vec = getProperties();
        for (int i = 0; i < vec.size(); i++) {
            NameValuePair pair = (NameValuePair)vec.elementAt(i);
            if (pair.getParamName().equals(propName)) 
                return pair.getParamValue();
        }
        return null;
    }
    
    public Vector addProperty(NameValuePair pair) {
        Vector names = getPropertyNames();
        if (names.contains(pair.getParamName()))
            return null;
        Vector props = getProperties();
        props.add(pair);   
        return props;
    }
    
    public Vector addProperty(String name, String value) {
        NameValuePair pair = new NameValuePair();
        pair.setParamName(name);
        pair.setParamValue(value); 
        return addProperty(pair);
    }
    
    public void removeProperty(int index) {
        Vector props = getProperties();
        props.removeElementAt(index);
    }
    
    public void setProperties(Vector props) {
           set(__Properties, props); 
    }
    
    public String toString() {
        StringBuffer retValue = new StringBuffer();
        retValue.append(getResourceName() + "::\n");  //NOI18N
        String[] fieldNames = getFieldNames();
        for (int i = 0; i < fieldNames.length; i++) {
            if (fieldNames[i].equals(__Properties)) {
                retValue.append("properties: \n");  //NOI18N
                Vector props = (Vector)getProperties();
                for (int j = 0; j < props.size(); j++) {
                    NameValuePair pair = (NameValuePair)props.elementAt(j);
                    retValue.append("    " + pair.getParamName() + ": " + pair.getParamValue()); //NOI18N
                }
            }
            else 
                retValue.append(fieldNames[i] + ": " + getString(fieldNames[i]) + "\n");  //NOI18N
        }
        return retValue.toString();
    }
    
    public void setTargetFolder(DataFolder targetFolder){
        this.targetFolder = targetFolder;
    }
    
    public DataFolder getTargetFolder(){
        return this.targetFolder;
    }  
    
    public void setTargetFolderPath(String path){
        this.targetFolderPath = path;
    }
    
    public String getTargetFolderPath(){
        return this.targetFolderPath;
    }

    public void setTargetFile(String targetFile){
        this.targetFile = targetFile;
    }
    
    public String getTargetFile(){
        return this.targetFile;
    }
    
    public FileObject getTargetFileObject(){
        return this.targetFileObject;
    }
    
    public void setTargetFileObject(FileObject targetObject){
        this.targetFileObject = targetObject;
    }
 
    public ResourceConfigHelperHolder getHolder(){
        return this.holder;
    }
    
    public void setHolder(ResourceConfigHelperHolder holder){
        this.holder = holder;
    }
}
