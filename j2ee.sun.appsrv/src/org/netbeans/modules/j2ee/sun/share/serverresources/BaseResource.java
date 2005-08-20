/*
 * BaseResource.java
 *
 * Created on August 10, 2005, 5:34 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.j2ee.sun.share.serverresources;

import java.beans.*;

import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;


/**
 *
 * @author Nitya Doraisamy
 */
public class BaseResource extends Object implements java.io.Serializable {
    
    protected String name;
    protected String description;
    protected NameValuePair[] extraParams;
    
    transient protected PropertyChangeSupport propertySupport;
    
    /** Creates a new instance of BaseResource */
    public BaseResource() {
        propertySupport = new PropertyChangeSupport(this);
    }
    
    protected void initPropertyChangeSupport(){
        if(propertySupport==null)
            propertySupport = new PropertyChangeSupport ( this );

    }
    
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        initPropertyChangeSupport();
        propertySupport.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener (PropertyChangeListener listener) {
        initPropertyChangeSupport();
        propertySupport.removePropertyChangeListener (listener);
    }
    
    public String getName() {
        return name;
    }
    public void setName(String value) {
        String oldValue = name;
        this.name = value;
        initPropertyChangeSupport();  
        propertySupport.firePropertyChange ("name", oldValue, name);//NOI18N
    }
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        String oldValue = description;
        this.description = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("description", oldValue, description);//NOI18N
    }
    
    public NameValuePair[] getExtraParams() {
        if(this.extraParams == null)
            this.extraParams = new NameValuePair[0];   
        return this.extraParams;
    }
    public void setExtraParams(Object[] value) {
        NameValuePair[] pairs = new NameValuePair[value.length];
        for (int i = 0; i < value.length; i++) {
            NameValuePair val = (NameValuePair)value[i];
            NameValuePair pair = new NameValuePair();
            pair.setParamName(val.getParamName());
            pair.setParamValue(val.getParamValue());
            //pair.setParamDescription(val.getParamDescription());
            pairs[i] = pair;
        }
        NameValuePair[] oldValue = extraParams;
        this.extraParams = pairs;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("extraParams", oldValue, extraParams);//NOI18N
    }  
    
    public Resources getResourceGraph(){
        return DDProvider.getDefault().getResourcesGraph();
    }
    
    public PropertyElement populatePropertyElement(PropertyElement prop, NameValuePair pair){
        prop.setName(pair.getParamName()); 
        prop.setValue(pair.getParamValue()); 
        return prop;
    }
}
