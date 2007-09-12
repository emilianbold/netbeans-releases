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

package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.nodes.navigator.ImportComparator;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 6 April 2006 
 *
 */
public class ImportContainerChildren extends BpelNodeChildren<Process> {
    
    public ImportContainerChildren(Process entity, Lookup contextLookup) {
        super( entity, contextLookup );
    }

    public Collection getNodeKeys() {
        Process ref = getReference();
        if (ref == null) {
            return Collections.EMPTY_LIST;
        }

        List<BpelEntity> childs = new ArrayList<BpelEntity>();
        
        // set import(schema, wsdl) nodes
        Import[] imports = ref.getImports();
        Arrays.sort(imports, new ImportComparator(getLookup()));
        
        if (imports != null && imports.length > 0) {
            childs.addAll(Arrays.asList(imports));
        }
        
        return childs;
    }
    
    protected Node[] createNodes(Object object) {
        if (object == null) {
            return new Node[0];
        }
//        NavigatorTreesNodeFactory factory
//                = NavigatorTreesNodeFactory.getInstance();
        NavigatorNodeFactory factory
                = NavigatorNodeFactory.getInstance();
        Node childNode = null;
        
        // create variable container node
        if (object instanceof Import ) {
            if (isWsdl((Import)object)) {
                childNode = factory.createNode(
                        NodeType.IMPORT_WSDL
                        ,(Import)object
                        ,getLookup());
            } else if (isSchema((Import)object)) {
                childNode = factory.createNode(
                        NodeType.IMPORT_SCHEMA
                        ,(Import)object
                        ,getLookup());
            }  else {
                // TODO
                // now don't show unknown node 
            }
        }
        
        return childNode == null ? new Node[0] : new Node[] {childNode};
    }
 
    private boolean isWsdl(Import imprt) {
        return imprt.getImportType() != null 
            && imprt.getImportType().equals(Import.WSDL_IMPORT_TYPE); // NOI18N
    }

    private boolean isSchema(Import imprt) {
        return imprt.getImportType() != null 
            && imprt.getImportType().equals(Import.SCHEMA_IMPORT_TYPE); // NOI18N
    }

}
