/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.options;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.editor.MultiKeyBinding;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.awt.Insets;
import org.w3c.dom.Document;
import org.openide.xml.XMLUtil;
import org.openide.filesystems.FileLock;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import java.util.Enumeration;
import org.openide.cookies.InstanceCookie;
import org.openide.util.actions.SystemAction;
import java.util.Set;
import java.awt.Dimension;

/** Various utilities for Editor Options.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class OptionUtilities {

    private static final Logger LOG = Logger.getLogger(OptionUtilities.class.getName());
    
    // the name of default folder for storing default maps such as macros, abbrevs...
    public static final String DEFAULT_FOLDER = "Defaults"; //NOI18N
    
    private OptionUtilities() {
        // instantiation has no sense
    }
    
    private static String wrap(String s){
        return (s.length()==1)? "0"+s : s; // NOI18N
    }
    
    /** Converts Color to hexadecimal String representation */
    public static String color2String(Color c){
        StringBuffer sb = new StringBuffer();
        sb.append("#"); // NOI18N
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
        if (Font.BOLD   == i) return "bold"; // NOI18N
        if (Font.ITALIC == i) return "italic"; // NOI18N
        if ( (Font.BOLD+Font.ITALIC) == i ) return "bold-italic"; // NOI18N
        return "plain"; // NOI18N
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
    

    /** 
     * Creates textual representation of KeyStroke[].
     * @deprecated Without any replacement.
     */
    public static String keysToString(KeyStroke[] stroke){
        if (stroke == null) return "NULL"; // NOI18N
        StringBuffer sb = new StringBuffer();
        
        for (int i=0; i<stroke.length; i++){
            sb.append(org.openide.util.Utilities.keyToString(stroke[i]));
            if (i<stroke.length-1) sb.append("$"); // NOI18N
        }
        
        return sb.toString();
    }

    /** 
     * Creates textual representation of KeyStroke.
     * @deprecated Use <code>org.openide.util.Utilities.keyToString(KeyStroke)</code>.
     */
    public static String keyToString(KeyStroke stroke){
        if (stroke == null) return "NULL"; // NOI18N
        return org.openide.util.Utilities.keyToString(stroke);
    }
    
    /** 
     * Converts textual representatin of Keystroke
     * @deprecated Use <code>org.openide.util.Utilities.stringToKey(String)</code>.
     */
    public static KeyStroke stringToKey(String s){
        if (s.equals("NULL")) return null; // NOI18N
        return org.openide.util.Utilities.stringToKey(s);
    }

    /**
     * Converts textual representatin of Keystroke[]
     * @deprecated Without any replacement.
     */
    public static KeyStroke[] stringToKeys(String s) {
        if (s.equals("NULL")) return null; // NOI18N
        
        StringTokenizer st = new StringTokenizer(s.toUpperCase(), "$"); // NOI18N
        ArrayList arr = new ArrayList();
        
        while (st.hasMoreElements()) {
            s = st.nextToken();
            KeyStroke k = org.openide.util.Utilities.stringToKey(s);
            if (k == null) return null;
            arr.add(k);
        }
        
        return (KeyStroke[])arr.toArray(new KeyStroke[arr.size()]);
    }
    
    /** @deprecated Without any replacement. */
    public static void printDefaultAbbrevs(Map map) {
        System.out.println("-----------------------------------------------------------"); // NOI18N
        System.out.println("<?xml version=\"1.0\"?>"); // NOI18N
        System.out.println("<!DOCTYPE catalog PUBLIC \""+AbbrevsMIMEProcessor.PUBLIC_ID+"\""); // NOI18N
        System.out.println(" \""+AbbrevsMIMEProcessor.SYSTEM_ID+"\">"); // NOI18N
        System.out.println("");
        System.out.println("<"+AbbrevsMIMEOptionFile.TAG_ROOT+">"); // NOI18N
        for ( Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            String value = (String)map.get(key);
            System.out.println("<"+AbbrevsMIMEOptionFile.TAG_ABBREV+" "+AbbrevsMIMEOptionFile.ATTR_KEY+"=\""+key+"\">" // NOI18N
            +value+"</"+AbbrevsMIMEOptionFile.TAG_ABBREV+">"); // NOI18N
        }
        System.out.println("</"+AbbrevsMIMEOptionFile.TAG_ROOT+">"); // NOI18N
    }
    
    /** 
     * Prints given Abbreviations Map to XML file with given FO.
     * @deprecated Without any replacement.
     */
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
                XMLUtil.write(doc, file.getOutputStream(lock), "UTF-8"); // NOI18N
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
                XMLUtil.write(doc, file.getOutputStream(lock), "UTF-8"); // NOI18N
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                lock.releaseLock();
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    /** 
     * Prints given KeyBindings List to XML file with given FO
     * @deprecated Without any replacement.
     */
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
                XMLUtil.write(doc, file.getOutputStream(lock), "UTF-8"); // NOI18N
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

    /** Coverts Insets to String representation */
    public static String dimensionToString(Dimension dim){
        StringBuffer sb = new StringBuffer();
        sb.append(dim.width);
        sb.append(',');
        
        sb.append(dim.height);

        return sb.toString();
    }

    /** Converts textual representation of Insets */
    public static Dimension parseDimension(String s){
        StringTokenizer st = new StringTokenizer(s, ","); // NOI18N
        
        int arr[] = new int[2];
        int i=0;
        while (st.hasMoreElements()) {
            if (i>1) return null;
            try{
                arr[i]  = Integer.parseInt(st.nextToken());
            }catch(NumberFormatException nfe){
                return null;
            }
            i++;
        }
        if (i!=2) return null;
        return new Dimension(arr[0],arr[1]);
    }
    
    
    /** 
     * Converts KeyBings List to KeyBindings Map 
     * Map.key is the textual representation of keystroke(s) 
     * @deprecated Without any replacement.
     */
    public static Map<String, MultiKeyBinding> makeKeyBindingsMap(List propList){
        Map<String, MultiKeyBinding> ret = new HashMap<String, MultiKeyBinding>();

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
    
    /** Gets popup menu items (DataObjects) stored in base popup folder 
     */
    public static List getGlobalPopupMenuItems(){
        return getGlobalMenuItems("Popup"); //NOI18N
    }

    /** Retrieves a list of BaseOptions.BASE MultiPropertyFolder items */
    public static List getGlobalMenuItems(String folderName){
        MIMEOptionFolder mimeFolder = AllOptionsFolder.getDefault().getMIMEFolder();
        if (mimeFolder == null) return new ArrayList();
        MultiPropertyFolder mpf = mimeFolder.getMPFolder(folderName,false); //NOI18N
        if ( mpf!=null ){
            return mpf.getProperties();
        }
        return new ArrayList();
    }

    public static List getPopupStrings(List popup){
        return getPopupStrings(popup, false);
    }    
    
    /** Creates String representation of popup from DO representation
     *  @param addSeparatorInstance if true the result list will use instance of JSeparator in case of separator,
     *   if false null will be used
     */
    public static List getPopupStrings(List popup, boolean addSeparatorInstance){
        List retList = new ArrayList();
        for (int i=0; i<popup.size(); i++){
            if (!(popup.get(i) instanceof DataObject)) continue;
            
            DataObject dob = (DataObject) popup.get(i);
            InstanceCookie ic = (InstanceCookie)dob.getCookie(InstanceCookie.class);
            
            if (ic!=null){
                
                try{
                    if (SystemAction.class.isAssignableFrom(ic.instanceClass())){
                        retList.add(ic.instanceName());
                    }else if (javax.swing.Action.class.isAssignableFrom(ic.instanceClass())){
                        retList.add(ic.instanceCreate());
                    }
                    if(javax.swing.JSeparator.class.isAssignableFrom(ic.instanceClass())){
                        retList.add(addSeparatorInstance?new javax.swing.JSeparator():null);
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }catch(ClassNotFoundException cnfe){
                    cnfe.printStackTrace();
                }
            }else{
                if ("org-openide-windows-TopComponent".equals(dob.getName())){ //NOI18N
                    retList.add(dob.getName().replace('-','.'));
                }else{
                    retList.add(dob.getName());
                }
            }
        }
        
        return retList;
    }
    
}
