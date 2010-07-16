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
package org.netbeans.modules.bpel.nodes;

import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.ext.Extensions;
import org.netbeans.modules.bpel.properties.BpelStandardFaults;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.editors.controls.filter.PreferredFaultFilter;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class FaultNode extends BpelNode<QName> {
    
    public FaultNode(QName reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public FaultNode(QName reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.FAULT;
    }
    
    @Override
    protected String getNameImpl() {
        String name = null;
        QName ref = getReference();
        String namespace = ref.getNamespaceURI();
        if (BpelStandardFaults.BPEL_2_0_NS.equals(namespace)) {
            name = ref.getLocalPart();
        } else if (Extensions.ERROR_HANDLER_URI.equals(namespace)) {
            name = ref.getLocalPart();
        } else {
            name = ResolverUtility.qName2DisplayText(ref);
        }
        //
        return (name != null) ? name : "";
    }
    
    @Override
    protected String getImplHtmlDisplayName() {
        String result = super.getImplHtmlDisplayName();
        PreferredFaultFilter filter = (PreferredFaultFilter)getLookup().
                lookup(PreferredFaultFilter.class);
        if (filter != null) {
            //
            // This case is used when the Fault Name chooser is called from
            // the Reply property dialog
            QName faultName = getReference();
            if (filter.isFaultPreferred(faultName)) {
                result = EditorUtil.getAccentedString(result);
            }
        }
        //
        return result;
    }
    
}
