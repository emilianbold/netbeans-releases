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

package org.netbeans.modules.identity.profile.api.configurator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.identity.profile.api.configurator.impl.dynamic.SecurityMechanismRetriever;

/**
 * Helper class for retrieving various collections of SecurityMechanism
 * from the AM client sdk.
 *
 * Created on July 12, 2006, 11:58 AM
 *
 * @author ptliu
 */
public class SecurityMechanismHelper {
    
    private static final String LIBERTY_PREFIX = "Liberty";     //NOI18N
    
    private static SecurityMechanismHelper instance;
    
    private Collection<SecurityMechanism> allSecurityMechs;
    
    private Collection<SecurityMechanism> allMsgLevelSecurityMechs;
    
    private Collection<SecurityMechanism> allWSPLibertySecurityMechs;
    
    private Collection<SecurityMechanism> allWSCLibertySecurityMechs;
    
    private Collection<SecurityMechanism> allWSPSecurityMechs;
    
    private Collection<SecurityMechanism> allWSCSecurityMechs;
    
    private SecurityMechanismHelper() {
        
    }
    
    public static SecurityMechanismHelper getDefault() {
        if (instance == null) {
            instance = new SecurityMechanismHelper();
        }
        
        return instance;
    }
    
    /**
     * Returns all the security mechanism including Liberty.
     *
     */
    public Collection<SecurityMechanism> getAllSecurityMechanisms() {
        if (allSecurityMechs == null) {
            allSecurityMechs = new ArrayList<SecurityMechanism>();
            allSecurityMechs.addAll(getAllMessageLevelSecurityMechanisms());
            allSecurityMechs.addAll(getAllWSPLibertySecurityMechanisms());
            allSecurityMechs.addAll(getAllWSCLibertySecurityMechanisms());
            
            allSecurityMechs = Collections.unmodifiableCollection(allSecurityMechs);
        }
        
        return allSecurityMechs;
    }
    
    public Collection<SecurityMechanism> getAllMessageLevelSecurityMechanisms() {
        if (allMsgLevelSecurityMechs == null) {
            allMsgLevelSecurityMechs =
                    SecurityMechanismRetriever.getAllMessageLevelSecurityMechanism();
        }
        
        return allMsgLevelSecurityMechs;
    }
    
    public Collection<SecurityMechanism> getAllWSPLibertySecurityMechanisms() {
        if (allWSPLibertySecurityMechs == null) {
            Collection<SecurityMechanism> secMechs = 
                    SecurityMechanismRetriever.getAllWSPLibertySecurityMechanisms();
            
            allWSPLibertySecurityMechs = new ArrayList<SecurityMechanism>();
            
            for (SecurityMechanism secMech : secMechs) {
                if (secMech.getName().startsWith(LIBERTY_PREFIX)) {
                    allWSPLibertySecurityMechs.add(secMech);
                }
            }
            
            allWSPLibertySecurityMechs = Collections.unmodifiableCollection(
                    allWSPLibertySecurityMechs);
        }
        
        return allWSPLibertySecurityMechs;
    }
    
   public Collection<SecurityMechanism> getAllWSCLibertySecurityMechanisms() {
        if (allWSCLibertySecurityMechs == null) {
            Collection<SecurityMechanism> secMechs = 
                    SecurityMechanismRetriever.getAllWSCLibertySecurityMechanisms();
            
            allWSCLibertySecurityMechs = new ArrayList<SecurityMechanism>();
            
            for (SecurityMechanism secMech : secMechs) {
                if (secMech.getName().startsWith(LIBERTY_PREFIX)) {
                    allWSCLibertySecurityMechs.add(secMech);
                }
            }
            
            allWSCLibertySecurityMechs = Collections.unmodifiableCollection(
                    allWSCLibertySecurityMechs);
        }
        
        return allWSCLibertySecurityMechs;
    }
    
    public Collection<SecurityMechanism> getAllWSPSecurityMechanisms() {
        if (allWSPSecurityMechs == null) {
            allWSPSecurityMechs = new ArrayList<SecurityMechanism>();
            allWSPSecurityMechs.addAll(getAllMessageLevelSecurityMechanisms());
            allWSPSecurityMechs.addAll(getAllWSPLibertySecurityMechanisms());
            
            allWSPSecurityMechs = Collections.unmodifiableCollection(allWSPSecurityMechs);
        }
        
        return allWSPSecurityMechs;
    }

     public Collection<SecurityMechanism> getAllWSCSecurityMechanisms() {
        if (allWSCSecurityMechs == null) {
            allWSCSecurityMechs = new ArrayList<SecurityMechanism>();
            allWSCSecurityMechs.addAll(getAllMessageLevelSecurityMechanisms());
            allWSCSecurityMechs.addAll(getAllWSCLibertySecurityMechanisms());
            
            allWSCSecurityMechs = Collections.unmodifiableCollection(allWSCSecurityMechs);
        }
        
        return allWSCSecurityMechs;
    }
     
     
    public Collection<String> getSecurityMechanismURIs(Collection<SecurityMechanism> secMechs) {
        Collection<String> uris = new ArrayList<String>();
        
        if (secMechs != null) {
            for (SecurityMechanism secMech : secMechs) {
                uris.add(secMech.getURI());
            }
        }
        
        return uris;
    }
    
    public Collection<SecurityMechanism> getSecurityMechanismsFromURIs(Collection<String> uris) {
        Collection<SecurityMechanism> secMechs = new ArrayList<SecurityMechanism>();
        Collection<SecurityMechanism> allMechs = getAllSecurityMechanisms();
        
        if (uris != null) {
            for (String uri : uris) {
                for (SecurityMechanism mech : allMechs) {
                    if (uri.equals(mech.getURI())) {
                        secMechs.add(mech);
                        break;
                    }
                }
            }
        }
        
        return secMechs;
    }
    
    public Collection<String> getSecurityMechanismURIsFromNames(Collection<String> secMechNames) {
        Collection<String> uris = new ArrayList<String>();
        Collection<SecurityMechanism> allMechs = getAllSecurityMechanisms();
        
        for (String name : secMechNames) {
            for (SecurityMechanism mech : allMechs) {
                if (name.equals(mech.getName())) {
                    uris.add(mech.getURI());
                }
            }
        }
        
        return uris;
    }
}
