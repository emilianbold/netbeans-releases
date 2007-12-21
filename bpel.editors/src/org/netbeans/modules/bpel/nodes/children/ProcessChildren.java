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
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.netbeans.modules.bpel.nodes.navigator.NoneSchemaNodeFilter;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 18 January 2006
 */
public class ProcessChildren extends org.netbeans.modules.bpel.nodes.navigator.BaseScopeChildren {
    
    public ProcessChildren(Process process, Lookup contextLookup) {
        super(process, new ExtendedLookup(contextLookup,new NoneSchemaNodeFilter()));
    }
    
    public Collection getNodeKeys() {
        assert getReference() instanceof Process;
        Process process = (Process)getReference();
        if (process == null) {
            return Collections.EMPTY_LIST;
        }
        
        ArrayList<Object> processChilds = new ArrayList<Object>();
        Collection baseNodes = super.getNodeKeys();
        
        if (baseNodes != null) {
            processChilds.addAll(baseNodes);
        }
        
        //set PartnerLink nodes
        PartnerLinkContainer plCont = process.getPartnerLinkContainer();
        if (plCont != null) {
            PartnerLink[] pls = plCont.getPartnerLinks();
            if (pls != null && pls.length > 0) {
                processChilds.addAll(Arrays.asList(pls));
            }
        }
        
        // set import container node
        processChilds.add(Import.class);
        
        return processChilds;
    }
    
    protected Node[] createNodes(Object object) {
        if (object == null) {
            return new Node[0];
        }
        Node[] childNodes = super.createNodes(object);
        if (childNodes != null && childNodes.length > 0) {
            return childNodes;
        }
        
        Node childNode = null;
        
        // create variable container node
        if (object == Import.class) {
//            childNode = NavigatorTreesNodeFactory.getInstance().createNode(
            childNode = NavigatorNodeFactory.getInstance().createNode(
                    NodeType.IMPORT_CONTAINER
                    ,getReference()
                    ,getLookup());
        } else  if (object instanceof BpelEntity) {
            childNode = NavigatorNodeFactory.getInstance()
            .createNode((BpelEntity)object,getLookup());
        }
        
        return childNode == null ? new Node[0] : new Node[] {childNode};
    }
    
}
