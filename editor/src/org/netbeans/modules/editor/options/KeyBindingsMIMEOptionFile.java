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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.openide.xml.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.util.List;
import org.netbeans.editor.MultiKeyBinding;

/** MIME Option XML file for KeyBindings settings.
 *  KeyBindings settings are loaded and saved in XML format
 *  according to EditorKeyBindings-1_0.dtd.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class KeyBindingsMIMEOptionFile extends MIMEOptionFile{
    
    /** Elements */
    public static final String TAG_ROOT = "bindings"; //NOI18N
    public static final String TAG_BIND = "bind"; //NOI18N
    
    /** Attributes */
    public static final String ATTR_KEY = "key"; //NOI18N
    public static final String ATTR_ACTION_NAME = "actionName"; //NOI18N
    public static final String ATTR_REMOVE = "remove"; //NOI18N
    
    /** File name of this MIMEOptionFile */
    static final String FILENAME = "keybindings"; //NOI18N
    
    public KeyBindingsMIMEOptionFile(BaseOptions base, Object proc) {
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
        
        // gets current keyBindings map
        List keybs = (List) Settings.getValue(base.getKitClass(), SettingsNames.KEY_BINDING_LIST);
        Map mapa = OptionUtilities.makeKeyBindingsMap(keybs);
        properties.clear();
        
        NodeList bind = rootElement.getElementsByTagName(TAG_BIND);
        for (int i=0;i<bind.getLength();i++){
            Node node = bind.item(i);
            Element bindElement = (Element)node;
            
            if (bindElement == null){
                continue;
            }
            
            String key    = bindElement.getAttribute(ATTR_KEY);
            String delete    = bindElement.getAttribute(ATTR_REMOVE);
            String actionName = bindElement.getAttribute(ATTR_ACTION_NAME);
            if (actionName==null) actionName="";
            

            if ((actionName.length() != 0) && (!new Boolean(delete).booleanValue())){
                if(key.indexOf('$') > 0){
                    MultiKeyBinding mkb = new MultiKeyBinding( OptionUtilities.stringToKeys(key) , actionName );
                    properties.put(key,  mkb);
                }else{
                    MultiKeyBinding mkb = new MultiKeyBinding( OptionUtilities.stringToKey(key) , actionName );
                    properties.put(key, mkb );
                }
            }else{
                properties.put(key, "" );
            }
            
        }
        
        if (properties.size()>0){
            // create updated map
            mapa.putAll(properties);
            
            // remove all deleted values
            for( Iterator i = properties.keySet().iterator(); i.hasNext(); ) {
                String key = (String)i.next();
                if(properties.get(key) instanceof String){
                    // remove all deleted props
                    mapa.remove(key);
                }
            }

            // setKeybMap without saving to XML
            if (propagate){
                base.setKeyBindingList(new ArrayList(mapa.values()), false);
                setLoaded(true);
            }
        }
    }
    
    /** Save settings to XML file 
     *  @param changedProp the Map of settings to save */
    protected synchronized void updateSettings(Map changedProp){
        // put changed properties to local map
        properties.putAll(changedProp);
        
        // now we can save local map to XML file
        Document doc = XMLUtil.createDocument(TAG_ROOT, null, processor.getPublicID(), processor.getSystemID());
        Element rootElem = doc.getDocumentElement();
        
        ArrayList removed = new ArrayList();
        
        Map defaultKeybs = base.getDefaultKeyBindingsMap();

        // if default keybindings don't exist for appropriate kit, set them empty
        if (defaultKeybs == null) defaultKeybs = new HashMap();
        
        // save XML
        for( Iterator i = properties.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            // Process deleted properties
            if (properties.get(key) instanceof String){
                // if deleted property is in default set, mark it as deleted
                if (defaultKeybs.containsKey(key)){
                    Element keybElem = doc.createElement(TAG_BIND);
                    keybElem.setAttribute(ATTR_KEY, key);
                    keybElem.setAttribute(ATTR_REMOVE, Boolean.TRUE.toString());
                    rootElem.appendChild(keybElem);
                }else{
                    // put removed keybindings to deleted Map
                    removed.add(key);
                }
                
                // if property is not in default set, it will not be written and will be deleted
                continue;
            }
            
            // if property is in default set we don't have to write it
            if ((properties.get(key) instanceof MultiKeyBinding) && (!defaultKeybs.containsKey(key))){
                MultiKeyBinding mkb = (MultiKeyBinding) properties.get(key);
                
                Element keybElem = doc.createElement(TAG_BIND);
                keybElem.setAttribute(ATTR_KEY, key);
                keybElem.setAttribute(ATTR_ACTION_NAME, mkb.actionName);
                rootElem.appendChild(keybElem);
            }
        }
        
        // remove deleted properties from local Map
        for (int i=0; i<removed.size(); i++){
            properties.remove(removed.get(i));
        }
        
        doc.getDocumentElement().normalize();

        saveSettings(doc);
    }
    
}
