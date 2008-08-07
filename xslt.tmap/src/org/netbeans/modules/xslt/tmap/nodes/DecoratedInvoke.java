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

package org.netbeans.modules.xslt.tmap.nodes;

import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class DecoratedInvoke extends DecoratedTMapComponentAbstract<Invoke>{

    public DecoratedInvoke(Invoke orig) {
        super(orig);
    }

    @Override
    public String getHtmlDisplayName() {
        Invoke ref = getOriginal();
        String pltName = null;
        String roleName = null;
        String opName = null;
        if (ref != null) {
            // 142908
            pltName = Util.getReferenceLocalName(ref.getPartnerLinkType());
            roleName = Util.getReferenceLocalName(ref.getRole());
//142908            pltName = Util.getReferenceLocalName(ref.getPortType());
            opName = Util.getReferenceLocalName(ref.getOperation());
        }
        String addon = null;
        if (pltName != null) {
            addon = TMapComponentNode.WHITE_SPACE+pltName; // NOI18N
        }
        
        // 142908
        if (roleName != null) {
            addon = (addon == null ? TMapComponentNode.EMPTY_STRING : addon+TMapComponentNode.WHITE_SPACE) + roleName; // NOI18N
        }

        if (opName != null) {
            addon = (addon == null ? TMapComponentNode.EMPTY_STRING : addon+TMapComponentNode.WHITE_SPACE) + opName; // NOI18N
        }

        return Util.getGrayString(super.getHtmlDisplayName(), addon);
    }

    @Override
    public String getTooltip() {
        Invoke ref = getOriginal();
        StringBuffer attributesTooltip = new StringBuffer();
        if (ref != null) {
            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getName()
                    , Invoke.NAME_PROPERTY));

            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getPartnerLinkType()
                    , Invoke.PARTNER_LINK_TYPE));

            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getRole()
                    , Invoke.ROLE_NAME));

// 142908            
//            attributesTooltip.append(
//                    Util.getLocalizedAttribute(ref.getPortType()
//                    , Invoke.PORT_TYPE));

            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getOperation()
                    , Invoke.OPERATION_NAME));
        }
        return  NbBundle.getMessage(TMapComponentNode.class, 
                "LBL_LONG_TOOLTIP_HTML_TEMPLATE", super.getName(), 
                attributesTooltip.toString());      
    }
}
