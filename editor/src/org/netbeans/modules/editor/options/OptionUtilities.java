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
import java.awt.Font;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.KeyStroke;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.ext.java.JavaSettingsDefaults;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.awt.Insets;
import org.w3c.dom.Document;
import org.openide.xml.XMLUtil;
import org.openide.filesystems.FileLock;
import java.io.IOException;
import org.openide.filesystems.FileObject;

/** Various utilities for Editor Options.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class OptionUtilities {

    // the name of default folder for storing default maps such as macros, abbrevs...
    public static final String DEFAULT_FOLDER = "Defaults"; //NOI18N
    
    private OptionUtilities() {
        // instantiation has no sense
    }
    
    private static String wrap(String s){
        return (s.length()==1)? "0"+s : s;
    }
    
    /** Converts Color to hexadecimal String representation */
    public static String color2String(Color c){
        StringBuffer sb = new StringBuffer();
        sb.append("#");
        sb.append(wrap(Integer.toHexString(c.getRed()).toUpperCase()));
        sb.append(wrap(Integer.toHexString(c.getGreen()).toUpperCase()));
        sb.append(wrap(Integer.toHexString(c.getBlue()).toUpperCase()));
        return sb.toString();
    }
    
    /** Converts a String to an integer and returns the specified opaque Color. */
    public static Color string2Color(String s){
        try{
            return Color.decode(s);
        }catch(NumberFormatException nfe){
            return null;
        }
    }
    
    /** Converts String to integer */
    public static int string2Int(String s){
        try{
            return Integer.parseInt(s);
        }catch(NumberFormatException nfe){
            return -1;
        }
    }
    
    /** Decodes font style from string representation */
    public static int getFontStyle(String s){
        s=s.toLowerCase();
        int ret = Font.PLAIN;
        if (s.indexOf("bold") != -1) ret |= Font.BOLD; //NOI18N
        if (s.indexOf("italic") != -1) ret |= Font.ITALIC; //NOI18N
        return ret;
    }
    
    /** Encodes font style to string representation */
    public static String style2String(int i){
        if (Font.BOLD   == i) return "bold";
        if (Font.ITALIC == i) return "italic";
        if ( (Font.BOLD+Font.ITALIC) == i ) return "bold-italic";
        return "plain";
    }
    
    
    /** Gets changed values of newMap against the oldMap.
     *  If allowNewKey is false then diff will contain only
     *  changed oldMap keys
     */
    public static Map getMapDiff(Map oldMap, Map newMap, boolean allowNewKeys){
        Map ret = new HashMap();

        for( Iterator i = newMap.keySet().iterator(); i.hasNext(); ) {
            Object key = (Object)i.next();
            Object value = newMap.get( key );
            // if value in newMap is different from oldMap, put it to return Map
            if (!value.equals(oldMap.get(key)))
                ret.put(key,newMap.get(key));
            // or we can add some new key if allowNewKeys is true
            else if (allowNewKeys && !oldMap.containsKey(key))
                ret.put(key, newMap.get(key));
        }
        
        for ( Iterator i = oldMap.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            Object value = oldMap.get( key );
            // all deleted keys
            if (!newMap.containsKey(key)) ret.put(key, "");
        }
        
        return ret;
    }
    

    /** Creates textual representation of KeyStroke[]. */
    public static String keysToString(KeyStroke[] stroke){
        if (stroke == null) return "NULL";
        StringBuffer sb = new StringBuffer();
        
        for (int i=0; i<stroke.length; i++){
            sb.append(org.openide.util.Utilities.keyToString(stroke[i]));
            if (i<stroke.length-1) sb.append("$");
        }
        
        return sb.toString();
    }

    /** Creates textual representation of KeyStroke. */    
    public static String keyToString(KeyStroke stroke){
        if (stroke == null) return "NULL";
        return org.openide.util.Utilities.keyToString(stroke);
    }
    
    /** Converts textual representatin of Keystroke */    
    public static KeyStroke stringToKey(String s){
        if (s.equals("NULL")) return null;
        return org.openide.util.Utilities.stringToKey(s);
    }

    /** Converts textual representatin of Keystroke[] */        
    public static KeyStroke[] stringToKeys(String s){
        if (s.equals("NULL")) return null;
        
        StringTokenizer st = new StringTokenizer(s.toUpperCase(), "$"); // NOI18N
        ArrayList arr = new ArrayList();
        
        while (st.hasMoreElements()) {
            s = st.nextToken();
            KeyStroke k = stringToKey(s);
            if (k == null) return null;
            arr.add(k);
        }
        
        return (KeyStroke[])arr.toArray(new KeyStroke[arr.size()]);
    }
    
    public static void printDefaultAbbrevs(Map map){
        System.out.println("-----------------------------------------------------------");
        System.out.println("<?xml version=\"1.0\"?>");
        System.out.println("<!DOCTYPE catalog PUBLIC \""+AbbrevsMIMEProcessor.PUBLIC_ID+"\"");
        System.out.println(" \""+AbbrevsMIMEProcessor.SYSTEM_ID+"\">");
        System.out.println("");
        System.out.println("<"+AbbrevsMIMEOptionFile.TAG_ROOT+">");
        for ( Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            String value = (String)map.get(key);
            System.out.println("<"+AbbrevsMIMEOptionFile.TAG_ABBREV+" "+AbbrevsMIMEOptionFile.ATTR_KEY+"=\""+key+"\">"
            +value+"</"+AbbrevsMIMEOptionFile.TAG_ABBREV+">");
        }
        System.out.println("</"+AbbrevsMIMEOptionFile.TAG_ROOT+">");
    }
    
    /** Prints given Abbreviations Map to XML file with given FO */
    public static void printDefaultAbbrevs(Map map, FileObject file){
        Document doc = XMLUtil.createDocument(AbbrevsMIMEOptionFile.TAG_ROOT, null, AbbrevsMIMEProcessor.PUBLIC_ID, AbbrevsMIMEProcessor.SYSTEM_ID);
        org.w3c.dom.Element rootElem = doc.getDocumentElement();
        
        // save XML
        for( Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            if (map.get(key) instanceof String){
                
                String action = (String) map.get(key);
                
                org.w3c.dom.Element abbrevElem = doc.createElement(AbbrevsMIMEOptionFile.TAG_ABBREV);
                abbrevElem.setAttribute(AbbrevsMIMEOptionFile.ATTR_KEY, key);
                abbrevElem.appendChild(doc.createTextNode(action));
                rootElem.appendChild(abbrevElem);
                
            }
        }
        
        doc.getDocumentElement().normalize();
        
        try{
            FileLock lock = file.lock();
            try {
                XMLUtil.write(doc, file.getOutputStream(lock), null);
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                lock.releaseLock();
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    
    /** Prints given Macro Map to XML file with given FO */
    public static void printDefaultMacros(Map map, FileObject file){
        Document doc = XMLUtil.createDocument(MacrosMIMEOptionFile.TAG_ROOT, null, MacrosMIMEProcessor.PUBLIC_ID, MacrosMIMEProcessor.SYSTEM_ID);
        org.w3c.dom.Element rootElem = doc.getDocumentElement();
        
        // save XML
        for( Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            if (map.get(key) instanceof String){
                
                String action = (String) map.get(key);
                
                org.w3c.dom.Element macroElem = doc.createElement(MacrosMIMEOptionFile.TAG_MACRO);
                macroElem.setAttribute(MacrosMIMEOptionFile.ATTR_NAME, key);
                macroElem.appendChild(doc.createTextNode(action));
                rootElem.appendChild(macroElem);
            }
        }
        
        doc.getDocumentElement().normalize();
        
        try{
            FileLock lock = file.lock();
            try {
                XMLUtil.write(doc, file.getOutputStream(lock), null);
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                lock.releaseLock();
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    /** Prints given KeyBindings List to XML file with given FO */    
    public static void printDefaultKeyBindings(List list, FileObject file){
        Map map  = makeKeyBindingsMap(list);
        Document doc = XMLUtil.createDocument(KeyBindingsMIMEOptionFile.TAG_ROOT, null, KeyBindingsMIMEProcessor.PUBLIC_ID, KeyBindingsMIMEProcessor.SYSTEM_ID);
        org.w3c.dom.Element rootElem = doc.getDocumentElement();
        
        // save XML
        for( Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            if (map.get(key) instanceof MultiKeyBinding){
                
                MultiKeyBinding mkb = (MultiKeyBinding) map.get(key);
                if (mkb==null) continue;
                
                org.w3c.dom.Element keybElem = doc.createElement(KeyBindingsMIMEOptionFile.TAG_BIND);
                keybElem.setAttribute(KeyBindingsMIMEOptionFile.ATTR_KEY, key);
                keybElem.setAttribute(KeyBindingsMIMEOptionFile.ATTR_ACTION_NAME, mkb.actionName);
                rootElem.appendChild(keybElem);
            }
        }
        
        doc.getDocumentElement().normalize();
        
        try{
            FileLock lock = file.lock();
            try {
                XMLUtil.write(doc, file.getOutputStream(lock), null);
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                lock.releaseLock();
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    /** Coverts Insets to String representation */
    public static String insetsToString(Insets ins){
        StringBuffer sb = new StringBuffer();
        sb.append(ins.top);
        sb.append(',');
        
        sb.append(ins.left);
        sb.append(',');
        
        sb.append(ins.bottom);
        sb.append(',');
        
        sb.append(ins.right);
        
        return sb.toString();
    }

    /** Converts textual representation of Insets */
    public static Insets parseInsets(String s){
        StringTokenizer st = new StringTokenizer(s, ","); // NOI18N
        
        int arr[] = new int[4];
        int i=0;
        while (st.hasMoreElements()) {
            if (i>3) return null;
            try{
                arr[i]  = Integer.parseInt(st.nextToken());
            }catch(NumberFormatException nfe){
                return null;
            }
            i++;
        }
        if (i!=4) return null;
        return new Insets(arr[0],arr[1],arr[2],arr[3]);
    }

    /** Converts KeyBings List to KeyBindings Map 
     *  Map.key is the textual representation of keystroke(s) */
    public static Map makeKeyBindingsMap(List propList){
        Map ret = new HashMap();

        boolean output = true;
        for (int i=0; i<propList.size(); i++){
            Object obj = propList.get(i);
            if (! (obj instanceof org.netbeans.editor.MultiKeyBinding)) {
                if (!org.netbeans.editor.MultiKeyBinding.class.getClassLoader().equals(obj.getClass().getClassLoader())){
                    if (output) {
                        System.err.println("Different classloaders:");
                        System.err.println(org.netbeans.editor.MultiKeyBinding.class.getClassLoader());
                        System.err.println(obj.getClass().getClassLoader());
                        output = false;
                    }
                }
                
                continue;
            }
            MultiKeyBinding mkb = (MultiKeyBinding)obj;
            String fileName = (mkb.keys == null) ?
            OptionUtilities.keyToString(mkb.key) : OptionUtilities.keysToString(mkb.keys);
            if (fileName!=null){
                ret.put(fileName,mkb);
            }
        }
        return ret;
    }
    
}
