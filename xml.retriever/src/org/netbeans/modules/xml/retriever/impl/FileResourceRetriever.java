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
 * FileResourceRetriever.java
 *
 * Created on January 9, 2006, 10:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.netbeans.modules.xml.retriever.*;

/**
 *
 * @author girix
 */
public class FileResourceRetriever implements ResourceRetriever{
    
    /** Creates a new instance of FileResourceRetriever */
    public FileResourceRetriever() {
    }
    
    public boolean accept(String baseAddr, String currentAddr) throws URISyntaxException {
        
        URI currURI = new URI(currentAddr);
        if( (currURI.isAbsolute()) && (currURI.getScheme().equalsIgnoreCase("file"))) //NOI18N
            return true;
        if(!currURI.isAbsolute() && (baseAddr == null))
            return true;
        if(baseAddr != null){
            if(!currURI.isAbsolute()){
                URI baseURI = new URI(baseAddr);
                if(baseURI.getScheme().equalsIgnoreCase("file")) //NOI18N
                    return true;
            }
        }
        
        
        return false;
        
    }
    
    long streamLength = 0;
    public HashMap<String, InputStream> retrieveDocument(String baseAddress, String documentAddress) throws IOException,URISyntaxException{
        URI currURI = new URI(getEffectiveAddress(baseAddress, documentAddress));
        HashMap<String, InputStream> result = null;
        File curFile = new File(currURI);
        if(curFile.isFile()){
            InputStream is = new FileInputStream(curFile);
            result = new HashMap<String, InputStream>();
            result.put(curFile.toURI().toString(), is);
            streamLength = curFile.length();
            return result;
        }else{
            //file not found in the system
            throw new IOException("File not found: "+curFile.toString()); //NOI18N
        }
    }
    
    public long getStreamLength() {
        return streamLength;
    }
    
    public String getEffectiveAddress(String baseAddress, String documentAddress) throws IOException, URISyntaxException {
        URI currURI = new URI(documentAddress);
        if(currURI.isAbsolute()){
            //abs file URI
            return currURI.toString();
        }else{
            //relative URI
            if(baseAddress != null){
                URI baseURI = new URI(baseAddress);
                return (baseURI.resolve(currURI)).toString();
            }else{
                //neither the current URI nor the base URI are absoulte. So, can not resolve this
                //path
                return null;
            }
        }
    }
}
