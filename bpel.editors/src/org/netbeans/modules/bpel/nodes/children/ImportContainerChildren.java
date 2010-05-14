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
