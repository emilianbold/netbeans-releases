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

package org.netbeans.core.xml;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;

/** Entity resolver which loads entities (typically DTDs) from fixed
 * locations in the system file system, according to public ID.
 *
 * @author  Jaroslav Tulach
 */
public class FileEntityResolver implements EntityResolver {
    private static final String ENTITY_PREFIX = "/xml/entities"; // NOI18N
    
    /** Constructor
     */
    public FileEntityResolver() {
    }
    
    /** Tries to find the entity on system file system.
     */
    public InputSource resolveEntity(String publicID, String systemID) throws IOException, SAXException {
        String id = convertPublicId (publicID);
        
        StringBuffer sb = new StringBuffer (200);
        sb.append (ENTITY_PREFIX);
        sb.append (id);
        
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().findResource (sb.toString ());
        if (fo != null) {
            return new InputSource (fo.getInputStream ());
        } else {
            return null;
        }
    }
    

    /** Converts the publicID into filesystem friendly name.
     * 
     */
    private static String convertPublicId (String publicID) {
        char[] arr = publicID.toCharArray ();


        int numberofslashes = 0;
        int state = 0;
        int write = 0;
        OUT: for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];

            switch (state) {
            case 0:
                // initial state 
                if (ch == '+' || ch == '-') {
                    // do not write that char
                    continue;
                }
                // switch to regular state
                state = 1;
                // fallthru
            case 1:
                // regular state expecting any character
                if (ch == '/') {
                    state = 2;
                    if (++numberofslashes == 3) {
                        // last part of the ID, exit
                        break OUT;
                    }
                    arr[write++] = '/';
                    continue;
                }
                break;
            case 2:
                // previous character was /
                if (ch == '/') {
                    // ignore second / and write nothing
                    continue;
                }
                state = 1;
                break;
            }

            // write the char into the array
            if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9') {
                arr[write++] = ch;
            } else {
                arr[write++] = '_';
            }
        }

        return new String (arr, 0, write);
    }
    
}
