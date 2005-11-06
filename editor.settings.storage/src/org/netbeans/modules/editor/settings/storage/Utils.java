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

package org.netbeans.modules.editor.settings.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Utilities;


/**
 * This class contains support static methods for loading / saving and 
 * translating coloring (fontsColors.xml) files. It calls XMLStorage utilities.
 *
 * @author Jan Jancura
 */
public class Utils {
    
    static String keyStrokesToString (List /*<KeyStroke[]>*/ keys) {
        StringBuffer sb = new StringBuffer ();
        Iterator it = keys.iterator ();
        if (it.hasNext ()) {
            sb.append (Utilities.keyToString ((KeyStroke) it.next ()));
            //S ystem.out.println("2 " + keys [0] + ">" + Utilities.keyToString (keys [0]));
            while (it.hasNext ()) {
                sb.append ('$').
                    append (Utilities.keyToString ((KeyStroke) it.next ()));
                //S ystem.out.println("2 " + keys [i] + ">" + Utilities.keyToString (keys [i]));
            }
        }
        return sb.toString ();
    }
    
    static KeyStroke[] stringToKeyStrokes (String key) {
        StringTokenizer st = new StringTokenizer (key, "$");
        List result = new ArrayList ();
        key = null;
        while (st.hasMoreTokens ()) {
            String ks = st.nextToken ().trim ();
            KeyStroke keyStroke = Utilities.stringToKey (ks);
            //S ystem.out.println("1 " + ks + ">" + keyStroke);
            if (keyStroke == null) {
                if (System.getProperty ("org.netbeans.optionsDialog") != null)
                    System.out.println("no key stroke for:" + key);
                continue;
            }
//            if (key == null)
//                key = Utilities.keyToString (keyStroke);
//            else
//                key += "$" + Utilities.keyToString (keyStroke);
            result.add (keyStroke);
        }
        return (KeyStroke[]) result.toArray 
            (new KeyStroke [result.size ()]);
    }
    
    static FileObject getFileObject (
        String[] mimeTypes, 
        String profile,
        String fileNameExt
    ) {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        String folderName = Utils.getFolderName (mimeTypes, profile);
        if (folderName == null) return null;
        if (fileNameExt == null)
            return fs.findResource (folderName);
        return fs.findResource (
            folderName + fileNameExt
        );
    }
    
    /**
     * Crates FileObject for given mimeTypes and profile.
     */ 
    static String getFolderName (
        String[] mimeTypes, 
        String profile
    ) {
        StringBuffer sb = new StringBuffer ();
        sb.append ("Editors");
        int i, k = mimeTypes.length;
        for (i = 0; i < k; i++)
            sb.append ('/').append (mimeTypes [i]);
        if (profile != null)
            sb.append ('/').append (profile);
        return sb.append ('/').toString ();
    }
    
    /**
     * Crates FileObject for given mimeTypes and profile.
     */ 
    static FileObject createFileObject (
        String[] mimeTypes, 
        String profile,
        String fileName
    ) {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        try {
            FileObject fo = getFO (fs.getRoot (), "Editors");
            int i, k = mimeTypes.length;
            for (i = 0; i < k; i++)
                fo = getFO (fo, mimeTypes [i]);
            if (profile != null)
                fo = getFO (fo, profile);
            if (fileName == null)
                return fo;
            FileObject fo1 = fo.getFileObject (fileName);
            if (fo1 != null) return fo1;
            return fo.createData (fileName);
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
            return null;
        }
    }
    
    /**
     * Crates FileObject for given mimeTypes and profile.
     */ 
    static void deleteFileObject (
        String[] mimeTypes, 
        String profile,
        String fileName
    ) {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        try {
            FileObject fo = getFO (fs.getRoot (), "Editors");
            int i, k = mimeTypes.length;
            for (i = 0; i < k; i++)
                fo = getFO (fo, mimeTypes [i]);
            if (profile != null)
                fo = getFO (fo, profile);
            fo = fo.getFileObject (fileName);
            if (fo == null) return;
            fo.delete ();
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
       
    private static FileObject getFO (FileObject fo, String next) throws IOException {
        FileObject fo1 = fo.getFileObject (next);
        if (fo1 == null) 
            return fo.createFolder (next);
        return fo1;
    }
}
