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

package org.netbeans.modules.httpserver;

import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.MalformedURLException;
import java.util.StringTokenizer;

import org.openide.ErrorManager;
import org.openide.util.SharedClassObject;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;

/** Implementation of a URLMapper which creates http URLs for fileobjects in the IDE.
 * Directs the requests for URLs to WrapperServlet.
 *
 * @author Petr Jiricka
 */
public class HttpServerURLMapper extends URLMapper {
    
    /** Creates a new instance of HttpServerURLMapper */
    public HttpServerURLMapper() {
    }
    
    /** Get an array of FileObjects for this url
     * @param url to wanted FileObjects
     * @return a suitable arry of FileObjects, or null
     */
    public FileObject[] getFileObjects(URL url) {
        String path = url.getPath();

        // remove the wrapper servlet URI
        HttpServerSettings settings = (HttpServerSettings)SharedClassObject.findObject(HttpServerSettings.class, true);
        String wrapper = settings.getWrapperBaseURL ();
        if (path == null || !path.startsWith(wrapper))
            return null;
        path = path.substring(wrapper.length());
        
        // resource name
        if (path.startsWith ("/")) path = path.substring (1); // NOI18N
        
        // extract the encoded filesystem name
        int index = path.indexOf('/');
        if (index == -1)
            return null;
        String fsName = path.substring(0, index);
        FileSystem fs = decodeFileSystemName(fsName);
        if (fs == null)
            return null;

        path = path.substring(index + 1);
        StringTokenizer slashTok = new StringTokenizer(path, "/", true); // NOI18N
        StringBuffer newPath = new StringBuffer();
        for ( ; slashTok.hasMoreTokens(); ) {
            String tok = slashTok.nextToken();
            if (tok.startsWith("/")) { // NOI18N
                newPath.append(tok);
            }
            else {
                newPath.append(URLDecoder.decode(tok));
            }
        }
            
        FileObject fo = fs.findResource(newPath.toString());
        if (fo == null)
            return null;
        
        return new FileObject[] {fo};
    }
    
    /** Get a good URL for this file object which works according to type:
     * -inside this VM
     * - inside this machine
     * - from networked machines 
     * @return a suitable URL, or null
     */            
    public URL getURL(FileObject fileObject, int type) {
        
        // only do external and network URLs
        if (type == URLMapper.INTERNAL)
            return null;
        
        // fileObject must not be null
        if (fileObject == null)
            return null;
        
        // if the file is on the localhost, don't return URL with HTTP
        if (FileUtil.toFile(fileObject) != null)
            return null;
        
        try {
            String encodedFs = encodeFileSystemName(fileObject.getFileSystem());

            String orig = fileObject.getPath ();
            StringTokenizer slashTok = new StringTokenizer(orig, "/", true); // NOI18N
            StringBuffer path = new StringBuffer();
            for ( ; slashTok.hasMoreTokens(); ) {
                String tok = slashTok.nextToken();
                if (tok.startsWith("/")) { // NOI18N
                    path.append(tok);
                }
                else {
                    path.append(URLEncoder.encode(tok));
                }
            }
            if (fileObject.isFolder() &&
                (orig.length() > 0) &&
                !(path.toString().endsWith("/"))) { // NOI18N
                path.append("/"); // NOI18N
            }

            HttpServerSettings settings = (HttpServerSettings)SharedClassObject.findObject(HttpServerSettings.class, true);
            settings.setRunning (true);
            URL newURL = new URL ("http",   // NOI18N
                                  getLocalHost(type), 
                                  settings.getPort (),
                                  settings.getWrapperBaseURL () + encodedFs + "/" + path.toString()); // NOI18N
            return newURL;
        }
        catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
        catch (MalformedURLException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
    }
    
    private static String encodeFileSystemName(FileSystem fs) {
        String fsname = fs.getSystemName();
        return URLEncoder.encode(fsname);
    }

    private static FileSystem decodeFileSystemName(String s) {
        String decoded = URLDecoder.decode(s);
        return Repository.getDefault ().findFileSystem(decoded);
    }

    /** Returns string for localhost */
    private static String getLocalHost(int type) {
        // external URL
        if (URLMapper.EXTERNAL == type) {
            return "localhost"; // NOI18N
        }
        // network URL
        if (URLMapper.NETWORK == type) {
            try {
                return InetAddress.getLocalHost().getHostName();
            }
            catch (UnknownHostException e) {
                return "localhost"; // NOI18N
            }
        }
        // other URL
        throw new IllegalArgumentException("Bad URL type: " + type); // NOI18N
    }

    
}
