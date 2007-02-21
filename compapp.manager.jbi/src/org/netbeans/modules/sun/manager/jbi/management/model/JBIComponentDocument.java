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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sun.manager.jbi.management.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Retrieves the list of JBI Components like JBI Service Engines,
 * JBI Binding Components, and JBI Shared Libraries installed
 * on the JBI Container
 *
 * @author Graj
 */
public class JBIComponentDocument implements Serializable {

    public static final String COMP_INFO_LIST_NODE_NAME = "component-info-list"; // NOI18N
    public static final String COMP_INFO_NODE_NAME = "component-info"; // NOI18N
//    public static final String ID_NODE_NAME = "id";
    public static final String NAME_NODE_NAME = "name"; // NOI18N
    public static final String TYPE_NODE_NAME = "type"; // NOI18N
    public static final String STATUS_NODE_NAME = "state"; // NOI18N
    public static final String DESCRIPTION_NODE_NAME = "description"; // NOI18N
    public static final String VERSION_NODE_NAME = "version"; // NOI18N
    public static final String NAMESPACE_NODE_NAME = "xmlns"; // NOI18N

    List<JBIComponentStatus> jbiComponentList = new ArrayList<JBIComponentStatus>();


    /**
     *
     */
    public JBIComponentDocument() {
        super();
        // TODO Auto-generated constructor stub
    }



    /**
     * @return Returns the jbiComponentList.
     */
    public List<JBIComponentStatus> getJbiComponentList() {
        return this.jbiComponentList;
    }
    /**
     * @param jbiComponentList The jbiComponentList to set.
     */
    public void setJbiComponentList(List<JBIComponentStatus> jbiComponentList) {
        this.jbiComponentList = jbiComponentList;
    }

    public void dump() {
        Iterator<JBIComponentStatus> iterator = this.jbiComponentList.iterator();
        JBIComponentStatus component = null;
        while((iterator != null) && (iterator.hasNext() == true)) {
            component = iterator.next();
            component.dump();
        }
    }
    
    public static void main(String[] args) {
    }
}
