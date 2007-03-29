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
 * ResourceRetriever.java
 *
 * Created on January 9, 2006, 2:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.netbeans.modules.xml.retriever.*;

/**
 *
 * @author girix
 */
public interface ResourceRetriever {
    /**
     * This method will be called by the factory class. Impl should decide if it supports the protocol
     * @param baseAddr originating documents address
     * @param currentAddr address of the document that needs to be fetched - as mentioned in the base document.
     * @return if impl supports then true else false
     */
    public boolean accept(String baseAddr, String currentAddr) throws URISyntaxException;
    
    /**
     * Given the base doc address and the current address (that could be either relative or absoulte), determine the final address to fetch doc and get the stream of that doc.
     * @param baseAddress address of the base document where the link was found
     * @param documentAddress current document address as mentioned in the base doc
     * @return Hash map has the "key" as the final address from where the file was fetched and "value" has the input sream of the file.
     */
    public HashMap<String,InputStream> retrieveDocument(
            String baseAddress, String documentAddress) throws IOException,URISyntaxException;
    
    
     /**
     * Given the base doc address and the current address (that could be either relative or absoulte), determine the final address to fetch doc and get the stream of that doc.
     * @param baseAddress address of the base document where the link was found
     * @param documentAddress current document address as mentioned in the base doc
     * @return Hash map has the "key" as the final address from where the file was fetched and "value" has the input sream of the file.
     */
    public String getEffectiveAddress(
            String baseAddress, String documentAddress) throws IOException,URISyntaxException;
    
    
    /**
     * Must be called after retrieveDocument() method. 
     * This returns the number of chars in the stream. 
     * Useful for URL retriever where its better to know the length upfront.
     */
    public long getStreamLength();
}
