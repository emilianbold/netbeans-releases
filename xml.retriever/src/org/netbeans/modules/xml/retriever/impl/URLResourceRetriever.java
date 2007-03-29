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

/*
 * URLResourceRetriever.java
 *
 * Created on January 9, 2006, 10:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import org.netbeans.modules.xml.retriever.*;

/**
 *
 * @author girix
 */
public class URLResourceRetriever implements ResourceRetriever{
    
    private static final String URI_SCHEME = "http"; //NOI18N
    /** Creates a new instance of FileResourceRetriever */
    public URLResourceRetriever() {
    }
    
    public boolean accept(String baseAddr, String currentAddr) throws URISyntaxException {
        URI currURI = new URI(currentAddr);
        if( (currURI.isAbsolute()) && (currURI.getScheme().equalsIgnoreCase(URI_SCHEME)))
            return true;
        if(baseAddr != null){
            if(!currURI.isAbsolute()){
                URI baseURI = new URI(baseAddr);
                if(baseURI.getScheme().equalsIgnoreCase(URI_SCHEME))
                    return true;
            }
        }
        return false;
    }
    
    public HashMap<String, InputStream> retrieveDocument(String baseAddress,
            String documentAddress) throws IOException,URISyntaxException{
        
        String effAddr = getEffectiveAddress(baseAddress, documentAddress);
        if(effAddr == null)
            return null;
        URI currURI = new URI(effAddr);
        HashMap<String, InputStream> result = null;
        
        InputStream is = getInputStreamOfURL(currURI.toURL(), ProxySelector.
                getDefault().select(currURI).get(0));
        result = new HashMap<String, InputStream>();
        result.put(effectiveURL.toString(), is);
        return result;
        
    }
    
    long streamLength = 0;
    URL effectiveURL = null;
    public InputStream getInputStreamOfURL(URL downloadURL, Proxy proxy) throws IOException{
        
        URLConnection ucn = null;
        
        if(Thread.currentThread().isInterrupted())
            return null;
        if(proxy != null)
            ucn = downloadURL.openConnection(proxy);
        else
            ucn = downloadURL.openConnection();
        HttpURLConnection hucn = null;
        if(ucn instanceof HttpURLConnection){
            hucn = ((HttpURLConnection)ucn);
            hucn.setFollowRedirects(false);
        }
        if(Thread.currentThread().isInterrupted())
            return null;
        ucn.connect();
        //follow HTTP redirects
        while( (hucn.getResponseCode() == hucn.HTTP_MOVED_TEMP) ||
                (hucn.getResponseCode() == hucn.HTTP_MOVED_PERM) ) {
            String addr = hucn.getHeaderField("Location");
            downloadURL = new URL(addr);
            if(proxy != null)
                ucn = downloadURL.openConnection(proxy);
            else
                ucn = downloadURL.openConnection();
            if(ucn instanceof HttpURLConnection){
                hucn = ((HttpURLConnection)ucn);
                hucn.setFollowRedirects(false);
            }
            if(Thread.currentThread().isInterrupted())
                return null;
            ucn.connect();
        }
        ucn.setReadTimeout(10000);
        InputStream is = ucn.getInputStream();
        streamLength = ucn.getContentLength();
        effectiveURL = ucn.getURL();
        return is;
        
    }
    
    public long getStreamLength() {
        return streamLength;
    }
    
    public String getEffectiveAddress(String baseAddress, String documentAddress) throws IOException, URISyntaxException {
        return resolveURL(baseAddress, documentAddress);
    }
    
    public static String resolveURL(String baseAddress, String documentAddress) throws URISyntaxException{
        URI currURI = new URI(documentAddress);
        String result = null;
        if(currURI.isAbsolute()){
            result = currURI.toString();
            return result;
        }else{
            //relative URI
            if(baseAddress != null){
                URI baseURI = new URI(baseAddress);
                URI finalURI = baseURI.resolve(currURI);
                result = finalURI.toString();
                return result;
            }else{
                //neither the current URI nor the base URI are absoulte. So, can not resolve this
                //path
                return null;
            }
        }
    }
}
