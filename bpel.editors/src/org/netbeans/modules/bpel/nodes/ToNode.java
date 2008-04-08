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
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.Variable;
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
public class ToNode extends BpelNode<To>{
    
    public ToNode(To reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public ToNode(To reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.TO;
    }
    
    public String getNameImpl() {
        To to = getReference();
        if (to == null) {
            return super.getNameImpl();
        }
        
        String stringTo = ""; // NOI18N
        
        String plString = getPartnerLink();
        plString = plString == null ? "" : PARTNER_LINK_EQ+plString;// NOI18N
        stringTo += plString;

        String varString = getVariable();
        varString = varString == null ? "" : VARIABLE_EQ+varString;// NOI18N
        stringTo += varString;
        
        String partString = getPart();
        partString = partString == null ? "" : PART_EQ+partString;// NOI18N
        stringTo += partString;

        stringTo = stringTo.length() == 0 ? to.getContent() : stringTo;
        if (stringTo != null && stringTo.length() > MAX_CONTENT_NAME_LENGTH) {
            stringTo = stringTo.substring(0, MAX_CONTENT_NAME_LENGTH);
        }
        
        return stringTo;
    }
    
    private String getPart() {
        To to = getReference();
        if (to == null) {
            return null; 
        }
    
        return to.getPart() == null ? null : to.getPart().getRefString();
    }
    
    private String getVariable() {
        To to = getReference();
        if (to == null) {
            return null; 
        }
    
        return to.getVariable() == null ? null : to.getVariable().getRefString();
    }
    
    private String getPartnerLink() {
        To to = getReference();
        if (to == null) {
            return null; 
        }
    
        return to.getPartnerLink() == null ? null : to.getPartnerLink().getRefString();
    }
}
