/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
  
    private Collection<SecurityMechanism> allSecurityMechs;
    
    private Collection<SecurityMechanism> allMsgLevelSecurityMechs;
    
    private Collection<SecurityMechanism> allWSPLibertySecurityMechs;
    
    private Collection<SecurityMechanism> allWSCLibertySecurityMechs;
    
    private Collection<SecurityMechanism> allWSPSecurityMechs;
    
    private Collection<SecurityMechanism> allWSCSecurityMechs;
    
    private SecurityMechanismRetriever retriever;
    
    public SecurityMechanismHelper(String id) {
        retriever = new SecurityMechanismRetriever(id);
    }
   
    /**
     * Returns all the security mechanism including Liberty.
     *
     */
    public Collection<SecurityMechanism> getAllSecurityMechanisms() {
        if (allSecurityMechs == null) {
            ArrayList<SecurityMechanism> list = new ArrayList<SecurityMechanism>();
            list.addAll(getAllMessageLevelSecurityMechanisms());
            list.addAll(getAllWSPLibertySecurityMechanisms());
            list.addAll(getAllWSCLibertySecurityMechanisms());
            
            allSecurityMechs = Collections.unmodifiableCollection(list);
        }
        
        return allSecurityMechs;
    }
    
    public Collection<SecurityMechanism> getAllMessageLevelSecurityMechanisms() {
        if (allMsgLevelSecurityMechs == null) {
            allMsgLevelSecurityMechs = retriever.getAllMessageLevelSecurityMechanism();
        }
        
        return allMsgLevelSecurityMechs;
    }
    
    public Collection<SecurityMechanism> getAllWSPLibertySecurityMechanisms() {
        if (allWSPLibertySecurityMechs == null) {
            Collection<SecurityMechanism> secMechs = retriever.getAllWSPLibertySecurityMechanisms();
            
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
            Collection<SecurityMechanism> secMechs = retriever.getAllWSCLibertySecurityMechanisms();
            
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
