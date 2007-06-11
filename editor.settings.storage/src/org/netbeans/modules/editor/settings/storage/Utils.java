/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.settings.storage;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;


/**
 * This class contains support static methods for loading / saving and 
 * translating coloring (fontsColors.xml) files. It calls XMLStorage utilities.
 *
 * @author Jan Jancura
 */
public class Utils {
    
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());
    
    private static final Map<Color, String> colorToName = new HashMap<Color, String>();
    private static final Map<String, Color> nameToColor = new HashMap<String, Color>();
    private static final Map<String, Integer> nameToFontStyle = new HashMap<String, Integer>();
    private static final Map<Integer, String> fontStyleToName = new HashMap<Integer, String>();
    static {
        colorToName.put (Color.black, "black");
        nameToColor.put ("black", Color.black);
        colorToName.put (Color.blue, "blue");
        nameToColor.put ("blue", Color.blue);
        colorToName.put (Color.cyan, "cyan");
        nameToColor.put ("cyan", Color.cyan);
        colorToName.put (Color.darkGray, "darkGray");
        nameToColor.put ("darkGray", Color.darkGray);
        colorToName.put (Color.gray, "gray");
        nameToColor.put ("gray", Color.gray);
        colorToName.put (Color.green, "green");
        nameToColor.put ("green", Color.green);
        colorToName.put (Color.lightGray, "lightGray");
        nameToColor.put ("lightGray", Color.lightGray);
        colorToName.put (Color.magenta, "magenta");
        nameToColor.put ("magenta", Color.magenta);
        colorToName.put (Color.orange, "orange");
        nameToColor.put ("orange", Color.orange);
        colorToName.put (Color.pink, "pink");
        nameToColor.put ("pink", Color.pink);
        colorToName.put (Color.red, "red");
        nameToColor.put ("red", Color.red);
        colorToName.put (Color.white, "white");
        nameToColor.put ("white", Color.white);
        colorToName.put (Color.yellow, "yellow");
        nameToColor.put ("yellow", Color.yellow);
        
        nameToFontStyle.put ("plain", new Integer (Font.PLAIN));
        fontStyleToName.put (new Integer (Font.PLAIN), "plain");
        nameToFontStyle.put ("bold", new Integer (Font.BOLD));
        fontStyleToName.put (new Integer (Font.BOLD), "bold");
        nameToFontStyle.put ("italic", new Integer (Font.ITALIC));
        fontStyleToName.put (new Integer (Font.ITALIC), "italic");
        nameToFontStyle.put ("bold+italic", new Integer (Font.BOLD + Font.ITALIC));
        fontStyleToName.put (new Integer (Font.BOLD + Font.ITALIC), "bold+italic");
    }
    
    static String colorToString (Color color) {
	if (colorToName.containsKey (color))
	    return (String) colorToName.get (color);
	return Integer.toHexString (color.getRGB ());
    }
    
    static Color stringToColor (String color) throws Exception {
	if (nameToColor.containsKey (color))
	    return (Color) nameToColor.get (color);
        try {
            return new Color ((int) Long.parseLong (color, 16));
        } catch (NumberFormatException ex) {
            throw new Exception ();
        }
    }
    
    static String keyStrokesToString (Collection<KeyStroke> keys) {
        StringBuffer sb = new StringBuffer ();
        
        Iterator<KeyStroke> it = keys.iterator();
        if (it.hasNext ()) {
            sb.append(Utilities.keyToString(it.next()));
            //S ystem.out.println("2 " + keys [0] + ">" + Utilities.keyToString (keys [0]));
            while (it.hasNext()) {
                sb.append ('$').
                    append(Utilities.keyToString(it.next()));
                //S ystem.out.println("2 " + keys [i] + ">" + Utilities.keyToString (keys [i]));
            }
        }
        
        return sb.toString ();
    }
    
    static KeyStroke[] stringToKeyStrokes (String key) {
        StringTokenizer st = new StringTokenizer (key, "$");
        List<KeyStroke> result = new ArrayList<KeyStroke>();
        key = null;
        while (st.hasMoreTokens ()) {
            String ks = st.nextToken ().trim ();
            KeyStroke keyStroke = Utilities.stringToKey (ks);
            //S ystem.out.println("1 " + ks + ">" + keyStroke);
            if (keyStroke == null) {
                LOG.fine("no key stroke for:" + key);
                continue;
            }
//            if (key == null)
//                key = Utilities.keyToString (keyStroke);
//            else
//                key += "$" + Utilities.keyToString (keyStroke);
            result.add(keyStroke);
        }
        return result.toArray(new KeyStroke[result.size ()]);
    }
    
    public static String getLocalizedName(FileObject fo, String defaultValue) {
        try {
            return fo.getFileSystem().getStatus().annotateName(defaultValue, Collections.singleton(fo));
        } catch (FileStateInvalidException ex) {
            if (LOG.isLoggable(Level.INFO)) {
                logOnce(LOG, Level.INFO, "Can't find localized name of " + fo, ex); //NOI18N
            }
            return defaultValue;
        }
    }
    
    public static String getLocalizedName(FileObject fo, String key, String defaultValue) {
        return getLocalizedName(fo, key, defaultValue, false);
    }
    
    public static String getLocalizedName(FileObject fo, String key, String defaultValue, boolean silent) {
        assert key != null : "The key can't be null"; //NOI18N

        Object [] bundleInfo = findResourceBundle(fo, silent);
        if (bundleInfo[1] != null) {
            try {
                return ((ResourceBundle) bundleInfo[1]).getString(key);
            } catch (MissingResourceException ex) {
                if (!silent && LOG.isLoggable(Level.INFO)) {
                    logOnce(LOG, Level.INFO, "The bundle '" + bundleInfo[0] + "' is missing key '" + key + "'.", ex); //NOI18N
                }
            }
        }
        
        return defaultValue;
    }

    private static final WeakHashMap<FileObject, Object []> bundleInfos = new WeakHashMap<FileObject, Object []>();
    private static final FileChangeListener listener = new FileChangeAdapter() {
        @Override
        public void fileDeleted(FileEvent fe) {
            synchronized (bundleInfos) {
                bundleInfos.remove(fe.getFile());
            }
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            if (fe.getName() != null && fe.getName().equals("SystemFileSystem.localizingBundle")) { //NOI18N
                synchronized (bundleInfos) {
                    bundleInfos.remove(fe.getFile());
                }
            }
        }
    };
    private static final FileChangeListener weakListener = WeakListeners.create(FileChangeListener.class, listener, null);
    private static Object [] findResourceBundle(FileObject fo, boolean silent) {
        assert fo != null : "FileObject can't be null"; //NOI18N
        
        synchronized (bundleInfos) {
            Object [] bundleInfo = bundleInfos.get(fo);
            if (bundleInfo == null) {
                String bundleName = null;
                Object attrValue = fo.getAttribute("SystemFileSystem.localizingBundle"); //NOI18N
                if (attrValue instanceof String) {
                    bundleName = (String) attrValue;
                }

                if (bundleName != null) {
                    try {
                        bundleInfo = new Object [] { bundleName, NbBundle.getBundle(bundleName) };
                    } catch (MissingResourceException ex) {
                        if (!silent && LOG.isLoggable(Level.INFO)) {
                            logOnce(LOG, Level.INFO, "Can't find resource bundle for " + fo.getPath(), ex); //NOI18N
                        }
                    }
                } else {
                    if (!silent && LOG.isLoggable(Level.FINE)) {
                        logOnce(LOG, Level.FINE, "The file " + fo.getPath() + " does not specify its resource bundle.", null); //NOI18N
                    }
                }

                if (bundleInfo == null) {
                   bundleInfo = new Object [] { bundleName, null }; 
                }

                bundleInfos.put(fo, bundleInfo);
                fo.removeFileChangeListener(weakListener);
                fo.addFileChangeListener(weakListener);
            }

            return bundleInfo;
        }
    }
    
    private static final Set<String> ALREADY_LOGGED = Collections.synchronizedSet(new HashSet<String>());
    public static void logOnce(Logger logger, Level level, String msg, Throwable t) {
        if (!ALREADY_LOGGED.contains(msg)) {
            ALREADY_LOGGED.add(msg);
            if (t != null) {
                logger.log(level, msg, t);
            } else {
                logger.log(level, msg);
            }
            
            if (ALREADY_LOGGED.size() > 100) {
                ALREADY_LOGGED.clear();
            }
        }
    }
    
    /**
     * Converts an array of mime types to a <code>MimePath</code> instance.
     */
    public static MimePath mimeTypes2mimePath(String[] mimeTypes) {
        MimePath mimePath = MimePath.EMPTY;
        
        for (int i = 0; i < mimeTypes.length; i++) {
            mimePath = MimePath.get(mimePath, mimeTypes[i]);
        }
        
        return mimePath;
    }

    /**
     * Creates unmodifiable copy of the original map converting <code>AttributeSet</code>s
     * to their immutable versions.
     */
    public static Map<String, AttributeSet> immutize(Map<String, ? extends AttributeSet> map, Object... filterOutKeys) {
        Map<String, AttributeSet> immutizedMap = new HashMap<String, AttributeSet>();
        
        for(String name : map.keySet()) {
            AttributeSet attribs = map.get(name);
            
            if (filterOutKeys.length == 0) {
                immutizedMap.put(name, AttributesUtilities.createImmutable(attribs));
            } else {
                List<Object> pairs = new ArrayList<Object>();

                // filter out attributes specified by filterOutKeys
                first:
                for(Enumeration<? extends Object> keys = attribs.getAttributeNames(); keys.hasMoreElements(); ) {
                    Object key = keys.nextElement();
                    
                    for(Object filterOutKey : filterOutKeys) {
                        if (Utilities.compareObjects(key, filterOutKey)) {
                            continue first;
                        }
                    }
                    
                    pairs.add(key);
                    pairs.add(attribs.getAttribute(key));
                }

                immutizedMap.put(name, AttributesUtilities.createImmutable(pairs.toArray()));
            }
        }
        
        return Collections.unmodifiableMap(immutizedMap);
    }
    
    public static Map<String, AttributeSet> immutize(Collection<AttributeSet> set) {
        Map<String, AttributeSet> immutizedMap = new HashMap<String, AttributeSet>();
    
        for(AttributeSet as : set) {
            Object nameObject = as.getAttribute(StyleConstants.NameAttribute);
            if (nameObject instanceof String) {
                immutizedMap.put((String) nameObject, as);
            } else {
                LOG.warning("Ignoring AttributeSet with invalid StyleConstants.NameAttribute. AttributeSet: " + as); //NOI18N
            }
        }
            
        return Collections.unmodifiableMap(immutizedMap);
    }
}
