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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


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
    
    static FileObject getFileObject(MimePath mimePath, String profile, String fileNameExt) {
        String name = getFileName(mimePath, profile, fileNameExt);
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        return fs.findResource(name);
    }
    
    /**
     * Crates FileObject for given mimeTypes and profile.
     */ 
    static FileObject createFileObject(MimePath mimePath, String profile, String fileName) {
        String name = getFileName(mimePath, profile, fileName);
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        try {
            if (fileName == null) {
                return FileUtil.createFolder(fs.getRoot(), name);
            } else {
                return FileUtil.createData(fs.getRoot(), name);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Can't create editor settings file or folder: " + name, ex); //NOI18N
            return null;
        }
    }
    
    /**
     * Crates FileObject for given mimeTypes and profile.
     */ 
    static void deleteFileObject(MimePath mimePath, String profile, String fileName) {
        String name = getFileName(mimePath, profile, fileName);
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = fs.findResource(name);
        if (fo != null) {
            try {
                fo.delete();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Can't delete editor settings file " + fo.getPath(), ex); //NOI18N
            }
        }
    }
    
    static String getFileName(MimePath mimePath, String profile, String fileName) {
        StringBuilder sb = new StringBuilder("Editors");
        
        if (mimePath.size() > 0) {
            sb.append('/').append(mimePath.getPath());
        }
        
        if (profile != null) {
            sb.append('/').append(profile);
        }
        
        if (fileName != null) {
            sb.append('/').append(fileName);
        }
        
        return sb.toString();
    }

    private static FileObject createFile (FileObject fo, String next) throws IOException {
        FileObject fo1 = fo.getFileObject (next);
        if (fo1 == null) 
            return fo.createFolder (next);
        return fo1;
    }
    
    static String getLocalizedName(FileObject fo, String key, String defaultValue) {
        assert key != null : "The key can't be null"; //NOI18N

        Object [] bundleInfo = findResourceBundle(fo);
        if (bundleInfo[1] != null) {
            try {
                return ((ResourceBundle) bundleInfo[1]).getString(key);
            } catch (MissingResourceException ex) {
                LOG.log(Level.WARNING, "The bundle '" + bundleInfo[0] + "' is missing key '" + key + "'.", ex); //NOI18N
            }
        }
        
        return defaultValue;
    }

    private static final WeakHashMap<FileObject, Object []> bundleInfos = new WeakHashMap<FileObject, Object []>();
    private static Object [] findResourceBundle(FileObject fo) {
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
                        LOG.log(Level.WARNING, "Can't find resource bundle for " + fo.getPath(), ex); //NOI18N
                    }
                } else {
                    //[PENDING][HACK] LOG.log(Level.WARNING, "The file " + fo.getPath() + " does not specify its resource bundle.", new Throwable("@@@")); //NOI18N
                }

                if (bundleInfo == null) {
                   bundleInfo = new Object [] { bundleName, null }; 
                }

                bundleInfos.put(fo, bundleInfo);
            }

            return bundleInfo;
        }
    }
}
