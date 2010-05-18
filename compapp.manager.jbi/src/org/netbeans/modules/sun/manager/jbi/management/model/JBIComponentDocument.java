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
    }

//    /**
//     * @return Returns the jbiComponentList.
//     */
//    public List<JBIComponentStatus> getJbiComponentList() {
//        return this.jbiComponentList;
//    }
//    /**
//     * @param jbiComponentList The jbiComponentList to set.
//     */
//    public void setJbiComponentList(List<JBIComponentStatus> jbiComponentList) {
//        this.jbiComponentList = jbiComponentList;
//    }

    public void dump() {
        Iterator<JBIComponentStatus> iterator = this.jbiComponentList.iterator();
        JBIComponentStatus component = null;
        while((iterator != null) && (iterator.hasNext() == true)) {
            component = iterator.next();
            component.dump();
        }
    }
}
