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

import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class FromNode extends BpelNode<From> {
    
    public FromNode(From reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public FromNode(From reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.FROM;
    }

    public String getNameImpl() {
        From from = getReference();
        if (from == null) {
            return super.getNameImpl(); 
        }
        
        String stringFrom = ""; // NOI18N
        
        String plString = getPartnerLink();
        plString = plString == null ? "" : PARTNER_LINK_EQ+plString;// NOI18N
        stringFrom += plString;

        String varString = getVariable();
        varString = varString == null ? "" : VARIABLE_EQ+varString; // NOI18N
        stringFrom += varString;
        
        String partString = getPart();
        partString = partString == null ? "" : PART_EQ+partString; // NOI18N
        stringFrom += partString;

        stringFrom = stringFrom.length() == 0 ? from.getContent() : stringFrom;
        if (stringFrom != null && stringFrom.length() > MAX_CONTENT_NAME_LENGTH) {
            stringFrom = stringFrom.substring(0, MAX_CONTENT_NAME_LENGTH);
        }
        
        return stringFrom;
    }
    
    private String getPart() {
        From from = getReference();
        if (from == null) {
            return null; 
        }
    
        return from.getPart() == null ? null : from.getPart().getRefString();
    }
    
    private String getVariable() {
        From from = getReference();
        if (from == null) {
            return null; 
        }
    
        return from.getVariable() == null ? null : from.getVariable().getRefString();
    }
    
    private String getPartnerLink() {
        From from = getReference();
        if (from == null) {
            return null; 
        }
    
        return from.getPartnerLink() == null ? null : from.getPartnerLink().getRefString();
    }
}
