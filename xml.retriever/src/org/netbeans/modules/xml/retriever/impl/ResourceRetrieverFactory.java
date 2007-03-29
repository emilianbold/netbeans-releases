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
 * ResourceRetrieverFactory.java
 *
 * Created on January 9, 2006, 2:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.impl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.retriever.*;

/**
 *
 * @author girix
 */
public class ResourceRetrieverFactory {
    private static ArrayList<ResourceRetriever> registeredResourceRetrievers = new ArrayList<ResourceRetriever>();
    
    static{
        registeredResourceRetrievers.add(new FileResourceRetriever());
        registeredResourceRetrievers.add(new URLResourceRetriever());
        registeredResourceRetrievers.add(new SecureURLResourceRetriever());
    }
    
    public static ResourceRetriever getResourceRetriever(String baseAddress, String address) throws URISyntaxException{
        for(ResourceRetriever retriever: registeredResourceRetrievers){
            if(retriever.accept(baseAddress, address))
                return retriever;
        }
        return null;
    }
    
    public static List<ResourceRetriever>getRegisteredResourceRetrievers(){
        return (List<ResourceRetriever>) registeredResourceRetrievers;
    }
    
    public static boolean removeRegisteredResourceRetriever(ResourceRetriever oldResourceRetriever){
        return registeredResourceRetrievers.remove(oldResourceRetriever);
    }
    
    public static void addResourceRetriever(ResourceRetriever newResourceRetriever){
        registeredResourceRetrievers.add(newResourceRetriever);
    }
    
    
}
