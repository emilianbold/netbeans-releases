/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.awt.Color;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.openide.filesystems.FileLock;
import org.openide.xml.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.io.OutputStream;


/** MIME Option XML file for Properties settings.
 *  Properties settings are loaded and saved in XML format
 *  according to EditorProperties-1_0.dtd.
 *  Properties is common name for all additional Editor settings like
 *  expert settings or simple boolean, integer, string, etc. properties type.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class PropertiesMIMEOptionFile extends MIMEOptionFile{
    
    /** Elements */
    public static final String TAG_ROOT = "properties"; //NOI18N
    public static final String TAG_PROPERTY = "property"; //NOI18N
    
    /** Attributes */
    public static final String ATTR_NAME = "name"; //NOI18N
    public static final String ATTR_CLASS = "class"; //NOI18N
    public static final String ATTR_VALUE = "value"; //NOI18N
    
    /** File name of this MIMEOptionFile */
    static final String FILENAME = "properties"; //NOI18N
    
    public PropertiesMIMEOptionFile(BaseOptions base, Object proc) {
        super(base, proc);
    }

    /** Loads settings from XML file.
     * @param propagate if true - propagates the loaded settings to Editor UI */
    protected synchronized void loadSettings(boolean propagate){
        Document doc = dom;
        Element rootElement = doc.getDocumentElement();
        
        if (!TAG_ROOT.equals(rootElement.getTagName())) {
            // Wrong root element
            return;
        }
        
        properties.clear();
        
        NodeList prop = rootElement.getElementsByTagName(TAG_PROPERTY);
        for (int i=0;i<prop.getLength();i++){
            Node node = prop.item(i);
            Element propElem = (Element)node;
            
            if (propElem == null){
                continue;
            }
            
            String  name = propElem.getAttribute(ATTR_NAME);
            String  className = propElem.getAttribute(ATTR_CLASS);
            String  value = propElem.getAttribute(ATTR_VALUE);
            
            Class clazz;
            
            try{
                clazz = Class.forName(className);
            }catch(ClassNotFoundException cnfe){
                continue;
            }
            
            if (Boolean.class.isAssignableFrom(clazz)){
                Boolean boolValue =Boolean.valueOf(value);
                if (propagate) base.doSetSettingValue(name, boolValue ,null);
                properties.put(name, boolValue);
                
            } else if (Integer.class.isAssignableFrom(clazz)){
                Integer intValue = Integer.valueOf(value);
                if (intValue != null){
                    if (propagate) base.doSetSettingValue(name, intValue,null);
                    properties.put(name, intValue );
                }
                
            } else if (Float.class.isAssignableFrom(clazz)){
                Float floatValue = Float.valueOf(value);
                if (floatValue!=null){
                    if (propagate) base.doSetSettingValue(name, floatValue, null);
                    properties.put(name, floatValue );
                }
                
            } else if (Insets.class.isAssignableFrom(clazz)){
                Insets insetsValue = OptionUtilities.parseInsets(value);
                if (insetsValue!=null){
                    if (propagate) base.doSetSettingValue(name, insetsValue, null);
                    properties.put(name, insetsValue );
                }
                
            } else if (Color.class.isAssignableFrom(clazz)){
                Color colorValue = OptionUtilities.string2Color(value);
                if (colorValue!=null){
                    if (propagate) base.doSetSettingValue(name, colorValue, null);
                    properties.put(name, colorValue );
                }
                
            } else if (String.class.isAssignableFrom(clazz)){
                if (value!=null){
                    if (propagate) base.doSetSettingValue(name, value, null);
                    properties.put(name, value );
                }
            }
        }
        if (propagate) setLoaded(true);
    }
    
    /** Save settings to XML file 
     *  @param changedProp the Map of settings to save */
    protected synchronized void updateSettings(Map changedProp){
        
        boolean save = false;
        
        // prepare properties for saving
        for( Iterator i = changedProp.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            if (changedProp.get(key) instanceof Boolean){
                if (!changedProp.get(key).equals(properties.put(key, changedProp.get(key)))){
                    save = true;
                }
            } else if (changedProp.get(key) instanceof Integer){
                if (!changedProp.get(key).equals(properties.put(key, changedProp.get(key)))){
                    save = true;
                }
            } else if (changedProp.get(key) instanceof Float){
                if (!changedProp.get(key).equals(properties.put(key, changedProp.get(key)))){
                    save = true;
                }
            } else if (changedProp.get(key) instanceof Insets){
                if (!changedProp.get(key).equals(properties.put(key, changedProp.get(key)))){
                    save = true;
                }
            } else if (changedProp.get(key) instanceof Color){
                if (!changedProp.get(key).equals(properties.put(key, changedProp.get(key)))){
                    save = true;
                }
            } else if (changedProp.get(key) instanceof String){
                if (!changedProp.get(key).equals(properties.put(key, changedProp.get(key)))){
                    save = true;
                }
            }
        }
        
        if (save == false) return;
        
        // now we can save local map to XML file
        Document doc = XMLUtil.createDocument(TAG_ROOT, null, processor.getPublicID(), processor.getSystemID());
        Element rootElem = doc.getDocumentElement();
        
        // save XML
        for( Iterator i = properties.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            String className;
            String value;
            if (properties.get(key) instanceof Boolean){
                className = "java.lang.Boolean"; //NOI18N
                Boolean booleanValue = (Boolean) properties.get(key);
                value = booleanValue.toString();
            } else if (properties.get(key) instanceof Integer){
                className = "java.lang.Integer"; //NOI18N
                Integer intValue = (Integer) properties.get(key);
                value = Integer.toString(intValue.intValue());
            } else if (properties.get(key) instanceof Float){
                className = "java.lang.Float"; //NOI18N
                Float floatValue = (Float) properties.get(key);
                value = Float.toString(floatValue.floatValue());
            } else if (properties.get(key) instanceof Insets){
                className = "java.awt.Insets"; //NOI18N
                Insets insetsValue = (Insets) properties.get(key);
                value = OptionUtilities.insetsToString(insetsValue);
            } else if (properties.get(key) instanceof Color){
                className = "java.awt.Color"; //NOI18N
                Color colorValue = (Color) properties.get(key);
                value = OptionUtilities.color2String(colorValue);
            } else if (properties.get(key) instanceof String){
                className = "java.lang.String"; //NOI18N
                value = (String) properties.get(key);
            } else {
                continue;
            }
            
            String name = key;
            Element propElem = doc.createElement(TAG_PROPERTY);
            propElem.setAttribute(ATTR_NAME, name);
            propElem.setAttribute(ATTR_CLASS, className);
            propElem.setAttribute(ATTR_VALUE, value);
            
            rootElem.appendChild(propElem);
        }
        
        doc.getDocumentElement().normalize();
        
        try{
            FileLock lock = processor.getXMLDataObject().getPrimaryFile().lock();
            try{
                OutputStream os = processor.getXMLDataObject().getPrimaryFile().getOutputStream(lock);
                try {
                    XMLUtil.write(doc, os, null);
                    os.flush();
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    os.close();
                }
            }catch (IOException ioe){
                ioe.printStackTrace();
            }finally{
                lock.releaseLock();
            }
        }catch (IOException ioexc){
            ioexc.printStackTrace();
        }
    }
    
}
