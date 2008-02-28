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
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.support.Roles;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface DecorationProvider<T extends Object> {
    
    String getTooltip(NodeType nodeType, T component);

    class Util {
        private Util() {
        }
        
        public static String getFromLabel(From from) {
            String stringFrom = null;
            
            stringFrom =  from.getVariable() == null ? null : from.getVariable().getRefString();
            
            stringFrom = stringFrom != null ? stringFrom :
                from.getPartnerLink() != null ? from.getPartnerLink().getRefString()
                : null;
            
            stringFrom = stringFrom != null ? stringFrom :
                from.getContent() == null ? null
                    : from.getContent().length() < 20 ? from.getContent() : BpelNode.EXP_LABEL;
            
            return stringFrom;
        }
        
        public static String getToLabel(To to) {
            String stringTo = null;
            
            stringTo = to.getVariable() == null ? null : to.getVariable().getRefString();
            stringTo = stringTo != null ? stringTo
                    : to.getPartnerLink() == null ? null : to.getPartnerLink().getRefString();
            
            stringTo = stringTo != null ? stringTo :
                to.getContent() == null ? null
                    : to.getContent().length() < 20 ? to.getContent() : BpelNode.QUERY_LABEL;
            return stringTo;
        }
        
        public static String getEndpointReferenceLabelPart(From from) {
            StringBuffer labelStr = new StringBuffer();
            Roles roles = from.getEndpointReference();
            if (roles != null) {
                labelStr.append(BpelNode.WHITE_SPACE).append(BpelNode.ENDPOINT_REFERENCE).append(BpelNode.EQUAL_SIGN);
                labelStr.append(roles.toString());
            }
            
            return labelStr.toString();
        }
    }
    
}
