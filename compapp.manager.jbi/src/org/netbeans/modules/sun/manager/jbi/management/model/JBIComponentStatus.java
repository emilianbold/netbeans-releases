/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.sun.manager.jbi.management.model;

import com.sun.jbi.ui.common.JBIComponentInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Retrieves the status of JBI components installed on the
 * JBI Container on the Server
 *
 * @author Graj
 */
public class JBIComponentStatus extends JBIComponentInfo {
    
    
    private List<String> namespaceList = new ArrayList<String>();

    /** Binding type */
    public static final String BINDING = "Binding";    // NOI18N
    
    /** Engine Type */
    public static final String ENGINE = "Engine";  // NOI18N
    
//    /** Namespace Type */
//    public static final String NAMESPACE = "Namespace";    // NOI18N
    

    public JBIComponentStatus() {
        super();
    }

//    /**
//     * @param componentId
//     * @param state
//     * @param name
//     * @param description
//     * @param type
//     */
//    public JBIComponentStatus(String componentId, String name, 
//            String description, String type, String state) {
//        this(componentId, name, description, type, state, null);
//    }
    
    public JBIComponentStatus(String name, 
            String description, String type, String state, String[] ns) {
        super(type, state, name, description);
        setNamespace(ns);
    }
        
    /**
     * DOCUMENT ME!
     *
     * @return the namespace.
     */
    public List<String> getNamespaceList() {
        return new ArrayList<String>(namespaceList);
    }
     
    /**
     * DOCUMENT ME!
     *
     * @param namesapce The namespace to set.
     */
    // RENAME ME 
    public void setNamespace(String namespaces[]) {
        namespaceList.clear();
        for (String namespace : namespaces) {
            if (!namespaceList.contains(namespace)) {
                namespaceList.add(namespace);
            }            
        }
    }
    
    // RENAME ME 
    public void setNamespace(List<String> namespaces) {
        namespaceList.clear();
        for (String namespace : namespaces) {
            if (!namespaceList.contains(namespace)) {
                namespaceList.add(namespace);
            }            
        }
    }
    
    public boolean addNamespace(String namespace) {
        if (!namespaceList.contains(namespace)) {
            namespaceList.add(namespace);
            return true;
        }      
        
        return false;
    }
    
    public boolean isBindingComponent() {
        return getType().equalsIgnoreCase(BINDING); 
    }
    
    public boolean isServiceEngine() {
        return getType().equalsIgnoreCase(ENGINE);
    }
    
    public boolean isSharedLibrary() {
        return getType().equalsIgnoreCase("shared-library"); // NOI18N
    }

    public void dump() {
        System.out.println("/////////////////////////////////////////////////"); // NOI18N
        System.out.println("//  -- JBI Component --                        //"); // NOI18N
        System.out.println("/////////////////////////////////////////////////"); // NOI18N
        //System.out.println("//  componentId is: "+ this.componentId);
        System.out.println("//  name is: "+ getName()); // NOI18N
        System.out.println("//  description is: "+ getDescription()); // NOI18N
        System.out.println("//  type is: "+ getType()); // NOI18N
        System.out.println("//  state is: "+ getState()); // NOI18N
        System.out.println("/////////////////////////////////////////////////"); // NOI18N
    }
}

