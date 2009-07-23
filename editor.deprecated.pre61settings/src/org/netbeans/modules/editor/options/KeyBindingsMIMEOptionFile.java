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

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.openide.xml.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.List;
import org.netbeans.editor.MultiKeyBinding;
import javax.swing.text.JTextComponent;

/** MIME Option XML file for KeyBindings settings.
 *  KeyBindings settings are loaded and saved in XML format
 *  according to EditorKeyBindings-1_0.dtd.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 * 
 * @deprecated Use Editor Settings Storage API instead.
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
    
    private List getKBList(){
      Settings.KitAndValue[] kav = Settings.getValueHierarchy(base.getKitClass(), SettingsNames.KEY_BINDING_LIST);
      List kbList = null;
      for (int i = 0; i < kav.length; i++) {
          if (kav[i].kitClass == base.getKitClass()) {
              kbList = (List)kav[i].value;
          }
      }
      if (kbList == null) {
          kbList = new ArrayList();
      }

      // must convert all members to serializable MultiKeyBinding
      int cnt = kbList.size();
      for (int i = 0; i < cnt; i++) {
          Object o = kbList.get(i);
          if (!(o instanceof MultiKeyBinding) && o != null) {
              JTextComponent.KeyBinding b = (JTextComponent.KeyBinding)o;
              kbList.set(i, new MultiKeyBinding(b.key, b.actionName));
          }
      }
      return new ArrayList( kbList );
    }   
    
    /** Loads settings from XML file.
     * @param propagate if true - propagates the loaded settings to Editor UI */
    protected void loadSettings(boolean propagate){
        assert false : "KeyBindingsMIMEOptionFile should not be used anymore. " + //NOI18N
            "Please file a bug (http://www.netbeans.org/community/issues.html) " + //NOI18N
            "for editor/settings and attach this stacktrace to it."; //NOI18N
        
        synchronized (Settings.class) {
            Document doc = dom;
            Element rootElement = doc.getDocumentElement();

            if (!TAG_ROOT.equals(rootElement.getTagName())) {
                // Wrong root element
                return;
            }

            // gets current keyBindings map
            List keybs = getKBList();
            Map mapa = OptionUtilities.makeKeyBindingsMap(keybs);
            properties.clear();

            NodeList bind = rootElement.getElementsByTagName(TAG_BIND);
            int len = bind.getLength();
            for (int i=0; i<len; i++){
                Node node = bind.item(i);
                Element bindElement = (Element)node;

                if (bindElement == null){
                    continue;
                }

                String key    = bindElement.getAttribute(ATTR_KEY);
                String delete    = bindElement.getAttribute(ATTR_REMOVE);
                String actionName = bindElement.getAttribute(ATTR_ACTION_NAME);
                if (actionName==null) actionName="";


                if ((actionName.length() != 0) && (!Boolean.valueOf(delete).booleanValue())){
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
                    setLoaded(true);
                    base.setKeyBindingList(new ArrayList(mapa.values()), false);
                }
            }
            if (propagate) setLoaded(true);        
        }
    }
    
    /** Save settings to XML file 
     *  @param changedProp the Map of settings to save */
    protected void updateSettings(Map changedProp){
        assert false : "KeyBindingsMIMEOptionFile should not be used anymore. " + //NOI18N
            "Please file a bug (http://www.netbeans.org/community/issues.html) " + //NOI18N
            "for editor/settings and attach this stacktrace to it."; //NOI18N
        
        synchronized (Settings.class) {
            Document doc = XMLUtil.createDocument(TAG_ROOT, null, processor.getPublicID(), processor.getSystemID());
            
            // put changed properties to local map
            properties.putAll(changedProp);

            // now we can save local map to XML file
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
                    String realKey = tryRemoveKeyFromMap(doc, properties, key, defaultKeybs, rootElem);
                    if (realKey != null) {
                        removed.add(realKey);
                        key = realKey;
                    }

                    // if property is not in default set, it will not be written and will be deleted
                    continue;
                }

                if (properties.get(key) instanceof MultiKeyBinding){
                    MultiKeyBinding mkb = (MultiKeyBinding) properties.get(key);
                    String curActionName= mkb.actionName;
                    if (curActionName == null) curActionName=""; //NOI18N

                    boolean save = true;
                    if (defaultKeybs.get(key) instanceof MultiKeyBinding){
                        String defActionName = ((MultiKeyBinding)defaultKeybs.get(key)).actionName;

                        boolean hasKey = defaultKeybs.containsKey(key);
                        //Also look for permutations, i.e. CA-F5 may be DA-F5, AD-F5 or AC-F5
                        if (!hasKey) {
                            String[] s = getPermutations (key);
                            for (int j=0; j < s.length && !hasKey; j++) {
                                hasKey |= defaultKeybs.containsKey(s[j]);
                                if (hasKey) {
                                    key = s[j];
                                    break;
                                }
                            }
                        }

                        // if property is in default set and the action names are the same we don't have to write it
                        if (hasKey && curActionName.equals(defActionName)) save = false;
                    }

                    if (save){
                        Element keybElem = doc.createElement(TAG_BIND);
                        keybElem.setAttribute(ATTR_KEY, key);
                        keybElem.setAttribute(ATTR_ACTION_NAME, curActionName);
                        rootElem.appendChild(keybElem);
                    }
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
    
    private static String tryRemoveKeyFromMap (Document doc, Map props, String key, Map defaultKeybs, Element root) {
        // if deleted property is in default set, mark it as deleted
        if (defaultKeybs.containsKey(key)){
            removeKeyFromMap (doc, props, key, root);
            return key;
        } else {
            String[] s = getPermutations(key);
            for (int i=0; i < s.length; i++) {
                if (defaultKeybs.containsKey(s[i])){
                    removeKeyFromMap (doc, props, key, root);
                    return s[i];
                }
            }
        }
        return null;
    }
    
    private static void removeKeyFromMap(Document doc, Map props, String key, Element root) {
        Element keybElem = doc.createElement(TAG_BIND);
        keybElem.setAttribute(ATTR_KEY, key);
        keybElem.setAttribute(ATTR_REMOVE, Boolean.TRUE.toString());
        root.appendChild(keybElem);        
    }
    
    /**
     * There is no required ordering of key modifiers (C, M, S, A), and the
     * D (default) wildcard character can map to either C or M depending on 
     * the platform.  So when we need to delete a keybinding, the editor has
     * given us one possible ordering, but not necessarily the correct one; it
     * has also given us a hard keybinding, but the key may really be bound to
     * D.  So, for "MAS-F5" (meta-alt-shift F5) on the pc, we need to check
     * MSA-F5, SMA-F5, SAM-F5, AMS-F5, ASM-F5; on the mac, we also need to check
     * the same permutations of DAS-F5, since it may be registered with the 
     * wildcard character.
     * <p>
     * Finally, for each permutation, it is legal to separate characters with
     * dashes - so for each permutation, we must also check for a hyphenated
     * variant - i.e. for MAS-F5, we must check M-A-S-F5.  Note that mixed
     * hyphenation (M-AS-F5) is not supported.  It either is or it isn't.
     *
     */
    static String[] getPermutations (String name) {
        //IMPORTANT: THERE IS A COPY OF THE SAME CODE IN 
        //org.netbeans.core.ShortcutsFolder (it has unit tests there)
        //ANY CHANGES MADE HERE SHOULD ALSO BE MADE THERE!
        String key = KeyEvent.META_MASK == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ?
            "M" : "C"; //NOI18N
        
        String ctrlWildcard = "D"; //NOI18N
        
        String altKey = System.getProperty ("mrj.version") != null ?
            "C" : "A"; //NOI18N
        
        String altWildcard = "O"; //NOI18N
        
        
        int pos = name.lastIndexOf ("-"); //NOI18N
        if (pos == -1) {
            //#49590 - key binding like "F11" will not have modifiers
            return new String[] { name };
        }
        String keyPart = name.substring (pos);
        String modsPart = name.substring(0, pos).replace("-", ""); //NOI18N
        if (modsPart.length() > 1) {
            Collection perms = new HashSet(modsPart.length() * modsPart.length());
            int idx = name.indexOf(key);
            if (idx != -1) {
                //First, try with the wildcard key.  Remove all hyphens - we'll
                //put them back later
                StringBuffer sb = new StringBuffer(modsPart);
                sb.replace(idx, idx+1, ctrlWildcard);
                perms.add (sb.toString() + keyPart);
                getAllPossibleOrderings (sb.toString(), keyPart, perms);
                createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                idx = name.indexOf (altKey);
                if (idx != -1) {
                    sb.replace (idx, idx+1, altWildcard);
                    perms.add (sb.toString() + keyPart);
                    getAllPossibleOrderings (sb.toString(), keyPart, perms);
                    createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                } else {
                    idx = name.indexOf(altWildcard);
                    if (idx != -1) {
                        sb.replace (idx, idx+1, altKey);
                        perms.add (sb.toString() + keyPart);
                        getAllPossibleOrderings (sb.toString(), keyPart, perms);
                        createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                    }
                }                
            } else {
                idx = name.indexOf (ctrlWildcard); //NOI18N
                if (idx != -1) {
                    StringBuffer sb = new StringBuffer(modsPart);
                    sb.replace(idx, idx+1, key);
                    perms.add (sb.toString() + keyPart);
                    getAllPossibleOrderings (sb.toString(), keyPart, perms);
                    createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                    idx = name.indexOf (altKey);
                    if (idx != -1) {
                        sb.replace (idx, idx+1, altWildcard);
                        perms.add (sb.toString() + keyPart);
                        getAllPossibleOrderings (sb.toString(), keyPart, perms);
                    } else {
                        idx = name.indexOf(altWildcard);
                        if (idx != -1) {
                            sb.replace (idx, idx+1, altKey);
                            perms.add (sb.toString() + keyPart);
                            getAllPossibleOrderings (sb.toString(), keyPart, perms);
                            createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                        }
                    }                    
                }
            }
            
            idx = name.indexOf (altKey);
            if (idx != -1) {
                StringBuffer sb = new StringBuffer(modsPart);
                sb.replace (idx, idx+1, altWildcard);
                perms.add (sb.toString() + keyPart);
                getAllPossibleOrderings (sb.toString(), keyPart, perms);
                createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
            } else {
                StringBuffer sb = new StringBuffer(modsPart);
                idx = name.indexOf(altWildcard);
                if (idx != -1) {
                    sb.replace (idx, idx+1, altKey);
                    perms.add (sb.toString() + keyPart);
                    getAllPossibleOrderings (sb.toString(), keyPart, perms);
                    createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                }
            }
            
            getAllPossibleOrderings (modsPart, keyPart, perms);
            createHyphenatedPermutation(modsPart.toCharArray(), perms, keyPart);
            return (String[]) perms.toArray(new String[perms.size()]);
        } else {
            return key.equals (modsPart) ?
                new String[] {ctrlWildcard + keyPart} : altKey.equals(modsPart) ?
                    new String[]{altWildcard + keyPart} : altWildcard.equals(modsPart) ? 
                    new String[] {altKey + keyPart} : 
                    new String[0];
        }
    }
    
    /**
     * Retrieves all the possible orders for the passed in string, and puts them
     * in the passed collection, appending <code>toAppend</code> to each.
     */
    static void getAllPossibleOrderings (String s, String toAppend, final Collection store) {
        //IMPORTANT: THIS IS A COPY OF THE SAME CODE IN org.netbeans.core.ShortcutsFolder.
        //ANY CHANGES MADE HERE SHOULD ALSO BE MADE THERE!
        char[] c = s.toCharArray();
        mutate (c, store, 0, toAppend);
        String[] result = (String[]) store.toArray(new String[store.size()]);
    }   
    
    /**
     * Recursively generates all possible orderings of the passed char array
     */
    private static void mutate(char[] c, Collection l, int n, String toAppend) {
        //IMPORTANT: THIS IS A COPY OF THE SAME CODE IN org.netbeans.core.ShortcutsFolder.
        //ANY CHANGES MADE HERE SHOULD ALSO BE MADE THERE!
        if (n == c.length) {
            l.add (new String(c) + toAppend);
            createHyphenatedPermutation(c, l, toAppend);
            return;
        }
        //XXX could be optimized to eliminate duplicates
        for (int i=0; i < c.length; i++) {
            char x = c[i];
            c[i] = c[n];
            c[n] = x;
            if (n < c.length) { 
                mutate (c, l, n+1, toAppend);
            } 
        }
    } 
    
    /**
     * Inserts "-" characters between each character in the char array and
     * adds the result + toAppend to the collection.
     */
    static void createHyphenatedPermutation (char[] c, Collection l, String toAppend) {
        //IMPORTANT: THIS IS A COPY OF THE SAME CODE IN org.netbeans.core.ShortcutsFolder.
        //ANY CHANGES MADE HERE SHOULD ALSO BE MADE THERE!
        if (c.length == 1) {
            return;
        }
        StringBuffer sb = new StringBuffer (new String(c));
        for (int i=c.length-1; i >= 1; i-=1) {
            sb.insert (i, '-');
        }
        sb.append (toAppend);
        l.add (sb.toString());
    }    
}
