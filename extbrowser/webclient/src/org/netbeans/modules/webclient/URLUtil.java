/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.webclient;

import java.net.URL;
import java.net.MalformedURLException;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/** Utility class for various useful URL-related tasks.
 * !!! COPIED from extbrowser.
 *
 * @author Petr Jiricka
 */
public class URLUtil {
    
    /** Creates a URL that is suitable for using in a different process on the 
     * same node, similarly to URLMapper.EXTERNAL. May just return the original 
     * URL if that's good enough.
     */
    public static URL createExternalURL(URL url) {
        if (url == null)
            return null;

        // return if the protocol is fine
        if ("http".equals (url.getProtocol ())   // NOI18N
        ||  "ftp".equals (url.getProtocol ()))   // NOI18N
            return url;
        
        // remove the anchor
        String anchor = url.getRef();
        String urlString = url.toString ();
        int ind = urlString.indexOf('#');
        if (ind >= 0) {
            urlString = urlString.substring(0, ind);
        }
        
        // map to an external URL using the anchor-less URL
        try {
            FileObject fos[] = URLMapper.findFileObjects(new URL(urlString));
            if ((fos != null) && (fos.length > 0)) {
                URL newUrl = URLMapper.findURL(fos[0], URLMapper.EXTERNAL);
                if (newUrl != null) {
                    // re-add the anchor if exists
                    urlString = newUrl.toString();
                    if (ind >=0) {
                        urlString = urlString + "#" + anchor;
                    }
                    return new URL(urlString);
                }
            }
        }
        catch (MalformedURLException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
        return url;
    }
    

    /**
     * Returns whether given protocol is internal or not. 
     * (Internal protocols cannot be displayed by external viewers.
     * They must be wrapped somehow.)
     *
     * @return true if protocol is internal, false otherwise
     */
/*    private static boolean isInternalProtocol (String protocol) {
        // internal protocols cannot be displayed in external viewer
        if (protocol.equals ("nbfs")               // NOI18N
        ||  protocol.equals ("nbres")              // NOI18N
        ||  protocol.equals ("nbrescurr")          // NOI18N
        ||  protocol.equals ("nbresloc")           // NOI18N
        ||  protocol.equals ("nbrescurrloc"))      // NOI18N
            return true;
        
        if (protocol.startsWith ("nb"))            // NOI18N
            return true;
        
        return false;
    }*/
    
    
}
