/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/** Utility class for various useful URL-related tasks.
 * <p>There is similar class in extbrowser/webclient module doing almost the same work.
 * @author Petr Jiricka
 */
public class URLUtil {
    
    /** results with URLMapper instances*/
    private static Lookup.Result result;            
    
    static {
        result = Lookup.getDefault().lookup(new Lookup.Template (URLMapper.class));
    }            
    
    /** Creates a URL that is suitable for using in a different process on the
     * same node, similarly to URLMapper.EXTERNAL. May just return the original
     * URL if that's good enough.
     * @param url URL that needs to be displayed in browser
     * @param allowJar is <CODE>jar:</CODE> acceptable protocol?
     * @return browsable URL or null
     */
    public static URL createExternalURL(URL url, boolean allowJar) {
        if (url == null)
            return null;

        // return if the protocol is fine
        if (isAcceptableProtocol(url.getProtocol().toLowerCase(), allowJar))
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
            FileObject fo = URLMapper.findFileObject(new URL(urlString));
            if (fo != null) {
                URL newUrl = getURLOfAppropriateType(fo, allowJar);
                if (newUrl != null) {
                    // re-add the anchor if exists
                    urlString = newUrl.toString();
                    if (ind >=0) {
                        urlString = urlString + "#" + anchor; // NOI18N
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
    
    /** Returns a URL for the given file object that can be correctly interpreted
     * by usual web browsers (including Netscape 4.71, IE and Mozilla).
     * First attempts to get an EXTERNAL URL, if that is a suitable URL, it is used;
     * otherwise a NETWORK URL is used.
     */
    private static URL getURLOfAppropriateType(FileObject fo, boolean allowJar) {
        // PENDING - there is still the problem that the HTTP server will be started 
        // (because the HttpServerURLMapper.getURL(...) method starts it), 
        // even when it is not needed
        URL retVal;
        URL suitable = null;
        
        Iterator instances = result.allInstances ().iterator();                
        while (instances.hasNext()) {
            URLMapper mapper = (URLMapper) instances.next();
            retVal = mapper.getURL (fo, URLMapper.EXTERNAL);
            if ((retVal != null) && isAcceptableProtocol(retVal.getProtocol().toLowerCase(), allowJar)) {
                // return if this is a 'file' or 'jar' URL
                String p = retVal.getProtocol().toLowerCase();
                if ("file".equals(p) || "jar".equals(p)) { // NOI18N
                    return retVal;
                }
                suitable = retVal;
            }
        }
        
        // if we found a suitable URL, return it
        if (suitable != null) {
            return suitable;
        }
        
        return URLMapper.findURL(fo, URLMapper.NETWORK);
    }
        
    /** Returns true if the protocol is acceptable for usual web browsers.
     * Specifically, returns true for file, http and ftp protocols.
     */
    private static boolean isAcceptableProtocol(String protocol, boolean allowJar) {
        if ("http".equals(protocol)          // NOI18N
        ||  "ftp".equals(protocol)           // NOI18N
        ||  "file".equals(protocol))         // NOI18N
            return true;
        if (allowJar && "jar".equals(protocol)) 
            return true;
        
        return false;
    }
    
    /** Determines whether a given browser is capable of displaying URLs with the jar: protocol.
     *  Currently, Mozilla and Firefox can do this.
     *  @param browser browser id - one of the constants defined in ExtWebBrowser
     *  @return true if the browser handles jar URLs
     */
    public static boolean browserHandlesJarURLs(String browser) {
        return (ExtWebBrowser.MOZILLA.equals(browser) ||
                ExtWebBrowser.FIREFOX.equals(browser));
    }

}
